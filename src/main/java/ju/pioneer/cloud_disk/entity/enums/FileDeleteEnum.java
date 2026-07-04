package ju.pioneer.cloud_disk.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FileDeleteEnum {
    DEL(0, "删除"),
    RECYCLE(1, "回收站"),
    USING(2, "使用中");

    private final int flag;
    private final String desc;
}
