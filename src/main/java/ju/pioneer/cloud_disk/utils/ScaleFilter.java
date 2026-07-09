package ju.pioneer.cloud_disk.utils;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;

public class ScaleFilter {

    private static final Logger logger = LoggerFactory.getLogger(ScaleFilter.class);

    /**
     * 从视频中提取封面
     *
     * @param sourceFile 视频文件
     * @param width      宽度
     * @param targetFile 目标文件
     */
    public static void createCoverOfVideo(File sourceFile, int width, File targetFile) {
        /*
         *   ffmpeg
         *   -i %s              输入文件路径占位符（第一个%s：原视频路径）
         *   -y                 自动覆盖输出文件，不弹窗询问确认
         *   -vframes 1         只输出 1 帧画面（只截取一张图）
         *   -vf scale=%d:%d/a  视频滤镜：缩放分辨率
         *    scale=宽:高/a
         *   %d                 宽占位符
         *    %d                高占位符
         *    /a                保持原始视频宽高比，避免拉伸变形
         *    %s                输出图片路径占位符（最后一个%s：图片保存地址）
         * */
        try {
            String cmd = String.format("ffmpeg -i %s -y -vframes 1 -vf scale=%d:%d/a %s", sourceFile.getAbsolutePath(), width, width, targetFile.getAbsolutePath());
            ProcessUtils.executeCommand(cmd, false);
        } catch (Exception e) {
            logger.error("从视频中提取封面失败", e);
        }
    }

    /**
     * 创建缩略图
     *
     * @param file           图片文件
     * @param thumbnailWidth 缩略图的宽度
     * @param targetFile     目标文件
     * @param delSource      是否删除源文件
     * @return 是否成功
     */
    public static Boolean createThumbnailWidthFFmpeg(File file, int thumbnailWidth, File targetFile, Boolean delSource) {
        try {
            BufferedImage src = ImageIO.read(file);
            //thumbnailWidth 缩略图的宽度   thumbnailHeight 缩略图的高度
            int sorceW = src.getWidth();
            int sorceH = src.getHeight();
            //小于 指定高宽不压缩
            if (sorceW <= thumbnailWidth) {
                return false;
            }
            compressImage(file, thumbnailWidth, targetFile, delSource);
            return true;
        } catch (Exception e) {
            logger.error("创建缩略图失败", e);
        }
        return false;
    }

    /**
     * 根据比例压缩图片
     *
     * @param sourceFile      源文件
     * @param widthPercentage 宽度比例
     * @param targetFile      目标文件
     */
    public static void compressImageWidthPercentage(File sourceFile, BigDecimal widthPercentage, File targetFile) {
        try {
            BigDecimal widthResult = widthPercentage.multiply(new BigDecimal(ImageIO.read(sourceFile).getWidth()));
            compressImage(sourceFile, widthResult.intValue(), targetFile, true);
        } catch (Exception e) {
            logger.error("压缩图片失败");
        }
    }

    /**
     * 根据宽度压缩图片
     *
     * @param sourceFile 源文件
     * @param width      宽度
     * @param targetFile 目标文件
     * @param delSource  是否删除源文件
     */
    public static void compressImage(File sourceFile, Integer width, File targetFile, Boolean delSource) {
        try {
            String cmd = "ffmpeg -i %s -vf scale=%d:-1 %s -y";
            ProcessUtils.executeCommand(String.format(cmd, sourceFile.getAbsoluteFile(), width, targetFile.getAbsoluteFile()), false);
            if (delSource) {
                FileUtils.forceDelete(sourceFile);
            }
        } catch (Exception e) {
            logger.error("压缩图片失败");
        }
    }

}
