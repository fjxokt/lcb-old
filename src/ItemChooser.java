import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JCheckBox;

@SuppressWarnings("serial")
public class ItemChooser extends JDialog implements ActionListener, KeyListener, MouseListener {
	 private JTextField search = new JTextField("");
     private JList list;
     private DefaultListModel sampleModel;
     private TreeMap<String, String> map;
     private String selection;
     private ArrayList<String> filters;
     private JButton clear;
     private JButton close;
     private JPanel checks;
     
	 public ItemChooser(LoLResources lol) {
		 this.setSize(508, 520);
		 this.setLocationRelativeTo(null);
         this.setUndecorated(true);
         this.setModal(true);
         
		 JPanel pan = new JPanel();
		 
		 pan.add(new JLabel(LocaleString.string("Filter")));
		 
		 search.setPreferredSize(new Dimension(235, 25));
		 search.addKeyListener(this);
		 pan.add(search);
		 
		 clear = new JButton(LocaleString.string("Clear"));
		 clear.addActionListener(this);
		 pan.add(clear);
		 
		 close = new JButton(LocaleString.string("Close"));
		 close.addActionListener(this);
		 pan.add(close);
		 
		 filters = new ArrayList<String>();
		 GridLayout grid = new GridLayout(0,3);
		 checks = new JPanel(grid);
		 checks.setBorder(BorderFactory.createTitledBorder(LocaleString.string("Categories")));
		 checks.setPreferredSize(new Dimension(498, 132));
		 
		 // sort by category name
         Map<String,String> mapf = new TreeMap<String,String>();
         for (Entry<String,String> entry: lol.filters.entrySet()) {
        	 mapf.put(entry.getValue(), entry.getKey());
         }
         // and add to the model
         for (Entry<String,String> e : mapf.entrySet()) {
			 JCheckBox j = new JCheckBox(e.getKey());
			 j.setName(e.getValue());
			 j.setToolTipText(e.getKey());
			 checks.add(j);
				 
			 j.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					JCheckBox c = (JCheckBox)e.getSource();
					String tag = c.getName();
					if (c.isSelected()) filters.add(tag);
					else filters.remove(tag);
					filterList();
				}
			 });
		 }
		 pan.add(checks);
		 
		 sampleModel = new DefaultListModel();
         list = new JList(sampleModel);
         list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
         list.setVisibleRowCount(-1);
         list.addMouseListener(this);
         list.setCellRenderer(new DataIconListRenderer(lol, LoLResources.Type.ITEM));
         list.setBackground(Color.black);
         JScrollPane listPane = new JScrollPane(list);
         listPane.setPreferredSize(new Dimension(482, 335));
         pan.add(listPane);
         
         // sort by item name
         map = new TreeMap<String,String>();
         for (Entry<String,String> entry: lol.items.entrySet()) {
        	 map.put(entry.getValue(), entry.getKey());
         }
         // and add to the model
         for (Entry<String,String> entry: map.entrySet()) {
        	 sampleModel.addElement(entry.getValue());
         }
         
         this.setContentPane(pan);
	 }
	 
	 public void filterList() {
		 sampleModel.clear();
		 boolean skip;
		 // for each item
			for (Entry<String,String> entry: map.entrySet()) {
				skip = false;
				String[] t = entry.getKey().split("\\$");
				// if we have categories filters
				if (filters.size() > 0) {
					for (String tag : filters) {
						// if the cur item is not part of one of the cat, skip it
						if (!t[1].contains(tag)) {
							skip = true;
							break;
						}
					}
				}
				// skip this item
				if (skip) continue;
				
				// filter item using the filter textfield
				if (search.getText().isEmpty() || t[0].toLowerCase().contains(search.getText().toLowerCase()))
					sampleModel.addElement(entry.getValue());
			}
	 }
	 
	 public String getSelection() {
		 return selection;
	 }
	 
	 public boolean hasSelection() {
		 return !selection.equals("");
	 }

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == close) {
			selection = "";
			this.setVisible(false);
		}
		else if (e.getSource() == clear) {
			search.setText("");
			for (Component c : checks.getComponents())
				((JCheckBox)c).setSelected(false);
			filterList();
		}
	}

	public void keyPressed(KeyEvent arg0) {}
	public void keyTyped(KeyEvent arg0) {}
	public void keyReleased(KeyEvent e) {
		filterList();
	}

	public void mouseClicked(MouseEvent arg0) {
		if (list.getSelectedIndex() != -1) {
        	selection = (String)sampleModel.getElementAt(list.getSelectedIndex());
        	this.setVisible(false);
        }		
	}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mousePressed(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}
 }