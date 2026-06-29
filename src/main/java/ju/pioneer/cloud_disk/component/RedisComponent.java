package ju.pioneer.cloud_disk.component;

import jakarta.annotation.Resource;
import ju.pioneer.cloud_disk.constants.Constants;
import ju.pioneer.cloud_disk.entity.dto.UserSpaceDto;
import org.springframework.stereotype.Component;

@Component("redisComponent")
public class RedisComponent {

    @Resource
    private RedisUtils<Object> redisUtils;

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
        if (userSpaceDto == null) {
            userSpaceDto = new UserSpaceDto();
            userSpaceDto.setUseSpace(0L);
            userSpaceDto.setTotalSpace(Constants.DEFAULT_SPACE);
            saveUserSpaceUse(userId, userSpaceDto);
        }
        return userSpaceDto;
    }
}
