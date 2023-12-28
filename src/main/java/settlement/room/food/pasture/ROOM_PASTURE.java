package settlement.room.food.pasture;

import java.io.IOException;

import game.boosting.BoostSpec;
import game.time.TIME;
import init.biomes.CLIMATE;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.entity.animal.AnimalSpecies;
import settlement.main.SETT;
import settlement.path.finder.SFinderRoomService;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.Industry.IndustryResource;
import settlement.room.industry.module.Industry.RoomBoost;
import settlement.room.main.*;
import settlement.room.main.BonusExp.RoomExperienceBonus;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.job.ROOM_EMPLOY_AUTO;
import settlement.room.main.job.RoomResStorage;
import settlement.room.main.util.RoomInitData;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.*;
import util.dic.DicMisc;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.info.INFO;
import view.sett.ui.room.UIRoomModule;

public final class ROOM_PASTURE extends RoomBlueprintIns<PastureInstance> implements INDUSTRY_HASER, ROOM_EMPLOY_AUTO{

	public static final String type = "PASTURE";
	final Constructor constructor;
	final Industry productionData;
	final int jobsPerDay = TIME.getWorkPerDay(JobManager.workTime);
	final double capacityPerDay = 1.0/2.0;
	public final AnimalSpecies species;
	final LIST<Industry> indus;
	
	final double ANIMALS_PER_TILE;
	final static double WORKERS_PER_TILE = 1.0/64;

	final RoomResStorage s1 = new RoomResStorage(0b01111111) {
		@Override
		public RESOURCE resource() {
			if (productionData.outs().size() > 0)
				return productionData.outs().get(0).resource;
			return RESOURCES.ALL().get(0);
		}
		
		@Override
		protected boolean is(int tx, int ty) {
			return SETT.ROOMS().fData.tile.is(tx, ty, constructor.s1);
		}
	};
	
	final RoomResStorage s2 = new RoomResStorage(0b01111111) {
		@Override
		public RESOURCE resource() {
			if (productionData.outs().size() > 1)
				return productionData.outs().get(1).resource;
			return RESOURCES.ALL().get(0);
		}
		
		@Override
		protected boolean is(int tx, int ty) {
			return SETT.ROOMS().fData.tile.is(tx, ty, constructor.s2);
		}
	};
	
	final RoomResStorage s3 = new RoomResStorage(0b01111111) {
		@Override
		public RESOURCE resource() {
			if (productionData.outs().size() > 2)
				return productionData.outs().get(2).resource;
			return productionData.outs().get(0).resource;
		}
		
		@Override
		protected boolean is(int tx, int ty) {
			return SETT.ROOMS().fData.tile.is(tx, ty, constructor.s3);
		}
	};
	final RoomResStorage[] st = new RoomResStorage[] {
		s1,s2,s3
	};
	
	public ROOM_PASTURE(RoomInitData data, String key, RoomCategorySub cat, int index) throws IOException {
		super(index, data, key, cat);
		species = SETT.ANIMALS().species.get(data.data());
		
		ANIMALS_PER_TILE = 5.0/(species.mass()+10);
		
		this.constructor = new Constructor(this, data);
		pushBo(data.data(), type, true);
		RoomBoost[] bos = new RoomBoost[] {
			constructor.efficiency,
			new RoomBoost() {
				
				INFO info = new INFO(DicMisc.¤¤Capacity, DicMisc.¤¤Capacity);
				
				@Override
				public INFO info() {
					return info;
				}
				
				@Override
				public double get(RoomInstance r) {
					return constructor.ferarea.get(r)*ROOM_PASTURE.WORKERS_PER_TILE;
				}
			},
			new RoomBoost() {
				
				@Override
				public INFO info() {
					return SETT.ENV().environment.WATER_SWEET;
				}
				
				@Override
				public double get(RoomInstance r) {
					PastureInstance p = (PastureInstance) r;
					return 1 + p.water*0.25;
				}
			},
			new RoomBoost() {
				
				INFO info = new INFO(Gui.¤¤Skill, Gui.¤¤SkillD);
				
				@Override
				public INFO info() {
					return info;
				}
				
				@Override
				public double get(RoomInstance r) {
					PastureInstance p = (PastureInstance) r;
					return p.skill();
				}
			},
			new RoomBoost() {
				
				INFO info = new INFO(Gui.¤¤Animals, Gui.¤¤Animals);
				
				@Override
				public INFO info() {
					return info;
				}
				
				@Override
				public double get(RoomInstance r) {
					PastureInstance p = (PastureInstance) r;
					return (double)(p.animalsCurrent)/p.animalsMax;
				}
			},
			new RoomBoost() {
				
				INFO info = new INFO(Gui.¤¤Tending, Gui.¤¤Tending);
				
				@Override
				public INFO info() {
					return info;
				}
				
				@Override
				public double get(RoomInstance r) {
					PastureInstance p = (PastureInstance) r;
					if (p.animalsCurrent == 0)
						return 1.0;
					
					return 1.0-(double)(p.animalsToDie)/p.animalsCurrent;
				}
			},

		};
		
		productionData = new Industry(this, data.data(), bos, bonus());
		
		if (productionData.outs().size() > 3)
			data.data().error("Can't declare more than 3 output in industry!", "");
		
		//productionData = new Industry(this, null, null, resses, rates, bos, bonus2);
		
		indus = new ArrayList<>(productionData);
		new RoomExperienceBonus(this, data.data(), bonus());
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
		productionData.save(saveFile);
	}
	
	@Override
	protected void loadP(FileGetter saveFile) throws IOException{
		productionData.load(saveFile);
	}
	
	@Override
	protected void clearP() {
		productionData.clear();
	}
	
	@Override
	public boolean degrades() {
		return false;
	}
	
	public static boolean isGate(int tx, int ty) {
		return SETT.ROOMS().map.blueprint.get(tx, ty) instanceof ROOM_PASTURE && SETT.ROOMS().fData.tile.is(tx, ty);
	}
	
	@Override
	public Furnisher constructor() {
		return constructor;
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
	public boolean isAvailable(CLIMATE c) {
		for (BoostSpec s : c.boosters.all()) {
			if (s.boostable == bonus() && s.booster.isMul && s.booster.min() == 0)
				return false;
		}
		return true;
	}

	@Override
	public boolean autoEmploy(Room r) {
		return ((PastureInstance) r).auto;
	}

	@Override
	public void autoEmploy(Room r, boolean b) {
		((PastureInstance) r).auto = b;
	}
	
	@Override
	public double degradeRate() {
		return 0;
	}

	@Override
	public double industryFormatProductionRate(GText text, IndustryResource i, RoomInstance ins) {
		double prod = i.rate;
		
		for (RoomBoost bb : indus.get(0).boosts()) {
			prod *= bb.get(ins);
		}
		text.add('+');
		GFORMAT.f(text, prod);
		return prod;
	}
	
	@Override
	public void industryHoverProductionRate(GBox b, IndustryResource i, RoomInstance ins) {
		Gui.industryHoverProductionRate(b, i, ins);
		
	}
	

	@Override
	public double industryFormatProductionRateEmpl(GText text, IndustryResource i, RoomInstance ins) {
		text.clear();
		double prod = i.rate;
		
		for (RoomBoost bb : indus.get(0).boosts()) {
			prod *= bb.get(ins);
		}
		return prod;
	}
	
}
