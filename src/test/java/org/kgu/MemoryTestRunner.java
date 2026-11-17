package org.kgu;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.tools.*;

/** quiz에서 java --source 17 src/test/java/org/kgu/MemoryTestRunner.java 로 실행한다. */
public final class MemoryTestRunner {
    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) { throw new IllegalStateException("테스트에는 JDK 17 이상이 필요합니다."); }
        Map<String, ByteArrayOutputStream> classes = new HashMap<>();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager standard = compiler.getStandardFileManager(diagnostics, null, null)) {
            JavaFileManager memory = new ForwardingJavaFileManager<StandardJavaFileManager>(standard) {
                @Override
                public JavaFileObject getJavaFileForOutput(Location location, String name,
                                                          JavaFileObject.Kind kind, FileObject sibling) {
                    return new SimpleJavaFileObject(URI.create("memory:///" + name.replace('.', '/') + kind.extension), kind) {
                        @Override public OutputStream openOutputStream() {
                            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                            classes.put(name, bytes);
                            return bytes;
                        }
                    };
                }
            };
            List<File> sources;
            try (var paths = Files.walk(Path.of("src"))) {
                sources = paths.filter(path -> path.toString().endsWith(".java")).map(Path::toFile).toList();
            }
            boolean compiled = compiler.getTask(null, memory, diagnostics, List.of("--release", "17", "-proc:none"),
                    null, standard.getJavaFileObjectsFromFiles(sources)).call();
            diagnostics.getDiagnostics().forEach(System.err::println);
            if (!compiled) { throw new IllegalStateException("메모리 컴파일 실패"); }
            System.out.println("PASS: " + sources.size() + " Java sources compiled in memory (--release 17).");
            URL[] resources = {Path.of("src/main/resources").toUri().toURL()};
            try (URLClassLoader loader = new URLClassLoader(resources, ClassLoader.getPlatformClassLoader()) {
                @Override protected Class<?> findClass(String name) throws ClassNotFoundException {
                    ByteArrayOutputStream bytes = classes.get(name);
                    if (bytes == null) { throw new ClassNotFoundException(name); }
                    byte[] code = bytes.toByteArray();
                    return defineClass(name, code, 0, code.length);
                }
            }) {
                try {
                    loader.loadClass("org.kgu.IntegrationChecks").getMethod("main", String[].class)
                            .invoke(null, (Object) new String[0]);
                } catch (InvocationTargetException e) {
                    throw new AssertionError("통합 검증 실패", e.getCause());
                }
            }
        }
    }
}
