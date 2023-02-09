package com.example.minio.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.minio.common.config.AppConfig;
import com.example.minio.common.enums.FilePathEnum;
import com.example.minio.common.enums.FileTypeEnum;
import com.example.minio.common.exception.RRException;
import com.example.minio.common.utils.AesUtil;
import com.example.minio.common.utils.AppKeyUtils;
import com.example.minio.common.utils.FileUtils;
import com.example.minio.common.utils.entity.appKey.Encode;
import com.example.minio.common.utils.office.minio.MinioClientPool;
import com.example.minio.dao.FilesDao;
import com.example.minio.entity.apps.Apps;
import com.example.minio.entity.files.Files;
import com.example.minio.entity.files.ShareFile;
import com.example.minio.service.FilesService;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

/**
 * @author zhy
 * @email 2434017367@qq.com
 * @date 2022/6/4 18:24
 */
@Log4j2
@Service("filesService")
public class FilesServiceImpl extends ServiceImpl<FilesDao, Files> implements FilesService {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private FilesDao filesDao;

    @Autowired
    private MinioClientPool minioClientPool;

    /**
     * 根据应用id和文件id获取文件信息
     * @param appId
     * @param fileId
     * @return
     */
    @Override
    public Files getFiles(String appId, String fileId) {
        return this.getOne(new LambdaQueryWrapper<Files>().eq(Files::getAppId, appId).eq(Files::getId, fileId));
    }

    /**
     * 文件上传
     * @param apps
     * @param path
     * @param multipartFile
     * @return
     */
    @Override
    public String upload(Apps apps, String path, MultipartFile multipartFile) {
        // 获取文件名
        String originalFilename = multipartFile.getOriginalFilename();
        // 获取文件流
        InputStream inputStream = null;
        try {
            inputStream = multipartFile.getInputStream();
        } catch (IOException e) {
            throw new RRException("上传文件获取流失败", e);
        }

        // 保存文件
        String fileId = saveFile(apps, path, originalFilename, inputStream);

        // 返回文件id
        return fileId;
    }

    /**
     * url文件上传
     *
     * @param apps
     * @param path
     * @param filename
     * @param fileurl
     * @return
     */
    @Override
    public String uploadUrl(Apps apps, String path, String filename, String fileurl) {
        try {
            InputStream inputStream = new URL(fileurl).openStream();

            // 保存文件
            String fileId = saveFile(apps, path, filename, inputStream);

            // 返回文件id
            return fileId;
        } catch (IOException e) {
            throw new RRException("通过文件链接获取文件失败");
        }
    }

    private String saveFile(Apps apps, String path, String originalFilename, InputStream in) {
        MinioClient minioClient = minioClientPool.getMinioClient();

        // 获取文件名和后缀
        Files parseFile = parseFilename(originalFilename);
        String fileName = parseFile.getFileName();
        String suffix = parseFile.getFileSuffix();

        // 判断文件类型是否在白名单中
        if (FileTypeEnum.isNotExistFileType(suffix)){
            throw new RRException("该文件类型不允许上传");
        }

        // 生成文件id
        String fileId = IdUtil.simpleUUID();
        // 文件路径
        if (StrUtil.isEmpty(path)) {
            path = FilePathEnum.FILES.getPath();
        }
        // 获取当前日期
        String dateFormat = DateUtil.format(new Date(), "yyyyMMdd");
        // 拼接路径
        path += ("/" + dateFormat);

        // 上传到minio中
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(apps.getMinioBucket())
                    .object(path + "/" + fileId)
                    .stream(in, in.available(), -1)
                    .build()
            );

            // 保存文件信息
            Files files = new Files();
            files.setId(fileId);
            files.setAppId(apps.getId());
            files.setFilePath(path);
            files.setFileName(fileName);
            files.setFileSuffix(suffix);
            files.setCreateTime(new Date());
            this.save(files);
        } catch (Exception e) {
            throw new RRException("minio文件上传错误", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            minioClientPool.closeMinioClient(minioClient);
        }

        return fileId;
    }

    /**
     * 删除文件
     * @param fileId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delFile(Apps apps, String fileId) throws Exception {
        Files files = this.getFiles(apps.getId(), fileId);
        if (files == null) {
            throw new RRException("文件不存在");
        }

        MinioClient minioClient = minioClientPool.getMinioClient();
        try {
            this.removeById(fileId);

            String minioBucket = apps.getMinioBucket();
            String wholeFilePath = files.getWholeFilePath();

            minioClient.removeObject(RemoveObjectArgs
                    .builder()
                    .bucket(minioBucket)
                    .object(wholeFilePath)
                    .build());
        } finally {
            minioClientPool.closeMinioClient(minioClient);
        }
    }

    /**
     * 获取文件信息列表
     * @param appId
     * @param idList
     * @return
     */
    @Override
    public List<Files> getListByIdList(String appId, List<String> idList){
        if (CollUtil.isNotEmpty(idList)){
            List<Files> sysFilesList = this.list(new LambdaQueryWrapper<Files>()
                            .eq(Files::getAppId, appId)
                            .in(Files::getId, idList)
                            .orderByAsc(Files::getCreateTime));
            return sysFilesList;
        }else{
            return new ArrayList<>();
        }
    }

    @Override
    public List<Files> getListByIds(String appId, String ids){
        if (StrUtil.isNotEmpty(ids)){
            if(ids.endsWith(",")){
                ids = ids.substring(0, ids.length() - 1);
            }
            List<String> idList = new ArrayList<>(Arrays.asList(ids.split(",")));
            return getListByIdList(appId, idList);
        }else{
            return new ArrayList<>();
        }
    }

    /**
     * 清除临时文件
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void clearInterim(Apps apps) throws Exception{
        String minioBucket = apps.getMinioBucket();
        MinioClient minioClient = minioClientPool.getMinioClient();
        String interimPath = FilePathEnum.INTERIM.getPath();

        try {
            this.remove(new LambdaQueryWrapper<Files>().likeRight(Files::getFilePath, interimPath));

            Iterable<io.minio.Result<Item>> interimFileList = minioClient.listObjects(ListObjectsArgs
                    .builder()
                    .bucket(minioBucket)
                    .prefix(interimPath)
                    .recursive(true)
                    .build());

            List<DeleteObject> deleteObjectList = new LinkedList<>();
            for (io.minio.Result<Item> item : interimFileList) {
                String objectName = item.get().objectName();
                log.info("delete object name: {}", objectName);
                deleteObjectList.add(new DeleteObject(objectName));
            }

            Iterable<io.minio.Result<DeleteError>> removeResults = minioClient.removeObjects(RemoveObjectsArgs
                    .builder()
                    .bucket(minioBucket)
                    .objects(deleteObjectList)
                    .build());

            Iterator<io.minio.Result<DeleteError>> iterator = removeResults.iterator();
            while (iterator.hasNext()) {
                io.minio.Result<DeleteError> next = iterator.next();
                DeleteError deleteError = next.get();
                log.info("deleteError: {}", deleteError.toString());
            }
        } finally {
            minioClientPool.closeMinioClient(minioClient);
        }
    }

    /**
     * 解析文件名称
     * @param filename
     * @return
     */
    @Override
    public Files parseFilename(String filename) {
        // 获取文件名和后缀
        String[] split = FileUtils.getFileNameSuffix(filename);
        if (split.length == 1) {
            throw new RRException("文件无后缀无法判断文件类型");
        }

        String fileName = split[0];
        String suffix = split[1].toLowerCase();

        Files files = new Files();
        files.setFileName(fileName);
        files.setFileSuffix(suffix);

        return files;
    }

    /**
     * 文件分享
     *
     * @param apps
     * @param fileId
     * @param second
     * @return
     */
    @Override
    public ShareFile getShareFile(Apps apps, String fileId, long second) {
        String appId = apps.getId();
        String appKey = apps.getAppKey();

        Files files = this.getFiles(appId, fileId);

        if (files != null) {
            long m;
            if (second < 0) {
                // 为负数就是永久2154年
                m = 5814823574516L;
            } else {
                long time = System.currentTimeMillis();
                m = time + (second * 1000L);
            }
            try {
                // 加密
                Encode encode = AppKeyUtils.encode(appKey, m);
                String secretKey = encode.getSecretKey();
                String timeStamp = encode.getTimeStamp();

                // url编码
                secretKey = URLEncoder.encode(secretKey, "UTF-8");
                timeStamp = URLEncoder.encode(timeStamp, "UTF-8");

                // 对文件id进行加密
                fileId = AesUtil.encode(timeStamp, fileId);
                fileId = URLEncoder.encode(fileId, "UTF-8");

                // 生成url
                String url = String.format("%s/files/shareFile?fileId=%s&key1=%s&key2=%s",
                        appConfig.getServerUrl(),
                        fileId,
                        secretKey,
                        timeStamp);

                ShareFile shareFile = new ShareFile();
                shareFile.setFileUrl(url);
                shareFile.setFileName(files.getFileName() + "." + files.getFileSuffix());
                return shareFile;
            } catch (Exception e) {
                throw new RRException("加密失败", e);
            }
        } else {
            throw new RRException("文件不存在");
        }
    }

}
