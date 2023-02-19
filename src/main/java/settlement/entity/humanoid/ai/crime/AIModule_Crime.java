package settlement.entity.humanoid.ai.crime;

import game.GAME;
import game.time.TIME;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.law.LAW;
import settlement.stats.law.PRISONER_TYPE;
import settlement.stats.law.PRISONER_TYPE.CRIME;
import snake2d.util.bit.Bits;
import snake2d.util.rnd.RND;
import util.data.INT_O.INT_OE;

public final class AIModule_Crime extends AIModule{

	public final AIPLAN theft = new Theft(this);
	public final AIPLAN murder = new Murder(this);
	public final AIPLAN vandal = new Vandalism(this);
	public final AIPLAN flash = new Flasher(this);
	public final AIPLAN disrespect = new Disrespect(this);
	
	public final AIPLAN serial = new SerialKiller();
	
	private final INT_OE<AIManager> crimesToCommit = AIModules.data().byte1;
	private final INT_OE<AIManager> ctimer = new wrap(new Bits(0b011));
	private final INT_OE<AIManager> ccrimes = new wrap(new Bits(0b01111_11_00));
	
	public AIModule_Crime() {
		for (CRIME c : PRISONER_TYPE.CRIMES)
			getPlan(c);
	}
	
	@Override
	protected AiPlanActivation getPlan(Humanoid a, AIManager d) {
		
		if (GAME.events().killer.theKiller() == a && GAME.events().killer.theKillerShouldKill())
			return serial.activate(a, d);
		
		CRIME crime = PRISONER_TYPE.RND(a.race());
		
		return getPlan(crime).activate(a, d);
	}
	
	private AIPLAN getPlan(CRIME crime) {
		
		if (crime == CRIME.THEFT)
			return theft;
		if (crime == CRIME.DISRESPECT)
			return disrespect;
		if (crime == CRIME.FLASHING)
			return flash;
		if (crime == CRIME.MURDER)
			return murder;
		if (crime == CRIME.VANDALISM)
			return vandal;
		throw new RuntimeException(crime.name+"");
	}

	private static final double di = 1.0/16;
	
//	private int day = -1;
//	private double acc = 0;
//	private int ii = 0;
//	private int kk = 0;
//	private int max = 0;
	
	
	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int updateOfDay) {
		
		double rate = LAW.getCrimeRate(a)*di;
//		ii++;
//		acc += rate;
//		if (day != TIME.days().bitsSinceStart()) {
//			day = TIME.days().bitsSinceStart();
//			System.out.println(max + " " + acc + " " + acc/ii + " " + ii + " " + kk);
//			acc = 0; 
//			ii = 0;
//			kk = 0;
//			max = 0;
//			
//		}
		
		if (rate >= RND.rFloat()) {
			crimesToCommit.inc(d, 16);
//			kk++;
//			max = Math.max(crimesToCommit.get(d), max);
		}
		
		if (newDay) {
			int c = ccrimes.get(d);
			if (c > 0) {
				
				int t = ctimer.get(d);
				if (t == 0) {
					
					ccrimes.inc(d, -1);
					LAW.process().arrests.inc(a.race(), false);
					ctimer.set(d, 2);
				}else {
					ctimer.inc(d, -1);
				}
				
			}
		}
		if ((updateOfDay & 0b011) == 0 && LAW.process().prosecute.allowed.is(a.race()) || STATS.MULTIPLIERS().PROSECUTION.markIs(a)) {
			SETT.ROOMS().GUARD.reportCriminal(a, true);
		}
	}
	
	void commitCrime(Humanoid a, AIManager d, boolean notify, CRIME crime) {
		LAW.crimes().register(a.race(), crime);
		PRISONER_TYPE old = STATS.LAW().prisonerType.get(a.indu());
		if (old instanceof CRIME) {
			if (crime.rarty() < ((CRIME)old).rarty())
				STATS.LAW().prisonerType.set(a.indu(), crime);
		}else {
			STATS.LAW().prisonerType.set(a.indu(), crime);
		}
		
		if (notify)
			AIModule_Crime.notify(a);
		SETT.ROOMS().GUARD.reportCriminal(a, false);
		if (ccrimes.isMax(d)) {
			LAW.process().arrests.inc(a.race(), false);
		}else
			ccrimes.inc(d, 1);
		ctimer.set(d, 2);
		crimesToCommit.inc(d, -16);
	}
	
	
	public boolean catchPrisoner(Humanoid a) {
		AIManager d = (AIManager) a.ai();
		
		
		
		if (ccrimes.get(d) > 0) {
			for (int i = 0; i < ccrimes.get(d); i++) {
				LAW.process().arrests.inc(a.race(), true);
			}
			ccrimes.set(d, 0);
			return true;
		}
		if (LAW.process().prosecute.allowed.is(a.race()) || STATS.MULTIPLIERS().PROSECUTION.markIs(a)) {
			STATS.LAW().prisonerType.set(a.indu(), PRISONER_TYPE.PLEASURE);
			LAW.process().prosecute.inc(a.race(), true);
			ccrimes.set(d, 0);
			return true;
		}
		
		return false;
		
	}

	@Override
	public int getPriority(Humanoid a, AIManager d) {
		if (GAME.events().killer.theKiller() == a) {
			if (GAME.events().killer.theKillerShouldKill()) {
				return TIME.light().nightIs()  ? 4 : 0;
			}
			return 0;
		}
		if (crimesToCommit.get(d) >= 16) {
			return 4;
		}
		return 0;
	}
	
	public static void notify(Humanoid criminal) {
		for (ENTITY e : SETT.ENTITIES().getInProximity(criminal, 5)) {
			if (e instanceof Humanoid) {
				HEvent.Handler.notifyCrime((Humanoid) e, criminal);
			}
		}
	}

	public boolean isCriminal(Humanoid a) {
		AIManager d = (AIManager) a.ai();
		return ccrimes.get(d) > 0 || a.indu().hostile() || LAW.process().prosecute.allowed.is(a.race()) || STATS.MULTIPLIERS().PROSECUTION.markIs(a);
	}
	
	@Override
	protected void init(Humanoid a, AIManager d) {
		crimesToCommit.set(d, RND.rInt(16));
	}
	
	private static class wrap implements INT_OE<AIManager> {

		private final Bits bits;
		
		wrap(Bits bits){
			this.bits = bits;
		}
		
		@Override
		public int get(AIManager t) {
			return bits.get(AIModules.data().byte2.get(t));
		}

		@Override
		public int min(AIManager t) {
			return 0;
		}

		@Override
		public int max(AIManager t) {
			return bits.mask;
		}

		@Override
		public void set(AIManager t, int i) {
			int d = bits.set(AIModules.data().byte2.get(t), i);
			AIModules.data().byte2.set(t, d);
		}
		
		
	}

}
