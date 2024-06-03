package com.winter.factory;

import com.winter.domain.LogData;
import com.winter.req.LogQueryReq;

import java.util.List;

/**
 * 采用ES存储数据
 * */
public class ESStore implements LogStore{

    @Override
    public void storeData(LogData logData) {

    }

    @Override
    public List<LogData> queryData(LogQueryReq logQueryReq) {
        return null;
    }
}
