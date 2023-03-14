package settlement.stats.health;

import java.io.IOException;

import game.time.TIME;
import init.D;
import init.boostable.BOOSTABLE;
import init.boostable.BOOSTABLES;
import init.disease.DISEASES;
import init.race.RACES;
import settlement.main.CapitolArea;
import settlement.main.SETT.SettResource;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.law.LAW;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import util.dic.DicMisc;
import util.statistics.HISTORY_INT;
import util.statistics.HistoryInt;

public final class HEALTH extends SettResource{

	private static HEALTH self;
	public static CharSequence ¤¤name = "Sanitation";
	public static CharSequence ¤¤desc = "Sanitation is a combination of your city hygiene, population size and the health of your individual subjects. Sanitation determines the amount of sickness you have in the city, which might cause low efficiency and death. Low sanitation also increases the chances of an epidemic.";
	private static CharSequence ¤¤populationD = "¤With larger populations come more risks of disease.";
	
	static {
		D.ts(HEALTH.class);
	}
	
	
	private final HistoryInt rate = new Rate(¤¤name, ¤¤desc);
	private final HistoryInt health = new Rate(BOOSTABLES.PHYSICS().HEALTH.name, BOOSTABLES.PHYSICS().HEALTH.desc);
	private final HistoryInt hygine = new Rate(BOOSTABLES.CIVICS().HYGINE.name, BOOSTABLES.CIVICS().HYGINE.desc);
	private final HistoryInt squalor = new Rate(DicMisc.¤¤Population, ¤¤populationD);
	
	private double iinfectionChance = DISEASES.DISEASE_PER_YEAR / TIME.years().bitConversion(TIME.days());
	
	public HEALTH(){
		self = this;
		new Bonuses();
		rate.fill(100);
	}
	
	@Override
	protected void save(FilePutter file) {
		rate.save(file);
		health.save(file);
		hygine.save(file);
		squalor.save(file);
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		rate.load(file);
		health.load(file);
		hygine.load(file);
		squalor.load(file);
	}
	
	@Override
	protected void clearBeforeGeneration(CapitolArea area) {
		rate.fill(100);
		health.fill((int) (BOOSTABLES.PHYSICS().HEALTH.defAdd*100));
		hygine.fill((int) (BOOSTABLES.PHYSICS().HEALTH.defAdd*100));
		super.clearBeforeGeneration(area);
	}
	
	@Override
	protected void update(float ds) {

		health.set((int) (BOOSTABLES.PHYSICS().HEALTH.get(null, null)*100));
		hygine.set((int) (BOOSTABLES.CIVICS().HYGINE.get(null, null)*100));
		double pop = STATS.POP().POP.data().get(null);
		{
			
			
			int min = DISEASES.SQUALOR_POPULATION_MIN;
			if (pop < min) {
				double d = 1 + 5-5*pop/min;
				squalor.set((int) (d*100));
			}else {
				double high = 1.0/max(BOOSTABLES.PHYSICS().HEALTH)*max(BOOSTABLES.CIVICS().HYGINE);
				
				double pi = 1.0/DISEASES.SQUALOR_POPULATION_DELTA;
				
				double delta = (1.0-high);
				
				double res = 1.0 - delta*CLAMP.d((pop-min)*pi, 0, 1);
				squalor.set((int) (res*100));
			}
			
			
		}
		
		double r = health.getD()*hygine.getD()*squalor.getD();
		r = CLAMP.d(r, 0, 1);
		rate.set((int) (r*100));
		
		iinfectionChance = DISEASES.DISEASE_PER_YEAR / TIME.years().bitConversion(TIME.days());
		if (pop < 100.0) {
			iinfectionChance = 0;
		}else if (pop < 200) {
			iinfectionChance *= (pop-100)/200.0;
		}
		
	}
	
	private double max(BOOSTABLE b) {
		double add = Math.max(BOOSTABLES.player().maxAdd(b), 0);
		add -= Math.max(RACES.bonus().maxAdd(b), 0);
		double mul = Math.max(BOOSTABLES.player().maxMul(b), 0);
		mul /= Math.max(RACES.bonus().maxMul(b), 1);
		return mul * (add+1);
	}

	public static HISTORY_INT rate() {
		return self.rate;
	}
	
	public static HISTORY_INT health() {
		return self.health;
	}
	
	public static HISTORY_INT hygine() {
		return self.hygine;
	}
	
	public static HISTORY_INT pop() {
		return self.squalor;
	}
	
	public static boolean shouldGetSickDay(Induvidual a) {
		double c = BOOSTABLES.PHYSICS().HEALTH.get(a)*self.hygine.getD()*CLAMP.d(self.squalor.getD(), 0, 2);
		if (c < 0.25)
			c = 0.25;
		c = self.iinfectionChance / c;
		if (LAW.curfew().is())
			c *= 0.25;
		return c > RND.rFloat();
	}
	
	private static class Rate extends HistoryInt {

		private static double di = 1.0/100;
		
		public Rate(CharSequence name, CharSequence desc) {
			super(name, desc, STATS.DAYS_SAVED, TIME.days(), true);
		}
		
		@Override
		public double getD() {
			return get()*di;
		}
		
		@Override
		public double getD(int fromZero) {
			return get(fromZero)*di;
		}
		
	}
	
}
