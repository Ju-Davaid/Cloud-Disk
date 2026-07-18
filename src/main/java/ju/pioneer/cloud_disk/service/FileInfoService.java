package ju.pioneer.cloud_disk.service;

import ju.pioneer.cloud_disk.entity.dto.SessionWebUserDto;
import ju.pioneer.cloud_disk.entity.po.FileInfo;
import ju.pioneer.cloud_disk.entity.query.FileInfoQuery;
import ju.pioneer.cloud_disk.entity.vo.FileInfoVo;
import ju.pioneer.cloud_disk.entity.vo.PaginateResultVo;
import ju.pioneer.cloud_disk.entity.vo.UploadResultVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileInfoService {

    /**
     * 分页查询文件信息
     *
     * @param param 查询参数
     * @return 分页结果Vo<FileInfoVo>
     */
    PaginateResultVo<FileInfoVo> findListByPage(FileInfoQuery param);

    /**
     * 根据参数查询文件信息列表
     *
     * @param param 查询参数
     * @return 文件信息列表
     */
    List<FileInfo> findListByParam(FileInfoQuery param);

    /**
     * 根据参数查询文件信息数量
     *
     * @param param 查询参数
     * @return 文件信息数量
     */
    Integer findCountByParam(FileInfoQuery param);

    /**
     * 上传文件
     *
     * @param sessionWebUserDto 会话用户dto
     * @param fileId            文件id
     * @param file              文件
     * @param fileName          文件名
     * @param parentId          父文件id
     * @param fileMd5           文件md5值
     * @param chunkSize         分块大小
     * @param chunkIndex        分块索引
     * @return 上传结果vo
     */
    UploadResultVo uploadFile(SessionWebUserDto sessionWebUserDto, String fileId, MultipartFile file, String fileName, String parentId, String fileMd5, int chunkSize, int chunkIndex);

    /**
     * 传输文件到目标目录
     *
     * @param fileId            文件id
     * @param sessionWebUserDto 会话用户dto
     */
    void transferFile(String fileId, SessionWebUserDto sessionWebUserDto);

    /**
     * 根据文件id和用户id查询文件信息
     *
     * @param fileId 文件id
     * @param userId 用户id
     * @return 文件信息
     */
    FileInfo findFileInfoByFiledIdAndUserId(String fileId, String userId);

    /**
     * 创建文件夹
     *
     * @param sessionWebUserDto 会话用户dto
     * @param filePid           父文件id
     * @param fileName          文件夹名
     */
    FileInfoVo createFolder(SessionWebUserDto sessionWebUserDto, String filePid, String fileName);

    /**
     * 重命名文件
     *
     * @param userId   用户id
     * @param fileId   文件id
     * @param fileName 文件名
     * @return 重命名后的文件信息vo
     */
    FileInfoVo renameFile(String userId, String fileId, String fileName);

    /**
     * 移动文件到目标目录
     *
     * @param fileIds 文件id列表
     * @param filePid 目标文件id
     * @param userId  用户id
     */
    void moveFile(String[] fileIds, String filePid, String userId);

    /**
     * 回收文件
     *
     * @param userId  用户id
     * @param fileIds 文件id列表
     */
    void recycleFile(String userId, String[] fileIds);

    /**
     * 恢复文件
     *
     * @param userId  用户id
     * @param fileIds 文件id列表
     */
    void recoverFile(String userId, String[] fileIds);

    /**
     * 删除文件
     *
     * @param userId  用户id
     * @param fileIds 文件id列表
     * @param isAdmin 是否为管理员
     */
    void deleteFile(String userId, String[] fileIds, boolean isAdmin);
}
