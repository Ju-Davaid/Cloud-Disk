package ju.pioneer.cloud_disk.controller;

import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import ju.pioneer.cloud_disk.constants.Constants;
import ju.pioneer.cloud_disk.entity.vo.ResponseVO;
import ju.pioneer.cloud_disk.exception.BusinessException;
import ju.pioneer.cloud_disk.service.EmailCodeService;
import ju.pioneer.cloud_disk.utils.CaptchaUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

// 账户控制器
@RestController("accountController")
public class AccountController extends BaseController{
    @Resource
    private EmailCodeService emailCodeService;
    // 验证码图片生成
    @GetMapping("/generateCheckCodeImage")
    public void generateCheckCodeImage(HttpServletResponse response, HttpSession session,@RequestParam(defaultValue = "0") int type) throws IOException {
        Object[] captcha = CaptchaUtils.createCaptcha();
        String code = (String) captcha[0];
        BufferedImage image = (BufferedImage) captcha[1];
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setContentType("image/jpeg");
        response.setDateHeader("Expires", 0);
        if(type==Constants.ZERO){
            session.setAttribute(Constants.CHECK_CODE_KEY,code);
        }else{
            session.setAttribute(Constants.CHECK_CODE_KEY_EMAIL,code);
        }
        try (ServletOutputStream out = response.getOutputStream()) {
            ImageIO.write(image, "jpeg", out);
            out.flush();
        }catch (IOException e){
            throw new BusinessException("验证码图片生成失败");
        }
    }
    // 发送邮箱验证码
    @GetMapping("/sendEmailCode")
    public ResponseVO<Object> sendEmailCode(HttpSession session, @RequestParam String email, @RequestParam String checkCode , @RequestParam int type)  {
        try{
            String checkCodeSession = (String) session.getAttribute(Constants.CHECK_CODE_KEY_EMAIL);
            if(checkCodeSession==null){
                throw new BusinessException("请先获取验证码");
            }
            if(!checkCodeSession.equalsIgnoreCase(checkCode)){
                throw new BusinessException("验证码错误");
            }
            emailCodeService.sendEmailCode(email,type);
            return getSuccessResponseVO(null);
        }finally {
            // 移除验证码
            session.removeAttribute(Constants.CHECK_CODE_KEY_EMAIL);
        }
    }
}
