package derek.tool.sql.settings;

import com.intellij.icons.AllIcons;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

/**
 * @author Derek
 * @date 2022/11/1
 */
public class CustomDialectRowRender implements ListCellRenderer<DbInfoDTO> {

    @Override
    public Component getListCellRendererComponent(JList<? extends DbInfoDTO> list, DbInfoDTO value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        CustomDialectRow row = new CustomDialectRow();
        row.setComponentOrientation(list.getComponentOrientation());
        row.setBorder(JBUI.Borders.empty(1));
        Color bg, fg;
        bg = isSelected ? list.getSelectionBackground() : list.getBackground();
        fg = isSelected ? list.getSelectionForeground() : list.getForeground();
        row.setBackground(bg);
        row.setForeground(fg);
        row.setFont(list.getFont());
        row.setOpaque(isSelected);
        row.index = index;
        value.writeValueTo(row.checkBox, row.versionField);
        return row;
    }
    public CustomDialectRow renderRow(CustomDialectDTO value){
        CustomDialectRow row = new CustomDialectRow();
        row.dto = value;
        DbInfoDTO dbInfo = value.getDbInfo();
        dbInfo.writeValueTo(row.checkBox, row.versionField);
        return row;
    }

    static class CustomDialectRow extends JPanel {
        private static final long serialVersionUID = -5850599494203708908L;
        CustomDialectDTO dto;
        int index;
        JCheckBox checkBox;

        JLabel versionLab;
        JLabel versionField;

        JButton deleteBt;

        public CustomDialectRow() {
            JPanel jPanel = new JPanel();
            GridBagLayout gridBagLayout = new GridBagLayout();

            jPanel.setLayout(gridBagLayout);
            //实例化这个对象用来对组件进行管理
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = JBUI.insets(5, 5, 5, 5);

            checkBox = new JBCheckBox();
            checkBox.setPreferredSize(new Dimension(300,30));
            gridBagLayout.setConstraints(checkBox, gridBagConstraints);

            versionLab = new JLabel("version:");
            versionLab.setAlignmentX(RIGHT_ALIGNMENT);
            versionField = new JLabel();
            versionField.setPreferredSize(new Dimension(70,30));
            versionField.setAlignmentY(CENTER_ALIGNMENT);

            gridBagConstraints.gridwidth = GridBagConstraints.BOTH;
            gridBagConstraints.fill = GridBagConstraints.NONE;
            gridBagLayout.setConstraints(versionLab, gridBagConstraints);
            gridBagConstraints.gridwidth = GridBagConstraints.BOTH;
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagLayout.setConstraints(versionField, gridBagConstraints);

            deleteBt = new JButton(AllIcons.Actions.Cancel);

            jPanel.add(checkBox);
            jPanel.add(versionLab);
            jPanel.add(versionField);
            setLayout(new BorderLayout());
            add(jPanel,BorderLayout.WEST);
            add(deleteBt,BorderLayout.EAST);

            deleteBt.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 0, deleteBt.getForeground()));
            if (deleteBt.getParent() != null) {
                Color background = deleteBt.getParent().getBackground();
                deleteBt.setBackground(background);
            }
            setAlignmentY(CENTER_ALIGNMENT);
        }
    }
}
