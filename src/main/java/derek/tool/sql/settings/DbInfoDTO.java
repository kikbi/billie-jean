package derek.tool.sql.settings;

import com.fasterxml.jackson.annotation.JsonIgnore;
import derek.tool.sql.util.OptionalUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.util.Optional;

/**
 * @author Derek
 * @date 2022/10/31
 */
@Data
public class DbInfoDTO implements DialectResolutionInfo {
    String dbName;
    String majorVersion;
    String minorVersion;

    @JsonIgnore
    @Override
    public String getDatabaseName() {
        return dbName;
    }

    @JsonIgnore
    @Override
    public int getDatabaseMajorVersion() {
        return Optional.ofNullable(majorVersion)
                .map(OptionalUtils.STR_TO_INT)
                .orElse(-1);
    }

    @JsonIgnore
    @Override
    public int getDatabaseMinorVersion() {
        return Optional.ofNullable(minorVersion)
                .map(OptionalUtils.STR_TO_INT)
                .orElse(0);
    }

    @JsonIgnore
    @Override
    public String getDriverName() {
        return null;
    }

    @JsonIgnore
    @Override
    public int getDriverMajorVersion() {
        return -1;
    }

    @JsonIgnore
    @Override
    public int getDriverMinorVersion() {
        return 0;
    }


    public static DbInfoDTO viewToDTO(JCheckBox checkBox, JTextField version) {
        DbInfoDTO dbInfoDTO = new DbInfoDTO();
        dbInfoDTO.setDbName(checkBox.getText());
        String versionText = version.getText();
        String majorVersion = null;
        String minorVersion = null;
        if (StringUtils.isNumeric(versionText)) {
            String[] versionSplit = versionText.split("\\.");
            try {
                majorVersion = versionSplit[0];
            } catch (Exception e) {
            }
            if (versionSplit.length > 1) {
                try {
                    minorVersion = versionSplit[1];
                } catch (Exception e) {
                }
            }
        }
        dbInfoDTO.setMajorVersion(majorVersion);
        dbInfoDTO.setMinorVersion(minorVersion);
        return dbInfoDTO;
    }

    public void writeValueTo(JCheckBox checkBox, JLabel versionField) {
        checkBox.setText(dbName);
        String version = Optional.ofNullable(majorVersion)
                .map(m -> majorVersion + "." + minorVersion)
                .orElse("");
        versionField.setText(version);
    }
}
