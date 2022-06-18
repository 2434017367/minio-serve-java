package com.example.minio.controller;

import cn.hutool.core.io.FileUtil;
import com.example.minio.common.enums.FileTypeEnum;
import com.example.minio.common.exception.RRException;
import com.example.minio.common.minio.MinioClientPool;
import com.example.minio.common.result.Result;
import com.example.minio.common.utils.FileUtils;
import com.example.minio.common.utils.LoginAppUtils;
import com.example.minio.common.utils.office.WordUtils;
import com.example.minio.entity.apps.Apps;
import com.example.minio.entity.files.Files;
import com.example.minio.service.FilesService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.extern.log4j.Log4j2;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public Result upload(@RequestParam(value = "path", required = false) String path,
                         @RequestParam("file") MultipartFile file) {
        Apps appInfo = LoginAppUtils.getAppInfo();
        return filesService.upload(appInfo, path, file);
    }

    /**
     * 下载文件
     * @param fileId
     * @return
     */
    @GetMapping("/download")
    public Result download(@RequestParam("fileId") String fileId,
                           HttpServletResponse response) {
        Files files = filesService.getById(fileId);
        if (files == null) {
            return Result.error("文件不存在");
        }

        MinioClient minioClient = minioClientPool.getMinioClient();
        InputStream in = null;
        ServletOutputStream out = null;
        try {
            String wholeFilePath = files.getWholeFilePath();
            String minioBucket = LoginAppUtils.getAppInfo().getMinioBucket();

            in = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioBucket)
                    .object(wholeFilePath)
                    .build());

            //设置相关参数
            response.setHeader("Accept-Ranges","bytes");
            String fileName = files.getFileName();
            String fileSuffix = "." + files.getFileSuffix();
            response.setHeader("Content-Disposition","attachment;filename=\""+
                    new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1)
                    + fileSuffix + "\"");

            out = response.getOutputStream();
            long length = IOUtils.copyLarge(in, out);
        } catch (Exception e) {
            throw new RRException("文件下载失败", e);
        } finally {
            minioClientPool.closeMinioClient(minioClient);
            try {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return Result.ok();
    }

    /**
     * 文件预览
     * @param fileId
     * @param response
     * @return
     */
    @GetMapping("/preview")
    public void preview(@RequestParam("fileId") String fileId,
                          HttpServletResponse response) {
        Files files = filesService.getById(fileId);
        if (files == null) {
            throw new RRException("文件不存在");
        }

        String wholeFilePath = files.getWholeFilePath();
        String fileName = files.getFileName();
        String fileSuffix = files.getFileSuffix();

        MinioClient minioClient = minioClientPool.getMinioClient();
        InputStream in = null;
        ServletOutputStream out = null;
        String docFilePath = null;
        String pdfFilePath = null;
        try {
            String minioBucket = LoginAppUtils.getAppInfo().getMinioBucket();
            in = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioBucket)
                    .object(wholeFilePath)
                    .build());

            // 根据不同的文件类型设置响应头
            FileTypeEnum fileTypeEnum = FileTypeEnum.getTypeBySuffix(fileSuffix);
            if (FileTypeEnum.PDF.equals(fileTypeEnum)) {
                response.setContentType(MediaType.APPLICATION_PDF_VALUE);
            } else if (FileTypeEnum.WORD.equals(fileTypeEnum)) {
                response.setContentType(MediaType.APPLICATION_PDF_VALUE);
                // 将文件转为pdf
                try {
                    // 保存doc文件
                    docFilePath = fileUtils.getInterimFilePath();
                    FileOutputStream fileOutputStream = new FileOutputStream(docFilePath);
                    IOUtils.copyLarge(in, fileOutputStream);
                    fileOutputStream.close();
                    in.close();

                    // 转化pdf
                    pdfFilePath = fileUtils.getInterimFilePath();
                    WordUtils.docToPdf(docFilePath, pdfFilePath);

                    // 封装pdf input
                    in = new FileInputStream(pdfFilePath);
                } catch (Exception e) {
                    throw new RRException("pdf转换失败", e);
                }
            } else if (FileTypeEnum.IMAGE.equals(fileTypeEnum)) {
                response.setContentType(MediaType.IMAGE_JPEG_VALUE);
            }
            out = response.getOutputStream();
            long length = IOUtils.copyLarge(in, out);
        } catch (Exception e) {
            if (e.getClass().equals(RRException.class)){
                throw (RRException) e;
            } else {
                throw new RRException("文件预览失败", e);
            }
        } finally {
            minioClientPool.closeMinioClient(minioClient);
            try {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                log.error("io流关闭失败");
            }
            try {
                FileUtil.del(docFilePath);
                FileUtil.del(pdfFilePath);
            } catch (Exception e) {
                log.error("临时文件删除失败");
            }
        }
    }

    /**
     * 文件删除
     * @param fileId 文件id
     * @param isDel  是否真正删除文件
     * @return
     */
    @DeleteMapping("/delFile")
    public Result delFile(@RequestParam("fileId") String fileId,
                          @RequestParam(value = "isDel", defaultValue = "true") boolean isDel){
        if (isDel){
            Apps appInfo = LoginAppUtils.getAppInfo();
            return filesService.delFile(appInfo, fileId);
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
        List<Files> sysFilesList = filesService.getListByIds(ids);
        return Result.ok(sysFilesList);
    }

}
