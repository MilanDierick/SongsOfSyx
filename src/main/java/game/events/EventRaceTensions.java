package game.events;

import java.io.IOException;
import java.util.Arrays;

import game.events.EVENTS.EventResource;
import game.time.TIME;
import init.D;
import init.race.RACES;
import init.race.Race;
import settlement.entity.humanoid.HCLASS;
import settlement.stats.STATS;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.StrInserter;
import view.main.MessageText;
import view.sett.IDebugPanelSett;

public class EventRaceTensions extends EventResource{

	private static CharSequence ¤¤title = "Tensions high!";
	private static CharSequence ¤¤desc = "Due to recent immigration policies, tensions have risen between our {RACE_A} and {RACE_B} subjects. We can expect to see more brawls and devastation as a result.";
	
	private final StrInserter<Race> iA = new StrInserter<Race>("RACE_A") {

		@Override
		protected void set(Race t, Str str) {
			str.add(t.info.namePosessive);
		}
		
	};
	
	private final StrInserter<Race> iB = new StrInserter<Race>("RACE_B") {

		@Override
		protected void set(Race t, Str str) {
			str.add(t.info.namePosessive);
		}
		
	};
	
	private double[] timers = new double[RACES.all().size()];
	private double upD;
	
	static {
		D.ts(EventRaceTensions.class);
		
	}
	
	EventRaceTensions() {
		IDebugPanelSett.add("Event: race war", new ACTION() {
			
			@Override
			public void exe() {
				spawn();
			}
		});
		clear();
	}
	
	@Override
	protected void update(double ds) {
		int ri = (int) upD;
		upD += ds;
		if (upD >= timers.length)
			upD -= timers.length;
		if (ri == (int) upD)
			return;
		if (STATS.POP().POP.data(HCLASS.CITIZEN).get(RACES.all().get(ri)) == 0) {
			timers[ri] = CLAMP.d(timers[ri]+1, 0, TIME.secondsPerDay*8);
		}else if (timers[ri] < 0) {
			timers[ri] += timers.length;
			if (timers[ri] > 0)
				timers[ri] = TIME.secondsPerDay*8;
		}else {
			double max = STATS.ENV().OTHERS.standing().max(HCLASS.CITIZEN, RACES.all().get(ri));
			double v = STATS.ENV().OTHERS.standing().get(HCLASS.CITIZEN, RACES.all().get(ri));
			if (v/max < 0.75) {
				timers[ri] -= (max-v)*timers.length;
				if (timers[ri] < 0) {
					timers[ri] = TIME.secondsPerDay*8;
					spawn(RACES.all().get(ri));
				}
			}else {
				timers[ri] = CLAMP.d(timers[ri]+1, 0, TIME.secondsPerDay*8);
			}
		}
	}
	
	private void spawn() {
		double max = 0;
		for (int ri = 0; ri < RACES.all().size(); ri++) {
			Race r = RACES.all().get(ri);
			if (STATS.POP().POP.data().get(r) > 0 && STATS.ENV().OTHERS.data(HCLASS.CITIZEN).getD(r) < 0.5) {
				max += STATS.POP().POP.data().get(r)*(1.0-STATS.ENV().OTHERS.data(HCLASS.CITIZEN).getD(r)); 
			}
		}
		
		if (max == 0)
			return;
		
		max *= RND.rFloat();
		
		for (int ri = 0; ri < RACES.all().size(); ri++) {
			Race r = RACES.all().get(ri);
			if (STATS.POP().POP.data().get(r) > 0 && STATS.ENV().OTHERS.data(HCLASS.CITIZEN).getD(r) < 0.5) {
				max -= STATS.POP().POP.data().get(r)*(1.0-STATS.ENV().OTHERS.data(HCLASS.CITIZEN).getD(r)); 
				if (max <= 0) {
					spawn(r);
					break;
				}
			}
		}
	}
	
	private void spawn(Race race) {
		double max = 0;
		for (int ri = 0; ri < RACES.all().size(); ri++) {
			Race r = RACES.all().get(ri);
			if (STATS.POP().POP.data().get(r) > 0 && race != r && race.pref().other(r) < 0.75) {
				max += STATS.POP().POP.data().get(r)*(1 - race.pref().other(r)); 
			}
		}
		if (max == 0)
			return;
		
		max *= RND.rFloat();
		
		for (int ri = 0; ri < RACES.all().size(); ri++) {
			Race r = RACES.all().get(ri);
			if (STATS.POP().POP.data().get(r) > 0 && race != r && race.pref().other(r) < 0.75) {
				max -= STATS.POP().POP.data().get(r)*(1 - race.pref().other(r)); 
				if (max <= 0) {
					double time = TIME.secondsPerDay*(1+RND.rFloat(4));
					timers[race.index] = -time;
					timers[r.index] = -time;
					Str s = new Str(¤¤desc);
					iA.insert(race, s);
					iB.insert(r, s);
					new MessageText(¤¤title, s).send();
				}
			}
		}
	}
	
	public boolean isAtOdds(Race r1, Race r2) {
		if (r1 == r2)
			return false;
		int i1 = r1.index();
		int i2 = r2.index();
		if (timers[i1] >= 0)
			return false;
		if (timers[i2] >= 0)
			return false;
		return true;
	}

	@Override
	protected void save(FilePutter file) {
		file.dsE(timers);
		file.d(upD);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		file.dsE(timers);
		upD = file.d();
	}

	@Override
	protected void clear() {
		Arrays.fill(timers, TIME.secondsPerDay*8);
	}
	
	

}
