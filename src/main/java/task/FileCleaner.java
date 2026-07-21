package task;

import jakarta.annotation.Resource;
import ju.pioneer.cloud_disk.constants.Constants;
import ju.pioneer.cloud_disk.entity.enums.FileDeleteEnum;
import ju.pioneer.cloud_disk.entity.po.FileInfo;
import ju.pioneer.cloud_disk.entity.query.FileInfoQuery;
import ju.pioneer.cloud_disk.service.FileInfoService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FileCleaner {
    @Resource
    FileInfoService fileInfoService;

    @Scheduled(initialDelay = Constants.FILE_CLEANER_DELAY, fixedDelay = Constants.FILE_CLEANER_DELAY)
    public void clean() {
        FileInfoQuery query = new FileInfoQuery();
        query.setDelFlag(FileDeleteEnum.RECYCLE.getFlag());
        query.setQueryExpire(true);
        // 过期文件
        List<FileInfo> expireFileInfoList = fileInfoService.findListByParam(query);
        Map<String, List<FileInfo>> fileInfoMap = expireFileInfoList.stream().collect(Collectors.groupingBy(FileInfo::getUserId));
        fileInfoMap.forEach((userId, expireFileInfos) -> {
            String[] ids = expireFileInfos.stream().map(FileInfo::getFileId).toArray(String[]::new);
            fileInfoService.deleteFile(userId, ids, false);
        });
    }
}
