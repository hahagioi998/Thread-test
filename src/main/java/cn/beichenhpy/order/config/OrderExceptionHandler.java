package cn.beichenhpy.order.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class OrderExceptionHandler {
    @ExceptionHandler(value = InterruptedException.class)
    public String interruptedExceptionHandler(InterruptedException e){
        log.error("线程：{},强制中断",Thread.currentThread().getName());
        return "error";
    }
    @ExceptionHandler(value = IllegalArgumentException.class)
    public String IllegalArgumentExceptionHandler(IllegalArgumentException e){
        log.error(e.getMessage());
        return "error";
    }
    @ExceptionHandler(value = Exception.class)
    public String exceptionHandler(Exception e){
        log.error(e.getMessage());
        return "error";
    }
}
