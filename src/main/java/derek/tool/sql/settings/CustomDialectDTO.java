package derek.tool.sql.settings;

import derek.tool.sql.dialect.SqlType;
import lombok.Data;

import java.util.List;

/**
 * @author Derek
 * @date 2022/10/31
 */
@Data
public class CustomDialectDTO {
    List<SqlType> types;
    DbInfoDTO dbInfo;
    DbInfoDTO extendDb;

}
