package ju.pioneer.cloud_disk.entity.vo;

import lombok.Data;

import java.util.Date;

@Data
public class UserInfoVo {
    private String userId;

    private String nickName;

    private String email;

    private String qqAvatar;

    private Date joinTime;

    private Date lastLoginTime;

    private Byte status;

    private Long useSpace;

    private Long totalSpace;
}
