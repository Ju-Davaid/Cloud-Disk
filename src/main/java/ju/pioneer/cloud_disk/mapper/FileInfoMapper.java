package ju.pioneer.cloud_disk.mapper;

import ju.pioneer.cloud_disk.entity.FileInfo;
import ju.pioneer.cloud_disk.entity.FileInfoKey;

public interface FileInfoMapper {
    int deleteByPrimaryKey(FileInfoKey key);

    int insert(FileInfo record);

    int insertSelective(FileInfo record);

    FileInfo selectByPrimaryKey(FileInfoKey key);

    int updateByPrimaryKeySelective(FileInfo record);

    int updateByPrimaryKey(FileInfo record);
}