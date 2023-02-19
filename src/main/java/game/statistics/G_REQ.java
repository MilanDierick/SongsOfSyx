package game.statistics;

import game.GAME;
import game.statistics.GCOUNTS.SAccumilator;
import game.statistics.GRequirementImp.*;
import init.race.Race;
import init.resources.RESOURCE;
import settlement.entity.humanoid.HCLASS;
import settlement.main.SETT;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.RoomsJson;
import settlement.room.spirit.temple.ROOM_TEMPLE;
import settlement.stats.*;
import settlement.stats.StatsMultipliers.StatMultiplier;
import snake2d.util.color.ColorImp;
import snake2d.util.file.Json;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.*;
import util.colors.GCOLOR;
import util.dic.DicMisc;
import util.dic.DicRes;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.keymap.KEY_COLLECTION;

public abstract class G_REQ {
	
	public G_REQ(){

	}
	
	public abstract boolean isInt();
	public abstract CharSequence name();
	public abstract int value();
	public abstract int target();
	public abstract boolean isAnti();
	public final boolean isFulfilled() {
		if (!isAnti())
			return value() >= target();
		return value() <= target();
				
	}
	public final double progress() {
		if (isAnti()) {
			if (value() == 0)
				return 1;
			return CLAMP.d(target()/value(), 0, 1);
		}
		return CLAMP.d((double)value()/target(), 0, 1);
	}
	public final boolean isSameBase(G_REQ r) {
		return key().equalsIgnoreCase(r.key());
	}
	protected abstract String key();
	

	
	public static LIST<G_REQ> READ(Json json){
		
		LinkedList<G_REQ> li = new LinkedList<>();
		
		json = json.json("REQUIREMENTS");
		
		if (json.has("COUNT")){
			Json j = json.json("COUNT");
			for (String k : j.keys()) {
				SAccumilator s = GAME.stats().MAP.getByKeyWarn(k, j);
				if (s != null) {
					RData d = new RData(j.json(k));
					li.add(new GRequirementStatistics(s, d));
				}
			}
		}
		
		if (json.has("STATS")) {
			new StatsJson(json) {
				
				@Override
				public void doWithTheJson(StatCollection col, STAT s, Json j, String key) {
					RData d = new RData(j.json(key));
					li.add(new GRequirementStat(col, s, d));
				}
				
				@Override
				public void doWithMultiplier(StatMultiplier m, Json j, String key) {
					
					
				}
			};
		}
		
		new RoomsJson("RELIGION", json) {
			
			@Override
			public void doWithTheJson(RoomBlueprintImp pp, Json j, String key) {
				if (!(pp instanceof ROOM_TEMPLE)) {
					if (key != KEY_COLLECTION.WILDCARD)
						j.error(pp.key + "is not a temple room", pp.key);
				}else {
					ROOM_TEMPLE tt = (ROOM_TEMPLE) pp;
					RData d = new RData(j.json(key));
					li.add(new GRequirementRel(tt, d));
				}
				
			}
		};
		
		new RoomsJson("EMPLOYED", json) {
			
			@Override
			public void doWithTheJson(RoomBlueprintImp pp, Json j, String key) {
				if (!(pp.employment() == null)) {
					if (key != KEY_COLLECTION.WILDCARD)
						j.error(pp.key + "is not a room with employees", pp.key);
				}else {
					RData d = new RData(j.json(key));
					li.add(new GRequirementEmp(pp, d));
				}
				
			}
		};
		
		if (json.has("POPULATION")){
			for (Json j : json.jsons("POPULATION")) {
				RData d = new RData(j);
				li.add(new GRequirementPop(d));
			}
		}
		
		if (json.has("STORED")){
			for (Json j : json.jsons("STORED")) {
				RData d = new RData(j);
				li.add(new GRequirementStored(d));
			}
		}
		
		if (json.has("HAPPINESS")){
			for (Json j : json.jsons("HAPPINESS")) {
				RData d = new RData(j);
				li.add(new GHappiness(d));
			}
		}
		
		
		return new ArrayList<>(li);
	}
	
	public static void hover(LIST<G_REQ> ll, GUI_BOX bb){
		
		
		
		GBox b = (GBox) bb;
		b.textLL(DicMisc.¤¤Requirement);
		
		b.tab(9);
		b.textLL(DicMisc.¤¤Current);
		b.tab(12);
		b.textLL(DicMisc.¤¤Target);
		b.NL(2);
		
		for (G_REQ q : ll) {
			b.textL(q.name());
			
			GText v = b.text();
			GText t = b.text();
			
			if (q.isAnti())
				t.add('<').add('=').s();
			else
				t.add('>').add('=').s();
			
			if (q.isInt()) {
				GFORMAT.iBig(v, q.value());
				GFORMAT.iBig(t, q.target());
			}else {
				GFORMAT.perc(v, q.value()/100.0);
				GFORMAT.perc(t, q.target()/100.0);
			}
			t.normalify();
			
			if (q.progress() >= 1) {
				v.color(GCOLOR.T().IGREAT);
			}else
				v.color(ColorImp.TMP.interpolate(GCOLOR.T().IBAD, GCOLOR.T().IGOOD, q.progress()));
			
			b.tab(9);
			b.add(v);
			b.tab(12);
			b.add(t);

			b.NL();
		}
		
	}
	

	
	private static final class GRequirementRel extends GRequirementImp {
		

		private final Race race;
		private final HCLASS cl;
		private final ROOM_TEMPLE t;
		
		public GRequirementRel(ROOM_TEMPLE t, RData d) {
			super("RELIGION"+t.key, d,  nameGet(t, d));
			this.t = t;
			this.race = d.race;
			this.cl = d.clas;
		}
		
		private static CharSequence nameGet(ROOM_TEMPLE t, RData data) {
			HCLASS cl = data.clas;
			Race race = data.race;
			String n = ""+t.rel().info.name;
			CharSequence name;
			if (cl != null && race != null) {
				name = n + " (" + cl.names + "-" + race.info.names + ")";
			}else if (cl != null) {
				name = n + " (" + cl.names + ")";
			}else if (race != null) {
				name = n + " (" + race.info.names + ")";
			}else {
				name = n;
			}
			return name;
		}

		@Override
		public boolean isInt() {
			return false;
		}

		@Override
		public int value() {
			return (int) Math.ceil(t.rel().followers.data(cl).getD(race)*100);
		}
		
	}
	
	private static final class GRequirementStored extends GRequirementImp {
		

		private final RESOURCE res;
		
		public GRequirementStored(RData d) {
			super("STORED", d, DicRes.¤¤Stored + (d.resource == null ? "" : ": " + d.resource.names));
			res = d.resource;
		}
		
		@Override
		public int value() {
			return (int) SETT.ROOMS().STOCKPILE.tally().amountReservable(res);
		}
		
	}
	
}
