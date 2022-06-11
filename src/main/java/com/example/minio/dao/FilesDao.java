package com.example.minio.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.minio.entity.files.Files;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 文件表
 *
 * @author zhy
 * @email 2434017367@qq.com
 * @date 2022-06-05 12:24:10
 */
@Mapper
@Repository
public interface FilesDao extends BaseMapper<Files> {

}
