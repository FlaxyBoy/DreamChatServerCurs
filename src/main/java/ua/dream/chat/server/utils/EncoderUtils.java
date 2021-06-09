package ua.dream.chat.server.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class EncoderUtils {

    private EncoderUtils() {
    }

    public static byte[] toSha256(String password){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(password.getBytes(StandardCharsets.UTF_8));
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
