package init.race;

import settlement.entity.humanoid.HCLASS;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBurial.StatGrave;
import settlement.stats.colls.StatsService.StatService;
import settlement.stats.colls.StatsService.StatServiceGroup;
import settlement.stats.standing.StatStanding.StandingDef.StandingData;
import settlement.stats.stat.STAT;
import snake2d.util.sets.*;
import snake2d.util.sprite.text.Str;

final class BioOpinion {

	private final BioOpinionData[] datas;
	private final ArrayList<ArrayList<Prio>> prios = new ArrayList<ArrayList<Prio>>(HCLASS.ALL.size());;
	private final ArrayList<Prio> tmp = new ArrayList<>(STATS.all().size());
	private final double[] tres = new double[] {0.25, 0.5, 0.95};
	
	BioOpinion(BioOpinionData scared, BioOpinionData normal, BioOpinionData confident, Race race) {
		
		this.datas = new BioOpinionData[] {
			scared,
			normal,
			confident
		};
		
		for (HCLASS cl : HCLASS.ALL) {
			
			
			Race r = race;
			Tree<Prio> sort = new Tree<Prio>(STATS.all().size()) {

				@Override
				protected boolean isGreaterThan(Prio current, Prio cmp) {
					return current.prio > cmp.prio;
				}
			
			};
			
			boolean[] has = new boolean[STATS.all().size()];
			for (StatServiceGroup g : STATS.SERVICE().groups()) {
				double m = 0;
				STAT ss = null;
				for (StatService s : g.all()) {
					has[s.total().index()] = true;
					if (s.total().standing().def(cl, r) > m) {
						ss = s.total();
						m = s.total().standing().def(cl, r);
					}
				}
				if (ss != null) {
					double pri = (ss.standing().definition(r).prio)* ss.standing().definition(r).get(cl).max;
					if (pri > 0)
						sort.add(new Prio(ss, pri));
				}
			}
			{
				double m = 0;
				STAT ss = null;
				for (StatGrave s : STATS.BURIAL().graves()) {
					has[s.index()] = true;
					if (s.standing().def(cl, r) > m) {
						ss = s;
						m = s.standing().def(cl, r);
					}
				}
				if (ss != null) {
					double pri = (ss.standing().definition(r).prio)* ss.standing().definition(r).get(cl).max;
					if (pri > 0)
						sort.add(new Prio(ss, pri));
				}
			}
			
			for (STAT s : STATS.all()) {
				if (has[s.index()])
					continue;
				if (s.standing() != null) {
					double pri = (s.standing().definition(r).prio)* s.standing().definition(r).get(cl).max;
					if (pri > 0)
						sort.add(new Prio(s, pri));
				}
				
			}
			
			ArrayList<Prio> pris = new ArrayList<>(sort.size());
			
			
			
			while(sort.hasMore()) {
				
				pris.add(sort.pollGreatest());
			}
			prios.add(pris);
		}
		
	}
	
	private BioOpinionData get(Induvidual indu) {
		return datas[1];
	}
	
	private BioOpinionData get(HCLASS cl, Race race) {
		return datas[1];
	}
	
	public CharSequence title(Induvidual indu, double value) {
		return get(indu).title(indu, value);
	}
	
	public void get(LIST<Str> res, HCLASS cl, Race race, long ran) {
		for (Str s : res)
			s.clear();
		
		BioOpinionData data = get(cl, race);
		
		int index = 0;
		if ((ran & 0x01F) == 0) {
			res.get(index++).add(data.funny(ran));
		}
		ArrayList<Prio> pp = prios.get(cl.index());
		
		{
			tmp.clearSloppy();
			long r = ran;
			for (Prio p : pp) {
				if ((r & 0b1) == 1) {
					tmp.add(p);
				}
				r = r >> 1;
			}
		}
		

		
		for (double t : tres) {
			for (int i = 0; i < tmp.size(); i++) {
				Prio p = tmp.get(i);
				double v = value(p, cl, race, p.stat.data(cl).getD(race));
				if (v < t) {
					res.get(index++).add(data.get(p.stat, cl, race, ran));
					tmp.removeOrdered(i);
					i--;
					if (index >= res.size())
						return;
				}
			}
		}
		
		{
			tmp.clearSloppy();
			long r = ran;
			for (Prio p : pp) {
				if ((r & 0b1) == 0) {
					tmp.add(p);
				}
				r = r >> 1;
			}
		}
		
		for (double t : tres) {
			for (int i = 0; i < tmp.size(); i++) {
				Prio p = tmp.get(i);
				double v = value(p, cl, race, p.stat.data(cl).getD(race));
				if (v < t) {
					res.get(index++).add(data.get(p.stat, cl, race, ran));
					tmp.removeOrdered(i);
					i--;
					if (index >= res.size())
						return;
				}
			}
		}
		
		if (res.size() == 0) {
			res.get(index).add(data.full(ran));
		}
		
	}
	
	public void get(LIST<Str> res, Induvidual indu) {
		for (Str s : res)
			s.clear();
		
		BioOpinionData data = get(indu);
		
		long ran = STATS.RAN().get(indu, 0);
		ran = ran << 32;
		ran |= STATS.RAN().get(indu, 36);
		
		int index = 0;
		if ((ran & 0x01F) == 0) {
			res.get(index++).add(data.funny(ran));
		}
		ArrayList<Prio> pp = prios.get(indu.clas().index());
		
		{
			tmp.clearSloppy();
			long r = ran;
			for (Prio p : pp) {
				if ((r & 0b1) == 1) {
					tmp.add(p);
				}
				r = r >> 1;
			}
		}
		
		for (double t : tres) {
			for (int i = 0; i < tmp.size(); i++) {
				Prio p = tmp.get(i);
				double v = value(p, indu.clas(), indu.race(), p.stat.indu().getD(indu));
				if (v < t) {
					res.get(index++).add(data.get(p.stat, indu.clas(), indu.race(), ran));
					tmp.removeOrdered(i);
					i--;
					if (index >= res.size())
						return;
				}
			}
		}
		
		{
			tmp.clearSloppy();
			long r = ran;
			for (Prio p : pp) {
				if ((r & 0b1) == 0) {
					tmp.add(p);
				}
				r = r >> 1;
			}
		}
		
		for (double t : tres) {
			for (int i = 0; i < tmp.size(); i++) {
				Prio p = tmp.get(i);
				double v = value(p, indu.clas(), indu.race(), p.stat.indu().getD(indu));
				if (v < t) {
					res.get(index++).add(data.get(p.stat, indu.clas(), indu.race(), ran));
					tmp.removeOrdered(i);
					i--;
					if (index >= res.size())
						return;
				}
			}
		}
		
		if (res.size() == 0) {
			res.get(index).add(data.full(ran));
		}
		
	}
	
	private double value(Prio p, HCLASS cl, Race r, double v) {
		StandingData def =  p.stat.standing().definition(r).get(cl);
		if (def.from > def.to) 
			v = 1.0-v;
		return v;
	}

	private static final class Prio {
		
		private final STAT stat;
		private final float prio;
		
		Prio(STAT stat, double prio){
			this.stat = stat;
			this.prio = (float) prio;
		}
		
	}
}
