package com.nanda.common.crypto;

public interface CryptoService {

    String encrypt(String plain, EncryptLevel level);

    String decrypt(String cipher, EncryptLevel level);

    String hashForIndex(String plain, String salt);

    String mask(String plain, String maskType);
}
