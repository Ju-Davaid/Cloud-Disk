package ju.pioneer.cloud_disk.entity.dto;

import lombok.Data;

import java.util.Date;

@Data
public class SessionShareDto {
    private String shareId;
    private String userId;
    private String fileId;
    private Date expireTime;
}
