package init.race;

import static settlement.main.SETT.*;

import java.util.Arrays;
import java.util.Comparator;

import settlement.entity.humanoid.HCLASS;
import settlement.main.SETT;
import settlement.room.service.lavatory.ROOM_LAVATORY;
import settlement.room.service.module.RoomServiceDataAccess.ROOM_SERVICE_ACCESS_HASER;
import settlement.stats.STANDING;
import settlement.stats.STATS;
import settlement.stats.StatsBurial.StatGrave;
import snake2d.util.sets.*;

public final class RaceServiceSorter {
	
	public final LIST<LIST<ROOM_SERVICE_ACCESS_HASER>> HYGINE;
	public final LIST<LIST<ROOM_SERVICE_ACCESS_HASER>> EAT;
	public final LIST<LIST<ROOM_SERVICE_ACCESS_HASER>> DRINK;
	public final LIST<LIST<ROOM_LAVATORY>> SHIT;
	public final LIST<LIST<StatGrave>> GRAVES;
	
	private final boolean[][] entertains = new boolean[HCLASS.ALL.size()][SETT.ROOMS().ENTERTAINMENT.size()];
	private final int[][] entertainsIs = new int[HCLASS.ALL.size()][];
	
	public final LIST<LIST<ROOM_SERVICE_ACCESS_HASER>> ENTERTAINMENT;
	
	RaceServiceSorter(Race race) {
		
		Sorter<ROOM_SERVICE_ACCESS_HASER> ser = new Sorter<ROOM_SERVICE_ACCESS_HASER>() {

			@Override
			STANDING standing(ROOM_SERVICE_ACCESS_HASER t) {
				return t.service().stats().total().standing();
			}
		
		};
		
		EAT = ser.sort(ROOMS().EAT, race);
		HYGINE = ser.sort(ROOMS().HYGINE, race);
		DRINK = ser.sort(ROOMS().DRINK, race);
		SHIT = new Sorter<ROOM_LAVATORY>() {

			@Override
			STANDING standing(ROOM_LAVATORY t) {
				return t.service().stats().total().standing();
			}
		
		}.sort(ROOMS().LAVATORIES, race);
		
		Sorter<StatGrave> serG = new Sorter<StatGrave>() {

			@Override
			STANDING standing(StatGrave t) {
				return t.standing();
			}
		};
		
		GRAVES = serG.sort(STATS.BURIAL().graves(), race);

		{
			ArrayList<LIST<ROOM_SERVICE_ACCESS_HASER>> all = new ArrayList<>(HCLASS.ALL.size());
			
			int am = 0;
			for (HCLASS s : HCLASS.ALL) {
				int i = 0;
				for (ROOM_SERVICE_ACCESS_HASER ss : SETT.ROOMS().ENTERTAINMENT) {
					if (ss.service().stats().total().standing().max(s, race) > 0) {
						entertains[s.index()][i] = true;
						am++;
					}
					i++;
				}
				entertainsIs[s.index()] = new int[am];
				i = 0;
				int k = 0;
				for (ROOM_SERVICE_ACCESS_HASER ss : SETT.ROOMS().ENTERTAINMENT) {
					if (ss.service().stats().total().standing().max(s, race) > 0) {
						entertainsIs[s.index()][k++] = i;
						am++;
					}
					i++;
				}
			}
			
			for (HCLASS s : HCLASS.ALL) {
				LinkedList<ROOM_SERVICE_ACCESS_HASER> ra = new LinkedList<>();
				for (ROOM_SERVICE_ACCESS_HASER ss : SETT.ROOMS().ENTERTAINMENT) {
					if (ss.service().stats().total().standing().max(s, race) > 0) {
						ra.add(ss);
					}
				}
				LIST<ROOM_SERVICE_ACCESS_HASER> l = new ArrayList<>(ra);
				all.add(l);
			}
			ENTERTAINMENT = all;
		}
		

		
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
		
		abstract STANDING standing(T t);
		
		private void dismiss(Race r, LIST<LIST<T>> ss) {
			
			for (HCLASS c : HCLASS.ALL()) {
				for (int i = 1; i < ss.get(c.index()).size(); i++) {
					standing(ss.get(c.index()).get(i)).definition(r).get(c).dismiss = true;
				}
			}
		}
		
	}
	
	public boolean entertainsAny(HCLASS cl) {
		return entertainsIs[cl.index()].length > 0;
	}
	
	public boolean entertains(HCLASS cl, int ei) {
		return entertains[cl.index()][ei];
	}
	
	public int[] entertainIs(HCLASS cl) {
		return entertainsIs[cl.index()];
	}
}


