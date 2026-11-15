package com.studystation.controller;

import com.studystation.pojo.Result;
import com.studystation.pojo.SubjectArea;
import com.studystation.service.SubjectAreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/subjectArea")
@RestController
public class SubjectAreaController {

    @Autowired
    private SubjectAreaService subjectAreaService;

    // 查询
    @GetMapping
    public Result list(){
        List<SubjectArea> subjectAreaList = subjectAreaService.findAll();
        return Result.success(subjectAreaList);
    }

    // 新增
    @PostMapping
    public Result add(@RequestBody SubjectArea subjectArea) {
        String errorMsg = subjectAreaService.add(subjectArea);
        if (errorMsg != null) {
            return Result.error(errorMsg); // 失败返回 0 和提示信息
        }
        return Result.success();
    }

    // 修改
    @PutMapping
    public Result update(@RequestBody SubjectArea subjectArea) {
        String errorMsg = subjectAreaService.update(subjectArea);
        if (errorMsg != null) {
            return Result.error(errorMsg);
        }
        return Result.success();
    }

    // 删除 (路径传参：/subjectArea/1)
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        subjectAreaService.deleteById(id);
        return Result.success();
    }
}
