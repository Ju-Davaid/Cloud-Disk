package ju.pioneer.cloud_disk.entity.po;

import lombok.Getter;

@Getter
public class FileInfoKey {
    private String fileId;

    private String userId;

    public void setFileId(String fileId) {
        this.fileId = fileId == null ? null : fileId.trim();
    }

    public void setUserId(String userId) {
        this.userId = userId == null ? null : userId.trim();
    }
}