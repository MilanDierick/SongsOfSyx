package settlement.invasion;

import java.io.IOException;

import game.faction.FACTIONS;
import game.faction.Faction;
import init.RES;
import settlement.army.Div;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.HTYPE;
import settlement.entity.humanoid.Humanoid;
import settlement.main.CapitolArea;
import settlement.main.SETT;
import settlement.main.SETT.SettResource;
import settlement.room.military.artillery.ArtilleryInstance;
import settlement.room.military.artillery.ROOM_ARTILLERY;
import settlement.stats.STATS;
import settlement.stats.StatsEquippables.EQUIPPABLE_MILITARY;
import settlement.stats.StatsEquippables.StatEquippableRange;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import view.sett.IDebugPanelSett;

public final class Invador extends SettResource{

	private ArrayList<Invasion> active = new ArrayList<>(16);
	private int wins;
	
	public Invador() {
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
	}
	
	public void invade(int amount, double quality) {
		
		if (active.size() == 0) {
			for (Div d : SETT.ARMIES().enemy().divisions())
				d.morale.init();
			SETT.ARMIES().enemy().initMorale();
		}
		
		if (!active.hasRoom())
			return;
		
		int menPerDivision = amount/RES.config().BATTLE.DIVISIONS_PER_ARMY;
		if (menPerDivision < 50)
			menPerDivision = 50;
		if (amount < menPerDivision)
			menPerDivision = amount;
		
		int divisions = amount/menPerDivision;
		
		InvadorDiv[] divs = new InvadorDiv[divisions];
		for (int i = 0; i < divs.length; i++) {
			InvadorDiv d = new InvadorDiv();
			d.race = FACTIONS.player().race();
			d.men = menPerDivision;
			
			boolean ranged = RND.oneIn(5);
			
			for (EQUIPPABLE_MILITARY m : STATS.EQUIP().military()) {
				d.equipment[m.indexMilitary()] = (byte) (Math.pow(RND.rFloat(), 1.5)*m.max());
			}
			
			if (ranged) {
				StatEquippableRange a = STATS.EQUIP().ammo().rnd(); 
				d.equipment[a.indexMilitary()] = (byte) (1 + (Math.pow(RND.rFloat(), 1.5)*(a.max()-1)));
				d.trainingR = quality;
			}else
				d.trainingM = quality;
			d.experience = quality/2;
			divs[i] = d;
		}
		
		active.add(new Invasion(divs, DIR.ORTHO.rnd(), null));
		
	}
	
	public void invade(DIR d, InvadorDiv[] divs, Faction faction) {
		active.add(new Invasion(divs, d, faction));
		
	}
	
	@Override
	protected void update(float ds) {
		
		int os = active.size();

		for (int ii = 0; ii < active.size(); ii++) {
			if (!active.get(ii).update(ds)) {
				if(active.get(ii).victory) {
					wins ++;
				}else
					wins --;
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
