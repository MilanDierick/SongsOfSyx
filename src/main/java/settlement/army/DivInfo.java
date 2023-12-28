package settlement.army;

import java.io.IOException;

import game.faction.FACTIONS;
import game.faction.Faction;
import init.race.RACES;
import init.race.Race;
import settlement.army.DivisionBanners.DivisionBanner;
import settlement.main.SETT;
import settlement.room.military.training.ROOM_M_TRAINER;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.equip.EquipBattle;
import snake2d.util.file.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.text.Str;
import util.data.DOUBLE.DoubleImp;
import util.data.GETTER.GETTERE;
import util.data.INT.INTE;
import util.dic.DicArmy;
import world.army.util.DIV_STATS;

public final class DivInfo implements DIV_STATS{
	
	private final Str name = new Str(32);
	private final Div div;
	private int menTarget;
	private int raceI;
	private int exMin;
	private int symbolI;
	private final DoubleImp[] trains = new DoubleImp[ROOM_M_TRAINER.ALL().size()];
	
	DivInfo(Div div){
		this.div = div;
		raceI = 0;
		symbolI = div.index();
		name.clear().add(DicArmy.¤¤Division).add(' ').add('#').add(div.index());
		for (int i = 0; i < trains.length; i++)
			trains[i] = new DoubleImp();
	}
	
	public Div div() {
		return div;
	}
	
	@Override
	public int men() {
		return menTarget;
	}
	
	@Override
	public double equip(EquipBattle e) {
		return (double)e.target(div)/e.equipMax;
	}
	
	@Override
	public double training(StatTraining e) {
		return trainingD(e.room).getD();
	}
	
	@Override
	public double experience() {
		return STATS.BATTLE().COMBAT_EXPERIENCE.div().getD(div);
	}
	
	@Override
	public Faction faction() {
		return FACTIONS.player();
	}
	
	public DoubleImp trainingD(ROOM_M_TRAINER<?> room) {
		return trains[room.INDEX_TRAINING];
	}
	
	@Override
	public Race race() {
		return RACES.all().get(raceI);
	}
	
	public int symbolI() {
		return symbolI;
	}
	
	public void symbolSet(int i) {
		symbolI = i;
	}
	
	public DivisionBanner banner() {
		return SETT.ARMIES().banners.get(symbolI);
	}
	
	public Str name() {
		return name;
	}
	
	public final GETTERE<Race> race = new GETTERE<Race>() {

		@Override
		public Race get() {
			return RACES.all().get(raceI);
		}

		@Override
		public void set(Race t) {
			int i = men.get();
			men.set(0);
			raceI = t.index;
			men.set(i);
		}
	
	};
	
	public final INTE experienceT = new INTE() {
		
		@Override
		public void set(int t) {

			exMin = CLAMP.i(t, min(), max());

		}
		
		@Override
		public int min() {
			return 0;
		}
		
		@Override
		public int max() {
			return 15;
		}
		
		@Override
		public int get() {
			return exMin;
		}
	};
	
	public final INTE men = new INTE() {
		
		@Override
		public void set(int t) {
			if (div.army() == SETT.ARMIES().player()) {
				SETT.ARMIES().info.incTarget(race.get(), -get());
				menTarget = CLAMP.i(t, min(), max());
				SETT.ARMIES().info.incTarget(race.get(), +get());
			}
		}
		
		@Override
		public int min() {
			return 0;
		}
		
		@Override
		public int max() {
			return div.men().freeSpots() + div.menNrOf();
		}
		
		@Override
		public int get() {
			return menTarget;
		}
	};

	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			for (DoubleImp t : trains) {
				t.save(file);
			}
				
			
			file.i(menTarget);
			file.i(raceI);
			file.i(exMin);
			file.i(symbolI);
			name.save(file);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			for (DoubleImp t : trains) {
				t.load(file);
			}
			menTarget = file.i();
			raceI = file.i();
			exMin = file.i();
			symbolI = file.i();
			name.load(file);
		}
		
		@Override
		public void clear() {
			for (DoubleImp t : trains) {
				t.setD(0);
			}
			menTarget = 0;
			raceI = FACTIONS.player().race().index;
			exMin = 0;
			symbolI = div.index();
			name.clear().add(DicArmy.¤¤Division).add(' ').add('#').add(div.index());
		}
	};
	
	public static class Training implements INTE {
		
		private int t = 0;
		private final static double[] stat = new double[] {0, 0.25, 0.5, 0.75, 1.0};

		@Override
		public int get() {
			return t;
		}

		@Override
		public int min() {
			return 0;
		}

		@Override
		public int max() {
			return 4;
		}

		@Override
		public void set(int t) {
			this.t = CLAMP.i(t, 0, 4);
		}
		
		public double toStat() {
			return stat[t];
		}
		
	}


	
}