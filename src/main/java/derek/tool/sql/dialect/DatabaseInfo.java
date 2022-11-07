package derek.tool.sql.dialect;

import derek.tool.sql.util.StringUtil;
import lombok.Data;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;

import java.util.Optional;

/**
 * @author Derek
 * @date 2022/6/17
 */
@Data
public class DatabaseInfo implements DialectResolutionInfo {
    /**
     * 这些数据库名标识来源于{@link org.hibernate.dialect.Database}
     */
    public static final String MYSQL = "MySQL";
    public static final String ORACLE = "Oracle";
    public static final String MS_SQLSERVER = "Microsoft SQL Server";
    public static final String POSTGRESQL = "PostgreSQL";


    protected String databaseName;

    protected Integer majorVersion;

    protected Integer minorVersion;

    protected String driverName;

    protected Integer driverMajorVersion;

    protected Integer driverMinorVersion;

    public DatabaseInfo() {
    }

    public DatabaseInfo(String databaseName, int majorVersion, int minorVersion) {
        this.databaseName = databaseName;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
    }

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    @Override
    public int getDatabaseMajorVersion() {
        return Optional.ofNullable(majorVersion).orElse(-1);
    }

    @Override
    public int getDatabaseMinorVersion() {
        return Optional.ofNullable(minorVersion).orElse(0);
    }

    @Override
    public String getDriverName() {
        return driverName;
    }

    @Override
    public int getDriverMajorVersion() {
        return Optional.ofNullable(driverMajorVersion).orElse(-1);
    }

    @Override
    public int getDriverMinorVersion() {
        return Optional.ofNullable(driverMinorVersion).orElse(0);
    }

    public String dataBaseInfo(){
        String str = databaseName;
        str = StringUtil.addIf(majorVersion!=null&&majorVersion!=-1,str," " + majorVersion + "." + minorVersion);
        return StringUtil.addIf(driverName != null,
                str,
                "driver " + driverName + " " + driverMajorVersion + "." + driverMinorVersion
        );
    }

    @Override
    public String toString() {
        return dataBaseInfo();
    }
}
