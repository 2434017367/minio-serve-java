package com.example.minio.entity.files;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 文件表
 *
 * @author zhy
 * @email 2434017367@qq.com
 * @date 2022-06-05 12:24:10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("files")
public class Files implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    /**
     * 应用id
     */
    private String appId;
    /**
     * 文件目录
     */
    private String filePath;
    /**
     * 文件名
     */
    private String fileName;
    /**
     * 文件后缀
     */
    private String fileSuffix;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 获取完整文件地址
     * @return
     */
    public String getWholeFilePath(){
        return filePath + "/" + id;
    }

}
