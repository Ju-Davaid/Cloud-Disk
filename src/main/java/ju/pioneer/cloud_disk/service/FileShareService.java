package ju.pioneer.cloud_disk.service;

import ju.pioneer.cloud_disk.entity.po.FileShare;
import ju.pioneer.cloud_disk.entity.query.FileShareQuery;
import ju.pioneer.cloud_disk.entity.vo.PaginateResultVo;

import java.util.List;


/**
 * 分享信息 业务接口
 */
public interface FileShareService {

    /**
     * 根据条件查询列表
     */
    List<FileShare> findListByParam(FileShareQuery param);

    /**
     * 根据条件查询列表
     */
    Integer findCountByParam(FileShareQuery param);

    /**
     * 分页查询
     */
    PaginateResultVo<FileShare> findListByPage(FileShareQuery param);

    /**
     * 新增
     */
    Integer add(FileShare bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<FileShare> listBean);

    /**
     * 批量新增/修改
     */
    Integer addOrUpdateBatch(List<FileShare> listBean);

    /**
     * 根据ShareId查询对象
     */
    FileShare getFileShareByShareId(String shareId);


    /**
     * 根据ShareId修改
     */
    Integer updateFileShareByShareId(FileShare bean);


    /**
     * 根据ShareId删除
     */
    Integer deleteFileShareByShareId(String shareId);

    FileShare saveShare(FileShare share);

    void deleteFileShareBatch(String[] shareIdArray, String userId);

//    SessionShareDto checkShareCode(  String shareId,String code);
}