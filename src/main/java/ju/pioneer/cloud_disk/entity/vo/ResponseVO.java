package ju.pioneer.cloud_disk.entity.vo;

import com.alibaba.fastjson.JSON;
import lombok.Data;

@Data
public class ResponseVO<T> {
    private String status;
    private Integer code;
    private String info;
    private T data;
    public String toJson() {
        return JSON.toJSONString(this);
    }
}
