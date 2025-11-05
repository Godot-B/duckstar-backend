package com.duckstar.web.controller;

import com.sksamuel.scrimage.ImmutableImage;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/images")
public class ImageController {

    @Operation(summary = "Open Graph 이미지 변환 API", description = "WebP 이미지를 JPG 또는 PNG로 변환하여 반환 (OG 태그용)")
    @GetMapping("/og")
    public ResponseEntity<byte[]> convertForOpenGraph(
            @RequestParam String url,
            @RequestParam(defaultValue = "jpg") String format,
            @RequestParam(required = false) Integer width,
            @RequestParam(required = false) Integer height
    ) throws IOException {
        // 지원 형식 검증
        List<String> supportedFormats = Arrays.asList("jpg", "jpeg", "png");
        String lowerFormat = format.toLowerCase();
        if (!supportedFormats.contains(lowerFormat)) {
            return ResponseEntity.badRequest().build();
        }

        // 이미지 다운로드
        ImmutableImage image;
        try (InputStream inputStream = new URL(url).openStream()) {
            image = ImmutableImage.loader().fromStream(inputStream);
        }

        // 리사이즈 (선택적)
        if (width != null && height != null) {
            image = image.scaleTo(width, height);
        } else if (width != null) {
            // width만 지정된 경우 비율 유지
            double scale = (double) width / image.width;
            int newHeight = (int) (image.height * scale);
            image = image.scaleTo(width, newHeight);
        } else if (height != null) {
            // height만 지정된 경우 비율 유지
            double scale = (double) height / image.height;
            int newWidth = (int) (image.width * scale);
            image = image.scaleTo(newWidth, height);
        }

        // OG 이미지 최적 크기 (1200x630)로 리사이즈 (지정되지 않은 경우)
        if (width == null && height == null) {
            int targetWidth = 1200;
            int targetHeight = 630;
            double widthScale = (double) targetWidth / image.width;
            double heightScale = (double) targetHeight / image.height;
            double scale = Math.max(widthScale, heightScale); // 비율 유지하며 최소 크기 보장
            
            int newWidth = (int) (image.width * scale);
            int newHeight = (int) (image.height * scale);
            image = image.scaleTo(newWidth, newHeight);
        }

        // BufferedImage로 변환
        BufferedImage bufferedImage = image.awt();
        
        // 바이트 배열로 변환
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String imageFormat = lowerFormat.equals("jpeg") ? "jpg" : lowerFormat;
        ImageIO.write(bufferedImage, imageFormat, baos);
        byte[] imageBytes = baos.toByteArray();

        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        MediaType mediaType = lowerFormat.equals("png") 
            ? MediaType.IMAGE_PNG 
            : MediaType.IMAGE_JPEG;
        headers.setContentType(mediaType);
        headers.setContentLength(imageBytes.length);
        headers.setCacheControl("public, max-age=86400"); // 1일 캐시

        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }
}

