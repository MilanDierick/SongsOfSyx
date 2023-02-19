package settlement.stats;

import init.race.Race;
import settlement.entity.humanoid.HCLASS;
import settlement.stats.law.LAW;
import settlement.stats.law.PRISONER_TYPE;
import settlement.stats.law.Processing.Punishment;
import settlement.stats.law.Processing.PunishmentImp;
import snake2d.util.sets.*;
import util.data.GETTER_TRANS.GETTER_TRANSE;
import util.data.INT_O.INT_OE;

public class StatsLaw extends StatCollection{
	
	private final LIST<STAT> all;
	public STAT EQUALITY;
	public STAT CRIME;
	public final LIST<StatLaw> punishments;
	public final GETTER_TRANSE<Induvidual, PRISONER_TYPE> prisonerType;
	
	StatsLaw(Init init){
		super(init, "LAW");
		
		new StatLaw(LAW.process().pardoned, "PARDON", init);
		new StatLaw(LAW.process().exile, "EXILE", init);
		new StatLaw(LAW.process().prison, "PRISON", init);
		new StatLaw(LAW.process().stocks, "STOCKS", init);
		new StatLaw(LAW.process().execution, "EXECUTION", init);
		new StatLaw(LAW.process().enslaved, "ENSLAVED", init);
		new StatLaw(LAW.process().judgement, "JUDGEMENT", init);

		EQUALITY = new STAT.STATImp("EQUALITY", init, 1.0) {
			
			@Override
			int getDD(HCLASS s, Race r) {
				
				double d = 0;
				double t = 0;
				for (Punishment p : LAW.process().punishments) {
					d += p.rate(r).getD()*p.multiplier;
					t += p.rate(null).getD()*p.multiplier;
				}
				
				if (d*0.6 > t) {
					return (int) (pdivider(s, r, 0)*t/d); 
				}
				
				return pdivider(s, r, 0);
			}
		};
		
		CRIME = new STAT.STATFacade("CRIME", init, 0) {
			
			@Override
			double getDD(HCLASS s, Race r, int daysBack) {
				return LAW.crimes().rate().getD(daysBack);
			}
		};
		
		final INT_OE<Induvidual> data = init.count.new DataNibble();
		
		prisonerType = new GETTER_TRANSE<Induvidual, PRISONER_TYPE>(){

			@Override
			public PRISONER_TYPE get(Induvidual f) {
				return PRISONER_TYPE.ALL.get(data.get(f));
			}

			@Override
			public void set(Induvidual f, PRISONER_TYPE t) {
				data.set(f, t.index());
			}
			
		};
		
		all = makeStats(init);
	
		LinkedList<StatLaw> ll = new LinkedList<>();
		for (STAT s : all)
			if (s instanceof StatLaw)
				ll.add((StatLaw) s);
		
		punishments = new ArrayList<StatsLaw.StatLaw>(ll);
		
		
	}

	@Override
	public LIST<STAT> all() {
		return all;
	}

	public static class StatLaw extends STAT.STATFacade {

		public final PunishmentImp p;
		
		StatLaw(PunishmentImp p, String key, Init init) {
			super(key, init);
			this.p = p;
		}

		@Override
		double getDD(HCLASS s, Race r, int daysBack) {
			return p.rate(null).getD(daysBack);
		}

	}
	
	
	
}
