package ju.pioneer.cloud_disk.annotation;

import ju.pioneer.cloud_disk.entity.enums.VerifyRegexEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 校验参数注解
@Retention(RetentionPolicy.RUNTIME) // 运行时注解
@Target({ElementType.PARAMETER,ElementType.FIELD}) // 注解目标为参数
public @interface VerifyParameter {
    // 最小值
    int min() default -1;
    // 最大值
    int max() default -1;
    // 是否必填
    boolean required() default false;
    // 校验正则表达式(默认不校验)
    VerifyRegexEnum regex() default VerifyRegexEnum.NO;
}
