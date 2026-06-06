package ju.pioneer.cloud_disk.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
@Getter
@Component("appConfig")
public class AppConfig {
    @Value("${spring.mail.username:}")
    private String senderUsername;
    @Value("${spring.application.name:}")
    private String appName;
}
