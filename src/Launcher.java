

public class Launcher {
	public static void main(String[] args) {
		/*
		try {
            // Set System L&F
			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
    } 
    catch (Exception e) {
       // handle exception
    }*/
		// for mac users
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		//System.setProperty("com.apple.mrj.application.apple.menu.about.name", LoLWin.appName);
 	 
		// if there was an update, the app will be restarted
		LoLUpdater.applyUpdate();
		
		// run the main win
		//javax.swing.SwingUtilities.invokeLater(new Runnable() {
			//public void run() {
				new LoLWin();
			//}
		//});
	}
}