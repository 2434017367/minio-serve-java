package com.example.minio.test;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import org.junit.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.text.DateFormat;
import java.util.Date;

/**
 * @author zhy
 * @email 2434017367@qq.com
 * @date 2022/6/16 21:57
 */
public class Test3 {

    @Test
    public void test() {
        byte[] key = SecureUtil.generateKey(SymmetricAlgorithm.DESede.getValue()).getEncoded();
        System.out.println(key);
        SymmetricCrypto des = new SymmetricCrypto(SymmetricAlgorithm.DESede, key);

        String s = IdUtil.simpleUUID();
        System.out.println(s);

        //加密
        byte[] encrypt = des.encrypt(s);
        System.out.println(encrypt);

        //解密
        byte[] decrypt = des.decrypt(encrypt);
        System.out.println(decrypt);
    }

    @Test
    public void test2() {
        String content = "test中文";

//        byte[] key = SecureUtil.generateKey(SymmetricAlgorithm.DESede.getValue()).getEncoded();
        String format = DateUtil.format(new Date(), DatePattern.PURE_DATE_PATTERN);
        System.out.println("format = " + format);

        byte[] key = SecureUtil.generateKey(format).getEncoded();

        SymmetricCrypto des = new SymmetricCrypto(SymmetricAlgorithm.DESede, key);

        //加密
        byte[] encrypt = des.encrypt(content);
        //解密
        byte[] decrypt = des.decrypt(encrypt);

        //加密为16进制字符串（Hex表示）
        String encryptHex = des.encryptHex(content);
        System.out.println("encryptHex = " + encryptHex);

        //解密为字符串
        String decryptStr = des.decryptStr(encryptHex);
        System.out.println("decryptStr = " + decryptStr);
    }

    private static final String appKey = "dca6d7dc7da342af9a7afb1eb3b95626";

    @Test
    public void test3(){
        String[] cs = new String[]{"o", "8", "L", "A", "e", "5", "K", "i", "y", "1"};
        String s = DateUtil.format(new Date(), "ssyyHHmmMMdd");
        StringBuffer sb = new StringBuffer();
        int pyl = 0;
        for (int i = 0; i < s.length(); i++) {
            Integer index = Integer.parseInt(s.substring(i, i + 1));
            if (i == 0) {
                pyl = index;
            }
            sb.append(cs[index]);
        }
        String sjc = sb.toString();
        System.out.println("sjc = " + sjc);
        System.out.println("pyl = " + pyl);

        String m = appKey.substring(0, pyl) + sjc + appKey.substring(pyl);
        System.out.println(m);

        StringBuffer mj = new StringBuffer();
        for (int i = 0; i < m.length(); i++) {
            char c = (char)((int)m.charAt(i) + pyl);
            mj.append(c);
        }
        System.out.println("mj = " + mj);

    }




}
