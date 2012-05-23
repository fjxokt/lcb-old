import java.util.HashMap;

import javax.swing.ImageIcon;


public class ResourcesManager {

    private static ResourcesManager instance;
    private HashMap<String,ImageIcon> map;
    private HashMap<String,Object> cellMap;
    
    public static ResourcesManager getInstance() {
        if (instance == null) {
            instance = new ResourcesManager();
        }
        return instance;
    }
    
    public ImageIcon getIcon(String res) {
    	return getIcon(res, false);
    }
    
    public ImageIcon getIcon(String res, boolean isResource) {
    	ImageIcon icon = map.get(res);
    	if (icon == null) {
    		icon = (isResource) ? new ImageIcon(this.getClass().getResource(res)) : new ImageIcon(res);
			map.put(res, icon);
    	}
    	return icon;
    }
    
    public Object getCell(int col) {
    	Object res = cellMap.get("" + col);
    	return res;
    }
    
    public Object putCell(int col, Object o) {
    	cellMap.put("" + col, o);
    	return o;
    }

    private ResourcesManager() {
    	map = new HashMap<String,ImageIcon>();
    	cellMap = new HashMap<String,Object>();
    }
}