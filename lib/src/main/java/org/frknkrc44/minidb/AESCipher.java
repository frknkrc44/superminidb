package org.frknkrc44.minidb;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

final class AESCipher extends BaseCipher {
    private final int AES_KEY_SIZE = 256;
    private final int GCM_TAG_LENGTH = AES_KEY_SIZE / 2;
    private final byte[] iv;
    private final SecretKeySpec secretKey;

    public AESCipher(byte[] key) throws InvalidKeySpecException, NoSuchAlgorithmException {
        super(key);

        if (key.length != AES_KEY_SIZE) {
            byte[] nKey = new byte[AES_KEY_SIZE];

            if (key.length < AES_KEY_SIZE) {
                for (int i = 0; i < nKey.length; i += key.length) {
                    int copyRange = key.length;

                    if (i + key.length >= nKey.length) {
                        break;
                    }

                    System.arraycopy(key, 0, nKey, i, copyRange);
                }
            } else {
                System.arraycopy(key, 0, nKey, 0, nKey.length);
            }

            key = nKey;
        }

        SecureRandom secureRandom = new SecureRandom();
        iv = new byte[12 /* GCM_IV_LENGTH */];
        secureRandom.nextBytes(iv);

        PBEKeySpec pbeKeySpec = new PBEKeySpec(
                toCharArray(key), iv, 65536, 256);
        SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        key = f.generateSecret(pbeKeySpec).getEncoded();

        secretKey = new SecretKeySpec(key, "AES");
    }

    private char[] toCharArray(byte[] array) {
        char[] out = new char[array.length];
        for (int i = 0; i < array.length; i++) {
            out[i] = (char) array[i];
        }
        return out;
    }

    @Override
    public byte[] encode(byte[] input) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec);

            return Base64Provider.encode(cipher.doFinal(input));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] decode(byte[] input) {
        try {
            input = Base64Provider.decode(input);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);

            return cipher.doFinal(input);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
