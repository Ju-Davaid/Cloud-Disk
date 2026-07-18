package ju.pioneer.cloud_disk.component;

import jakarta.annotation.Resource;
import ju.pioneer.cloud_disk.constants.Constants;
import ju.pioneer.cloud_disk.entity.dto.DownloadFileDto;
import ju.pioneer.cloud_disk.entity.dto.UserSpaceDto;
import ju.pioneer.cloud_disk.entity.po.UserInfo;
import ju.pioneer.cloud_disk.mapper.FileInfoMapper;
import ju.pioneer.cloud_disk.mapper.UserInfoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("redisComponent")
public class RedisComponent {

    @Resource
    private RedisUtils<Object> redisUtils;
    @Resource
    FileInfoMapper fileInfoMapper;
    @Resource
    private UserInfoMapper userInfoMapper;

    private final Logger logger = LoggerFactory.getLogger(RedisComponent.class);

    /**
     * 保存用户空间使用量
     *
     * @param userId       用户ID
     * @param userSpaceDto 用户空间DTO
     */
    public void saveUserSpaceUse(String userId, UserSpaceDto userSpaceDto) {
        redisUtils.setExpireTime(String.format(Constants.REDIS_USER_SPACE_USE_KEY, userId), userSpaceDto, Constants.REDIS_KEY_EXPIRE_TIME);
    }

    /**
     * 获取用户空间使用量
     *
     * @param userId 用户ID
     * @return 用户空间DTO
     */
    public UserSpaceDto getUserSpaceUse(String userId) {
        UserSpaceDto userSpaceDto = (UserSpaceDto) redisUtils.get(String.format(Constants.REDIS_USER_SPACE_USE_KEY, userId));
        logger.info("getUserSpaceUse userId[{}],userSpaceDto[{}]", userId, userSpaceDto);
        if (userSpaceDto == null) {
            long useSpace = fileInfoMapper.selectUseSpace(userId);
            userSpaceDto = new UserSpaceDto();
            userSpaceDto.setUseSpace(useSpace);
            userSpaceDto.setTotalSpace(Constants.DEFAULT_SPACE);
            saveUserSpaceUse(userId, userSpaceDto);
        }
        return userSpaceDto;
    }

    public void resetUserSpaceUse(String userId) {
        UserSpaceDto userSpaceDto = new UserSpaceDto();
        long useSpace = fileInfoMapper.selectUseSpace(userId);
        userSpaceDto.setUseSpace(useSpace);
        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(userId);
        userSpaceDto.setTotalSpace(userInfo.getTotalSpace());
        saveUserSpaceUse(userId, userSpaceDto);
    }

    /**
     * 获取临时文件大小
     *
     * @param userId 用户ID
     * @param fileId 文件ID
     * @return 文件大小
     */
    public Long getTempFileSize(String userId, String fileId) {
        return getFileSizeFromRedis(userId, fileId);
    }

    /**
     * 设置临时文件大小
     *
     * @param userId   用户ID
     * @param fileId   文件ID
     * @param fileSize 文件大小
     */
    public void setTempFileSize(String userId, String fileId, Long fileSize) {
        Long currentSize = getTempFileSize(userId, fileId);
        String key = String.format(Constants.REDIS_TEMP_FILE_SIZE_KEY, userId, fileId);
        redisUtils.setExpireTime(key, currentSize + fileSize, Constants.REDIS_TEMP_FILE_SIZE_EXPIRE_TIME);

    }

    /**
     * 从Redis中获取文件大小
     *
     * @param userId 用户ID
     * @param fileId 文件ID
     * @return 文件大小
     */
    private Long getFileSizeFromRedis(String userId, String fileId) {
        String key = String.format(Constants.REDIS_TEMP_FILE_SIZE_KEY, userId, fileId);
        Object sizeObj = redisUtils.get(key);
        if (sizeObj == null) {
            return 0L;
        }
        if (sizeObj instanceof Integer) {
            return ((Integer) sizeObj).longValue();
        } else if (sizeObj instanceof Long) {
            return (Long) sizeObj;
        }
        return 0L;
    }

    /**
     * 保存下载码
     *
     * @param code            下载码
     * @param downloadFileDto 下载文件DTO
     */
    public void saveDownloadCode(String code, DownloadFileDto downloadFileDto) {
        redisUtils.setExpireTime(String.format(Constants.REDIS_DOWNLOAD_CODE_KEY, code), downloadFileDto, Constants.REDIS_DOWNLOAD_CODE_EXPIRE_TIME);
    }

    /**
     * 获取下载码
     *
     * @param code 下载码
     * @return 下载文件DTO
     */
    public DownloadFileDto getDownloadCode(String code) {
        return (DownloadFileDto) redisUtils.get(String.format(Constants.REDIS_DOWNLOAD_CODE_KEY, code));
    }
}
