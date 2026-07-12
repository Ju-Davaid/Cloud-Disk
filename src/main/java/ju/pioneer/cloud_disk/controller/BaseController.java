package ju.pioneer.cloud_disk.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import ju.pioneer.cloud_disk.constants.Constants;
import ju.pioneer.cloud_disk.entity.dto.SessionWebUserDto;
import ju.pioneer.cloud_disk.entity.enums.ResponseCodeEnum;
import ju.pioneer.cloud_disk.entity.vo.PaginateResultVo;
import ju.pioneer.cloud_disk.entity.vo.ResponseVO;
import ju.pioneer.cloud_disk.exception.BusinessException;
import ju.pioneer.cloud_disk.utils.CopyTools;
import ju.pioneer.cloud_disk.utils.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

// 基础控制器
public class BaseController {

    private static final Logger logger = LoggerFactory.getLogger(BaseController.class);

    protected static final String STATUS_SUCCESS = "success";

    protected static final String STATUS_ERROR = "error";

    /**
     * 成功响应VO
     *
     * @param t   数据
     * @param <T> 数据类型
     * @return 成功响应VO
     */
    protected <T> ResponseVO<T> getSuccessResponseVO(T t) {
        ResponseVO<T> responseVO = new ResponseVO<>();
        responseVO.setStatus(STATUS_SUCCESS);
        responseVO.setCode(ResponseCodeEnum.CODE_200.getCode());
        responseVO.setInfo(ResponseCodeEnum.CODE_200.getMsg());
        responseVO.setData(t);
        return responseVO;
    }

    /**
     * 业务异常响应VO
     *
     * @param e   业务异常
     * @param t   数据
     * @param <T> 数据类型
     * @return 业务异常响应VO
     */
    protected <T> ResponseVO<T> getBusinessErrorResponseVO(BusinessException e, T t) {
        ResponseVO<T> vo = new ResponseVO<>();
        vo.setStatus(STATUS_ERROR);
        if (e.getCode() == null) {
            vo.setCode(ResponseCodeEnum.CODE_600.getCode());
        } else {
            vo.setCode(e.getCode());
        }
        vo.setInfo(e.getMessage());
        vo.setData(t);
        return vo;
    }

    /**
     * 服务器异常响应VO
     *
     * @param t   数据
     * @param <T> 数据类型
     * @return 服务器异常响应VO
     */
    protected <T> ResponseVO<T> getServerErrorResponseVO(T t) {
        ResponseVO<T> vo = new ResponseVO<>();
        vo.setStatus(STATUS_ERROR);
        vo.setCode(ResponseCodeEnum.CODE_500.getCode());
        vo.setInfo(ResponseCodeEnum.CODE_500.getMsg());
        vo.setData(t);
        return vo;
    }

    /**
     * 获取文件响应
     *
     * @param response 响应
     * @param filePath 文件路径
     */
    protected void getFileResponse(HttpServletResponse response, String filePath) {
        if (StringTools.isPathValid(filePath)) return;
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) return;
        try (FileInputStream fileInputStream = new FileInputStream(filePath); OutputStream outputStream = response.getOutputStream()) {
            byte[] byteData = new byte[1024];
            int len;
            while ((len = fileInputStream.read(byteData)) != -1) {
                outputStream.write(byteData, 0, len);
            }
            outputStream.flush();
        } catch (IOException exception) {
            logger.error("获取文件响应失败", exception);
        }
    }

    /**
     * 从会话中获取用户信息
     *
     * @param session 会话
     * @return 用户信息
     */
    protected SessionWebUserDto getUserInfoFromSession(HttpSession session) {
        return (SessionWebUserDto) session.getAttribute(Constants.SESSION_WEB_USER_KEY);
    }

    /**
     * 分页结果VO转换
     *
     * @param result 分页结果VO
     * @param tClass 目标VO类型
     * @return 目标VO
     */
    protected <S, T> PaginateResultVo<T> convertPaginateResultVo(PaginateResultVo<S> result, Class<T> tClass) {
        PaginateResultVo<T> resultVo = new PaginateResultVo<>();
        resultVo.setList(CopyTools.copyList(result.getList(), tClass));
        resultVo.setPageNo(result.getPageNo());
        resultVo.setPageTotal(result.getPageTotal());
        resultVo.setTotalCount(result.getTotalCount());
        resultVo.setPageSize(result.getPageSize());
        return resultVo;

    }
}
