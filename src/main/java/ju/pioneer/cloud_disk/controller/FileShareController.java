package ju.pioneer.cloud_disk.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import ju.pioneer.cloud_disk.annotation.GlobalInterceptor;
import ju.pioneer.cloud_disk.annotation.VerifyParameter;
import ju.pioneer.cloud_disk.entity.dto.SessionWebUserDto;
import ju.pioneer.cloud_disk.entity.po.FileShare;
import ju.pioneer.cloud_disk.entity.query.FileShareQuery;
import ju.pioneer.cloud_disk.entity.vo.PaginateResultVo;
import ju.pioneer.cloud_disk.entity.vo.ResponseVO;
import ju.pioneer.cloud_disk.service.FileShareService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("fileShareController")
@RequestMapping("/share")
public class FileShareController extends BaseFileController {
    @Resource
    private FileShareService fileShareService;

    /**
     * 分页查询分享列表
     *
     * @param session 会话
     * @param query   查询参数
     * @return 分页结果
     */
    @GlobalInterceptor(checkLogin = true)
    @GetMapping("/list")
    public ResponseVO<PaginateResultVo<FileShare>> getShareList(HttpSession session, FileShareQuery query) {
        SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
        query.setUserId(sessionWebUserDto.getUserId());
        query.setOrderBy("share_time desc");
        PaginateResultVo<FileShare> resultVo = fileShareService.findListByPage(query);
        return getSuccessResponseVO(resultVo);
    }

    /**
     * 创建分享
     *
     * @param session   会话
     * @param fileId    文件ID
     * @param validType 有效类型
     * @param code      有效码
     * @return 分享信息
     */
    @GlobalInterceptor(checkLogin = true, checkParameters = true)
    @PostMapping("/create")
    public ResponseVO<FileShare> createShare(HttpSession session, @VerifyParameter(required = true) String fileId, @VerifyParameter(required = true) Integer validType, String code) {
        SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
        FileShare fileShare = new FileShare();
        fileShare.setUserId(sessionWebUserDto.getUserId());
        fileShare.setFileId(fileId);
        fileShare.setValidType(validType);
        fileShare.setCode(code);
        FileShare share = fileShareService.saveShare(fileShare);
        return getSuccessResponseVO(share);
    }

    /**
     * 取消分享
     *
     * @param session  会话
     * @param shareIds 分享ID列表
     * @return 无
     */
    @GlobalInterceptor(checkLogin = true, checkParameters = true)
    @GetMapping("/cancel")
    public ResponseVO<Void> cancelShare(HttpSession session, @VerifyParameter(required = true) String shareIds) {
        String[] shareIdArray = shareIds.split(",");
        fileShareService.deleteFileShareBatch(shareIdArray, getUserInfoFromSession(session).getUserId());
        return getSuccessResponseVO(null);
    }

}
