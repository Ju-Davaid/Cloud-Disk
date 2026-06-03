package ju.pioneer.cloud_disk.entity;

import lombok.Getter;

import java.util.Date;

@Getter
public class FileInfo extends FileInfoKey {
    private String fileMd5;

    private String filePid;

    private Long fileSize;

    private String fileName;

    private String fileCover;

    private String filePath;

    private Date createTime;

    private Date lastUpdateTime;

    private Boolean folderType;

    private Boolean fileCategory;

    private Boolean fileType;

    private Boolean status;

    private Date recoveryTime;

    private Boolean delFlag;

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5 == null ? null : fileMd5.trim();
    }

    public void setFilePid(String filePid) {
        this.filePid = filePid == null ? null : filePid.trim();
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName == null ? null : fileName.trim();
    }

    public void setFileCover(String fileCover) {
        this.fileCover = fileCover == null ? null : fileCover.trim();
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath == null ? null : filePath.trim();
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public void setFolderType(Boolean folderType) {
        this.folderType = folderType;
    }

    public void setFileCategory(Boolean fileCategory) {
        this.fileCategory = fileCategory;
    }

    public void setFileType(Boolean fileType) {
        this.fileType = fileType;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public void setRecoveryTime(Date recoveryTime) {
        this.recoveryTime = recoveryTime;
    }

    public void setDelFlag(Boolean delFlag) {
        this.delFlag = delFlag;
    }
}