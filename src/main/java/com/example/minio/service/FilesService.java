package com.example.minio.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.minio.common.result.Result;
import com.example.minio.entity.apps.Apps;
import com.example.minio.entity.files.Files;
import com.example.minio.entity.files.ShareFile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * @author zhy
 * @email 2434017367@qq.com
 * @date 2022/6/4 18:24
 */
public interface FilesService extends IService<Files> {

    /**
     * 根据应用id和文件id获取文件信息
     * @param appId
     * @param fileId
     * @return
     */
    Files getFiles(String appId, String fileId);

    /**
     * 文件上传
     * @param apps
     * @param path
     * @param multipartFile
     * @return
     */
    String upload(Apps apps, String path, MultipartFile multipartFile);

    /**
     * 文件上传
     * @param apps
     * @param path
     * @param fileName  文件名
     * @param inputStream  文件流
     * @return
     */
    String upload(Apps apps, String path, String fileName, InputStream inputStream, long byteLength);

    /**
     * url文件上传
     * @param apps
     * @param path
     * @param filename
     * @param fileurl
     * @return
     */
    String uploadUrl(Apps apps, String path, String filename, String fileurl);

    /**
     * 删除文件
     * @param fileId
     */
    void delFile(Apps apps, String fileId) throws Exception;

    /**
     * 获取文件信息列表
     * @param appId
     * @param idList
     * @return
     */
    List<Files> getListByIdList(String appId, List<String> idList);

    List<Files> getListByIds(String appId, String ids);

    /**
     * 清除临时文件
     * @return
     */
    void clearInterim(Apps apps) throws Exception;

    /**
     * 解析文件名称
     * @param filename
     * @return
     */
    Files parseFilename(String filename);

    /**
     * 获取文件分享链接
     * @param apps
     * @param fileId
     * @param second
     * @return
     */
    ShareFile getShareFile(Apps apps, String fileId, long second);

    /**
     * 获取跳过认证文件上传的上传链接
     * @param path
     * @return
     */
    String getSkipAuthFileUploadUrl(Apps apps, String path);

}
