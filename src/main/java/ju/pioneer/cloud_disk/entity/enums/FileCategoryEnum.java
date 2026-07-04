package ju.pioneer.cloud_disk.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FileCategoryEnum {
    VIDEO(1, "video", "视频"),
    MUSIC(2, "music", "音乐"),
    IMAGE(3, "image", "图片"),
    DOCUMENT(4, "document", "文档"),
    OTHER(5, "other", "其他");

    private final int category;
    private final String code;
    private final String desc;

    /**
    * 根据code获取枚举值
    * @param code 文件分类code
    * @return FileCategoryEnum枚举值，若未找到则返回null
    * */
    public static FileCategoryEnum getByCode(String code) {
        for (FileCategoryEnum category : values()) {
            if (category.getCode().equals(code)) {
                return category;
            }
        }
        return null;
    }
}
