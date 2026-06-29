package ju.pioneer.cloud_disk.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component("appConfig")
public class AppConfig {
    /**
     * 发送邮件的用户名
     */
    @Value("${spring.mail.username:}")
    private String senderUsername;
    /**
     * 应用名称
     */
    @Value("${spring.application.name:}")
    private String appName;
    /**
     * 管理员邮箱
     */
    @Value("${admin.emails:}")
    private String adminEmails;
    /**
     * 项目文件夹路径
     */
    @Value("${project.folder:}")
    private String projectFolder;
}
