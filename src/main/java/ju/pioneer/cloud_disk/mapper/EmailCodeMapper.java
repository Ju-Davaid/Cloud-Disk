package ju.pioneer.cloud_disk.mapper;

import ju.pioneer.cloud_disk.entity.po.EmailCode;
import ju.pioneer.cloud_disk.entity.po.EmailCodeKey;
import org.apache.ibatis.annotations.Param;

public interface EmailCodeMapper {
    /**
     * 根据主键删除邮箱验证码
     * @param key 主键
     * @return 影响行数
     */
    int deleteByPrimaryKey(EmailCodeKey key);

    /**
     * 添加邮箱验证码
     * @param  record 邮箱验证码
     * @return  影响行数
     */
    int insert(EmailCode record);

    /**
     * 添加邮箱验证码
     * @param  record 邮箱验证码
     * @return int 影响行数
     */
    int insertSelective(EmailCode record);

    /**
     * 根据主键查询邮箱验证码
     * @param key 主键
     * @return 邮箱验证码
     */
    EmailCode selectByPrimaryKey(EmailCodeKey key);

    /**
     * 更新邮箱验证码
     * @param  record 邮箱验证码
     * @return  影响行数
     */
    int updateByPrimaryKeySelective(EmailCode record);

    /**
     * 更新邮箱验证码
     * @param  record 邮箱验证码
     * @return  影响行数
     */
    int updateByPrimaryKey(EmailCode record);

    /**
     * 禁用邮箱验证码
     * @param email 邮箱
     */
    void disableEmailCode(@Param("email") String email);

    /**
     * 启用邮箱验证码
     * @param email 邮箱
     */
    void enableEmailCode(@Param("email") String email);
}