package ju.pioneer.cloud_disk.mapper;

import ju.pioneer.cloud_disk.entity.EmailCode;
import ju.pioneer.cloud_disk.entity.EmailCodeKey;

public interface EmailCodeMapper {
    int deleteByPrimaryKey(EmailCodeKey key);

    int insert(EmailCode record);

    int insertSelective(EmailCode record);

    EmailCode selectByPrimaryKey(EmailCodeKey key);

    int updateByPrimaryKeySelective(EmailCode record);

    int updateByPrimaryKey(EmailCode record);
}