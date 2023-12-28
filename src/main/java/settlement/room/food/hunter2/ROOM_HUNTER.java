package settlement.room.food.hunter2;

import java.io.IOException;

import game.time.TIME;
import init.D;
import settlement.entity.humanoid.Humanoid;
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
import settlement.thing.ThingsCadavers.Cadaver;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.*;
import snake2d.util.sprite.text.Str;
import util.data.DOUBLE_O;
import util.dic.DicMisc;
import util.info.INFO;
import view.sett.ui.room.UIRoomModule;
import world.regions.Region;

public class ROOM_HUNTER extends RoomBlueprintIns<HunterInstance> implements INDUSTRY_HASER{

	public static final String type = "HUNTER";
	
	final Constructor constructor;
	final LIST<Industry> indus;
	final Tile tile;
	
	public final int MAX_EMPLOYED;
	
	public static CharSequence ¤¤emp = "The more employees you have, the less efficient this industry will become. The max amount for this room is {0}. Employees after that point will decrease the output gradually."; 
	
	static {
		D.ts(ROOM_HUNTER.class);
	}
	
	public final RoomBoost bEmployed;
	
	public ROOM_HUNTER(int index, RoomInitData init, String key, RoomCategorySub cat) throws IOException {
		super(index, init, key, cat);
		
		constructor = new Constructor(this, init);
		pushBo(init.data(), type, true);
		MAX_EMPLOYED = init.data().i("MAX_EMPLOYED", 1, 10000);
		
		
		
		INFO info = new INFO(DicMisc.¤¤Employees, "" + Str.TMP.clear().add(¤¤emp).insert(0, MAX_EMPLOYED));
		
		bEmployed = new RoomBoost() {
			@Override
			public INFO info() {
				return info;
			}
			
			@Override
			public double get(RoomInstance r) {
				return eBonus(0);
			}
		};
		
		DOUBLE_O<Region> rrr = new DOUBLE_O<Region>() {

			@Override
			public double getD(Region t) {
				return 1 - t.info.fertility();
			}
			
		};
		
		indus = INDUSTRY_HASER.createIndustries(this, init, new RoomBoost[] {bEmployed, constructor.efficiency}, bonus(), rrr);
		tile = new Tile(this);
	}
	
	public double eBonus(int delta) {
		double emp = employment().employed()+delta;
		if (emp < MAX_EMPLOYED)
			return 1.0;
		double d = 1 + (emp-MAX_EMPLOYED)/(MAX_EMPLOYED);
		return 1.0/d;
	}
	
	@Override
	protected void update(float ds) {
		
	}

	@Override
	public SFinderRoomService service(int tx, int ty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void saveP(FilePutter saveFile){
		for (Industry i : indus)
			i.save(saveFile);
	}
	
	@Override
	protected void loadP(FileGetter saveFile) throws IOException{
		for (Industry i : indus)
			i.load(saveFile);
	}
	
	@Override
	protected void clearP() {
		for (Industry i : indus)
			i.clear();
	}
	
	@Override
	public Furnisher constructor() {
		return constructor;
	}
	
	@Override
	public void appendView(LISTE<UIRoomModule> mm) {
		
	}

	@Override
	public LIST<Industry> industries() {
		return indus;
	}
	
	public void resetGore(COORDINATE c) {
		tile.reset(getter.get(c), c);
	}
	
	public void gore(COORDINATE c) {
		tile.gore(getter.get(c), c);
	}

	public COORDINATE reserveWork(RoomInstance inss, Humanoid h) {
		
		COORDINATE start = h.tc();
		
		
		HunterInstance ins = (HunterInstance) inss;
		
		for (DIR d : DIR.ORTHO) {
			Tile j = tile.init(start.x()+d.x(), start.y()+d.y(), ins);
			if (j != null && j.reserved.get() == 0 )
				return clean(j);
		}
		
		
		ArrayCooShort coos = ins.coos;
		for (int i = 0; i < coos.size(); i++) {
			coos.inc();
			Tile j = tile.init(coos.get().x(), coos.get().y(), ins);
			if (j.reserved.get() == 0)
				return clean(j);
		}
		
		return null;
	}
	
	private COORDINATE clean(Tile j) {
		
//		Cadaver ca = SETT.THINGS().cadavers.tGet.get(j.coo);
//		if (ca != null)
//			ca.remove();
		
		
		return j.coo;
	}
	
	public void reportSkill(RoomInstance inss, Humanoid h) {
		HunterInstance ins = (HunterInstance) inss;
		ins.dSkill += bonus.get(h.indu());
		ins.iSkill ++;
	}
	
	public boolean work(RoomInstance inss, COORDINATE work, Humanoid h, boolean cadaver) {
		
		HunterInstance ins = getter.get(work);
		if (ins == null)
			return false;
		
		
		if (ins.produce > 1) {
			double mm = 1 + ins.produce/10;
			mm = CLAMP.d(mm, 0, ins.produce);
			ins.produce -= mm;
			int am = ins.industry().outs().get(0).inc(ins, mm/TIME.workHours);
			if (am > 0) {
				DIR dir = storeDir(work);
				SETT.THINGS().resources.createPrecise(work.x()+dir.x(), work.y()+dir.y(), ins.industry().outs().get(0).resource, am);
			}
			
		}
		Tile j = tile.init(work.x(), work.y(), ins);
		j.cadaver.set(ins, cadaver ? 1 : 0);
		
		return true;
		
	}
	
	public void workFinish(COORDINATE work) {
		
		HunterInstance ins = getter.get(work);
		if (ins == null)
			return;
		
		Tile j = tile.init(work.x(), work.y(), ins);
		if (j != null) {
			j.cadaver.set(ins, 0);
			j.reserved.set(ins, 0);
		}
		
		
	}
	
	
	private DIR storeDir(COORDINATE c) {
		for (DIR d : DIR.ORTHO) {
			if (SETT.ROOMS().fData.tile.get(c, d) == constructor.rr)
				return d;
		}
		return DIR.C;
	}

}
