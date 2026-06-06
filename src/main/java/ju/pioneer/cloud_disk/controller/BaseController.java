package ju.pioneer.cloud_disk.controller;

import ju.pioneer.cloud_disk.entity.enums.ResponseCodeEnum;
import ju.pioneer.cloud_disk.entity.vo.ResponseVO;
import ju.pioneer.cloud_disk.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// 基础控制器
public class BaseController {

    private static final Logger logger = LoggerFactory.getLogger(BaseController.class);

    protected static final String STATUS_SUCCESS = "success";

    protected static final String STATUS_ERROR = "error";

    /**
     * 成功响应VO
     * @param t 数据
     * @return 成功响应VO
     * @param <T> 数据类型
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
     * @param e 业务异常
     * @param t 数据
     * @return 业务异常响应VO
     * @param <T> 数据类型
     */
    protected <T> ResponseVO<T> getBusinessErrorResponseVO(BusinessException e, T t){
        ResponseVO<T> vo=new ResponseVO<>();
        vo.setStatus(STATUS_ERROR);
        if(e.getCode()==null){
            vo.setCode(ResponseCodeEnum.CODE_600.getCode());
        }else{
            vo.setCode(e.getCode());
        }
        vo.setInfo(e.getMessage());
        vo.setData(t);
        return vo;
    }

    /**
     * 服务器异常响应VO
     * @param t 数据
     * @return 服务器异常响应VO
     * @param <T> 数据类型
     */
    protected <T> ResponseVO<T> getServerErrorResponseVO(T t){
        ResponseVO<T> vo=new ResponseVO<>();
        vo.setStatus(STATUS_ERROR);
        vo.setCode(ResponseCodeEnum.CODE_500.getCode());
        vo.setInfo(ResponseCodeEnum.CODE_500.getMsg());
        vo.setData(t);
        return vo;
    }
}
