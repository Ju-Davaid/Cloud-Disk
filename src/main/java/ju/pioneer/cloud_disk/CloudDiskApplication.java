package ju.pioneer.cloud_disk;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

// 项目启动入口
// 启动异步
@EnableAsync
// 扫描基础包
@SpringBootApplication(scanBasePackages = "ju.pioneer.cloud_disk")
// 启动事务管理
@EnableTransactionManagement
// 启动定时任务
@EnableScheduling
// 扫描Mapper接口
@MapperScan("ju.pioneer.cloud_disk.mapper")
// 开启CGLIB代理，避免jdk代理注解问题
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class CloudDiskApplication {
    public static void main(String[] args) {
        SpringApplication.run(CloudDiskApplication.class, args);
        System.out.println("云盘服务启动成功");
    }

}
