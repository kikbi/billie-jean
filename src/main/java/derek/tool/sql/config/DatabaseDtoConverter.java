package derek.tool.sql.config;

import com.intellij.util.xmlb.Converter;
import derek.tool.sql.dialog.DialectSqlDTO;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Derek
 */
public class DatabaseDtoConverter extends Converter<DialectSqlDTO> {
    @Override
    public @Nullable DialectSqlDTO fromString(@NotNull String s) {
        String[] split = s.split("\\|");
        DialectSqlDTO databaseDTO = new DialectSqlDTO(
                split[0],
                Integer.parseInt(split[1]),
                Integer.parseInt(split[2]),
                split[3]
        );
        if (split.length == 5) {
            boolean selected = Boolean.parseBoolean(split[4]);
            databaseDTO.setSelected(selected);
        }

        return databaseDTO;
    }

    @Override
    public @Nullable String toString(@NotNull DialectSqlDTO dto) {
        String databaseName = dto.getDatabaseName();
        int majorVersion = dto.getMajorVersion();
        int minorVersion = dto.getMinorVersion();
        String path = dto.getPath();
        boolean selected = dto.isSelected();
        String str = String.join("|",
                databaseName,
                String.valueOf(majorVersion),
                String.valueOf(minorVersion),
                path,
                String.valueOf(selected));
        return str;
    }
}
