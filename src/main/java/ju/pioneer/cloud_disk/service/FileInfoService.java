package ju.pioneer.cloud_disk.service;

import ju.pioneer.cloud_disk.entity.dto.SessionWebUserDto;
import ju.pioneer.cloud_disk.entity.po.FileInfo;
import ju.pioneer.cloud_disk.entity.query.FileInfoQuery;
import ju.pioneer.cloud_disk.entity.vo.PaginateResultVo;
import ju.pioneer.cloud_disk.entity.vo.UploadResultVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileInfoService {
    /**
     * 分页查询文件信息
     *
     * @param param 查询参数
     * @return 分页结果Vo<FileInfo>
     */
    PaginateResultVo<FileInfo> findListByPage(FileInfoQuery param);

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

    UploadResultVo uploadFile(SessionWebUserDto sessionWebUserDto, String fileId, MultipartFile file, String fileName, String parentId, String fileMd5, int chunkSize, int chunkIndex);

}
