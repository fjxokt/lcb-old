import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

@SuppressWarnings("serial")
public class LoLSynchronizer extends JDialog implements ActionListener {

	private LoLWin parent;
	private IniFile config;
	private JComboBox boxA;
	private JComboBox boxB;
	private JButton stats;
	private JButton okBt;
	private JButton closeBt;
	private JRadioButton synchRb;
	private JRadioButton copyRb;
	private JRadioButton repRb;
	private JCheckBox classic;
	private JCheckBox dominion;
	private ButtonGroup group;
	
	private Map<String,IniFile> clientA;
	private Map<String,IniFile> clientB;
	
	public LoLSynchronizer(LoLWin win, IniFile conf) {
		parent = win;
		config = conf;
		//this.setSize(500, 300);
		this.setLocationRelativeTo(null);
		this.setTitle("Synchronize");
		
		JPanel pan = new JPanel();
		BoxLayout layout = new BoxLayout(pan, BoxLayout.Y_AXIS);
		pan.setLayout(layout);
			
		JLabel lsync = new JLabel("Synchronize builds between your different clients");
		//lsync.setPreferredSize(new Dimension(400, 30));
		//lsync.setAlignmentX(LEFT_ALIGNMENT);
		pan.add(lsync);

		JPanel pa = new JPanel();
		
		JLabel lca = new JLabel("Client A :");
		lca.setAlignmentX(LEFT_ALIGNMENT);
		pa.add(lca);
	    //pan.add(Box.createVerticalStrut(10));
		boxA = new JComboBox();
        boxA.setRenderer(new IconListRenderer());
        boxA.addActionListener(this);
        //boxA.setPreferredSize(new Dimension(450,20));
        pa.add(boxA);
        //boxA.setAlignmentX(LEFT_ALIGNMENT);
       // pa.setAlignmentX(LEFT_ALIGNMENT);
        pan.add(pa);
        JLabel lcb = new JLabel("Client B :");
        //lca.setAlignmentX(LEFT_ALIGNMENT);
		pan.add(lcb);
	    pan.add(Box.createVerticalStrut(10));
		boxB = new JComboBox();
        boxB.setRenderer(new IconListRenderer());
        boxB.addActionListener(this);
		pan.add(boxB);
        boxB.setAlignmentX(LEFT_ALIGNMENT);

		group = new ButtonGroup();
				
		synchRb = new JRadioButton("Synchronize builds from both clients");
		synchRb.setSelected(true);
		group.add(synchRb);
		pan.add(synchRb);
		
		copyRb = new JRadioButton("Copy builds from Client A to Client B");
		group.add(copyRb);
		pan.add(copyRb);
		
		repRb = new JRadioButton("Replace builds from Client B with builds from client A");
		group.add(repRb);
		pan.add(repRb);
		
		classic = new JCheckBox("Classic");
		classic.setSelected(true);
		pan.add(classic);
		
		dominion = new JCheckBox("Dominion");
		dominion.setSelected(true);
		pan.add(dominion);
		
		JPanel jp = new JPanel();
		
		stats = new JButton("Show stats");
		stats.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				printStats();
			}
		});
		jp.add(stats);
		
		okBt = new JButton("Synchronize");
		okBt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				synchronize();
			}			
		});
		jp.add(okBt);
			
		closeBt = new JButton("Close");
		closeBt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {	
				dispose();
				parent.refresh();
			}
		});
		jp.add(closeBt);
		pan.add(jp);
		
		// fill in the combo
		for (int i=0, imax=Integer.parseInt(config.getValue("general", "clients")); i<imax; i++) {
			String p = config.getValue("client"+i, "clientPath");
			boxA.addItem(p);
			boxB.addItem(p);
		}
		boxA.setSelectedIndex(-1);
		boxB.setSelectedIndex(-1);
		
		this.setContentPane(pan);
		this.pack();
        this.setModal(true);
		this.setVisible(true);
	}
	
	public void synchronize() {
		if (classic.isSelected()) {
			load(LoLResources.GameMode.Classic);
			
			ButtonModel sel = group.getSelection();
			if (sel == synchRb.getModel()) {
				synch();
			}
			else if (sel == copyRb.getModel()) {
				copy();
			}
			else if (sel == repRb.getModel()) {
				replace();
			}
			save(LoLResources.GameMode.Classic);
		}
				
		if (dominion.isSelected()) {
			load(LoLResources.GameMode.Dominion);
			ButtonModel sel = group.getSelection();
			if (sel == synchRb.getModel()) {
				synch();
			}
			else if (sel == copyRb.getModel()) {
				copy();
			}
			else if (sel == repRb.getModel()) {
				replace();
			}
			save(LoLResources.GameMode.Dominion);
		}
		
		// TODO: save the file, update program data and UI
		// display msgbox, and close the dialog ?
		
	}
	
	private void save(LoLResources.GameMode mode) {
		String fres = (mode == LoLResources.GameMode.Classic) ? "RecItemsCLASSIC.ini" : "RecItemsODIN.ini";
		String path = config.getValue("client" + boxA.getSelectedIndex(), "clientDataPath") +  File.separator + "Characters";
		for (String champ : clientA.keySet()) {
			clientA.get(champ).save(path + File.separator + champ + File.separator + fres);
		}
		path = config.getValue("client" + boxB.getSelectedIndex(), "clientDataPath") +  File.separator + "Characters";
		for (String champ : clientB.keySet()) {
			clientB.get(champ).save(path + File.separator + champ + File.separator + fres);
		}
	}
	
	private void load(LoLResources.GameMode mode) {
		String fres = (mode == LoLResources.GameMode.Classic) ? "RecItemsCLASSIC.ini" : "RecItemsODIN.ini";
		 
		clientA = new TreeMap<String,IniFile>();
		clientB = new TreeMap<String,IniFile>();
		 
		String b1 = config.getValue("client" + boxA.getSelectedIndex(), "clientDataPath");
		String b2 = config.getValue("client" + boxB.getSelectedIndex(), "clientDataPath");
	 
		File f1 = new File(b1 + File.separator + "Characters");
		File f2 = new File(b2 + File.separator + "Characters");
		
		File[] dirs = f1.listFiles();
		for (File f : dirs) {
			IniFile ini = new IniFile(new File(f + File.separator + fres));
			clientA.put(f.getName(), ini);
		}	
		dirs = f2.listFiles();
		for (File f : dirs) {
			IniFile ini = new IniFile(new File(f + File.separator + fres));
			clientB.put(f.getName(), ini);
		}
	}
	
	private void printStats() {
		load(LoLResources.GameMode.Classic);
		StringBuffer stra = new StringBuffer();
		StringBuffer strb = new StringBuffer();
		stra.append("Client A<br/><br/>");
		//client A
		for (String key : clientA.keySet()) {
			int v = clientA.get(key).size();
			if (v > 0) {
				String t = key + "   " + v + "<br/>";
				if (clientB.get(key) != null && clientB.get(key).size() > 0)
					t = "<b>" + t + "</b>";
				stra.append(t);
			}
		}
		
		strb.append("&nbsp;&nbsp;Client B<br/><br/>");
		for (String key : clientB.keySet()) {
			int v = clientB.get(key).size();
			if (v > 0) {
				String t = "&nbsp;&nbsp;" + key + "   " + v + "<br/>";
				if (clientA.get(key) != null && clientA.get(key).size() > 0)
					t = "<b>" + t + "</b>";
				strb.append(t);	
			}
		}
		
		JPanel pan = new JPanel(new GridLayout(1,2));
		JEditorPane la = new JEditorPane("text/html", stra.toString());
		la.setBackground(pan.getBackground());
		JEditorPane lb = new JEditorPane("text/html", strb.toString());
		lb.setBackground(pan.getBackground());
		pan.add(la);
		pan.add(lb);		
		JOptionPane.showMessageDialog(null, pan, "Builds Statistics", JOptionPane.INFORMATION_MESSAGE);
		
	}
	
	// synch both clients
	private void synch() {
		// first, Add all builds from B in A
		Iterator<Map.Entry<String,IniFile>> iter = clientA.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String,IniFile> mEntry = (Map.Entry<String,IniFile>)iter.next();
			String champ = mEntry.getKey();
			IniFile fA = mEntry.getValue();
			// TODO: what if clientB has champ that clientA doesnt ??
			// normalement tous les dossiers sont crées par LoLResources, a voir...
			IniFile fB = clientB.get(champ);
			for (int i=0, imax=fB.size(); i<imax; i++) {
				IniFile.IniSection sec = fB.getSection(i);
				// TODO: verifier pour les doublons de noms
				fA.addSection(sec);
			}
		}
		// then replace builds from B with A
		replace();
	}
	
	// copy items from A in B
	private void copy() {
		Iterator<Map.Entry<String,IniFile>> iter = clientA.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String,IniFile> mEntry = (Map.Entry<String,IniFile>)iter.next();
			String champ = mEntry.getKey();
			IniFile fA = mEntry.getValue();
			if (fA.size() > 0)
				clientB.put(champ, fA);
		}
	}
	
	// replace builds from B with builds from A
	private void replace() {
		clientB.clear();
		copy();
	}

	public void actionPerformed(ActionEvent e) {
		// two different clients have to be selected
		if (boxA.getSelectedItem() != null && boxB.getSelectedItem() != null
				&& !boxA.getSelectedItem().equals(boxB.getSelectedItem())) {
			okBt.setEnabled(true);
			stats.setEnabled(true);
		}
		else {
			okBt.setEnabled(false);
			stats.setEnabled(false);
		}
	}
	
}
