// Copyright (C) 2023 frknkrc44 <krc440002@gmail.com>
//
// This file is part of SuperMiniDB project,
// and licensed under GNU Affero General Public License v3.
// See the GNU Affero General Public License for more details.
//
// All rights reserved. See COPYING, AUTHORS.
//

package org.frknkrc44.minidb;

/*
 ----------------------------
  This DB project is started
     by Furkan Karcıoğlu
        25.08.2017 Fri
 ----------------------------
  Minimized version started
      at 23.06.2019 Sun
 ----------------------------
*/

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executors;

@SuppressWarnings({"ResultOfMethodCallIgnored", "unused"})
public class SuperMiniDB {

    public static final String QUERY_RULE_STARTS_WITH = "SW",
            QUERY_RULE_ENDS_WITH = "EW",
            QUERY_RULE_EQUALS = "EQ",
            QUERY_RULE_CONTAINS = "CT";
    private final HashMap<String, String> hm1 = new HashMap<>();
    private boolean ready = false;
    private File folder;
    private BaseCipher cipher = null;
    private LogProvider logProvider = null;

    /**
     * Initialize the DB with YACipher and default log provider
     * @param dbName Database name
     * @param path Database path
     * @param notRead Don't read the database content, just initialize it.
     *                You will need to read the key contents manually.
     */
    public SuperMiniDB(String dbName, File path, boolean notRead) {
        this(dbName, path, notRead, false);
    }

    /**
     * Initialize the DB with YACipher
     * @param dbName Database name
     * @param path Database path
     * @param notRead Don't read the database content, just initialize it.
     *                You will need to read the key contents manually.
     * @param provider Log provider
     */
    public SuperMiniDB(String dbName, File path, boolean notRead, LogProvider provider) {
        this(dbName, path, notRead, false, provider);
    }

    /**
     * Initialize the DB with AESCipher/YACipher and default log provider
     * @param dbName Database name
     * @param path Database path
     * @param notRead Don't read the database content, just initialize it.
     *                You will need to read the key contents manually.
     * @param useAes Use AESCipher instead of YACipher (more secure but slow)
     */
    public SuperMiniDB(String dbName, File path, boolean notRead, boolean useAes) {
        this(dbName, path, notRead, useAes, null);
    }

    /**
     * Initialize the DB with AESCipher/YACipher
     * @param dbName Database name
     * @param path Database path
     * @param notRead Don't read the database content, just initialize it.
     *                You will need to read the key contents manually.
     * @param useAes Use AESCipher instead of YACipher (more secure but slow)
     * @param provider Log provider
     */
    public SuperMiniDB(String dbName, File path, boolean notRead, boolean useAes, LogProvider provider) {
        init(dbName, path, useAes, provider);
        if (!notRead) {
            readAll();
        }
    }

    /**
     * Initialize the DB with YACipher async and default log provider
     * @param dbName Database name
     * @param path Database path
     * @param onDBLoadFinished Callback function which executed after init
     */
    public SuperMiniDB(String dbName, File path, Runnable onDBLoadFinished) {
        this(dbName, path, onDBLoadFinished, null);
    }

    /**
     * Initialize the DB with YACipher async
     * @param dbName Database name
     * @param path Database path
     * @param onDBLoadFinished Callback function which executed after init
     * @param provider Log provider
     */
    public SuperMiniDB(String dbName, File path, Runnable onDBLoadFinished, LogProvider provider) {
        this(dbName, path, onDBLoadFinished, false, provider);
    }

    /**
     * İnitialize the DB with AESCipher/YACipher async and default log provider
     * @param dbName Database name
     * @param path Database path
     * @param onDBLoadFinished Callback function which executed after init
     * @param useAes Use AESCipher instead of YACipher (more secure but slow)
     */
    public SuperMiniDB(String dbName, File path, Runnable onDBLoadFinished, boolean useAes) {
        this(dbName, path, onDBLoadFinished, useAes, null);
    }

    /**
     * İnitialize the DB with AESCipher/YACipher async
     * @param dbName Database name
     * @param path Database path
     * @param onDBLoadFinished Callback function which executed after init
     * @param useAes Use AESCipher instead of YACipher (more secure but slow)
     * @param provider Log provider
     */
    public SuperMiniDB(String dbName, File path, Runnable onDBLoadFinished, boolean useAes, LogProvider provider) {
        init(dbName, path, useAes, provider);
        readAllAsync(onDBLoadFinished);
    }

    private void init(String dbName, File path, boolean useAes, LogProvider provider) {
        logProvider = provider == null ? new DefaultLogProvider() : provider;

        if (useAes) {
            try {
                logProvider.onLog("Trying to init AESCipher...");
                cipher = new AESCipher(dbName.getBytes());
            } catch (Throwable ignored) {
                logProvider.onLog("!!! AESCipher init failed, falling back to YACipher !!!");
            }
        }

        if (cipher == null) {
            try {
                logProvider.onLog("Trying to init YACipher...");
                cipher = new YACipher(dbName.getBytes());
            } catch (Throwable ignored) {
                logProvider.onLog("!!! YACipher init failed, storing files insecurely !!!");
            }
        }

        folder = new File(path + File.separator + "smdb" + File.separator + dbName);
        if (!folder.exists()) folder.mkdirs();
    }

    public final int getLength() {
        return hm1.size();
    }

    public final boolean isDBContainsKey(String key) {
        return hm1.containsKey(key);
    }

    public final String[] getStringArray(String key, String[] def) {
        String s = getString(key, null);
        if (s != null) {
            try {
                return s.split(String.valueOf((char) 1));
            } catch (Throwable ignored) {}
        }

        return def;
    }

    public final String getString(String key, String def) {
        return isDBContainsKey(key) ? decode(hm1.get(key)) : def;
    }

    public final long getLong(String key, long def) {
        String s = getString(key, String.valueOf(def));
        try {
            return Long.parseLong(s);
        } catch (Throwable ignored) {
            return def;
        }
    }

    public final byte getByte(String key, byte def) {
        String s = getString(key, String.valueOf(def));
        try {
            return Byte.parseByte(s);
        } catch (Throwable ignored) {
            return def;
        }
    }

    public final int getInteger(String key, int def) {
        String s = getString(key, String.valueOf(def));
        try {
            return Integer.parseInt(s);
        } catch (Throwable ignored) {
            return def;
        }
    }

    public final float getFloat(String key, float def) {
        String s = getString(key, String.valueOf(def));
        try {
            return Float.parseFloat(s);
        } catch (Throwable ignored) {
            return def;
        }
    }

    public final double getDouble(String key, double def) {
        String s = getString(key, String.valueOf(def));
        try {
            return Double.parseDouble(s);
        } catch (Throwable ignored) {
            return def;
        }
    }

    public final boolean getBoolean(String key, boolean def) {
        String s = getString(key, String.valueOf(def));
        try {
            return Boolean.parseBoolean(s);
        } catch (Throwable ignored) {
            return def;
        }
    }

    public final void putStringArray(String key, String[] value) {
        putStringArray(key, value, false);
    }

    public final void putStringArray(String key, String[] value, boolean permanent) {
        StringBuilder stringBuilder = new StringBuilder();

        for (String item : value)
            stringBuilder.append(item).append((char) 1);

        stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
        putString(key, stringBuilder.toString(), permanent);
    }

    public final void putString(String key, String value) {
        putString(key, value, false);
    }

    public final void putString(String key, String value, boolean permanent) {
        hm1.put(key, encode(value));
        if (permanent) writeKey(key);
    }

    public final void putLong(String key, long value) {
        putLong(key, value, false);
    }

    public final void putLong(String key, long value, boolean permanent) {
        putString(key, String.valueOf(value), permanent);
    }

    public final void putByte(String key, byte value) {
        putByte(key, value, false);
    }

    public final void putByte(String key, byte value, boolean permanent) {
        putString(key, String.valueOf(value), permanent);
    }

    public final void putInteger(String key, int value) {
        putInteger(key, value, false);
    }

    public final void putInteger(String key, int value, boolean permanent) {
        putString(key, String.valueOf(value), permanent);
    }

    public final void putFloat(String key, float value) {
        putFloat(key, value, false);
    }

    public final void putFloat(String key, float value, boolean permanent) {
        putString(key, String.valueOf(value), permanent);
    }

    public final void putDouble(String key, double value) {
        putDouble(key, value, false);
    }

    public final void putDouble(String key, double value, boolean permanent) {
        putString(key, String.valueOf(value), permanent);
    }

    public final void putBoolean(String key, boolean value) {
        putBoolean(key, value, false);
    }

    public final void putBoolean(String key, boolean value, boolean permanent) {
        putString(key, String.valueOf(value), permanent);
    }

    public final Map<String, String> getDatabaseDump() {
        return hm1;
    }

    public final void putDatabaseDump(Map<String, String> dump) {
        hm1.clear();
        hm1.putAll(dump);
    }

    public final void removeKeyFromDB(String key) {
        hm1.remove(key);
        new File(folder + File.separator + key).delete();
    }

    public final void removeDB() {
        hm1.clear();
        removeRecursive(folder);
    }

    private void removeRecursive(File f) {
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            if (files != null) {
                for (File g : files) {
                    removeRecursive(g);
                }
            }
        }
        f.delete();
    }

    public final void clearRAM() {
        hm1.clear();
    }

    public final boolean isRAMClean() {
        return hm1.isEmpty();
    }

    public final void exportToDir(File dir) {
        if (dir.isDirectory()) {
            writeAll(dir);
        } else {
            throw new RuntimeException(dir + " is not a directory");
        }
    }

    public final void refresh() {
        writeAll();
        readAll();
    }

    public void writeKey(String key) {
        writeKey(folder, key);
    }

    private void writeKey(File dir, String key) {
        FileOutputStream os = null;

        try {
            key = getValidKey(key);

            if (!key.isEmpty()) {
                File tf = new File(dir + File.separator + key);
                os = new FileOutputStream(tf);
                os.write(hm1.get(key).getBytes());
                os.flush();
            }
        } catch (Throwable ignored) {}

        try {
            if (os != null) {
                os.close();
            }
        } catch (Throwable ignored) {}
    }

    public void writeAll() {
        writeAll(folder);
    }

    private void writeAll(File dir) {
        for (String key : getKeys()) {
            writeKey(dir, key);
        }
    }

    public void writeAllAsync(Runnable onFinished) {
        Executors.newSingleThreadExecutor().execute(() -> {
            synchronized (SuperMiniDB.this) {
                ready = false;
                writeAll();
                ready = true;
                onFinished.run();
            }
        });
    }

    private String getValidKey(String key) {
        if (key == null) {
            return "";
        }

        key = key.trim();

        if (key.length() > 128) {
            String key1 = key.substring(0, 64);
            key1 += key.substring(key.length() - 64);
            key = key1;
        }

        return key;
    }

    public final void readKey(String key) {
        try {
            key = getValidKey(key);

            parseValues(new File(folder + File.separator + key));
        } catch (Throwable ignored) {}
    }

    public void readAll() {
        hm1.clear();
        try {
            if (folder.exists()) {
                for (File f : Objects.requireNonNull(folder.listFiles())) {
                    parseValues(f);
                }
            } else folder.mkdirs();
        } catch (Throwable ignored) {}
    }

    public void readAllAsync(Runnable onFinished) {
        Executors.newSingleThreadExecutor().execute(() -> {
            synchronized (SuperMiniDB.this) {
                ready = false;
                readAll();
                ready = true;
                onFinished.run();
            }
        });
    }

    public final void refreshKey(String key) {
        writeKey(key);
        readKey(key);
    }

    private void parseValues(File f) throws FileNotFoundException {
        StringBuilder sq = new StringBuilder();
        Scanner sc = new Scanner(f);
        while (sc.hasNext()) sq.append(sc.nextLine());
        sc.close();
        hm1.put(f.getName(), sq.toString());
    }

    private String encode(String in) {
        if (cipher == null) return in;

        return cipher.encodeStr(in);
    }

    private String decode(String in) {
        if (cipher == null) return in;

        return cipher.decodeStr(in);
    }

    public String[] getKeys() {
        return getKeys(false);
    }

    public String[] getKeys(boolean descending) {
        return getKeys(false, descending);
    }

    public String[] getKeys(boolean sort, boolean descending) {
        Set<String> keySet = hm1.keySet();
        String[] ref = new String[0];

        if (!sort) {
            return keySet.toArray(ref);
        }

        TreeSet<String> treeSet = new TreeSet<>(keySet);
        return descending
                ? treeSet.descendingSet().toArray(ref)
                : treeSet.toArray(ref);
    }

    /*	DB QUERY
     *
     * 	SW=xxx key starts with
     * 	EW=xxx key ends with
     * 	EQ=xxx key equals to
     * 	CT=xxx key contains
     */
    public Map<String, String> query(String rule) {
        if (rule == null || !rule.contains("="))
            throw new RuntimeException("rule must be non-null and must contain = as delimiter");
        String[] ruleArr = rule.split("=", 2);
        if (ruleArr.length != 2 || ruleArr[0].trim().isEmpty() || ruleArr[1].trim().isEmpty()) {
            throw new RuntimeException("invalid rule");
        }
        List<String> keys = new ArrayList<>(hm1.keySet());
        Map<String, String> out = new HashMap<>();
        switch (ruleArr[0].trim()) {
            case QUERY_RULE_STARTS_WITH:
                for (String key : keys) {
                    if (key.startsWith(ruleArr[1].trim())) {
                        out.put(key, getString(key, ""));
                    }
                }
                break;
            case QUERY_RULE_ENDS_WITH:
                for (String key : keys) {
                    if (key.endsWith(ruleArr[1].trim())) {
                        out.put(key, getString(key, ""));
                    }
                }
                break;
            case QUERY_RULE_EQUALS:
                for (String key : keys) {
                    if (key.equals(ruleArr[1].trim())) {
                        out.put(key, getString(key, ""));
                    }
                }
                break;
            case QUERY_RULE_CONTAINS:
                for (String key : keys) {
                    if (key.contains(ruleArr[1].trim())) {
                        out.put(key, getString(key, ""));
                    }
                }
                break;
            default:
                throw new RuntimeException("invalid rule");
        }
        return out;
    }

    private static class DefaultLogProvider implements LogProvider {
        @Override
        public void onLog(String text) {
            System.out.println(text);
        }
    }
}
