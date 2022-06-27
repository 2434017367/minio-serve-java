package com.example.minio.common.utils;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.example.minio.common.config.AppConfig;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.List;

/**
 * @author zhy
 * @email 2434017367@qq.com
 * @date 2022/6/11 14:04
 */
@Log4j2
@Component
public class FileUtils {

    @Autowired
    private AppConfig appConfig;

    /**
     * 临时文件目录绝对地址
     */
    private String interimPath;

    @PostConstruct
    public void init(){
        String interimPath = appConfig.getInterimPath();
        log.info("interimPath:{}", interimPath);
        File interimPathFile = new File(interimPath);
        if (!interimPathFile.exists()) {
            interimPathFile.mkdirs();
        }
        this.interimPath = interimPathFile.getAbsolutePath();
    }

    /**
     * 获取临时文件地址
     * @return
     */
    public String getInterimFilePath() {
        return getInterimFilePath(IdUtil.simpleUUID());
    }

    public String getInterimFilePath(String s) {
        return packStorPath(this.interimPath, s);
    }

    /**
     * 获取路径
     * @param paths
     * @return
     */
    private String packStorPath(String... paths){
        String p = "";
        for (String s : paths) {
            if (StrUtil.isEmpty(s)){
                continue;
            }
            p += (s + "/");
        }
        p = p.replaceAll("//", "/");
        if (p.lastIndexOf("/") == p.length() - 1){
            p = p.substring(0, p.length() - 1);
        }
        return p;
    }

    /**
     * 获取文件名称和后缀
     * @param s
     * @return
     */
    public static String[] getFileNameSuffix(String s) {
        List<String> split = StrUtil.split(s, '.');
        if (split.size() > 1) {
            return new String[]{split.get(0), split.get(split.size() - 1)};
        } else {
            return new String[]{split.get(0)};
        }
    }

}
