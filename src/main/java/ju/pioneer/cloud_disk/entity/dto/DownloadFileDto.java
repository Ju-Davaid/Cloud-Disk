package ju.pioneer.cloud_disk.entity.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DownloadFileDto implements Serializable {
    private String code;
    private String fileId;
    private String fileName;
    private String filePath;
}
