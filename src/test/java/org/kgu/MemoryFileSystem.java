package org.kgu;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;
import java.util.regex.Pattern;

/** 저장 테스트가 실제 세이브·설정·임시 파일을 만들지 않도록 하는 테스트 전용 파일 시스템. */
final class MemoryFileSystem extends FileSystem {
    final Map<String, byte[]> files = new HashMap<>();
    final Set<String> directories = new HashSet<>(Set.of("/"));
    boolean failWrites;
    private final Provider provider = new Provider();
    private boolean open = true;

    @Override public FileSystemProvider provider() { return provider; }
    @Override public void close() { open = false; }
    @Override public boolean isOpen() { return open; }
    @Override public boolean isReadOnly() { return false; }
    @Override public String getSeparator() { return "/"; }
    @Override public Iterable<Path> getRootDirectories() { return List.of(getPath("/")); }
    @Override public Iterable<FileStore> getFileStores() { return List.of(); }
    @Override public Set<String> supportedFileAttributeViews() { return Set.of("basic"); }
    @Override public Path getPath(String first, String... more) { return new MemoryPath(Path.of(first, more)); }
    @Override public PathMatcher getPathMatcher(String syntaxAndPattern) {
        int colon = syntaxAndPattern.indexOf(':');
        if (colon < 0 || !syntaxAndPattern.startsWith("regex:")) { throw new UnsupportedOperationException("regex only"); }
        Pattern regex = Pattern.compile(syntaxAndPattern.substring(colon + 1));
        return path -> regex.matcher(path.toString()).matches();
    }
    @Override public UserPrincipalLookupService getUserPrincipalLookupService() { throw new UnsupportedOperationException(); }
    @Override public WatchService newWatchService() { throw new UnsupportedOperationException(); }

    private String key(Path path) { return Path.of("/").resolve(((MemoryPath) path).delegate).normalize().toString(); }

    private final class MemoryPath implements Path {
        private final Path delegate;
        private MemoryPath(Path delegate) { this.delegate = delegate; }
        private Path wrap(Path path) { return path == null ? null : new MemoryPath(path); }
        private Path unwrap(Path path) { return ((MemoryPath) path).delegate; }
        @Override public FileSystem getFileSystem() { return MemoryFileSystem.this; }
        @Override public boolean isAbsolute() { return delegate.isAbsolute(); }
        @Override public Path getRoot() { return wrap(delegate.getRoot()); }
        @Override public Path getFileName() { return wrap(delegate.getFileName()); }
        @Override public Path getParent() { return wrap(delegate.getParent()); }
        @Override public int getNameCount() { return delegate.getNameCount(); }
        @Override public Path getName(int index) { return wrap(delegate.getName(index)); }
        @Override public Path subpath(int begin, int end) { return wrap(delegate.subpath(begin, end)); }
        @Override public boolean startsWith(Path other) { return other instanceof MemoryPath && delegate.startsWith(unwrap(other)); }
        @Override public boolean endsWith(Path other) { return other instanceof MemoryPath && delegate.endsWith(unwrap(other)); }
        @Override public Path normalize() { return wrap(delegate.normalize()); }
        @Override public Path resolve(Path other) { return wrap(delegate.resolve(unwrap(other))); }
        @Override public Path relativize(Path other) { return wrap(delegate.relativize(unwrap(other))); }
        @Override public URI toUri() { return URI.create("memory:" + toAbsolutePath()); }
        @Override public Path toAbsolutePath() { return wrap(Path.of("/").resolve(delegate)); }
        @Override public Path toRealPath(LinkOption... options) throws IOException { provider.checkAccess(this); return toAbsolutePath().normalize(); }
        @Override public WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) { throw new UnsupportedOperationException(); }
        @Override public Iterator<Path> iterator() {
            Iterator<Path> iterator = delegate.iterator();
            return new Iterator<>() {
                @Override public boolean hasNext() { return iterator.hasNext(); }
                @Override public Path next() { return wrap(iterator.next()); }
            };
        }
        @Override public int compareTo(Path other) { return delegate.compareTo(unwrap(other)); }
        @Override public String toString() { return delegate.toString(); }
        @Override public boolean equals(Object other) { return other instanceof MemoryPath && ((MemoryPath) other).getFileSystem() == MemoryFileSystem.this && delegate.equals(((MemoryPath) other).delegate); }
        @Override public int hashCode() { return Objects.hash(System.identityHashCode(MemoryFileSystem.this), delegate); }
    }

    private final class Provider extends FileSystemProvider {
        @Override public String getScheme() { return "memory"; }
        @Override public FileSystem newFileSystem(URI uri, Map<String, ?> env) { return MemoryFileSystem.this; }
        @Override public FileSystem getFileSystem(URI uri) { return MemoryFileSystem.this; }
        @Override public Path getPath(URI uri) { return getFileSystem(uri).getPath(uri.getPath()); }

        @Override public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options,
                                                           FileAttribute<?>... attributes) throws IOException {
            String name = key(path);
            boolean write = options.contains(StandardOpenOption.WRITE) || options.contains(StandardOpenOption.APPEND);
            if (write && failWrites) { throw new IOException("Simulated write failure"); }
            if (directories.contains(name)) { throw new FileSystemException(name, null, "Is a directory"); }
            if (!files.containsKey(name)) {
                if (!write || !(options.contains(StandardOpenOption.CREATE) || options.contains(StandardOpenOption.CREATE_NEW))) {
                    throw new NoSuchFileException(name);
                }
                if (!directories.contains(key(path.toAbsolutePath().getParent()))) { throw new NoSuchFileException("Parent directory"); }
                files.put(name, new byte[0]);
            } else if (options.contains(StandardOpenOption.CREATE_NEW)) { throw new FileAlreadyExistsException(name); }
            if (write && options.contains(StandardOpenOption.TRUNCATE_EXISTING)) { files.put(name, new byte[0]); }
            return new SeekableByteChannel() {
                private int position = options.contains(StandardOpenOption.APPEND) ? files.get(name).length : 0;
                private boolean active = true;
                @Override public int read(ByteBuffer destination) throws IOException {
                    ensureOpen(); byte[] data = files.get(name);
                    if (position >= data.length) { return -1; }
                    int count = Math.min(destination.remaining(), data.length - position);
                    destination.put(data, position, count); position += count; return count;
                }
                @Override public int write(ByteBuffer source) throws IOException {
                    ensureOpen(); if (!write) { throw new java.nio.channels.NonWritableChannelException(); }
                    if (failWrites) { throw new IOException("Simulated write failure"); }
                    int count = source.remaining();
                    byte[] data = Arrays.copyOf(files.get(name), Math.max(files.get(name).length, position + count));
                    source.get(data, position, count); position += count; files.put(name, data); return count;
                }
                @Override public long position() throws IOException { ensureOpen(); return position; }
                @Override public SeekableByteChannel position(long value) throws IOException { ensureOpen(); position = Math.toIntExact(value); return this; }
                @Override public long size() throws IOException { ensureOpen(); return files.get(name).length; }
                @Override public SeekableByteChannel truncate(long size) throws IOException {
                    ensureOpen(); if (!write) { throw new java.nio.channels.NonWritableChannelException(); }
                    if (size < files.get(name).length) { files.put(name, Arrays.copyOf(files.get(name), Math.toIntExact(size))); }
                    position = Math.min(position, Math.toIntExact(size)); return this;
                }
                @Override public boolean isOpen() { return active; }
                @Override public void close() { active = false; }
                private void ensureOpen() throws IOException { if (!active) { throw new java.nio.channels.ClosedChannelException(); } }
            };
        }
        @Override public void createDirectory(Path path, FileAttribute<?>... attributes) throws IOException {
            String name = key(path);
            if (failWrites) { throw new IOException("Simulated write failure"); }
            if (directories.contains(name) || files.containsKey(name)) { throw new FileAlreadyExistsException(name); }
            if (!directories.contains(key(path.toAbsolutePath().getParent()))) { throw new NoSuchFileException(name); }
            directories.add(name);
        }
        @Override public DirectoryStream<Path> newDirectoryStream(Path directory, DirectoryStream.Filter<? super Path> filter) throws IOException {
            checkAccess(directory);
            List<Path> children = new ArrayList<>();
            Set<String> names = new HashSet<>(directories); names.addAll(files.keySet());
            for (String name : names) {
                Path child = getPath(URI.create("memory:" + name));
                if (Objects.equals(child.getParent(), directory.toAbsolutePath()) && filter.accept(child)) { children.add(child); }
            }
            return new DirectoryStream<>() {
                @Override public Iterator<Path> iterator() { return children.iterator(); }
                @Override public void close() { }
            };
        }
        @Override public void delete(Path path) throws IOException {
            String name = key(path);
            if (files.remove(name) == null && !directories.remove(name)) { throw new NoSuchFileException(name); }
        }
        @Override public void copy(Path source, Path target, CopyOption... options) { throw new UnsupportedOperationException(); }
        @Override public void move(Path source, Path target, CopyOption... options) { throw new UnsupportedOperationException(); }
        @Override public boolean isSameFile(Path a, Path b) { return key(a).equals(key(b)); }
        @Override public boolean isHidden(Path path) { return false; }
        @Override public FileStore getFileStore(Path path) { throw new UnsupportedOperationException(); }
        @Override public void checkAccess(Path path, AccessMode... modes) throws IOException {
            String name = key(path);
            if (!files.containsKey(name) && !directories.contains(name)) { throw new NoSuchFileException(name); }
            if (failWrites && Arrays.asList(modes).contains(AccessMode.WRITE)) { throw new AccessDeniedException(name); }
        }
        @Override public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) { return null; }
        @Override public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
            if (type != BasicFileAttributes.class) { throw new UnsupportedOperationException(); }
            checkAccess(path); String name = key(path); boolean directory = directories.contains(name);
            BasicFileAttributes attributes = new BasicFileAttributes() {
                @Override public FileTime lastModifiedTime() { return FileTime.fromMillis(0); }
                @Override public FileTime lastAccessTime() { return FileTime.fromMillis(0); }
                @Override public FileTime creationTime() { return FileTime.fromMillis(0); }
                @Override public boolean isRegularFile() { return !directory; }
                @Override public boolean isDirectory() { return directory; }
                @Override public boolean isSymbolicLink() { return false; }
                @Override public boolean isOther() { return false; }
                @Override public long size() { return directory ? 0 : files.get(name).length; }
                @Override public Object fileKey() { return name; }
            };
            return type.cast(attributes);
        }
        @Override public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) { throw new UnsupportedOperationException(); }
        @Override public void setAttribute(Path path, String attribute, Object value, LinkOption... options) { throw new UnsupportedOperationException(); }
    }
}
