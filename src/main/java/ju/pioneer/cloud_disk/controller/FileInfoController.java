package ju.pioneer.cloud_disk.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import ju.pioneer.cloud_disk.annotation.GlobalInterceptor;
import ju.pioneer.cloud_disk.annotation.VerifyParameter;
import ju.pioneer.cloud_disk.constants.Constants;
import ju.pioneer.cloud_disk.entity.dto.SessionWebUserDto;
import ju.pioneer.cloud_disk.entity.enums.FileCategoryEnum;
import ju.pioneer.cloud_disk.entity.enums.FileDeleteEnum;
import ju.pioneer.cloud_disk.entity.po.FileInfo;
import ju.pioneer.cloud_disk.entity.query.FileInfoQuery;
import ju.pioneer.cloud_disk.entity.vo.FileInfoVo;
import ju.pioneer.cloud_disk.entity.vo.PaginateResultVo;
import ju.pioneer.cloud_disk.entity.vo.ResponseVO;
import ju.pioneer.cloud_disk.entity.vo.UploadResultVo;
import ju.pioneer.cloud_disk.service.FileInfoService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController("fileController")
@RequestMapping("/file")
public class FileInfoController extends BaseController {
    @Resource
    private FileInfoService fileInfoService;

    /**
     * 分页查询文件信息
     *
     * @param query 查询参数
     * @return 分页结果Vo<FileInfo>
     */
    @GlobalInterceptor(checkLogin = true, checkParameters = true)
    @PostMapping("/getFileList")
    public ResponseVO<?> getFileList(HttpSession session, @VerifyParameter(required = true) FileInfoQuery query, @VerifyParameter(required = true) String category) {
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
     * @param file       文件
     * @param fileName   文件名
     * @param filePid    父级ID
     * @param fileMd5    文件md5值
     * @param chunkIndex 分块索引
     * @param chunkCount 分块数量
     * @return 上传结果VO
     */
    @GlobalInterceptor(checkLogin = true, checkParameters = true)
    @PostMapping("/uploadFile")
    public ResponseVO<UploadResultVo> uploadFile(HttpSession session,
                                                 String fileId,
                                                 @VerifyParameter(required = true) MultipartFile file,
                                                 @VerifyParameter(required = true) String fileName,
                                                 @VerifyParameter(required = true) String filePid,
                                                 @VerifyParameter(required = true) String fileMd5,
                                                 @VerifyParameter(required = true) int chunkIndex,
                                                 @VerifyParameter(required = true) int chunkCount
    ) {
        SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
        UploadResultVo uploadResultVo = fileInfoService.uploadFile(sessionWebUserDto, fileId, file, fileName, filePid, fileMd5, chunkCount, chunkIndex);
        return getSuccessResponseVO(uploadResultVo);
    }
}
