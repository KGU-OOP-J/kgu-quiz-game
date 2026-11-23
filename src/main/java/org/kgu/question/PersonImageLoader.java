
package org.kgu.ui.question;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

public final class PersonImageLoader {

    private PersonImageLoader() {}

    // 난이도를 사진 번호로 변환
    private static String getLevel(String difficulty) {
        if ("하".equals(difficulty) || "쉬움".equals(difficulty)) return "1";
        if ("중".equals(difficulty) || "보통".equals(difficulty)) return "2";
        if ("상".equals(difficulty) || "어려움".equals(difficulty)) return "3";
        return "1";
    }

    // 인물 이름과 난이도로 사진 불러오기
    public static BufferedImage load(String personName, String difficulty) {
        if (personName == null || personName.isBlank()) return null;

        String fileName = personName.trim()
                + "_" + getLevel(difficulty) + ".png";

        String path = "/images/" + fileName;

        try {
            URL url = PersonImageLoader.class.getResource(path);

            if (url == null) {
                System.err.println("사진을 찾을 수 없습니다: " + path);
                return null;
            }

            return ImageIO.read(url);

        } catch (IOException e) {
            System.err.println("사진 로드 실패: " + path);
            e.printStackTrace();
            return null;
        }
    }
}
