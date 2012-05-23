import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

@SuppressWarnings("serial")
public class ChampionRenderer extends JLabel implements TableCellRenderer {

	public Component getTableCellRendererComponent(JTable table, Object value,
													boolean isSelected, boolean isFocus,
													int row, int col)
	{
		String champ = (String)value;
		LoLResources src = ((LoLTable)table).getBuildManager().getLoLItems();
 	   	ImageIcon icon = ResourcesManager.getInstance().getIcon(
 	   			src.getImagePath(LoLResources.Type.CHAMPION, champ, true));
		// Set icon to display for value
 	   	if (icon != null)
 	   		setIcon(icon);
 	   	setText(champ);
		setToolTipText(champ);
		return this;
	}
}