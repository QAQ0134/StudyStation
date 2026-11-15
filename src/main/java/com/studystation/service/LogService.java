package com.studystation.service;

import com.studystation.pojo.PageResult;
import com.studystation.pojo.OperationLog;

public interface LogService {
    PageResult<OperationLog> queryLogs(Integer pageNum, Integer pageSize, String type, String description);
}