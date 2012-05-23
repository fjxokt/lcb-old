import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

 
@SuppressWarnings("serial")
public class LoLWin extends JFrame
					implements ActionListener, ListSelectionListener, KeyListener, FocusListener, MouseListener {
	
	private LoLWin self;
	private JPanel pan = new JPanel();
	private JComboBox combo;
	private JLabel label;
	private JButton addClient;
	private JButton save;
	private JButton delete;
	private JButton exit;
	private String filterText;
	private JTextField search;
	private JList list;
	private DefaultListModel sampleModel;
	private JComboBox modeSel;
	private LoLTableModel tableModel;
	private JButton createBuild;
	private LoLTable tableau;
	private JMenu clientMenu;
	private JMenuItem synch;
	private ButtonGroup group;
	LoLResources LoL;
	LoLBuildManager buildManager;
	ArrayList<String>champList;
	IniFile config;
     
     final static String appName = "LoL Custom Builds";
     final static String version = "1.0.0";
     final static String appFullName = appName + " (" + version + ")";
     final static String configFile = "lolbuilds.ini";
     final static String dataFile = "data.ini";
     final static String buildsFile = "builds.ini";
     
     // Class Loading
     public static class Loading extends JFrame {
    	 private JProgressBar barre = new JProgressBar();
    	 private JLabel label = new JLabel(LocaleString.string("Loading"));
    	 public Loading() {
    		 this.setSize(450, 90);
    		 this.setLocationRelativeTo(null);
             this.setUndecorated(true);
    		 JPanel pan = new JPanel();
    		 barre.setIndeterminate(true);
             barre.setPreferredSize(new Dimension(400, 40));
             pan.add(barre);
    		 pan.add(label);
    		 this.setContentPane(pan);
             this.setVisible(true);
    	 }
    	 public void setLabel(String str) {
    		 label.setText(str);
    	 }
     }
    
     @SuppressWarnings("unchecked")
     public LoLWin() {
    	 self = this;
    	 // load config data
         File conf = new File(configFile);
         
         // if no file create and put default settings
         if (!conf.exists()) {
			createDefaultConfigFile(conf);
         }
         config = new IniFile(conf);
         
         // set language
    	 LocaleString.getInstance().initLocale(config.getValue("general", "language"));
         
         // loading frame
         Loading win = new Loading();
         
         // initialization of the data
         LoL = new LoLResources(win);
         LoL.init();
         buildManager = new LoLBuildManager(LoL);
         champList = LoL.champList;
         champList = (ArrayList<String>)LoL.champList.clone();
         Collections.sort(champList);
         
         // building window
         this.setTitle(appFullName);
         this.setSize(640, 690);
         this.setLocationRelativeTo(null);
         this.setResizable(false);
         this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         this.setJMenuBar(createMenus());
         
         // adding components
         label = new JLabel(LocaleString.string("List of your LoL clients"));
         pan.add(label);
         
         combo = new JComboBox();
         combo.setPreferredSize(new Dimension(550, 30));
         combo.setRenderer(new IconListRenderer());
         combo.setEnabled(false);
         pan.add(combo);
         
         delete = new JButton(ResourcesManager.getInstance().getIcon("remove.png", true));
         delete.addActionListener(this);
         delete.setToolTipText(LocaleString.string("Delete client"));
         delete.setEnabled(false);
         pan.add(delete);
         
         //Ajout du bouton a notre contentPane
         addClient = new JButton(LocaleString.string("Add new LoL Client"),
        		 ResourcesManager.getInstance().getIcon("add.png", true));
         addClient.setPreferredSize(new Dimension(addClient.getPreferredSize().width, 35));
         pan.add(addClient);
         addClient.addActionListener(this);
         
         // save button
         save = new JButton(LocaleString.string("Save"), ResourcesManager.getInstance().getIcon("save.png", true));
         save.addActionListener(this);
         save.setPreferredSize(new Dimension(save.getPreferredSize().width, 35));
         save.setEnabled(false);
         pan.add(save);
         
         //Ajout du bouton a notre contentPane
         exit =	new JButton(LocaleString.string("Exit $0$", new String[]{appName}),
        		 ResourcesManager.getInstance().getIcon("exit.png", true));
         exit.setPreferredSize(new Dimension(exit.getPreferredSize().width, 35));
         pan.add(exit);
         exit.addActionListener(this);
         
         // champions list
         sampleModel = new DefaultListModel();
         list = new JList(sampleModel);
         list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
         list.setVisibleRowCount(-1);
         list.addListSelectionListener(this);
         list.addMouseListener(this);
         list.setCellRenderer(new DataIconListRenderer(LoL, LoLResources.Type.CHAMPION));
         list.setBackground(Color.black);
         JScrollPane listPane2 = new JScrollPane(list);
         listPane2.setPreferredSize(new Dimension(580, 189));
         pan.add(listPane2);
         
         // search textfield
         filterText = LocaleString.string("Filter champions list");
         search = new JTextField(filterText, 20);
         search.addKeyListener(this);
         search.addFocusListener(this);
         search.addMouseListener(this);
         search.setEnabled(false);
         
         Box box = Box.createHorizontalBox();
         box.setPreferredSize(new Dimension(580, 30));
         box.add(search);
         box.add(Box.createHorizontalStrut(15));

         JLabel modeLb = new JLabel(LocaleString.string("Game Mode"));
         modeSel = new JComboBox(LoLResources.GameMode.values());
         modeSel.addActionListener(this);
         box.add(modeLb);
         box.add(modeSel);
         
         box.add(Box.createHorizontalStrut(15));

         createBuild = new JButton(LocaleString.string("Create new build"));
         createBuild.setEnabled(false);
         createBuild.addActionListener(this);
         box.add(createBuild);    
         pan.add(box);

         // tableau
         String  title[] = {LocaleString.string("Champion"),
        		 			LocaleString.string("Active Build"),
        		 			LocaleString.string("Build Items"), ""};
         tableModel = new LoLTableModel(title, buildManager);
         tableau = new LoLTable(tableModel);        
         JScrollPane listPane3 = new JScrollPane(tableau);
         listPane3.setPreferredSize(new Dimension(640, 320));
         pan.add(listPane3);
         
         // fill the clients combo
         int imax = Integer.parseInt(config.getValue("general", "clients"));
         String activeClient = config.getValue("general", "activeClient");
         for (int i=0; i<imax; i++) {
        	 combo.addItem(config.getValue("client"+i, "clientPath"));
         }
         // enable delete button if clients, synch if at least 2 clients
         if (imax > 0) {
        	 delete.setEnabled(true);
        	 save.setEnabled(true);
        	 if (imax > 1)
        		 synch.setEnabled(true);
         }
         
         // add the combo listener and select active client
         combo.addActionListener(this);
         if (activeClient != null) {
        	 combo.setSelectedItem(config.getValue(activeClient, "clientPath"));
         }
      
         // fill the champions list
         for (int i=0; i<LoL.champList.size(); i++)
			sampleModel.addElement(champList.get(i));
         
         this.setContentPane(pan);
         
         win.dispose();
         this.setVisible(true);
         
         // if auto update mode on, check for update
         if (config.getValue("general", "autoupdate").equals("1"))
        	 LoLUpdater.checkUpdate(true);
     }
     
     public JMenuBar createMenus() {
    	 JMenuBar menuBar;
    	 JMenu options, language;
    	 JMenuItem reset, update;
    	 JCheckBoxMenuItem autoUpdate;

    	 //Create the menu bar.
    	 menuBar = new JMenuBar();
    	 
    	 clientMenu = new JMenu(LocaleString.string("Client"));
    	 
    	 JMenuItem importM = new JMenuItem(LocaleString.string("Import") + "...");
    	 importM.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {	
				// select the file to import
				JFileChooser chooser = new JFileChooser(new File("."));
				// filter for our special file format
				chooser.setAcceptAllFileFilterUsed(false);
				chooser.setFileFilter(new FileFilter() {
					public boolean accept(File f) {
						return (f.isDirectory() || f.getName().endsWith(".lcb"));
					}
					public String getDescription() {
						return appName + " (*.lcb)";
					}
				});
			    // display the dialog
			    int returnVal = chooser.showOpenDialog(null);
			    if (returnVal == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile().exists()) {
			    	
			    	int res = JOptionPane.showConfirmDialog(null, LocaleString.string("Import warning"),
							 LocaleString.string("Import"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		    		 
			    	if (res == JOptionPane.OK_OPTION) {
			    		buildManager.importBuilds(chooser.getSelectedFile());
			    		tableModel.updateData();
			    	}
			    }
			}
    	 });
    	 clientMenu.add(importM);
    	 
    	 JMenuItem exportM = new JMenuItem(LocaleString.string("Export") + "...");
    	 exportM.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// select the file to export
				JFileChooser chooser = new JFileChooser(new File("."));
				// filter for our special file format
				chooser.setAcceptAllFileFilterUsed(false);
				chooser.setFileFilter(new FileFilter() {
					public boolean accept(File f) {
						return (f.isDirectory() || f.getName().endsWith(".lcb"));
					}
					public String getDescription() {
						return appName + " (*.lcb)";
					}
				});
			    // display the dialog
			    int returnVal = chooser.showSaveDialog(null);
			    if (returnVal == JOptionPane.OK_OPTION) {
			    	String file = chooser.getSelectedFile().getAbsolutePath();
			    	if (!file.endsWith(".lcb")) file += ".lcb";
			    	if (new File(file).exists()) {
			    		int res = JOptionPane.showConfirmDialog(null, LocaleString.string("File exists desc $0$", file),
			    				LocaleString.string("File exists"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				    	if (res != JOptionPane.OK_OPTION)
				    		return;
			    	}
			    	buildManager.exportBuilds(file);
		    	}
			}
    	 });
    	 clientMenu.add(exportM);
    	 
    	 clientMenu.add(new JSeparator());
    	 
    	 JMenuItem clientClr = new JMenuItem(LocaleString.string("Delete all"));
    	 clientClr.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String cname = new File((String) combo.getSelectedItem()).getName();
				int res = JOptionPane.showConfirmDialog(null, LocaleString.string("Delete all confirm $0$", cname),
						LocaleString.string("Delete all"), JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
	    		 
		    	if (res == JOptionPane.OK_OPTION) {
					buildManager.clear();
		    		tableModel.updateData();
		    	}
			}
    	 });
    	 clientMenu.add(clientClr);
    	 
    	 clientMenu.setEnabled(false);
    	 menuBar.add(clientMenu);
    	 
    	 //Build the first menu.
    	 options = new JMenu(LocaleString.string("Options"));
    	
    	 synch = new JMenuItem(LocaleString.string("Synch clients") + "...", KeyEvent.VK_S);
    	 synch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {	
				new LoLSynchronizer(self, config);
			}
    	 });
    	 synch.setEnabled(false);
    	 options.add(synch);

    	 reset = new JMenuItem(LocaleString.string("Reset data"), KeyEvent.VK_R);
    	 reset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {	
				int res = JOptionPane.showConfirmDialog(null, LocaleString.string("Reset data confirm"),
						LocaleString.string("Reset data"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
	    		 if (res == JOptionPane.OK_OPTION) {
	    			 File f = new File(LoLWin.dataFile);
	    			 if (f.exists()) f.deleteOnExit();
	    			 JOptionPane.showMessageDialog(null, LocaleString.string("Reset restart"),
							 LocaleString.string("Reset data"), JOptionPane.INFORMATION_MESSAGE);
	    		 }
			} 
    	 });
    	 options.add(reset);
    	 options.addSeparator();
    	 
    	 // language menu
    	 language = createLanguageMenu();
    	 options.add(language);
    	 
    	 update = new JMenuItem(LocaleString.string("Check update") + "...", KeyEvent.VK_W);
    	 update.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {	
				LoLUpdater.checkUpdate(false);
			} 
    	 });
    	 options.add(update);
    	 
    	 autoUpdate = new JCheckBoxMenuItem(LocaleString.string("Autoupdate"));
    	 autoUpdate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String autoupdate = (((JCheckBoxMenuItem)e.getSource()).isSelected()) ? "1" : "0";
				config.addValue("general", "autoupdate", autoupdate);
				config.save(new File(configFile));
			}
    	 });
    	 autoUpdate.setSelected(config.getValue("general", "autoupdate").equals("1"));
    	 options.add(autoUpdate);
    	 
    	 menuBar.add(options);
    	
    	 // help menu
    	 JMenu help = new JMenu(LocaleString.string("Help"));
    	 
    	 JMenuItem about = new JMenuItem(LocaleString.string("About"));
    	 about.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "<html><b>" + appFullName + "</b>\nBy fjxokt\nfjxokt@gmail.com",
						 LocaleString.string("About"), JOptionPane.INFORMATION_MESSAGE);
			}
    	 });
    	 help.add(about);
    	 
    	 menuBar.add(help);
    	    	 
    	 return menuBar;
     }
     
     public JMenu createLanguageMenu() {
    	 // list of languages
    	 String[][] langs = new String[][]{{"Français","fr_FR"},{"English","en_US"}};
    	 // create the menu
    	 JMenu language = new JMenu(LocaleString.string("Language"));
    	 // get current language
    	 String curLang = config.getValue("general", "language");
    	 // button group
    	 group = new ButtonGroup();
    	 // create the different languages menu items
    	 JMenuItem len;
    	 for (String[] lang : langs) {
    		 len = new JRadioButtonMenuItem(lang[0]);
    		 len.setActionCommand(lang[1]);
    		 // listener
    		 len.addActionListener(new ActionListener() {
    			 public void actionPerformed(ActionEvent e) {
    				 JRadioButtonMenuItem m = (JRadioButtonMenuItem)e.getSource();
    				 // if selected language is already active, nothing to do
    				 if (m.getActionCommand().equals(LocaleString.locale)) return;
    				 // select the menu item
    				 group.setSelected(m.getModel(), true);
    				 // change the current locale in LocaleString
    				 LocaleString.locale = m.getActionCommand();
    				 // save the config file with the changes
    				 config.addValue("general", "language", m.getActionCommand());
    				 config.save(configFile);
    				 JOptionPane.showMessageDialog(null, LocaleString.string("Change language text"),
    						 LocaleString.string("Change language title"), JOptionPane.INFORMATION_MESSAGE);
    			 }
    		 });
    		 language.add(len);
    		 group.add(len);
    		// if active language, just check it
    		 if (curLang.equals(lang[1])) {
    			 len.setSelected(true);
    		 }
    	 }
    	 return language;
     }
     
     public void createDefaultConfigFile(File conf) {
    	 try {
			conf.createNewFile();
	        IniFile config = new IniFile(conf);
	        config.createSection("general");
	        config.addValue("general", "version", version);
	        config.addValue("general", "clients", "0");
	        config.addValue("general", "autoupdate", "1");
	        config.addValue("general", "language", "en_US");
	        config.save(conf); 
		} catch (IOException e) {
			e.printStackTrace();
		}
     }
     
     public void createBuild() {
    	 String champ = (String)list.getSelectedValue();
    	 tableModel.createBuild(champ, buildManager.getMode());
    	 tableau.scrollRectToVisible(new Rectangle(0,
				tableau.getRowHeight()*tableModel.currentList().indexOf(champ),
				400, tableau.getRowHeight()));
     }
     
     public void refresh() {
    	 combo.setSelectedIndex(combo.getSelectedIndex());
     }
     
     public void resetUI() {
    	 delete.setEnabled(false);
    	 save.setEnabled(false);
    	 combo.setEnabled(false);
    	 createBuild.setEnabled(false);
    	 clientMenu.setEnabled(false);
    	 synch.setEnabled(false);
    	 tableModel.clear();
     }
     
     public void deleteClient(int index) {
    	 // check
    	 if (index == -1) return;
    	// remove string from combo
		combo.removeItemAt(index);
		// update config file
		// decrease the number of clients
		int nbClients = Integer.parseInt(config.getValue("general", "clients"));
		config.addValue("general", "clients", Integer.toString(nbClients-1));
		// remove the client
		config.removeSection("client" + index);
		// rename the clients above the client deleted
		for (int i=index+1; i<nbClients; i++) {
			config.renameSection("client" + i, "client" + (i-1));
		}
		// if no more client
		if (nbClients-1 == 0) {
			config.removeValue("general", "activeClient");
			resetUI();
		}
		else if (nbClients-1 == 1) {
			synch.setEnabled(false);
		}
		// save data
		config.save(configFile);
     }
     
     //////////////////////
     // EVENTS HANDLING
     //////////////////////

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == addClient) {
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle(LocaleString.string("Select League of legends folder"));
		    chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		    int returnVal = chooser.showOpenDialog(null);
		    if (returnVal == JFileChooser.APPROVE_OPTION) {
		    	File dir = chooser.getSelectedFile();
			       
		    	// look is the client is correct
		    	boolean isClient = LoL.isClient(dir);
		    	
		    	if (isClient) {
		    		// save this new client
		    		int nbClients = Integer.parseInt(config.getValue("general", "clients")) + 1;
		    		config.addValue("general", "clients", Integer.toString(nbClients));
		    		String section = "client" + (nbClients-1);
		    		config.addValue("general", "activeClient", section);
		    		config.createSection(section);
		    		config.addValue(section, "clientPath", dir.getAbsolutePath());
		    		config.addValue(section, "clientDataPath", LoL.found.getAbsolutePath());
		    		config.save(configFile);
		    		
		    		combo.addItem(dir.getAbsolutePath());
		    		combo.setSelectedIndex(combo.getItemCount()-1);
		    		delete.setEnabled(true);
		    	}
		    	else {
		    		Log.getInst().warning("No client found in: " + dir);
		    	}
		    }
		}
		else if (e.getSource() == delete) {
			int index = combo.getSelectedIndex();
			if (index != -1) {
				// confirmation box
				String cname = new File((String) combo.getSelectedItem()).getName();
				int res = JOptionPane.showConfirmDialog(null, LocaleString.string("Delete client $0$", cname),
						LocaleString.string("Delete client"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				// if ok, delete client
	    		 if (res == JOptionPane.OK_OPTION) {
		    		 deleteClient(index);
	    		 }
			}
		}
		else if (e.getSource() == save) {
			tableModel.getManager().saveBuilds();
			JOptionPane.showMessageDialog(null, LocaleString.string("Save confirmation"),
					 LocaleString.string("Save"), JOptionPane.INFORMATION_MESSAGE);
		}
		else if (e.getSource() == exit) {
			int res = JOptionPane.showConfirmDialog(null, LocaleString.string("Exit $0$ text", new String[]{appName}),
					LocaleString.string("Exit"), JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
   		 	// if ok, exit
   		 	if (res == JOptionPane.OK_OPTION)
   		 		System.exit(DISPOSE_ON_CLOSE);
		}
		else if (e.getSource() == createBuild) {
			createBuild();
		}
		else if (e.getSource() == combo) {
			// load
			IniFile.IniSection s = config.getSection("client" + combo.getSelectedIndex());
			// empty combo
			if (s == null) {
				// reset the ui
				resetUI();
				return;
			}
			
			// enable items
			save.setEnabled(true);
			combo.setEnabled(true);
			clientMenu.setEnabled(true);
			// only if at least 2 clients
			if (combo.getModel().getSize() > 1)
				synch.setEnabled(true);
			
			// check for changes in files path
			int changes = LoL.updateClient(s.get("clientPath"), s.get("clientDataPath"));
			// if client not existing anymore
			if (changes == 1) {
				// TODO: what to do when clientPath doesnt exist ? remove it from the file ? autre ?
				// ask if we should delete it
				int res = JOptionPane.showConfirmDialog(null, LocaleString.string("Client not existing $0$", s.get("clientPath")),
	    				LocaleString.string("Client not existing"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				// delete client
		    	if (res == JOptionPane.OK_OPTION) {
		    		deleteClient(combo.getSelectedIndex());
		    	}
		    	else {
		    		// TODO: check that the button are enabled if select a working client after selecting a bad one
		    		// disable everything
					resetUI();
					// except the combo and the delete button
					 delete.setEnabled(true);
			    	 combo.setEnabled(true);
		    	}
				
			}
			// if changes, we update both dataPath and propPath
			else if (changes == 2) {
				s.put("clientDataPath", LoL.found.getAbsolutePath());
			}
			// load the client data
			LoL.load(new File(s.get("clientDataPath")));
			buildManager.init();
	    	tableModel.updateData();
	    	// set as active client in conf file
	    	config.addValue("general", "activeClient", s.getName());
	    	// save config file
			config.save(new File(configFile));
		}
		else if (e.getSource() == modeSel) {
			// change the game mode
			tableModel.setMode((LoLResources.GameMode)modeSel.getSelectedItem());
		}
			
	}

	public void valueChanged(ListSelectionEvent e) {
		if (combo.getSelectedIndex() == -1) return;
		if (e.getValueIsAdjusting() == false) {
			createBuild.setEnabled((list.getSelectedIndex() != -1));
	    }
	}

	public void keyPressed(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {
		if (e.getSource() == search) {
			ArrayList<String>res = champList;
			sampleModel.clear();
			for (int i=0; i<res.size(); i++) {
				if (search.getText().isEmpty())
					sampleModel.addElement(res.get(i));
				else if (res.get(i).toLowerCase().contains(search.getText().toLowerCase())) {
					sampleModel.addElement(res.get(i));
				}
			}
		}
	}

	public void focusGained(FocusEvent e) {}
	public void focusLost(FocusEvent e) {
		if (e.getSource() == search) {
			if (search.getText().equals("")) {
				search.setText(filterText);
				search.setEnabled(false);
			}
		}
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == search) {
			search.setEnabled(true);
			search.grabFocus();
			if (search.getText().equals(filterText))
				search.setText("");
		}
		else if (e.getSource() == list) {
			if (e.getClickCount() == 2 && combo.getSelectedIndex() != -1) {
				createBuild();
			}
		}
	}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mousePressed(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}

}