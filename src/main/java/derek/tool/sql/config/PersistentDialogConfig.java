package derek.tool.sql.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.OptionTag;
import derek.tool.sql.dialog.DialectSqlDTO;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
@State(name = "BJDialectInteractViewComponent", storages = {@Storage("BJDialectInteractViewComponent.xml")})
public final class PersistentDialogConfig implements PersistentStateComponent<PersistentDialogConfig> {

    public boolean tableUpperCase;
    public boolean prettier;
    public boolean columnUpperCase;
    @OptionTag(converter = DatabaseDtoConverter.class)
    public List<DialectSqlDTO> infoList;

    public static PersistentDialogConfig getInstance(Project project) {
        return project.getService(PersistentDialogConfig.class);
    }

    @Override
    public @Nullable PersistentDialogConfig getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull PersistentDialogConfig persistentDialog) {
        XmlSerializerUtil.copyBean(persistentDialog, this);
    }


}
