package ju.pioneer.cloud_disk.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import ju.pioneer.cloud_disk.annotation.GlobalInterceptor;
import ju.pioneer.cloud_disk.entity.dto.SessionWebUserDto;
import ju.pioneer.cloud_disk.entity.enums.FileDeleteEnum;
import ju.pioneer.cloud_disk.entity.enums.ResponseCodeEnum;
import ju.pioneer.cloud_disk.entity.query.FileInfoQuery;
import ju.pioneer.cloud_disk.entity.vo.FileInfoVo;
import ju.pioneer.cloud_disk.entity.vo.PaginateResultVo;
import ju.pioneer.cloud_disk.entity.vo.ResponseVO;
import ju.pioneer.cloud_disk.exception.BusinessException;
import ju.pioneer.cloud_disk.service.FileInfoService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/recycle")
public class RecycleController extends BaseController {
    @Resource
    private FileInfoService fileInfoService;

    /**
     * 获取回收站文件列表
     *
     * @param pageNo   页码
     * @param pageSize 每页数量
     * @return 分页结果Vo<FileInfoVo>
     */
    @GlobalInterceptor(checkLogin = true)
    @GetMapping("/list")
    public ResponseVO<PaginateResultVo<FileInfoVo>> getRecycleList(HttpSession session, @RequestParam(defaultValue = "1") Integer pageNo, @RequestParam(defaultValue = "10") Integer pageSize) {
        FileInfoQuery query = new FileInfoQuery();
        query.setPageNo(pageNo);
        query.setPageSize(pageSize);
        SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
        query.setUserId(sessionWebUserDto.getUserId());
        query.setOrderBy("recovery_time desc");
        query.setDelFlag(FileDeleteEnum.RECYCLE.getFlag());
        return getSuccessResponseVO(fileInfoService.findListByPage(query));
    }

    @GlobalInterceptor(checkLogin = true)
    @GetMapping("/recover/{fileIds}")
    public ResponseVO<?> recoverFile(HttpSession session, @PathVariable String fileIds) {
        SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
        String userId = sessionWebUserDto.getUserId();
        String[] fileIdArr = fileIds.split(",");
        if (fileIdArr.length == 0) {
            throw new BusinessException(ResponseCodeEnum.CODE_400);
        }
        fileInfoService.recoverFile(userId, fileIdArr);
        return getSuccessResponseVO(null);
    }

    @GlobalInterceptor(checkLogin = true)
    @GetMapping("/delete/{fileIds}")
    public ResponseVO<?> deleteFile(HttpSession session, @PathVariable String fileIds) {
        SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
        String userId = sessionWebUserDto.getUserId();
        String[] fileIdArr = fileIds.split(",");
        if (fileIdArr.length == 0) {
            throw new BusinessException(ResponseCodeEnum.CODE_400);
        }
        fileInfoService.deleteFile(userId, fileIdArr, false);
        return getSuccessResponseVO(null);
    }
}
