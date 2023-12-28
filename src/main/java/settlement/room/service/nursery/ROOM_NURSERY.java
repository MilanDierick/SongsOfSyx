package settlement.room.service.nursery;

import java.io.IOException;

import game.GAME_LOAD_FIXER;
import game.VERSION;
import init.race.RACES;
import init.race.Race;
import settlement.entity.ENTETIES;
import settlement.main.SETT;
import settlement.path.finder.SFinderRoomService;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.Industry.RoomBoost;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.RoomInstance;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.util.RoomInitData;
import settlement.stats.STATS;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.*;
import util.info.INFO;
import view.sett.ui.room.UIRoomModule;

public final class ROOM_NURSERY extends RoomBlueprintIns<NurseryInstance> implements INDUSTRY_HASER {
	
	public final String type = "NURSERY";
	final NurseryConstructor constructor;
	final Industry productionData;
	final int BABY_DAYS;
	public final Race race;
	final Station ss = new Station(this);
	final Util util = new Util(ss);
	int kidSpotsUsed = 0;
	int kidSpotsTotal = 0;
	int babies;
	int limit = ENTETIES.MAX;
	final LIST<Industry> indus;
	
	public ROOM_NURSERY(int index, RoomInitData init, RoomCategorySub block, String key) throws IOException {
		super(index, init, key, block);
		constructor = new NurseryConstructor(this, init);
		pushBo(init.data(), type, true);
		productionData = new Industry(this, init.data(), new RoomBoost[] {new RoomBoost() {
			
			@Override
			public INFO info() {
				return constructor.coziness.info();
			}
			
			@Override
			public double get(RoomInstance r) {
				return 0.25 + 0.75*constructor.coziness.get(r);
			}
		}}, bonus());
		if (productionData.ins().size() == 0) {
			init.data().error("Nurseries must have an in resource (food)", "INDUSTRY");
		}
		BABY_DAYS = init.data().i("INCUBATION_DAYS", 0, Byte.MAX_VALUE);
		race = RACES.map().getByKey("RACE", init.data());

		indus = new ArrayList<>(productionData);
		
		if (VERSION.versionIsBefore(65, 1))
		new GAME_LOAD_FIXER() {
			
			@Override
			protected void fix() {
				babies = 0;
				for (COORDINATE coo : SETT.TILE_BOUNDS) {
					if (ss.init(coo.x(), coo.y())) {
						if (ss.all.get(SETT.ROOMS().data.get(coo)) != 0)
							babies ++;
					}
					
				}
				
			}
		};
	}
	
	@Override
	protected void update(float ds) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Furnisher constructor() {
		return constructor;
	}
	
	@Override
	public SFinderRoomService service(int tx, int ty) {
		return null;
	}

	@Override
	protected void saveP(FilePutter saveFile){
		productionData.save(saveFile);
		saveFile.i(kidSpotsUsed);
		saveFile.i(kidSpotsTotal);
		saveFile.i(babies);
		saveFile.i(limit);
	}
	
	@Override
	protected void loadP(FileGetter saveFile) throws IOException{
		productionData.load(saveFile);
		kidSpotsUsed = saveFile.i();
		kidSpotsTotal = saveFile.i();
		babies = saveFile.i();
		limit = saveFile.i();
	}
	
	@Override
	protected void clearP() {
		productionData.clear();
		kidSpotsUsed = 0;
		kidSpotsTotal = 0;
		babies = 0;
		limit = ENTETIES.MAX;
	}
	
	public int rmax() {
		int m = (int) Math.ceil(STATS.POP().POP.data().get(race)*0.15);
		return m;
	}
	
	public int max() {
		int m = (int) Math.ceil(STATS.POP().POP.data().get(race)*0.15);
		return Math.min(m, limit);
	}

	@Override
	public void appendView(LISTE<UIRoomModule> mm) {
		mm.add(new Gui(this).make());
	}

	@Override
	public LIST<Industry> industries() {
		return indus;
	}
	
	public COORDINATE childGetAndReserveSpot(int tx, int ty) {
		return util.getAndReserveChildSpot(tx, ty);
	}
	
	public void childCancelSpot(int tx, int ty) {
		util.cancelChildSpot(tx, ty);
	}
	
	public boolean childUseSpot(int tx, int ty, boolean eat) {
		return util.useChildSpot(tx, ty, eat);
	}
	
	public DIR childSleepDir(int tx, int ty) {
		return util.getSleepDir(tx, ty);
	}
	
	public boolean childIsReservedSpot(int tx, int ty, Race race) {
		if (race != this.race)
			return false;
		if (ss.init(tx, ty)) {
			if (ss.ins.active() && ss.usedByKid.get() == 1);
			return true;
		}
		return false;
	}
	
	public boolean childIsReservedAndUsableSpot(int tx, int ty, Race race) {
		return childIsReservedSpot(tx, ty, race) && ss.amount.get() == ss.amount.max() && ss.job.jobResourceBitToFetch() == null;
	}
	
	

}
