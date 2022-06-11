package com.example.minio.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.minio.entity.apps.Apps;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 对接应用表
 *
 * @author zhy
 * @email 2434017367@qq.com
 * @date 2022-06-05 12:24:10
 */
@Mapper
@Repository
public interface AppsDao extends BaseMapper<Apps> {

}
