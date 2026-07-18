package ju.pioneer.cloud_disk.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import ju.pioneer.cloud_disk.annotation.GlobalInterceptor;
import ju.pioneer.cloud_disk.annotation.VerifyParameter;
import ju.pioneer.cloud_disk.component.RedisComponent;
import ju.pioneer.cloud_disk.constants.Constants;
import ju.pioneer.cloud_disk.entity.dto.DownloadFileDto;
import ju.pioneer.cloud_disk.entity.enums.ResponseCodeEnum;
import ju.pioneer.cloud_disk.entity.query.FileInfoQuery;
import ju.pioneer.cloud_disk.entity.query.UserInfoQuery;
import ju.pioneer.cloud_disk.entity.vo.FileInfoVo;
import ju.pioneer.cloud_disk.entity.vo.PaginateResultVo;
import ju.pioneer.cloud_disk.entity.vo.ResponseVO;
import ju.pioneer.cloud_disk.entity.vo.UserInfoVo;
import ju.pioneer.cloud_disk.exception.BusinessException;
import ju.pioneer.cloud_disk.service.FileInfoService;
import ju.pioneer.cloud_disk.service.UserInfoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.http.HttpResponse;

@RestController
@RequestMapping("/admin")
public class AdminController extends BaseFileController {
    @Resource
    private UserInfoService userInfoService;
    @Resource
    private FileInfoService fileInfoService;

    @Resource
    private RedisComponent redisComponent;


    /**
     * 获取用户列表
     *
     * @param query 用户查询参数
     * @return 用户列表
     */
    @GlobalInterceptor(checkAdmin = true, checkLogin = true)
    @GetMapping("/users")
    public ResponseVO<PaginateResultVo<UserInfoVo>> getUsers(UserInfoQuery query) {
        query.setOrderBy("join_time desc");
        PaginateResultVo<UserInfoVo> userList = userInfoService.selectList(query);
        return getSuccessResponseVO(userList);
    }

    /**
     * 设置用户状态
     *
     * @param userId 用户ID
     * @param status 用户状态
     * @return 响应VO
     */
    @GlobalInterceptor(checkParameters = true, checkAdmin = true, checkLogin = true)
    @GetMapping("/users/status")
    public ResponseVO<?> setUserStatus(@VerifyParameter(required = true) String userId, @VerifyParameter(required = true) Integer status) {
        userInfoService.updateStatus(userId, status);
        return getSuccessResponseVO(null);
    }

    /**
     * 设置用户空间
     *
     * @param userId 用户ID
     * @param space  用户空间
     * @return 响应VO
     */
    @GlobalInterceptor(checkParameters = true, checkAdmin = true, checkLogin = true)
    @GetMapping("/users/space")
    public ResponseVO<?> setUserSpace(@VerifyParameter(required = true) String userId, @VerifyParameter(required = true) Integer space) {
        userInfoService.updateUserSpace(userId, space);
        redisComponent.resetUserSpaceUse(userId);
        return getSuccessResponseVO(null);
    }

    /**
     * 获取文件列表
     *
     * @param query 文件查询参数
     * @return 文件列表
     */
    @GlobalInterceptor(checkParameters = true, checkAdmin = true, checkLogin = true)
    @GetMapping("/files")
    public ResponseVO<PaginateResultVo<FileInfoVo>> getFileList(FileInfoQuery query) {
        query.setOrderBy("last_update_time desc");
        query.setQueryNickName(true);
        PaginateResultVo<FileInfoVo> fileInfoVoPaginateResultVo = fileInfoService.findListByPage(query);
        return getSuccessResponseVO(fileInfoVoPaginateResultVo);
    }

    /**
     * 获取文件夹信息
     *
     * @param path 文件夹路径
     * @return 文件夹信息
     */
    @GlobalInterceptor(checkParameters = true, checkAdmin = true, checkLogin = true)
    @GetMapping("/getFolderInfo")
    public ResponseVO<?> getFolderInfo(@VerifyParameter(required = true) String path) {
        return getSuccessResponseVO(super.getFolderInfo(path, null));
    }

    /**
     * 获取文件内容
     *
     * @param response 响应
     * @param userId   用户ID
     * @param fileId   文件ID
     */
    @GlobalInterceptor(checkParameters = true, checkAdmin = true, checkLogin = true)
    @GetMapping("/getFile/{userId}/{fileId}")
    public void getFile(HttpServletResponse response, HttpSession session, @PathVariable String userId, @PathVariable String fileId) {
        super.getFile(response, userId, fileId);
    }

    /**
     * 获取视频流
     *
     * @param response 响应
     * @param userId   用户ID
     * @param fileId   文件ID
     */
    @GlobalInterceptor(checkParameters = true, checkAdmin = true, checkLogin = true)
    @GetMapping("/video/stream/{userId}/{fileId}")
    public void playVideo(HttpServletResponse response, @PathVariable String userId, @PathVariable String fileId) {
        super.getFile(response, userId, fileId);
    }

    /**
     * 创建下载链接
     *
     * @param userId 用户ID
     * @param fileId 文件ID
     * @return 下载链接
     */
    @GlobalInterceptor(checkParameters = true, checkAdmin = true, checkLogin = true)
    @GetMapping("/createDownloadUrl/{userId}/{fileId}")
    public ResponseVO<?> createDownloadUrl(@PathVariable String userId, @PathVariable String fileId) {
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
     * 删除文件
     *
     * @param userIdAndFileIds 用户ID和文件ID列表 示例：userId_123
     * @return 响应VO
     */
    @GlobalInterceptor(checkParameters = true, checkAdmin = true, checkLogin = true)
    @GetMapping("/delete/{userIdAndFileIds}")
    public ResponseVO<?> deleteFile(@PathVariable String userIdAndFileIds) {
        String[] userIdAndFileIdsArray = userIdAndFileIds.split(",");
        for (String userIdAndFileIdsItem : userIdAndFileIdsArray) {
            String userId = userIdAndFileIdsItem.split("_")[0];
            String[] fileIdArray = userIdAndFileIdsItem.split("_")[1].split(",");
            fileInfoService.deleteFile(userId, fileIdArray, true);
        }
        return getSuccessResponseVO(null);
    }

}
