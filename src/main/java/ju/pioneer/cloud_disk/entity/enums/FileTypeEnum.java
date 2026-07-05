package ju.pioneer.cloud_disk.entity.enums;

import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;

@Getter
public enum FileTypeEnum {
    //1:视频 2:音频  3:图片 4:pdf 5:word 6:excel 7:txt 8:code 9:zip 10:其他文件
    VIDEO(FileCategoryEnum.VIDEO, 1, new String[]{".mp4", ".avi", ".rmvb", ".mkv", ".mov"}, "视频"),
    MUSIC(FileCategoryEnum.MUSIC, 2, new String[]{".mp3", ".wav", ".wma", ".mp2", ".flac", ".midi", ".ra", ".ape", ".aac", ".cda"}, "音频"),
    IMAGE(FileCategoryEnum.IMAGE, 3, new String[]{".jpeg", ".jpg", ".png", ".gif", ".bmp", ".dds", ".psd", ".pdt", ".webp", ".xmp", ".svg", ".tiff"}, "图片"),
    PDF(FileCategoryEnum.DOCUMENT, 4, new String[]{".pdf"}, "pdf"),
    WORD(FileCategoryEnum.DOCUMENT, 5, new String[]{".docx"}, "word"),
    EXCEL(FileCategoryEnum.DOCUMENT, 6, new String[]{".xlsx"}, "excel"),
    TXT(FileCategoryEnum.DOCUMENT, 7, new String[]{".txt"}, "txt文本"),
    PROGRAM(FileCategoryEnum.OTHER, 8, new String[]{".h", ".c", ".hpp", ".hxx", ".cpp", ".cc", ".c++", ".cxx", ".m", ".o", ".s", ".dll", ".cs",
            ".java", ".class", ".js", ".ts", ".css", ".scss", ".vue", ".jsx", ".sql", ".md", ".json", ".html", ".xml"}, "CODE"),
    ZIP(FileCategoryEnum.OTHER, 9, new String[]{"rar", ".zip", ".7z", ".cab", ".arj", ".lzh", ".tar", ".gz", ".ace", ".uue", ".bz", ".jar", ".iso",
            ".mpq"}, "压缩包"),
    OTHERS(FileCategoryEnum.OTHER, 10, new String[]{}, "其他");

    private final FileCategoryEnum category;
    private final Integer type;
    private final String[] suffixes;
    private final String desc;

    FileTypeEnum(FileCategoryEnum category, Integer type, String[] suffixes, String desc) {
        this.category = category;
        this.type = type;
        this.suffixes = suffixes;
        this.desc = desc;
    }

    public static FileTypeEnum getFileTypeBySuffix(String suffix) {
        for (FileTypeEnum item : FileTypeEnum.values()) {
            if (ArrayUtils.contains(item.getSuffixes(), suffix)) {
                return item;
            }
        }
        return FileTypeEnum.OTHERS;
    }

    public static FileTypeEnum getByType(Integer type) {
        for (FileTypeEnum item : FileTypeEnum.values()) {
            if (item.getType().equals(type)) {
                return item;
            }
        }
        return null;
    }

}
