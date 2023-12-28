package settlement.entry;

import java.io.IOException;

import game.GAME;
import game.boosting.BOOSTABLES;
import game.faction.FACTIONS;
import game.time.TIME;
import init.D;
import init.race.RACES;
import init.race.Race;
import settlement.entity.ENTETIES;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.*;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.standing.STANDINGS;
import settlement.stats.standing.StandingCitizen;
import snake2d.util.file.*;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.misc.CLAMP;
import util.data.INT.INTE;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import util.updating.IUpdater;
import world.WORLD;

public class Immigration {

	public static final int MAX_POPULATION = ENTETIES.MAX - 5000;
	
	private final Immigrator[] imms = new Immigrator[RACES.all().size()]; 
	
	boolean killall = false;
	private int killAllI = 0;
	
	Immigration() {
		
		for (Race r : RACES.all())
			imms[r.index] = new Immigrator(r);
		
	}
	
	void update(float ds) {
		
		
		
		if (killall) {
			
			for (int k = 0; k < 10; k++) {
				int i = killAllI;
				ENTITY e = SETT.ENTITIES().getAllEnts()[i];
				if (e != null && e instanceof Humanoid) {
					Humanoid a = (Humanoid) e;
					SETT.THINGS().gore.cloud(SETT.ENTITIES().getAllEnts()[i], a.race().appearance().colors.blood);
					SETT.THINGS().gore.explode(SETT.ENTITIES().getAllEnts()[i], a.race().appearance().colors.blood);
					SETT.ENTITIES().getAllEnts()[i].helloMyNameIsInigoMontoyaYouKilledMyFatherPrepareToDie();
				}
				killAllI++;
				if (killAllI >= SETT.ENTITIES().getAllEnts().length)
					killAllI = 0;
			}
			return;
		}
	
		updater.update(ds);
		
	}
	
	private final IUpdater updater = new IUpdater(imms.length, 10) {
		
		@Override
		protected void update(int i, double timeSinceLast) {
			imms[i].update(timeSinceLast);
		}
	};
	
	public int admitted(Race race) {
		return SETT.ENTRY().onTheirWay(race, HTYPE.SUBJECT);
	}
	
	public int wanted(Race race) {
		if (race == null) {
			int im = 0;
			for (int ri = 0; ri < RACES.all().size(); ri++) {
				Race r = RACES.all().get(ri);
				im+= wanted(r);
			}
			return CLAMP.i(im, 0, im);
		}
		if (GAME.events().riot.shouldEmigrate(race))
			return 0;
		
		return CLAMP.i(imms[race.index].wanted()-SETT.ENTRY().onTheirWay(race, HTYPE.SUBJECT), 0, 1000);
	}
	
	public final INTE auto(Race race) {
		return imms[race.index].auto;
	}
	
	public void admit(Race race, int amount) {
		SETT.ENTRY().add(race, HTYPE.SUBJECT, amount);
		imms[race.index].timer = CLAMP.d(imms[race.index].timer-amount, 0, imms[race.index].timer);
	}
	
	public int maxPop(Race race) {
		return (int) (MAX_POPULATION*race.population().max);
	}
	
	public double secondsTillNext(Race race) {
		return imms[race.index].secondsTillNext();
	}
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			updater.save(file);
			for (Immigrator i : imms)
				i.save(file);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			updater.load(file);
			for (Immigrator i : imms)
				i.load(file);
		}
		
		@Override
		public void clear() {
			for (Immigrator i : imms)
				i.clear();
			killall = false;
			killAllI = 0;
		}
	};
	
	public boolean shouldEmmigrate(Race race) {
		return imms[race.index].shouldEmmigrate();
	}


	private static final class Immigrator implements SAVABLE{

		private static double rate = 1.0/TIME.secondsPerDay;
		
		private final Race race;
		private int autoAdmit = 0;
		private double timer = 0;
		private double emmigrants = 0;
		
		Immigrator(Race race) {
			this.race = race;
			if (race.population().max == 0)
				autoAdmit = ENTETIES.MAX;
		}
		
		private int wantedUltimately() {

			return getImmigrants(race)-SETT.ENTRY().onTheirWay(race, HTYPE.SUBJECT);
		}
		
		private double speed(int wanted) {
			if (wanted <= 0)
				return 0;
		
			
			double speed = rate;
			if (WORLD.BUILDINGS().camp.available(race)) {
				speed *= WORLD.BUILDINGS().camp.factions.replenishPerDay(FACTIONS.player(), race);
			}else {
				speed*= BOOSTABLES.CIVICS().IMMIGRATION.get(RACES.clP());
				double boost = (8*(1.0-CLAMP.i(wanted, 0, 1000)/1000.0));
				speed *= (1.0 + boost)*race.population().immigrantsPerDay*race.population().climate(SETT.ENV().climate());
			}
			
			return speed;
		}
		
		public double secondsTillNext() {
			double rem = 1.0 - (timer-(int)timer);
			double speed = speed(wantedUltimately());
			if (speed == 0)
				return Double.NaN;
			return rem/speed;
		}

		
		void update(double ds) {
		
			int wanted = wantedUltimately();
			if (wanted < 0) {
				emmigrants += -ds*wanted/(2.0*TIME.secondsPerDay);
				timer = 0;
				return;
			}
			emmigrants = 0;
			
			
			timer += speed(wanted)*ds;
			
			timer = CLAMP.d(timer, 0, wanted);
			
			int a = (int) (auto.get());
			a -= STATS.POP().POP.data(HCLASS.CITIZEN).get(race) + STATS.POP().POP.data(HCLASS.CHILD).get(race);
			
			int w =  wanted()-SETT.ENTRY().onTheirWay(race, HTYPE.SUBJECT);
			
			if (a > 0 && w > 0) {
				int am = CLAMP.i(w, 0, a);
				SETT.ENTRY().add(race, HTYPE.SUBJECT, am);
				timer -= am;
			}

		}
		
		public boolean shouldEmmigrate() {
			if (emmigrants > 1) {
				emmigrants --;
				return true;
			}
			return false;
		}


		@Override
		public void save(FilePutter file) {
			file.d(timer);
			file.i(autoAdmit);
			file.d(emmigrants);
		}

		@Override
		public void load(FileGetter file) throws IOException {
			timer = file.d();
			autoAdmit = file.i();
			emmigrants = file.d();
		}
		
		@Override
		public void clear() {
			timer = 0;
			autoAdmit = 0;
			if (race.population().max == 0)
				autoAdmit = ENTETIES.MAX;
		}
		
		public int wanted() {
			return CLAMP.i((int)timer, 0, wantedUltimately());
		}
		
		public final INTE auto = new INTE() {

			@Override
			public int get() {
				return autoAdmit;
			}

			@Override
			public int min() {
				return 0;
			}

			@Override
			public int max() {
				return ENTETIES.MAX;
			}

			@Override
			public void set(int t) {
				autoAdmit = t;
			}
		};
		
		


		
	}
	
	private static int getImmigrants(Race r) {
		
		
		HCLASS cl = HCLASS.CITIZEN;
		double pop = STATS.POP().POP.data(cl).get(r, 0)+WORLD.ARMIES().cityDivs().total(r);
		if (pop == 0) {
			double hap = BOOSTABLES.BEHAVIOUR().HAPPI.get(RACES.clP(r, HCLASS.CITIZEN));
			
			return (int) hap;
		}
		
		{
			double ful = STANDINGS.CITIZEN().fullfillment.getD(r);
			double exp = StandingCitizen.expectation(r, pop, STATS.POP().POP.data(cl).get(null, 0)-pop);
			double hap = ful/exp;
			hap*= BOOSTABLES.BEHAVIOUR().HAPPI.get(RACES.clP(r, HCLASS.CITIZEN));
			hap = CLAMP.d(hap, 0, 2);

			hap -= threshold; 
			if (hap <= 0)
				return (int) (pop*hap/threshold);
			hap*= 0.5;
			double am = hap*r.population().max*pop;
			if (am > 1) {
				double d = am / (am+pop);
				am = am*(1-d) + d*Math.pow(am, 1/STANDINGS.CITIZEN().fullPow(r));
			}
			
			int res = (int) Math.ceil(am);
			
			if (WORLD.BUILDINGS().camp.available(r)) {
				res = CLAMP.i(res, 0, WORLD.BUILDINGS().camp.factions.max(FACTIONS.player(), r));
			}
			
			return (int) res;
		}

	}
	
	private static double threshold = 0.8;
	private static CharSequence ¤¤immigrants = "Immigrants";
	private static CharSequence ¤¤camp = "Havens Max";
	private static CharSequence ¤¤immigrantD = "Immigrants are subjects from either your regional population, or camps that have joined your cause. These subjects will be attracted by your current happiness. Happiness above 80% will attract immigrants at an increasing rate.";
	private static CharSequence ¤¤admitted = "Admitted";

	static {
		D.ts(Immigration.class);
	}
	
	public void hoverImmigrants(GUI_BOX box, Race r) {
		if (r == null)
			return;
		GBox b = (GBox) box;
		StandingCitizen st = STANDINGS.CITIZEN();
		
		b.textLL(¤¤immigrants);
		b.tab(7);
		b.add(GFORMAT.iBig(b.text(), wanted(r)));
		b.NL();
		
		b.text(¤¤immigrantD);
		b.NL(4);
		
		if (WORLD.BUILDINGS().camp.available(r)) {
			b.textL(¤¤camp);
			b.tab(7);
			b.add(GFORMAT.iBig(b.text(), WORLD.BUILDINGS().camp.factions.max(FACTIONS.player(), r)));
			b.NL();
		}
		
		
		b.textL(¤¤admitted);
		b.tab(7);
		b.add(GFORMAT.iBig(b.text(), admitted(r)));
		b.NL();
		
		b.textL(st.happiness.info().name);
		b.tab(7);
		b.add(GFORMAT.perc(b.text(), st.happiness.getD(r)));
		
		b.NL(8);
		
		
	}

	
}
