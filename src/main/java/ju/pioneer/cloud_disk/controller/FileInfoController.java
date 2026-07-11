package ju.pioneer.cloud_disk.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import ju.pioneer.cloud_disk.annotation.GlobalInterceptor;
import ju.pioneer.cloud_disk.annotation.VerifyParameter;
import ju.pioneer.cloud_disk.entity.dto.SessionWebUserDto;
import ju.pioneer.cloud_disk.entity.enums.FileCategoryEnum;
import ju.pioneer.cloud_disk.entity.enums.FileDeleteEnum;
import ju.pioneer.cloud_disk.entity.enums.ResponseCodeEnum;
import ju.pioneer.cloud_disk.entity.po.FileInfo;
import ju.pioneer.cloud_disk.entity.query.FileInfoQuery;
import ju.pioneer.cloud_disk.entity.vo.FileInfoVo;
import ju.pioneer.cloud_disk.entity.vo.PaginateResultVo;
import ju.pioneer.cloud_disk.entity.vo.ResponseVO;
import ju.pioneer.cloud_disk.entity.vo.UploadResultVo;
import ju.pioneer.cloud_disk.exception.BusinessException;
import ju.pioneer.cloud_disk.service.FileInfoService;
import ju.pioneer.cloud_disk.utils.StringTools;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController("fileController")
@RequestMapping("/file")
public class FileInfoController extends BaseFileController {
    @Resource
    private FileInfoService fileInfoService;

    /**
     * 分页查询文件信息
     *
     * @param query 查询参数
     * @return 分页结果Vo<FileInfo>
     */
    @GlobalInterceptor(checkLogin = true, checkParameters = true)
    @PostMapping("/list")
    public ResponseVO<?> getFileList(HttpSession session, FileInfoQuery query, String category) {
        FileCategoryEnum fileCategoryEnum = FileCategoryEnum.getByCode(category);
        SessionWebUserDto userInfo = getUserInfoFromSession(session);
        if (fileCategoryEnum != null) {
            query.setFileCategory(fileCategoryEnum.getCategory());
        }
        query.setUserId(userInfo.getUserId());
        query.setOrderBy("last_update_time desc");
        query.setDelFlag(FileDeleteEnum.USING.getFlag());
        PaginateResultVo<FileInfo> resultVo = fileInfoService.findListByPage(query);
        return getSuccessResponseVO(convertPaginateResultVo(resultVo, FileInfoVo.class));
    }


    /**
     * 上传文件
     *
     * @param session    会话
     * @param fileId     文件ID
     * @param filePid    父级ID
     * @param file       文件
     * @param fileName   文件名
     * @param fileMd5    文件md5值
     * @param chunkIndex 分块索引
     * @param chunkCount 分块数量
     * @return 上传结果VO
     */
    @GlobalInterceptor(checkLogin = true, checkParameters = true)
    @PostMapping("/upload")
    public ResponseVO<UploadResultVo> uploadFile(HttpSession session, String fileId, String filePid, @VerifyParameter(required = true) MultipartFile file, @VerifyParameter(required = true) String fileName, @VerifyParameter(required = true) String fileMd5, @VerifyParameter(required = true) int chunkIndex, @VerifyParameter(required = true) int chunkCount) {
        SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
        UploadResultVo uploadResultVo = fileInfoService.uploadFile(sessionWebUserDto, fileId, file, fileName, filePid, fileMd5, chunkCount, chunkIndex);
        return getSuccessResponseVO(uploadResultVo);
    }

    /**
     * 获取图片
     *
     * @param response    响应
     * @param imageFolder 图片文件夹
     * @param imageName   图片名
     */
    @GetMapping("/getImage/{imageFolder}/{imageName}")
    public void getImage(HttpServletResponse response, @PathVariable String imageFolder, @PathVariable String imageName) {
        if (StringTools.isEmpty(imageFolder) || StringTools.isEmpty(imageName) || !StringTools.isPathValid(imageFolder)) {
            throw new BusinessException(ResponseCodeEnum.CODE_400);
        }
        super.getImage(response, imageFolder, imageName);
    }

    /**
     * 获取视频流
     *
     * @param response 响应
     * @param session  会话
     * @param fileId   视频ID
     */
    @GlobalInterceptor(checkLogin = true)
    @GetMapping("/video/stream/{fileId}")
    public void playVideo(HttpServletResponse response, HttpSession session, @PathVariable String fileId) {
        super.getFile(response, session, fileId);
    }
    @GlobalInterceptor(checkLogin = true)
    @GetMapping("/doc/{fileId}")
    public void getDoc(HttpServletResponse response, HttpSession session, @PathVariable String fileId) {
        super.getFile(response, session, fileId);
    }
}
