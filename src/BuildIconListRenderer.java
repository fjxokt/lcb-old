import java.awt.Component;
import java.awt.FlowLayout;
import java.io.File;

import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class BuildIconListRenderer extends DefaultListCellRenderer {
	
	public class BuildLabel extends JPanel {
		private JLabel buildName;
		private JLabel[] icons;
		public BuildLabel(String name, String[] items) {
			setLayout(new FlowLayout(FlowLayout.LEFT));
			buildName = new JLabel(name);
			icons = new JLabel[6];
			for (int i=0; i<6; i++) {
				ImageIcon ic = ResourcesManager.getInstance().getIcon("images" + File.separator + items[i] + "_small.png");
				JLabel l = new JLabel(ic);
				l.setName(items[i]);
		 	   	l.setToolTipText(LoLBuildManager.createToolTip(items[i]));
				icons[i] = l;
				add(icons[i]);
			}
			add(Box.createHorizontalStrut(1));
			add(buildName);
		}
		public String getBuildName() {
			return buildName.getText();
		}
		public String[] getItems() {
			String[] r = new String[6];
			int i = 0;
			for (JLabel l : icons) {
				r[i++] = l.getName();
			}
			return r;
		}
	}
	
	public Component getListCellRendererComponent(JList list, Object value, int index,
			boolean isSelected, boolean cellHasFocus){
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}
		// return our BuildLabel
		return (value == null) ? this : (Component)value;
	}
}
