package derek.tool.sql.dialog;

import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

/**
 * @author Derek
 * @date 2022/8/1
 */
public class DialectRowRender implements ListCellRenderer<DialectSqlDTO> {

    @Override
    public Component getListCellRendererComponent(JList<? extends DialectSqlDTO> list, DialectSqlDTO value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        DialectRow row = new DialectRow();
        row.setComponentOrientation(list.getComponentOrientation());
        row.setBorder(JBUI.Borders.empty(1));
        Color bg, fg;
        bg = isSelected ? list.getSelectionBackground() : list.getBackground();
        fg = isSelected ? list.getSelectionForeground() : list.getForeground();
        row.setBackground(bg);
        row.setForeground(fg);
        row.setFont(list.getFont());
        row.setOpaque(isSelected);
        value.writeValueTo(row.checkBox, row.versionField, row.pathField);
        return row;
    }

    public DialectRow renderRow(DialectSqlDTO value){
        DialectRow row = new DialectRow();
        value.writeValueTo(row.checkBox, row.versionField, row.pathField);
        return row;
    }

    static class DialectRow extends JPanel {
        private static final long serialVersionUID = -8068557897824883675L;
        JCheckBox checkBox;

        JLabel versionLab;
        JTextField versionField;

        JLabel pathLab;
        JTextField pathField;

        public DialectRow() {
            GridBagLayout gridBagLayout = new GridBagLayout();

            setLayout(gridBagLayout);
            //实例化这个对象用来对组件进行管理
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = JBUI.insets(5, 5, 5, 5);

            checkBox = new JBCheckBox();
            checkBox.setPreferredSize(new Dimension(150,30));
            gridBagLayout.setConstraints(checkBox, gridBagConstraints);

            versionLab = new JLabel("version:");
            versionLab.setAlignmentX(RIGHT_ALIGNMENT);
            versionField = new JTextField();
            versionField.setPreferredSize(new Dimension(60,30));
            versionField.setAlignmentY(CENTER_ALIGNMENT);

            pathLab = new JLabel("path:");
            pathLab.setAlignmentX(RIGHT_ALIGNMENT);
            pathField = new JTextField();
            pathField.setAlignmentY(CENTER_ALIGNMENT);
            pathField.setPreferredSize(new Dimension(240,30));

            gridBagConstraints.gridwidth = GridBagConstraints.BOTH;
            gridBagConstraints.fill = GridBagConstraints.NONE;
            gridBagLayout.setConstraints(versionLab, gridBagConstraints);
            gridBagConstraints.gridwidth = GridBagConstraints.BOTH;
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagLayout.setConstraints(versionField, gridBagConstraints);

            gridBagConstraints.gridwidth = 1;
            gridBagConstraints.fill = GridBagConstraints.NONE;
            gridBagLayout.setConstraints(pathLab, gridBagConstraints);
            gridBagConstraints.weightx = 1;
            gridBagConstraints.gridwidth = GridBagConstraints.BOTH;
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagLayout.setConstraints(pathField, gridBagConstraints);

            add(checkBox);
            add(versionLab);
            add(versionField);
            add(pathLab);
            add(pathField);
            setAlignmentY(CENTER_ALIGNMENT);
//            Border border = BorderFactory.createLineBorder(JBColor.GRAY);
//            setBorder(border);

        }
    }
}
