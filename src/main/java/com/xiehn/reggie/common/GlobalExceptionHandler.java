package com.xiehn.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理
 */
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 新增员工重名异常
     * @param ex 数据库主键重名异常
     * @return 前端的错误提示信息
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){
        log.error(ex.getMessage());

        //判断新增的员工用户名是否重名
        if(ex.getMessage().contains("Duplicate entry")){
            String[] split=ex.getMessage().split(" ");
            String msg=split[2]+"已存在";
            return R.error(msg);
        }
        return R.error("未知错误");
    }

    /**
     * 异常处理方法，用来捕获删除菜品分类时的异常
     * @param ex
     * @return
     */
    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException ex){
        log.error(ex.getMessage());
        return R.error(ex.getMessage());
    }


}
