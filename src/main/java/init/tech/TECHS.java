package init.tech;

import java.io.IOException;
import java.util.Arrays;

import game.GAME;
import init.D;
import init.paths.PATHS.ResFolder;
import init.tech.TECH.TechRequirement;
import snake2d.Errors;
import snake2d.util.file.Json;
import snake2d.util.sets.*;
import util.info.INFO;

public final class TECHS {

	private static LIST<TECH> ALL;
	public static CharSequence ¤¤name = "Technology";
	public static CharSequence ¤¤desc = "Technologies unlocks various boosts and rooms";
	static {D.ts(TECHS.class);}
	private static final INFO info = new INFO(¤¤name, ¤¤desc);
	
	private static LIST<TechTree> trees;

	public static LIST<TechTree> TREES(){
		return trees;
	}
	
	public static LIST<TECH> ALL(){
		return ALL;
	}
	
	public static INFO INFO(){
		return info;
	}
	
	public TECHS() throws IOException{
		
		
		


		
		final KeyMap<TECH> map = new KeyMap<>();
		LinkedList<TECH> all = new LinkedList<>();
		
		{
			
			//ArrayListGrower<TECH> all = new ArrayListGrower<>();
			
			ArrayListGrower<TechTree> trees = new ArrayListGrower<>();
			ResFolder data = new ResFolder("tech", false);
			
			for (String k : data.init.folders()) {
				if (data.init.getFolder(k).exists("TREE")) {
					trees.add(new TechTree(data, k, all));
				}
				
			}
			TECHS.trees = trees;
		}
		
		
		

		ALL = new ArrayList<>(all);
		
		for (TECH t : ALL) {
			map.put(t.key, t);
		}
		
		for (TECH tech : ALL) {
			
			Json j = tech.requires;
			tech.requires = null;
			LinkedList<TechRequirement> needs = new LinkedList<>();
			if (j.has("REQUIRES_TECH_LEVEL")) {
				Json jj = j.json("REQUIRES_TECH_LEVEL");
				for (String k : jj.keys()) {
					String kk = k;
					if (!map.containsKey(k)) {
						if (tech.tree != null)
							k = tech.tree.key + "_" + k;

						if (!map.containsKey(k)) {
							GAME.Warn(jj.errorGet(k, "REQUIRES_TECH_LEVEL"));
							continue;
						}
					}
					TechRequirement t = new TechRequirement(map.get(k), jj.i(kk, 0, map.get(k).levelMax));
					needs.add(t);
				}
			}
			tech.set(new ArrayList<>(needs));
			tech.prune(new ArrayList<>(needs));
		}
		
		
//		for (String key : files) {
//			
//			Json[] js = null;
//			TECH[] techs = null;
//			
//			{
//				Json jj = new Json(gData.get(key));
//				
//				if (jj.has("TECHS") && jj.jsonsIs("TECHS")) {
//					js = jj.jsons("TECHS");
//					techs = new TECH[js.length];
//					for (int i = 0; i < js.length; i++)
//						techs[i] = map.get(key+i);
//					
//				}else {
//					js = new Json[] {jj};
//					techs = new TECH[] {map.get(key)};
//				}
//			}
//			
//			for (int i = 0; i < js.length; i++) {
//				Json j =  js[i];
//				TECH tech = techs[i];
//				LinkedList<TechRequirement> needs = new LinkedList<>();
//				if (j.has("REQUIRES_TECH_LEVEL")) {
//					Json jj = j.json("REQUIRES_TECH_LEVEL");
//					for (String k : jj.keys()) {
//						if (!map.containsKey(k)) {
//							GAME.Warn(jj.errorGet(k, "REQUIRES_TECH_LEVEL"));
//						}else {
//							TechRequirement t = new TechRequirement(map.get(k), jj.i(k, 0, map.get(k).levelMax));
//							needs.add(t);
//						}
//					}
//				}
//				
//				tech.set(new ArrayList<>(needs));
//				tech.prune(new ArrayList<>(needs));
//			}
//		}
		
		detectCycles();
	
		{
			int[] reqed = new int[ALL.size()];
			
			for (int i = 0; i < ALL.size(); i++) {
				TECH t = ALL.get(i);
				Arrays.fill(reqed, 0);
				fillRequirements(reqed, t);
				
				LinkedList<TechRequirement> needs = new LinkedList<>();
				for (int ri = 0; ri < ALL.size(); ri++) {
					if (reqed[ri] > 0) {
						TechRequirement tt = new TechRequirement(ALL.get(ri), reqed[ri]-1);
						needs.add(tt);
					}
				}
				
				t.set(new ArrayList<>(needs));
			}
			
			
		}
		
		
	}
	
	private void fillRequirements(int[] reqed, TECH t) {
		
		for (int i = 0; i < t.requires().size(); i++) {
			TechRequirement r = t.requires().get(i);
			reqed[r.tech.index()] = Math.max(reqed[r.tech.index()], r.level+1);
			fillRequirements(reqed, r.tech);	
		}
	}
	

	
	private void detectCycles() {
		
		boolean[] checked = new boolean[ALL.size()];
		
		for (int i = 0; i < ALL.size(); i++) {
			if (ALL.get(i).requires().size() == 0)
				continue;
			Arrays.fill(checked, false);
			detectCycles(ALL.get(i), checked);
		}
		
	}
	
	private void detectCycles(TECH tech, boolean[] checked) {
		
		checked[tech.index()] = true;
		
		for (int i = 0; i < tech.requires().size(); i++) {
			TECH t = tech.requires().get(i).tech;
			if (checked[t.index()])
				throw new Errors.DataError("tech: " + t.info.name + " has a cyclic requirement", "");
		}
		
		for (int i = 0; i < tech.requires().size(); i++) {
			TECH t = tech.requires().get(i).tech;
			detectCycles(t, Arrays.copyOf(checked, checked.length));
		}
		
	}
	


	
	
}
