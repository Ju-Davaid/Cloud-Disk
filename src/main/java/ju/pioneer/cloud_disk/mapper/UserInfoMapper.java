package ju.pioneer.cloud_disk.mapper;

import ju.pioneer.cloud_disk.entity.po.UserInfo;
import ju.pioneer.cloud_disk.entity.query.UserInfoQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserInfoMapper {
    int deleteByPrimaryKey(String userId);

    int insert(UserInfo record);

    int insertSelective(UserInfo record);

    UserInfo selectByPrimaryKey(String userId);

    int updateByPrimaryKeySelective(UserInfo record);

    int updateByPrimaryKey(UserInfo record);

    UserInfo selectByEmail(String email);

    UserInfo selectByNickName(String nickName);

    /**
     * 更新用户使用空间
     *
     * @param userId     用户ID
     * @param useSpace   使用空间
     * @param totalSpace 总空间
     * @return 更新行数
     */
    int updateUserSpace(@Param("userId") String userId, @Param("useSpace") Long useSpace, @Param("totalSpace") Long totalSpace);

    /**
     * 查询用户总数
     *
     * @param param 查询页
     * @return 用户总数
     */
    int selectCountByQuery(@Param("query") UserInfoQuery param);

    /**
     * 分页查询用户列表
     *
     * @param param 查询页
     * @return 分页查询结果
     */
    List<UserInfo> selectList(@Param("query") UserInfoQuery param);
}