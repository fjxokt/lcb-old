import java.util.ArrayList;

public class LoLBuild {
	String buildId;
	String buildName;
	String[] items;
	
	public LoLBuild(String id, String name, String[] _items) {
		buildId = id;
		buildName = name;
		items = _items;
	}
	
	public LoLBuild(IniFile.IniSection s) {
		buildId = s.getName();
		buildName = s.get("SetName");
		items = new String[] { s.get("RecItem1"), s.get("RecItem2"), s.get("RecItem3"),
							s.get("RecItem4"), s.get("RecItem5"), s.get("RecItem6") };
	}
	
	public static ArrayList<LoLBuild> loadBuilds(IniFile f) {
		ArrayList<LoLBuild> res = new ArrayList<LoLBuild>();
		int imax = f.size();
		for (int i=0; i<imax; i++) {
			res.add(new LoLBuild(f.getSection(i)));
		}
		return res;
	}
	
	public String toString() {
		return "[" + buildId + "," + buildName + ",[" + items[0] + items[1] + items[2] + items[3] + items[4] + items[5]+ "]]";
	}
}
