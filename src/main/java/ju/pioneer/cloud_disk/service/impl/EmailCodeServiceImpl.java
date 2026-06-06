package ju.pioneer.cloud_disk.service.impl;

import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import ju.pioneer.cloud_disk.config.AppConfig;
import ju.pioneer.cloud_disk.constants.Constants;
import ju.pioneer.cloud_disk.entity.po.EmailCode;
import ju.pioneer.cloud_disk.entity.po.UserInfo;
import ju.pioneer.cloud_disk.exception.BusinessException;
import ju.pioneer.cloud_disk.mapper.EmailCodeMapper;
import ju.pioneer.cloud_disk.mapper.UserInfoMapper;
import ju.pioneer.cloud_disk.service.EmailCodeService;
import ju.pioneer.cloud_disk.utils.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class EmailCodeServiceImpl implements EmailCodeService {
    private static final Logger logger = LoggerFactory.getLogger(EmailCodeServiceImpl.class.getName());
    @Resource
    private EmailCodeMapper emailCodeMapper;
    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private JavaMailSender javaMailSender;
    @Resource
    private AppConfig appConfig;

    @Override
    public EmailCode add(EmailCode bean) {
        try {
            emailCodeMapper.insert(bean);
        } catch (Exception e) {
            throw new BusinessException("添加邮箱验证码失败");
        }
        return bean;
    }

    /**
     * 发送邮箱验证码
     * @param email 邮箱
     * @param type 类型
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void sendEmailCode(String email, int type) {
        // 注册
        if(type == Constants.ZERO){
            UserInfo userInfo = userInfoMapper.selectByEmail(email);
            if(userInfo!=null){
                throw new BusinessException("邮箱已注册");
            }
        }
        String code = StringTools.getRandomNumber(Constants.LENGTH_EMAIL_CODE);
        // 发送验证码
        sendEmailCode(email,code);
        // 将之前的验证码失效化
        emailCodeMapper.disableEmailCode(email);
        EmailCode emailCode = new EmailCode();
        emailCode.setEmail(email);
        emailCode.setCode(code);
        emailCode.setStatus(Constants.ZERO);
        emailCode.setCreateTime(new Date());
        this.add(emailCode);
    }

    /**
     * 发送邮箱验证码
     * @param email 邮箱
     * @param code 验证码
     */
    private void sendEmailCode(String email,String code) {
       try {
           MimeMessage mimeMessage=javaMailSender.createMimeMessage();
           // 发送邮件
           MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage,true,"UTF-8");
           mimeMessageHelper.setFrom(appConfig.getSenderUsername());
           mimeMessageHelper.setTo(email);
           mimeMessageHelper.setSubject(appConfig.getAppName()+"邮箱验证码");
           mimeMessageHelper.setText("您的验证码为："+code);
           mimeMessage.setSentDate(new Date());
           javaMailSender.send(mimeMessage);
           logger.info("发送邮箱验证码成功");
       }catch (MessagingException e){
           logger.error("发送邮箱验证码失败",e);
           throw new BusinessException("发送邮箱验证码失败");
       }
    }
}
