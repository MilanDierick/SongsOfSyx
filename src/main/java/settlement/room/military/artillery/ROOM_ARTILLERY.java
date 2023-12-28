package settlement.room.military.artillery;

import java.io.IOException;

import init.D;
import init.race.POP_CL;
import init.race.RACES;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.main.SETT;
import settlement.path.finder.SFinderFindable;
import settlement.room.main.*;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.util.RoomInit;
import settlement.room.main.util.RoomInitData;
import settlement.thing.projectiles.Projectile;
import settlement.thing.projectiles.Projectile.ProjectileImp;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.ArrayListResize;
import snake2d.util.sets.LISTE;
import util.gui.misc.GBox;
import view.sett.ui.room.UIRoomModule;
import view.tool.PlacableFixed;

public final class ROOM_ARTILLERY extends RoomBlueprintIns<ArtilleryInstance> {

	public static final String type = "ARTILLERY";
	final Constructor constructor;
	
	public final RESOURCE PROJECTILE;
	
	private volatile boolean threadLock;
	private final ArrayListResize<ArtilleryInstance> threadSafe = new ArrayListResize<>(256, ROOMS.ROOM_MAX);
	public final Projectile projectile;
	
	final Service service = new Service(this);
	private double ref = 0;
	
	private static CharSequence ¤¤control = "¤Control artillery piece in the battle view.";
	
	public final int services = 6;
	
	static {
		D.ts(ROOM_ARTILLERY.class);
	}
	
	public PlacableFixed eplacer;
	
	public ROOM_ARTILLERY(int ti, RoomInitData data, String key, RoomCategorySub cat) throws IOException {
		super(ti, data, key, cat);
		
		constructor = new Constructor(data, this) {

			@Override
			public Room create(TmpArea area, RoomInit init) {
				return new ArtilleryInstance(ROOM_ARTILLERY.this, area, init);
			}
			
		};
		pushBo(data.data(), type, true); 
		PROJECTILE = RESOURCES.map().getByKey("PROJECTILE_RESOURCE", data.data());
		projectile = new ProjectileImp(data.data());
		eplacer = new Placer(this, data.m);
	}

	@Override
	protected void saveP(FilePutter f) {
		
	}

	@Override
	protected void loadP(FileGetter f) throws IOException {
		
	}

	@Override
	protected void clearP() {
		
	}
		

	@Override
	protected void update(float ds) {
		lock();
		threadSafe.clearSoft();
		for (int k = 0; k < instancesSize(); k++)
			threadSafe.add(getInstance(k));
		threadLock = false;
		ref = bonus().get(RACES.clP(null, null))/bonus().max(POP_CL.class);
	}

	@Override
	public SFinderFindable service(int tx, int ty) {
		ArtilleryInstance ins = get(tx, ty);
		if (ins != null)
			return SETT.PATH().finders.manning(ins.army());
		return null;
	}
	
	@Override
	public Furnisher constructor() {
		return constructor;
	}

	@Override
	public void appendView(LISTE<UIRoomModule> mm) {
		mm.add(new UIRoomModule() {
			@Override
			public void hover(GBox box, Room i, int rx, int ry) {
				ArtilleryInstance ins = (ArtilleryInstance) i;
				Hoverer.hover(box, ins);
				if (ins.army() == SETT.ARMIES().player()) {
					box.NL();
					box.text(¤¤control);
				}
				
			}
		});
	}
	
	public double ref() {
		return ref;
	}

	private synchronized void lock() {
		while(threadLock)
			;
		threadLock = true;
	}
	
	public void threadInstances(LISTE<ArtilleryInstance> res){
		lock();
		res.add(this.threadSafe);
		threadLock = false;
	}
	

}
