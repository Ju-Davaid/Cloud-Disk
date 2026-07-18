package ju.pioneer.cloud_disk.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ShareValidTypeEnum {
    ONE_DAY(0, 1, "1天"),
    ONE_WEEK(1, 7, "7天"),
    ONE_MONTH(2, 30, "30天"),
    FOREVER(3, 0, "永久");

    private final Integer type;
    private final Integer days;
    private final String desc;

    public static ShareValidTypeEnum getByType(Integer type) {
        for (ShareValidTypeEnum item : ShareValidTypeEnum.values()) {
            if (item.getType().equals(type)) {
                return item;
            }
        }
        return null;
    }
}
