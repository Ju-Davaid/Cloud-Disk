package ju.pioneer.cloud_disk.entity.enums;

import lombok.Getter;

@Getter
public enum PageSizeEnum {
    SIZE15(15),
    SIZE20(20);

    private final int size;

    PageSizeEnum(int size) {
        this.size = size;
    }
}
