package ju.pioneer.cloud_disk.mapper;

import ju.pioneer.cloud_disk.entity.po.FileInfo;
import ju.pioneer.cloud_disk.entity.po.FileInfoKey;
import ju.pioneer.cloud_disk.entity.query.FileInfoQuery;
import ju.pioneer.cloud_disk.entity.query.RecursiveFileInfoQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

import java.util.List;

/**
 * 文件信息 数据库操作接口
 */
public interface FileInfoMapper {

    /**
     * 根据FileIdAndUserId更新对象
     */
    Integer updateByFileIdAndUserId(@Param("bean") FileInfo fileInfo, @Param("fileId") String fileId, @Param("userId") String userId);


    /**
     * 根据FileIdAndUserId删除
     */
    Integer deleteByFileIdAndUserId(@Param("fileId") String fileId, @Param("userId") String userId);


    /**
     * 根据FileIdAndUserId获取对象
     */
    FileInfo selectByFileIdAndUserId(@Param("fileId") String fileId, @Param("userId") String userId);


    void updateFileStatusWithOldStatus(@Param("fileId") String fileId, @Param("userId") String userId, @Param("bean") FileInfo fileInfo,
                                       @Param("oldStatus") Integer oldStatus);

    void updateFileDelFlagBatch(@Param("bean") FileInfo fileInfo,
                                @Param("userId") String userId,
                                @Param("filePidList") List<String> filePidList,
                                @Param("fileIdList") List<String> fileIdList,
                                @Param("oldDelFlag") Integer oldDelFlag);


    void delFileBatch(@Param("userId") String userId,
                      @Param("filePidList") List<String> filePidList,
                      @Param("fileIdList") List<String> fileIdList,
                      @Param("oldDelFlag") Integer oldDelFlag);

    Long selectUseSpace(@Param("userId") String userId);

    void deleteFileByUserId(@Param("userId") String userId);

    /**
     * insert:(插入)
     */
    Integer insert(@Param("bean") FileInfo t);


    /**
     * insertOrUpdate:(插入或者更新)
     */
    Integer insertOrUpdate(@Param("bean") FileInfo t);


    /**
     * insertBatch:(批量插入)
     */
    Integer insertBatch(@Param("list") List<FileInfo> list);


    /**
     * insertOrUpdateBatch:(批量插入或更新)
     */
    Integer insertOrUpdateBatch(@Param("list") List<FileInfo> list);


    /**
     * selectList:(根据参数查询集合)
     */
    List<FileInfo> selectList(@Param("query") FileInfoQuery p);

    /**
     * selectCount:(根据集合查询数量). <br/>
     */
    Integer selectCount(@Param("query") FileInfoQuery p);

    /**
     * selectAllChildFile:(查询用户回收站所有子孙文件ID)
     * @param p 查询参数
     * @return 子孙文件ID列表
     */
    List<FileInfo> selectAllChildFileInfo(@Param("query") RecursiveFileInfoQuery p);
}
