package settlement.room.law.execution;

import java.io.IOException;

import init.race.Race;
import settlement.main.SETT;
import settlement.misc.util.FSERVICE;
import settlement.path.finder.SFinderRoomService;
import settlement.room.law.PUNISHMENT_SERVICE;
import settlement.room.main.*;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.job.ROOM_EMPLOY_AUTO;
import settlement.room.main.util.RoomInitData;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LISTE;
import view.sett.ui.room.UIRoomModule;

public final class ROOM_EXECTUTION extends RoomBlueprintIns<ExecutionInstance> implements ROOM_EMPLOY_AUTO, PUNISHMENT_SERVICE{

	public SFinderRoomService finder = new SFinderRoomService(this.info.name) {
		
		@Override
		public FSERVICE get(int tx, int ty) {
			// TODO Auto-generated method stub
			return null;
		}
	};
	final Constructor constructor;
	private int executions;
	private int total;
	
	public ROOM_EXECTUTION(RoomInitData init, RoomCategorySub block) throws IOException {
		super(0, init, "_EXECUTION", block);
		
		constructor = new Constructor(this, init);

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
	public int punishUsed() {
		return executions;
	}
	
	@Override
	public int punishTotal() {
		return total;
	}


	void incPrisoners(int current, int total){
		this.executions += current;
		this.total += total;
	}


	@Override
	protected void saveP(FilePutter f){
		f.i(executions);
		f.i(total);
	}
	
	@Override
	protected void loadP(FileGetter f) throws IOException{
		executions = f.i();
		total = f.i();
	}
	
	@Override
	protected void clearP() {
		executions = 0;
		total = 0;
	}

	@Override
	public SFinderRoomService service(int tx, int ty) {
		return finder;
	}
	
	@Override
	public void appendView(LISTE<UIRoomModule> mm) {
		mm.add(new Gui(this).make());
	}

	@Override
	public boolean autoEmploy(Room r) {
		return ((ExecutionInstance)r).autoEmploy;
	}

	@Override
	public void autoEmploy(Room r, boolean b) {
		((ExecutionInstance)r).autoEmploy = b;
	}
	
	public ExecutionStation exectuionReserve() {
		if (executions == total)
			return null;
		int i = RND.rInt(instancesSize());
		for (int k = 0; k < instancesSize(); k++) {
			ExecutionInstance ins = getInstance((k+i)%instancesSize());
			if (ins.active() && ins.executions() < ins.total()) {
				return ins.reserveSpot();
			}
		}
		throw new RuntimeException();
	}
	
	public ExecutionStation executionSpot(COORDINATE c) {
		if (is(c)) {
			return ExecutionStation.init(c.x(), c.y());
		}
		return null;
	}
	
	public ExecutionStation workReserve(Room r) {
		ExecutionInstance ins = (ExecutionInstance) r;
		return ins.work();
	}
	
	public COORDINATE dumpCorpe(RoomInstance r) {
		int tx = r.body().x1() + RND.rInt(r.body().width());
		int ty = r.body().y1() + RND.rInt(r.body().height());
		int am = r.body().width()*r.body().height();
		while(am-- > 0) {
			if (r.is(tx, ty) && SETT.ROOMS().fData.sprite.get(tx, ty) == null) {
				Coo.TMP.set(tx, ty);
				return Coo.TMP;
			}
			tx++;
			if (tx > r.body().width()) {
				tx = r.body().x1();
				ty++;
				if (ty > r.body().height())
					ty = r.body().x1();
			}
		}
		return null;
	}
	
	public boolean shouldCheer(int tx, int ty) {
		ExecutionInstance ins = getter.get(tx, ty);
		return ins != null && ins.executions() > 0;
	}
	
	public void renderHead(int x, int y, Race race, int ran) {
		
	}

}
