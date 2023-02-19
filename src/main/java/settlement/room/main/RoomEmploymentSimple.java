package settlement.room.main;

import java.io.IOException;

import game.GameDisposable;
import init.RES;
import init.sound.SoundSettlement.Sound;
import settlement.entity.humanoid.Humanoid;
import settlement.room.main.util.RoomInitData;
import settlement.stats.STATS;
import snake2d.LOG;
import snake2d.util.file.*;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayListResize;

public class RoomEmploymentSimple {

	private final RoomBlueprintIns<?> p;
	protected int workersNeeded = 0;
	private int employed = 0;
	public final CharSequence title;
	public final CharSequence verb;
	private final double hourStart;
	private final Sound sound;
	private final boolean shift;
	public final double accidentsPerYear;
	public final double defaultFullfillment;
	public final double educationFactor;
	public final double indoctorFactor;
	public final double healthFactor;
	private double fill = 1.0;
	private final int eindex;
	public final int largeWorkforce;
	private int efficiency = 0;
	
	static ArrayListResize<RoomEmploymentSimple> WORK_ALL = new ArrayListResize<>(10, 512);
	static {
		new GameDisposable() {
			@Override
			protected void dispose() {
				WORK_ALL.clear();
			}
		};
	}
	
	RoomEmploymentSimple(String key, RoomBlueprintIns<?> p, RoomInitData init) {
		this.p = p;
		Json data = init.data().json(key);
		Json text = init.text().json(key);
		this.title = text.text("TITLE");
		this.verb = text.text("VERB");
		this.hourStart = data.d("SHIFT_OFFSET", 0, 0.99);
		shift = data.has("NIGHT_SHIFT") && data.bool("NIGHT_SHIFT");
		this.sound = RES.sound().settlement.action.tryGet(data);
		if (data.has("FULLFILLMENT"))
			defaultFullfillment = data.d("FULLFILLMENT", 0, 1.0);
		else
			defaultFullfillment = 0.5;
		if (data.has("EDUCATION_FACTOR"))
			educationFactor = data.d("EDUCATION_FACTOR", 0, 10000.0);
		else
			educationFactor = 0;
		indoctorFactor = data.dTry("INDOCTRINATION_FACTOR", 0, 10000, 0);
		accidentsPerYear = data.dTry("ACCIDENTS_PER_YEAR", 0, 10000, 0);
		
		healthFactor = data.dTry("HEALTH_FACTOR", 0, 1000, 1);
		largeWorkforce = data.i("LARGE_WORKFORCE", 10, 10000, 1000);
		eindex =  WORK_ALL.add(this);
	}
	
	public int eindex() {
		return eindex;
	}
	
	public int employed() {
		return employed;
	}
	
	public final int neededWorkers() {
		return workersNeeded;
	}
	
	public Sound sound() {
		return sound;
	}
	
	void register(RoomEmploymentIns ins, int delta) {
		workersNeeded += delta*ins.hardTarget();
		efficiency += delta*ins.efficiency()*100;
		if (delta > 0 && workersNeeded < 0)
			throw new RuntimeException(blueprint().info.name + " " + workersNeeded + " ");
	}
	
	void employ(Humanoid h, int delta) {
		employed += delta;
	}
	
	public double getFill() {
		return fill;
	}
	
	public double efficiency() {
		if (p.instancesSize() == 0)
			return 1;
		return efficiency/(100.0*p.instancesSize());
	}
	
	public RoomBlueprintIns<?> blueprint(){
		return p;
	}

	void save(FilePutter file) {

		file.i(workersNeeded);
		file.i(employed);
		file.i(efficiency);
		file.d(fill);
	}

	void load(FileGetter file) throws IOException {
		workersNeeded = file.i();
		employed = file.i();
		efficiency = file.i();
		fill = file.d();
		fill = 1;
	}

	void clear() {
		workersNeeded = 0;
		employed = 0;
		fill = 1.0;
		efficiency = 0;
	}
	
	public double getShiftStart() {
		return hourStart;
	}
	
	public boolean worksNights() {
		return shift;
	}
	
	public static class EmployerSimple {
		
		private final RoomEmploymentSimple si;
		
		public EmployerSimple(RoomEmploymentSimple si) {
			this.si = si;
		}
		
		public boolean employ(Humanoid h) {
			RoomInstance ins = STATS.WORK().EMPLOYED.get(h);
			if (ins != null && ins.blueprintI() == si.blueprint()) {
				if (ins.blueprintI() == si.blueprint()) {
					if (ins.employees().isOverstaffed()) {
						STATS.WORK().EMPLOYED.set(h, null);
						ins = null;
					}else
						return true;
				}else {
					STATS.WORK().EMPLOYED.set(h, null);
				}
			}
			
			if (si.neededWorkers() > si.employed()) {
				int i = RND.rInt(si.blueprint().instancesSize());
				for (int k = 0; k < si.blueprint().instancesSize(); k++) {
					RoomInstance in = si.blueprint().getInstance((i+k)%si.blueprint().instancesSize());
					if (in.active() && in.employees().employed() < in.employees().target()) {
						STATS.WORK().EMPLOYED.set(h, in);
						return true;
					}
				}
				LOG.err("no!" + " " + si.neededWorkers() + " " + si.employed() + " " + si.blueprint().instancesSize());
				for (int k = 0; k < si.blueprint().instancesSize(); k++) {
					RoomInstance in = si.blueprint().getInstance((i+k)%si.blueprint().instancesSize());
					LOG.err(k + " " + in.active() + " " + in.employees().employed() + " " + in.employees().target() + " " + in.employees().hardTarget() + " " + si.getFill() + " " + i);
				}
				
			}
			return false;
			
		}
		
		public int employable() {
			return si.neededWorkers()-si.employed();
		}
		
	}
	
}
