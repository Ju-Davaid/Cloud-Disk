package ju.pioneer.cloud_disk.entity.query;

import lombok.Data;

@Data
public class BaseQuery {
    private PaginateQuery paginateQuery;
    private Integer pageNo;
    private Integer pageSize;
    private String orderBy;
}
