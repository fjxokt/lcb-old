import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;


public class LocaleString {

    private static LocaleString instance;
    private static ResourceBundle messages = null;
    public static String locale;
    
    public static LocaleString getInstance() {
        if (instance == null) {
            instance = new LocaleString();
        }
        return instance;
    }
    
    public static String string(String str) {
    	if (messages == null) return "ERROR";
    	return messages.getString(str.replaceAll(" ", "_"));
    }
    
    public static String string(String str, String var) {
    	return string(str, new String[]{var});
    }

    public static String string(String str, String[] var) {
    	if (messages == null) return "ERROR";
    	String res = messages.getString(str.replaceAll(" ", "_"));
    	for (int i=0; i<var.length; i++)
    		res = res.replaceFirst("\\$" + i + "\\$", var[i]);
    	return res;
    }
    
    public void initLocale(String lang) {
    	try {
    		InputStream u = this.getClass().getResourceAsStream("language_"+lang+".properties");
    		messages = new PropertyResourceBundle(new BufferedReader(new InputStreamReader(u, "UTF-8")));
    		locale = lang;
    	} catch (Exception e) { e.printStackTrace(); }
    }

    private LocaleString() {}
}