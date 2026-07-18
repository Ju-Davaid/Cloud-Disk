package ju.pioneer.cloud_disk.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import ju.pioneer.cloud_disk.component.RedisComponent;
import ju.pioneer.cloud_disk.config.AppConfig;
import ju.pioneer.cloud_disk.constants.Constants;
import ju.pioneer.cloud_disk.entity.dto.DownloadFileDto;
import ju.pioneer.cloud_disk.entity.dto.SessionWebUserDto;
import ju.pioneer.cloud_disk.entity.enums.FileFolderTypeEnum;
import ju.pioneer.cloud_disk.entity.enums.FileTypeEnum;
import ju.pioneer.cloud_disk.entity.enums.ResponseCodeEnum;
import ju.pioneer.cloud_disk.entity.po.FileInfo;
import ju.pioneer.cloud_disk.entity.query.FileInfoQuery;
import ju.pioneer.cloud_disk.entity.vo.FileInfoVo;
import ju.pioneer.cloud_disk.entity.vo.ResponseVO;
import ju.pioneer.cloud_disk.exception.BusinessException;
import ju.pioneer.cloud_disk.service.FileInfoService;
import ju.pioneer.cloud_disk.utils.CopyTools;
import ju.pioneer.cloud_disk.utils.StringTools;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class BaseFileController extends BaseController {
    private final Logger logger = LoggerFactory.getLogger(BaseFileController.class);
    @Resource
    AppConfig appConfig;
    @Resource
    private FileInfoService fileInfoService;
    @Resource
    private RedisComponent redisComponent;

    /**
     * 获取图片
     *
     * @param response    响应
     * @param imageFolder 图片文件夹
     * @param imageName   图片名称
     */
    protected void getImage(HttpServletResponse response, String imageFolder, String imageName) {
        String imageSuffix = StringTools.getSuffixOfFileName(imageName).replace(".", "");
        // 图片文件路径
        String filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER + imageFolder + File.separator + imageName;
        File targetFile = new File(filePath);
        if (!targetFile.exists()) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
        response.setContentType("image/" + imageSuffix);
        // 缓存1小时
        response.setHeader("Cache-Control", "max-age=3600");
        getFileResponse(response, filePath);
    }

    /**
     * 获取文件内容
     *
     * @param response 响应
     * @param userId   用户ID
     * @param fileId   文件ID
     */
    protected void getFile(HttpServletResponse response, String userId, String fileId) {
        String filePath;
        if (fileId.endsWith(".ts")) {
            String[] tsArray = fileId.split("_");
            String realFileId = tsArray[0];
            FileInfo fileInfo = fileInfoService.findFileInfoByFiledIdAndUserId(realFileId, userId);
            if (fileInfo == null) {
                throw new BusinessException(ResponseCodeEnum.CODE_404);
            }
            String fileName = fileInfo.getFilePath();
            filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER + StringTools.getFileNameOfNoSuffix(fileName) + "/" + fileId;
            response.setContentType("video/mp2t");
        } else {
            FileInfo fileInfo = fileInfoService.findFileInfoByFiledIdAndUserId(fileId, userId);
            if (fileInfo == null) {
                throw new BusinessException(ResponseCodeEnum.CODE_404);
            }
            if (fileInfo.getFolderType() == FileFolderTypeEnum.FOLDER.getType()) {
                throw new BusinessException("目录不能预览");
            }
            if (FileTypeEnum.VIDEO.getCategory().getCategory() == fileInfo.getFileCategory()) {
                String fileNameNoSuffix = StringTools.getFileNameOfNoSuffix(fileInfo.getFilePath());
                filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER + fileNameNoSuffix + "/" + Constants.M3U8_NAME;
                response.setContentType("application/vnd.apple.mpegurl");
            } else {
                filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER + fileInfo.getFilePath();
            }
            File targetFile = new File(filePath);
            if (!targetFile.exists()) {
                throw new BusinessException(ResponseCodeEnum.CODE_404);
            }
        }
        logger.info("getFile filePath:{}", filePath);
        getFileResponse(response, filePath);
    }

    /**
     * 获取目录下的文件列表
     *
     * @param path   目录路径
     * @param userId 用户ID
     * @return 目录下的文件列表
     */
    protected ResponseVO<?> getFolderInfo(String path, String userId) {
        String[] pathArr = path.split("/");
        FileInfoQuery query = new FileInfoQuery();
        query.setUserId(userId);
        query.setFolderType(FileFolderTypeEnum.FOLDER.getType());
        query.setFileIdArray(pathArr);
        String orderBy = "field(file_id,\"" + StringUtils.join(pathArr, "\",\"") + "\")";
        query.setOrderBy(orderBy);
        List<FileInfo> fileInfoList = fileInfoService.findListByParam(query);
        List<FileInfoVo> fileInfoVoList = CopyTools.copyList(fileInfoList, FileInfoVo.class);
        return getSuccessResponseVO(fileInfoVoList);
    }

    protected ResponseVO<?> createDownloadUrl(String fileId, String userId) {
        FileInfo fileInfo = fileInfoService.findFileInfoByFiledIdAndUserId(fileId, userId);
        if (fileInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_400);
        }
        if (Objects.equals(FileFolderTypeEnum.FOLDER.getType(), fileInfo.getFolderType())) {
            throw new BusinessException("目录不能下载");
        }
        String code = StringTools.getUUID();
        DownloadFileDto downloadFileDto = new DownloadFileDto();
        downloadFileDto.setCode(code);
        downloadFileDto.setFileId(fileId);
        downloadFileDto.setFileName(fileInfo.getFilePath());
        downloadFileDto.setFilePath(fileInfo.getFilePath());
        redisComponent.saveDownloadCode(code, downloadFileDto);
        return getSuccessResponseVO(code);
    }
}
