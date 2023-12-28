package settlement.room.infra.stockpile;

import static settlement.main.SETT.*;

import java.io.IOException;

import game.*;
import game.faction.FACTIONS;
import game.faction.FResources.RTYPE;
import init.D;
import init.resources.*;
import init.resources.STOCKPILE.StockpileImp;
import settlement.misc.util.RESOURCE_TILE;
import settlement.path.finder.SFinderRoomService;
import settlement.room.main.Room;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.job.ROOM_EMPLOY_AUTO;
import settlement.room.main.job.ROOM_RADIUS.ROOM_RADIUSE;
import settlement.room.main.util.RoomInitData;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.LISTE;
import view.sett.ui.room.UIRoomModule;

public final class ROOM_STOCKPILE extends RoomBlueprintIns<StockpileInstance> implements ROOM_RADIUSE, ROOM_EMPLOY_AUTO{

	private final StockpileTally tally = new StockpileTally();
	
	final Constructor constructor;
	
	final Crate crate = new Crate(this);
	private static CharSequence ¤¤bname = "¤Carry Capacity";
	private static CharSequence ¤¤bdesc = "¤Carry Capacity of all logistics workers.";
	
	static {
		D.ts(ROOM_STOCKPILE.class);
	}
	
	public ROOM_STOCKPILE(RoomInitData init, RoomCategorySub cat) throws IOException {
		super(0, init, "_STOCKPILE", cat);
		constructor = new Constructor(this, init);
		pushBo(init.data(), ¤¤bname, ¤¤bdesc, null, false);
		if (VERSION.versionIsBefore(65, 29)) {
			new GAME_LOAD_FIXER() {
				
				@Override
				protected void fix() {
					tally.saver.clear();
					for (int i = 0; i < instancesSize(); i++) {
						StockpileInstance ins = getInstance(i);
						ins.fixiFix();
						
//						for (RESOURCE res : RESOURCES.ALL()) {
//							tally(res.index(), ins.cratesGet(res), ins.amountTotal[res.index()], ins.amountUnreserved[res.index()], ins.spaceReserved[res.index()], ins.crateSize(), ins.fetchesFromEveryone(res));
//						}
						
					}
					
				}
			};
		}
//		new GAME_LOAD_FIXER() {
//			
//			@Override
//			protected void fix() {
//				for (int i = 0; i < instancesSize(); i++) {
//					StockpileInstance in = getInstance(i);
//					for (RESOURCE rr : RESOURCES.ALL())
//						in.debug(rr);
//					
//				}
//				
//			}
//		};
		
	}
	
	void tally(int res, int crates, int amountTot, int amountUnres, int spaceRes, int crateSize, boolean fetch) {
		
		tally.tally(res, crates, amountTot, amountUnres, spaceRes, crates*crateSize, fetch);
		
	}

	
	@Override
	protected void update(float ds) {
	
		
		
//		tally.needsFetching.clear();
//		for (int i = 0; i < RESOURCE.all().size(); i++) {
//			RESOURCE r = RESOURCE.all().get(i);
//			if (hasSpaceFor(r) && THINGS().resources.hasRandomNext(r)) {
//				tally.needsFetching.add(r);
//			}
//		}
//		tally.fetchI = 0;
		
	}

	
	@Override
	public Furnisher constructor() {
		return constructor;
	}

	@Override
	public SFinderRoomService service(int tx, int ty) {
		return null;
	}
	
	public StockpileTally tally() {
		return tally;
	}
	
	@Override
	protected void saveP(FilePutter saveFile){
		tally.saver.save(saveFile);
	}
	
	@Override
	protected void loadP(FileGetter saveFile) throws IOException{
		tally.saver.load(saveFile);
//		for (COORDINATE c : SETT.TILE_BOUNDS) {
//			StorageCrate cc = crate.get(c.x(), c.y());
//			if (cc != null && (cc.resource() == RESOURCES.BATTLEGEAR()))
//				cc.storageUnreserve(cc.storageReserved());
//		}
		
	}
	
	@Override
	protected void clearP() {
		this.tally.saver.clear();
	}
	
	@Override
	public void appendView(LISTE<UIRoomModule> mm) {
		mm.add(new Gui(this).make());
	}

	@Override
	public byte radiusRaw(Room t) {
		return ((StockpileInstance) t).radius;
	}

	@Override
	public void radiusRawSet(Room t, byte r) {
		((StockpileInstance) t).radius = r;
	}

	@Override
	public boolean autoEmploy(Room r) {
		return ((StockpileInstance) r).autoE;
	}

	@Override
	public void autoEmploy(Room r, boolean b) {
		((StockpileInstance) r).autoE = b;
	}
	
	public STOCKPILE.StockpileImp removeFromEverywhere(double am, RBIT mask, RTYPE record) {
		StockpileImp imp = new StockpileImp();
		
		for (COORDINATE c : TILE_BOUNDS) {
			Room r = ROOMS().STOCKPILE.get(c.x(), c.y());
			if (r == null)
				continue;
			RESOURCE_TILE cr = (RESOURCE_TILE) r.storage(c.x(), c.y());
			if (cr != null && cr.resource() != null && mask.has(cr.resource())) {
				int a = (int) Math.ceil(am*cr.reservable());
				for (int i = 0; i < a; i++) {
					cr.findableReserve();
					cr.resourcePickup();
				}
				FACTIONS.player().res().inc(cr.resource(), record, -a);
				imp.add(cr.resource(), a);
			}
		}
		return imp;
	}
	
	public int remove(RESOURCE res, int total, RTYPE record) {
		
		int tot  = CLAMP.i(total, 0, (int) tally.amountReservable(res));
		
		double d = tally.amountReservable(res);
		if (d == 0)
			return tot;
		d = total/d;
		d = CLAMP.d(d, 0, 1);
		for (int ii = 0; ii < instancesSize(); ii++) {
			StockpileInstance i = getInstance(ii);
			int am = (int) Math.ceil(d*i.amountUnreservedGet(res));
			if (am == 0)
				continue;
			
			for (COORDINATE c : i.body()) {
				if (i.is(c)) {
					RESOURCE_TILE cr = (RESOURCE_TILE) i.storage(c.x(), c.y());
					if (cr != null && cr.resource() == res) {
						
						int a = CLAMP.i(am, 0, cr.reservable());
						for (int k = 0; k < a; k++) {
							cr.findableReserve();
							cr.resourcePickup();
							if (record != null)
								FACTIONS.player().res().inc(cr.resource(), record, -1);
							total --;
							if (total <= 0) {			
								return tot;
							}
						}
					}
				}
			}
			
		}
		
		return tot;
	}
	
}
