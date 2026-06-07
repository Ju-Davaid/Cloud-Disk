package ju.pioneer.cloud_disk.utils;

import org.apache.commons.lang3.RandomStringUtils;

public class StringTools {
    /**
     * 生成随机数
     * @param count 随机数长度
     * @return 随机数
     */
    public static String getRandomNumber(int count){
        return RandomStringUtils.random(count,false,true);
    }

    /**
     * 判断字符串是否为空
     * @param str 字符串
     * @return 是否为空字符串
     */
    public static boolean isEmpty(String str) {
        if (null == str || str.isEmpty() || "null".equals(str) || "\u0000".equals(str)) {
            return true;
        } else return str.trim().isEmpty();
    }
}
