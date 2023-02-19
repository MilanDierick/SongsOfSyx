package settlement.room.service.food.eatery;

import java.io.IOException;

import game.GAME;
import init.resources.*;
import settlement.misc.util.FSERVICE;
import settlement.path.finder.SFinderRoomService;
import settlement.room.industry.module.Industry;
import settlement.room.main.Room;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.job.ROOM_EMPLOY_AUTO;
import settlement.room.main.util.RoomInitData;
import settlement.room.service.module.RoomServiceDataAccess;
import settlement.room.service.module.RoomServiceDataAccess.ROOM_SERVICE_ACCESS_HASER;
import settlement.stats.STATS;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LISTE;
import view.sett.ui.room.UIRoomModule;

public final class ROOM_EATERY extends RoomBlueprintIns<EateryInstance> implements ROOM_EMPLOY_AUTO, ROOM_SERVICE_ACCESS_HASER{

	
	final Constructor constructor;
	final Crate crate = new Crate(this);
	final Industry industry;
	final RoomServiceDataAccess service;
	final long[] amounts = new long[RESOURCES.EDI().all().size()];
	long total;

	public ROOM_EATERY(String key, int index, RoomInitData data, RoomCategorySub cat) throws IOException {
		super(index, data, key, cat);
		constructor = new Constructor(this, data);
		industry = new Industry(this, 
				RESOURCES.EDI().makeArray(), new double[RESOURCES.EDI().all().size()], 
				null, null, 
				null, null);
		service = new RoomServiceDataAccess(this, data) {
			
			@Override
			public FSERVICE service(int tx, int ty) {
				return crate.service(tx, ty);
			}
			
			@Override
			public double totalMultiplier() {
				return 2.0/STATS.NEEDS().HUNGER.rate.get(null, null);
			}
		};
	}


	@Override
	protected void update(float ds) {
		
	}

	
	@Override
	public Furnisher constructor() {
		return constructor;
	}

	@Override
	public SFinderRoomService service(int tx, int ty) {
		return service.finder;
	}
	
	

	
	@Override
	protected void saveP(FilePutter saveFile){
		industry.save(saveFile);
		service.saver.save(saveFile);
		saveFile.l(total);
	}
	
	@Override
	protected void loadP(FileGetter saveFile) throws IOException{
		industry.load(saveFile);
		service.saver.load(saveFile);
		total = saveFile.l();
	}
	
	@Override
	protected void clearP() {
		industry.clear();
		service.saver.clear();
		total = 0;
	}
	
	public long totalFood() {
		return total;
	}
	
	public long amount(Edible e) {
		return amounts[e.index()];
	}
	
	@Override
	public void appendView(LISTE<UIRoomModule> mm) {
		mm.add(new Gui(this).make());
	}

	@Override
	public boolean autoEmploy(Room r) {
		return ((EateryInstance) r).autoE;
	}

	@Override
	public void autoEmploy(Room r, boolean b) {
		((EateryInstance) r).autoE = b;
	}

	public Industry industry() {
		return industry;
	}

	@Override
	public RoomServiceDataAccess service() {
		return service;
	}
	
	public short eat(Edible pref, int amount, int tx, int ty) {
		EateryInstance ins = getter.get(tx, ty);
		if (ins == null)
			return Meal.make(pref, 0);
		FSERVICE f = crate.service(tx, ty);
		if (f == null)
			return Meal.make(pref, 0);
		
		f.consume();
		
		int am = amount;
		if (amount > ins.amountTotal()-ins.serviceReserved()+1) {
			am = ins.amountTotal()-ins.serviceReserved()+1;
		}

		
		
		
		if (am < 0)
			GAME.Notify("here! " + am + " " + tx + " " + ty);
		
		
		if (ins.amount(pref) >= am) {
			ins.consume(pref, am, tx, ty);
			industry.ins().get(pref.index()).inc(ins, am);
			return Meal.make(pref, am);
		}
		
		if (ins.amountTotal() > 0) {

			int k = RND.rInt(RESOURCES.EDI().all().size());
			
			Edible largest = null;
			int al = -1;
			for (int i = 0; i < RESOURCES.EDI().all().size(); i++) {
				Edible e = RESOURCES.EDI().all().get((i+k)%RESOURCES.EDI().all().size());
				if (ins.amount(e) > am) {
					ins.consume(e, am, tx, ty);
					industry.ins().get(e.index()).inc(ins, am);
					return Meal.make(e, am);
				}
				if (ins.amount(e) > al) {
					largest = e;
					al = ins.amount(e);
				}
					
			}
			
			if (largest != null) {
				if (al > am)
					al = am;
				industry.ins().get(largest.index()).inc(ins, al);
				ins.consume(largest, al, tx, ty);
				return Meal.make(largest, al);
			}
			
		}

		GAME.Notify("weird!" + " " + tx + " " + ty + " " + am + " " + ins.amountTotal() + " " + ins.serviceReserved());
		return Meal.make(pref, 0);
		
	}
	
}
