package com.example.minio.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.minio.dao.AppsDao;
import com.example.minio.entity.apps.Apps;
import com.example.minio.service.AppsService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zhy
 * @email 2434017367@qq.com
 * @date 2022/6/5 12:40
 */
@Log4j2
@Service("appsService")
public class AppsServiceImpl extends ServiceImpl<AppsDao, Apps> implements AppsService {

    @Autowired
    private AppsDao appsDao;

    @Override
    public Apps getByAppKey(String appKey) {
        Apps one = this.getOne(new LambdaQueryWrapper<Apps>()
                .eq(Apps::getAppKey, appKey));

        return one;
    }
}
