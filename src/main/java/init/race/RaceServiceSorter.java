package init.race;

import java.util.Arrays;
import java.util.Comparator;

import init.need.NEED;
import init.need.NEEDS;
import settlement.entity.humanoid.HCLASS;
import settlement.main.SETT;
import settlement.room.service.module.RoomService;
import settlement.room.service.module.RoomServiceNeed;
import settlement.room.spirit.shrine.ROOM_SHRINE;
import settlement.room.spirit.temple.ROOM_TEMPLE;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBurial.StatGrave;
import settlement.stats.standing.StatStanding;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

public final class RaceServiceSorter {
	

	public final LIST<LIST<StatGrave>> GRAVES;
	private final ArrayList<Sort> sorts = new ArrayList<>(NEEDS.ALL().size());
	private final ArrayList<RoomService> tmp = new ArrayList<RoomService>(16);
	
	RaceServiceSorter(Race race) {
		
		
		for (NEED n : NEEDS.ALL())
			sorts.add(new Sort(n, race));
		
		
		
		Sorter<StatGrave> serG = new Sorter<StatGrave>() {

			@Override
			StatStanding standing(StatGrave t) {
				return t.standing();
			}
		};
		
		GRAVES = serG.sort(STATS.BURIAL().graves(), race);


		

		
	}
	
	private static abstract class Sorter<T> {
		
		@SuppressWarnings("unchecked") LIST<LIST<T>> sort(LIST<T> all, Race race) {
			ArrayList<LIST<T>> res = new ArrayList<>(HCLASS.ALL().size());
			
			for (HCLASS dd : HCLASS.ALL()) {
				
				HCLASS c = dd == HCLASS.OTHER ? HCLASS.CITIZEN : dd;
				
				
				int am = 0;
				for (T h : all) {
					if (standing(h).definition(race).get(c).max > 0) {
						am++;
					}
				}
				
				Object[] al = new Object[am];
				am = 0;
				for (T h : all) {
					if (standing(h).definition(race).get(c).max > 0) {
						al[am++] = h;
					}
				}
		
				Arrays.sort((T[])al, new Comparator<T>() {

					@Override
					public int compare(T o1, T o2) {
						double d = standing(o1).definition(race).get(c).max - standing(o2).definition(race).get(c).max;
						if (d < 0)
							return 1;
						if (d > 0)
							return -1;
						return 0;
					}
				});
				res.add(new ArrayList<T>((T[])al));
			}
			dismiss(race, res);
			
			return res;

		}
		
		abstract StatStanding standing(T t);
		
		private void dismiss(Race r, LIST<LIST<T>> ss) {
			
			for (HCLASS c : HCLASS.ALL()) {
				for (int i = 1; i < ss.get(c.index()).size(); i++) {
					standing(ss.get(c.index()).get(i)).definition(r).get(c).dismiss = true;
				}
			}
		}
		
	}
	

	
	public LIST<RoomServiceNeed> services(HCLASS cl, NEED need){
		return sorts.get(need.index()).res.get(cl.index());
	}
	

	
	public LIST<RoomService> services(Induvidual i, NEED need){
		tmp.clearSloppy();
		
		if (need == NEEDS.TYPES().TEMPLE) {
			if (STATS.RELIGION().TEMPLE_TOTAL.standing().max(i.clas(), i.race()) > 0)
				for (ROOM_TEMPLE t : SETT.ROOMS().TEMPLES.perRel.get(STATS.RELIGION().getter.get(i).religion.index()))
					tmp.add(t.service());
			return tmp;
		}
		
		if (need == NEEDS.TYPES().SHRINE) {
			if (STATS.RELIGION().SHRINE_TOTAL.standing().max(i.clas(), i.race()) > 0)
				for (ROOM_SHRINE t : SETT.ROOMS().TEMPLES.perRelShrine.get(STATS.RELIGION().getter.get(i).religion.index()))
					tmp.add(t.service());
			return tmp;
		}
		
		
		for (RoomServiceNeed n : sorts.get(need.index()).res.get(i.clas().index())) {
			tmp.tryAdd(n);
		}
		return tmp;
	}
	
	private class Sort {
		
		public final ArrayList<ArrayList<RoomServiceNeed>> res;
		
		Sort(NEED need, Race race){
			
			res = new ArrayList<ArrayList<RoomServiceNeed>>(HCLASS.ALL.size());
			
			for (HCLASS cl : HCLASS.ALL) {
				int am = 0;
				for (RoomServiceNeed n : need.sGroup().all()) {
					if (n.stats().total().standing().definition(race).get(cl).max > 0)
						am++;
				}
				ArrayList<RoomServiceNeed> ss = new ArrayList<>(am);
				for (RoomServiceNeed n : need.sGroup().all()) {
					if (n.stats().total().standing().definition(race).get(cl).max > 0)
						ss.add(n);
				}
				
				ss.sort(new Comparator<RoomServiceNeed>() {

					@Override
					public int compare(RoomServiceNeed o1, RoomServiceNeed o2) {
						double d1 = o1.stats().total().standing().definition(race).get(cl).max;
						double d2 = o2.stats().total().standing().definition(race).get(cl).max;
						if (d1 < d2)
							return 1;
						if (d1 > d2)
							return -1;
						return 0;
					}
				
				});
				
				res.add(ss);
						
			
			}
			
			for (HCLASS c : HCLASS.ALL()) {
				for (int i = 1; i < res.get(c.index()).size(); i++) {
					res.get(c.index()).get(i).stats().total().standing().definition(race).get(c).dismiss = true;
				}
			}
		}
		
	}

}


