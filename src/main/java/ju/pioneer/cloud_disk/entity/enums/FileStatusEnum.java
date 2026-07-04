package ju.pioneer.cloud_disk.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileStatusEnum {
    TRANSFORMING(0, "转码中"),
    TRANSFORM_FAIL(1, "转码失败"),
    USING(2, "使用中");

    private final int status;
    private final String desc;
}
