package com.studystation.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD) // 作用在方法上
@Retention(RetentionPolicy.RUNTIME) // 运行时有效
@Documented
public @interface Log {
    String module() default ""; // 模块名，如：用户模块
    String type() default "";   // 操作类型，如：登录、注册
}