package ju.pioneer.cloud_disk.entity.po;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class FileInfo extends FileInfoKey {
    private String fileMd5;

    private String filePid;

    private Long fileSize;

    private String fileName;

    private String fileCover;

    private String filePath;

    private Date createTime;

    private Date lastUpdateTime;

    private Integer folderType;

    private Integer fileCategory;

    private Integer fileType;

    private Integer status;

    private Date recoveryTime;

    private Integer delFlag;
}