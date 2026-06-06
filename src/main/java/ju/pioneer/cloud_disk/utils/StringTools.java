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
}
