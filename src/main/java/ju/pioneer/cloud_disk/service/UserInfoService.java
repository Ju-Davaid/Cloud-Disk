package ju.pioneer.cloud_disk.service;

import ju.pioneer.cloud_disk.entity.dto.SessionWebUserDto;
import ju.pioneer.cloud_disk.entity.po.UserInfo;
import ju.pioneer.cloud_disk.entity.query.UserInfoQuery;
import ju.pioneer.cloud_disk.entity.vo.FileInfoVo;
import ju.pioneer.cloud_disk.entity.vo.PaginateResultVo;
import ju.pioneer.cloud_disk.entity.vo.UserInfoVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserInfoService {


    UserInfo selectById(String userId);

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
     * @param userId 用户ID
     */
    String getAvatar(String userId);

    /**
     * 更新用户头像
     *
     * @param userId 用户ID
     * @param avatar 头像文件
     */
    void updateAvatar(String userId, MultipartFile avatar);

    /**
     * 更新用户密码
     *
     * @param userId   用户ID
     * @param password 密码
     */
    void updatePassword(String userId, String password);

    /**
     * 分页查询用户列表
     *
     * @param param 查询页
     * @return 分页查询结果
     */
    PaginateResultVo<UserInfoVo> selectList(UserInfoQuery param);

    /**
     * 更新用户状态
     *
     * @param userId 用户ID
     * @param status 状态
     */
    void updateStatus(String userId, Integer status);

    void updateUserSpace(String userId, Integer space);
}
