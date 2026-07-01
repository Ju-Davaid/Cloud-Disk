package ju.pioneer.cloud_disk.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserStatuseEnum {
    ENABLE((byte) 1, "启用"),
    DISABLE((byte) 0, "禁用");

    private final byte status;
    private final String desc;

    public UserStatuseEnum getByStatus(byte status) {
        for (UserStatuseEnum value : values()) {
            if (value.getStatus() == status) {
                return value;
            }
        }
        return null;
    }

}
