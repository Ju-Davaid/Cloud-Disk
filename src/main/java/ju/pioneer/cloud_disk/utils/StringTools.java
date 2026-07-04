package ju.pioneer.cloud_disk.utils;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.UUID;

public class StringTools {
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    /**
     * 生成随机数
     *
     * @param count 随机数长度
     * @return 随机数
     */
    public static String getRandomNumber(int count) {
        return RandomStringUtils.random(count, false, true);
    }

    /**
     * 判断字符串是否为空
     *
     * @param str 字符串
     * @return 是否为空字符串
     */
    public static boolean isEmpty(String str) {
        if (null == str || str.isEmpty() || "null".equals(str) || "\u0000".equals(str)) {
            return true;
        } else return str.trim().isEmpty();
    }

    /**
     * 生成UUID
     *
     * @return UUID
     */
    public static String getUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 密码加密
     *
     * @param password 密码
     * @return 加密后的密码
     */
    public static String encryptPassword(String password) {
        return isEmpty(password) ? null : ENCODER.encode(password);
    }

    /**
     * 密码校验
     *
     * @param password          密码
     * @param encryptedPassword 加密后的密码
     * @return 是否校验通过
     */
    public static boolean checkPassword(String password, String encryptedPassword) {
        return ENCODER.matches(password, encryptedPassword);
    }

    /**
     * 判断路径是否有效
     *
     * @param path 路径
     * @return 是否有效
     */
    public static boolean isPathValid(String path) {
        if (isEmpty(path)) {
            return false;
        }
        return !path.contains("../") && !path.contains("..\\");
    }

    /**
     * 重命名文件名
     *
     * @param fileName 文件名
     * @return 重命名后的文件名
     */
    public static String rename(String fileName) {
        String fileNameNoSuffix = getFileNameOfNoSuffix(fileName);
        String suffix = getSuffixOfFileName(fileName);
        return fileNameNoSuffix + "_" + getRandomNumber(4) + suffix;
    }

    /**
     * 获取文件后缀名 (.xxx)
     *
     * @param fileName 文件名
     * @return 文件后缀名 (.xxx)
     */
    public static String getSuffixOfFileName(String fileName) {
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return fileName.substring(lastIndexOf);
    }

    /**
     * 获取文件名 (不包含后缀名)
     *
     * @param fileName 文件名
     * @return 文件名 (不包含后缀名)
     */
    public static String getFileNameOfNoSuffix(String fileName) {
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return fileName;
        }
        return fileName.substring(0, lastIndexOf);
    }
}
