package ju.pioneer.cloud_disk.entity.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserInfoQuery extends BaseQuery {
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

    private SimplePage simplePage;
}
