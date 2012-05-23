import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class IniFile {
	
	@SuppressWarnings("serial")
	public class IniSection extends HashMap<String,String> {
		private String sectionName;
		public IniSection(String section) {
			super();
			sectionName = section;
		}
		public String getName() {
			return sectionName;
		}
		public void setName(String newName) {
			sectionName = newName;
		}
		public String toString() {
			return "[" + sectionName + "] " + super.toString();
		}
	}
	
	// attribute
	List<IniSection> data;
	
	public IniFile()
	{
		data = new ArrayList<IniSection>();
	}

	public IniFile(File src)
	{
		data = new ArrayList<IniSection>();
		// if no file exists, initialization is over
		if (!src.exists()) return;
		FileInputStream fstream;
		try {
			fstream = new FileInputStream(src);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			IniSection cur = null;
			while ((strLine = br.readLine()) != null) {
				// section found
				if (strLine.matches("^\\[.+\\]$")) {
					if (cur != null)
						data.add(cur);
					cur = new IniSection(strLine.substring(1, strLine.length()-1));
				}
				// key/val found
				else {
					int pos = strLine.indexOf('=');
					if (pos == -1) {
						Log.getInst().warning(strLine + " malformated");
						continue;
					}
					if (cur == null) {
						Log.getInst().warning("no section found");
						continue;
					}
					cur.put(strLine.substring(0, pos), strLine.substring(pos+1));
				}
			}
			if (cur != null)
				data.add(cur);
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public IniSection getSection(String section)
	{
		for (IniSection in : data) {
			if (in.getName().equals(section))
				return in;
		}
		return null;
	}
	
	public void renameSection(String section, String newName) {
		IniSection se = getSection(section);
		if (se != null) {
			se.setName(newName);
		}
	}
	
	public IniSection getSection(int i) {
		return data.get(i);
	}

	public int size() {
		return data.size();
	}
	
	public void clear() {
		data.clear();
	}
	
	public IniSection createSection(String section) {
		if (getSection(section) == null) {
			IniSection s = new IniSection(section);
			data.add(s);
			return s;
		}
		return null;
	}
	
	public void addSection(IniSection s) {
		data.add(s);
	}
	
	public void removeSection(String section) {
		IniSection sect = getSection(section);
		if (sect != null) {
			data.remove(sect);
		}
	}
	
	public void addValue(String section, String key, String value)
	{
		IniSection sect = getSection(section);
		if (sect != null) {
			sect.put(key, value);
		}
	}
	
	public String getValue(String section, String key)
	{
		IniSection sect = getSection(section);
		if (sect != null) {
			return sect.get(key);
		}
		return null;
	}
	
	public void removeValue(String section, String key) {
		IniSection sect = getSection(section);
		if (sect != null) {
			sect.remove(key);
		}
	}
	
	public void save(String file) {
		save(new File(file));
	}
	
	public void save(File file) {
		try {
			// Create file 
			FileWriter fstream = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fstream);
			
			for (int i=0; i<size(); i++) {
				IniSection cur = getSection(i);
				out.write("[" + cur.getName() + "]");
				out.newLine();
				Iterator<Map.Entry<String,String>> iter = cur.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry<String,String> mEntry = (Map.Entry<String,String>)iter.next();
					out.write(mEntry.getKey() + "=" + mEntry.getValue());
					out.newLine();
				}
			}
			out.close();
			Log.getInst().info("File \"" + file + "\" saved correctly");
		} catch (Exception e) {
			Log.getInst().severe("save error: " + e.getMessage());
		}	
	}
	
}
