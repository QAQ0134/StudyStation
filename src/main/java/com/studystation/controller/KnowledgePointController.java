package com.studystation.controller;

import com.studystation.pojo.KnowledgePoint;
import com.studystation.pojo.Result;
import com.studystation.service.KnowledgePointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/knowledgePoint")
public class KnowledgePointController {

    @Autowired
    private KnowledgePointService knowledgePointService;

    @GetMapping
    public Result list() {
        return Result.success(knowledgePointService.findAll());
    }

    @PostMapping
    public Result add(@RequestBody KnowledgePoint knowledgePoint) {
        String error = knowledgePointService.add(knowledgePoint);
        return error != null ? Result.error(error) : Result.success();
    }

    // 根据学科ID查询
    @GetMapping("/subject/{subjectAreaId}")
    public Result getBySubjectAreaId(@PathVariable Integer subjectAreaId) {
        return Result.success(knowledgePointService.findBySubjectAreaId(subjectAreaId));
    }

    @PutMapping
    public Result update(@RequestBody KnowledgePoint knowledgePoint) {
        String error = knowledgePointService.update(knowledgePoint);
        return error != null ? Result.error(error) : Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        knowledgePointService.deleteById(id);
        return Result.success();
    }
}