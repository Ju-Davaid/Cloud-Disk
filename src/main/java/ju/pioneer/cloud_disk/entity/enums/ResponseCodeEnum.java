package ju.pioneer.cloud_disk.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 响应码枚举类
@AllArgsConstructor
@Getter
public enum ResponseCodeEnum {
    CODE_200(200,"请求成功"),
    CODE_404(404,"请求地址不存在"),
    CODE_600(600,"请求参数错误"),
    CODE_400(400,"请求参数错误"),
    CODE_601(601,"信息已存在"),
    CODE_500(500,"服务器返回错误，请联系管理员");
    private final int code;
    private final String msg;
}
