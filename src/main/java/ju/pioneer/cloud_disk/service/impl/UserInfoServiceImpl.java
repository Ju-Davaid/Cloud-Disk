package ju.pioneer.cloud_disk.service.impl;

import jakarta.annotation.Resource;
import ju.pioneer.cloud_disk.component.RedisComponent;
import ju.pioneer.cloud_disk.config.AppConfig;
import ju.pioneer.cloud_disk.constants.Constants;
import ju.pioneer.cloud_disk.entity.dto.SessionWebUserDto;
import ju.pioneer.cloud_disk.entity.dto.UserSpaceDto;
import ju.pioneer.cloud_disk.entity.enums.UserStatuseEnum;
import ju.pioneer.cloud_disk.entity.po.UserInfo;
import ju.pioneer.cloud_disk.exception.BusinessException;
import ju.pioneer.cloud_disk.mapper.UserInfoMapper;
import ju.pioneer.cloud_disk.service.EmailCodeService;
import ju.pioneer.cloud_disk.service.UserInfoService;
import ju.pioneer.cloud_disk.utils.StringTools;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;

@Service
public class UserInfoServiceImpl implements UserInfoService {
    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private EmailCodeService emailService;
    @Resource
    private AppConfig appConfig;
    @Resource
    private RedisComponent redisComponent;
    private static final Logger logger = LoggerFactory.getLogger(UserInfoServiceImpl.class);

    /**
     * 注册
     *
     * @param email          邮箱
     * @param nickName       昵称
     * @param password       密码
     * @param emailCheckCode 邮箱验证码
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(String email, String nickName, String password, String emailCheckCode) {
        // 检查邮箱是否已注册
        UserInfo existingUser = userInfoMapper.selectByEmail(email);
        if (existingUser != null) {
            throw new BusinessException("邮箱已注册");
        }
        // 检查昵称是否已注册
        existingUser = userInfoMapper.selectByNickName(nickName);
        if (existingUser != null) {
            throw new BusinessException("昵称已存在");
        }
        // 校验邮箱验证码
        emailService.checkEmailCode(email, emailCheckCode);
        // 注册用户
        UserInfo user = new UserInfo();
        user.setUserId(StringTools.getUUID());
        user.setEmail(email);
        user.setNickName(nickName);
        user.setPassword(StringTools.encryptPassword(password));
        user.setJoinTime(new Date());
        user.setStatus(UserStatuseEnum.ENABLE.getStatus());
        user.setUseSpace(0L);
        user.setTotalSpace(Constants.DEFAULT_SPACE);
        userInfoMapper.insert(user);
    }

    /**
     * 登录
     *
     * @param email    邮箱
     * @param password 密码
     * @return 登录成功后的用户信息
     */
    @Override
    public SessionWebUserDto login(String email, String password) {
        UserInfo user = userInfoMapper.selectByEmail(email);
        logger.info("登录邮箱：{}", email);
        logger.info("登录密码：{}", password);
        if (user == null || !StringTools.checkPassword(password, user.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }
        if (user.getStatus() != UserStatuseEnum.ENABLE.getStatus()) {
            throw new BusinessException("该用户已被禁用");
        }
        UserInfo updateUserInfo = new UserInfo();
        updateUserInfo.setLastLoginTime(new Date());
        updateUserInfo.setUserId(user.getUserId());
        userInfoMapper.updateByPrimaryKeySelective(updateUserInfo);
        // 登录成功，返回用户信息
        SessionWebUserDto sessionWebUserDto = new SessionWebUserDto();
        sessionWebUserDto.setUserId(user.getUserId());
        sessionWebUserDto.setUserName(user.getNickName());
        sessionWebUserDto.setAvatar(user.getQqAvatar());
        // 检查是否为管理员
        sessionWebUserDto.setAdmin(ArrayUtils.contains(appConfig.getAdminEmails().split(","), user.getEmail()));
        // 设置用户空间信息
        UserSpaceDto userSpaceDto = new UserSpaceDto();
        userSpaceDto.setUseSpace(user.getUseSpace());
        userSpaceDto.setTotalSpace(user.getTotalSpace());
        redisComponent.saveUserSpaceUse(user.getUserId(), userSpaceDto);
        return sessionWebUserDto;
    }

    /**
     * 重置密码
     *
     * @param email          邮箱
     * @param password       密码
     * @param emailCheckCode 邮箱验证码
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(String email, String password, String emailCheckCode) {
        UserInfo user = userInfoMapper.selectByEmail(email);
        if (user == null) {
            throw new BusinessException("邮箱账号不存在");
        }
        emailService.checkEmailCode(email, emailCheckCode);
        user.setPassword(StringTools.encryptPassword(password));
        UserInfo updateUserInfo = new UserInfo();
        updateUserInfo.setUserId(user.getUserId());
        updateUserInfo.setPassword(user.getPassword());
        userInfoMapper.updateByPrimaryKeySelective(updateUserInfo);
    }

    /**
     * 获取用户头像路径
     *
     * @param userId 用户ID
     * @return 用户头像路径
     */
    public String getAvatar(String userId) {
        String avatarFolderName = Constants.FILE_FOLDER + Constants.AVATAR_FOLDER;
        File avatarFolder = new File(appConfig.getProjectFolder() + avatarFolderName);
        if (!avatarFolder.exists()) {
            boolean isCreate = avatarFolder.mkdirs();
            if (!isCreate) {
                throw new BusinessException("创建头像文件夹失败");
            }
        }
        return getAvatar(userId, avatarFolderName);
    }

    /**
     * 获取用户头像路径
     *
     * @param userId           用户ID
     * @param avatarFolderName 头像文件夹名称
     * @return 用户头像路径
     */
    private String getAvatar(String userId, String avatarFolderName) {
        String avatarPath = appConfig.getProjectFolder() + avatarFolderName + userId + Constants.AVATAR_EXTENSION_NAME;
        File avatarFile = new File(avatarPath);
        File defaultAvatarFile = new File(appConfig.getProjectFolder() + avatarFolderName + Constants.DEFAULT_AVATAR);
        if (!avatarFile.exists()) {
            if (!defaultAvatarFile.exists()) {
                throw new BusinessException("用户默认头像不存在");
            }
            avatarPath = defaultAvatarFile.getAbsolutePath();
        }
        return avatarPath;
    }

    /**
     * 更新用户头像
     *
     * @param userId 用户ID
     * @param avatar 用户头像
     */
    @Override
    public void updateAvatar(String userId, MultipartFile avatar) {
        File userAvatarFile = new File(getAvatar(userId));
        File targetFile = new File(userAvatarFile.getParent() + File.separator + userId + Constants.AVATAR_EXTENSION_NAME);
        logger.info("用户头像文件夹路径：{}, 头像文件夹是否存在：{}", targetFile, targetFile.exists());
        try {
            avatar.transferTo(targetFile);
        } catch (IOException e) {
            logger.error("上传头像失败", e);
            throw new BusinessException("上传头像失败");
        }
        UserInfo newUserInfo = new UserInfo();
        newUserInfo.setUserId(userId);
        newUserInfo.setQqAvatar("");
        userInfoMapper.updateByPrimaryKeySelective(newUserInfo);
    }

    /**
     * 更新用户密码
     *
     * @param userId   用户ID
     * @param password 密码
     */
    @Override
    public void updatePassword(String userId, String password) {
        UserInfo newUserInfo = new UserInfo();
        newUserInfo.setUserId(userId);
        newUserInfo.setPassword(StringTools.encryptPassword(password));
        userInfoMapper.updateByPrimaryKeySelective(newUserInfo);
    }

}
