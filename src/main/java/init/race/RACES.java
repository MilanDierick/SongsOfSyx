package init.race;

import java.io.IOException;

import init.D;
import init.paths.PATH;
import init.paths.PATHS;
import init.race.appearence.RaceSprites;
import init.resources.*;
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
	static RacePriorities prios;
	
	private static CharSequence ¤¤name = "¤Species";
	
	private LIST<LIST<RES_AMOUNT>> homeres;
	private LIST<RES_AMOUNT> homeresAll;
	
	private RaceSprites sprites;
	
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
		
	}
	
	public static void expand() throws IOException {
		
		ExpandInit init = new ExpandInit();
		
		for (Race r : i.all) {
			r.expand(init);
		}
		
		ArrayList<LIST<RES_AMOUNT>> ress = new ArrayList<>(HCLASS.ALL().size());
		RES_AMOUNT.Imp[] all = new RES_AMOUNT.Imp[RESOURCES.ALL().size()];
		
		for (HCLASS c : HCLASS.ALL) {
			ArrayList<RES_AMOUNT> rr = new ArrayList<>(RESOURCES.ALL().size());
			for (RESOURCE res : RESOURCES.ALL()) {
				int am = 0;
				for (Race r : RACES.all()) {
					am = Math.max(am, r.home().clas(c).amount(res));
				}
				if (am > 0) {
					rr.add(new RES_AMOUNT.Abs(res, am));
					if (all[res.index()] != null) {
						all[res.index()].set(Math.max(all[res.index()].amount(), am));
					}else {
						all[res.index()] = new RES_AMOUNT.Imp(res, am);
					}
				}
			}
			ress.add(new ArrayList<RES_AMOUNT>(rr));
		}
		i.homeres = ress;
		
		LinkedList<RES_AMOUNT> tm = new LinkedList<>();
		for (RES_AMOUNT a : all) {
			if (a != null)
				tm.add(a);
		}
		
		i.homeresAll = new ArrayList<RES_AMOUNT>(tm);
		
		i.sprites = new RaceSprites();
		
		prios = new RacePriorities();
	}
	
	public static LIST<RES_AMOUNT> homeResMax(HCLASS c){
		if (c == null)
			return i.homeresAll;
		return i.homeres.get(c.index());
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
	
	public static RacePriorities bonus() {
		return prios;
	}
	
	
}
