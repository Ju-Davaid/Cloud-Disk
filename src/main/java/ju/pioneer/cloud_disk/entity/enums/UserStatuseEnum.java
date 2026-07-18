package ju.pioneer.cloud_disk.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserStatuseEnum {
    ENABLE(1, "启用"),
    DISABLE(0, "禁用");

    private final Integer status;
    private final String desc;

    public UserStatuseEnum getByStatus(Integer status) {
        for (UserStatuseEnum value : values()) {
            if (value.getStatus() == status) {
                return value;
            }
        }
        return null;
    }

}
