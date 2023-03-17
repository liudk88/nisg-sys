package com.hcxinan.sys.util;

import java.security.MessageDigest;

public class MD5Util {

    public static String passwordEncrypt(String password) {
        String md5 = encrypt(password.getBytes());
        return encrypt((md5 + "IgtUdEQJyVevaCxQnY").getBytes());
    }

    private static String encrypt(byte[] source) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(source);
            StringBuffer buf = new StringBuffer();
            byte[] var3 = md.digest();
            int var4 = var3.length;

            for (int var5 = 0; var5 < var4; ++var5) {
                byte b = var3[var5];
                buf.append(String.format("%02x", b & 255));
            }
            return buf.toString();
        } catch (Exception var7) {
            var7.printStackTrace();
            return null;
        }
    }

}
