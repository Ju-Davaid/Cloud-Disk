package ju.pioneer.cloud_disk.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import ju.pioneer.cloud_disk.constants.Constants;
import ju.pioneer.cloud_disk.utils.CaptchaUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

// 账户控制器
@RestController("accountController")
@RequestMapping("/account")
public class AccountController{
    // 验证码图片生成
    @GetMapping("/generateCheckCodeImage")
    public void generateCheckCodeImage(HttpServletResponse response, HttpSession session,@RequestParam(defaultValue = "0") int type) throws IOException {
        Object[] captcha = CaptchaUtils.createCaptcha();
        String code = (String) captcha[0];
        BufferedImage image = (BufferedImage) captcha[1];
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setContentType("image/jpeg");
        if(type==0){
            session.setAttribute(Constants.CHECK_CODE_KEY,code);
        }else{
            session.setAttribute(Constants.CHECK_CODE_KEY_EMAIL,code);
        }
        ImageIO.write(image, "jpeg",response.getOutputStream());
    }
}
