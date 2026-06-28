package ju.pioneer.cloud_disk.service;

import ju.pioneer.cloud_disk.entity.dto.SessionWebUserDto;

public interface UserInfoService {

    /**
     * 注册
     * @param email 邮箱
     * @param nickName 昵称
     * @param password 密码
     * @param emailCheckCode 邮箱验证码
     */
    void register(String email, String nickName, String password, String emailCheckCode);
    /**
     * 登录
     * @param email 邮箱
     * @param password 密码
     * @return 登录成功后的用户信息
     */
    SessionWebUserDto login(String email, String password);
}
