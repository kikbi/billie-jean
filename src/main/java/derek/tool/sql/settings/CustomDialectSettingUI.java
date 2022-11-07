package derek.tool.sql.settings;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import derek.tool.sql.icons.DerekIcons;
import derek.tool.sql.util.IdeaConfigUtil;
import derek.tool.sql.util.JacksonUtil;
import derek.tool.sql.util.NotificationUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Derek
 * @date 2022/10/31
 */
@Slf4j
public class CustomDialectSettingUI {
    /**
     * 上次打开路径
     */
    public static final String LAST_PATH = "derek.tool.sql.last.path";

    @Getter
    private JPanel rootPanel;

    private JComponent contentPane;

    JScrollPane scrollPane;

    JPanel dialectsPanel;

    private JPanel headerPanel;

    private JButton importButton = null;

    private JButton exportButton = null;

    private List<CustomDialectRowRender.CustomDialectRow> rows = new ArrayList<>();

    CustomDialectRowRender rowRender = new CustomDialectRowRender();

    @Getter
    private final List<CustomDialectDTO> customDialectList;

    public CustomDialectSettingUI(List<CustomDialectDTO> customDialectDTOList) {
        if (customDialectDTOList == null) {
            customDialectList = new ArrayList<>();
        } else {
            customDialectList = customDialectDTOList;
        }
        rootPanel = new JPanel(new BorderLayout());
        JPanel header = buildHeaderPanel();
        rootPanel.add(header, BorderLayout.NORTH);
        JComponent content = createContent();
        rootPanel.add(content, BorderLayout.CENTER);
    }

    void immersed(JComponent component) {
        component.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 0, component.getForeground()));
        if (rootPanel != null) {
            Color background = rootPanel.getBackground();
            component.setBackground(background);
        }

    }

    public JComponent createContent(){

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = JBUI.insets(5, 5, 5, 5);
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        JPanel headerPanel = buildHeaderPanel();
        layout.setConstraints(headerPanel, gridBagConstraints);

        gridBagConstraints.anchor = GridBagConstraints.SOUTH;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        scrollPane = buildMainPane(customDialectList);
        layout.setConstraints(scrollPane, gridBagConstraints);

        BorderLayout borderLayout = new BorderLayout();
        contentPane = new JPanel(borderLayout);
        Dimension dimension = new Dimension(600, 190);
        contentPane.setPreferredSize(dimension);
        contentPane.add(scrollPane);
        return contentPane;
    }

    private JScrollPane buildMainPane(List<CustomDialectDTO> dtoList) {
        VerticalFlowLayout centerLayout = new VerticalFlowLayout();
        dialectsPanel = new JPanel(centerLayout);
        paintDialectList(dtoList);
        JBScrollPane jbScrollPane = new JBScrollPane(dialectsPanel);
        return jbScrollPane;
    }

    private void deleteDialect(ActionEvent event, CustomDialectRowRender.CustomDialectRow component) {
        rows.remove(component);
        customDialectList.remove(component.dto);
        refreshContent();
        CustomDialectSettings.saveCustomDialect(customDialectList);
    }

    private void paintDialectList(List<CustomDialectDTO> dtoList) {
        for (CustomDialectDTO value : dtoList) {
            CustomDialectRowRender.CustomDialectRow component = rowRender.renderRow(value);
            component.deleteBt.addActionListener(event -> deleteDialect(event, component));
            dialectsPanel.add(component);
            rows.add(component);
        }
    }

    private JPanel buildHeaderPanel() {
        if (headerPanel != null) {
            return headerPanel;
        }
        headerPanel = new JPanel(new BorderLayout());
        importButton = new JButton();
        exportButton = new JButton();
        JPanel jp = new JPanel();
        jp.add(importButton);
        jp.add(exportButton);
        headerPanel.add(jp, BorderLayout.WEST);

        DerekIcons.iconOnly(exportButton, AllIcons.ToolbarDecorator.Export);
        DerekIcons.iconOnly(importButton, AllIcons.ToolbarDecorator.Import);

        immersed(exportButton);
        immersed(importButton);
        exportButton.addActionListener(this::exportDialects);
        importButton.addActionListener(this::importDialect);

        return headerPanel;
    }

    private void exportDialects(ActionEvent event) {
        var descriptor = new FileSaverDescriptor(
                "Export Setting",
                "Choose directory to export setting to",
                "json"
        );
        descriptor.withHideIgnored(false);
        var chooser = FileChooserFactory.getInstance()
                .createSaveFileDialog(descriptor, getRootPanel());
        VirtualFile toSelect = null;
        var lastLocation = IdeaConfigUtil.getString(LAST_PATH);
        if (lastLocation != null) {
            toSelect = LocalFileSystem.getInstance().refreshAndFindFileByPath(lastLocation);
        }
        val fileWrapper = chooser.save(toSelect, "customField.xml");
        if (fileWrapper != null) {
            File file = fileWrapper.getFile();
            try {
                XmlMapper xm = JacksonUtil.xml();
                if (customDialectList.size()>0) {
                    xm.writeValue(file,customDialectList.get(0));
                }
            } catch (Exception e) {
                log.error("Export custom dialect error",e);
                NotificationUtil.error("Export custom dialect error");
            }


        }
    }

    private void importDialect(ActionEvent event) {
        val descriptor = FileChooserDescriptorFactory
                .createSingleFileOrFolderDescriptor()
                .withTitle("Import Setting")
                .withDescription("Choose setting file")
                .withHideIgnored(false);
        val chooser = FileChooserFactory.getInstance().createFileChooser(descriptor, null, getRootPanel());
        VirtualFile toSelect = null;
        val lastLocation = IdeaConfigUtil.getString(LAST_PATH);
        if (lastLocation != null) {
            toSelect = LocalFileSystem.getInstance().refreshAndFindFileByPath(lastLocation);
        }
        val files = chooser.choose(null, toSelect);
        if (files.length > 0) {
            val virtualFile = files[0];
            String path = virtualFile.getPath();
            File file = new File(path);
            XmlMapper xm = JacksonUtil.xml();
            try {
                CustomDialectDTO customDialectDTO = xm.readValue(file, CustomDialectDTO.class);
                appendDialect(customDialectDTO);

            } catch (IOException e) {
                log.error("Could not read file or Incorrect file format." +
                        "You may validate xml with custom_dialect.xsd", e);
                NotificationUtil.error("Could not read file or Incorrect file format." +
                        "You may validate xml with custom_dialect.xsd");
            }

        }
    }

    private void appendDialect(CustomDialectDTO customDialectDTO) {
        customDialectList.add(customDialectDTO);
        CustomDialectRowRender.CustomDialectRow component = rowRender.renderRow(customDialectDTO);
        rows.add(component);
        dialectsPanel.add(component);
        CustomDialectSettings.saveCustomDialect(customDialectList);
    }

    /**
     * 刷新列表
     */
    private void refreshContent() {
        dialectsPanel.removeAll();
        for (CustomDialectRowRender.CustomDialectRow row : rows) {
            dialectsPanel.add(row);
        }
        dialectsPanel.repaint();
    }
}
