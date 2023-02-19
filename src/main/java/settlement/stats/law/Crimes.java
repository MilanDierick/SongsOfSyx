package settlement.stats.law;

import java.io.IOException;

import game.time.TIME;
import init.D;
import init.race.Race;
import settlement.entity.humanoid.HCLASS;
import settlement.stats.STATS;
import settlement.stats.law.PRISONER_TYPE.CRIME;
import snake2d.util.file.*;
import snake2d.util.misc.CLAMP;
import util.statistics.*;

public final class Crimes {

	private final HistoryInt rate = new HistoryInt(STATS.DAYS_SAVED, TIME.days(), true);
	private final HistoryInt total = new HistoryInt(STATS.DAYS_SAVED, TIME.days(), false);
	private final HistoryInt[] crimes = new HistoryInt[PRISONER_TYPE.CRIMES.size()];
	private final HistoryRace perRace = new HistoryRace(STATS.DAYS_SAVED, TIME.days(), false);
	
	public static CharSequence ¤¤expl = "¤Each race has their own inclination towards criminality. This is further increased by high populations. A good way to deter crime is through well working law facilities and harsh punishments.";
	public static CharSequence ¤¤rate = "¤Your rate is the average crimes committed throughout the year in regards to your population.";
	
	private double upD = 0;
	
	static {
		D.ts(Crimes.class);
	}
	
	Crimes() {
		for (int i = 0; i < crimes.length; i++)
			crimes[i] = new HistoryInt(STATS.DAYS_SAVED, TIME.days(), false);
	}
	
	public HISTORY_INT crimes(CRIME c) {
		if (c == null)
			return total;
		return crimes[c.crimeI];
	}
	
	public HISTORY_COLLECTION<Race> perRace() {
		return perRace;
	}
	
	public void register(Race race, CRIME c) {
		total.inc(1);
		crimes[c.crimeI].inc(1);
		perRace.inc(race, 1);
	}
	
	public HISTORY rate() {
		return rate;
	}
	
	void update(double ds) {
		upD -= ds;
		if (upD < 0) {
			upD += 5;
			rate.setD(rateCurrent());
		}
	}
	
	public double rateCurrent() {
		double pop = 0;
		double res = 0;
		for (int i = 0; i < 10; i++) {
			res += total.get(i);
			pop += STATS.POP().POP.data(HCLASS.CITIZEN).get(null, i);
		}
		
		if (pop <= 0)
			return res > 0 ? 1 : 0;
		
		res = CLAMP.d(res/(pop*LAW.HI_RATE), 0, 1);
		return res;
	}

	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			total.save(file);
			rate.save(file);
			for (HistoryInt i : crimes)
				i.save(file);
			perRace.save(file);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			total.load(file);
			rate.load(file);
			for (HistoryInt i : crimes)
				i.load(file);
			perRace.load(file);
		}
		
		@Override
		public void clear() {
			total.clear();
			rate.clear();
			for (HistoryInt i : crimes)
				i.clear();
			perRace.clear();
		}
	};
	
	
}
