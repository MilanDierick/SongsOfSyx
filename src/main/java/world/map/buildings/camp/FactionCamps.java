package world.map.buildings.camp;

import java.io.IOException;
import java.util.Arrays;

import game.faction.FACTIONS;
import game.faction.Faction;
import init.D;
import init.race.RACES;
import init.race.Race;
import snake2d.util.file.*;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.text.Str;
import view.ui.message.MessageText;
import world.WORLD;
import world.regions.Region;
import world.regions.data.RD;

public final class FactionCamps {

	private final RaceData[][] data;
	private final WCampType[][] racemap = new WCampType[RACES.all().size()][];
	
	private byte[] playerHas;
	private static CharSequence ¤¤titleJoin = "Havens join your cause.";
	private static CharSequence ¤¤bodyJoin = "The {0} havens, which is in your kingdom, have decided to join your cause. The inhabitants will now start immigrating to your capital at a steady pace. Make sure you allow them in and treat them right, else they will leave again.";
	private static CharSequence ¤¤titleLeave = "Havens quit corporation!.";
	private static CharSequence ¤¤bodyLeave = "Since you've failed to uphold the standards of your {0} havens, they have now stopped supporting you, and its members will start to return home.";
	private final Str s = Str.TMP;
	private double playerCheck = -60;
	
	static {
		D.ts(FactionCamps.class);
	}
	
	FactionCamps(LIST<WCampType> types) {

		
		data = new RaceData[FACTIONS.MAX][types.size()];
		for (Race race : RACES.all()) {
			int am = 0;
			for (WCampType t : types)
				if (t.race == race) {
					am++;
				}
			racemap[race.index()] = new WCampType[am];
			for (WCampType t : types)
				if (t.race == race) {
					racemap[race.index][--am] = t;
				}
				
		}
		
		for (int i = 0; i < data.length; i++) {
			RaceData[] dd = new RaceData[types.size()];
			for (int k = 0;  k < dd.length; k++)
				dd[k] = new RaceData();
			data[i] = dd;
		}
		playerHas = new byte[types.size()];
		
		new RD.RDOwnerChanger() {
			
			@Override
			public void change(Region reg, Faction oldOwner, Faction newOwner) {
				
				if (oldOwner != null || newOwner != null) {
					init(WORLD.BUILDINGS().camp.all());
				}
			}
		};
		
	}
	
	void init(LIST<WCampInstance> all) {
		for (RaceData[] ds : data)
			for (RaceData d : ds)
				d.clear();
		for (WCampInstance ii : all) {
			if (ii == null)
				continue;
			Region reg = ii.region();
			if (reg != null && reg.faction() != null) {
				data[reg.faction().index()][ii.type().index()].add(ii);
			}
		}
		
	}
	
	void update(float ds) {
		playerCheck += ds;
		if (playerCheck >= 0) {
			playerCheck -= 60;
			
			for (WCampType t : WORLD.camps().types) {
				
				int am = max(FACTIONS.player(), t);
				if (am == 0) {
					playerHas[t.index()] = 0;
					continue;
				}
				if (playerHas[t.index()] == 0 && t.reqs.passes(null)) {
					playerHas[t.index()] = 1;
					s.clear().add(¤¤bodyJoin).insert(0, t.race.info.namePosessive);
					new MessageText(¤¤titleJoin, s).send();
				}else if (playerHas[t.index()] == 1 && !t.reqs.passes(null)) {
					playerHas[t.index()] = 0;
					s.clear().add(¤¤bodyLeave).insert(0, t.race.info.namePosessive);
					new MessageText(¤¤titleLeave, s).send();
				}
			}
			
		}
	}
	
	public int max(Faction f, Race type) {
		int am = 0;
		for (WCampType t : racemap[type.index()])
			if (f == FACTIONS.player() && playerHas[t.index()] == 0)
				;
			else
				am += max(f, t);
		return am;
	}
	
	public double replenishPerDay(Faction f, Race type) {
		double am = 0;
		for (WCampType t : racemap[type.index()])
			if (f == FACTIONS.player() && playerHas[t.index()] == 0)
				;
			else
				am += replenishPerDay(f, t);
		
		return am;
	}
	
	public int max(Faction f, WCampType type) {
		return data[f.index()][type.index()].pop;
	}
	
	public double replenishPerDay(Faction f, WCampType type) {
		return data[f.index()][type.index()].replenish;
	}
	
	public int camps(Faction f, WCampType type) {
		return data[f.index()][type.index()].camps;
	}
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			file.bsE(playerHas);
			
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			file.bsE(playerHas);
			
		}
		
		@Override
		public void clear() {
			Arrays.fill(playerHas, (byte)0);
		}
	};
	
	private static class RaceData {
		
		private int camps;
		private int pop;
		private double replenish;
		public void clear() {
			camps = 0;
			pop = 0;
			replenish = 0;
		}
		public void add(WCampInstance ii) {
			camps += 1;
			pop += ii.max;
			replenish += ii.replenishRateDay;
		}
	}
	
}
