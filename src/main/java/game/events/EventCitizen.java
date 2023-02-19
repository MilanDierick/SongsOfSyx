package game.events;

import java.io.IOException;

import game.events.EVENTS.EventResource;
import game.time.TIME;
import init.D;
import init.race.RACES;
import init.race.Race;
import settlement.entity.humanoid.HCLASS;
import settlement.entity.humanoid.Humanoid;
import settlement.stats.STATS;
import settlement.stats.standing.STANDINGS;
import snake2d.LOG;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import view.main.MessageText;
import view.sett.IDebugPanelSett;

public final class EventCitizen extends EventResource{

	public static final double breakPoint = 0.75;
	private static CharSequence ¤¤loyaltyWarning = "¤wavering loyalty";
	private static CharSequence ¤¤loyaltyWarningD = "¤Loyalty is decreasing. It is of utmost importance that you tend to your subjects needs. Check the citizen tab upper-left to see what can be done to increase happiness, and don't invite more immigrants to join until you have it under control.";
	private static CharSequence ¤¤riotWarning = "¤Ungrateful plebs!";
	private static CharSequence ¤¤riotWarningD = "¤Rumour has it that your citizens are grinding their teeth in frustration over what they claim is your incompetent rule. If nothing is done in time, a riot might follow! Try to increase their loyalty immediately.";

	static {
		D.ts(EventCitizen.class);
	}
	
	private boolean hasSentWarning = false;
	private final double timerD = 10;
	private double timer = timerD;
	private double count = 5.0;
	private final double countD = timerD/TIME.secondsPerDay;
	private boolean emigrate;
	private final EventCitizenEmmigrate emmi = new EventCitizenEmmigrate();
	private final EventCitizenStrike strike = new EventCitizenStrike();
	private final EventCitizenRiot riot = new EventCitizenRiot();
	
	private final int[] amounts = new int[RACES.all().size()];
	
	EventCitizen(){
		
		clear();

		IDebugPanelSett.add("Event: Emmigration2", new ACTION() {
			
			@Override
			public void exe() {
				double total = 0;
				for (Race r : RACES.all()) {
					int a = getAmount(r);
					total += a;
					amounts[r.index()] = a;
				}
				
				
				double c = total/STATS.POP().POP.data(HCLASS.CITIZEN).get(null);
				
				if (c == 0) {
					LOG.ln("nay!");
				}else {
					Race r = getRace(total);
					emmi.emmigrate(amounts[r.index], r);
				}
			}
		});
		
	}
	
	@Override
	protected void save(FilePutter file) {
		file.d(timer);
		file.d(count);
		file.bool(emigrate);
		file.bool(hasSentWarning);
		emmi.save(file);
		strike.save(file);
		riot.save(file);
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		timer = file.d();
		count = file.d();
		emigrate = file.bool(); 
		hasSentWarning = file.bool();
		emmi.load(file);
		strike.load(file);
		riot.load(file);
	}
	
	@Override
	protected void clear() {
		emigrate = true;
		hasSentWarning = false;
		timer = timerD;
		count = 2.0;
		emmi.clear();
		strike.clear();
		riot.clear();
	}
	
	public boolean shouldEmigrate(Humanoid h) {
		return shouldEmigrate(h.race());
	}
	
	public boolean shouldEmigrate(Race r) {
		return emmi.shouldEmigrate(r);
	}
	
	public void emigrate(Humanoid h) {
		emmi.emigrate(h);
	}
	
	public boolean onStrike(Humanoid h) {
		return strike.isStriking(h);
	}
	
	@Override
	protected void update(double ds) {
		
		riot.update(ds);
		strike.update(ds);
		
		timer -= ds;
		if (timer > 0)
			return;
		
		timer += timerD;
		
		if (STATS.POP().POP.data().get(null) == 0)
			return;
		
		double total = 0;
		for (Race r : RACES.all()) {
			int a = getAmount(r);
			total += a;
			amounts[r.index()] = a;
		}
		
		
		
		
		if (total == 0) {
			if (count < 1.0) {
				count += countD;
				count = CLAMP.d(count, 0, 1.0);
			}
		}else {
			
			double c = total/STATS.POP().POP.data(HCLASS.CITIZEN).get(null);
			
			if (!hasSentWarning) {
				new MessageText(¤¤loyaltyWarning, ¤¤loyaltyWarningD).send();
				hasSentWarning = true;
			}
			
			double old = count;
			c = Math.pow(c, 0.8);
			count -= c*countD;
			if (count >= 0.25)
				return;
			
			if (emigrate) {
				if (old > 0.25) {
					emigrate = !RND.oneIn(3);
					Race r = getRace(total);
					
					
					
					if (RND.rBoolean() && strike.strike(r))
						count += 0.5;
					else if (emmi.emmigrate(amounts[r.index], r)) {
						count += 0.5;
					}else {
						emigrate = false;
						new MessageText(¤¤riotWarning, ¤¤riotWarningD).send();
					}
					
				}
				
			}else if (count <= 0) {
				emigrate = !RND.oneIn(3);
				riot.riot(amounts);
				count += 1.5;
				return;
			}
		}
		
		
		
	}
	
	private Race getRace(double total) {
		
		total = total*RND.rFloat();
		for (int i = 0; i < RACES.all().size(); i++) {
			total -= amounts[i];
			if (total <= 0 && amounts[i] > 0)
				return RACES.all().get(i);
		}
		for (int i = 0; i < RACES.all().size(); i++) {
			if (amounts[i] > 0)
				return RACES.all().get(i);
		}
		return RACES.all().get(0);
		
	}
	
	private int getAmount(Race r) {
		
		double m = Math.max(STANDINGS.CITIZEN().main.getD(r), STANDINGS.CITIZEN().mainTarget.getD(r));
		if (m >= breakPoint) {
			return 0;
		}
		
		m = 1.0 - m/breakPoint;
		
		double dPop = STATS.POP().POP.data().get(null);
		dPop = 0.1 + 0.9*CLAMP.d(dPop/600.0, 0, 1);
		
		
		m *= dPop;
		int pop = STATS.POP().POP.data(HCLASS.CITIZEN).get(r);
		
		int rebels = (int) (pop*m);
		
		return CLAMP.i(rebels, 0, pop);
	}
	

	
	


	
}
