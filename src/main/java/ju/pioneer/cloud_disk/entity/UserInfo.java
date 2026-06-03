package ju.pioneer.cloud_disk.entity;

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

    public void setUserId(String userId) {
        this.userId = userId == null ? null : userId.trim();
    }

    public void setNickName(String nickName) {
        this.nickName = nickName == null ? null : nickName.trim();
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }

    public void setQqOpenId(String qqOpenId) {
        this.qqOpenId = qqOpenId == null ? null : qqOpenId.trim();
    }

    public void setQqAvatar(String qqAvatar) {
        this.qqAvatar = qqAvatar == null ? null : qqAvatar.trim();
    }

    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
    }
}