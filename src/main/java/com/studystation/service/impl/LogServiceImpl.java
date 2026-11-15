package com.studystation.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.studystation.mapper.OperationLogMapper;
import com.studystation.pojo.OperationLog;
import com.studystation.pojo.PageResult;
import com.studystation.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogServiceImpl implements LogService {

    @Autowired
    private OperationLogMapper logMapper;

    @Override
    public PageResult<OperationLog> queryLogs(Integer pageNum, Integer pageSize, String type, String description) {
        PageHelper.startPage(pageNum, pageSize);
        List<OperationLog> logs = logMapper.queryLogs(type, description);
        Page<OperationLog> page = (Page<OperationLog>) logs;
        return new PageResult<>(page.getTotal(), page.getResult());
    }
}