package ju.pioneer.cloud_disk.service.impl;

import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import ju.pioneer.cloud_disk.component.RedisComponent;
import ju.pioneer.cloud_disk.config.AppConfig;
import ju.pioneer.cloud_disk.constants.Constants;
import ju.pioneer.cloud_disk.entity.dto.SessionWebUserDto;
import ju.pioneer.cloud_disk.entity.dto.UserSpaceDto;
import ju.pioneer.cloud_disk.entity.enums.*;
import ju.pioneer.cloud_disk.entity.po.FileInfo;
import ju.pioneer.cloud_disk.entity.po.UserInfo;
import ju.pioneer.cloud_disk.entity.query.FileInfoQuery;
import ju.pioneer.cloud_disk.entity.query.RecursiveFileInfoQuery;
import ju.pioneer.cloud_disk.entity.query.SimplePage;
import ju.pioneer.cloud_disk.entity.vo.FileInfoVo;
import ju.pioneer.cloud_disk.entity.vo.PaginateResultVo;
import ju.pioneer.cloud_disk.entity.vo.UploadResultVo;
import ju.pioneer.cloud_disk.exception.BusinessException;
import ju.pioneer.cloud_disk.mapper.FileInfoMapper;
import ju.pioneer.cloud_disk.mapper.UserInfoMapper;
import ju.pioneer.cloud_disk.service.FileInfoService;
import ju.pioneer.cloud_disk.utils.CopyTools;
import ju.pioneer.cloud_disk.utils.ScaleFilter;
import ju.pioneer.cloud_disk.utils.StringTools;
import ju.pioneer.cloud_disk.utils.VideoUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

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
    @Resource
    @Lazy
    private FileInfoService fileInfoService;

    /**
     * 根据参数查询文件信息列表
     *
     * @param param 查询参数
     * @return 文件信息列表
     */
//    @Override
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
     * 根据文件ID和用户ID查询文件信息
     *
     * @param fileId 文件 文件ID
     * @param userId 用户ID
     * @return 文件信息
     */
    @Override
    public FileInfo findFileInfoByFiledIdAndUserId(String fileId, String userId) {
        return fileInfoMapper.selectByFileIdAndUserId(fileId, userId);
    }

    /**
     * 分页查询文件信息
     *
     * @param param 查询参数
     * @return 分页结果Vo<FileInfo>
     */
    @Override
    public PaginateResultVo<FileInfoVo> findListByPage(FileInfoQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSizeEnum.SIZE15.getSize() : param.getPageSize();
        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<FileInfo> list = this.findListByParam(param);
        logger.info("分页查询文件信息，参数：{}，分页信息：{}，查询结果：{}", param, page, list);
        List<FileInfoVo> fileInfoVoList = CopyTools.copyList(list, FileInfoVo.class);
        return new PaginateResultVo<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), fileInfoVoList);
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
        String userId = sessionWebUserDto.getUserId();
        if (StringTools.isEmpty(fileId)) {
            fileId = StringTools.getUUID();
        }
        if (StringTools.isEmpty(filePid)) {
            filePid = Constants.USER_ROOT_DIRECTORY_ID;
        }
        uploadResultVo.setFileId(fileId);
        String tempFolderPath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_TEMP + userId + fileId;
        File tempFileFolder = new File(tempFolderPath);
        boolean isUploadSuccess = true;
        try {
            UserSpaceDto userSpaceDto = redisComponent.getUserSpaceUse(userId);
            if (chunkIndex == 0) {
                FileInfoQuery query = new FileInfoQuery();
                query.setFileMd5(fileMd5);
                query.setSimplePage(new SimplePage(0, 1));
                query.setStatus(FileStatusEnum.USING.getStatus());
                List<FileInfo> fileInfoListFromDB = fileInfoMapper.selectList(query);
                if (!fileInfoListFromDB.isEmpty()) {
                    FileInfo fileFromDB = fileInfoListFromDB.get(0);
                    uploadWithSeconds(sessionWebUserDto, fileFromDB, userSpaceDto, fileId, userId, fileName, filePid, fileMd5);
                    uploadResultVo.setStatus(UploadStatusEnum.UPLOAD_SECONDS.getStatus());
                    return uploadResultVo;
                }
            }
            uploadWithChunks(sessionWebUserDto, fileId, userSpaceDto, file, chunkIndex, tempFileFolder);
            redisComponent.setTempFileSize(userId, fileId, file.getSize());
            if (chunkIndex < chunkCount - 1) {
                uploadResultVo.setStatus(UploadStatusEnum.UPLOADING.getStatus());
                return uploadResultVo;
            }
            mergeFileChunks(file, fileMd5, filePid, fileId, sessionWebUserDto, fileName);
            uploadResultVo.setStatus(UploadStatusEnum.UPLOAD_SUCCESS.getStatus());
        } catch (BusinessException e) {
            isUploadSuccess = false;
            logger.error("文件上传失败", e);
            throw e;
        } catch (Exception e) {
            isUploadSuccess = false;
            logger.error("文件上传失败", e);
            throw new BusinessException("文件上传失败", e);
        } finally {
            try {
                if (!isUploadSuccess && tempFileFolder.exists()) {
                    FileUtils.deleteDirectory(tempFileFolder);
                }
            } catch (IOException e) {
                logger.error("删除临时文件夹失败", e);
            }
        }
        return uploadResultVo;
    }

    /**
     * 创建文件夹
     *
     * @param sessionWebUserDto 会话用户DTO
     * @param filePid           父级ID
     * @param fileName          文件夹名
     * @return 文件夹信息
     */
    @Override
    public FileInfoVo createFolder(SessionWebUserDto sessionWebUserDto, String filePid, String fileName) {
        String userId = sessionWebUserDto.getUserId();
        Date date = new Date();
        String fileId = StringTools.getUUID();
        fileName = autoRename(filePid, fileId, userId, fileName);
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileId(fileId);
        if (StringTools.isEmpty(filePid)) {
            filePid = Constants.USER_ROOT_DIRECTORY_ID;
        }
        fileInfo.setFilePid(filePid);
        fileInfo.setUserId(userId);
        fileInfo.setFileName(fileName);
        fileInfo.setFolderType(FileFolderTypeEnum.FOLDER.getType());
        fileInfo.setStatus(FileStatusEnum.USING.getStatus());
        fileInfo.setDelFlag(FileDeleteEnum.USING.getFlag());
        fileInfo.setCreateTime(date);
        fileInfo.setLastUpdateTime(date);
        fileInfoMapper.insert(fileInfo);
        return CopyTools.copy(fileInfo, FileInfoVo.class);
    }

    /**
     * 重命名文件
     *
     * @param userId   用户ID
     * @param fileId   文件ID
     * @param fileName 文件名
     * @return 重命名后的文件VO
     */
    @Override
    public FileInfoVo renameFile(String userId, String fileId, String fileName) {
        FileInfo fileInfo = fileInfoMapper.selectByFileIdAndUserId(fileId, userId);
        if (fileInfo == null) {
            throw new BusinessException("文件不存在或已被删除");
        }
        fileName = autoRename(fileInfo.getFilePid(), fileId, userId, fileName);
        fileInfo.setFileName(fileName);
        fileInfo.setLastUpdateTime(new Date());
        fileInfoMapper.updateByFileIdAndUserId(fileInfo, fileId, userId);
        return CopyTools.copy(fileInfo, FileInfoVo.class);
    }

    /**
     * 移动文件到目标目录
     *
     * @param fileIds 文件id列表，逗号分隔
     * @param filePid 目标文件id
     */
    @Override
    public void moveFile(String[] fileIds, String filePid, String userId) {
        if (!Constants.USER_ROOT_DIRECTORY_ID.equals(filePid)) {
            FileInfo fileInfo = this.findFileInfoByFiledIdAndUserId(filePid, userId);
            if (fileInfo == null || FileDeleteEnum.USING.getFlag() != fileInfo.getDelFlag()) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
        }
        FileInfoQuery query = new FileInfoQuery();
        query.setUserId(userId);
        query.setFileIdArray(fileIds);
        // 需要移动的文件信息列表
        List<FileInfo> movedFileInfo = this.findListByParam(query);
        movedFileInfo = movedFileInfo.stream().peek(item -> {
            item.setFileName(autoRename(filePid, item.getFileId(), userId, item.getFileName()));
            item.setFilePid(filePid);
        }).toList();
        fileInfoMapper.insertOrUpdateBatch(movedFileInfo);
    }

    /**
     * 回收文件
     *
     * @param userId  用户id
     * @param fileIds 文件id列表
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void recycleFile(String userId, String[] fileIds) {
        FileInfoQuery query = new FileInfoQuery();
        query.setUserId(userId);
        query.setFileIdArray(fileIds);
        query.setDelFlag(FileDeleteEnum.USING.getFlag());
        List<FileInfo> fileInfoList = this.findListByParam(query);
        if (fileInfoList.isEmpty()) {
            throw new BusinessException("文件不存在或已被删除");
        }
        String[] folderIds = fileInfoList.stream().filter(item -> Objects.equals(item.getFolderType(), FileFolderTypeEnum.FOLDER.getType())).map(FileInfo::getFileId).toArray(String[]::new);
        RecursiveFileInfoQuery recursiveFileInfoQuery = new RecursiveFileInfoQuery();
        recursiveFileInfoQuery.setUserId(userId);
        recursiveFileInfoQuery.setResultFolderType(FileFolderTypeEnum.FOLDER.getType());
        recursiveFileInfoQuery.setParentFileIdArray(folderIds);
        recursiveFileInfoQuery.setChildDelFlag(FileDeleteEnum.USING.getFlag());
        recursiveFileInfoQuery.setIsIncludeParent(true);
        // 所选文件的子文件夹列表
        List<FileInfo> recoverChildFolderList = fileInfoMapper.selectAllChildFileInfo(recursiveFileInfoQuery);
        // 所选文件的子文件夹id列表
        List<String> recoverFilePidList = recoverChildFolderList.stream().map(FileInfo::getFileId).toList();
        if (!recoverFilePidList.isEmpty()) {
            FileInfo updateInfo = new FileInfo();
            updateInfo.setDelFlag(FileDeleteEnum.DEL.getFlag());
            updateInfo.setRecoveryTime(new Date());
            fileInfoMapper.updateFileDelFlagBatch(updateInfo, userId, recoverFilePidList, null, FileDeleteEnum.USING.getFlag());
        }
        List<String> recycleFileIdList = Arrays.asList(fileIds);
        FileInfo fileInfo = new FileInfo();
        fileInfo.setRecoveryTime(new Date());
        fileInfo.setDelFlag(FileDeleteEnum.RECYCLE.getFlag());
        fileInfoMapper.updateFileDelFlagBatch(fileInfo, userId, null, recycleFileIdList, FileDeleteEnum.USING.getFlag());
    }

    /**
     * 恢复文件
     *
     * @param userId  用户id
     * @param fileIds 文件id列表
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void recoverFile(String userId, String[] fileIds) {
        FileInfoQuery query = new FileInfoQuery();
        query.setUserId(userId);
        query.setFileIdArray(fileIds);
        query.setDelFlag(FileDeleteEnum.RECYCLE.getFlag());
        List<FileInfo> fileInfoList = this.findListByParam(query);
        if (fileInfoList.isEmpty()) {
            throw new BusinessException("文件不存在或已被删除");
        }
        String[] folderIds = fileInfoList.stream().filter(item -> Objects.equals(item.getFolderType(), FileFolderTypeEnum.FOLDER.getType())).map(FileInfo::getFileId).toArray(String[]::new);
        RecursiveFileInfoQuery recursiveFileInfoQuery = new RecursiveFileInfoQuery();
        recursiveFileInfoQuery.setUserId(userId);
        recursiveFileInfoQuery.setResultFolderType(FileFolderTypeEnum.FOLDER.getType());
        recursiveFileInfoQuery.setParentFileIdArray(folderIds);
        recursiveFileInfoQuery.setChildDelFlag(FileDeleteEnum.DEL.getFlag());
        recursiveFileInfoQuery.setIsIncludeParent(true);
        // 所选文件的子文件夹列表
        List<FileInfo> recoverChildFolderList = fileInfoMapper.selectAllChildFileInfo(recursiveFileInfoQuery);
        // 所选文件的子文件夹id列表
        List<String> recoverFilePidList = recoverChildFolderList.stream().map(FileInfo::getFileId).toList();
        logger.info("recoverChildFolderList:{}", recoverFilePidList);
        if (!recoverFilePidList.isEmpty()) {
            FileInfo updateInfo = new FileInfo();
            updateInfo.setDelFlag(FileDeleteEnum.USING.getFlag());
            updateInfo.setLastUpdateTime(new Date());
            fileInfoMapper.updateFileDelFlagBatch(updateInfo, userId, recoverFilePidList, null, FileDeleteEnum.DEL.getFlag());
        }
        // 更新所选文件的子文件夹的文件名
        List<FileInfo> updateFileInfoList = fileInfoList.stream().peek(item -> {
            item.setFileName(autoRename(item.getFilePid(), item.getFileId(), userId, item.getFileName()));
            item.setDelFlag(FileDeleteEnum.USING.getFlag());
            item.setLastUpdateTime(new Date());
        }).toList();
        for (FileInfo updateFileInfo : updateFileInfoList) {
            fileInfoMapper.updateByFileIdAndUserId(updateFileInfo, updateFileInfo.getFileId(), userId);
        }
    }

    /**
     * 删除文件
     *
     * @param userId  用户id
     * @param fileIds 文件id列表
     * @param isAdmin 是否是管理员
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteFile(String userId, String[] fileIds, boolean isAdmin) {
        FileInfoQuery query = new FileInfoQuery();
        query.setUserId(userId);
        query.setFileIdArray(fileIds);
        query.setDelFlag(isAdmin ? null : FileDeleteEnum.RECYCLE.getFlag());
        List<FileInfo> fileInfoList = this.findListByParam(query);
        if (fileInfoList.isEmpty()) {
            throw new BusinessException("文件不存在或已被删除");
        }
        RecursiveFileInfoQuery recursiveFileInfoQuery = new RecursiveFileInfoQuery();
        recursiveFileInfoQuery.setUserId(userId);
        recursiveFileInfoQuery.setResultFolderType(FileFolderTypeEnum.FOLDER.getType());
        recursiveFileInfoQuery.setParentFileIdArray(fileIds);
        recursiveFileInfoQuery.setChildDelFlag(FileDeleteEnum.DEL.getFlag());
        recursiveFileInfoQuery.setIsIncludeParent(true);
        // 子文件夹
        List<FileInfo> deleteChildFolderList = fileInfoMapper.selectAllChildFileInfo(recursiveFileInfoQuery);
        List<String> deleteFilePidList = deleteChildFolderList.stream().map(FileInfo::getFileId).toList();
        logger.info("deleteChildFolderList:{}", deleteFilePidList);
        if (!deleteFilePidList.isEmpty()) {
            fileInfoMapper.delFileBatch(userId, deleteFilePidList, null, isAdmin ? null : FileDeleteEnum.DEL.getFlag());
        }
        // 删除所选文件
        fileInfoMapper.delFileBatch(userId, null, Arrays.asList(fileIds), isAdmin ? null : FileDeleteEnum.RECYCLE.getFlag());
        long useSpace = fileInfoMapper.selectUseSpace(userId);
        // 更新用户使用空间
        UserInfo updateUserInfo = new UserInfo();
        updateUserInfo.setUserId(userId);
        updateUserInfo.setUseSpace(useSpace);
        userInfoMapper.updateByPrimaryKeySelective(updateUserInfo);
        UserSpaceDto oldUserSpaceDto = redisComponent.getUserSpaceUse(userId);
        // 更新用户空间缓存
        UserSpaceDto userSpaceDto = new UserSpaceDto();
        userSpaceDto.setUseSpace(useSpace);
        userSpaceDto.setTotalSpace(oldUserSpaceDto.getTotalSpace());
        redisComponent.saveUserSpaceUse(userId, userSpaceDto);
    }

    /**
     * 校验根目录
     *
     * @param rootFilePid 文件父目录ID
     * @param userId      用户ID
     * @param fileId      文件ID
     */
    @Override
    public void checkRootDirectory(String rootFilePid, String userId, String fileId) {
        if (StringTools.isEmpty(fileId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (rootFilePid.equals(fileId)) return;
        checkFilePid(rootFilePid, userId, fileId);
    }

    /**
     * 保存分享文件
     *
     * @param shareRootFilePid 分享文件根目录ID
     * @param fileIds          分享文件ID列表
     * @param targetFolderId   分享文件ID列表
     * @param shareUserId      分享用户ID
     * @param targetUserId     目标用户ID
     */
    @Override
    public void saveShareFile(String shareRootFilePid, String fileIds, String targetFolderId, String shareUserId, String targetUserId) {
        String[] fileIdArray = fileIds.split(",");
        // 递归查询所有子孙文件查询条件
        RecursiveFileInfoQuery recursiveFileInfoQuery = new RecursiveFileInfoQuery();
        recursiveFileInfoQuery.setUserId(shareUserId);
        recursiveFileInfoQuery.setParentFileIdArray(fileIdArray);
        recursiveFileInfoQuery.setIsIncludeParent(true);
        // 查询分享文件所有子孙文件总大小
        Long shareFileTreeTotalSize = fileInfoMapper.getFileTreeTotalSize(recursiveFileInfoQuery);

        UserSpaceDto userSpaceDto = redisComponent.getUserSpaceUse(targetUserId);
        // 校验目标用户空间是否足够
        if (userSpaceDto.getUseSpace() + shareFileTreeTotalSize > userSpaceDto.getTotalSpace()) {
            throw new BusinessException(ResponseCodeEnum.CODE_904);
        }
        FileInfoQuery targetFolderChildFileQuery = new FileInfoQuery();
        targetFolderChildFileQuery.setUserId(targetUserId);
        targetFolderChildFileQuery.setFilePid(targetFolderId);
        List<FileInfo> targetFolderChildFileList = fileInfoMapper.selectList(targetFolderChildFileQuery);
        Set<String> targetFolderChildFileNameList = targetFolderChildFileList.stream().map(FileInfo::getFileName).collect(Collectors.toSet());
        // 要保存的文件夹的新旧fileId映射(为后续更新子文件信息做准备)
        FileInfoQuery savedFileInfoQuery = new FileInfoQuery();
        savedFileInfoQuery.setUserId(shareUserId);
        savedFileInfoQuery.setFileIdArray(fileIdArray);
        // 递归查询分享文件下的所有文件
        List<FileInfo> savedFileList = fileInfoMapper.selectAllChildFileInfo(recursiveFileInfoQuery);
        // 新旧目录ID映射 旧ID -> 新ID (为后续更新子文件信息做准备)
        Map<String, String> filePidMap = savedFileList.stream().filter(fileInfo -> fileInfo.getFolderType().equals(FileFolderTypeEnum.FOLDER.getType())).collect(Collectors.toMap(FileInfo::getFileId, (i) -> StringTools.getUUID()));
        logger.info("filePidMap:{}", filePidMap);
        Date curDate = new Date();
        // 修改分享文件信息为目标文件夹下的文件信息
        for (FileInfo savedItem : savedFileList) {
            logger.info("savedItem:{}", savedItem);
            // 更改分享文件filePid
            if (ArrayUtils.contains(fileIdArray, savedItem.getFileId())) {
                savedItem.setFilePid(targetFolderId);
                String newName = StringTools.getSafeFileName(savedItem.getFileName(), targetFolderChildFileNameList);
                savedItem.setFileName(newName);
                targetFolderChildFileNameList.add(newName);
            } else if (filePidMap.containsKey(savedItem.getFilePid())) {
                savedItem.setFilePid(filePidMap.get(savedItem.getFilePid()));
            }
            // 更改分享文件fileId
            if (savedItem.getFolderType().equals(FileFolderTypeEnum.FOLDER.getType())) {
                savedItem.setFileId(filePidMap.get(savedItem.getFileId()));
            } else {
                savedItem.setFileId(StringTools.getUUID());
            }
            savedItem.setUserId(targetUserId);
            savedItem.setDelFlag(FileDeleteEnum.USING.getFlag());
            savedItem.setCreateTime(curDate);
            savedItem.setLastUpdateTime(curDate);
        }
        fileInfoMapper.insertBatch(savedFileList);
    }

    /**
     * 校验文件父目录ID
     *
     * @param rootFilePid 文件父目录ID
     * @param userId      用户ID
     * @param fileId      文件ID
     */
    private void checkFilePid(String rootFilePid, String userId, String fileId) {
        FileInfo fileInfo = fileInfoMapper.selectByFileIdAndUserId(fileId, userId);
        if (fileInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (fileInfo.getFilePid().equals(Constants.USER_ROOT_DIRECTORY_ID)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (rootFilePid.equals(fileInfo.getFilePid())) return;
        checkFilePid(rootFilePid, userId, fileInfo.getFilePid());
    }

    /**
     * 自动重命名文件
     *
     * @param filePid  父级ID
     * @param fileId   文件ID
     * @param userId   用户ID
     * @param fileName 文件名
     * @return 重命名后的文件名
     */
    private String autoRename(String filePid, String fileId, String userId, String fileName) {
        FileInfoQuery query = new FileInfoQuery();
        query.setFilePid(filePid);
        query.setUserId(userId);
        query.setDelFlag(FileDeleteEnum.USING.getFlag());
        query.setExcludeFileIdArray(new String[]{fileId});
        List<FileInfo> fileInfoListFromDB = fileInfoMapper.selectList(query);
        Set<String> existNames = fileInfoListFromDB.stream().map(FileInfo::getFileName).collect(Collectors.toSet());
        fileName = StringTools.getSafeFileName(fileName, existNames);
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
        fileName = autoRename(filePid, fileId, userId, fileName);
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
    private void uploadWithChunks(SessionWebUserDto sessionWebUserDto, String fileId, UserSpaceDto userSpaceDto, MultipartFile file, int chunkIndex, File tempFileFolder) throws IOException {
        Long currentTempSize = redisComponent.getTempFileSize(sessionWebUserDto.getUserId(), fileId);
        if (currentTempSize + userSpaceDto.getUseSpace() + file.getSize() > userSpaceDto.getTotalSpace()) {
            throw new BusinessException(ResponseCodeEnum.CODE_904);
        }
        if (!tempFileFolder.exists()) {
            boolean isCreate = tempFileFolder.mkdirs();
            if (!isCreate) {
                throw new BusinessException(ResponseCodeEnum.CODE_500);
            }
        }
        File newFile = new File(tempFileFolder.getPath() + "/" + chunkIndex);
        file.transferTo(newFile);
    }

    /**
     * 合并文件分块
     *
     * @param file              文件
     * @param fileMd5           文件md5值
     * @param filePid           父文件id
     * @param fileId            文件id
     * @param sessionWebUserDto 会话用户dto
     * @param fileName          文件名
     */
    private void mergeFileChunks(MultipartFile file, String fileMd5, String filePid, String fileId, SessionWebUserDto sessionWebUserDto, String fileName) {
        String userId = sessionWebUserDto.getUserId();
        Date curDate = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
        // 文件真实存放目录，以yyyy-MM为目录
        String month = simpleDateFormat.format(curDate);
        // 获取文件后缀
        String fileSuffix = StringTools.getSuffixOfFileName(fileName);
        // 文件真实存放文件名 （用户ID+文件ID+文件后缀）
        String realFileName = userId + fileId + fileSuffix;
        // 获取当前文件类型
        FileTypeEnum fileType = FileTypeEnum.getFileTypeBySuffix(fileSuffix);
        fileName = autoRename(filePid, fileId, userId, fileName);
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileId(fileId);
        fileInfo.setUserId(userId);
        fileInfo.setFileMd5(fileMd5);
        fileInfo.setFileName(fileName);
        fileInfo.setFilePath(month + "/" + realFileName);
        fileInfo.setFilePid(filePid);
        fileInfo.setCreateTime(curDate);
        fileInfo.setLastUpdateTime(curDate);
        fileInfo.setFileCategory(fileType.getCategory().getCategory());
        fileInfo.setFileType(fileType.getType());
        fileInfo.setStatus(FileStatusEnum.TRANSFORMING.getStatus());
        fileInfo.setFolderType(FileFolderTypeEnum.FILE.getType());
        fileInfo.setDelFlag(FileDeleteEnum.USING.getFlag());
        logger.info("合并文件分块，文件信息：{}", fileInfo);
        fileInfoMapper.insert(fileInfo);
        // 更新用户使用空间
        Long tempSize = redisComponent.getTempFileSize(userId, fileId);
        updateUserSpace(sessionWebUserDto, tempSize);
        // 待事务提交后转换文件
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                fileInfoService.transferFile(fileId, sessionWebUserDto);
            }
        });
    }

    /**
     * 传输分块文件到目标目录
     *
     * @param fileId            文件id
     * @param sessionWebUserDto 会话用户dto
     */
    @Override
    @Async
    public void transferFile(String fileId, SessionWebUserDto sessionWebUserDto) {
        boolean isTransformSuccess = true;
        String targetFilePath = null;
        String cover = null;
        FileTypeEnum fileType = null;
        FileInfo fileInfo = fileInfoMapper.selectByFileIdAndUserId(fileId, sessionWebUserDto.getUserId());
        try {
            if (fileInfo == null || FileStatusEnum.TRANSFORMING.getStatus() != fileInfo.getStatus()) {
                return;
            }
            // 临时文件目录名称
            String tempFolderName = appConfig.getProjectFolder() + Constants.FILE_FOLDER_TEMP;
            // 合并分块后的文件名称
            String currentFolderName = sessionWebUserDto.getUserId() + fileId;
            // 存放分块的临时文件夹
            File tempFileFolder = new File(tempFolderName + currentFolderName);
            // 前端传递的文件名后缀
            String fileSuffix = StringTools.getSuffixOfFileName(fileInfo.getFileName());
            // 存放合并后的文件目录（以日期yyyy-MM为目录名称）
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
            String month = simpleDateFormat.format(fileInfo.getCreateTime());
            // 系统文件目录
            String targetFolderName = appConfig.getProjectFolder() + Constants.FILE_FOLDER;
            // 分块合并目标文件
            File targetFolder = new File(targetFolderName + "/" + month);
            if (!targetFolder.exists()) {
                boolean isCreate = targetFolder.mkdirs();
                if (!isCreate) {
                    throw new BusinessException("创建目标文件目录失败");
                }
            }
            // 存放在系统的真实文件名
            String realFileName = currentFolderName + fileSuffix;
            // 存放上传文件的文件路径
            targetFilePath = targetFolder.getPath() + "/" + realFileName;
            // 合并文件分块
            merge(tempFileFolder.getPath(), targetFilePath, fileInfo.getFileName(), true);
            // 视频文件切割
            fileType = FileTypeEnum.getFileTypeBySuffix(fileSuffix);
            if (FileTypeEnum.VIDEO.equals(fileType)) {
                // 视频文件切割
                VideoUtils.videoCut(fileId, targetFilePath);
                // 生成视频封面
                // 封面文件路径
                cover = month + "/" + currentFolderName + Constants.IMAGE_PNG_SUFFIX;
                String coverPath = targetFolderName + "/" + cover;
                ScaleFilter.createCoverOfVideo(new File(targetFilePath), Constants.COVER_SIZE, new File(coverPath));
            } else if (FileTypeEnum.IMAGE.equals(fileType)) {
                // 图片缩略图生成
                cover = month + "/" + realFileName.replace(".", "_thumb.");
                String coverPath = targetFolderName + "/" + cover;
                boolean created = ScaleFilter.createThumbnailWidthFFmpeg(new File(targetFilePath), Constants.COVER_SIZE, new File(coverPath), false);
                // 如果创建失败，复制源图片为缩略图
                if (!created) {
                    FileUtils.copyFile(new File(targetFilePath), new File(coverPath));
                }
            }
        } catch (Exception e) {
            isTransformSuccess = false;
            logger.error("文件转码失败，文件id：{}，userId：{}", fileId, sessionWebUserDto.getUserId(), e);
        } finally {
            FileInfo updateFileInfo = new FileInfo();
            if (targetFilePath != null) {
                updateFileInfo.setFileSize(new File(targetFilePath).length());
            }
            updateFileInfo.setFileCover(cover);
            if (fileType != null) {
                updateFileInfo.setFileType(fileType.getType());
            }
            updateFileInfo.setStatus(isTransformSuccess ? FileStatusEnum.USING.getStatus() : FileStatusEnum.TRANSFORM_FAIL.getStatus());
            logger.info("文件转码状态更新，文件信息：{}", updateFileInfo);
            fileInfoMapper.updateFileStatusWithOldStatus(fileId, sessionWebUserDto.getUserId(), updateFileInfo, FileStatusEnum.TRANSFORMING.getStatus());
        }
    }

    /**
     * 合并文件分块
     *
     * @param dirPath    分块文件目录路径
     * @param toFilePath 目标文件路径
     * @param fileName   文件名
     * @param delSource  是否删除源文件
     * @throws BusinessException 异常
     */
    private void merge(String dirPath, String toFilePath, String fileName, boolean delSource) throws BusinessException {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            throw new BusinessException("分块文件目录不存在");
        }
        File[] fileList = dir.listFiles();
        File targetFile = new File(toFilePath);
        try (RandomAccessFile writeFile = new RandomAccessFile(targetFile, "rw")) {
            byte[] buffer = new byte[1024 * 10];
            if (fileList != null) {
                for (int i = 0; i < fileList.length; i++) {
                    int len;
                    File chunkFIle = new File(dirPath + "/" + i);
                    try (RandomAccessFile readFile = new RandomAccessFile(chunkFIle, "r")) {
                        while ((len = readFile.read(buffer)) != -1) {
                            writeFile.write(buffer, 0, len);
                        }
                    } catch (IOException e) {
                        logger.error("合并文件分块失败", e);
                        throw new BusinessException("合并文件分块失败", e);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("合并文件：{}失败", fileName, e);
            throw new BusinessException("合并文件：" + fileName + "失败", e);
        } finally {
            if (delSource && dir.exists()) {
                try {
                    FileUtils.deleteDirectory(dir);
                } catch (IOException e) {
                    logger.error("删除分块文件目录失败", e);
                }
            }
        }
    }

}
