import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
class LoLTableModel extends AbstractTableModel {

	private String[] title;
	private LoLBuildManager manager;
	private ArrayList<String> champClassic;
	private ArrayList<String> champDominion;
	
	public LoLTableModel(String[] title, LoLBuildManager src){
		this.title = title;
		manager = src;
		champClassic = new ArrayList<String>();
		champDominion = new ArrayList<String>();
	}
	
	public void setMode(LoLResources.GameMode m) {
		manager.setMode(m);
		this.fireTableDataChanged();
	}
	
	public LoLBuildManager getManager() {
		return manager;
	}
	
	public LoLResources.GameMode getMode() {
		return manager.getMode();
	}
	
	public void updateData() {
		champClassic.clear();
		champDominion.clear();
		Set<String> keys = manager.getBuildsList(LoLResources.GameMode.Classic).keySet();
		for (String champ : keys)
			champClassic.add(champ);
		keys = manager.getBuildsList(LoLResources.GameMode.Dominion).keySet();
		for (String champ : keys)
			champDominion.add(champ);		
		fireTableDataChanged();
	}
	
	public void clear() {
		champClassic.clear();
		champDominion.clear();
		fireTableDataChanged();
	}
	
	public void createBuild(String champ, LoLResources.GameMode mode) {
		// first create a temporary build (0 is used to get the state of the build)
		LoLBuild b = manager.addBuild(champ, manager.getMode(), "_TMP_BUILD_0", null);
		
		// display an optionpane with an ItemRenderer
		ImageIcon icon = ResourcesManager.getInstance().getIcon(
				manager.getLoLItems().getImagePath(LoLResources.Type.CHAMPION, champ, false));
		ItemsRendererEditor ird = new ItemsRendererEditor(champ, manager, b);
		int res = JOptionPane.showConfirmDialog(null, ird.getPanel(),
				LocaleString.string("Create new build"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, icon);
				
		String buildName = ird.getBuildName();
		
		// if a build name has been entered
		if (res == JOptionPane.OK_OPTION && buildName != null && !buildName.isEmpty()) {
			// will this build be the active build ?
			boolean isActive = b.buildName.endsWith("1");
			// change the build name to the right one
			b.buildName = buildName;
			// make the build the active one
			if (isActive) manager.enableActiveBuild(champ, mode, buildName);
			
			// if global build
			if (ird.isGlobalBuild()) {
				b.items = ird.getItems();
			}
			else {
				// add the build to the global list
				if (ird.addToGlobalBuilds()) {
					LoLBuildManager.addGlobalBuild(b);
				}
			}

			// update the table
			ArrayList<String> curList = currentList();
			// inserer le new champ a la bonne position dans la currentList() puis lancer le fireXXX
			if (curList.indexOf(champ) == -1) {
				int i, val = -1;
				for (i=0; i<curList.size(); i++) {
					if (curList.get(i).compareTo(champ) > 0) {
						val = i; break;
					}
				}
				if (val == -1) val = i;
				curList.add(val, champ);
				fireTableRowsInserted(val, val);
			}
		}
		// otherwise remove this temporary created build
		else {
			manager.removeBuild(champ, manager.getMode(), b.buildName);
		}
	}
	
	public void removeBuild(String champ, LoLResources.GameMode mode, String buildName) {
		ImageIcon icon = ResourcesManager.getInstance().getIcon(
				manager.getLoLItems().getImagePath(LoLResources.Type.CHAMPION, champ, false));
		JLabel msg = new JLabel(LocaleString.string("delete build label",
				new String[]{champ, manager.getMode().toString(), buildName}));
		int retour = JOptionPane.showConfirmDialog(null, msg, LocaleString.string("Deleting build"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, icon);
		
		if (retour != JOptionPane.OK_OPTION)
			return;
		
		int n = manager.removeBuild(champ, mode, buildName);
		ArrayList<String> curList = currentList();
		int row = curList.indexOf(champ);
		if (n == 0) {
			curList.remove(champ);
			fireTableRowsDeleted(row, row);
		}
		else {
			fireTableRowsUpdated(row, row);
		}
	}
	
	public ArrayList<String> currentList() {
		return (getMode() == LoLResources.GameMode.Classic) ? champClassic : champDominion;
	}
	
	// number of columns
	public int getColumnCount() {
		return title.length;
	}
	
	// return number of lines
	public int getRowCount() {
		return currentList().size();
	}
	
	public boolean isCellEditable(int row, int col) {
		return (col > 0);
	}
	
	// return value at coordinates row col
	public Object getValueAt(int row, int col) {
		switch (col) {
		case 0:
			return currentList().get(row);
		case 1:
			List<LoLBuild> builds = manager.getBuilds(currentList().get(row), getMode());
			for (LoLBuild b : builds) {
				if (b.buildId.equals("ItemSet1"))
					return b.buildName;
			}
			return "Default";
		case 2:
			return new Object[]{currentList().get(row), getValueAt(row, 1)} ;
		case 3:
			return "";
		}
		return null;
	}
	
	// return column col name
	public String getColumnName(int col) {
		  return this.title[col];
	}
	
}