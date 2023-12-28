package settlement.entry;

import java.io.IOException;
import java.util.Arrays;

import game.faction.FACTIONS;
import init.race.RACES;
import init.race.Race;
import settlement.entity.ENTETIES;
import settlement.entity.humanoid.*;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.colls.StatsService.StatService;
import settlement.stats.util.CAUSE_ARRIVE;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.*;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayListShortResize;
import view.sett.IDebugPanelSett;

final class PeopleSpawner implements SAVABLE{

	private static ArrayListShortResize queue = new ArrayListShortResize(128, ENTETIES.MAX);
	private int rspot = RND.rInt();
	private int currentAmount;
	private int currentType;
	private int currentRace;
	private double time;
	private final int[][] onTheirWay = new int[RACES.all().size()+1][HTYPE.ALL().size()+1];

	public PeopleSpawner() {
		IDebugPanelSett.add("Spawn immigrants", new ACTION() {
			
			@Override
			public void exe() {
				add(FACTIONS.player().race(), HTYPE.SUBJECT, 100);
			}
		});
	}
	
	@Override
	public void save(FilePutter file) {
		queue.save(file);
		file.i(rspot);
		file.i(currentAmount);
		file.i(currentType);
		file.i(currentRace);
		file.d(time);
		file.is(onTheirWay);
		
			
	}

	@Override
	public void load(FileGetter file) throws IOException {
		queue.load(file);
		rspot = file.i();
		currentAmount = file.i();
		currentType = file.i();
		currentRace = file.i();
		time = file.d();
		file.is(onTheirWay);
	}

	@Override
	public void clear() {
		queue.clear();
		currentAmount = 0;
		for (int[] is: onTheirWay)
			Arrays.fill(is, 0);
	}
	
	public int onTheirWay(Race race, HTYPE type) {
		int ri = race == null ? RACES.all().size() : race.index;
		int ti = type == null ? HTYPE.ALL().size() : type.index();
		return CLAMP.i(onTheirWay[ri][ti], 0, Integer.MAX_VALUE);
	}
	
	public void add(Race race, HTYPE type, int amount) {
		if (amount > Short.MAX_VALUE)
			throw new RuntimeException();
		if (amount < 0)
			return;
		queue.add((short) race.index);
		queue.add((short) type.index());
		queue.add((short) amount);
		onTheirWay[race.index][type.index()] += amount;
		onTheirWay[RACES.all().size()][type.index()] += amount;
		onTheirWay[race.index][HTYPE.ALL().size()] += amount;
		onTheirWay[RACES.all().size()][HTYPE.ALL().size()] += amount;
		
	}
	
	void update(double ds) {
		if (currentAmount <= 0) {
			if (!queue.isEmpty()) {
				currentAmount = queue.get(queue.size()-1);
				currentType = queue.get(queue.size()-2);
				currentRace = queue.get(queue.size()-3);
				queue.remove(queue.size()-1);
				queue.remove(queue.size()-1);
				queue.remove(queue.size()-1);
				rspot = RND.rInt();
				
			}
		}
		
		if (currentAmount <= 0) {
			return;
		}
		
		time -= ds;
		while (time < 0) {
			
			if (currentAmount > 0) {
				COORDINATE c = SETT.ENTRY().points.randomReachable(rspot);
				if (c == null)
					return;
				spawn(c);
			}
			time += 1;
			
		}
			
	}
	
	private boolean spawn(COORDINATE spot) {
		
		Race r = RACES.all().get(currentRace);
		HTYPE t = HTYPE.ALL().get(currentType);
		
		DIR d = DIR.get(SETT.TWIDTH/2, SETT.THEIGHT/2, spot.x(), spot.y()).next(2);
		if (!d.isOrtho())
			d = d.next((int) (1*RND.rSign()));
		
		int tx = spot.x() + d.x()*RND.rInt(6);
		int ty = spot.y() + d.y()*RND.rInt(6);
		for (int dd = 0; dd <= 6; dd++) {
			if (SETT.PATH().connectivity.is(tx, ty)) {
				
				
				Humanoid h = SETT.HUMANOIDS().create(r, tx, ty, t, CAUSE_ARRIVE.IMMIGRATED);
				init(h);;
				
				onTheirWay[r.index][t.index()] = CLAMP.i(onTheirWay[r.index][t.index()]-1, 0, Integer.MAX_VALUE);
				onTheirWay[RACES.all().size()][t.index()] = CLAMP.i(onTheirWay[RACES.all().size()][t.index()]-1, 0, Integer.MAX_VALUE);
				onTheirWay[r.index][HTYPE.ALL().size()] = CLAMP.i(onTheirWay[r.index][HTYPE.ALL().size()]-1, 0, Integer.MAX_VALUE);
				onTheirWay[RACES.all().size()][HTYPE.ALL().size()] = CLAMP.i(onTheirWay[RACES.all().size()][HTYPE.ALL().size()]-1, 0, Integer.MAX_VALUE);
				currentAmount--;
				
				return true;
			}
			tx -= d.x();
			ty -= d.y();
		}
		return false;
	}
	
	private void init(Humanoid h) {
		if (h == null || !h.indu().hType().player) {
			return;
		}
		STATS.NEEDS().initNeeds(h);
//		for (STAT s : STATS.all()) {
//			if (s.indu().max(h.indu()) > 0){
//				double d = s.data(h.indu().clas()).getD(h.race())*s.indu().max(h.indu());
//				int ii = (int)d;
//				d -= ii;
//				if (RND.rFloat() < d)
//					ii++;
//				ii = CLAMP.i(ii, 0, s.indu().max(h.indu()));
//				s.indu().set(h.indu(), ii);
//			}
//			
//			
//		}
		HCLASS c = h.indu().clas();
		Race r = h.race();
		for (StatService  s : STATS.SERVICE().allE()) {
			if (s.access().data(c).getD(r) > RND.rFloat()) {
				s.setAccess(h, true, s.quality().data(c).getD(r), s.proximity().data(c).getD(r));
			}
		}
	}
	
}
