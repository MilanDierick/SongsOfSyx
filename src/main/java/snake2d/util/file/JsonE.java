package snake2d.util.file;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map.Entry;

public class JsonE {

	private final HashMap<String, String> map = new HashMap<>();
	
	public JsonE(){
		
	}
	
	public void add(String key, String value) {
		map.put(key, value);
	}
	
	public void add(String key, String[] values) {
		StringBuilder b = new StringBuilder();
		b.append('[');
		b.append(System.lineSeparator());
		for (String v : values) {
			b.append(v);
			b.append(',');
			b.append(System.lineSeparator());
		}
		b.append(']');
		add(key, b.toString());
	}
	
	public void addString(String key, String value) {
		add(key, "\""+ value + "\"");
	}
	
	public void addStrings(String key, String[] values) {
		for (int i = 0; i < values.length; i++) {
			values[i] = "\""+ values[i] + "\"";
		}
		add(key, values);
	}
	
	public boolean has(String key) {
		return map.containsKey(key);
	}
	
	public void add(String key, boolean b) {
		map.put(key, b ? "true" : "false");
	}
	
	public void add(String key, int i) {
		map.put(key, ""+i);
	}
	
	public void add(String key, int[] is) {
		String[] values = new String[is.length];
		for (int i = 0; i < is.length; i++) {
			values[i] = ""+is[i];
		}
		add(key, values);
	}
	
	public void add(String key, double d) {
		map.put(key, ""+d);
	}
	
	public void add(String key, double[] is) {
		String[] values = new String[is.length];
		for (int i = 0; i < is.length; i++) {
			values[i] = ""+is[i];
		}
		add(key, values);
	}
	
	public void add(String key, JsonE json) {
		StringBuilder b = new StringBuilder();
		b.append('{');
		b.append(System.lineSeparator());
		b.append(json.toString());
		b.append('}');
		add(key, b.toString());
	}
	
	public void add(String key, JsonE[] jsons) {
		String[] strings = new String[jsons.length];
		for (int i = 0; i < jsons.length; i++) {
			strings[i] = "{" + System.lineSeparator() + jsons[i].toString() + System.lineSeparator() + "}";
		}
		add(key, strings);
	}
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		for(Entry<String, String> e : map.entrySet()) {
			b.append(e.getKey());
			b.append(':');
			b.append(' ');
			String[] ss = e.getValue().split(System.lineSeparator());
			b.append(ss[0]);
			for (int i = 1; i < ss.length; i++) {
				b.append(System.lineSeparator());
				b.append('\t');
				b.append(ss[i]);
			}
			b.append(',');
			b.append(System.lineSeparator());
		}
		return b.toString();
	}
	
	public boolean save(String path) {
		try {
			if (new File(path).exists())
				new File(path).delete();
			PrintWriter out = new PrintWriter(path);
		    out.println(toString());
		    out.flush();
		    out.close();
		    return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean save(java.nio.file.Path path) {
		try {
			Files.deleteIfExists(path);
			PrintWriter out = new PrintWriter(Files.newOutputStream(path));
		    out.println(toString());
		    out.flush();
		    out.close();
		    return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
}
