package ju.pioneer.cloud_disk.entity.dto;


import lombok.Data;

@Data
public class SessionWebUserDto {
    private String userName;
    private String userId;
    private boolean isAdmin;
    private String avatar;
}
