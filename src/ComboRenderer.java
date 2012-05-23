import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;


@SuppressWarnings("serial")
public class ComboRenderer extends JComboBox implements TableCellRenderer {
	
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean isFocus, int row, int col) {
		this.removeAllItems();
		String val = (String)value;
		this.setToolTipText(val);
		if (val.equals("Default")) val = "<html><b>" + val + "</b></html>";
		this.addItem(val);
        return this;
	}
}