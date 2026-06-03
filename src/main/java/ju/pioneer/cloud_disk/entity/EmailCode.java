package ju.pioneer.cloud_disk.entity;

import java.util.Date;

public class EmailCode extends EmailCodeKey {
    private Date createTime;

    private Boolean status;

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}