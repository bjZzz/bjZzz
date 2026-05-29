package com.nanda.common.crypto;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Service
public class CryptoServiceImpl implements CryptoService {

    private static final String AES_KEY = "NandaDevKey16Byte";

    @Override
    public String encrypt(String plain, EncryptLevel level) {
        if (plain == null || level == EncryptLevel.L0) {
            return plain;
        }
        try {
            SecretKeySpec key = new SecretKeySpec(AES_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return Base64.getEncoder().encodeToString(cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("encrypt failed", e);
        }
    }

    @Override
    public String decrypt(String cipherText, EncryptLevel level) {
        if (cipherText == null || level == EncryptLevel.L0) {
            return cipherText;
        }
        try {
            SecretKeySpec key = new SecretKeySpec(AES_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decoded = Base64.getDecoder().decode(cipherText);
            return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("decrypt failed", e);
        }
    }

    @Override
    public String hashForIndex(String plain, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String input = plain + (salt == null ? "" : salt);
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("hash failed", e);
        }
    }

    @Override
    public String mask(String plain, String maskType) {
        if (plain == null || plain.length() <= 2) {
            return "***";
        }
        return plain.charAt(0) + "***" + plain.charAt(plain.length() - 1);
    }
}
