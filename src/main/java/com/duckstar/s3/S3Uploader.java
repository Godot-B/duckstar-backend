package com.duckstar.s3;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.ImageHandler;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.webp.WebpWriter;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3Uploader {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String uploadWithUUID(MultipartFile file, String dir) {
        validateImageFile(file);

        String ext = FilenameUtils.getExtension(file.getOriginalFilename());
        String fileName = dir + "/" + UUID.randomUUID() + "_" + ext;

        try (InputStream inputStream = file.getInputStream()) {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(fileName)
                            .contentType(file.getContentType())
                            .acl(ObjectCannedACL.PUBLIC_READ)
                            .build(),
                    RequestBody.fromInputStream(inputStream, file.getSize())
            );
        } catch (IOException e) {
            throw new ImageHandler(ErrorStatus.S3_FILE_UPLOAD_FAILURE);
        }

        return "https://" + bucket + "/" + fileName;
    }

    private void validateImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ImageHandler(ErrorStatus.INVALID_IMAGE_FILE);
        }

        String ext = FilenameUtils.getExtension(file.getOriginalFilename());
        List<String> allowedExt = List.of("jpg", "jpeg", "png", "gif", "webp");
        List<String> allowedMime = List.of("image/jpeg", "image/png", "image/gif", "image/webp");

        boolean validExt = allowedExt.contains(ext);
        boolean validMime = allowedMime.contains(contentType);

        if (!validExt || !validMime) {
            throw new ImageHandler(ErrorStatus.UNSUPPORTED_IMAGE_EXTENSION);
        }

        try (InputStream is = file.getInputStream()) {
            BufferedImage image = ImageIO.read(is);
            if (image == null) {
                throw new ImageHandler(ErrorStatus.INVALID_IMAGE_FILE);
            }
        } catch (IOException e) {
            throw new ImageHandler(ErrorStatus.INVALID_IMAGE_FILE);
        }
    }

    public void delete(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith("https://" + bucket + "/")) {
            return;
        }

        String key = extractKeyFromUrl(imageUrl);
        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build()
        );
    }

    private String extractKeyFromUrl(String imageUrl) {
        String bucketUrl = "https://" + bucket + "/";
        if (!imageUrl.startsWith(bucketUrl)) {
            throw new ImageHandler(ErrorStatus.INVALID_S3_IMAGE_URL);
        }

        return imageUrl.substring(bucketUrl.length());
    }

    public File createThumbnail(File original, int minWidth, int minHeight) throws IOException {
        // 원본 이미지 로드
        ImmutableImage img = ImmutableImage.loader().fromFile(original);

        int ow = img.width;
        int oh = img.height;

        // 1. 최소 배율 계산
        double scaleW = (double) minWidth / ow;
        double scaleH = (double) minHeight / oh;

        // 2. 둘 중 더 큰 배율 선택 → 최소 크기 보장
        double scale = Math.max(scaleW, scaleH);

        int tw = (int) Math.round(ow * scale);
        int th = (int) Math.round(oh * scale);

        // 3. 리사이즈 & WebP 변환
        ImmutableImage thumb = img.cover(tw, th);

        File thumbFile = new File(original.getParent(), "thumb.webp");
        WebpWriter writer = WebpWriter.DEFAULT.withQ(80); // 품질 80%
        thumb.output(writer, thumbFile);

        return thumbFile;
    }

    public String uploadWithKey(File file, String s3Key) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(s3Key)
                .contentType("image/webp")
                .cacheControl("public, max-age=31536000")
                .build();

        s3Client.putObject(request, RequestBody.fromFile(file));

        return "https://" + bucket + "/" + s3Key;
    }
}
