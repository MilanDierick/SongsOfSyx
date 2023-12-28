package util.save;

import java.util.Arrays;

import game.VERSION;
import init.D;
import init.paths.PATHS;
import settlement.stats.STATS;
import snake2d.util.sprite.text.Str;

public class SaveFile implements Comparable<SaveFile>{
	
	public final String name;
	public final CharSequence ago;
	public final CharSequence fullName;
	public final int version;
	public final int modHash;
	public final int pop;
	public final long t;
	private static CharSequence ¤¤1Minute = "¤1 minute ago";
	private static CharSequence ¤¤Minutes = "¤{0} minutes ago";
	private static CharSequence ¤¤1Hour = "¤1 hour ago";
	private static CharSequence ¤¤Hours = "¤{0} hours ago";
	private static CharSequence ¤¤Yesterday = "¤yesterday";
	private static CharSequence ¤¤DaysAgo = "¤{0} days ago";
	
	private static CharSequence ¤¤Version = "¤This save is from a previous version and will probably not load!";
	private static CharSequence ¤¤Mod = "¤This save is made with a different mod configuration and will probably not load!";
	
	static {
		D.ts(SaveFile.class);
	}
	
	public SaveFile(String f){
		fullName = f;
		name = name(f);
		t = time(f);
		version = version(f);
		modHash = modHash(f);
		pop = pop(f);
		if (t > 0 && System.currentTimeMillis()-t > 0) {
			
			
			long now = (System.currentTimeMillis() - t)/(1000*60*60*24);

			if (now == 0) {
				now = (System.currentTimeMillis() - t)/(1000*60*60);
				if (now == 0) {
					now = (System.currentTimeMillis() - t)/(1000*60);
					if (now == 1) {
						ago = ¤¤1Minute;
					}else {
						ago = new Str(¤¤Minutes).insert(0, (int)now);
					}
				}else if (now == 1) {
					ago = ¤¤1Hour;
				}else {
					ago = new Str(¤¤Hours).insert(0, (int)now);
				}
			}else if(now == 1) {
				ago = ¤¤Yesterday;
			}else {
				ago = new Str(¤¤DaysAgo).insert(0, (int)now);
			}

		}else {
			ago = "???";
		}
	}
	

	public static SaveFile[] list(){
		String[] ss = PATHS.local().SAVE.getFiles();
		
		SaveFile[] saves = new SaveFile[ss.length];
		for (int i = 0; i < ss.length; i++) {
			saves[i] = new SaveFile(ss[i]);
		}
		Arrays.sort(saves);
		return saves;
	}
	
	private String name(String file) {
		return get(file, 4, false);
	}
	
	private static long time(String file) {
		String s = get(file, 3, true);
		try {
			return Long.parseLong(s, 16)*60*1000;
		}catch(Exception e) {
			return -1;
		}
	}
	
	private static int version(String file) {
		String s = get(file, 2, true);
		
		try {
			return (int) Long.parseLong(s, 16);
		}catch(Exception e) {
			return 0;
		}
	}
	
	private static int modHash(String file) {
		String s = get(file, 1, true);
		try {
			return (int) Long.parseLong(s, 16);
		}catch(Exception e) {
			return 0;
		}
	}
	
	private static int pop(String file) {
		String s = get(file, 0, true);
		try {
			return (int) Long.parseLong(s, 16);
		}catch(Exception e) {
			return 0;
		}
	}

	private static String get(String file, int part, boolean p) {
		while(part > 0) {
			int i = file.lastIndexOf('-');
			if (i <= 0)
				return "0";
			file = file.substring(0, i);
			part--;
		}
		int i = file.lastIndexOf('-');
		if (i <= 0 || !p)
			return file;
		return file.substring(i+1, file.length());
	}
	
	@Override
	public int compareTo(SaveFile arg0) {
//		if (VERSION.versionMajor(version) < VERSION.versionMajor(arg0.version)) {
//			return 1;
//		}
//		if (VERSION.versionMajor(version) > VERSION.versionMajor(arg0.version)) {
//			return -1;
//		}
		if (t < 0 && arg0.t >= 0)
			return 1;
		if (t >= 0 && arg0.t < 0)
			return -1;
		
		long ti = arg0.t - t;
		if (ti < 0)
			return -1;
		if (ti > 0)
			return 1;
		return name.compareTo(arg0.name);
	}
	
	public static String stamp(CharSequence savefile) {
		String t = Long.toHexString(System.currentTimeMillis()/(1000*60));
		String v = Integer.toHexString(VERSION.VERSION);
		String mods = Integer.toHexString(PATHS.modHash());
		String pop = Integer.toHexString(STATS.POP().POP.data(null).get(null));
		return savefile + "-" + t + "-" + v + "-" + mods  + "-" + pop;
	}
	
	public CharSequence problem() {
		if (VERSION.VERSION_MAJOR != VERSION.versionMajor(version)) {
			return ¤¤Version;
		}
		if (modHash != PATHS.modHash()) {
			return ¤¤Mod;
		}
		return null;
	}
	
}