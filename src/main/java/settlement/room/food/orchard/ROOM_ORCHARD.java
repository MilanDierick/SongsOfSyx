package settlement.room.food.orchard;

import java.io.IOException;

import game.time.TIME;
import init.boostable.BOOSTABLE;
import init.boostable.BOOSTABLES;
import init.resources.RESOURCES;
import init.resources.RES_AMOUNT;
import settlement.main.SETT;
import settlement.path.finder.SFinderRoomService;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.Industry.RoomBoost;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.util.RoomInitData;
import snake2d.util.MATH;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.*;
import view.sett.ui.room.UIRoomModule;

public class ROOM_ORCHARD extends RoomBlueprintIns<Instance> implements INDUSTRY_HASER{

	public final static String type = "ORCHARD";
	
	final static double TILES_PER_WORKER = TIME.workSeconds/(OTile.WORK_TIME+TIME.workSecondsWalkNext);
	
	
	final Constructor constructor;
	final Industry productionData;
	public final BOOSTABLE bonus2;
	final LIST<Industry> indus;
	final OTile tile;
	public final RES_AMOUNT auxRes;
	private byte year = -1;
	public final Time time;
	public final double AmountPerTile = TIME.years().bitConversion(TIME.days())/TILES_PER_WORKER;
	public double moisture = 1;
	private static final double mSpeed = 1.0/(TIME.days().bitSeconds()*3);
	
	public ROOM_ORCHARD(RoomInitData data, String key, RoomCategorySub cat, int index) throws IOException {
		super(index, data, key, cat);
		
		constructor = new Constructor(this, data);
		bonus2 = BOOSTABLES.ROOMS().pushRoom(this, data.data(), type);
		productionData = new Industry(this, data.data(), new RoomBoost[] {constructor.fertility, }, bonus2);
		
		indus = new ArrayList<>(productionData);
		this.time = new Time(data);
		auxRes = new RES_AMOUNT.Abs(RESOURCES.map().getByKey("EXTRA_RESOURCE", data.data()), data.data().i("EXTRA_RESOURCE_AMOUNT"));
		tile = new OTile(this);
		
		
		
	}
	
	OTile tile(int tx, int ty) {
		return tile.get(tx, ty);
	}

	@Override
	protected void update(float ds) {
		if (SETT.WEATHER().moisture.growthValue() < 1) {
			moisture -= ds*mSpeed;
			moisture = CLAMP.d(moisture, 0.20, 1);
		}
		
		if (time.dayI() == time.deadDay) {
			moisture = 1;
		}
	}
	
	
	@Override
	public Furnisher constructor() {
		return constructor;
	}
	
	@Override
	public SFinderRoomService service(int tx, int ty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void saveP(FilePutter saveFile){
		productionData.save(saveFile);
		saveFile.b(year);
		saveFile.d(moisture);
	}
	
	@Override
	protected void loadP(FileGetter saveFile) throws IOException{
		productionData.load(saveFile);
		year = saveFile.b();
		moisture = saveFile.d();
	}
	
	@Override
	protected void clearP() {
		productionData.clear();
		year = -1;
		moisture = 1.0;
	}
	
	@Override
	public boolean degrades() {
		return false;
	}

	@Override
	public void appendView(LISTE<UIRoomModule> mm) {
		mm.add(new Gui(this).make());
	}

	@Override
	public LIST<Industry> industries() {
		return indus;
	}
	
	@Override
	public boolean industryIgnoreUI() {
		return true;
	}
	
	public boolean event(int tx, int ty, double severity) {
		Instance ins = getter.get(tx, ty);
		if (ins != null) {
			return ins.event();
		}
		return false;
	}
	
	public static class Time {
		public final int DAYS_TILL_GROWTH;
		public final int ripeDay;
		public final int deadDay;
		public final int days = (int)TIME.years().bitConversion(TIME.days());
		
		Time(RoomInitData data){
			DAYS_TILL_GROWTH = data.data().i("DAYS_TILL_GROWTH", 8, 1024);
			ripeDay = (int) (days*data.data().d("RIPE_AT_PART_OF_YEAR", 0, 1));
			deadDay = MATH.mod(ripeDay + 3, days);
			
			
		}
		
		public boolean isRipe() {
			return isRipe(dayI());
		}
		
		public boolean isRipe(double day) {
			return MATH.isWithin(day, ripeDay, deadDay);
		}
		
		public boolean isDeadDay() {
			return dayI() == deadDay;
		}
		
		public double fruit() {
			return fruit(day());
		}
		
		private double fruit(double day) {

			double rd = (int) (ripeDay+days-2);
			double dd = (int) (deadDay+days-1);
			if (deadDay < ripeDay) {
				dd+=TIME.years().bitConversion(TIME.days());
			}
			
			double di = day + days;
			
			if (di > rd) {
				if (di < dd)
					return CLAMP.d(0.5*(di-rd), 0, 1);
				else if (di > dd)
					return CLAMP.d(1.0-(di-dd), 0, 1);
				return 1.0;
			}
			return 0;
		}
		
		public double day() {
			return TIME.years().bitPartOf()*TIME.years().bitConversion(TIME.days());
		}
		
		public int dayI() {
			return (int) day();
		}
		
		public int daysTillHarvest() {
			return MATH.distance(dayI(), ripeDay, days);
		}
		
	}
	
}
