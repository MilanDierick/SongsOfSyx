package settlement.room.food.pasture;

import java.io.IOException;

import game.time.TIME;
import init.D;
import init.biomes.CLIMATE;
import init.biomes.CLIMATES;
import init.boostable.BOOSTABLE;
import init.boostable.BOOSTABLES;
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
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.job.ROOM_EMPLOY_AUTO;
import settlement.room.main.job.RoomResStorage;
import settlement.room.main.util.RoomInitData;
import snake2d.util.MATH;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.rnd.RND;
import snake2d.util.sets.*;
import snake2d.util.sprite.text.Str;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import view.main.MessageText;
import view.main.VIEW;
import view.sett.IDebugPanelSett;
import view.sett.ui.room.UIRoomModule;

public final class ROOM_PASTURE extends RoomBlueprintIns<PastureInstance> implements INDUSTRY_HASER, ROOM_EMPLOY_AUTO{

	public static final String type = "PASTURE";
	final Constructor constructor;
	final Industry productionData;
	final int jobsPerDay = TIME.getWorkPerDay(JobManager.workTime);
	final double capacityPerDay = 1.0/2.0;
	public final AnimalSpecies species;
	final BOOSTABLE bonus2;
	final LIST<Industry> indus;
	
	final double ANIMALS_PER_TILE;
	final static double WORKERS_PER_TILE = 1.0/64;
	
	private final Event eventer = new Event();	
	private static CharSequence ¤¤mTitle = "¤Livestock Dying";
	private static CharSequence ¤¤mBody = "¤Terrible news! our {0} have been afflicted by a disease. As a countermeasure, our herders have culled the sick animals, {1}% of them.";

	static {
		D.ts(ROOM_PASTURE.class);
	}

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
		bonus2 = BOOSTABLES.ROOMS().pushRoom(this, data.data(), type);
		
		RoomBoost[] bos = new RoomBoost[] {

		};
		
		productionData = new Industry(this, data.data(), bos, bonus2);
		
		if (productionData.outs().size() > 3)
			data.data().error("Can't declare more than 3 output in industry!", "");
		
		//productionData = new Industry(this, null, null, resses, rates, bos, bonus2);
		
		indus = new ArrayList<>(productionData);
	}
	
	@Override
	protected void update(float ds) {
		eventer.update(ds);
	}
	
	@Override
	public SFinderRoomService service(int tx, int ty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void saveP(FilePutter saveFile){
		productionData.save(saveFile);
		saveFile.d(eventer.time);
	}
	
	@Override
	protected void loadP(FileGetter saveFile) throws IOException{
		productionData.load(saveFile);
		eventer.time = saveFile.d();
	}
	
	@Override
	protected void clearP() {
		productionData.clear();
		eventer.time = 0;
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
		return CLIMATES.BONUS().mul(bonus2, c)*(1+CLIMATES.BONUS().add(bonus2, c)) > 0;
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
		return Gui.industryFormatProductionRate(text, i, ins);
	}
	
	@Override
	public void industryHoverProductionRate(GBox b, IndustryResource i, RoomInstance ins) {
		Gui.industryHoverProductionRate(b, i, ins);
		
	}
	
	private final class Event {
		
		private double time;
		
		Event(){
			IDebugPanelSett.add("Event: " + key, new ACTION() {
				
				@Override
				public void exe() {
					event();
				}
			});
		}
		
		void update(double ds) {
			
			if (VIEW.b().isActive())
				return;
			
			time -= ds;
			if (time > 0)
				return;
			
			if (employment().employed() > 25) {
				event();
			}
			
			time += (1 + RND.rFloat())*SETT.ROOMS().PASTURES.size()*4*TIME.years().bitSeconds();
		}
		
		private void event() {
			
			if (instancesSize() == 0)
				return;
			
			double death = 0.2 + MATH.pow15.pow(RND.rFloat())*0.8;
			int tot = 0;
			
			for (int i = 0; i < instancesSize(); i++) {
				PastureInstance ins = getInstance(i);
				int d = (int) Math.ceil(Math.ceil(ins.animalsCurrent*death)); 
				tot += ins.kill(d);
			}
			
			if (tot > 0) {
				new MessageText(¤¤mTitle).paragraph(Str.TMP.clear().add(¤¤mBody).insert(0, species.names).insert(1, (int)(100*(death)))).send();
				 
			}
		}
		
	}

}
