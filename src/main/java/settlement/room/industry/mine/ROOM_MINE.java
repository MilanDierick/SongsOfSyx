package settlement.room.industry.mine;

import java.io.IOException;

import game.boosting.Boostable;
import init.D;
import init.resources.*;
import settlement.path.finder.SFinderRoomService;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.Industry.RoomBoost;
import settlement.room.main.*;
import settlement.room.main.BonusExp.RoomExperienceBonus;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.util.RoomInitData;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.gui.GuiSection;
import snake2d.util.sets.*;
import snake2d.util.sprite.text.Str;
import util.data.GETTER;
import util.gui.misc.*;
import util.info.GFORMAT;
import util.info.INFO;
import view.sett.ui.room.UIRoomModule;
import world.regions.Region;

public final class ROOM_MINE extends RoomBlueprintIns<MineInstance> implements INDUSTRY_HASER{

	public final static String type = "MINE";
	int rawNeeded = 2;
	final Job job;

	
	final Industry productionData;
	final Constructor constructor;
	public final Minable minable;
	
	private final RoomBoost employed;
	final LIST<Industry> indus;
	
	public ROOM_MINE(RoomInitData init, String key, int index, RoomCategorySub cat) throws IOException {
		super(index, init, key, cat);
		
		minable = RESOURCES.minables().get(init.data());
		constructor = new Constructor(init, this);
		Boostable skill = pushBo(init.data(), type, true);
		{D.t(this);}
		
		CharSequence out = D.g("OutputD", "When building the mine, the density of the {0} determines the output. The highest density is applied when you have but 1 worker. This value will move down to the average density the more people you employ in the mine. Increasing the amount of workers will always result in more produce, the efficiency/worker will go down however.");
		
		employed = new RoomBoost() {
			
			private final INFO info = new INFO(
					D.g("Output"),
					new Str(out).insert(0, minable.name)
					);
			
			@Override
			public INFO info() {
				return info;
			}
			
			@Override
			public double get(RoomInstance r) {
				MineInstance m = (MineInstance) r;
				
				double h = m.outputMax;
				double a = constructor.deposits.get(r);
				double d = h-a;
				
				double e = r.employees().hardTarget();
				double em = r.employees().max();
				if (e == 0)
					return h;
				return (h + h - (e/em)*d)/2;
				
			}
		};
		
		productionData = new Industry(this, 
				null, 
				null, 
				new RESOURCE[] {minable.resource}, 
				new double[] {init.data().d("YEILD_WORKER_DAILY", 0, 1000)}, 
				new RoomBoost[] {constructor.efficiency, employed}, 
				skill) {
			@Override
			public double getRegionBonus(Region reg) {
				return reg.info.minableBonus(minable);
			}
		};
		
		job = new Job(this);
		indus = new ArrayList<>(productionData);
		
		new RoomExperienceBonus(this, init.data(), skill);
	}
	
	@Override
	public Furnisher constructor() {
		return constructor;
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
	public boolean makesDudesDirty() {
		return true;
	}
	
	@Override
	public void appendView(LISTE<UIRoomModule> mm) {
		mm.add(constructor.deposits.applier(this));
		mm.add(constructor.efficiency.applier(this));
		mm.add(new UIRoomModule() {
			

			
			@Override
			public void appendPanel(GuiSection section, GETTER<RoomInstance> get, int x1, int y1) {
				section.addRelBody(4, DIR.S, new GStat() {
					@Override
					public void update(GText text) {
						GFORMAT.perc(text, employed.get((RoomInstance) get.get()));
					}
				}.hh(employed.info()));
			}
			
			
		});
		mm.add(new UIRoomModule() {
			@Override
			public void hover(GBox box, Room i, int rx, int ry) {
				box.NL();
				box.add(box.text().add(((MineInstance)(i)).workage));
			}
		});
	}
	
	@Override
	public LIST<Industry> industries() {
		return indus;
	}


}
