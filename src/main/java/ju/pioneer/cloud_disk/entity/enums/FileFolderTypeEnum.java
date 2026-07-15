package ju.pioneer.cloud_disk.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileFolderTypeEnum {
    FILE(0, "文件"), FOLDER(1, "文件夹");
    private final Integer type;
    private final String desc;
}
