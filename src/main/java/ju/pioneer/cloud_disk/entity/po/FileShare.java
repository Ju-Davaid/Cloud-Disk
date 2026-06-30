package ju.pioneer.cloud_disk.entity.po;

import lombok.Data;
import java.util.Date;


@Data
public class FileShare {
    private String shareId;

    private String fileId;

    private String userId;

    private Boolean validType;

    private Date expireTime;

    private Date shareTime;

    private String code;

    private Integer showCount;
}