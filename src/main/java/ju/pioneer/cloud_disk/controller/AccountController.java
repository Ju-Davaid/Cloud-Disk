package ju.pioneer.cloud_disk.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import ju.pioneer.cloud_disk.annotation.GlobalInterceptor;
import ju.pioneer.cloud_disk.annotation.VerifyParameter;
import ju.pioneer.cloud_disk.constants.Constants;
import ju.pioneer.cloud_disk.entity.dto.SessionWebUserDto;
import ju.pioneer.cloud_disk.entity.enums.VerifyRegexEnum;
import ju.pioneer.cloud_disk.entity.vo.ResponseVO;
import ju.pioneer.cloud_disk.exception.BusinessException;
import ju.pioneer.cloud_disk.service.EmailCodeService;
import ju.pioneer.cloud_disk.service.UserInfoService;
import ju.pioneer.cloud_disk.utils.CaptchaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

// 账户控制器
@RestController("accountController")
public class AccountController extends BaseController {
    @Resource
    private EmailCodeService emailCodeService;
    @Resource
    private UserInfoService userInfoService;
    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);



    /**
     * 验证码图片生成
     *
     * @param response 响应
     * @param session  会话
     * @param type     类型
     */
    @GlobalInterceptor(checkParameters = true)
    @GetMapping("/generateCheckCodeImage")
    public void generateCheckCodeImage(HttpServletResponse response, HttpSession session, @RequestParam(defaultValue = Constants.ZERO + "") Integer type) {
        Object[] captcha = CaptchaUtils.createCaptcha();
        String code = (String) captcha[0];
        BufferedImage image = (BufferedImage) captcha[1];
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setContentType("image/jpeg");
        response.setDateHeader("Expires", 0);
        if (type == Constants.ZERO) {
            session.setAttribute(Constants.CHECK_CODE_KEY, code);
        } else {
            session.setAttribute(Constants.CHECK_CODE_KEY_EMAIL, code);
        }
        try (ServletOutputStream out = response.getOutputStream()) {
            ImageIO.write(image, "jpeg", out);
            out.flush();
        } catch (IOException e) {
            throw new BusinessException("验证码图片生成失败");
        }
    }

    /**
     * 发送邮箱验证码
     *
     * @param session   会话
     * @param email     邮箱
     * @param checkCode 验证码
     * @param type      类型
     */
    @GlobalInterceptor(checkParameters = true)
    @GetMapping("/sendEmailCode")
    public ResponseVO<?> sendEmailCode(HttpSession session, @VerifyParameter(required = true, regex = VerifyRegexEnum.EMAIL, max = 150) String email, @VerifyParameter(required = true) String checkCode, @VerifyParameter(required = true) Integer type) {
        try {
            String checkCodeSession = (String) session.getAttribute(Constants.CHECK_CODE_KEY_EMAIL);
            if (!checkCode.equalsIgnoreCase(checkCodeSession)) {
                throw new BusinessException("验证码错误");
            }
            emailCodeService.sendEmailCode(email, type);
            return getSuccessResponseVO(null);
        } finally {
            // 移除验证码
            session.removeAttribute(Constants.CHECK_CODE_KEY_EMAIL);
        }
    }

    /**
     * 注册
     *
     * @param session        会话
     * @param email          邮箱
     * @param nickName       昵称
     * @param password       密码
     * @param checkCode      验证码
     * @param emailCheckCode 邮箱验证码
     * @return 注册结果
     */
    @GlobalInterceptor(checkParameters = true)
    @PostMapping("/register")
    public ResponseVO<?> register(
            HttpSession session,
            @VerifyParameter(required = true, regex = VerifyRegexEnum.EMAIL, max = 150) String email,
            @VerifyParameter(required = true) String nickName,
            @VerifyParameter(required = true, regex = VerifyRegexEnum.PASSWORD, min = 8, max = 18) String password,
            @VerifyParameter(required = true) String checkCode,
            @VerifyParameter(required = true) String emailCheckCode
    ) {
        try {
            String checkCodeSession = (String) session.getAttribute(Constants.CHECK_CODE_KEY);
            logger.info("checkCodeSession: {}", checkCodeSession);
            logger.info("checkCode: {}", checkCode);
            if (!checkCode.equalsIgnoreCase(checkCodeSession)) {
                throw new BusinessException("验证码错误");
            }
            userInfoService.register(email, nickName, password, emailCheckCode);
            return getSuccessResponseVO(null);
        } finally {
            // 移除验证码
            session.removeAttribute(Constants.CHECK_CODE_KEY);
        }
    }

    @GlobalInterceptor(checkParameters = true)
    @PostMapping("/login")
    public ResponseVO<?> login(
            HttpSession session,
            @VerifyParameter(required = true) String email,
            @VerifyParameter(required = true) String password
    ) {
        try {
            SessionWebUserDto sessionWebUserDto = userInfoService.login(email, password);
            session.setAttribute(Constants.SESSION_WEB_USER_KEY, sessionWebUserDto);
            return getSuccessResponseVO(sessionWebUserDto);
        } finally {
            // 移除验证码
            session.removeAttribute(Constants.CHECK_CODE_KEY);
        }
    }
}
