package ju.pioneer.cloud_disk.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import ju.pioneer.cloud_disk.annotation.GlobalInterceptor;
import ju.pioneer.cloud_disk.annotation.VerifyParameter;
import ju.pioneer.cloud_disk.component.RedisComponent;
import ju.pioneer.cloud_disk.constants.Constants;
import ju.pioneer.cloud_disk.entity.dto.DownloadFileDto;
import ju.pioneer.cloud_disk.entity.dto.SessionShareDto;
import ju.pioneer.cloud_disk.entity.dto.SessionWebUserDto;
import ju.pioneer.cloud_disk.entity.enums.FileDeleteEnum;
import ju.pioneer.cloud_disk.entity.enums.ResponseCodeEnum;
import ju.pioneer.cloud_disk.entity.po.FileInfo;
import ju.pioneer.cloud_disk.entity.po.FileShare;
import ju.pioneer.cloud_disk.entity.po.UserInfo;
import ju.pioneer.cloud_disk.entity.query.FileInfoQuery;
import ju.pioneer.cloud_disk.entity.vo.FileInfoVo;
import ju.pioneer.cloud_disk.entity.vo.PaginateResultVo;
import ju.pioneer.cloud_disk.entity.vo.ResponseVO;
import ju.pioneer.cloud_disk.entity.vo.ShareInfoVo;
import ju.pioneer.cloud_disk.exception.BusinessException;
import ju.pioneer.cloud_disk.service.FileInfoService;
import ju.pioneer.cloud_disk.service.FileShareService;
import ju.pioneer.cloud_disk.service.UserInfoService;
import ju.pioneer.cloud_disk.utils.CopyTools;
import ju.pioneer.cloud_disk.utils.StringTools;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController("outerShareController")
@RequestMapping("/outer/share")
public class OuterShareController extends BaseFileController {
    @Resource
    private FileShareService fileShareService;
    @Resource
    private UserInfoService userInfoService;
    @Resource
    private FileInfoService fileInfoService;
    @Resource
    private RedisComponent redisComponent;

    /**
     * 获取登录用户信息
     *
     * @param session 会话
     * @param shareId 分享ID
     * @return 登录用户信息
     */
    @GlobalInterceptor(checkParameters = true)
    @GetMapping("/getLoginInfo")
    public ResponseVO<?> getLoginInfo(HttpSession session, @VerifyParameter(required = true) String shareId) {
        SessionShareDto sessionShareDto = getSessionShareDtoFromSession(session, shareId);
        if (sessionShareDto == null) {
            return getSuccessResponseVO(null);
        }
        ShareInfoVo shareInfoVo = _getShareInfoVo(shareId);
        SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
        shareInfoVo.setCurrentUser(sessionWebUserDto != null && sessionWebUserDto.getUserId().equals(shareInfoVo.getUserId()));
        return getSuccessResponseVO(shareInfoVo);
    }

    /**
     * 获取分享信息
     *
     * @param shareId 分享ID
     * @return 分享信息
     */
    @GlobalInterceptor(checkParameters = true)
    @GetMapping("")
    public ResponseVO<?> getShareInfo(@VerifyParameter(required = true) String shareId) {
        return getSuccessResponseVO(_getShareInfoVo(shareId));
    }

    /**
     * 检查提取分享码
     *
     * @param code 分享码
     * @return 分享码检查结果
     */
    @GlobalInterceptor(checkParameters = true)
    @GetMapping("/check")
    public ResponseVO<?> checkShareCode(HttpSession session, @VerifyParameter(required = true) String shareId, @VerifyParameter(required = true) String code) {
        SessionShareDto sessionShareDto = fileShareService.checkShareCode(shareId, code);
        session.setAttribute(Constants.SESSION_SHARE_DTO_KEY + shareId, sessionShareDto);
        return getSuccessResponseVO(null);
    }

    @GlobalInterceptor(checkParameters = true)
    @GetMapping("/files")
    public ResponseVO<?> getShareFiles(HttpSession session, @VerifyParameter(required = true) String shareId, @VerifyParameter(required = true) String filePid) {
        SessionShareDto sessionShareDto = checkShare(session, shareId);
        FileInfoQuery query = new FileInfoQuery();
        if (!StringTools.isEmpty(filePid) && !Constants.USER_ROOT_DIRECTORY_ID.equals(filePid)) {
            fileInfoService.checkRootDirectory(filePid, sessionShareDto.getUserId(), sessionShareDto.getFileId());
            query.setFilePid(filePid);
        } else {
            query.setFileId(sessionShareDto.getFileId());
        }
        query.setUserId(sessionShareDto.getUserId());
        query.setOrderBy("last_update_time desc");
        query.setDelFlag(FileDeleteEnum.USING.getFlag());
        PaginateResultVo<FileInfoVo> paginateResultVo = fileInfoService.findListByPage(query);
        return getSuccessResponseVO(paginateResultVo);
    }

    /**
     * 获取分享文件夹信息
     *
     * @param session 会话
     * @param shareId 分享ID
     * @param path    文件夹路径
     * @return 文件夹信息
     */
    @GlobalInterceptor(checkParameters = true)
    @GetMapping("/getFolderInfo")
    public ResponseVO<?> getFolderInfo(HttpSession session, @VerifyParameter(required = true) String shareId, @VerifyParameter(required = true) String path) {
        SessionShareDto sessionShareDto = checkShare(session, shareId);
        return super.getFolderInfo(path, sessionShareDto.getUserId());
    }

    /**
     * 获取文件内容
     *
     * @param response 响应
     * @param shareId  分享ID
     * @param fileId   文件ID
     */
    @GetMapping("/getFile/{shareId}/{fileId}")
    public void getFile(HttpSession session, HttpServletResponse response, @PathVariable String shareId, @PathVariable String fileId) {
        SessionShareDto sessionShareDto = checkShare(session, shareId);
        super.getFile(response, sessionShareDto.getUserId(), fileId);
    }

    /**
     * 获取视频流
     *
     * @param response 响应
     * @param shareId  分享ID
     * @param fileId   文件ID
     */
    @GetMapping("/video/stream/{shareId}/{fileId}")
    public void playVideo(HttpSession session, HttpServletResponse response, @PathVariable String shareId, @PathVariable String fileId) {
        SessionShareDto sessionShareDto = checkShare(session, shareId);
        super.getFile(response, sessionShareDto.getUserId(), fileId);
    }

    /**
     * 创建下载链接
     *
     * @param shareId 分享ID
     * @param fileId  文件ID
     * @return 下载链接
     */
    @GetMapping("/createDownloadUrl/{shareId}/{fileId}")
    public ResponseVO<?> createDownloadUrl(HttpSession session, @PathVariable String shareId, @PathVariable String fileId) {
        SessionShareDto sessionShareDto = checkShare(session, shareId);
        return super.createDownloadUrl(fileId, sessionShareDto.getUserId());
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

    @GlobalInterceptor(checkLogin = true, checkParameters = true)
    @GetMapping("/save")
    public ResponseVO<?> save(HttpSession session, @VerifyParameter(required = true) String shareId, @VerifyParameter(required = true) String fileIds, @VerifyParameter(required = true) String targetFolderId) {
        SessionShareDto sessionShareDto = checkShare(session, shareId);
        SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
//        if (!sessionWebUserDto.getUserId().equals(sessionShareDto.getUserId())) {
//            throw new BusinessException("自己不能保存自己分享文件");
//        }
        fileInfoService.saveShareFile(sessionShareDto.getFileId(), fileIds, targetFolderId, sessionShareDto.getUserId(), sessionWebUserDto.getUserId());
        return getSuccessResponseVO(null);
    }

    /**
     * 校验分享信息
     *
     * @param session 会话
     * @param shareId 分享ID
     * @return 分享信息
     */
    private SessionShareDto checkShare(HttpSession session, String shareId) {
        SessionShareDto sessionShareDto = getSessionShareDtoFromSession(session, shareId);
        if (sessionShareDto == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_903);
        }
        if (sessionShareDto.getExpireTime() != null && new Date().after(sessionShareDto.getExpireTime())) {
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }
        return sessionShareDto;
    }

    /**
     * 获取分享信息VO
     *
     * @return 分享信息VO
     */
    private ShareInfoVo _getShareInfoVo(String shareId) {
        FileShare share = fileShareService.getFileShareByShareId(shareId);
        if (share == null || (share.getExpireTime() != null && new Date().after(share.getExpireTime()))) {
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }
        ShareInfoVo shareInfoVo = CopyTools.copy(share, ShareInfoVo.class);
        FileInfo fileInfo = fileInfoService.findFileInfoByFiledIdAndUserId(share.getFileId(), share.getUserId());
        if (fileInfo == null || !fileInfo.getDelFlag().equals(FileDeleteEnum.USING.getFlag())) {
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }
        UserInfo userInfo = userInfoService.selectById(share.getUserId());
        shareInfoVo.setNickName(userInfo.getNickName());
        shareInfoVo.setAvatar(userInfo.getQqAvatar());
        shareInfoVo.setFileId(fileInfo.getFileId());
        shareInfoVo.setFileName(fileInfo.getFileName());
        return shareInfoVo;
    }
}
