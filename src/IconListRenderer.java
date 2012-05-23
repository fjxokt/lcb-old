import java.awt.Component;
import java.io.File;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;

@SuppressWarnings("serial")
public class IconListRenderer extends DefaultListCellRenderer {

	public Component getListCellRendererComponent(JList list, Object value, int index,
			boolean isSelected, boolean cellHasFocus) {

		// Get the renderer component from parent class
		JLabel label = (JLabel) super.getListCellRendererComponent(list,value, index, isSelected, cellHasFocus);

		if (!label.getText().equals("")) {
			// Get icon to use for the list item value
	 	   	Icon icon = new JFileChooser().getIcon(new File(label.getText()));
			label.setIcon(icon);
		}
		return label;
	}
}
