package settlement.stats.law;

import java.io.IOException;

import game.time.TIME;
import init.D;
import settlement.stats.STATS;
import settlement.stats.law.Processing.PunishmentImp;
import snake2d.util.file.*;
import snake2d.util.misc.CLAMP;
import util.statistics.HISTORY;
import util.statistics.HistoryInt;

public final class LawRate {
	
	private double upI = 0;
	private final HistoryInt rate = new HistoryInt(STATS.DAYS_SAVED, TIME.days(), true);
	public static CharSequence ¤¤desc = "Law prevents crime and makes your subjects feel safe. The most important aspect is to catch the criminals. You can then process them as you see fit, with harsher punishments decreasing crime further. Law is determined by the effective rates of your arrests and punishments. The effective rate is the average of an 8-day period."; 
	
	static {
		D.ts(LawRate.class);
	}
	
	LawRate(){
		
	}
	
	public HISTORY rate() {
		return rate;
	}
	
	public double increase() {
		return rate.getD(0)-rate.getD(1);
	}
	
	public double today() {
		return rate.getD();
	}
	
	SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			rate.save(file);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			rate.load(file);
		}
		
		@Override
		public void clear() {
			rate.clear();
		}
	};

	void update(float ds) {
		upI -= ds;
		if (upI > 0)
			return;
		
		double r = LAW.process().arrests.rate.getD(null);
		double m = 0;
		for (PunishmentImp p : LAW.process().punishments) {
			m += p.rate(null).getD()*p.multiplier;
		}
		for (PunishmentImp p : LAW.process().extras) {
			m += p.rate(null).getD()*p.multiplier;
		}
		r *= m;
		r = CLAMP.d(r, 0, 1);
		rate.setD(r);
		
		
	}
	
}
