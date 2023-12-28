package init.race;

import java.io.IOException;

import init.D;
import init.paths.PATH;
import init.paths.PATHS;
import init.race.appearence.RaceSprites;
import settlement.entity.humanoid.HCLASS;
import snake2d.Errors;
import snake2d.util.file.Json;
import snake2d.util.sets.*;
import util.keymap.RCollection;

public class RACES {

	private static RACES i;
	
	private final ArrayList<Race> all;
	private final ArrayList<Race> playable;
	private final RCollection<Race> map;
	private RaceServiceSorter service;
	
	private static CharSequence ¤¤name = "¤Species";
	
	private RaceSprites sprites;
	private final RaceBoosts boosts;
	private final RClasses rclasses;
	private RaceResources resources;
	static {
		D.ts(RACES.class);
	}
	
	public RACES() {
		
		i = this;
		PATH p = PATHS.INIT().getFolder("race");
		PATH pt = PATHS.TEXT().getFolder("race");
		String[] files = p.getFiles();
		all = new ArrayList<Race>(files.length);
		
		
		if (files.length == 0) {
			throw new Errors.DataError("no races defined!", p.get());
		}
		
		for (String s : files) {
			new Race(s, new Json(p.get(s)), new Json(pt.get(s)), all);
		}
		final KeyMap<Race> map = new KeyMap<>();
		int pl = 0;
		for (Race r : all) {
			if (r.playable)
				pl++;
			map.put(r.key, r);
		}
		this.rclasses = new RClasses(all);
		this.map = new RCollection<Race>("RACES", map) {

			@Override
			public Race getAt(int index) {
				return all.get(index);
			}

			@Override
			public LIST<Race> all() {
				return all;
			}
			
		};
		
		
		playable = new ArrayList<>(pl);
		for (Race r : all) {
			if (r.playable)
				playable.add(r);
			map.put(r.key, r);
		}

		EGROUP.init();
		boosts = new RaceBoosts();
		
		
	}
	
	public static void expand() throws IOException {
		
		ExpandInit init = new ExpandInit();
		
		for (Race r : i.all) {
			r.expand(init);
		}
		RacePreferrence.init();
		
		i.sprites = new RaceSprites();
		i.resources = new RaceResources(i.all);
	}
	
	public static RaceResources res(){
		return i.resources;
	}
	
	public static LIST<Race> all(){
		return i.all;
	}
	
	public static LIST<Race> playable(){
		return i.playable;
	}
	
	public static RCollection<Race> map(){
		return i.map;
	}
	
	public static RaceServiceSorter SERVICE() {
		return i.service;
	}
	
	public static CharSequence name() {
		return ¤¤name;
	}
	
	public static RaceSprites sprites() {
		return i.sprites;
	}
	
	public static RaceBoosts boosts() {
		return i.boosts;
	}
	

	public static POP_CL clP(HCLASS clas) {

		int ci = clas == null ? 0 : clas.index()+1;
		int ri = 0;
		return  i.rclasses.classes[ci][ri];
	}
	
	public static POP_CL clP(Race race) {
		int ci = 0;
		int ri = race == null ? 0 : race.index+1;
		return  i.rclasses.classes[ci][ri];
	}
	
	public static POP_CL clP() {
		return  i.rclasses.classes[0][0];
	}
	
	public static POP_CL clP(Race race, HCLASS clas) {
		
		
		int ci = clas == null ? 0 : clas.index()+1;
		int ri = race == null ? 0 : race.index+1;
		return  i.rclasses.classes[ci][ri];
	}
	
	public static LIST<POP_CL> cls() {
		return i.rclasses.all;
	}
	
	public final static class RClasses {
		
		private final POP_CL[][] classes;
		private final ArrayList<POP_CL> all;
		
		RClasses(LIST<Race> all) {
			
			this.all = new ArrayList<POP_CL>((all.size()+1)*(all.size()+1));
			classes = new POP_CL[all.size()+1][all.size()+1];
			
			
			classes[0][0] = this.all.addReturn(new POP_CL(this.all.size(), null, null));
			for (Race r : all) {
				classes[0][r.index+1] = this.all.addReturn(new POP_CL(this.all.size(), null, r));
			}
			
			for (HCLASS cl : HCLASS.ALL) {
				classes[cl.index()+1][0] = this.all.addReturn(new POP_CL(this.all.size(), cl, null));
				for (Race r : all) {
					classes[cl.index()+1][r.index+1] = this.all.addReturn(new POP_CL(this.all.size(), cl, r));
				}
			}
		}
		
	}
	
}
