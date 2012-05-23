import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;

@SuppressWarnings("serial")
public class DataIconListRenderer extends DefaultListCellRenderer {
	
	LoLResources ref;
	LoLResources.Type type;

	public DataIconListRenderer(LoLResources lol, LoLResources.Type t) {
		ref = lol;
		type = t;
	}

	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		// Get the renderer component from parent class
		JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

 	   	ImageIcon icon = ResourcesManager.getInstance().getIcon(ref.getImagePath(type, label.getText(), false));
		// Set icon to display for value
 	   	if (icon != null)
 	   		label.setIcon(icon);
		if (type == LoLResources.Type.ITEM) {
			String[] str = ref.items.get(label.getText()).split("\\$");
			label.setToolTipText("<html><div style=\"width: 200px\"><b>"+str[0]+"</b><br>"+str[2]+"</div></html>");
		}
		else {
			label.setToolTipText(label.getText());
		}
		label.setText("");
		return label;
	}
}
