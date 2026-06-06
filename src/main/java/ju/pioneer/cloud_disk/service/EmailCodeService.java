package ju.pioneer.cloud_disk.service;

import jakarta.mail.MessagingException;
import ju.pioneer.cloud_disk.entity.po.EmailCode;


public interface EmailCodeService {
    /**
     * 新增邮箱验证码
     */
    EmailCode add(EmailCode bean);

    /**
     * 发送邮箱验证码
     * @param email 邮箱
     * @param type 类型
     */
    void sendEmailCode(String email,int type);
}
