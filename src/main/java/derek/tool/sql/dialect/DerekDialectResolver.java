package derek.tool.sql.dialect;

import derek.tool.sql.settings.CustomDialectDTO;
import derek.tool.sql.settings.CustomDialectSettings;
import derek.tool.sql.settings.DbInfoDTO;
import derek.tool.sql.util.IdeaConfigUtil;
import org.hibernate.HibernateException;
import org.hibernate.dialect.Database;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolver;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * @author Derek
 * @date 2022/10/27
 */
public class DerekDialectResolver implements DialectResolver {
    private static final long serialVersionUID = -4155750279270333633L;

    @Override
    public Dialect resolveDialect(DialectResolutionInfo info) {
        String databaseName = info.getDatabaseName();
        for (Database database : Database.values()) {
            Dialect dialect = database.resolveDialect(info);
            if (dialect != null) {
                return dialect;
            }
            boolean lastVersionDialect = database.name().equals(databaseName)
                    && info.getDatabaseMajorVersion() == -1;
            if (lastVersionDialect) {
                try {
                    return database.latestDialect()
                            .getDeclaredConstructor()
                            .newInstance();
                } catch (Exception e) {
                    throw new HibernateException(e);
                }
            }
        }
        DelegateDialect delegateDialect = getDelegateDialect(info);
        if (delegateDialect != null) {
            return delegateDialect;
        }
        return null;
    }

    /**
     * 根据数据库信息获取已保存的自定义数据库配置，并生成返回自定义数据库方言
     * @param info
     * @return
     */
    @Nullable
    public DelegateDialect getDelegateDialect(DialectResolutionInfo info) {
        String databaseName = info.getDatabaseName();
        List<CustomDialectDTO> dtoList = IdeaConfigUtil.get(CustomDialectSettings.DIALECT_KEY);
        if (dtoList == null) {
            return null;
        }
        CustomDialectDTO candidate = null;
        for (CustomDialectDTO dialectDTO : dtoList) {
            DbInfoDTO dbInfo = dialectDTO.getDbInfo();
            if (Objects.equals(dbInfo, info)) {
                return new DelegateDialect(dialectDTO);
            }
            if (Objects.equals(databaseName, dbInfo.getDatabaseName())) {
                if (candidate == null) {
                    candidate = dialectDTO;
                } else {
                    //当前版本数据库比候选数据库版本高
                    if (dbInfo.getDatabaseMajorVersion() >= candidate.getDbInfo().getDatabaseMajorVersion()) {
                        candidate = dialectDTO;
                    }
                }
            }
        }
        if (candidate != null) {
            return new DelegateDialect(candidate);
        }
        return null;
    }
}
