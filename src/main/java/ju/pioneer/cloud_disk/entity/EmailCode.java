package ju.pioneer.cloud_disk.entity;

import lombok.Getter;

import java.util.Date;

@Getter
public class EmailCode extends EmailCodeKey {
    private Date createTime;

    private Boolean status;

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}