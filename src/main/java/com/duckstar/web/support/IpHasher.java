package com.duckstar.web.support;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.VoteHandler;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class IpHasher {
    private final byte[] key;
    private final boolean useHex; // true면 HEX, false면 Base64

    public IpHasher(byte[] key, boolean useHex) {
        this.key = key;
        this.useHex = useHex;
    }

    public String hash(String ip) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            byte[] macBytes = mac.doFinal(ip.getBytes(StandardCharsets.UTF_8));
            return useHex ? toHex(macBytes) : Base64.getUrlEncoder().withoutPadding().encodeToString(macBytes);
        } catch (Exception e) {
            throw new VoteHandler(ErrorStatus.HASH_IP_FAILED);
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
