package com.winter.factory;

import com.winter.domain.LogData;
import com.winter.req.LogQueryReq;

import java.util.List;

/**
 * 使用本地文件存储
 * */
public class LocalFileStore implements LogStore{
    @Override
    public void storeData(LogData logData) {

    }

    @Override
    public List<LogData> queryData(LogQueryReq logQueryReq) {
        return null;
    }
}
