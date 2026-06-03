package ju.pioneer.cloud_disk.mapper;

import ju.pioneer.cloud_disk.entity.po.EmailCode;
import ju.pioneer.cloud_disk.entity.po.EmailCodeKey;

public interface EmailCodeMapper {
    int deleteByPrimaryKey(EmailCodeKey key);

    int insert(EmailCode record);

    int insertSelective(EmailCode record);

    EmailCode selectByPrimaryKey(EmailCodeKey key);

    int updateByPrimaryKeySelective(EmailCode record);

    int updateByPrimaryKey(EmailCode record);
}