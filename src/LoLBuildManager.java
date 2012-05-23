import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;


public class LoLBuildManager {
	private static LoLResources data;
	private SortedMap<String, List<LoLBuild>> champBuildsClassic;
	private SortedMap<String, List<LoLBuild>> champBuildsDominion;
	private LoLResources.GameMode mode;
	private ItemChooser chooser;
	private static Map<String,LoLBuild> globalBuilds = null;

	public LoLBuildManager(LoLResources lol) {
		// init
		data = lol;
		champBuildsClassic = new TreeMap<String,List<LoLBuild>>();
		champBuildsDominion = new TreeMap<String,List<LoLBuild>>();
		mode = LoLResources.GameMode.Classic;
		chooser = new ItemChooser(data);
	}
	
	public void importBuilds(File file) {
		Log.getInst().info("Importing builds from file \"" + file.getAbsolutePath() + "\"...");
		
		IniFile load = new IniFile(file);
		importBuildsMap(load, "classic", champBuildsClassic);
		importBuildsMap(load, "dominion", champBuildsDominion);
	}
	
	private void importBuildsMap(IniFile f, String section, Map<String, List<LoLBuild>> map) {
		IniFile.IniSection sec = f.getSection(section);
		// for each champ, fill his builds list
		for (String champ : sec.keySet()) {
			List<LoLBuild> list = new ArrayList<LoLBuild>();
			String[] builds = sec.get(champ).split("¤");
			// get each champ build
			for (String build : builds) {
				String[] datas = build.split("£");
				// small check, if not 8 data, probably corrupted file, skip this build
				if (datas.length != 8) continue;
				String[] items = new String[6];
				for (int i=2; i<8; i++) {
					items[i-2] = datas[i];
				}
				// add each build to the list
				list.add(new LoLBuild(datas[0], datas[1], items));
			}
			// add the build list to the map
			map.put(champ, list);
		}
	}
	
	public void exportBuilds(String filename) {
		Log.getInst().info("Exporting builds to file \"" + filename + "\"...");
		
		IniFile save = new IniFile();
		exportBuildsMap(save.createSection("classic"), champBuildsClassic);
		exportBuildsMap(save.createSection("dominion"), champBuildsDominion);
		save.save(filename);
	}
	
	private void exportBuildsMap(IniFile.IniSection sec, Map<String, List<LoLBuild>> map) {
		for (String champ : map.keySet()) {
			StringBuffer val = new StringBuffer();
			ArrayList<LoLBuild> list = (ArrayList<LoLBuild>) map.get(champ);
			// create the value string (id:name:item1:item2:item3:item4:item5:item6Â¤id2:name2:etc...)
			for (LoLBuild b : list) {
				val.append(b.buildId + "£" + b.buildName);
				for (String s : b.items) {
					val.append("£" + s);
				}
				val.append("¤");
			}
			sec.put(champ, val.toString());
		}
	}
	
	public static Map<String,LoLBuild> getGlobalBuilds() {
		if (globalBuilds == null) {
			// load list
			globalBuilds = loadGlobalBuilds();
		}
		return globalBuilds;
	}
	
	public static Map<String,LoLBuild> loadGlobalBuilds() {
		Map<String,LoLBuild> res = new TreeMap<String,LoLBuild>();
		IniFile f = new IniFile(new File(LoLWin.buildsFile));
		IniFile.IniSection s = f.getSection("builds");
		// no data, nothing to load
		if (s == null) return res;
		for (String key : s.keySet()) {
			String b = s.get(key);
			if (b != null) {
				String[] items = b.split(":");
				LoLBuild build = new LoLBuild("0", key, items);
				res.put(key, build);
			}
		}
		return res;
	}
	
	public static void saveGlobalBuilds() {
		if (globalBuilds == null) return;
		
		IniFile f = new IniFile();
		f.createSection("builds");
		
		Set<String> keys = globalBuilds.keySet();
		for (String key : keys) {
			String[] i = globalBuilds.get(key).items;
			StringBuffer s = new StringBuffer();
			s.append(i[0]).append(":").append(i[1]).append(":").append(i[2]).append(":").append(i[3]).append(":")
				.append(i[4]).append(":").append(i[5]);
			f.addValue("builds", key, s.toString());
		}
		
		f.save(new File(LoLWin.buildsFile));
	}
	
	public static void addGlobalBuild(LoLBuild b) {
		getGlobalBuilds().put(b.buildName, b);
		saveGlobalBuilds();
	}
	
	public static void removeGlobalBuild(LoLBuild b) {
		getGlobalBuilds().remove(b.buildName);
		saveGlobalBuilds();
	}
	
	public ItemChooser getItemChooser() {
		return chooser;
	}
	
	public void setMode(LoLResources.GameMode m) {
		mode = m;
	}
	
	public LoLResources.GameMode getMode() {
		return mode;
	}
	
	public LoLResources getLoLItems() {
		return data;
	}
	
	public Map<String,List<LoLBuild>> getBuildsList(LoLResources.GameMode mode) {
		return (mode == LoLResources.GameMode.Classic) ? champBuildsClassic : champBuildsDominion;
	}
	
	public void clear() {
		champBuildsClassic.clear();
		champBuildsDominion.clear();
	}
	
	public void init() {
		clear();
		for (String champ : data.champList) {
			if (data.hasCustomItems(champ, LoLResources.GameMode.Classic))
				champBuildsClassic.put(champ, LoLBuild.loadBuilds(data.getCustomItems(champ, LoLResources.GameMode.Classic)));
			if (data.hasCustomItems(champ, LoLResources.GameMode.Dominion))
				champBuildsDominion.put(champ, LoLBuild.loadBuilds(data.getCustomItems(champ, LoLResources.GameMode.Dominion)));
		}
	}
	
	// add a build and return it so it can be modified from outside
	public LoLBuild addBuild(String champ, LoLResources.GameMode mode, String name, String[] bItems) {
		String[] items = (bItems != null) ? bItems.clone() : data.getRecItems(champ, mode);
		
		List<LoLBuild> buildsList = null;
		// if the camp doesnt have custom builds yet
		if (!getBuildsList(mode).containsKey(champ)) {
			buildsList = new ArrayList<LoLBuild>();
			getBuildsList(mode).put(champ, buildsList);
		}
		else {
			buildsList = getBuildsList(mode).get(champ);
		}
		
		String id = findId(buildsList);
		LoLBuild b = new LoLBuild(id, name, items);
		buildsList.add(b);
		
		return b;
	}
	
	// find a non used id name for the build
	private String findId(List<LoLBuild> builds) {
		boolean end = true;
		int i = 2;
		String str = null;
		while (end) {
			str = "SetItem" + i;
			boolean found = false;
			for (LoLBuild b : builds) {
				if (b.buildId.equals(str)) {
					found = true;
					continue;
				}
			}
			if (!found)
				end = false;
			i++;
		}
		return str;
	}
	
	public List<LoLBuild> getBuilds(String champ, LoLResources.GameMode mode) {
		return getBuildsList(mode).get(champ);
	}
	
	public LoLBuild getBuild(String champ, LoLResources.GameMode mode, String name) {
		if (name.equals("Default"))
			return new LoLBuild("id", "Default", data.getRecItems(champ, mode));
		List<LoLBuild> builds = getBuilds(champ, mode);
		for (LoLBuild b : builds) {
			if (b.buildName.equals(name))
				return b;
		}
		return null;
	}
	
	// remove a build
	public int removeBuild(String champ, LoLResources.GameMode mode, String name) {
		List<LoLBuild> builds = getBuildsList(mode).get(champ);
		for (LoLBuild b : builds) {
			if (b.buildName.equals(name)) {
				builds.remove(b);
				break;
			}
		}
		return builds.size();
	}
	
	public void renameBuild(String champ, LoLResources.GameMode mode, String name, String newName) {
		if (name.equals("Default")) return;
		if (newName.equals("Default")) return;
		LoLBuild b = getBuild(champ, mode, name);
		if (b != null) b.buildName = newName;
	}
	
	// save a build file in the corresponding file
	public void saveBuilds(String champ, LoLResources.GameMode mode) {
		IniFile file = new IniFile();
		List<LoLBuild> builds = getBuilds(champ, mode);
		if (builds != null) {
			for (LoLBuild b : builds) {
				file.createSection(b.buildId);
				file.addValue(b.buildId, "SetName", b.buildName);
				for (int i=1; i<7; i++)
					file.addValue(b.buildId, "RecItem"+i, b.items[i-1]);
			}
			String path = data.getItemsFilePath(champ, mode);
			file.save(new File(path));
		}
	}
	
	public void saveBuilds(String champ) {
		saveBuilds(champ, LoLResources.GameMode.Classic);
		saveBuilds(champ, LoLResources.GameMode.Dominion);
	}
	
	// save all data
	public void saveBuilds() {
		Set<String> keys = champBuildsClassic.keySet();
		for (String champ : keys)
			saveBuilds(champ);
		keys = champBuildsDominion.keySet();
		for (String champ : keys)
			saveBuilds(champ);
	}
	
	public void disableActiveBuild(String champ, LoLResources.GameMode mode) {
		List<LoLBuild> builds = getBuilds(champ, mode);
		for (LoLBuild b : builds) {
			if (b.buildId.equals("ItemSet1")) {
				b.buildId = findId(builds);
				break;
			}
		}
	}
	
	public void enableActiveBuild(String champ, LoLResources.GameMode mode, String buildName) {
		if (buildName.equals("Default")) {
			disableActiveBuild(champ, mode);
			return;
		}
		List<LoLBuild> builds = getBuilds(champ, mode);
		LoLBuild old = null, cur = null;
		for (LoLBuild b : builds) {
			if (b.buildId.equals("ItemSet1"))
				old = b;
			if (b.buildName.equals(buildName))
				cur = b;
		}
		// unknown build 'buildName', nothing to enable
		if (cur == null)
			return;
		// switch with the other active build
		if (old != null) {
			old.buildId = cur.buildId;
			cur.buildId = "ItemSet1";
		}
		// no active build set, lets set it then
		else {
			cur.buildId = "ItemSet1";
		}
	}
	
	public static String createToolTip(String item) {
 	   String[] str = data.items.get(item).split("\\$");
 	   return "<html><div style=\"width: 200px\"><b>"+str[0]+"</b><br>"+str[2]+"</div></html>";
	}
	
}
