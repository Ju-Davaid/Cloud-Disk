package ju.pioneer.cloud_disk.entity.po;

import lombok.Getter;

@Getter
public class EmailCodeKey {
    private String email;

    private String code;

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }

    public void setCode(String code) {
        this.code = code == null ? null : code.trim();
    }
}