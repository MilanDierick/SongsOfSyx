package settlement.room.food.cannibal;

import java.io.IOException;
import java.util.Arrays;

import game.time.TIME;
import init.race.RACES;
import init.race.Race;
import init.resources.*;
import settlement.main.SETT;
import settlement.path.finder.SFinderRoomService;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.util.RoomInitData;
import settlement.stats.STATS;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.LISTE;
import util.statistics.HistoryInt;
import view.sett.ui.room.UIRoomModule;

public class ROOM_CANNIBAL extends RoomBlueprintIns<CannibalInstance>{

	final Job job;
	final int[] produced = new int[RESOURCES.ALL().size()];
	private int year = -1;
	double cannibalism;
	private final HistoryInt cannHistory = new HistoryInt(STATS.DAYS_SAVED, TIME.days(), false);
	final Constructor constructor;
	
	private RESOURCE[] resources;
	
	public ROOM_CANNIBAL(RoomInitData init, RoomCategorySub cat) throws IOException {
		super(0, init, "_CANNIBAL", cat);
		
		constructor = new Constructor(this, init);
		job = new Job(this);

	}
	
	RESOURCE[] resources() {
		
		if (resources == null) {
			int am = 0;
			long m = 0;
			for (Race race : RACES.all()) {
				for (RES_AMOUNT r : race.resources()) {
					if ((m & r.resource().bit) == 0) {
						am++;
						m |= r.resource().bit;
					}
				}
			}
			RESOURCE[] res = new RESOURCE[am];
			m = 0;
			am = 0;
			for (Race race : RACES.all()) {
				for (RES_AMOUNT r : race.resources()) {
					if ((m & r.resource().bit) == 0) {
						res[am++] = r.resource();
						m |= r.resource().bit;
					}
				}
			}
			resources = res;
		}
		
		return resources;
	}
	
	@Override
	protected void update(float ds) {
		if (year != TIME.years().bitsSinceStart()) {
			Arrays.fill(produced, 0);
			year = TIME.years().bitsSinceStart();
		}
		
		double d = cannibalism;
		if (d < 1)
			d = 1;
		
		cannibalism -= d*ds/(TIME.years().bitSeconds());
		cannibalism = CLAMP.d(cannibalism, 0, 1.5);
		cannHistory.setD(cannibalism());
		
	}

	@Override
	public SFinderRoomService service(int tx, int ty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void saveP(FilePutter f){
		f.is(produced);
		f.i(year);
		f.d(cannibalism);
		cannHistory.save(f);
	}
	
	@Override
	protected void loadP(FileGetter f) throws IOException{
		f.is(produced);
		year = f.i();
		cannibalism = f.d();
		cannHistory.load(f);
	}
	
	@Override
	protected void clearP() {
		year = -1;
		Arrays.fill(produced, 0);
		cannibalism = 0;
		cannHistory.clear();
	}
	
	@Override
	public Furnisher constructor() {
		return constructor;
	}
	
	@Override
	public void appendView(LISTE<UIRoomModule> mm) {
		
	}
	
	public void reportCannibal() {
		cannibalism += 50.0/STATS.POP().POP.data().get(null);
	}
	
	public void reportCannibal2() {
		cannibalism += 4.0/STATS.POP().POP.data().get(null);
	}
	
	public double cannibalism() {
		return CLAMP.d(cannibalism, 0, 1);
	}
	
	public HistoryInt cannHistory() {
		return cannHistory;
	}
	
	public void setRace(int tx, int ty, Race race) {
		CannibalInstance ins = get(tx, ty);
		if (ins != null) {
			int d = SETT.ROOMS().data.get(tx, ty);
			d = Job.race.set(d, race.index());
			SETT.ROOMS().data.set(ins, tx, ty, d);
		}
			
	}
	

}
