package settlement.room.main;

import static settlement.main.SETT.*;

import java.io.IOException;
import java.util.Arrays;

import game.GameDisposable;
import init.race.EGROUP;
import init.race.RACES;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.main.util.RoomInitData;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayListResize;
import snake2d.util.sets.INDEXED;
import util.data.INT.INTE;
import util.data.INT_O;
import util.data.INT_O.INT_OE;

public final class RoomEmployment extends RoomEmploymentSimple implements INDEXED{

	static ArrayListResize<RoomEmployment> WORK = new ArrayListResize<>(10, 512);
	static {
		new GameDisposable() {
			@Override
			protected void dispose() {
				WORK.clear();
			}
		};
	}
	
	private final int index = WORK.add(this);
	public final Target target = new Target(this);
	public final Priority priority = new Priority(this);
	public final GRoupInt priorities = new GRoupInt(0, 20) {
		
		@Override
		public void set(EGROUP t, int i) {
			int o = get(t);
			super.set(t, i);
			if (o != get(t))
				SETT.ROOMS().employment.prios.updateAll();
		};
	};

	private final GRoupInt current = new GRoupInt(0, Integer.MAX_VALUE);
	
	RoomEmployment(RoomBlueprintIns<?> p, RoomInitData init) {
		super("WORK", p, init);
		
	}
	
	public INT_O<EGROUP> current() {
		return current;
	}
	
	@Override
	void employ(Humanoid h, int delta) {
		super.employ(h, delta);
		current.inc(EGROUP.get(h.indu()), delta);
		ROOMS().employment.changeCurrent(delta, EGROUP.get(h.indu()));
	}
	
	@Override
	void register(RoomEmploymentIns ins, int delta) {
		ROOMS().employment.changeNeeded(blueprint(), -workersNeeded);
		super.register(ins, delta);
		ROOMS().employment.changeNeeded(blueprint(), workersNeeded);
	}
	

	@Override
	void save(FilePutter file) {
		super.save(file);
		current.save(file);
		target.save(file);
		priority.save(file);
		priorities.save(file);
	}

	@Override
	void load(FileGetter file) throws IOException {
		super.load(file);
		current.load(file);
		target.load(file);
		priority.load(file);
		priorities.load(file);
	}

	@Override
	void clear() {
		super.clear();
		current.clear();
		target.clear();
		priority.clear();
		priorities.clear();
		setPrioOnSkill();
	}
	
	public void setPrioOnSkill() {
		for (EGROUP g : EGROUP.ALL()) {
			setPrioOnSkill(g);
		}
	}
	
	public void setPrioOnFullfillment() {
		for (EGROUP g : EGROUP.ALL()) {
			setPrioOnFullfillment(g);
		}
	}
	
	public void setPrioOnSkill(EGROUP g) {
		int p = CLAMP.i((int)Math.ceil(1 + RACES.bonus().priorityCapped(g.r, this)*19), 0, 20);
		priorities.set(g, p);
	}
	
	public void setPrioOnFullfillment(EGROUP g) {
		int p = CLAMP.i((int)Math.ceil(1 + g.r.pref().getWork(this)*19), 0, 20);
		priorities.set(g, p);
	}

	@Override
	public int index() {
		return index;
	}
	
	@Override
	public double efficiency() {
		// TODO Auto-generated method stub
		return super.efficiency();
	}
	
	public static class Priority implements INTE{
		
		final RoomEmployment p;
		private int prio = 10;

		public Priority(RoomEmployment p) {
			this.p = p;
		}
		
		void save(FilePutter file) {
			file.i(prio);
		}

		void load(FileGetter file) throws IOException {
			prio = file.i();
		}

		void clear() {
			prio = 10;
		}
		
		@Override
		public int max() {
			return 20;
		};
		
		@Override
		public int min() {
			return 0;
		}

		@Override
		public int get() {
			return prio;
		}

		@Override
		public void set(int i) {
			i = CLAMP.i(i, min(), max());
			if (prio != i) {
				prio = i;
				SETT.ROOMS().employment.prios.updateAll();
			}
		};
	}
	
	public static class GRoupInt implements INT_OE<EGROUP>{
		
		private int total;
		private final int[] racePrio = new int[EGROUP.ALL().size()];
		private final int min;
		private final int max;
		
		public GRoupInt(int min, int max) {
			this.min = min;
			this.max = max;
		}
		
		void save(FilePutter file) {
			file.is(racePrio);
			file.i(total);
		}

		void load(FileGetter file) throws IOException {
			file.is(racePrio);
			total = file.i();
		}

		void clear() {
			Arrays.fill(racePrio, 0);
			total = 0;
		}
		
		@Override
		public int max(EGROUP t) {
			return max;
		};
		
		@Override
		public int min(EGROUP t) {
			return min;
		}

		@Override
		public int get(EGROUP t) {
			if (t == null)
				return total;
			return racePrio[t.index()];
		}

		@Override
		public void set(EGROUP t, int i) {
			i = CLAMP.i(i, min(t), max(t));
			if (racePrio[t.index()] != i) {
				total -= racePrio[t.index()];
				racePrio[t.index()] = i;
				total += racePrio[t.index()];
			}
		};
		
	}
	
	public static class Target {
		
		private int target;
		private final int[] perGroup = new int[EGROUP.ALL().size()];

		
		public Target(RoomEmployment p) {

		}
		
		public int get() {
			return target;
		}
		
		public int group(EGROUP g) {
			return perGroup[g.index()];
		}
		
		void clear() {
			target = 0;
			for (EGROUP e : EGROUP.ALL()) {
				SETT.ROOMS().employment.changeTarget(-perGroup[e.index()], e);
				perGroup[e.index()] = 0;
			}
			
		}
		
		void add(EGROUP g, int amount) {
			SETT.ROOMS().employment.changeTarget(amount, g);
			target+= amount;
			perGroup[g.index()] += amount;
		}
		

		
		void save(FilePutter file) {
			file.i(target);
			file.is(perGroup);
		}

		void load(FileGetter file) throws IOException {
			target = file.i();
			file.is(perGroup);
		}

	
	}

	
}
