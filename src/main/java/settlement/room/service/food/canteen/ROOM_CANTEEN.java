package settlement.room.service.food.canteen;

import java.io.IOException;
import java.util.Arrays;

import game.GAME;
import init.resources.*;
import settlement.misc.util.FSERVICE;
import settlement.path.finder.SFinderRoomService;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.Industry.RoomBoost;
import settlement.room.main.Room;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.job.ROOM_EMPLOY_AUTO;
import settlement.room.main.util.RoomInitData;
import settlement.room.service.module.RoomServiceDataAccess;
import settlement.room.service.module.RoomServiceDataAccess.ROOM_SERVICE_ACCESS_HASER;
import settlement.stats.STATS;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.rnd.RND;
import snake2d.util.sets.*;
import view.sett.ui.room.UIRoomModule;

public final class ROOM_CANTEEN extends RoomBlueprintIns<CanteenInstance> implements ROOM_EMPLOY_AUTO, ROOM_SERVICE_ACCESS_HASER, INDUSTRY_HASER{

	final Constructor constructor;
	final Industry industryFuel;
	final RoomServiceDataAccess service;
	final long[] amounts = new long[RESOURCES.EDI().all().size()];
	long total;
	final SService food = new SService(this);
	final SOven job = new SOven(this);
	final SChair chair = new SChair(this);
	final LIST<Industry> indus;
	
	
	public ROOM_CANTEEN(String key, int index, RoomInitData data, RoomCategorySub cat) throws IOException {
		super(index, data, key, cat);
		constructor = new Constructor(this, data);
		industryFuel = new Industry(this, data.data(), (RoomBoost) null, null);
		service = new RoomServiceDataAccess(this, data) {
			
			@Override
			public FSERVICE service(int tx, int ty) {
				return food.get(tx, ty);
			}

			@Override
			public double totalMultiplier() {
				return 1.0/STATS.NEEDS().HUNGER.rate.get(null, null);
			}
		};

		indus = new ArrayList<>(industryFuel);
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
		industryFuel.save(saveFile);
		service.saver.save(saveFile);
		saveFile.l(total);
		saveFile.ls(amounts);
	}
	
	@Override
	protected void loadP(FileGetter saveFile) throws IOException{
		industryFuel.load(saveFile);
		service.saver.load(saveFile);
		total = saveFile.l();
		saveFile.ls(amounts);
	}
	
	@Override
	protected void clearP() {
		industryFuel.clear();
		service.saver.clear();
		total = 0;
		Arrays.fill(amounts, 0l);
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
		return ((CanteenInstance) r).autoE;
	}

	@Override
	public void autoEmploy(Room r, boolean b) {
		((CanteenInstance) r).autoE = b;
	}

	@Override
	public RoomServiceDataAccess service() {
		return service;
	}
	
	public short grab(Edible pref, int amount, int tx, int ty) {
		CanteenInstance ins = getter.get(tx, ty);
		if (ins == null)
			return Meal.make(pref, 0);
		FSERVICE f = food.get(tx, ty);
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
			GAME.player().res().outConsumed.inc(pref.resource, am);
			
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
					GAME.player().res().outConsumed.inc(e.resource, am);
					return Meal.make(e, am);
				}
				if (ins.amount(e) > al) {
					largest = e;
					al = ins.amount(e);
				}
					
			}
			
			if (largest != null && al > 0) {
				if (al > am)
					al = am;
				GAME.player().res().outConsumed.inc(largest.resource, al);
				ins.consume(largest, al, tx, ty);
				return Meal.make(largest, al);
			}
			
		}

		GAME.Notify("weird! + " + ins.amountTotal() + " " + ins.serviceReserved());
		
		return Meal.make(pref, 0);
		
	}
	
	public COORDINATE getChair(int tx, int ty) {
		return chair.get(tx, ty);
	}
	
	public DIR setChair(int tx, int ty, short mealData) {
		return chair.set(tx, ty, mealData);
	}
	
	public void returnChair(int tx, int ty) {
		chair.returnTable(tx, ty);
	}


	@Override
	public LIST<Industry> industries() {
		return indus;
	}
	
}
