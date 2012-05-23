import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

@SuppressWarnings("serial")
public class OptionsRendererEditor extends AbstractCellEditor implements TableCellRenderer, TableCellEditor
{
    private JPanel pan;
    private JButton add;
    private JButton remove;
    private LoLTable tableau;
    private int line;
    
    public OptionsRendererEditor() {
        super();
        pan = new JPanel(new GridLayout(2,1));
        add = new JButton(ResourcesManager.getInstance().getIcon("add.png", true));
        add.setToolTipText(LocaleString.string("Create new build"));
        add.setPreferredSize(new Dimension(18,18));
        add.setFocusPainted(false);
        add.setBorderPainted(false);
        remove = new JButton(ResourcesManager.getInstance().getIcon("remove.png", true));
        remove.setToolTipText(LocaleString.string("Delete this build"));
        remove.setFocusPainted(false);
        remove.setPreferredSize(new Dimension(18,18));
        remove.setBorderPainted(false);
        pan.add(add);
        pan.add(remove);
        
        add.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String champ = (String)tableau.getValueAt(line, 0);
				((LoLTableModel)tableau.getModel()).createBuild(champ, tableau.getBuildManager().getMode());
			}
        });
        
        remove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String champ = (String)tableau.getValueAt(line, 0);
				String build = (String)tableau.getValueAt(line, 1);
				((LoLTableModel)tableau.getModel()).removeBuild(champ, tableau.getBuildManager().getMode(), build);
			}
        });
        
    }
    
    public Component getTableCellComponent(JTable table, int row) {
    	tableau = (LoLTable)table;
        line = row;
		String build = (String)tableau.getValueAt(row, 1);
		remove.setEnabled(!build.equals("Default"));
    	return pan;
    }
   
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        return getTableCellComponent(table, row);
    }
    
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        return getTableCellComponent(table, row);
    }
    
    public Object getCellEditorValue() {
        return null;
    }
	
}