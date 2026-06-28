package ju.pioneer.cloud_disk.entity.enums;

import lombok.Getter;

@Getter
public enum UserStatuseEnum {
    ENABLE((byte) 1, "启用"),
    DISABLE((byte) 0, "禁用");

    private final byte status;
    private final String desc;

    UserStatuseEnum(byte status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public UserStatuseEnum getByStatus(byte status) {
        for (UserStatuseEnum value : values()) {
            if (value.getStatus() == status) {
                return value;
            }
        }
        return null;
    }

}
