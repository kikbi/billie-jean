package derek.tool.sql.dialog;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import derek.tool.sql.DBContext;
import derek.tool.sql.config.PersistentDialogConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 数据库方言交互页面
 * @author Derek
 */
public class DialectInteractView extends DialogWrapper {

    private static final String LOWER_CASE = "Lower Case";
    private static final String UPPER_CASE = "Upper Case";
    private static final String COLUMN_CASE = "Column Case";
    private static final String EXPORT = "Export";
    private static final String COPY = "Copy";
    private static final String CANCEL = "Cancel";


    private JPanel contentPane;

    private DBContext dbContext;

    private final List<DialectRowRender.DialectRow> rows = new ArrayList<>();

    private ButtonGroup caseButtonGroup;

    private JPanel headerPanel;

    private JBCheckBox prettier;
    private JBCheckBox tableUpperCase;

    public DialectInteractView(DBContext dbContext) {
        super(true);
        this.dbContext = dbContext;
        init();
    }

    public DialectInteractView() {
        super(true);
        init();
    }

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[]{
                createAnAction(EXPORT, this::export),
                createAnAction(COPY, this::copy),
                createAnAction(CANCEL, this::cancel)
        };
    }

    /**
     * 取消
     *
     * @param actionEvent 事件参数
     */
    private void cancel(ActionEvent actionEvent) {
        persistentState();
        dispose();
    }

    /**
     * 复制
     *
     * @param actionEvent 事件参数
     */
    private void copy(ActionEvent actionEvent) {
        List<DialectSqlDTO> dtoList = extractInfoList();
        dbContext.setInfoList(dtoList);
        dbContext.copyToClipboard(tableUpperCase.isSelected(),isColumnUpperCase(),prettier.isSelected());
        persistentState();
        dispose();
    }

    /**
     * 导出
     *
     * @param e  事件参数
     */
    private void export(ActionEvent e) {
        List<DialectSqlDTO> dtoList = extractInfoList();
        dbContext.setInfoList(dtoList);
        dbContext.export(tableUpperCase.isSelected(),isColumnUpperCase(),prettier.isSelected());
        persistentState();
        dispose();
    }

    private void persistentState(){
        PersistentDialogConfig pdc = dbContext.getPdc();
        if (pdc == null) {
            pdc = new PersistentDialogConfig();
        }
        List<DialectSqlDTO> infoList = extractInfoList();
        pdc.setInfoList(infoList);
        pdc.setPrettier(prettier.isSelected());
        pdc.setTableUpperCase(tableUpperCase.isSelected());
        pdc.setColumnUpperCase(isColumnUpperCase());
    }

    private boolean isColumnUpperCase(){
        return caseButtonGroup.getSelection().getActionCommand() == UPPER_CASE;
    }

    private List<DialectSqlDTO> extractInfoList() {
        return rows.stream()
                .map(row -> DialectSqlDTO.viewToDTO(row.checkBox, row.versionField, row.pathField))
                .collect(Collectors.toList());
    }

    private DialogWrapperAction createAnAction(String name, Consumer<ActionEvent> consumer) {
        return new DialogWrapperAction(name) {
            private static final long serialVersionUID = -62877809570565207L;

            @Override
            protected void doAction(ActionEvent e) {
                consumer.accept(e);
            }
        };
    }

    @Override
    protected @Nullable JComponent createNorthPanel() {
        return buildHeaderPanel();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {

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
        List<DialectSqlDTO> infoList = dbContext.getInfoList();
        JScrollPane scrollPane = buildMainPane(infoList);
        layout.setConstraints(scrollPane, gridBagConstraints);

        BorderLayout borderLayout = new BorderLayout();
        contentPane = new JPanel(borderLayout);
        Dimension dimension = new Dimension(600,190);
        contentPane.setPreferredSize(dimension);
        contentPane.add(scrollPane);
        return contentPane;
    }

    @NotNull
    private JPanel buildHeaderPanel() {
        if (headerPanel != null) {
            return headerPanel;
        }
        headerPanel = new JPanel();

        prettier = new JBCheckBox("Prettify SQL");
        prettier.setSelected(dbContext.isPrettier());
        tableUpperCase = new JBCheckBox("Table Upper Case");
        tableUpperCase.setSelected(dbContext.isTableUpperCase());
        JRadioButton lowerCaseRadio = new JRadioButton(LOWER_CASE);
        lowerCaseRadio.setActionCommand(LOWER_CASE);
        JRadioButton upperCaseRadio = new JRadioButton(UPPER_CASE);
        upperCaseRadio.setActionCommand(UPPER_CASE);
        if (dbContext.isColumnUpperCase()) {
            upperCaseRadio.setSelected(true);
        }else {
            lowerCaseRadio.setSelected(true);
        }
        upperCaseRadio.setBorder(BorderFactory.createLineBorder(JBColor.GRAY));
        lowerCaseRadio.setBorder(BorderFactory.createLineBorder(JBColor.GRAY));

        caseButtonGroup = new ButtonGroup();
        caseButtonGroup.add(lowerCaseRadio);
        caseButtonGroup.add(upperCaseRadio);
        JLabel label = new JLabel(COLUMN_CASE+":");




        headerPanel.add(prettier);
        headerPanel.add(tableUpperCase);
        headerPanel.add(label);
        headerPanel.add(lowerCaseRadio);
        headerPanel.add(upperCaseRadio);

//        label.setBorder(BorderFactory.createLineBorder(JBColor.GRAY));

        GridBagLayout gridBagLayout = new GridBagLayout();
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = JBUI.insets(5, 5, 5, 5);
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridx = 0;
        gridBagLayout.setConstraints(prettier,gridBagConstraints);
        gridBagConstraints.gridx++;
        gridBagLayout.setConstraints(tableUpperCase,gridBagConstraints);
        gridBagConstraints.gridx++;
        gridBagLayout.setConstraints(label, gridBagConstraints);
        gridBagConstraints.gridx++;
        gridBagLayout.setConstraints(lowerCaseRadio, gridBagConstraints);
        gridBagConstraints.gridx++;
        gridBagLayout.setConstraints(upperCaseRadio, gridBagConstraints);

        headerPanel.setLayout(gridBagLayout);
        headerPanel.setPreferredSize(new Dimension(200, 40));
//        headerPanel.setBorder(BorderFactory.createLineBorder(JBColor.GRAY));
        return headerPanel;
    }

    private JScrollPane buildMainPane(List<DialectSqlDTO> infoList) {
        DialectRowRender rowRender = new DialectRowRender();
        VerticalFlowLayout centerLayout = new VerticalFlowLayout();
        JPanel dialectsPanel = new JPanel(centerLayout);
        for (DialectSqlDTO value : infoList) {
            DialectRowRender.DialectRow component = rowRender.renderRow(value);
            dialectsPanel.add(component);
            rows.add(component);
        }
        JBScrollPane jbScrollPane = new JBScrollPane(dialectsPanel);
        return jbScrollPane;
    }
}
