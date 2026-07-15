package ju.pioneer.cloud_disk.entity.query;

import lombok.Data;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

@Data
public class RecursiveFileInfoQuery {
    private String userId;
    private Integer resultFolderType;
    private String[] parentFileIdArray;
    private Integer parentDelFlag;
    private Integer childDelFlag;
    private String orderBy;
    private Boolean isIncludeParent = false;
}
