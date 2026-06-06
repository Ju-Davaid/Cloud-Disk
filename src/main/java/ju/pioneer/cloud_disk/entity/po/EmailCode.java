package ju.pioneer.cloud_disk.entity.po;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class EmailCode extends EmailCodeKey {
    private Date createTime;

    private int status;

}