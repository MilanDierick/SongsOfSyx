package settlement.stats.standing;

import java.io.IOException;
import java.util.Arrays;

import game.GAME;
import game.faction.FACTIONS;
import game.time.TIME;
import init.D;
import init.paths.PATHS;
import init.race.RACES;
import init.race.Race;
import settlement.entity.humanoid.HCLASS;
import settlement.entry.Immigration;
import settlement.main.SETT;
import settlement.stats.*;
import settlement.stats.StatsMultipliers.StatMultiplier;
import snake2d.util.file.*;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.info.INFO;
import util.statistics.HistoryInt;
import util.updating.IUpdater;
import view.sett.IDebugPanelSett;
import world.World;
import world.army.WARMYD;

public final class StandingCitizen extends Standing{

	
	private static final double MAX = Immigration.MAX_POPULATION;
	private static final HCLASS cl = HCLASS.CITIZEN;
	
	{D.t(this);}

	public final CitizenThing expectation = new Expectation();
	public final CitizenThing fullfillment = new Fulfillment();
	public final CitizenThing happiness = new Happiness();
	public final CitizenThing mainTarget = new MainTarget();
	public final CitizenThing main = new Main();
	private double fullPows[] = new double[RACES.all().size()];
	private double defs[] = new double[RACES.all().size()];
	private double maxes[] = new double[RACES.all().size()];
	
	private final double[] mains = new double[RACES.all().size()];
	
	private final double POW = new Json(PATHS.CONFIG().get("Sett")).d("HAPPINESS_EXPONENT");
	
	public final LIST<CitizenThing> factors = new ArrayList<CitizenThing>(
			new CitizenThing(STATS.EDUCATION().INDOCTRINATION.info()) {
				
				@Override
				double update(Race race, double ds) {
					return 1.0 + STATS.EDUCATION().INDOCTRINATION.data(cl).getD(race)*2;
				}
			},
			new CitizenThing(STATS.EDUCATION().EDUCATION.info()) {
				
				@Override
				double update(Race race, double ds) {
					return 1.0-0.1*STATS.EDUCATION().EDUCATION.data(cl).getD(race);
				}
			},
			new CitizenThing(D.g("Soldiers"), D.g("SoldiersD", "The amount of soldiers in the city")) {
				
				@Override
				double update(Race race, double ds) {
					return 1.0 + 0.5*SETT.ARMIES().player().men()/(STATS.POP().POP.data(cl).get(null)+1.0);
				}
			},
			new CitizenThing(D.g("Armies"), D.g("ArmiesD", "The amount of soldiers stationed on the world map")) {
				
				@Override
				double update(Race race, double ds) {
					return 1.0 + 0.05*WARMYD.men(null).total().get(FACTIONS.player())/(STATS.POP().POP.data(cl).get(null)+1);
				}
			},
			new CitizenThing(D.g("Royalrace", "Royal Race"), D.g("RoyalD", "A constant buff to your starting race")) {
				
				@Override
				double update(Race race, double ds) {
					if (race == FACTIONS.player().race())
						return 1.1;
					return 1;
				}
			}
		);


	
	
	@Override
	void save(FilePutter file) {
		happiness.save(file);
		fullfillment.save(file);
		expectation.save(file);
		main.save(file);
		mainTarget.save(file);
		for (CitizenThing t : factors)
			t.save(file);
		file.ds(mains);
	}
	
	@Override
	void load(FileGetter file) throws IOException {
		happiness.load(file);
		fullfillment.load(file);
		expectation.load(file);
		main.load(file);
		mainTarget.load(file);
		for (CitizenThing t : factors)
			t.load(file);
		file.ds(mains);
		setAll();
	}
	
	@Override
	void clear() {
		happiness.clear();
		fullfillment.clear();
		expectation.clear();
		main.clear();
		mainTarget.clear();
		for (CitizenThing t : factors)
			t.clear();
		Arrays.fill(mains, 0);
	}
	
	StandingCitizen(){

		IDebugPanelSett.add("happiness++", new ACTION() {
			
			@Override
			public void exe() {
				for (Race race : RACES.all()) {
					happiness.set(race, happiness.getD(race)+0.1);
				}
			}
		});
		
		
	}
	
	private void setAll() {

		for (Race r : RACES.all()) {
			double max = 0;
			double def = 0;
			for (STAT ss : r.stats().standings(cl)) {
				if (!ss.standing().definition(r).get(cl).dismiss) {
					max += ss.standing().definition(r).get(cl).max;
					def += ss.standing().def(cl, r);
				}
			}
			maxes[r.index] = max;
			defs[r.index] = def;
			if (def >= max)
				fullPows[r.index] = 1;
			else
				fullPows[r.index] = Math.pow(max-def, POW);
		}
	}
	
	void update(double ds) {
		updater.update(ds);
	
	}

	
	void init() {
		setAll();
		for (int ri = 0; ri < RACES.all().size(); ri++) {
			Race r = RACES.all().get(ri);
			update(r, 0);
			mains[ri] = mainTarget.getD(r);
			main.set(r, mains[ri]);
		}
	}
	
	private final IUpdater updater = new IUpdater(RACES.all().size(), 4) {

		@Override
		protected void update(int i, double timeSinceLast) {
			
			Race r = RACES.all().get(i); 
			StandingCitizen.this.update(r, timeSinceLast);
			
		}
	};

	private void update(Race race, double ds){
		fullfillment.up(race, ds);
		expectation.up(race, ds);
		happiness.up(race, ds);
		for (CitizenThing t : factors)
			t.up(race, ds);
		mainTarget.up(race, ds);
		main.up(race, ds);
	}

	

	

	
	final class Main extends CitizenThing {
		
		private final double inter = 1.0/(100.0*TIME.secondsPerDay);
		
		Main() {
			super(
				D.g("Loyalty"), 
				D.g("loyalty_desc", "The overall loyalty of your citizens. The goal of any despot is to keep this high. Lower loyalty than 100% could lead to bloody riots. Happiness is a big factor, but other means exist...")
					);
		}

		@Override
		double update(Race race, double ds) {
			
			double now = mains[race.index];
			
			int t = (int) (mainTarget.getD(race)*100);
			int c = (int) (now*100);
			double d = t-c;
			double mul = 1 + Math.abs(d)/25.0;
			d *= ds*inter*mul;
			double cur = now+d;
			if (d < 0 && cur < mainTarget.getD(race))
				cur = mainTarget.getD(race);
			else if(d > 0 && cur > mainTarget.getD(race))
				cur = mainTarget.getD(race);
			
			cur = CLAMP.d(cur, 0, 1);
			mains[race.index] = cur;
			return cur;
		}
		
	}
	
	final class MainTarget extends CitizenThing {
		
		MainTarget() {
			super(
				D.g("Submission-Target"), 
				D.g("submissionTarget_desc", "What your submission will be in a few days.")
					);
		}

		@Override
		double update(Race race, double ds) {
			
			double h = happiness.getD(race);
			for (CitizenThing t : factors) {
				h*= t.getD(race);
			}
			return h;
		}
		
	}
	
	private double hap(Race r) {
		double sup = fullfillment.getD(r);
		double exp = expectation.getD(r);
		if (sup <= 0)
			return 0;
		if (exp == 0)
			return 1;
		sup/=exp;
		
		for (StatMultiplier m : STATS.MULTIPLIERS().get(cl))
			sup *= m.multiplier(cl, r, 0);
		
		return sup;
	}
	
	final class Happiness extends CitizenThing {
		
		Happiness() {
			super(
				D.g("Happiness"), 
				D.g("happiness_desc", "Happiness is fulfillment in proportion to expectations. Fulfillment is gained by providing services and a just rule. Expectation is the amount of citizens in your city. Happiness boosts submission and promotes immigration.")
					);
		}

		@Override
		double update(Race r, double ds) {
			
			
			double sup = hap(r);
			return (int)(100*CLAMP.d(sup, 0, 1))/100.0;
		}
		
	}
	
	final class Fulfillment extends CitizenThing {
		
		
		
		Fulfillment() {
			super(
				D.g("Fulfillment"),
				D.g("full_desc", "A fulfillment modifier can be access to a tavern, or a road, or increased food rations. Each race have different weights they tied to each modifier. Focus should be on the biggest modifiers first. Total Fulfillment is an exponential function of the sum of all your fulfillment modifiers divided by the sum of all max fulfillments.")
					);
		}

		@Override
		double update(Race r, double ds) {
			double d = fullfillment(r);
			return CLAMP.d(d, 0, 1);
			
		}
		
	}
	

	
	public double fullPow(Race r) {
		return fullPows[r.index];
	}
	
	public double fullfillment(Race r) {
		
		double current = 0;
		double max = maxes[r.index];
		double def = defs[r.index];
		for (STAT ss : r.stats().standings(cl)) {
			current += ss.standing().get(cl, r);
		}
		
		
		if (max <= 0)
			return 1;
		
		double d = 0;
		if (current < def) {
			d = -current/def;
		}else {
			current -= def;
			max -= def;
			d = Math.pow(current/max, fullPow(r));
		}
		
		if (GAME.player().race() == r) {
			double min = expectation(r, 10, 0);
			if (d < 0)
				return min*-d;
			return CLAMP.d(min + d, 0, 1);
			
		}else {
			double min = expectation(r, 2, 0);
			if (d < 0)
				return min*-d;
			return CLAMP.d(min + d, 0, 1);
		}
	
	}
	
	static final class Expectation extends CitizenThing {
		
		Expectation() {
			super(
				D.g("Expectations"),
				D.g("exp_desc", "As your population grows, so will your subjects' expectations. Expectation is tied to a species occurrence in the climate you've chosen to settle and grows linearly.")
					);
		}
		


		@Override
		double update(Race race, double ds) {
			return c(race);
		}
		
		private double c(Race race) {
			double pop = STATS.POP().POP.data(cl).get(race, 0)+1 + World.ARMIES().cityDivs().total(race);
			double popOther = STATS.POP().POP.data(cl).get(null, 0)+1+World.ARMIES().cityDivs().total() -pop;
			return expectation(race, pop, popOther);
		}
		
		
		
	}

	public static double expectation(Race race, double amount, double other) {
		
		double bo = Math.sqrt(amount/(amount+other));
		double exp = (amount+other)/MAX;
		double pe = 1.0/race.population().maxCity;
		return bo*exp*pe;
	}
	
	@Override
	public double current(Induvidual a) {
		Race r = a.race();
		double current = 0;
		double max = maxes[r.index];
		double def = defs[r.index];
		for (STAT ss : r.stats().standings(cl)) {
			current += ss.standing().get(a);
		}
		
		double h = 1;
		for (CitizenThing t : factors) {
			h*= t.getD(r);
		}
		
		if (max <= 0)
			return h;
		
		double d = 0;
		if (current < def) {
			d = -current/def;
		}else {
			current -= def;
			max -= def;
			d = Math.pow(current/max, fullPow(r));
		}
		
		if (GAME.player().race() == r) {
			double min = expectation(r, 10, 0);
			if (d < 0)
				h = min*-d;
			else
				h =  CLAMP.d(h*(min + d), 0, 1);
			
		}else {
			double min = expectation(r, 2, 0);
			if (d < 0)
				h = min*-d;
			else
				h = CLAMP.d(h*(min + d), 0, 1);
		}
		
		double pop = STATS.POP().POP.data(cl).get(r, 0)+1 + World.ARMIES().cityDivs().total(r);
		double popOther = STATS.POP().POP.data(cl).get(null, 0)+1+World.ARMIES().cityDivs().total() -pop;
		h/= expectation(r, pop, popOther);
		
		for (CitizenThing t : factors) {
			h*= t.getD(r);
		}
		return h;
	}
	
	@Override
	public double current() {
		return main.getD(null);
	}

	@Override
	public double target() {
		return mainTarget.getD(null);
	}
	
	@Override
	public INFO info() {
		return main.info();
	}
	
	
	public static abstract class CitizenThing {

		private final HistoryInt total = new HistoryInt(STATS.DAYS_SAVED, TIME.days(), true);
		private final HistoryInt[] histories = new HistoryInt[RACES.all().size()];
		private final INFO info;
		private static final double dd = 10000000;
				
		CitizenThing(INFO info) {
			this.info = info;
			for (int i = 0; i < histories.length; i++)
				histories[i] = new HistoryInt(STATS.DAYS_SAVED, TIME.days(), true);
		}
		
		CitizenThing(CharSequence name, CharSequence desc) {
			this(new INFO(name, desc));
		}
		
		final void up(Race race, double ds) {
			set(race, update(race, ds));
		}
		
		abstract double update(Race race, double ds);
		

		public double getD(Race t) {
			return getD(t, 0);
		}

		public double getD(Race t, int daysBack) {
			HistoryInt h = t == null ? total : histories[t.index];
			double d = h.get(daysBack)/dd;
			return CLAMP.d(d, 0, d);
		}
		
		void save(FilePutter file) {
			total.save(file);
			for (HistoryInt i : histories)
				i.save(file);
		}
		
		void load(FileGetter file) throws IOException {
			total.load(file);
			for (HistoryInt i : histories)
				i.load(file);
		}
		
		void clear() {
			total.clear();
			for (HistoryInt i : histories)
				i.clear();
		}
		
		void set(Race race, double v) {
			
			histories[race.index].set((int) (v*dd));
			double total = 0;
			for (int ri = 0; ri < RACES.all().size(); ri++) {
				Race r = RACES.all().get(ri);
				total +=  CLAMP.d(getD(r), 0, 1)*STATS.POP().POP.data(cl).get(r, 0);
			}
			double p = STATS.POP().POP.data(cl).get(null, 0);
			if (p == 0)
				p = 1;
			total /= p;
			this.total.set((int) (total*dd));
		}
		
		public INFO info() {
			return info;
		}
		
	}

	
}
