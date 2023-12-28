package init.tech;

import java.io.IOException;

import game.GAME;
import init.paths.PATHS.ResFolder;
import snake2d.util.file.Json;
import snake2d.util.sets.LISTE;

public class TechTree {

	public final static int MAX_COLS = 7;
	
	public final TECH[][] nodes;
	public final String key;
	public final CharSequence name;
	
	TechTree(ResFolder root, String key, LISTE<TECH> all) throws IOException{
		this.key = key.toUpperCase();
		root = root.folder(key);
		name = new Json(root.text.get("TREE")).text("NAME");
		
		Json rows = new Json(root.init.get("TREE")).json("TREE");
		
		
		
		nodes = new TECH[rows.keys().size()][];
		int ri = 0;
		
		ResFolder pnodes = root.folder("nodes");

		for (String __ : rows.keys()) {
			String[] values = rows.values(__);
			if (values.length > MAX_COLS)
				rows.error("Max columns in a row must be 7 to fit on smaller screeens", key);
			nodes[ri] = new TECH[values.length];
			
			for (int ci = 0; ci < values.length; ci++) {
				String v = values[ci];
				if (v.equals("_____"))
					continue;
				if (!pnodes.init.exists(v))
					GAME.Warn(rows.errorGet("there is no tech in the nodes folder named: " + v, v));
				else {
					TECH t = new TECH(this.key + "_" + v, all, new Json(pnodes.init.get(v)), new Json(pnodes.text.get(v)), this);
					nodes[ri][ci] = t;
				}
				
			}
			
			ri++;
			
		}
		
	}
	
}
