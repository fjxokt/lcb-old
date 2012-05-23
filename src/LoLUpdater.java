import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.List;

import javax.swing.JOptionPane;

import sun.management.ManagementFactory;
 

public class LoLUpdater {
	
    final static String SUN_JAVA_COMMAND = "sun.java.command";
    final static String updateUrl = "http://fjxokt.spreadeas.com/LoLCustomBuilds/update.txt";
    final static String updaterName = "LoLUpdater.jar";
    final static String appTempName = "update_lolbuilds";
    final static String appName = "LoLBuilds.jar";
    
    public static void applyUpdate() {
    	 
    	File updater = new File(updaterName);
   	 	File newApp = new File(appTempName);
   	 	
   	 	// if the new version has been downloaded
   	 	if (newApp.exists()) {
   	 		try {
   	 			// if there is an updater, launch it
   	 			if (updater.exists())
   	 				// it will rename and launch the new version
   	 				restartApplication(null);
			} catch (IOException e) {
				e.printStackTrace();
			}
   	 	}
   	 	
	   	 // delete updater if present
	    if (updater.exists())
	    	updater.delete();
    }

	 public static void checkUpdate(boolean silent) {    	
    	 URL site;
    	 String error = null;
    	 String v = null, updaterUrl = null, jarUrl = null;
    	 // reading the update file
    	 try {
 			site = new URL(updateUrl);
 	        URLConnection yc = site.openConnection();
 	        yc.setConnectTimeout(5000);
 	        BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream(), "UTF-8"));
 	        // program last version
 	        v = in.readLine();
 	        // url of the updater program
 	        updaterUrl = in.readLine();
 	        // url of the last lol program
 	        jarUrl = in.readLine();
 	        in.close();
    	 } catch (Exception e) {
    		 error = e.toString();
    		 e.printStackTrace();
    	 }

    	 if (error != null) {
    		 // if not silent (user manually checked for update), display a message
    		 if (!silent)
    			 JOptionPane.showMessageDialog(null, LocaleString.string("Error checking update") + "\n[error: "+error+"]",
    					 LocaleString.string("Update"), JOptionPane.ERROR_MESSAGE);
    		 return;
    	 }
    	  
    	 // new version found
    	 if (!v.equals(LoLWin.version)) {
    		 int res = JOptionPane.showConfirmDialog(null, LocaleString.string("New version $0$ available", new String[]{v}),
					 LocaleString.string("New version available"), JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
    		 
    		 if (res != JOptionPane.OK_OPTION)
    			 return;
    		 
    		 Log.getInst().info("New update available. Downloading " + jarUrl + "...");
    		 
    		 try {
    			 // download the updater
    			 URL newUpdaterJar = new URL(updaterUrl);
    			 ReadableByteChannel rbc = Channels.newChannel(newUpdaterJar.openStream());
    			 FileOutputStream fos = new FileOutputStream(updaterName);
    			 fos.getChannel().transferFrom(rbc, 0, 1 << 24);
    			 fos.close();
    			 
    			 // download the new app
    			 URL newJar = new URL(jarUrl);
    			 rbc = Channels.newChannel(newJar.openStream());
    			 fos = new FileOutputStream(appTempName);
    			 fos.getChannel().transferFrom(rbc, 0, 1 << 24);
    			 fos.close();
    			     			     			 
    			 // restart the app now ?
    			 int retour = JOptionPane.showConfirmDialog(null, LocaleString.string("Update downloaded"),
    					 LocaleString.string("Update"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    				
    			 // if ok close the app and launch the LoLUpdater
    			 if (retour == JOptionPane.OK_OPTION)
    				 restartApplication(null);
    			 else {
    				 JOptionPane.showMessageDialog(null, LocaleString.string("Update installed next start"),
    						 LocaleString.string("Update"), JOptionPane.INFORMATION_MESSAGE);
    			 }
    			 
			} catch (IOException e) {
				e.printStackTrace();
			}
    	 }
    	 // no update
    	 else {
    		 if (!silent) {
    			 JOptionPane.showMessageDialog(null, LocaleString.string("Version $0$ up to date", new String[]{LoLWin.version}),
    					 LocaleString.string("Update"), JOptionPane.INFORMATION_MESSAGE);
    		 }
    	 }
     }

	 public static void restartApplication(Runnable runBeforeRestart) throws IOException {
		 try {
			 // java binary
			 String java = System.getProperty("java.home") + "/bin/java";
			 // vm arguments
			 List<String> vmArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
			 StringBuffer vmArgsOneLine = new StringBuffer();
			 for (String arg : vmArguments) {
				 // if it's the agent argument : we ignore it otherwise the
				 // address of the old application and the new one will be in conflict
				 if (!arg.contains("-agentlib")) {
					 vmArgsOneLine.append(arg);
					 vmArgsOneLine.append(" ");
				 }
			 }
			 // init the command to execute, add the vm args
     		
			 String osName= System.getProperty("os.name");
			 if (osName.toLowerCase().contains("windows"))
				 java = "\"" + java + "\"";
     		
			 final StringBuffer cmd = new StringBuffer(java + " " + vmArgsOneLine);
			 
			 // program main and program arguments
			 String com = System.getProperty(SUN_JAVA_COMMAND);
			 if (com != null) {
				 String[] mainCommand = System.getProperty(SUN_JAVA_COMMAND).split(" ");
				 // program main is a jar
				 //if (mainCommand[0].endsWith(".jar")) {
				 // if it's a jar, add -jar mainJar
				 cmd.append("-jar " + updaterName + " " + appTempName + " " + appName);
				 //} else {
	     			// else it's a .class, add the classpath and mainClass
	     			//cmd.append("-cp \"" + System.getProperty("java.class.path") + "\" " + mainCommand[0]);
	     		//}
	     		// finally add program arguments
				 for (int i = 1; i < mainCommand.length; i++) {
					 cmd.append(" ");
					 cmd.append(mainCommand[i]);
				 }
			 }
			 else {
				 cmd.append("-jar " + updaterName + " " + appTempName + " " + appName);
			 }
			 
			 // log
			 Log.getInst().info("Executing : " + cmd.toString());
			 
			 // execute the command in a shutdown hook, to be sure that all the
			 // resources have been disposed before restarting the application
			 Runtime.getRuntime().addShutdownHook(new Thread() {
				 @Override
				 public void run() {
					 try {
						 Runtime.getRuntime().exec(cmd.toString());
					 } catch (IOException e) {
						 e.printStackTrace();
					 }
				 }
			 });
			 // execute some custom code before restarting
			 if (runBeforeRestart!= null) {
				 runBeforeRestart.run();
			 }
			 // exit
			 System.exit(0);
		 } catch (Exception e) {
			 // something went wrong
			 throw new IOException("Error while trying to restart the application", e);
		 }
	 }
}
