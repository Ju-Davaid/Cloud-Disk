package ju.pioneer.cloud_disk.entity.enums;

import java.util.HashMap;
import java.util.Map;

// 类型枚举
public enum TypeEnum {
    STRING,
    INTEGER,
    LONG,
    DOUBLE,
    BOOLEAN,
    NULL;
    // 类型映射
    public static final Map<TypeEnum, String> TYPE_MAP = new HashMap<>();
    // 初始化类型映射
    static {
        TYPE_MAP.put(STRING, "java.lang.String");
        TYPE_MAP.put(INTEGER, "java.lang.Integer");
        TYPE_MAP.put(LONG, "java.lang.Long");
        TYPE_MAP.put(DOUBLE, "java.lang.Double");
        TYPE_MAP.put(BOOLEAN, "java.lang.Boolean");
        TYPE_MAP.put(NULL, "null");
    }
    @Override
    public String toString() {
        return TYPE_MAP.get(this);
    }
}