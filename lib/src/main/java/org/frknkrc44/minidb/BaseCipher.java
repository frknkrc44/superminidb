package org.frknkrc44.minidb;

abstract class BaseCipher {
    protected final byte[] mKey;
    public BaseCipher(byte[] key) {
        mKey = key;
    }
    public abstract byte[] encode(byte[] input);
    public abstract byte[] decode(byte[] input);
    public String encodeStr(String input) {
        return new String(encode(input.getBytes()));
    }

    public String decodeStr(String input) {
        return new String(decode(input.getBytes()));
    }
}
