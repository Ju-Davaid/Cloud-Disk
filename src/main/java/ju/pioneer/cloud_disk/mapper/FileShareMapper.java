package ju.pioneer.cloud_disk.mapper;

import ju.pioneer.cloud_disk.entity.po.FileShare;
import ju.pioneer.cloud_disk.entity.query.FileShareQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FileShareMapper {
    int deleteByPrimaryKey(String shareId);

    int insert(FileShare record);

    int insertSelective(FileShare record);

    FileShare selectByPrimaryKey(String shareId);

    int updateByPrimaryKeySelective(FileShare record);

    int updateByPrimaryKey(FileShare record);


    Integer deleteFileShareBatch(@Param("shareIdArray") String[] shareIdArray, @Param("userId") String userId);

    void updateShareShowCount(@Param("shareId") String shareId);

    List<FileShare> selectList(@Param("query") FileShareQuery q);

    Integer selectCount(@Param("query") FileShareQuery q);

    Integer insertBatch(List<FileShare> listBean);

    Integer insertOrUpdateBatch(List<FileShare> listBean);

    void insertOrUpdate(@Param("query") FileShareQuery query);
}