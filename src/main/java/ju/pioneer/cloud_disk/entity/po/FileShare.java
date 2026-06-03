package ju.pioneer.cloud_disk.entity.po;

import lombok.Getter;

import java.util.Date;

@Getter
public class FileShare {
    private String shareId;

    private String fileId;

    private String userId;

    private Boolean validType;

    private Date expireTime;

    private Date shareTime;

    private String code;

    private Integer showCount;

    public void setShareId(String shareId) {
        this.shareId = shareId == null ? null : shareId.trim();
    }

    public void setFileId(String fileId) {
        this.fileId = fileId == null ? null : fileId.trim();
    }

    public void setUserId(String userId) {
        this.userId = userId == null ? null : userId.trim();
    }

    public void setValidType(Boolean validType) {
        this.validType = validType;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

    public void setShareTime(Date shareTime) {
        this.shareTime = shareTime;
    }

    public void setCode(String code) {
        this.code = code == null ? null : code.trim();
    }

    public void setShowCount(Integer showCount) {
        this.showCount = showCount;
    }
}