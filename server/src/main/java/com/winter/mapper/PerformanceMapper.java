package com.winter.mapper;

import com.winter.domain.Performance;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 持久层接口
 * */

@Mapper
public interface PerformanceMapper {
        List<Performance> findAll();
}
