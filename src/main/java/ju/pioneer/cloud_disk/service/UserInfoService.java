package ju.pioneer.cloud_disk.service;
import ju.pioneer.cloud_disk.entity.dto.SessionWebUserDto;
import org.springframework.web.multipart.MultipartFile;

public interface UserInfoService {

    /**
     * 注册
     *
     * @param email          邮箱
     * @param nickName       昵称
     * @param password       密码
     * @param emailCheckCode 邮箱验证码
     */
    void register(String email, String nickName, String password, String emailCheckCode);

    /**
     * 登录
     *
     * @param email    邮箱
     * @param password 密码
     * @return 登录成功后的用户信息
     */
    SessionWebUserDto login(String email, String password);

    /**
     * 重置密码
     *
     * @param email          邮箱
     * @param password       密码
     * @param emailCheckCode 邮箱验证码
     */
    void resetPassword(String email, String password, String emailCheckCode);

    /**
     * 获取用户头像
     *
     * @param userId   用户ID
     */
    String getAvatar(String userId);

    /**
     * 更新用户头像
     *
     * @param userId 用户ID
     * @param avatar 头像文件
     */
    void updateAvatar(String userId, MultipartFile avatar);

    void updatePassword(String userId, String password);
}
