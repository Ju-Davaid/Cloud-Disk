package ju.pioneer.cloud_disk.entity.po;

import lombok.Data;

import java.util.Date;

@Data
public class UserInfo {
    private String userId;

    private String nickName;

    private String email;

    private String qqOpenId;

    private String qqAvatar;

    private String password;
    private Date joinTime;

    private Date lastLoginTime;

    private Byte status;

    private Long useSpace;

    private Long totalSpace;
}