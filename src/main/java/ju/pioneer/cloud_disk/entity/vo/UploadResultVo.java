package ju.pioneer.cloud_disk.entity.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class UploadResultVo implements Serializable {
    private String fileId;
    private String status;
}
