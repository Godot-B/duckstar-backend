package com.duckstar.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * GIF 파일의 첫 번째 프레임을 추출하여 정적 이미지로 변환하는 유틸리티
 */
@Component
public class GifFrameExtractor {

    /**
     * GIF 파일의 첫 번째 프레임을 추출하여 PNG 형식의 MultipartFile로 반환
     * @param gifFile - GIF MultipartFile
     * @return PNG 형식의 MultipartFile (첫 번째 프레임)
     * @throws IOException - 이미지 처리 중 오류 발생 시
     */
    public MultipartFile extractFirstFrame(MultipartFile gifFile) throws IOException {
        if (!isGifFile(gifFile)) {
            throw new IllegalArgumentException("GIF 파일이 아닙니다.");
        }

        try (InputStream inputStream = gifFile.getInputStream();
             ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream)) {

            // GIF 이미지 리더 생성
            ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
            reader.setInput(imageInputStream);

            // 첫 번째 프레임 읽기
            BufferedImage firstFrame = reader.read(0);

            if (firstFrame == null) {
                throw new IOException("GIF의 첫 번째 프레임을 읽을 수 없습니다.");
            }

            // BufferedImage를 PNG 바이트 배열로 변환
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(firstFrame, "png", baos);
            byte[] pngBytes = baos.toByteArray();

            // 원본 파일명에서 확장자를 png로 변경
            String originalFilename = gifFile.getOriginalFilename();
            String filenameWithoutExt = originalFilename.substring(0, originalFilename.lastIndexOf('.'));
            String newFilename = filenameWithoutExt + "_frame.png";

            // PNG MultipartFile 생성
            return new PngMultipartFile(pngBytes, newFilename, "image/png");

        } catch (Exception e) {
            throw new IOException("GIF 프레임 추출 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 파일이 GIF인지 확인
     * @param file - 확인할 MultipartFile
     * @return boolean
     */
    public boolean isGifFile(MultipartFile file) {
        if (file == null || file.getContentType() == null) {
            return false;
        }
        return file.getContentType().equals("image/gif") || 
               (file.getOriginalFilename() != null && 
                file.getOriginalFilename().toLowerCase().endsWith(".gif"));
    }

    /**
     * PNG 형식의 MultipartFile 구현체
     */
    private static class PngMultipartFile implements MultipartFile {
        private final byte[] content;
        private final String filename;
        private final String contentType;

        public PngMultipartFile(byte[] content, String filename, String contentType) {
            this.content = content;
            this.filename = filename;
            this.contentType = contentType;
        }

        @Override
        public String getName() {
            return "image";
        }

        @Override
        public String getOriginalFilename() {
            return filename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() throws IOException {
            return content;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new java.io.ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(dest)) {
                fos.write(content);
            }
        }
    }
}
