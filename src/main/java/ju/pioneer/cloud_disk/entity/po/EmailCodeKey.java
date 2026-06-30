package ju.pioneer.cloud_disk.entity.po;

import lombok.Data;

@Data
public class EmailCodeKey {
    private String email;

    private String code;
}