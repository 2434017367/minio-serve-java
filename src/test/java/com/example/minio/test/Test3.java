package com.example.minio.test;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
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
import java.util.HashMap;
import java.util.Map;

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
            if (i == 1) {
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
            int c = (int) m.charAt(i);
            int y = c + pyl;
            if (y < 48) {
                y = 123 - (48 - y);
            } else if (y > 57 && y < 65) {
                y += 7;
            } else if (y > 90 && y < 97) {
                y += 6;
            } else if (y > 122) {
                y = y - 122 + 47;
            }
            mj.append((char) y);
        }
        System.out.println("mj = " + mj);

    }

    /**
     sjc = 81LL8KLLoKL8
     pyl = 9
     dca6d7dc781LL8KLLoKL8da342af9a7afb1eb3b95626
     mj = mljFmGmlGAAUUATUUxTUAmjCDBjoBjGjokAnkCkBEFBF
     */
    @Test
    public void Test4(){
        String timeStamp = "81LL8KLLoKL8";
        String miyao = "mljFmGmlGAAUUATUUxTUAmjCDBjoBjGjokAnkCkBEFBF";

        String[] cs = new String[]{"o", "8", "L", "A", "e", "5", "K", "i", "y", "1"};

        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < cs.length; i++) {
            map.put(cs[i], i);
        }
        // 计算偏移量
        int pyl = 0;
        StringBuffer time = new StringBuffer();
        for (int i = 0; i < timeStamp.length(); i++) {
            char c = timeStamp.charAt(i);
            Integer integer = map.get(Character.toString(c));
            if (i == 1) {
                pyl = integer;
            }
            time.append(integer);
        }

        // 获得原始密钥
        StringBuffer ysmy = new StringBuffer();
        for (int i = 0; i < miyao.length(); i++) {
            int c = (int)miyao.charAt(i);
            int y = c - pyl;
            if (y < 48) {
                y = 123 - (48 - y);
            } else if (y > 57 && y < 65) {
                y -= 7;
            } else if (y > 90 && y < 97) {
                y -= 6;
            } else if (y > 122) {
                y = y - 122 + 47;
            }
            ysmy.append((char) y);
        }

        // 获得appkey
        String appkey = ysmy.toString().replace(timeStamp, "");

        System.out.println("appkey = " + appkey);
    }

    @Test
    public void test6() {
        String m = "123456789";
        int pyl = 4;

        StringBuffer mj = new StringBuffer();
        for (int i = 0; i < m.length(); i++) {
            int c = (int)m.charAt(i);
            int y = c + pyl;
            if (y < 48) {
                y = 123 - (48 - y);
            } else if (y > 57 && y < 65) {
                y += 7;
            } else if (y > 90 && y < 97) {
                y += 6;
            } else if (y > 122) {
                y = y - 122 + 47;
            }
            mj.append((char) y);
        }
        System.out.println("mj = " + mj);
    }

    @Test
    public void test7() {
        String m = "56789ABCD";
        int pyl = 4;

        StringBuffer mj = new StringBuffer();
        for (int i = 0; i < m.length(); i++) {
            int c = (int)m.charAt(i);
            int y = c - pyl;
            if (y < 48) {
                y = 123 - (48 - y);
            } else if (y > 57 && y < 65) {
                y += 7;
            } else if (y > 90 && y < 97) {
                y += 6;
            } else if (y > 122) {
                y = y - 122 + 47;
            }
            mj.append((char) y);
        }
        System.out.println("mj = " + mj);
    }

    @Test
    public void Test5() {


        for (int i = 0; i < 4; i++) {
            for (int j = 5-i; j > 0; j--) {
                System.out.print(" ");
            }
            for (int j = 1; j <= 2 * i + 1; j++) {
                System.out.print(j);
            }
            System.out.println();
        }
    }



}
