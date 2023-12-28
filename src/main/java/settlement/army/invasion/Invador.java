package settlement.army.invasion;

import java.io.IOException;

import game.GAME;
import game.Profiler;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import init.config.Config;
import init.paths.PATHS;
import init.race.Race;
import settlement.army.Div;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.HTYPE;
import settlement.entity.humanoid.Humanoid;
import settlement.main.CapitolArea;
import settlement.main.SETT;
import settlement.main.SETT.SettResource;
import settlement.room.military.artillery.ArtilleryInstance;
import settlement.room.military.artillery.ROOM_ARTILLERY;
import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.equip.EquipBattle;
import settlement.thing.projectiles.Projectile;
import snake2d.util.file.*;
import snake2d.util.misc.ACTION;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import view.sett.IDebugPanelSett;
import world.army.AD;
import world.army.WDivGeneration;
import world.army.util.DIV_STATS;
import world.army.util.DivSpec;

public final class Invador extends SettResource{

	private ArrayList<Invasion> active = new ArrayList<>(16);
	private int wins;
	final Projectile proj;
	
	
	public Invador() throws IOException {
		
		IDebugPanelSett.add("Invade Small", new ACTION() {
			@Override
			public void exe() {
				invade(20, 0.2);
			}
		});
		
		IDebugPanelSett.add("Invade Medium", new ACTION() {
			@Override
			public void exe() {
				invade(200, 0.2);
			}
		});
		
		IDebugPanelSett.add("Invade Huge", new ACTION() {
			@Override
			public void exe() {
				invade(2000, 0.2);
			}
		});
		proj = new Projectile.ProjectileImp(new Json(PATHS.CONFIG().get("DefaultProjectile")));
	}
	
	public void invade(int amount, double quality) {
		
		if (active.size() == 0) {
			for (Div d : SETT.ARMIES().enemy().divisions())
				d.morale.init();
			SETT.ARMIES().enemy().resetMorale();
		}
		
		if (!active.hasRoom())
			return;
		
		
		int menPerDivision = amount/Config.BATTLE.DIVISIONS_PER_ARMY;
		if (menPerDivision < 50)
			menPerDivision = 50;
		if (amount < menPerDivision)
			menPerDivision = amount;
		
		int divisions = amount/menPerDivision;
		
		Race race = FACTIONS.player().race();
		DivSpec spec = new DivSpec();
		WDivGeneration[] divs = new WDivGeneration[divisions];
		for (int i = 0; i < divs.length; i++) {
			spec.copy(AD.UTIL().types.rnd(race, FACTIONS.player(), RND.rFloat()),Math.pow(RND.rFloat(), 1.5), Math.pow(RND.rFloat(), 1.5));
			double experience = quality/2;
			divs[i] = make(menPerDivision, race, spec, experience);
		}
		
		active.add(new Invasion(divs, RND.rInt(1000), RND.rInt(1000), null));
		
	}
	
	
	private WDivGeneration make(int men, Race race, DivSpec spec, double ex) {

		DIV_STATS dd = new DIV_STATS() {
			
			@Override
			public double training(StatTraining tr) {
				return spec.training(tr);
			}
			
			@Override
			public double equip(EquipBattle e) {
				return spec.equip(e);
			}
			
			@Override
			public Race race() {
				return race;
			}
			
			@Override
			public int men() {
				return men;
			}
			
			@Override
			public Faction faction() {
				return null;
			}
			
			@Override
			public double experience() {
				return ex;
			}
		};
		return new WDivGeneration(dd, race.info.armyNames.rnd(), RND.rInt(SETT.ARMIES().banners.size()));
	}
	
	public void invade(int wx, int wy, WDivGeneration[] divs, FactionNPC faction) {
		active.add(new Invasion(divs, wx, wy, faction));
		
	}
	
	@Override
	protected void update(float ds, Profiler profiler) {
		
		int os = active.size();

		for (int ii = 0; ii < active.size(); ii++) {
			if (!active.get(ii).update(ds)) {
				if(active.get(ii).victory) {
					GAME.count().INVASIONS_WON.inc(1);
					wins ++;
				}else {
					GAME.count().INVASIONS_LOST.inc(1);
					wins --;
				}
				wins = Math.max(0, wins);
				active.remove(ii);
				ii--;
			}
		}
		
		if (os > 0 && active.size() == 0 ) {
			for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
				if (e instanceof Humanoid) {
					Humanoid h = (Humanoid) e;
					if (h.indu().hType() == HTYPE.ENEMY) {
						h.helloMyNameIsInigoMontoyaYouKilledMyFatherPrepareToDie();
					}
				}
			}
			
			for (ROOM_ARTILLERY b : SETT.ROOMS().ARTILLERY) {
				for (int i = 0; i < b.instancesSize(); i++) {
					ArtilleryInstance ins = b.getInstance(i);
					if (ins.army() == SETT.ARMIES().enemy()) {
						ins.destroyTile(ins.mX(), ins.mY());
						i--;
					}
				}
			}
		}
	}
	
	public boolean invading() {
		return active.size() > 0;
	}
	
	public void increaseWins() {
		wins++;
	}
	
	public void decreaseWins() {
		wins--;
	}
	
	public int wins() {
		return wins;
	}
	
	@Override
	protected void save(FilePutter file) {
		file.i(active.size());
		for (Invasion i : active) {
			i.save(file);
		}
		file.i(wins);
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		active.clear();
		int am = file.i();
		for (int i = 0; i < am; i++)
			active.add(new Invasion(file));
		wins = file.i();
	}
	
	@Override
	protected void clearBeforeGeneration(CapitolArea area) {
		active.clear();
		wins = 0;
	}
	
	@Override
	protected void generate(CapitolArea area) {
		active.clear();
		wins = 0;
	}

	
}
