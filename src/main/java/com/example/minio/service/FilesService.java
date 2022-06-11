package com.example.minio.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.minio.common.result.Result;
import com.example.minio.entity.apps.Apps;
import com.example.minio.entity.files.Files;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author zhy
 * @email 2434017367@qq.com
 * @date 2022/6/4 18:24
 */
public interface FilesService extends IService<Files> {

    /**
     * 文件上传
     * @param apps
     * @param path
     * @param multipartFile
     * @return
     */
    Result upload(Apps apps, String path, MultipartFile multipartFile);

    /**
     * 删除文件
     * @param fileId
     */
    Result delFile(Apps apps, String fileId);

    /**
     * 获取文件信息列表
     * @param idList
     * @return
     */
    List<Files> getListByIdList(List<String> idList);

    List<Files> getListByIds(String ids);

}
