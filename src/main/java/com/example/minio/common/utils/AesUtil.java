package com.example.minio.common.utils;

import lombok.extern.log4j.Log4j2;
import cn.hutool.core.codec.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author zhy
 * @email 2434017367@qq.com
 * @date 2022/7/15 10:50
 */
@Log4j2
public class AesUtil {

    // 字符集
    private final static String ENCODING = "utf-8";

    /**
     * 加密
     * @param key 16位
     * @param text
     * @return
     * @throws Exception
     */
    public static String encode(String key, String text) throws Exception {
        byte[] encryptData = {};
        try {
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            encryptData = cipher.doFinal(text.getBytes(ENCODING));
        } catch (Exception e) {
            log.error("aesEncodeErr :调用encode失败! ", e);
            throw e;
        }
        String encode = Base64.encode(encryptData);
        return encode;
    }

    /**
     * 解密
     * @param key 16位
     * @param text
     * @return
     * @throws Exception
     */
    public static String decode(String key, String text) throws Exception {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] bOut = cipher.doFinal(Base64.decode(text));
            text = new String(bOut);
        } catch (Exception e) {
            log.error("aesDecodeErr :调用decode失败!", e);
            throw e;
        }
        return text;
    }


}
