package com.studystation.pojo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OperationLog {
    private Long id;
    private String username;
    private String module;
    private String type;
    private String description;
    private String ip;
    private LocalDateTime createTime;
}