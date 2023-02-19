package settlement.army;

import java.io.IOException;

import game.faction.FACTIONS;
import init.race.RACES;
import init.race.Race;
import settlement.main.SETT;
import snake2d.util.file.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.text.Str;
import util.data.GETTER.GETTERE;
import util.data.INT.INTE;
import util.dic.DicArmy;

public final class DivInfo{
	
	private final Str name = new Str(32);
	private final Div div;
	private int menTarget;
	private int raceI;
	private int exMin;
	private int symbolI;
	
	DivInfo(Div div){
		this.div = div;
		raceI = 0;
		symbolI = div.index();
		name.clear().add(DicArmy.¤¤Division).add(' ').add('#').add(div.index());
	}
	
	public Div div() {
		return div;
	}
	
	public int target() {
		return menTarget;
	}
	
//	public int trainingMin() {
//		return trainingI*2 + 1;
//	}
//	
//	public int trainingMax() {
//		return trainingI*4 + 2;
//	}
//	
//	public int trainingRangeMin() {
//		return trainingRI * 2;
//	}
//	
//	public int trainingRangeMax() {
//		return trainingRI * 4 + 2;
//	}
	
	public Race race() {
		return RACES.all().get(raceI);
	}
	
	public int symbolI() {
		return symbolI;
	}
	
	public void symbolSet(int i) {
		symbolI = i;
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
	
	public final Training training = new Training() {
		
		@Override
		public double toStat() {
			if (get() == 0 && trainingR.get() > 0) {
				return 0;
			}
			return super.toStat();
		};
		
	};
	
	public final Training trainingR = new Training() {
		@Override
		public double toStat() {
			if (get() == 0 && training.get() > 0) {
				return 0;
			}
			return super.toStat();
		};
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
			file.i(training.t);
			file.i(trainingR.t);
			file.i(menTarget);
			file.i(raceI);
			file.i(exMin);
			file.i(symbolI);
			name.save(file);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			training.t = file.i();
			trainingR.t = file.i();
			menTarget = file.i();
			raceI = file.i();
			exMin = file.i();
			symbolI = file.i();
			name.load(file);
		}
		
		@Override
		public void clear() {
			training.t = 0;
			trainingR.t = 0;
			menTarget = 0;
			raceI = FACTIONS.player().race().index;
			exMin = 0;
			symbolI = div.index();
			name.clear().add(DicArmy.¤¤Division).add(' ').add('#').add(div.index());
		}
	};
	
	public static class Training implements INTE {
		
		private int t = 0;
		private final static double[] stat = new double[] {0.1, 0.25, 0.5, 0.75, 1.0};

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