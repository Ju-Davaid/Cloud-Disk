package ju.pioneer.cloud_disk.constants;

public class Constants {
    /**
     * 用户根目录ID
     */
    public static final String USER_ROOT_DIRECTORY_ID = "root";
    /**
     * 邮箱验证码长度
     */
    public static final int LENGTH_EMAIL_CODE = 5;
    public static final int ZERO = 0;
    public static final int ONE = 1;
    public static final String CHECK_CODE_KEY = "checkCodeKey";
    public static final String CHECK_CODE_KEY_EMAIL = "checkCodeKeyEmail";
    /**
     * 邮箱验证码过期时间，单位：毫秒
     */
    public static final int EMAIL_CODE_EXPIRE_TIME = 15 * 60 * 1000;

    /**
     * 单位：MB
     */
    public static final long MB = 1024 * 1024L;
    /**
     * 默认空间：5GB，单位：字节
     */
    public static final long DEFAULT_SPACE = 5 * 1024 * MB;
    /**
     * 用户空间使用量key
     */
    public static final String REDIS_USER_SPACE_USE_KEY = "cloudDisk:userSpaceUse:%s";
    /**
     * Redis key过期时间，单位：分钟
     */
    public static final int REDIS_KEY_EXPIRE_TIME = 60 * 60 * 24;
    /**
     * 会话用户key
     */
    public static final String SESSION_WEB_USER_KEY = "sessionWebUserKey";
    /**
     * 文件存储路径
     */
    public static final String FILE_FOLDER = "/file/";
    /**
     * 用户头像存储路径
     */
    public static final String AVATAR_FOLDER = "avatar/";
    /**
     * 用户头像扩展名
     */
    public static final String AVATAR_EXTENSION_NAME = ".jpg";
    /**
     * 默认头像
     */
    public static final String DEFAULT_AVATAR = "default_avatar.jpg";
    /**
     * 临时文件存储路径
     */
    public static final String FILE_FOLDER_TEMP = "/temp/";
    /**
     * 临时文件大小key
     */
    public static final String REDIS_TEMP_FILE_SIZE_KEY = "cloudDisk:tempFileSize:%s:%s";
    /**
     * 临时文件大小过期时间，单位：分钟
     */
    public static final int REDIS_TEMP_FILE_SIZE_EXPIRE_TIME = 60;
    /**
     * 下载码key
     */
    public static final String REDIS_DOWNLOAD_CODE_KEY = "cloudDisk:downloadCode:%s";
    /**
     * 下载码过期时间，单位：分钟
     */
    public static final int REDIS_DOWNLOAD_CODE_EXPIRE_TIME = 5;
    /**
     * 视频文件切割m3u8文件名
     */
    public static final String M3U8_NAME = "index.m3u8";
    /**
     * PNG图片后缀
     */
    public static final String IMAGE_PNG_SUFFIX = ".png";

    /**
     * 视频封面大小
     */
    public static final int COVER_SIZE = 150;
}
