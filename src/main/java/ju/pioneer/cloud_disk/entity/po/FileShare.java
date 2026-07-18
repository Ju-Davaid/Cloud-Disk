package ju.pioneer.cloud_disk.entity.po;

import lombok.Data;

import java.util.Date;


@Data
public class FileShare {
    private String shareId;

    private String fileId;

    private String userId;

    private Integer validType;

    private Date expireTime;

    private Date shareTime;

    private String code;

    private Integer showCount;

    private String fileName;

    private Integer folderType;

    private Integer fileCategory;

    private Integer fileType;

    private String fileCover;
}