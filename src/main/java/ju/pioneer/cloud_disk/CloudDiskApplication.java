package ju.pioneer.cloud_disk;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

// 项目启动入口
// 启动异步
@EnableAsync
@SpringBootApplication(scanBasePackages = "ju.pioneer.cloud_disk")
// 启动事务管理
@EnableTransactionManagement
// 启动定时任务
@EnableScheduling
// 扫描Mapper接口
@MapperScan("ju.pioneer.cloud_disk.mapper")
public class CloudDiskApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudDiskApplication.class, args);
        System.out.println("云盘服务启动成功");
    }

}
