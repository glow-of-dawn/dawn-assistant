//package com.dawn.plugin.except;
//
//import com.dawn.plugin.enmu.CodeEnmu;
//import com.dawn.plugin.enmu.LogEnmu;
//import com.dawn.plugin.enmu.VarEnmu;
//import com.dawn.plugin.util.Response;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.core.annotation.Order;
//import org.springframework.util.StringUtils;
//import org.springframework.validation.BindException;
//import org.springframework.validation.FieldError;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.WebDataBinder;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.InitBinder;
//import org.springframework.web.bind.annotation.ResponseBody;
//
//import java.util.stream.Collectors;
//
///**
// * 创建时间 2021/3/4 11:53
// *
// * @author hforest-480s
// */
//@Slf4j
//@Order(10000)
//@ResponseBody
//@ControllerAdvice
//@ConditionalOnProperty(name = {"plugin-status.controller-advice-status"}, havingValue = "enable", matchIfMissing = true)
//public class GlobalControllerAdvice {
//
//    @InitBinder
//    public void setAllowedFields(WebDataBinder dataBinder) {
//        String[] adb = new String[]{"class.*", "*.class.*", "Class.*", "*.Class.*"};
//        dataBinder.setDisallowedFields(adb);
//    }
//
//    @ExceptionHandler(Exception.class)
//    public Response<Object> exceptionAdvie(Exception ex) {
//        log.warn(LogEnmu.LOG3.value(), "来访异常", "Exception", ex.getMessage());
//        return new Response<>().failure("请规范访问").code(CodeEnmu.HTTP_499.icode());
//    }
//
//    @ExceptionHandler(ArithmeticException.class)
//    public Response<Object> arithmeticAdvie(ArithmeticException ex) {
//        log.warn(LogEnmu.LOG3.value(), "运算错误", "ArithmeticException", ex.getMessage());
//        return new Response<>().failure(StringUtils.truncate(ex.getMessage(), VarEnmu.NUMBER_50.ivalue())).code(CodeEnmu.HTTP_499.icode());
//    }
//
//    @ExceptionHandler(IllegalArgumentException.class)
//    public Response<Object> illegalArgument(IllegalArgumentException ex) {
//        log.warn(LogEnmu.LOG3.value(), "断言阻断", "ArithmeticException", ex.getMessage());
//        return new Response<>().failure(StringUtils.truncate(ex.getMessage(), VarEnmu.NUMBER_50.ivalue())).code(CodeEnmu.HTTP_498.icode());
//    }
//
//    /**
//     * 处理对象属性校验异常（@Valid作用于对象时）
//     */
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public Response<Object> methodArgumentNotValidException(MethodArgumentNotValidException ex) {
//        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
//                .map(FieldError::getDefaultMessage)
//                .collect(Collectors.joining(", "));
//        log.warn(LogEnmu.LOG3.value(), "参数校验失败1", "methodArgumentNotValidException", errorMessage);
//        return new Response<>().failure(StringUtils.truncate(errorMessage, VarEnmu.NUMBER_50.ivalue())).code(CodeEnmu.HTTP_498.icode());
//    }
//
//    /**
//     * 处理对象属性校验异常（@Valid作用于对象时）
//     */
//    @ExceptionHandler(ConstraintViolationException.class)
//    public Response<Object> constraintViolationException(ConstraintViolationException ex) {
//        String errorMessage = ex.getConstraintViolations().stream()
//                .map(ConstraintViolation::getMessage)
//                .collect(Collectors.joining(", "));
//        log.warn(LogEnmu.LOG3.value(), "参数校验失败2", "ConstraintViolationException", errorMessage);
//        return new Response<>().failure(StringUtils.truncate(errorMessage, VarEnmu.NUMBER_50.ivalue())).code(CodeEnmu.HTTP_498.icode());
//    }
//
//    /**
//     * 处理对象属性校验异常（@Valid作用于对象时）
//     */
//    @ExceptionHandler(BindException.class)
//    public Response<Object> bindExceptione(BindException ex) {
//        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
//                .map(FieldError::getDefaultMessage)
//                .collect(Collectors.joining(", "));
//        log.warn(LogEnmu.LOG3.value(), "参数校验失败3", "BindException", errorMessage);
//        return new Response<>().failure(StringUtils.truncate(errorMessage, VarEnmu.NUMBER_50.ivalue())).code(CodeEnmu.HTTP_498.icode());
//    }
//
//}
