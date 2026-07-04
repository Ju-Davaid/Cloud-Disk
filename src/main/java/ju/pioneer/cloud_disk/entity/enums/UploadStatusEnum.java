package ju.pioneer.cloud_disk.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UploadStatusEnum {
    UPLOAD_SECONDS("upload_seconds", "秒传"),
    UPLOADING("uploading", "上传中"),
    UPLOAD_SUCCESS("upload_success", "上传成功");

    private final String status;
    private final String desc;
}
