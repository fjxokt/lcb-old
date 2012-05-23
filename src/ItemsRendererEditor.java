import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Set;

import javax.swing.AbstractCellEditor;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalComboBoxUI;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;


@SuppressWarnings("serial")
public class ItemsRendererEditor extends AbstractCellEditor implements TableCellRenderer, TableCellEditor{
	private JPanel pan = new JPanel();
	private DnDLabel[] items = new DnDLabel[6];
	private LoLBuildManager manager;
	private String mbuild;
	private String mchamp;
	private boolean isThumb = false;
	private LoLBuild lbuild;
	private JLabel lb;
	private ButtonGroup group;
	private JRadioButton rb1;
	private JRadioButton rb2;
	private JComboBox cbt;
	private JTextField buildName;
	private JCheckBox cb2;
	
	// called by the table
	public ItemsRendererEditor(LoLBuildManager m) {
		manager = m;
		isThumb = true;
		for (int i=0; i<6; i++) {
			DnDLabel l = new DnDLabel(items);
			items[i] = l;
			l.setName(""+i);
			l.addMouseListener(new LoLItemListener(i));
			pan.add(l);
		}
	}
	
	public JPanel getPanel() {
		return pan;
	}
	
	// called when creating a new build
	public ItemsRendererEditor(String champ, LoLBuildManager manag, LoLBuild build) {
		// value of the checkbox
		lbuild = build;
		mbuild = build.buildName;
		mchamp = champ;
		manager = manag;
		
		BoxLayout layout = new BoxLayout(pan, BoxLayout.Y_AXIS);
	    pan.setLayout(layout);
	    
	    JLabel l1 = new JLabel("<html><b>"+LocaleString.string("Creating new build $0$ mode for $1$", 
	    		new String[]{manag.getMode().toString(), champ})+"</b></html>");
	    pan.add(l1);
	    
	    pan.add(Box.createVerticalStrut(10));
	    
	    group = new ButtonGroup();
	    
	    rb1 = new JRadioButton(LocaleString.string("Click on pictures to choose items"));
	    rb1.setAlignmentX(Component.LEFT_ALIGNMENT);
		group.add(rb1);
		pan.add(rb1);
		rb1.addActionListener(new RadioListener(this));
		
		JPanel list = new JPanel();
		for (int i=0; i<6; i++) {
			DnDLabel l = new DnDLabel(items);
			l.setBuild(build);
			items[i] = l;
			l.setName(""+i);
			ImageIcon icon = ResourcesManager.getInstance().getIcon(
					manager.getLoLItems().getImagePath(LoLResources.Type.ITEM, build.items[i], false));
			items[i].setIcon(icon);
	 	   	items[i].setToolTipText(LoLBuildManager.createToolTip(build.items[i]));
			l.addMouseListener(new LoLItemListener(i));
			list.add(l);
		}
		list.setAlignmentX(Component.LEFT_ALIGNMENT);
		pan.add(list);
		
		lb = new JLabel(LocaleString.string("Build name"));
		lb.setAlignmentX(Component.LEFT_ALIGNMENT);
		pan.add(lb);
		
		buildName = new JTextField();
		pan.add(buildName);

	    cb2 = new JCheckBox(LocaleString.string("Add global list"));
	    pan.add(cb2);
		
		pan.add(Box.createVerticalStrut(10));
		
		rb2 = new JRadioButton(LocaleString.string("Use global builds"));
		rb2.setAlignmentX(Component.LEFT_ALIGNMENT);
		group.add(rb2);
		pan.add(rb2);
		rb2.addActionListener(new RadioListener(this));
		
		cbt = new JComboBox();
        UIManager.put("ComboBox.selectionBackground", UIManager.get("ComboBox.background"));
        UIManager.put("ComboBox.selectionForeground", UIManager.get("ComboBox.foreground"));
        cbt.setUI(new MetalComboBoxUI());
        BuildIconListRenderer br = new BuildIconListRenderer();
        cbt.setPreferredSize(new Dimension(200, 45));
        cbt.setRenderer(br);
        cbt.setAlignmentX(Component.LEFT_ALIGNMENT);
        pan.add(cbt);
        
        // fill the combo
        Set<String> keys = LoLBuildManager.getGlobalBuilds().keySet();
        for (String key : keys) {
        	LoLBuild bui = LoLBuildManager.getGlobalBuilds().get(key);
            cbt.addItem(br.new BuildLabel(key, bui.items));
        }
        
        if (cbt.getModel().getSize() == 0)
        	rb2.setEnabled(false);
        
        
		pan.add(Box.createVerticalStrut(10));
	    
	    JCheckBox cb = new JCheckBox(LocaleString.string("Make this build the active build"));
	    pan.add(cb);
	    
	    // change the ending of the build according to the checkbox value
	    cb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JCheckBox b = (JCheckBox)e.getSource();
				String bu = lbuild.buildName;
				String bool = (b.isSelected()) ? "1" : "0";
				lbuild.buildName = mbuild = bu.substring(0, bu.length()-1) + bool;
			}
	    });
	    
	    pan.add(Box.createVerticalStrut(10));
		
		// disable some stuff
		changeStat(true);
	}
	
	public class RadioListener implements ActionListener {
		private ItemsRendererEditor base;
		public RadioListener(ItemsRendererEditor r) {
			base = r;
		}
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == base.rb1) {
				base.changeStat(true);
			}
			else
				base.changeStat(false);
		}
	}
	
	public boolean addToGlobalBuilds() {
		return cb2.isSelected();
	}
	
	public boolean isGlobalBuild() {
		return rb2.isSelected();
	}
	
	public void changeStat(boolean newBuild) {
		rb1.setSelected(newBuild);
		rb2.setSelected(!newBuild);
		cbt.setEnabled(!newBuild);
		cb2.setEnabled(newBuild);
		buildName.setEnabled(newBuild);
	}
	
	public String[] getItems() {
		BuildIconListRenderer.BuildLabel l = ((BuildIconListRenderer.BuildLabel)cbt.getSelectedItem());
		return l.getItems();
	}
	
	public String getBuildName() {
		if (rb1.isSelected())
			return buildName.getText();
		else {
			BuildIconListRenderer.BuildLabel l = ((BuildIconListRenderer.BuildLabel)cbt.getSelectedItem());
			return l.getBuildName();
		}
	}
	
	public void changeItem(int id, String newItem) {
		manager.getBuild(mchamp, manager.getMode(), mbuild).items[id] = newItem;				
		ImageIcon icon = ResourcesManager.getInstance().getIcon(
				manager.getLoLItems().getImagePath(LoLResources.Type.ITEM, newItem, isThumb));
 	   	if (icon != null)
 	   		items[id].setIcon(icon);
 	   	items[id].setToolTipText(LoLBuildManager.createToolTip(newItem));
	}
	
	public class LoLItemListener implements MouseListener {
		int id;
		public LoLItemListener(int i) {
			id = i;
		}
		public void mouseClicked(MouseEvent e) {
			if (e.getButton() != MouseEvent.BUTTON1) return;
			if (mbuild.equals("Default")) return;
			ItemChooser ic = manager.getItemChooser();
			ic.setVisible(true);
			if (ic.hasSelection()) {
				changeItem(id, ic.getSelection());
			}
		}
		public void mouseReleased(MouseEvent arg0) {}
		public void mouseEntered(MouseEvent e) {
			if (((JLabel)e.getSource()).isEnabled())
				pan.setCursor(new Cursor(Cursor.HAND_CURSOR));
		}
		public void mouseExited(MouseEvent arg0) {
			 pan.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
		public void mousePressed(MouseEvent arg0) {}
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
													boolean isSelected, boolean isFocus,
													int row, int col) 
	{
		Object[] vals = (Object[])value;
		mchamp = (String)vals[0];
		mbuild = (String)vals[1];

		LoLBuild build = manager.getBuild(mchamp, manager.getMode(), mbuild);
		String[] recItems = build.items;
				
		for (int i=0; i<6; i++) {
			items[i].setBuild(build);
			ImageIcon icon = ResourcesManager.getInstance().getIcon(
					manager.getLoLItems().getImagePath(LoLResources.Type.ITEM, recItems[i], true));
			if (icon != null)
				items[i].setIcon(icon);
	 	   	items[i].setToolTipText(LoLBuildManager.createToolTip(recItems[i]));
	 	   	items[i].setEnabled(!mbuild.equals("Default"));
		}		
		return pan;
	}
	
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		return getTableCellRendererComponent(table, value, isSelected, true, row, column);
     }

	public Object getCellEditorValue() {
		return null;
	}
}
