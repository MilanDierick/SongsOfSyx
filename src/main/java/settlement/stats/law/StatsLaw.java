package settlement.stats.law;

import init.race.Race;
import settlement.entity.humanoid.HCLASS;
import settlement.stats.Induvidual;
import settlement.stats.StatsInit;
import settlement.stats.law.Processing.PunishmentDec;
import settlement.stats.law.Processing.PunishmentImp;
import settlement.stats.standing.StatStanding;
import settlement.stats.stat.*;
import snake2d.util.sets.*;
import util.data.GETTER_TRANS.GETTER_TRANSE;
import util.data.INT_O.INT_OE;

public class StatsLaw extends StatCollection{
	
	public final LAW law;
	public STAT EQUALITY;
	public STAT CRIME;
	public final LIST<StatLaw> punishments;
	public final GETTER_TRANSE<Induvidual, PRISONER_TYPE> prisonerType;
	
	public StatsLaw(StatsInit init){
		super(init, "LAW");
		law = new LAW(init);
		
		for (PunishmentImp p : LAW.process().punishments) {
			new StatLaw(p, p.key, init);
		}
		for (PunishmentImp p : LAW.process().extras) {
			new StatLaw(p, p.key, init);
		}

		EQUALITY = new STATImp("EQUALITY", init) {
			
			@Override
			protected int getDD(HCLASS s, Race r) {
				
				double d = 0;
				double t = 0;
				for (PunishmentDec p : LAW.process().punishmentsdec) {
					d += p.limit(r)*p.multiplier;
					t += p.limit(null)*p.multiplier;
				}
				
				if (d*0.6 > t) {
					return (int) (pdivider(s, r, 0)*t/d); 
				}
				
				return pdivider(s, r, 0);
			}
		};
		EQUALITY.standing = new StatStanding(EQUALITY, 1.0);
		
		CRIME = new STATFacade("CRIME", init) {
			
			@Override
			protected double getDD(HCLASS s, Race r, int daysBack) {
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
	
		LinkedList<StatLaw> ll = new LinkedList<>();
		for (STAT s : all())
			if (s instanceof StatLaw)
				ll.add((StatLaw) s);
		
		punishments = new ArrayList<StatsLaw.StatLaw>(ll);
		
		
	}

	public static class StatLaw extends STATFacade {

		public final PunishmentImp p;
		
		StatLaw(PunishmentImp p, String key, StatsInit init) {
			super(key, init, new StatInfo(p.name, p.desc));
			this.p = p;
		}

		@Override
		protected double getDD(HCLASS s, Race r, int daysBack) {
			return p.rate(null).getD(daysBack);
		}

	}
	
	
	
}
