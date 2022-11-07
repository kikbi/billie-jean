package derek.tool.sql.settings;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.util.NlsContexts;
import derek.tool.sql.util.IdeaConfigUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Derek
 * @date 2022/10/31
 */
public class CustomDialectSettings implements SearchableConfigurable {

    /**
     * 自定义方言保存key
     */
    public static final String DIALECT_KEY = "custom_dialects";


    CustomDialectSettingUI customDialectSettingUI;
    public CustomDialectSettings() {
        List<CustomDialectDTO> dtoList = IdeaConfigUtil.get(DIALECT_KEY);
        customDialectSettingUI = new CustomDialectSettingUI(dtoList);
    }

    @Override
    public @Nullable JComponent createComponent() {
        return customDialectSettingUI.getRootPanel();
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {
        //保存导入设置
        List<CustomDialectDTO> customDialectDTOList = customDialectSettingUI.getCustomDialectList();
        IdeaConfigUtil.save(DIALECT_KEY,customDialectDTOList);
    }

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "custom dialects";
    }

    @Override
    public @NotNull @NonNls String getId() {
        return "preference.BllieJeanConfigurable.custom_dialect";
    }

    public static List<CustomDialectDTO> getCustomDialect(){
        List<CustomDialectDTO> customDialectDTOList = IdeaConfigUtil.get(DIALECT_KEY);
        if (customDialectDTOList == null) {
            customDialectDTOList = new ArrayList<>();
        }
        return customDialectDTOList;
    }

    public static void saveCustomDialect(List<CustomDialectDTO> customDialectDTOList){
        IdeaConfigUtil.save(DIALECT_KEY,customDialectDTOList);
    }

}
