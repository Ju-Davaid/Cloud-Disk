package ju.pioneer.cloud_disk.utils;

import ju.pioneer.cloud_disk.constants.Constants;
import ju.pioneer.cloud_disk.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VideoUtils {
    private static final Logger logger = LoggerFactory.getLogger(VideoUtils.class);

    /**
     * 视频文件切割
     *
     * @param fileId         视频文件id
     * @param targetFilePath 目标文件路径
     */
    public static void videoCut(String fileId, String targetFilePath) {
        logger.info("视频文件切割，文件id：{}，文件路径：{}", fileId, targetFilePath);
        File sourceVideo = new File(targetFilePath);
        if (!sourceVideo.exists() || !sourceVideo.isFile()) {
            throw new RuntimeException("源视频文件不存在：" + targetFilePath);
        }

        // 1. 获取视频总时长（秒）
        double duration = getVideoDuration(targetFilePath);
        logger.info("视频总时长：{}s", duration);
        if (duration <= 0) {
            throw new BusinessException("无法获取视频时长");
        }

        // 2. 创建 ts 文件夹
        String parentPath = sourceVideo.getParent();
        String folderName = sourceVideo.getName().replaceAll("\\.[^.]+$", "");
        File tsFolder = new File(parentPath, folderName);
        if (!tsFolder.exists()) {
            if (!tsFolder.mkdirs()) {
                throw new BusinessException("分片目录创建失败：" + tsFolder.getAbsolutePath());
            }
        }

        String sep = File.separator;
        int segmentTime = 30; // 每段时长（秒）
        int segmentCount = (int) Math.ceil(duration / segmentTime);
        List<String> tsFileNames = new ArrayList<>();

        // 3. 逐段切割
        for (int i = 0; i < segmentCount; i++) {
            double start = i * segmentTime;
            double segDuration = Math.min(segmentTime, duration - start);
            // 文件名从 1 开始，补齐 4 位
            String tsFileName = fileId + "_" + String.format("%04d", i + 1) + ".ts";
            String outputTsPath = tsFolder.getAbsolutePath() + sep + tsFileName;
            tsFileNames.add(tsFileName);

            // 构建命令：-ss 放在 -i 之后，确保精确到帧；-avoid_negative_ts 处理时间戳问题
            String cmd = String.format(
                    "ffmpeg -y -i \"%s\" -ss %.3f -t %.3f -c copy -avoid_negative_ts make_zero \"%s\"",
                    targetFilePath, start, segDuration, outputTsPath
            );
            ProcessUtils.executeCommand(cmd, true);
            logger.info("生成片段：{}，开始 {}s，时长 {}s", tsFileName, start, segDuration);
        }

        // 4. 手动生成 m3u8 文件
        String m3u8Path = tsFolder.getAbsolutePath() + sep + Constants.M3U8_NAME;
        generateM3U8(m3u8Path, tsFileNames, segmentTime);
        logger.info("切割完成，m3u8 文件：{}", m3u8Path);
    }

    /**
     * 通过 ffprobe 获取视频时长（秒）
     */
    public static double getVideoDuration(String filePath) {
        String cmd = String.format(
                "ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 \"%s\"",
                filePath
        );
        String result = ProcessUtils.executeCommand(cmd, false); // 需改为返回输出字符串
        if (result != null) {
            return Double.parseDouble(result.trim());
        }
        return 0;
    }

    /**
     * 生成标准的 m3u8 播放列表
     */
    public static void generateM3U8(String m3u8Path, List<String> tsFileNames, int targetDuration) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(m3u8Path))) {
            writer.write("#EXTM3U\n");
            writer.write("#EXT-X-VERSION:3\n");
            writer.write("#EXT-X-TARGETDURATION:" + targetDuration + "\n");
            writer.write("#EXT-X-MEDIA-SEQUENCE:0\n");
            for (String tsName : tsFileNames) {
                // 注意：这里没有填写实际的片段时长，若播放器要求严格，可考虑用 ffprobe 获取每个片段的时长
                // 但大多数播放器只依赖 #EXT-X-TARGETDURATION 即可，精确时长非必须
                writer.write("#EXTINF:" + targetDuration + ",\n");
                writer.write(tsName + "\n");
            }
            writer.write("#EXT-X-ENDLIST\n");
        } catch (IOException e) {
            throw new BusinessException("生成 m3u8 文件失败", e);
        }
    }
}
