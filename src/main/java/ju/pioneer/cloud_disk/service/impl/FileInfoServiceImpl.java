package ju.pioneer.cloud_disk.service.impl;

import jakarta.annotation.Resource;
import ju.pioneer.cloud_disk.component.RedisComponent;
import ju.pioneer.cloud_disk.config.AppConfig;
import ju.pioneer.cloud_disk.constants.Constants;
import ju.pioneer.cloud_disk.entity.dto.SessionWebUserDto;
import ju.pioneer.cloud_disk.entity.dto.UserSpaceDto;
import ju.pioneer.cloud_disk.entity.enums.*;
import ju.pioneer.cloud_disk.entity.po.FileInfo;
import ju.pioneer.cloud_disk.entity.query.FileInfoQuery;
import ju.pioneer.cloud_disk.entity.query.SimplePage;
import ju.pioneer.cloud_disk.entity.vo.PaginateResultVo;
import ju.pioneer.cloud_disk.entity.vo.UploadResultVo;
import ju.pioneer.cloud_disk.exception.BusinessException;
import ju.pioneer.cloud_disk.mapper.FileInfoMapper;
import ju.pioneer.cloud_disk.mapper.UserInfoMapper;
import ju.pioneer.cloud_disk.service.FileInfoService;
import ju.pioneer.cloud_disk.utils.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@Service
public class FileInfoServiceImpl implements FileInfoService {

    private final Logger logger = LoggerFactory.getLogger(FileInfoServiceImpl.class);
    @Resource
    private RedisComponent redisComponent;
    @Resource
    private FileInfoMapper fileInfoMapper;
    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private AppConfig appConfig;

    /**
     * 根据参数查询文件信息列表
     *
     * @param param 查询参数
     * @return 文件信息列表
     */
    @Override
    public List<FileInfo> findListByParam(FileInfoQuery param) {
        return this.fileInfoMapper.selectList(param);
    }

    /**
     * 根据参数查询文件信息数量
     *
     * @param param 查询参数
     * @return 文件信息数量
     */
    @Override
    public Integer findCountByParam(FileInfoQuery param) {
        return this.fileInfoMapper.selectCount(param);
    }

    /**
     * 分页查询文件信息
     *
     * @param param 查询参数
     * @return 分页结果Vo<FileInfo>
     */
    @Override
    public PaginateResultVo<FileInfo> findListByPage(FileInfoQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSizeEnum.SIZE15.getSize() : param.getPageSize();
        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<FileInfo> list = this.findListByParam(param);
        return new PaginateResultVo<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
    }

    /**
     * 上传文件
     *
     * @param sessionWebUserDto 会话用户DTO
     * @param fileId            文件ID
     * @param file              文件
     * @param fileName          文件名
     * @param filePid           父级ID
     * @param fileMd5           文件MD5值
     * @param chunkCount        分块数量
     * @param chunkIndex        分块索引
     * @return 上传结果VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadResultVo uploadFile(SessionWebUserDto sessionWebUserDto, String fileId, MultipartFile file, String fileName, String filePid, String fileMd5, int chunkCount, int chunkIndex) {
        UploadResultVo uploadResultVo = new UploadResultVo();
        try {
            String userId = sessionWebUserDto.getUserId();
            if (StringTools.isEmpty(fileId)) {
                fileId = StringTools.getUUID();
            }
            uploadResultVo.setFileId(fileId);
            UserSpaceDto userSpaceDto = redisComponent.getUserSpaceUse(sessionWebUserDto.getUserId());
            // 分片索引为0时，判断文件是否存在，若存在则秒传
            if (chunkIndex == 0) {
                FileInfoQuery query = new FileInfoQuery();
                query.setFileMd5(fileMd5);
                query.setSimplePage(new SimplePage(0, 1));
                query.setStatus(FileStatusEnum.USING.getStatus());
                List<FileInfo> fileInfoListFromDB = fileInfoMapper.selectList(query);
                // 若文件存在，则秒传
                if (!fileInfoListFromDB.isEmpty()) {
                    FileInfo fileFromDB = fileInfoListFromDB.get(0);
                    uploadWithSeconds(sessionWebUserDto, fileFromDB, userSpaceDto, fileId, userId, fileName, filePid, fileMd5);
                    uploadResultVo.setStatus(UploadStatusEnum.UPLOAD_SECONDS.getStatus());
                    return uploadResultVo;
                }
            }
            // 分块上传
            uploadWithChunks(sessionWebUserDto, fileId, userSpaceDto, file, chunkIndex);
            // 若分块索引小于分块数量减1，则返回上传中状态
            if (chunkIndex < chunkCount - 1) {
                uploadResultVo.setStatus(UploadStatusEnum.UPLOADING.getStatus());
                redisComponent.setTempFileSize(userId, fileId, file.getSize());
            } else {
                uploadResultVo.setStatus(UploadStatusEnum.UPLOAD_SUCCESS.getStatus());
            }
            return uploadResultVo;
        } catch (IOException e) {
            logger.error("文件上传失败", e);
        }
        return uploadResultVo;
    }

    /**
     * 自动重命名文件
     *
     * @param filePid  父级ID
     * @param userId   用户ID
     * @param fileName 文件名
     * @return 重命名后的文件名
     */
    private String autoRename(String filePid, String userId, String fileName) {
        FileInfoQuery query = new FileInfoQuery();
        query.setFilePid(filePid);
        query.setUserId(userId);
        query.setDelFlag(FileDeleteEnum.USING.getFlag());
        query.setFileName(fileName);
        query.setSimplePage(new SimplePage(0, 1));
        List<FileInfo> fileInfoListFromDB = fileInfoMapper.selectList(query);
        if (!fileInfoListFromDB.isEmpty()) {
            fileName = StringTools.rename(fileName);
        }
        return fileName;
    }

    /**
     * 更新用户使用空间
     *
     * @param sessionWebUserDto 会话用户DTO
     * @param useSpace          使用空间
     */
    private void updateUserSpace(SessionWebUserDto sessionWebUserDto, Long useSpace) {
        String userId = sessionWebUserDto.getUserId();
        int count = userInfoMapper.updateUserSpace(userId, useSpace, null);
        if (count == 0) {
            throw new BusinessException(ResponseCodeEnum.CODE_904);
        }
        UserSpaceDto userSpaceDto = redisComponent.getUserSpaceUse(userId);
        userSpaceDto.setUseSpace(userSpaceDto.getUseSpace() + useSpace);
        redisComponent.saveUserSpaceUse(userId, userSpaceDto);
    }

    /**
     * 秒传文件
     *
     * @param sessionWebUserDto 会话用户DTO
     * @param fileFromDB        文件信息
     * @param userSpaceDto      用户空间DTO
     * @param fileId            文件ID
     * @param userId            用户ID
     * @param fileName          文件名
     * @param filePid           父级ID
     * @param fileMd5           文件MD5值
     */
    private void uploadWithSeconds(SessionWebUserDto sessionWebUserDto, FileInfo fileFromDB, UserSpaceDto userSpaceDto, String fileId, String userId, String fileName, String filePid, String fileMd5) {
        // 判断文件大小
        if ((fileFromDB.getFileSize() + userSpaceDto.getUseSpace()) > userSpaceDto.getTotalSpace()) {
            throw new BusinessException(ResponseCodeEnum.CODE_904);
        }
        Date now = new Date();
        fileFromDB.setFileId(fileId);
        fileFromDB.setFilePid(filePid);
        fileFromDB.setUserId(userId);
        fileFromDB.setCreateTime(now);
        fileFromDB.setLastUpdateTime(now);
        fileFromDB.setStatus(FileStatusEnum.USING.getStatus());
        fileFromDB.setDelFlag(FileDeleteEnum.USING.getFlag());
        fileFromDB.setFileMd5(fileMd5);
        // 文件重命名
        fileName = autoRename(filePid, userId, fileName);
        fileFromDB.setFileName(fileName);
        // 插入文件信息
        fileInfoMapper.insert(fileFromDB);
        // 更新用户使用空间
        updateUserSpace(sessionWebUserDto, fileFromDB.getFileSize());
    }

    /**
     * 分块上传文件
     *
     * @param sessionWebUserDto 会话用户DTO
     * @param fileId            文件ID
     * @param userSpaceDto      用户空间DTO
     * @param file              文件
     * @param chunkIndex        分块索引
     * @throws IOException IOException异常
     */
    private void uploadWithChunks(SessionWebUserDto sessionWebUserDto, String fileId, UserSpaceDto userSpaceDto, MultipartFile file, int chunkIndex) throws IOException {
        // 判断磁盘空间
        Long currentTempSize = redisComponent.getTempFileSize(sessionWebUserDto.getUserId(), fileId);
        if (currentTempSize + userSpaceDto.getUseSpace() + file.getSize() > userSpaceDto.getTotalSpace()) {
            throw new BusinessException(ResponseCodeEnum.CODE_904);
        }
        // 暂存临时文件夹
        String tempFolderName = appConfig.getProjectFolder() + Constants.FILE_FOLDER_TEMP;
        String currentFolderName = sessionWebUserDto.getUserId() + fileId;
        File tempFolder = new File(tempFolderName + currentFolderName);
        if (!tempFolder.exists()) {
            boolean isCreate = tempFolder.mkdirs();
            if (!isCreate) {
                throw new BusinessException(ResponseCodeEnum.CODE_500);
            }
        }
        // 上传文件
        File newFile = new File(tempFolder.getPath() + "/" + chunkIndex);
        file.transferTo(newFile);
    }
}
