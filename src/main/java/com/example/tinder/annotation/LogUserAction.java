package com.example.tinder.annotation;

import com.example.tinder.enums.UserAction;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogUserAction {
    UserAction action();
    boolean logRequestBody() default false;
    boolean logResponseBody() default false;
    String description() default "";
}
