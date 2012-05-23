import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;


@SuppressWarnings("serial")
public class LoLTable extends JTable {
	
	class BuildClipboard {
		String[] build = new String[6];
		boolean isFull;
		public void copyBuild(String[] bu) { 
			build = bu.clone();
			isFull = true;
		}
		public void pasteBuild(String[] dest) { 
			for (int i=0, imax=build.length; i<imax; i++) dest[i] = build[i];
		}
	}
	
	private LoLTableModel model;
	private BuildClipboard buildCb = new BuildClipboard();
	
	public LoLTable(LoLTableModel mod) {
		super(mod);
		model = mod;
		setRowHeight(50);
        getColumnModel().getColumn(0).setPreferredWidth(130);         
        getColumnModel().getColumn(1).setPreferredWidth(160);         
        getColumnModel().getColumn(2).setPreferredWidth(310);         
        getColumnModel().getColumn(3).setPreferredWidth(20);
        getTableHeader().setReorderingAllowed(false);
        ((JLabel)getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
        
        // create all the different cells used by the table
        initCells();
        
        // allow the tooltip to show for the 'items list' cell
        addMouseMotionListener(new MouseMotionListener() {
			public void mouseDragged(MouseEvent arg0) {}
			public void mouseMoved(MouseEvent e) {
				java.awt.Point p = e.getPoint();
		        int row = rowAtPoint(p);
		        int col = columnAtPoint(p);
		        if (col > 1) {
		        	// changes cell being edited only if mouse cursor moved to another cell
		        	if (getEditingColumn() != col || getEditingRow() != row) {
		       			editCellAt(row, col);
		        	}
		        }
			}
        });
        
        addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {
				// this 'trick' is used because of the scrolling that screws the jtable bounds and the cursor pos
				Rectangle rec = ((JTable)e.getSource()).getBounds();
				Point cursor = new Point(e.getX() + rec.x, e.getY() + rec.y);
				rec.x = rec.y = 0;
				// if not in the jtable cancel editing mode
				if (!rec.contains(cursor)) {
					editingCanceled(null);
				}
			}
			public void mousePressed(MouseEvent e) {}
	        // popup menu
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() != MouseEvent.BUTTON3) return;
				int row = rowAtPoint(e.getPoint());
				String buildName = (String)getValueAt(row, 1);
				
				JMenuItem header = new JMenuItem("[" + buildName + " - " + model.getManager().getMode() + "]");
				header.setEnabled(false);
				
				JMenuItem rename = new JMenuItem(LocaleString.string("Rename build"));
				rename.setEnabled(!buildName.equals("Default"));
				rename.addActionListener(new PopupListener(row, 1));

				JMenuItem create = new JMenuItem(LocaleString.string("Create new build"));
				create.addActionListener(new PopupListener(row, 2));
				
				JMenuItem delete = new JMenuItem(LocaleString.string("Delete this build"));
				delete.setEnabled(!buildName.equals("Default"));
				delete.addActionListener(new PopupListener(row, 3));
				
				JMenu menuGlob = createGlobalBuildsMenu(row);
				
				JMenuItem copy = new JMenuItem(LocaleString.string("Copy build items"));
				copy.addActionListener(new PopupListener(row, 4));

				JMenuItem paste = new JMenuItem(LocaleString.string("Paste build items"));
				paste.setEnabled(buildCb.isFull && !buildName.equals("Default"));
				paste.addActionListener(new PopupListener(row, 5));

				//Ajout du menu contextuel
				JPopupMenu menu = new JPopupMenu();
				menu.add(header);
				menu.add(new JPopupMenu.Separator());
				menu.add(rename);
				menu.add(create);
				menu.add(delete);
				menu.add(new JPopupMenu.Separator());
				menu.add(menuGlob);
				menu.add(new JPopupMenu.Separator());
				menu.add(copy);
				menu.add(paste);
				menu.show(e.getComponent(), e.getX(), e.getY());
			}
        });
	}
	
	private JMenu createGlobalBuildsMenu(int row) {
		JMenu menuGlob = new JMenu(LocaleString.string("Global builds"));
		
		// add to global list menuitem
		JMenuItem addM = new JMenuItem(LocaleString.string("Add global list short"));
		addM.addActionListener(new PopupListener(row, 6));
		menuGlob.add(addM);
		if (!LoLBuildManager.getGlobalBuilds().isEmpty())
			menuGlob.add(new JSeparator());

		// create the menuitems
		for (String key : LoLBuildManager.getGlobalBuilds().keySet()) {
			JPanel pan = new JPanel(new GridLayout(1,6, 1, 0));
			String[] items = LoLBuildManager.getGlobalBuilds().get(key).items;
			for (String id : items) {
				ImageIcon ic = ResourcesManager.getInstance().getIcon("images" + File.separator + id + "_small.png");
				JLabel lab = new JLabel(ic);
				lab.setToolTipText(LoLBuildManager.createToolTip(id));
				pan.add(lab);
			}
			
			JMenu it = new JMenu(key);
			JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
			p2.add(pan);
			p2.add(new JLabel(key));
			it.setPreferredSize(new Dimension(it.getPreferredSize().width + 30 * 6, 40));
			it.add("pan", p2);

			JMenuItem it1 = new JMenuItem(LocaleString.string("Global build use"));
			it1.setActionCommand(key);
			it1.addActionListener(new PopupListener(row, 7));
			JMenuItem it2 = new JMenuItem(LocaleString.string("Global build remove"));
			it2.setActionCommand(key);
			it2.addActionListener(new PopupListener(row, 8));
			
			it.add(it1);
			it.add(it2);
			
			menuGlob.add(it);
		}
		return menuGlob;
	}
	
	class PopupListener implements ActionListener {
		int row;
		int id;
		public PopupListener(int row, int id) {
			this.row = row;
			this.id = id;
		}
		public void actionPerformed(ActionEvent e) {
			String champ = (String)getValueAt(row, 0);
			String buildName = (String)getValueAt(row, 1);
			LoLResources.GameMode mode = model.getManager().getMode();
			switch (id) {
			case 1:
				ImageIcon icon = ResourcesManager.getInstance().getIcon(
						model.getManager().getLoLItems().getImagePath(LoLResources.Type.CHAMPION, champ, false));
				String newName = (String)JOptionPane.showInputDialog(null,
						LocaleString.string("Rename $0$ build $1$ to", new String[]{champ, buildName}),
						LocaleString.string("Rename build"), JOptionPane.INFORMATION_MESSAGE,
						icon, null, null);
				if (newName == null || newName.isEmpty() || newName.equals(buildName)) return;
				model.getManager().renameBuild(champ, mode, buildName, newName);
				model.fireTableCellUpdated(row, 1);
				break;
			case 2:
				model.createBuild(champ, mode);
				break;
			case 3:
				model.removeBuild(champ, mode, buildName);
				break;
			case 4:
				buildCb.copyBuild(model.getManager().getBuild(champ, mode, buildName).items);
				model.fireTableCellUpdated(row, 2);
				break;
			case 5:
				buildCb.pasteBuild(model.getManager().getBuild(champ, mode, buildName).items);
				model.fireTableCellUpdated(row, 2);
				break;
			case 6:
				LoLBuildManager.addGlobalBuild(model.getManager().getBuild(champ, mode, buildName));
				break;
			case 7:
				String build = ((JMenuItem)e.getSource()).getActionCommand();
				model.getManager().addBuild(champ, mode, build, LoLBuildManager.getGlobalBuilds().get(build).items);
				model.getManager().enableActiveBuild(champ, mode, build);
				model.fireTableRowsUpdated(row, row);
				break;
			case 8:
				String build2 = ((JMenuItem)e.getSource()).getActionCommand();
				int retour = JOptionPane.showConfirmDialog(null, LocaleString.string("Deleting global build $0$", new String[]{build2}),
						LocaleString.string("Deleting build"),
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
				
				if (retour != JOptionPane.OK_OPTION)
					return;
				LoLBuildManager.getGlobalBuilds().remove(build2);
				LoLBuildManager.saveGlobalBuilds();
				break;
			}
			
		}
	}
	
	public LoLBuildManager getBuildManager() {
		return model.getManager();
	}
	
	public void initCells() {
		// for renderer
		ResourcesManager.getInstance().putCell(0, new ChampionRenderer());
		ResourcesManager.getInstance().putCell(1, new ComboRenderer());
		ResourcesManager.getInstance().putCell(2, new ItemsRendererEditor(model.getManager()));
		ResourcesManager.getInstance().putCell(3, new OptionsRendererEditor());
		// for editor (can't use same instance than renderer, don't know why)
		ResourcesManager.getInstance().putCell(4, new ComboEditor());
		ResourcesManager.getInstance().putCell(5, new ItemsRendererEditor(model.getManager()));
		ResourcesManager.getInstance().putCell(6, new OptionsRendererEditor());
	}
	
	public TableCellRenderer getCellRenderer(int row, int column) {
		switch (column) {
			case 0:
				return (ChampionRenderer)ResourcesManager.getInstance().getCell(column);
			case 1:
				return (ComboRenderer)ResourcesManager.getInstance().getCell(column);
			case 2:
				return (ItemsRendererEditor)ResourcesManager.getInstance().getCell(column);
			case 3:
				return (OptionsRendererEditor)ResourcesManager.getInstance().getCell(column);
		}
		return super.getCellRenderer(row, column);
	}
	
	public TableCellEditor getCellEditor(int row, int column) {
		switch (column) {
			case 1:
				return (ComboEditor)ResourcesManager.getInstance().getCell(4);
			case 2:
				return (ItemsRendererEditor)ResourcesManager.getInstance().getCell(5);
			case 3:
				return (OptionsRendererEditor)ResourcesManager.getInstance().getCell(6);
		}
		return super.getCellEditor();
	}

}
