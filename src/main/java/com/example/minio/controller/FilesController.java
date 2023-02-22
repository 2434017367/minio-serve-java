package com.example.minio.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONObject;
import com.example.minio.common.enums.FileTypeEnum;
import com.example.minio.common.exception.RRException;
import com.example.minio.common.result.Result;
import com.example.minio.common.utils.AesUtil;
import com.example.minio.common.utils.AppKeyUtils;
import com.example.minio.common.utils.FileUtils;
import com.example.minio.common.utils.LoginAppUtils;
import com.example.minio.common.utils.entity.appKey.Decode;
import com.example.minio.common.utils.office.WordUtils;
import com.example.minio.common.utils.office.minio.MinioClientPool;
import com.example.minio.entity.SkipAuthFileUploadEntity;
import com.example.minio.entity.apps.Apps;
import com.example.minio.entity.files.Files;
import com.example.minio.entity.files.ShareFile;
import com.example.minio.service.AppsService;
import com.example.minio.service.FilesService;
import io.minio.*;
import lombok.extern.log4j.Log4j2;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * @author zhy
 * @email 2434017367@qq.com
 * @date 2022/6/4 10:17
 */
@Log4j2
@RequestMapping("/files")
@RestController
public class FilesController {

    @Autowired
    private FilesService filesService;

    @Autowired
    private MinioClientPool minioClientPool;

    @Autowired
    private FileUtils fileUtils;

    @Autowired
    private AppsService appsService;

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public Result upload(@RequestParam(value = "path", required = false) String path,
                         @RequestParam("file") MultipartFile file) {
        Apps appInfo = LoginAppUtils.getAppInfo();
        String fileId = filesService.upload(appInfo, path, file);
        return Result.ok(fileId);
    }

    /**
     * url文件上传
     * @param path
     * @param filename
     * @param fileurl
     * @return
     */
    @PostMapping("/uploadUrl")
    public Result uploadUrl(@RequestParam(value = "path", required = false) String path,
                            @RequestParam("filename") String filename,
                            @RequestParam("fileurl") String fileurl) {
        Apps appInfo = LoginAppUtils.getAppInfo();
        String fileId = filesService.uploadUrl(appInfo, path, filename, fileurl);
        return Result.ok(fileId);
    }

    /**
     * 下载文件
     * @param fileId
     * @return
     */
    @GetMapping("/download")
    public void download(@RequestParam("fileId") String fileId,
                           HttpServletRequest request,
                           HttpServletResponse response) throws Exception{
        String appId = LoginAppUtils.getAppId();
        Files files = filesService.getFiles(appId, fileId);
        if (files == null) {
            throw new RRException("文件不存在");
        }

        MinioClient minioClient = minioClientPool.getMinioClient();
        try {
            String wholeFilePath = files.getWholeFilePath();
            String minioBucket = LoginAppUtils.getAppInfo().getMinioBucket();

            // 获取minio文件流
            InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioBucket)
                    .object(wholeFilePath)
                    .build());

            // 获取minio文件字节大小
            StatObjectResponse statObjectResponse = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioBucket)
                    .object(wholeFilePath)
                    .build());
            long byteSize = statObjectResponse.size();

            //设置相关参数
            response.setHeader("Accept-Ranges","bytes");
            response.setContentLengthLong(byteSize);

            String userAgent = request.getHeader("User-Agent");
            log.info("userAgent:" + userAgent);
            String fileName = files.getFileName();
            String fileSuffix = "." + files.getFileSuffix();
            String formFileName = fileName + fileSuffix;
            log.info("formFileName:" + formFileName);

            // 针对IE或者以IE为内核的浏览器：
            if (userAgent.contains("MSIE") || userAgent.contains("Trident")) {
                formFileName = java.net.URLEncoder.encode(formFileName, "UTF-8");
            } else {
                // 非IE浏览器的处理：
                formFileName = new String(formFileName.getBytes("UTF-8"), "ISO-8859-1");
            }
            response.setHeader("Content-disposition",String.format("attachment; filename=\"%s\"", formFileName));
            response.setContentType("multipart/form-data");
            response.setCharacterEncoding("UTF-8");

            ServletOutputStream outputStream = response.getOutputStream();
            IOUtils.copyLarge(inputStream, outputStream);
            outputStream.close();
            inputStream.close();
        } finally {
            minioClientPool.closeMinioClient(minioClient);
        }
    }

    /**
     * 文件预览
     * @param fileId
     * @param response
     * @return
     */
    @GetMapping("/preview")
    public void preview(@RequestParam("fileId") String fileId,
                          HttpServletResponse response) throws Exception {
        String appId = LoginAppUtils.getAppId();
        Files files = filesService.getFiles(appId, fileId);
        if (files == null) {
            throw new RRException("文件不存在");
        }

        MinioClient minioClient = minioClientPool.getMinioClient();
        String pdfFilePath = null;
        String docFilePath = null;
        try {
            String wholeFilePath = files.getWholeFilePath();
            String fileName = files.getFileName();
            String fileSuffix = files.getFileSuffix();
            FileTypeEnum fileTypeEnum = FileTypeEnum.getTypeBySuffix(fileSuffix);

            String minioBucket = LoginAppUtils.getAppInfo().getMinioBucket();
            InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioBucket)
                    .object(wholeFilePath)
                    .build());

            // 判断是不是word，是转pdf
            if (FileTypeEnum.WORD.equals(fileTypeEnum)) {
                // 保存doc文件
                docFilePath = fileUtils.getInterimFilePath();
                FileOutputStream fileOutputStream = new FileOutputStream(docFilePath);
                IOUtils.copyLarge(inputStream, fileOutputStream);
                fileOutputStream.close();
                inputStream.close();

                // 转化pdf
                pdfFilePath = fileUtils.getInterimFilePath();
                WordUtils.docToPdf(docFilePath, pdfFilePath);

                // 封装pdf input
                inputStream = new FileInputStream(pdfFilePath);
            }

            if (FileTypeEnum.PDF.equals(fileTypeEnum)) {
                response.setContentType(MediaType.APPLICATION_PDF_VALUE);
            } else if (FileTypeEnum.WORD.equals(fileTypeEnum)) {
                response.setContentType(MediaType.APPLICATION_PDF_VALUE);
            } else if (FileTypeEnum.IMAGE.equals(fileTypeEnum)) {
                response.setContentType(MediaType.IMAGE_JPEG_VALUE);
            } else if (FileTypeEnum.VIDEO.equals(fileTypeEnum)) {
                response.setContentType("video/" + fileSuffix);
            }

            ServletOutputStream outputStream = response.getOutputStream();
            IoUtil.copy(inputStream, outputStream);
            outputStream.close();
            inputStream.close();
        } finally {
            minioClientPool.closeMinioClient(minioClient);
            // 删除临时文件
            FileUtil.del(docFilePath);
            FileUtil.del(pdfFilePath);
        }
    }

    /**
     * word转pdf
     * @param multipartFile
     */
    @PostMapping("/wordToPdf")
    public void wordToPdf(@RequestParam("file") MultipartFile multipartFile,
                         HttpServletResponse response) throws Exception {
        // 获取文件名和后缀
        String originalFilename = multipartFile.getOriginalFilename();
        Files parseFile = filesService.parseFilename(originalFilename);

        // 判断文件类型是word类型
        String suffix = parseFile.getFileSuffix();
        if (!FileTypeEnum.isWord(suffix)) {
            throw new RRException("文件格式不为word");
        }

        // 创建word文件
        InputStream inputStream = multipartFile.getInputStream();
        String docFileName = IdUtil.simpleUUID() + "." + suffix;
        String docFilePath = fileUtils.getInterimFilePath(docFileName);
        FileOutputStream fileOutputStream = new FileOutputStream(docFilePath);
        IoUtil.copy(inputStream, fileOutputStream);
        inputStream.close();
        fileOutputStream.close();

        // 创建pdf文件
        String pdfFileName = IdUtil.simpleUUID() + ".pdf";
        String pdfFilePath = fileUtils.getInterimFilePath(pdfFileName);

        // word转pdf
        WordUtils.docToPdf(docFilePath, pdfFilePath);

        // 回传pdf流
        response.setHeader("Accept-Ranges","bytes");
        ServletOutputStream outputStream = response.getOutputStream();
        FileInputStream fileInputStream = new FileInputStream(pdfFilePath);
        IoUtil.copy(fileInputStream, outputStream);
        fileInputStream.close();
        outputStream.close();

        // 删除临时文件
        FileUtil.del(docFilePath);
        FileUtil.del(pdfFilePath);
    }

    /**
     * 文件删除
     * @param fileId 文件id
     * @param isDel  是否真正删除文件
     * @return
     */
    @DeleteMapping("/delFile")
    public Result delFile(@RequestParam("fileId") String fileId,
                          @RequestParam(value = "isDel", defaultValue = "true") boolean isDel)
            throws Exception{
        if (isDel){
            Apps appInfo = LoginAppUtils.getAppInfo();
            filesService.delFile(appInfo, fileId);
        }
        return Result.ok();
    }

    /**
     * 根据文件id串获取文件信息列表
     * @param ids
     * @return
     */
    @GetMapping("/getListByIds")
    public Result getListByIds(@RequestParam("ids") String ids){
        String appId = LoginAppUtils.getAppId();
        List<Files> sysFilesList = filesService.getListByIds(appId, ids);

        if (CollUtil.isNotEmpty(sysFilesList)) {
            for (Files files : sysFilesList) {
                files.setAppId(null).setFilePath(null).setCreateTime(null);
            }
        }

        return Result.ok(sysFilesList);
    }

    /**
     * 清除临时文件
     * @return
     */
    @GetMapping("/clearInterim")
    public Result clearInterim() throws Exception{
        Apps appInfo = LoginAppUtils.getAppInfo();
        filesService.clearInterim(appInfo);
        return Result.ok();
    }

    /**
     * 获取文件分享链接
     * @param fileId
     * @param second
     * @return
     */
    @GetMapping("/getShareFile")
    public Result getShareFile(@RequestParam("fileId") String fileId,
                                  @RequestParam("second") long second) {
        Apps appInfo = LoginAppUtils.getAppInfo();
        ShareFile shareFile = filesService.getShareFile(appInfo, fileId, second);
        return Result.ok(shareFile);
    }

    /**
     * 分享文件
     * @param fileId
     * @return
     */
    @GetMapping("/shareFile")
    public void shareFile(@RequestParam("fileId") String fileId,
                          @RequestParam(value = "isPreview", defaultValue = "false") boolean isPreview,
                          HttpServletRequest request,
                          HttpServletResponse response) throws Exception{

        String stamp = (String) request.getAttribute("stamp");

        // 解密获取到真正的文件id
        fileId = AesUtil.decode(stamp, fileId);

        if (isPreview) {
            this.preview(fileId, response);
        } else {
            this.download(fileId, request, response);
        }
    }

    /**
     * 获取跳过认证文件上传的上传链接
     * @param path
     * @return
     */
    @GetMapping("/getSkipAuthFileUploadUrl")
    public Result getSkipAuthFileUploadUrl(@RequestParam(value = "path", required = false) String path) {
        Apps appInfo = LoginAppUtils.getAppInfo();
        String uploadUrl = filesService.getSkipAuthFileUploadUrl(appInfo, path);
        return Result.ok(uploadUrl);
    }

    /**
     * 跳过认证文件上传
     * @param key
     * @param stamp
     */
    @PostMapping("/skipAuthFileUpload")
    public Result skipAuthFileUpload(@RequestParam("key") String key,
                                     @RequestParam("stamp") String stamp,
                                     @RequestParam("fileName") String fileName,
                                     HttpServletRequest request) {
        try {
            Decode decode = AppKeyUtils.decode(key, stamp);
            Date date = decode.getStamp();

            // 校验时间
            long between = DateUtil.between(date, new Date(), DateUnit.MINUTE, false);
            // 时间允许误差前2分钟后5分钟
            if (between > -5 && between < 2) {
                String json = decode.getAppKey();
                SkipAuthFileUploadEntity skipAuthFileUploadEntity = JSONObject.parseObject(json, SkipAuthFileUploadEntity.class);

                String appKey = skipAuthFileUploadEntity.getAppKey();
                Apps apps = appsService.getByAppKey(appKey);

                if (apps != null) {
                    long contentLengthLong = request.getContentLengthLong();
                    ServletInputStream inputStream = request.getInputStream();
                    String filePath = skipAuthFileUploadEntity.getFilePath();
                    String fileId = filesService.upload(apps, filePath, fileName, inputStream, contentLengthLong);
                    return Result.ok(fileId);
                } else {
                    throw new RRException("app不存在");
                }
            } else {
                throw new RRException("请求超时");
            }
        } catch (Exception e) {
            throw new RRException("解密失败", e);
        }
    }

}
