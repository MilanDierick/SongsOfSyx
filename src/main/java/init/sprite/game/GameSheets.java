package init.sprite.game;

import java.io.IOException;
import java.nio.file.Path;

import game.GAME;
import init.paths.PATH;
import init.paths.PATHS;
import snake2d.util.file.Json;
import snake2d.util.sets.*;

public class GameSheets {
	
	private final Sheet[] overlays = new Sheet[SheetType.ALL.size()];
	private final LIST<KeyMap<LIST<Sheet>>> map;
	final KeyMap<SheetType> imap = new KeyMap<>();
	public GameSheets() throws IOException {
		ArrayList<KeyMap<LIST<Sheet>>> m = new ArrayList<>(SheetType.ALL.size());
		map = m;
		for (SheetType t : SheetType.ALL) {
			imap.put(t.path, t);
			m.add(new KeyMap<LIST<Sheet>>());
		}
		for (SheetType t : SheetType.ALL) {
			if (t == SheetType.sCombo)
				continue;
			if (t == SheetType.sBox)
				continue;
			this.overlays[t.index()] = get(t, "_OVERLAY").get(0);
		}
		
	}
	
	
	private static LIST<Sheet> make(SheetType t, Path p, boolean rotates) throws IOException {
		return t.make(p, rotates);
	}
	
	public Sheet overlay(SheetType t) {
		return overlays[t.index()];
	}
	
	public LIST<Sheet> get(SheetType t, String file) throws IOException {
		
		if (map.get(t.index()).containsKey(file)) {
			return map.get(t.index()).get(file);
		}
		
		Path p = PATHS.SPRITE_GAME().getFolder(t.path).get(file);
		
		LIST<Sheet> sh = make(t, p, true);
		map.get(t.index()).put(file, sh);
		return sh;
		
	}
	
	public void add(SheetType t, LIST<Sheet> sh, String key) {
		if (map.get(t.index()).containsKey(key)) {
			throw new RuntimeException(key);
		}
		map.get(t.index()).put(key, sh);
	}
	
	private boolean[] adump = new boolean[SheetType.ALL.size()];
	
	public LIST<SheetPair> get(SheetType type, Json json) throws IOException {
		String[] ss = json.values("FRAMES");
		
		SheetData[] datas = new SheetData[ss.length];
		{
			SheetData odata = new SheetData(json);
			for (int i = 0; i < datas.length; i++) {
				datas[i] = odata;
			}
			if (json.has("OVERWRITE")) {
				Json[] js = json.jsons("OVERWRITE");
				for (int i = 0; i < datas.length && i < js.length; i++) {
					datas[i] = new SheetData(odata, js[i]);
				}
			}
		}
		
		
		
		int i = 0;
		ArrayList<SheetPair> sheets = new ArrayList<>(ss.length);
		
		for (String k : ss) {
			
			SheetData data = datas[i];
			
			if (k.equals("-")) {
				sheets.add(new SheetPair(type.dummy(), data));
				i++;
				continue;
			}
				
			String[] chops = k.split(":");

			if (chops.length != 2) {
				json.error("malformatted frame. Format is FILENAME:ROW Current is: "+k, k);
			}
			
			String file = chops[0].trim();
			String snr = chops[1].trim();
			
			PATH p = PATHS.SPRITE_GAME().getFolder(type.path);
			
			if (!map.get(type.index()).containsKey(file)) {
				
				
				if (!p.exists(file)) {
					String a = "";
					if (!adump[type.index()]) {
						a = System.lineSeparator() + "Available: ";
						adump[type.index()] = true;
						for (String ke : map.get(type.index()).keys()) {
							a += System.lineSeparator() + ke;
						}
						for (String ke : p.getFiles()) {
							if (!map.get(type.index()).containsKey(ke)) {
								a += System.lineSeparator() + ke;
							}
						}
					}
					
					GAME.WarnLight(p.get() + "/" + file + " does not exist and will be ignored. Refrenced from: " + json.path() + a);
					continue;
				}
			}
			LIST<Sheet> shs = get(type, file);
			
			int nr = 0;
			try {
				nr = Integer.parseInt(snr);
			}catch(Exception e) {
				json.error("malformatted Row. Format is FOLDER:FILENAME:ROW", k);
				continue;
			}
			
			if (nr < 0 || nr >= shs.size()) {
				GAME.WarnLight("ROW: " + nr + " in file: " + p.get(file) + " is out of bounds. must specify a row of the image: " + json.path());
				continue;
			}

			
			sheets.add(new SheetPair(shs.get(nr), data));
		}
		
		return new ArrayList<>(sheets);
		
		
	}
	
}
