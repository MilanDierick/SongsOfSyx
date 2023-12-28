package game.tourism;

import java.io.IOException;

import game.time.TIME;
import init.C;
import init.D;
import init.race.RACES;
import init.race.Race;
import settlement.entity.humanoid.*;
import settlement.main.SETT;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.throne.THRONE;
import settlement.room.service.module.RoomServiceNeed;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.LOG;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.rnd.RND;
import snake2d.util.sets.*;
import util.updating.IUpdater;
import view.main.VIEW;
import view.sett.IDebugPanelSett;
import view.ui.message.MessageText;

final class Updater extends IUpdater{

	
	private final LIST<TRace> races;
	
	private int mI = 0;
	private static CharSequence ¤¤MessageReady = "¤Our city is now impressive enough to attract tourists. We should build an inn in order to accept them. Tourists are a good source of income!";
	private static CharSequence ¤¤MessageFirst = "¤Congratulations! our first ever tourist has arrived in our city. Make sure everything runs smooth and we should hopefully get a good review that will attract others.";
	private final Coo entry = new Coo(-1, -1);
	
	static {
		D.ts(Updater.class);
	}
	
	Updater(){
		super(TOURISM.AMOUNT, TIME.secondsPerDay*16);
		double tot = 0;
		
		for (Race r : RACES.all()) {
			tot += r.tourism().occurence;
		}
		
		LinkedList<TRace> li = new LinkedList<>();
		for (Race r : RACES.all()) {
			if (r.tourism().occurence > 0)
				li.add(new TRace(r, r.tourism().occurence/tot));
		}
		races = new ArrayList<>(li);
		
		IDebugPanelSett.add("TOURIST_SPAWN", new ACTION() {
			
			@Override
			public void exe() {
				Race r = races.rnd().race;
				RoomBlueprintIns<?> a = r.tourism().attractions.rnd();
				spawn(r, a);
			}
		});
		
		IDebugPanelSett.add("TOURIST_SPAWN_TRY", new ACTION() {
			
			@Override
			public void exe() {
				Race r = getRace();
				RoomBlueprintIns<?> a = getAttraction(r);
				LOG.ln(r + " " + a);
				if (a != null)
					LOG.ln(spawn(r, a));
			}
		});
		
	}
	
	@Override
	public void save(FilePutter file) {
		file.i(mI);
		super.save(file);
	}
	
	@Override
	public void load(FileGetter file) throws IOException {
		mI = file.i();
		super.load(file);
	}
	
	
	private static class TRace {
		
		public final Race race;
		public final double occ;
		
		private TRace(Race race, double occ) {
			this.race = race;
			this.occ = occ;
		}
		
	}

	@Override
	protected void update(int index, double timeSinceLast) {
		
		
		
		if (0.25 + TOURISM.score() < RND.rFloat()*1.25)
			return;
		
		if (!canAttract())
			return;
		
		if (mI >= 2) {
			if (!SETT.ROOMS().INN.service().finder.has(THRONE.coo()))
				return;
		}
		
		Race r = getRace();
		RoomBlueprintIns<?> a = getAttraction(r);
		
		if (a != null) {
			if (mI == 0 && !SETT.ROOMS().INN.service().finder.has(THRONE.coo())) {
				mI = 1;
				new MessageText(HTYPE.TOURIST.names, ¤¤MessageReady).send();
				return;
			}
			if (mI < 2) {
				
				Humanoid h = spawn(r, a);
				if (h != null) {
					if (new MessageText(HTYPE.TOURIST.names, ¤¤MessageFirst).send()) {
						mI = 2;
						VIEW.s().getWindow().centererTile.set(h.tc());
					}
				}
				return;
			}
			spawn(r, a);
			
			
		}
			
		
	}
	
	public static RoomServiceNeed getService(Induvidual in) {
		int ri = STATS.RAN().get(in, 4)&0x0FFF;
		for (int i = 0; i < SETT.ROOMS().SERVICE.needs().size(); i++) {
			int k = ri+i;
			RoomServiceNeed a = SETT.ROOMS().SERVICE.needs().getC(k);
			if (a.stats().total().standing.definition(in.race()).get(HCLASS.CITIZEN).to > 0)
				return a;
		}
		return null;
	}
	
	private Race getRace() {
		double d = RND.rFloat()-0.05;
		for (TRace r : races) {
			d -= r.occ;
			if (d <= 0) {
				return r.race;
			}
		}
		return null;
	}
	
	private RoomBlueprintIns<?> getAttraction(Race race) {
		int em = 0;
		int most = 0;
		RoomBlueprintIns<?> best = null;
		int tot = 0;
		LIST<RoomBlueprintIns<?>> li = race.tourism().attractions;
		
		
		
		for (int bi = 0; bi < li.size(); bi++) {
			int e = li.get(bi).employment().employed()-TOURISM.MIN_EMPLOYEES;
			tot += e;
			if (e > 0) {
				em += e;
				if (e > most) {
					most = e;
					best = li.get(bi);
				}
			}
		}
		
		if (most <= 0)
			return null;
		
		if (tot < TOURISM.MAX_EMPLOYEES*RND.rFloat())
			return null;
		
		if((em-most)/race.tourism().attractions.size() > RND.rInt(most)) {
			int other = (int) (RND.rFloat()*(em-most));
			for (int bi = 0; bi < li.size(); bi++) {
				int e = li.get(bi).employment().employed()-TOURISM.MIN_EMPLOYEES;
				if (e > 0) {
					other -= e;
					if (other <= 0)
						return li.get(bi);
				}
			}
		}
		return best;
	}
	
	private boolean canAttract() {
		if (SETT.ENTRY().isClosed())
			return false;
		COORDINATE c = SETT.ENTRY().points.randomReachable();
		if (c == null)
			return false;
		entry.set(c);
		return true;
	}
	
	private Humanoid spawn(Race race, RoomBlueprintIns<?> blue) {
		
		if (!canAttract())
			return null;
		
		Humanoid h = new Humanoid(entry.x()*C.TILE_SIZE+C.TILE_SIZEH, entry.y()*C.TILE_SIZE+C.TILE_SIZEH, race, HTYPE.TOURIST, null);
		
		if (h.isRemoved())
			return null;
		
		STATS.WORK().profession.set(h.indu(), blue);
		RoomServiceNeed s = getService(h.indu());
		if (s != null) {
			s.clearAccess(h);
			s.group.need.stat().setPrio(h.indu(), 1.0);
		}
		
		TOURISM.self.history.inc(1);

		return h;
		
		
	}
	
	public static RoomBlueprintIns<?> attraction(Induvidual indu) {
		RoomBlueprintImp r = STATS.WORK().profession.get(indu);
		if (r == null || !(r instanceof RoomBlueprintIns<?>))
			return indu.race().tourism().getAttraction(0);
		return (RoomBlueprintIns<?>) r;
	}
	

	
}
