package com.studystation.controller;

import com.studystation.pojo.PageResult;
import com.studystation.pojo.OperationLog;
import com.studystation.pojo.Result;
import com.studystation.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/log")
public class LogController {

    @Autowired
    private LogService logService;

    @GetMapping
    public Result page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String description) {

        PageResult<OperationLog> pageResult = logService.queryLogs(pageNum, pageSize, type, description);
        return Result.success(pageResult);
    }
}