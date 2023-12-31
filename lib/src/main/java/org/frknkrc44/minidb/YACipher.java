// Copyright (C) 2023 frknkrc44 <krc440002@gmail.com>
//
// This file is part of SuperMiniDB project,
// and licensed under GNU Affero General Public License v3.
// See the GNU Affero General Public License for more details.
//
// All rights reserved. See COPYING, AUTHORS.
//

package org.frknkrc44.minidb;

import java.security.spec.InvalidKeySpecException;

final class YACipher extends BaseCipher {
    private final char mSplitKey = 0x1B;

    public YACipher(byte[] key) {
        super(key);
    }

    @Override
    public byte[] encode(byte[] input) {
        return encode(input, false);
    }

    private byte[] encode(byte[] input, boolean raw) {
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < mKey.length; j++) {
                input[i] ^= mKey[j] ^ i ^ j;
            }
        }

        if (!raw) {
            return toHex(input);
        }

        return input;
    }

    @Override
    public byte[] decode(byte[] input) {
        return encode(fromHex(input), true);
    }

    private byte[] fromHex(byte[] input) {
        String str = new String(input);
        String[] split = str.split(String.valueOf(mSplitKey));

        byte[] output = new byte[split.length];
        for(int i = 0; i < split.length; i++) {
            output[i] = (byte) Long.parseLong(split[i], 16);
        }

        return output;
    }

    private byte[] toHex(byte[] input) {
        StringBuilder stringBuilder = new StringBuilder();

        for (byte item : input) {
            stringBuilder.append(Integer.toHexString(item));
            stringBuilder.append(mSplitKey);
        }

        stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
        return stringBuilder.toString().getBytes();
    }
}
