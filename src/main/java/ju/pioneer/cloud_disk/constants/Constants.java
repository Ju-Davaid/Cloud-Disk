package ju.pioneer.cloud_disk.constants;

public class Constants {
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
     * 默认空间：5GB，单位：字节
     */
    public static final long DEFAULT_SPACE = 5 * 1024 * 1024 * 1024L;
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
}
