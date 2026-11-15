package com.studystation.service;

import com.studystation.pojo.SubjectArea;

import java.util.List;

public interface SubjectAreaService {
    List<SubjectArea> findAll();
    String  add(SubjectArea subjectArea);
    String  update(SubjectArea subjectArea);
    void deleteById(Integer id);
}
