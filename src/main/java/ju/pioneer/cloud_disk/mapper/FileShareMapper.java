package ju.pioneer.cloud_disk.mapper;

import ju.pioneer.cloud_disk.entity.FileShare;

public interface FileShareMapper {
    int deleteByPrimaryKey(String shareId);

    int insert(FileShare record);

    int insertSelective(FileShare record);

    FileShare selectByPrimaryKey(String shareId);

    int updateByPrimaryKeySelective(FileShare record);

    int updateByPrimaryKey(FileShare record);
}