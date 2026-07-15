package ju.pioneer.cloud_disk.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import ju.pioneer.cloud_disk.annotation.GlobalInterceptor;
import ju.pioneer.cloud_disk.annotation.VerifyParameter;
import ju.pioneer.cloud_disk.component.RedisComponent;
import ju.pioneer.cloud_disk.constants.Constants;
import ju.pioneer.cloud_disk.entity.dto.DownloadFileDto;
import ju.pioneer.cloud_disk.entity.dto.SessionWebUserDto;
import ju.pioneer.cloud_disk.entity.enums.FileDeleteEnum;
import ju.pioneer.cloud_disk.entity.enums.ResponseCodeEnum;
import ju.pioneer.cloud_disk.entity.query.FileInfoQuery;
import ju.pioneer.cloud_disk.entity.vo.FileInfoVo;
import ju.pioneer.cloud_disk.entity.vo.PaginateResultVo;
import ju.pioneer.cloud_disk.entity.vo.ResponseVO;
import ju.pioneer.cloud_disk.entity.vo.UploadResultVo;
import ju.pioneer.cloud_disk.exception.BusinessException;
import ju.pioneer.cloud_disk.service.FileInfoService;
import ju.pioneer.cloud_disk.utils.StringTools;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController("fileController")
@RequestMapping("/file")
public class FileInfoController extends BaseFileController {
    @Resource
    private FileInfoService fileInfoService;
    @Resource
    private RedisComponent redisComponent;

    /**
     * 分页查询文件信息
     *
     * @param query 查询参数
     * @return 分页结果Vo<FileInfo>
     */
    @GlobalInterceptor(checkLogin = true)
    @PostMapping("/list")
    public ResponseVO<PaginateResultVo<FileInfoVo>> getFileList(HttpSession session, @RequestBody(required = false) FileInfoQuery query) {
        SessionWebUserDto userInfo = getUserInfoFromSession(session);
        if (query == null) {
            query = new FileInfoQuery();
        }
        if (StringTools.isEmpty(query.getFilePid())) {
            query.setFilePid(Constants.USER_ROOT_DIRECTORY_ID);
        }
        query.setUserId(userInfo.getUserId());
        query.setOrderBy("last_update_time desc");
        query.setDelFlag(FileDeleteEnum.USING.getFlag());
        PaginateResultVo<FileInfoVo> resultVo = fileInfoService.findListByPage(query);
        return getSuccessResponseVO(resultVo);
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
        if (StringTools.isEmpty(imageFolder) || StringTools.isEmpty(imageName) || StringTools.isPathValid(imageFolder)) {
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

    /**
     * 获取文件内容
     *
     * @param response 响应
     * @param session  会话
     * @param fileId   文件ID
     */
    @GlobalInterceptor(checkLogin = true)
    @GetMapping("/doc/{fileId}")
    public void getDoc(HttpServletResponse response, HttpSession session, @PathVariable String fileId) {
        super.getFile(response, session, fileId);
    }

    /**
     * 创建新目录
     *
     * @param filePid  目录父级ID
     * @param fileName 文件名
     * @return 文件创建结果
     */
    @GlobalInterceptor(checkLogin = true, checkParameters = true)
    @PostMapping("/folder")
    public ResponseVO<FileInfoVo> createFolder(HttpSession session, String filePid, @VerifyParameter(required = true) String fileName) {
        SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
        FileInfoVo fileInfoVo = fileInfoService.createFolder(sessionWebUserDto, filePid, fileName);
        return getSuccessResponseVO(fileInfoVo);
    }

    /**
     * 获取目录信息
     *
     * @param path 目录路径
     * @return 目录信息
     */

    @GlobalInterceptor(checkLogin = true, checkParameters = true)
    @GetMapping("/folder")
    public ResponseVO<?> getFolderInfo(HttpSession session, String path) {
        SessionWebUserDto userInfo = getUserInfoFromSession(session);
        String userId = userInfo.getUserId();
        return super.getFolderInfo(path, userId);
    }


    /**
     * 重命名文件或目录
     *
     * @param session  会话
     * @param fileId   文件ID
     * @param fileName 文件名
     * @return 文件信息VO
     */
    @GlobalInterceptor(checkLogin = true, checkParameters = true)
    @PostMapping("/rename")
    public ResponseVO<FileInfoVo> rename(HttpSession session, @VerifyParameter(required = true) String fileId, @VerifyParameter(required = true) String fileName) {
        SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
        String userId = sessionWebUserDto.getUserId();
        FileInfoVo fileInfo = fileInfoService.renameFile(userId, fileId, fileName);
        return getSuccessResponseVO(fileInfo);
    }

    @GlobalInterceptor(checkLogin = true, checkParameters = true)
    @PostMapping("/move")
    public ResponseVO<?> changeFolder(HttpSession session, @VerifyParameter(required = true) String fileIds, @VerifyParameter(required = true) String filePid) {
        String[] fileIdArr = fileIds.split(",");
        if (fileIdArr.length == 0) {
            throw new BusinessException(ResponseCodeEnum.CODE_400);
        }
        if (ArrayUtils.contains(fileIdArr, filePid)) {
            throw new BusinessException(ResponseCodeEnum.CODE_400);
        }
        SessionWebUserDto userInfo = getUserInfoFromSession(session);
        String userId = userInfo.getUserId();
        fileInfoService.moveFile(fileIdArr, filePid, userId);
        return getSuccessResponseVO(null);
    }

    /**
     * 创建下载链接
     *
     * @param session 会话
     * @param fileId  文件ID
     * @return 下载链接
     */
    @GlobalInterceptor(checkLogin = true, checkParameters = true)
    @GetMapping("/createDownloadUrl")
    public ResponseVO<?> createDownloadUrl(HttpSession session, @VerifyParameter(required = true) String fileId) {
        SessionWebUserDto userInfo = getUserInfoFromSession(session);
        String userId = userInfo.getUserId();
        return super.createDownloadUrl(fileId, userId);
    }

    /**
     * 下载文件
     *
     * @param response 响应
     * @param code     下载链接
     */
    @GetMapping("/download/{code}")
    public void download(HttpServletResponse response, @PathVariable String code) {
        DownloadFileDto downloadFileDto = redisComponent.getDownloadCode(code);
        if (downloadFileDto == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
        String filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER + downloadFileDto.getFilePath();
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment;filename=" + downloadFileDto.getFileName());
        super.getFileResponse(response, filePath);
    }

    /**
     * 回收文件
     *
     * @param session 会话
     * @param fileIds 文件id列表
     * @return 回收结果
     */
    @GlobalInterceptor(checkLogin = true)
    @GetMapping("/remove/{fileIds}")
    public ResponseVO<?> recycleFile(HttpSession session, @PathVariable String fileIds) {
        SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
        String userId = sessionWebUserDto.getUserId();
        String[] fileIdArr = fileIds.split(",");
        if (fileIdArr.length == 0) {
            throw new BusinessException(ResponseCodeEnum.CODE_400);
        }
        fileInfoService.recycleFile(userId, fileIdArr);
        return getSuccessResponseVO(null);
    }
    @GlobalInterceptor(checkLogin = true)
    @GetMapping("/delete/{fileIds}")
    public ResponseVO<?> deleteFile(HttpSession session,@PathVariable String fileIds){

        return getSuccessResponseVO(null);
    }
}
