import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;

public class LoLResources {
	// did we find the LolClient.exe
	File found = null;
	File found2 = null;
	// folder where to save the Champ/file.ini files
	File baseCharsFolder;
	// folder where proprety files are stored
	//File propertiesFolder;
	// used to see if the game has been patched
	String lolVersion;
	// data file
	File data;
	// list of champions
	ArrayList<String> champList;
	// each line = list of rec items id, separated by ,
	ArrayList<String> recommandedItems;
	// items <Name, ID>
	HashMap<String,String> items;
	// filters list
	HashMap<String,String> filters;
	// used to give informations to the gui
	LoLWin.Loading win;
	//String baseSiteUrl = "http://euw.leagueoflegends.com/fr";
	//String baseSiteUrl = "http://euw.leagueoflegends.com/de";
	//String baseSiteUrl = "http://euw.leagueoflegends.com/es";
	//String baseSiteUrl = "http://eune.leagueoflegends.com/pl";
	String baseSiteUrl = "http://na.leagueoflegends.com";
	String baseSiteUrlUS = "http://na.leagueoflegends.com";
	
	enum GameMode { Classic, Dominion };
	enum Type { CHAMPION, ITEM };
	
	public static class Finder {
		static File found = null;
		static File findFolder(String dir, String file, boolean findOnlyOne, boolean findChampFolder) {
			found = null;
			_findFolder(new File(dir), file, findOnlyOne, findChampFolder);
			return found;
		}
		static void _findFolder(File dir, String file, boolean findOnlyOne, boolean findChampFolder) {
	    	if (found != null) return;
			File[] files = dir.listFiles();
			if (files == null) return;
			for (File f : files) {
				//System.out.println(f);
				if (f.getName().equals(file)) {
					System.out.println("found: " + f.getAbsolutePath());
					if (findChampFolder) {
						//System.out.println("found: " + f.getAbsolutePath());
						if (f.getAbsolutePath().contains(File.separator + "solutions" + File.separator)) found = f;
							//System.out.println("OK NORMAL: " +f.getAbsolutePath());
						if (f.getAbsolutePath().contains(File.separator + "files" + File.separator + "DATA")) found = f;
							//System.out.println("OK ILOL: " +f.getAbsolutePath());
						if (f.getAbsolutePath().contains(File.separator + "game" + File.separator + "DATA")) found = f;
							//System.out.println("OK ACE: " +f.getAbsolutePath());*/
					}
					else {
						found = f;
					}
					// if we found one we stop
					if (findOnlyOne) {
						return;
					}

				}
				// avoid links to directories
				if (f.isDirectory() && !f.getName().contains(":"))
					_findFolder(f, file, findOnlyOne, findChampFolder);
			}
			return;
		}
	}
	
	public LoLResources(LoLWin.Loading win) {
		champList = new ArrayList<String>();
		items = new HashMap<String,String>();
		filters = new HashMap<String,String>();
		recommandedItems = new ArrayList<String>();
		this.win = win;
	}
	
	public File findDataFolder(String dir) {
		File res = Finder.findFolder(dir, "DATA", true, true);
		return res;
			
	}
	
	public void outputOnLoadWin(String str, boolean debug) {
		if (win != null) win.setLabel(str);
		if (debug) System.out.println(str);
	}
	
	public boolean init() {
		return _init(false);
	}
	
	public boolean reset() {
		return _init(true);
	}
	
	private boolean _init(boolean force) {
		// create the images folder
		File dir = new File("images");
		boolean dirExists = dir.exists();
		dir.mkdir();
		
		data = new File(LoLWin.dataFile);
		clear();
		
		// if data is here
		if (data.exists() && dirExists && !force)
			loadData(data);

		// get item/champ data if not existing, or checking for new updates
		getItems();
		getChampions();
		
		// save data
		saveData(data);
				
		return true;
	}
	
	public boolean isClient(File dir) {
		found = findDataFolder(dir.toString());
		if (found != null) {
			System.out.println("File found : " + found);
		}
		return (found != null);
	}
	
	public int updateClient(String dir, String dataDir) {
		File f = new File(dir);
		int res = 0;
		if (!f.exists()) {
			Log.getInst().warning(dir + " doesn't exists anymore !");
			return 1;
		}
		f = new File(dataDir);
		// data dir disapeared, new version of the game
		if (!f.exists()) {
			found = findDataFolder(dir);
			Log.getInst().info(dataDir + " updated to " + found.getAbsolutePath());
			res = 2;
		}
		else {
			found = new File(dataDir);
		}
		return res;
	}
	
	public boolean load(File dataDir) {
		
		found = dataDir;
		String data = found.getAbsolutePath();
				
		// creating /Characters folder
		File charFolder = new File(data + File.separator + "Characters");
		if (charFolder.mkdir()) {
			System.out.println("Folder /Characters created");
		}
		baseCharsFolder = charFolder;
		
		// create all champ folders
		for (String champ : champList) {
			champ = fixChampNames(champ);
			File champFolder = new File(charFolder + File.separator + champ);
			if (champFolder.mkdir()) {
				System.out.println("Folder /Characters/" + champ + " created");
			}
		}
		
		return true;
	}
	
	// remove all "strange" chars from champs names
	public String fixChampNames(String name) {
		return name.replaceAll(" |\\.|'", "");
	}
	
	public String getImagePath(Type type, String data, boolean thumb) {
		if (items == null) return "";
		String thumbi = (!thumb) ? "" : "_thumb";
		String res = null;
		switch (type) {
		case CHAMPION:
			res = "images" + File.separator + data + thumbi + ".jpg";
			break;
		case ITEM:
			res = "images" + File.separator + data + thumbi + ".png";
			break;
		}
		return res;
	}
	
	public void clear() {
		champList.clear();
		recommandedItems.clear();
		items.clear();
		filters.clear();
	}
	
	public void getChampions() {
		outputOnLoadWin(LocaleString.string("Retrieving champions list and pictures") + "...", true);
		URL site;
		String url = baseSiteUrlUS + "/champions";
		String link = null;
		try {
			site = new URL(url);
	        URLConnection yc = site.openConnection();
	        yc.setConnectTimeout(5000);
	        BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream(), "UTF-8"));
	        String inputLine;
	        while ((inputLine = in.readLine()) != null) {
	        	if (inputLine.contains("class=\"lol_champion\"")) {
	        		String[] s = inputLine.split("/");
	        		link = "/champions/" + s[2] + "/" + s[3].split("\"")[0];
	        	}
	        	else if (inputLine.contains("<div class=\"champion_name")) {
	        		String champ = inputLine.split(">")[1].split("<")[0];
	        		// if we don't have this champ
	        		if (!champList.contains(champ))
		        		getChampion(link);
	        	}
	        	// we parsed everything we needed, we can stop reading
	        	else if (inputLine.contains("mode_view_list")) {
	        		break;
	        	}
	        }
	        in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void getChampion(String champurl) {
		outputOnLoadWin(LocaleString.string("Reading from $0$", new String[]{champurl}) + "...", true);
		URL site;
		String url = baseSiteUrlUS + champurl;
		try {
			site = new URL(url);
	        URLConnection yc = site.openConnection();
	        yc.setConnectTimeout(5000);
	        BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream(), "UTF-8"));
	        String inputLine;
	        String version = null, champName = null;
	        String recItems = "";
	        while ((inputLine = in.readLine()) != null) {
	        	// champion name
	        	if (inputLine.contains("span class=\"champion_name\"")) {
	        		champName = inputLine.split(">")[1].split("<")[0];
	        	}
	        	// recommanded items
	        	else if (inputLine.contains("item_icon")) {
	        		recItems += inputLine.split("\"")[3].split("_")[2] + ",";
	        	}
	        	// lol version
	        	else if ((version == null) && (inputLine.contains("game_data"))) {
	        		String s[] = inputLine.split("/");
	        		version = s[5];
	        		String champId = s[9].split("\\.")[0];
	        		
	        		// get the picture
	        		String nurl = baseSiteUrlUS + "/sites/default/files/game_data/" + 
	        						version + "/content/champion/icons/" + champId + ".jpg";
	        		getImage(nurl, champName, "jpg", "images", 60);
	        		champList.add(champName);
	        	}
	        }
    		recommandedItems.add(recItems.substring(0, recItems.length()-1));
	        in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void getItems() {
		outputOnLoadWin(LocaleString.string("Retrieving items list and pictures") + "...", true);
		URL site;
		// TODO: change site url depending on LocaleString.locale
		String url = baseSiteUrl + "/items";
		String urlImages = baseSiteUrl + "/sites/default/files/game_data/";//1.0.0.134/content/item/3001.png";
		ArrayList<String> itemsFilters = new ArrayList<String>();
		try {
			site = new URL(url);
	        URLConnection yc = site.openConnection();
	        yc.setConnectTimeout(5000);
	        BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream(), "UTF-8"));
	        String inputLine;
	        String id = null, name = null, desc = null;
	        int cpt = -1;
	        boolean newItem = false;
	        boolean itemsData = false;
	        while ((inputLine = in.readLine()) != null) {
	        	// getting filters data (first part of the parsing)
	        	if (!itemsData) {
		        	// correspondance filter_tag / text
		        	if (inputLine.contains("filter checkbx")) {
		        		String tag = inputLine.substring(inputLine.indexOf("value=\"")).split("\"")[1];
		        		String val = inputLine.split(">")[2].split("<")[0];
		        		filters.put(tag, val);
		        	}
		        	// filter tags for the item
		        	else if (inputLine.contains("<li class=\" filter_tag")) {
		        		itemsFilters.add(inputLine.split("\"")[1].trim());
		        	}
		        	// handle the case of no filter
		        	else if (inputLine.contains("<li class=\"\"")) {
		        		itemsFilters.add(" ");
		        	}
		        	// now we know the number of items, we can stop reading if no new found
		        	else if (inputLine.contains("mode_view_list")) {
		        		if (items.size() == itemsFilters.size()) {
	        				break;
	        			}
		        		itemsData = true;
		        	}
	        	}
	        	// getting items data (second part of the parsing)
	        	else {
	        		if (inputLine.contains("style=\" position")) {
	        			cpt++;
		        		String[] res = inputLine.split("/");
		        		id = res[10].substring(0, 4);
		        		// new item found
		        		if (!items.containsKey(id)) {
		        			newItem = true;
		        			String version = res[7];
			        		getImage(urlImages + version + "/content/item/" + id + ".png", id, "png", "images", 0, 0, true, true);
		        		}
		        	}
		        	else if (newItem && inputLine.contains("item_detail_name")) {
		        		name = inputLine.split(">")[1].split("<")[0];
	        			outputOnLoadWin(LocaleString.string("Reading item $0$", new String[]{name}) + "...", true);
		        	}
		        	else if (newItem && inputLine.contains("item_description")) {
		        		desc = inputLine.split(">")[1];
		        		desc = desc.substring(0, desc.length()-3);
		        		items.put(id, name + "$" + itemsFilters.get(cpt) + "$" + desc);
		        		newItem = false;
		        	}
	        	}
	        }
	        in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void getImage(String url, String name, String ext, String folder, int newSize) {
		getImage(url, name, ext, folder, newSize, true);
	}
	
	public void getImage(String url, String name, String ext, String folder, int newSize, boolean fthumb) {
		getImage(url, name, ext, folder, newSize, newSize, fthumb, false);
	}
	
	public void getImage(String url, String name, String ext, String folder, int sizeX, int sizeW, boolean fthumb, boolean fsmall) {
		File res = new File(folder + File.separator + name + "." + ext);
		// if file already exists, return
		if (res.exists())
			return;
		BufferedImage image = null;
		try {
			URL u = new URL(url);
			image = ImageIO.read(u);
			if (sizeX > 0) image = resize(image, sizeX, sizeW);
			ImageIO.write(image, ext, res);
	        outputOnLoadWin(LocaleString.string("File $0$ downloaded", new String[]{name+"."+ext}), true);
		} catch (Exception e) {
			System.out.println(e);
		}
		// create miniature
		if (fthumb) {
	 	   	BufferedImage newimg = resize(image, 45, 45);
	 	   	File thumb = new File(folder + File.separator + name + "_thumb." + ext);
	        try {
	            ImageIO.write(newimg, ext, thumb);
	        } catch(IOException e) {
	            System.out.println("Write error for " + thumb.getPath() + ": " + e.getMessage());
	        }
		}
		// create small
		if (fsmall) {
	 	   	BufferedImage newimg = resize(image, 30, 30);
	 	   	File thumb = new File(folder + File.separator + name + "_small." + ext);
	        try {
	            ImageIO.write(newimg, ext, thumb);
	        } catch(IOException e) {
	            System.out.println("Write error for " + thumb.getPath() + ": " + e.getMessage());
	        }
		}
	}
	
	// resize a BufferedImage to the desired size
	private BufferedImage resize(BufferedImage img, int newW, int newH) {  
        int w = img.getWidth();  
        int h = img.getHeight();  
        BufferedImage dimg = new BufferedImage(newW, newH, img.getType());  
        Graphics2D g = dimg.createGraphics();  
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);  
        g.drawImage(img, 0, 0, newW, newH, 0, 0, w, h, null);  
        g.dispose();  
        return dimg;  
    }  
	
	public void saveData(File file) {
		IniFile save = new IniFile();
		// saving champions / recommanded items
		save.createSection("champions");
		int i = 0;
		for (String cur : champList) {
			save.addValue("champions", cur, recommandedItems.get(i++));
		}
		// saving filters
		save.createSection("filters");
		Iterator<Map.Entry<String,String>> iter = filters.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String,String> mEntry = (Map.Entry<String,String>)iter.next();
			save.addValue("filters", mEntry.getKey(), mEntry.getValue());
		}
		// saving items list
		save.createSection("items");
		iter = items.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String,String> mEntry = (Map.Entry<String,String>)iter.next();
			save.addValue("items", mEntry.getKey(), mEntry.getValue());
		}
		save.save(file);
	}
	
	public void loadData(File file) {
		IniFile res = new IniFile(file);
		// loading champions / recommanded items
		IniFile.IniSection sec = res.getSection("champions");
		champList.clear();
		Iterator<Map.Entry<String,String>> iter = sec.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String,String> mEntry = (Map.Entry<String,String>)iter.next();
			champList.add(mEntry.getKey());
			recommandedItems.add(mEntry.getValue());
		}
		// loading filters
		sec = res.getSection("filters");
		filters.clear();
		iter = sec.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String,String> mEntry = (Map.Entry<String,String>)iter.next();
			filters.put(mEntry.getKey(), mEntry.getValue());
		}
		// loading items
		sec = res.getSection("items");
		items.clear();
		iter = sec.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String,String> mEntry = (Map.Entry<String,String>)iter.next();
			items.put(mEntry.getKey(), mEntry.getValue());
		}
	}
	
	public String getItemDesc(String itemId) {
		return items.get(itemId).split("\\$")[1];
	}
	
	public String[] getRecItems(String champ, GameMode mode) {
		String[] str = recommandedItems.get(champList.indexOf(champ)).split(",");
		String[] res = new String[6];
		int i = 0, base = (mode == GameMode.Classic) ? 0 : 6;
		while (i < 6) {
			res[i] = str[base + i++];
		}
		return res;
	}
	
	public boolean hasCustomItems(String champ, GameMode mode) {
		String file = (mode == GameMode.Classic) ? "RecItemsCLASSIC.ini" : "RecItemsODIN.ini";
		File path = new File(baseCharsFolder + File.separator + fixChampNames(champ) + File.separator + file);
		return (path.length() > 0);
	}
	
	public IniFile getCustomItems(String champ, GameMode mode) {
		String file = (mode == GameMode.Classic) ? "RecItemsCLASSIC.ini" : "RecItemsODIN.ini";
		File path = new File(baseCharsFolder + File.separator + fixChampNames(champ) + File.separator + file);
		IniFile res = new IniFile(path);
		return res;
	}
	
	public String getItemsFilePath(String champ, GameMode mode) {
		String file = (mode == GameMode.Classic) ? "RecItemsCLASSIC.ini" : "RecItemsODIN.ini";
		return baseCharsFolder + File.separator + fixChampNames(champ) + File.separator + file;
	}

}
