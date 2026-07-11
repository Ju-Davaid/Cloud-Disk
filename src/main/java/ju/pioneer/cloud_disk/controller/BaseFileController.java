package ju.pioneer.cloud_disk.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import ju.pioneer.cloud_disk.config.AppConfig;
import ju.pioneer.cloud_disk.constants.Constants;
import ju.pioneer.cloud_disk.entity.dto.SessionWebUserDto;
import ju.pioneer.cloud_disk.entity.enums.FileTypeEnum;
import ju.pioneer.cloud_disk.entity.enums.ResponseCodeEnum;
import ju.pioneer.cloud_disk.entity.po.FileInfo;
import ju.pioneer.cloud_disk.exception.BusinessException;
import ju.pioneer.cloud_disk.service.FileInfoService;
import ju.pioneer.cloud_disk.utils.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class BaseFileController extends BaseController {
    private final Logger logger = LoggerFactory.getLogger(BaseFileController.class);
    @Resource
    AppConfig appConfig;
    @Resource
    private FileInfoService fileInfoService;

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

    protected void getFile(HttpServletResponse response, HttpSession session, String fileId) {
        SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
        String filePath;
        if (fileId.endsWith(".ts")) {
            String[] tsArray = fileId.split("_");
            String realFileId = tsArray[0];
            FileInfo fileInfo = fileInfoService.findFileInfoByFiledIdAndUserId(realFileId, sessionWebUserDto.getUserId());
            if (fileInfo == null) {
                throw new BusinessException(ResponseCodeEnum.CODE_404);
            }
            String fileName = fileInfo.getFilePath();
            filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER + StringTools.getFileNameOfNoSuffix(fileName) + "/" + fileId;
            response.setContentType("video/mp2t");
        } else {
            FileInfo fileInfo = fileInfoService.findFileInfoByFiledIdAndUserId(fileId, sessionWebUserDto.getUserId());
            if (fileInfo == null) {
                throw new BusinessException(ResponseCodeEnum.CODE_404);
            }
            if (FileTypeEnum.VIDEO.getCategory().getCategory() == fileInfo.getFileCategory()) {
                String fileNameNoSuffix = StringTools.getFileNameOfNoSuffix(fileInfo.getFilePath());
                filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER + fileNameNoSuffix + "/" + Constants.M3U8_NAME;
                response.setContentType("application/vnd.apple.mpegurl");
            }else{
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
}
