package settlement.room.spirit.grave;

import java.io.IOException;
import java.util.Arrays;

import game.GAME;
import game.time.TIME;
import init.D;
import init.race.RACES;
import init.race.Race;
import settlement.entity.humanoid.HCLASS;
import settlement.main.SETT;
import settlement.misc.util.FSERVICE;
import settlement.room.main.*;
import settlement.room.main.util.RoomInitData;
import settlement.stats.STANDING.StandingDef;
import settlement.stats.STATS;
import settlement.thing.ThingsCorpses.Corpse;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import util.data.*;
import util.info.INFO;
import util.race.PERMISSION;

public abstract class GraveData {

	private static final double disturbanceRemove = 1.0/(TIME.secondsPerDay*16);
	private int avai = 0;
	private int tot = 0;
	private int res;
	private double dist;
	private final RoomBlueprintIns<?> p;
	private final GraveDataClass[] classes = new GraveDataClass[HCLASS.ALL.size()];
	private double upD;
	private final Grave grave;
	private final PERMISSION.Permission permission;
	
	private final StandingDef standingDef;
	
	private static CharSequence ¤¤Respect = "¤Respect";
	private static CharSequence ¤¤RespectD = "¤Respect is gained by furnishing your rooms properly";
	private static CharSequence ¤¤Disturbance = "¤Grave Disturbance";
	private static CharSequence ¤¤DisturbanceD = "¤Disturbance happens when a occupied burial service is removed or broken. It will slowly decrease with time as subjects forget. Deactivate the room first and let the remains dissipate before removing them.";
	private static CharSequence ¤¤Burried = "¤Buried";
	private static CharSequence ¤¤BurriedD = "¤The amount of subjects successfully buried recently.";
	private static CharSequence ¤¤Corpses = "¤Failed";
	private static CharSequence ¤¤CorpsesD = "¤The amount of corpses that have failed to be buried recently.";
	private static CharSequence ¤¤Value = "¤Value";
	private static CharSequence ¤¤ValueD = "¤The value is the partition of successfully buried multiplied with respect and degrade.";
	private static CharSequence ¤¤Total = "¤Total Graves";
	private static CharSequence ¤¤TotalD = "¤Total amount of graves";
	private static CharSequence ¤¤Available = "¤Available";
	private static CharSequence ¤¤AvailableD = "¤Available Graves.";
	
	
	static {
		D.ts(GraveData.class);
	}
	
	public final int composeTime;
	
	GraveData(RoomBlueprintIns<GraveInstance> p, RoomInitData init, int composeTime){
		this.p = p;
		standingDef = new StandingDef(init.data());
		grave = new Grave(p, this);
		for (HCLASS c : HCLASS.ALL)
			classes[c.index()] = new GraveDataClass(c);
		permission = new PERMISSION.Permission(p.info);
		this.composeTime = composeTime;
	}
	
	public PERMISSION permission() {
		return permission;
	}
	

	protected void save(FilePutter file){
		file.i(avai);
		file.i(tot);
		file.i(res);
		file.d(dist);
		file.d(upD);
		for (GraveDataClass c : classes)
			c.saver.save(file);
		permission.save(file);
	}
	

	protected void load(FileGetter file) throws IOException{
		avai = file.i();
		tot = file.i();
		res = file.i();
		dist = file.d();
		upD = file.d();
		for (GraveDataClass c : classes)
			c.saver.load(file);
		permission.load(file);
		int res = 0;
		for (int i = 0; i < p.instancesSize(); i++) {
			int r = (int) (respect((GraveInstance) p.getInstance(i))*((GraveInstance) p.getInstance(i)).total());
			res += r;
			
		}
		this.res = res;
		
	}
	

	protected void clear() {
		avai = 0;
		tot = 0;
		res = 0;
		dist = 0;
		upD = 0;
		for (GraveDataClass c : classes)
			c.saver.clear();
	}
	
	public FSERVICE burrialService(int tx, int ty) {
		if (grave.get(tx, ty) != null)
			return grave.service;
		return null;
	}
	
	Grave grave(int tx, int ty) {
		return grave.get(tx, ty);
	}
	
	public GRAVE_JOB work(RoomInstance ins) {
		
		GRAVE_JOB j = (GRAVE_JOB) ((GraveInstance) ins).jobs.getReservableJob(null);
		if (j == null) {
			((GraveInstance) ins).reportWorkSuccess(false);
			return null;
		}else {
			((GraveInstance) ins).reportWorkSuccess(true);
			return j;
		}
	}
	
	public GRAVE_JOB work(int tx, int ty) {
		return grave.job(tx, ty);
	}
	
	void activate(GraveInstance i, int a, int tot) {
		this.avai += a;
		this.tot += tot;
		int res = (int)(respect(i)*tot);
		this.res += res;
	}
	
	void deactivate(GraveInstance i, int a, int tot) {
		this.avai -= a;
		this.tot -= tot;
		int res = (int)(respect(i)*tot);
		this.res -= res;
	}
	
	void deactivate(GraveInstance i) {
		for (COORDINATE c : i.body()) {
			if (i.is(c)) {
				Grave g = grave.get(c.x(), c.y());
				if (g != null) {
					g.deactivate();
				}
			}
		}
	}
	
	void dispose(GraveInstance i, int a, int tot) {
		dist += tot-a;
		for (COORDINATE c : i.body()) {
			if (i.is(c)) {
				Grave g = grave.get(c.x(), c.y());
				if (g != null) {
					g.dispose();
				}
			}
		}
	}
	
	public GraveDataClass get(HCLASS c) {
		return classes[c.index()];
	}
	
	protected abstract double respect(GraveInstance grave);

	public final INT_O<Room> available = new INT_O<Room>() {
		
		private final INFO info = new INFO(¤¤Available, ¤¤AvailableD);
		
		@Override
		public int min(Room r) {
			return 0;
		}
		
		@Override
		public int max(Room r) {
			if (r == null)
				return tot;
			return ((GraveInstance)r).total();
		}
		
		@Override
		public int get(Room r) {
			if (r == null)
				return avai;
			return ((GraveInstance)r).available();
		}
		
		@Override
		public INFO info() {
			return info;
		};
	};
	
	public final INT_O<Room> total = new INT_O<Room>() {
		
		private final INFO info = new INFO(¤¤Total, ¤¤TotalD);
		
		@Override
		public int min(Room r) {
			return 0;
		}
		
		@Override
		public int max(Room r) {
			if (r == null)
				return tot;
			return ((GraveInstance)r).total();
		}
		
		@Override
		public int get(Room r) {
			if (r == null)
				return tot;
			return ((GraveInstance)r).total();
		}
		
		@Override
		public INFO info() {
			return info;
		};
	};

	public final DOUBLE_O<Room> respect = new DOUBLE_O<Room>() {
		
		private final INFO info = new INFO(¤¤Respect, ¤¤RespectD);
		
		@Override
		public double getD(Room r) {
			if (r == null)
				return (double)res/tot;
			return respect((GraveInstance) r);
		}
		
		@Override
		public INFO info() {
			return info;
		};
		
	};
	
	public DOUBLE disturbance = new DOUBLE() {
		
		private final INFO info = new INFO(¤¤Disturbance, ¤¤DisturbanceD);
		
		@Override
		public double getD() {
			double p = STATS.POP().POP.data(null).get(null);
			if (p == 0)
				return dist > 0 ? 1 : 0;
			return CLAMP.d(100.0*dist/p, 0, 1);
		}
		
		@Override
		public INFO info() {
			return info;
		};
	};
	
	public int requestAccessCorpse(Corpse corpse) {
		if (avai == 0)
			return -1;
		
		int i = RND.rInt(p.instancesSize());
		
		for (int k = 0; k < p.instancesSize(); k++) {
			
			int ii = (i+k)%p.instancesSize();
			GraveInstance ins = (GraveInstance) p.getInstance(ii);
			if (ins.active() && ins.available() > 0) {
				for (COORDINATE c : ins.body()) {
					if (ins.is(c)) {
						Grave g = grave.get(c.x(), c.y());
						if (g != null && g.isUsable()) {
							g.setCorpse(corpse);
							return c.x() + c.y()*SETT.TWIDTH;
						}
						
					}
				}
				GAME.Notify("nono");
				
			}
		}
		GAME.Notify("nonono");
		return -1;
		
	}
	
	public boolean hasAccessCorpse(int tile, Corpse corpse) {
		
		if (tile < 0)
			return false;
		
		int tx = tile % SETT.TWIDTH;
		int ty = tile / SETT.TWIDTH;
		Grave g = grave.get(tx, ty);
		return g != null && g.corpse() == corpse;
		
	}
	

	
	void update(float ds) {
		{
			double d = dist*disturbanceRemove;
			if (d < disturbanceRemove)
				d = disturbanceRemove;
			dist -= d*ds;
			if (dist < 0)
				dist = 0;
		}
		upD += ds;
		if (upD > TIME.secondsPerDay) {
			upD -= TIME.secondsPerDay;
			for (HCLASS c : HCLASS.ALL) {
				classes[c.index()].update();
			}
		}
	}
	
	public StandingDef standingDef() {
		return standingDef;
	}
	
	public GraveInfo info(Room r, int i) {
		GraveInfo info = GraveInfo.get((GraveInstance)r, i);
		if (info.hasBody())
			return info;
		return null;
	}
	
	
	public interface GRAVE_DATA_HOLDER {
		
		public GraveData graveData();
		
	}
	
	public RoomBlueprintIns<?> blueprint(){
		return p;
	}
	
	public final class GraveDataClass {
		private final double[] burr = new double[RACES.all().size()];
		private final double[] fails = new double[RACES.all().size()];
		private final HCLASS cl;
		
		GraveDataClass(HCLASS cl) {
			this.cl = cl;
		}
		
		final SAVABLE saver = new SAVABLE() {
			
			@Override
			public void save(FilePutter file) {
				file.ds(burr);
				file.ds(fails);
			}
			
			@Override
			public void load(FileGetter file) throws IOException {
				file.ds(burr);
				file.ds(fails);
			}
			
			@Override
			public void clear() {
				Arrays.fill(burr, 0);
				Arrays.fill(fails, 0);
			}
		};
		
		public DOUBLE_O<Race> burried = new DOUBLE_O<Race>() {

			private final INFO info = new INFO(¤¤Burried, ¤¤BurriedD);
			
			@Override
			public double getD(Race t) {
				if (t == null) {
					double m = 0;
					for (Race r : RACES.all()) {
						m += burr[r.index];
					}
					if (m == 0)
						return 0;
					return m;
				}
				return burr[t.index];
			}
			
			@Override
			public INFO info() {
				return info;
			};
			
		};
		
		public DOUBLE_O<Race> failed = new DOUBLE_O<Race>() {
			
			private final INFO info = new INFO(¤¤Corpses, ¤¤CorpsesD);

			@Override
			public double getD(Race t) {
				if (t == null) {
					double m = 0;
					for (Race r : RACES.all()) {
						m += fails[r.index];
					}
					if (m == 0)
						return 0;
					return m;
				}
				return fails[t.index];
			}
			
			@Override
			public INFO info() {
				return info;
			};
			
		};
		
		public DOUBLE_O<Race> value = new DOUBLE_O<Race>() {
			
			private final INFO info = new INFO(¤¤Value, ¤¤ValueD);

			@Override
			public double getD(Race t) {
				if (t == null) {
					double m = 0;
					for (Race r : RACES.all()) {
						m += burr[r.index]*STATS.POP().POP.data(cl).get(r);
					}
					if (m == 0)
						return 0;
					return m / STATS.POP().POP.data(cl).get(null);
				}
				
				double tot = burr[t.index] + fails[t.index];
				double res = 1;
				if (tot != 0)
					res = 1.0 - fails[t.index]/tot;
				res *= 1.0 - 0.5*blueprint().degradeAverage();
				res *= 0.5 + 0.5*respect.getD(null);
				
				
				return res;
			}
			
			@Override
			public INFO info() {
				return info;
			};
			
		};
		
		public void fail(Corpse c, int delta) {
			fails[c.indu().race().index] += delta;
		}
		
		void burry(Corpse c) {
			burr[c.indu().race().index] ++;
		}
		
		void update() {
			for (Race r : RACES.all()) {
				fails[r.index] -= Math.max(fails[r.index]*0.05, 0.05);
				if (fails[r.index] < 0)
					fails[r.index] = 0;
				
				burr[r.index] -= Math.max(burr[r.index]*0.05, 0.05);
				if (burr[r.index] < 0)
					burr[r.index] = 0;
				
			}
		}
		
	}
	
}
