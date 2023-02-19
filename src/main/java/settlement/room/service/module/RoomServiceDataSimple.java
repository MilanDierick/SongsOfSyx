package settlement.room.service.module;

import java.io.IOException;

import game.time.TIME;
import init.RES;
import init.sound.SoundSettlement.Sound;
import settlement.misc.util.FSERVICE;
import settlement.path.finder.SFinderRoomService;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.util.RoomInitData;
import snake2d.util.file.*;

public abstract class RoomServiceDataSimple {


	private int available = 0; 
	private int total = 0;
	private double load,loadLast;
	private int day;
	
	public final int radius;
	public final CharSequence name;
	public final CharSequence names;
	public final CharSequence verb;
	final RoomBlueprintIns<?> room;
	public final Sound usageSound;
	
	public final SFinderRoomService finder;
	
	public abstract FSERVICE service(int tx, int ty);
	
	public RoomServiceDataSimple(RoomBlueprintIns<?> b, RoomInitData data) {
		
		
		
		Json jd = data.data().json("SERVICE");
		Json json = data.text().json("SERVICE");
		name = json.text("NAME");
		names = json.text("NAMES");
		verb = json.text("VERB");
		usageSound = RES.sound().settlement.action.tryGet(jd);
		this.room = b;
		
		radius = jd.has("RADIUS") ? jd.i("RADIUS", 0, 50000) : 150;
		
		finder = new SFinderRoomService(b.info.name) {
			
			@Override
			public FSERVICE get(int tx, int ty) {
				return service(tx, ty);
			}
		};
		
		day = -1;
	}

	public final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			file.i(available);
			file.i(total);
			file.d(load);
			file.d(loadLast);
			
		}

		@Override
		public void load(FileGetter file) throws IOException {
			available = file.i();
			total = file.i();
			load = file.d();
			loadLast = file.d();
		}

		@Override
		public void clear() {
			available = 0;
			total = 0;
			load = 0;
			loadLast = 0;
		}
	};
	
	public double load() {
		if (total == 0)
			return 1;
		if (day != TIME.days().bitsSinceStart()) {
			loadLast = load;
			load = 0;
			day =  TIME.days().bitsSinceStart();
		}
		return loadLast;
	}
	
	public int available(){
		return available;
	}
	
	public int total() {
		return total;
	}
	
	void increServices(int total, int available) {
		
		if (this.total == 0) {
			load = 1;
			loadLast = 1;
		}else {
			double d = 1.0 - this.available/(double)this.total;
			if (d > load)
				load = d;
			if (d > loadLast)
				loadLast = d;
		}
		
		
		this.available += available;
		this.total += total;
		
	}
	
	public RoomBlueprintIns<?> room(){
		return room;
	}
	
	public abstract double totalMultiplier();
	
	public interface ROOM_SERVICE_HASER {
		RoomServiceDataSimple service();

	}
}
