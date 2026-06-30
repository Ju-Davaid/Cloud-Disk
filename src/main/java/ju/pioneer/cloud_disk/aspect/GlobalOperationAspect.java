package ju.pioneer.cloud_disk.aspect;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import ju.pioneer.cloud_disk.annotation.GlobalInterceptor;
import ju.pioneer.cloud_disk.annotation.VerifyParameter;
import ju.pioneer.cloud_disk.constants.Constants;
import ju.pioneer.cloud_disk.entity.dto.SessionWebUserDto;
import ju.pioneer.cloud_disk.entity.enums.ResponseCodeEnum;
import ju.pioneer.cloud_disk.entity.enums.TypeEnum;
import ju.pioneer.cloud_disk.exception.BusinessException;
import ju.pioneer.cloud_disk.utils.StringTools;
import ju.pioneer.cloud_disk.utils.VerifyUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

// 全局操作切面
@Aspect
@Component("globalOperationAspect")
public class GlobalOperationAspect {
    private static final Logger logger = LoggerFactory.getLogger(GlobalOperationAspect.class);

    /**
     * 全局操作拦截器点切面
     * 匹配所有方法，注解为GlobalInterceptor的类
     */
    @Pointcut("@annotation(ju.pioneer.cloud_disk.annotation.GlobalInterceptor)")
    private void requestInterceptorPointcut() {
    }

    /**
     * 全局操作拦截器
     * 校验登录和参数
     *
     * @param joinPoint 连接点
     * @return 执行结果
     * @throws BusinessException 校验异常或执行异常
     */
    @Around("requestInterceptorPointcut()")
    public Object requestInterceptorAround(ProceedingJoinPoint joinPoint) throws BusinessException {
        try {
            Object target = joinPoint.getTarget();
            Object[] args = joinPoint.getArgs();
            // 获取方法名
            String methodName = joinPoint.getSignature().getName();
            // 获取方法参数类型
            Class<?>[] parameterTypes = ((MethodSignature) joinPoint.getSignature()).getMethod().getParameterTypes();
            // 获取方法对象
            Method method = target.getClass().getMethod(methodName, parameterTypes);
            // 获取拦截器注解
            GlobalInterceptor interceptor = method.getAnnotation(GlobalInterceptor.class);
            if (interceptor == null) {
                return null;
            }
            // 校验登录
            if (interceptor.checkLogin() || interceptor.checkAdmin()) {
                logger.info(target.getClass().getSimpleName() + "." + methodName + "检查登录");
                checkLogin(interceptor.checkAdmin());
            }
            // 校验参数
            if (interceptor.checkParameters()) verifyParameters(method, args);
            // 执行方法
            return joinPoint.proceed(args);
        } catch (BusinessException e) {
            logger.info("全局拦截器异常" + e.getMessage());
            throw e;
        } catch (Throwable e) {
            logger.info("全局拦截器异常" + e.getMessage());
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        }
    }

    /**
     * 校验参数
     *
     * @param method 方法对象
     * @param args   参数数组
     * @throws BusinessException 校验异常
     */
    private void verifyParameters(Method method, Object[] args) throws BusinessException {
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Object arg = args[i];
            VerifyParameter verifyParameter = parameter.getAnnotation(VerifyParameter.class);
            // 没有校验注解，跳过
            if (verifyParameter == null) {
                continue;
            }
            if (TypeEnum.TYPE_MAP.containsValue(parameter.getParameterizedType().getTypeName())) {
                checkValue(arg, verifyParameter);
            } else {
                checkObjectValue(parameter, arg);
            }
            logger.info(parameter.getName() + "参数" + arg);
        }
    }

    /**
     * 校验参数值
     *
     * @param value           参数值
     * @param verifyParameter 校验注解
     * @throws BusinessException 校验异常
     */
    private void checkValue(Object value, VerifyParameter verifyParameter) throws BusinessException {
        boolean isEmpty = value == null || StringTools.isEmpty(value.toString());
        int length = value == null ? 0 : value.toString().length();
        // 校验必填参数不能为空
        if (verifyParameter.required() && isEmpty) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        // 校验参数长度
        if (!isEmpty && (verifyParameter.max() != -1 && verifyParameter.max() < length || verifyParameter.min() != -1 && verifyParameter.min() > length)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        // 校验参数是否符合正则表达式
        if (!isEmpty && !StringTools.isEmpty(verifyParameter.regex().getRegex()) && !VerifyUtils.verify(verifyParameter.regex(), String.valueOf(value))) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
    }

    /**
     * 校验对象参数值
     *
     * @param parameter 参数对象
     * @param value     参数值
     * @throws BusinessException 校验异常
     */
    private void checkObjectValue(Parameter parameter, Object value) throws BusinessException {

    }

    /**
     * 校验登录
     *
     * @param isCheckAdmin 是否校验管理员权限
     */
    private void checkLogin(boolean isCheckAdmin) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpSession session = request.getSession();
        SessionWebUserDto sessionWebUserDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_WEB_USER_KEY);
        if (sessionWebUserDto == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        if (isCheckAdmin && !sessionWebUserDto.isAdmin()) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
    }
}
