package derek.tool.sql.dialect;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.relational.QualifiedNameImpl;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Index;

import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Derek
 * @date 2022/7/28
 * @see org.hibernate.tool.schema.internal.StandardIndexExporter
 */
public class IndexExporter {
    String[] NO_COMMANDS = new String[0];
    private final Dialect dialect;

    public IndexExporter(Dialect dialect) {
        this.dialect = dialect;
    }

    public String[] getSqlCreateStrings(Index index, Metadata metadata, SqlStringGenerationContext context) {
        JdbcEnvironment jdbcEnvironment = metadata.getDatabase().getJdbcEnvironment();
        String tableName = context.format( index.getTable().getQualifiedTableName() );

        String indexNameForCreation;
        if ( dialect.qualifyIndexName() ) {
            indexNameForCreation = context.format(
                    new QualifiedNameImpl(
                            index.getTable().getQualifiedTableName().getCatalogName(),
                            index.getTable().getQualifiedTableName().getSchemaName(),
                            jdbcEnvironment.getIdentifierHelper().toIdentifier( index.getQuotedName( dialect ) )
                    )
            );
        }
        else {
            indexNameForCreation = index.getName();
        }
        StringBuilder buf = new StringBuilder()
                .append( "create index " )
                .append( indexNameForCreation )
                .append( " on " )
                .append( tableName )
                .append( " (" );

        boolean first = true;
        Iterator<org.hibernate.mapping.Column> columnItr = index.getColumnIterator();
        Map<org.hibernate.mapping.Column, String> columnOrderMap = index.getColumnOrderMap();
        while ( columnItr.hasNext() ) {
            Column column = columnItr.next();
            if ( first ) {
                first = false;
            }
            else {
                buf.append( ", " );
            }
            buf.append( ( column.getQuotedName( dialect ) ) );
            if ( columnOrderMap.containsKey( column ) ) {
                buf.append( " " ).append( columnOrderMap.get( column ) );
            }
        }
        buf.append( ")" );
        return new String[] { buf.toString() };
    }

    public String[] getSqlDropStrings(Index index, Metadata metadata, SqlStringGenerationContext context) {
        if ( !dialect.dropConstraints() ) {
            return NO_COMMANDS;
        }

        String tableName = context.format( index.getTable().getQualifiedTableName() );

        String indexNameForCreation;
        if ( dialect.qualifyIndexName() ) {
            indexNameForCreation = StringHelper.qualify( tableName, index.getName() );
        }
        else {
            indexNameForCreation = index.getName();
        }

        return new String[] { "drop index " + indexNameForCreation };
    }
}
