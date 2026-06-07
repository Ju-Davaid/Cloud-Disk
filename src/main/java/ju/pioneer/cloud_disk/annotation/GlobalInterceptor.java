package ju.pioneer.cloud_disk.annotation;

import org.springframework.web.bind.annotation.Mapping;

import java.lang.annotation.*;


@Target(ElementType.METHOD) // 注解目标为方法
@Retention(RetentionPolicy.RUNTIME) // 运行时注解
@Documented // 文档化注解
@Mapping // 映射注解
public @interface GlobalInterceptor {
    /**
     * 是否需要校验登录
     * @return boolean
     */
    boolean checkLogin() default false;
    /**
     * 是否需要校验参数
     * @return boolean
     */
    boolean checkParameters() default false;
}
