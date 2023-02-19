package settlement.stats;

import init.paths.PATH;
import init.paths.PATHS;
import settlement.entity.humanoid.Humanoid;
import settlement.stats.StatsBoosts.StatBooster;
import snake2d.util.file.Json;
import snake2d.util.file.SAVABLE;
import snake2d.util.sets.ArrayListResize;
import util.data.DataOL;

final class Init {

	public final PATH pd = PATHS.INIT().getFolder("race");
	public final PATH pt = PATHS.TEXT().getFolder("race");

	public final ArrayListResize<SAVABLE> savables = new ArrayListResize<SAVABLE>(512, 512*8);
	public final ArrayListResize<Pushable> pushable = new ArrayListResize<Pushable>(256, 512*2);
	public final ArrayListResize<Updatable> updatable = new ArrayListResize<>(256, 256);
	public final ArrayListResize<Initable> initable = new ArrayListResize<>(256, 256);
	public final ArrayListResize<Disposable> disposable = new ArrayListResize<>(256, 256);
	public final ArrayListResize<StatCollection> holders = new ArrayListResize<>(256, 256);
	public final ArrayListResize<Updatable2> upers = new ArrayListResize<>(256, 256);
	public final ArrayListResize<Addable> addable = new ArrayListResize<>(256, 256);
	
	public final ArrayListResize<STAT> stats = new ArrayListResize<STAT>(256, 256);
	
	public final ArrayListResize<StatBooster> boosts = new ArrayListResize<StatBooster>(256, 4*512);
	public final ArrayListResize<DataStat> datas = new ArrayListResize<DataStat>(256, 512);
	
	public final DataOL<Induvidual> count = new DataOL<Induvidual>() {
		@Override
		protected long[] data(Induvidual t) {
			return t.data;
		}
	
	};
	
	private final Json jjData = new Json(pd.get("_STATS"));
	private final Json jjText = new Json(pt.get("_STATS"));

	
	public Json jData = null;
	public Json jText = null;
	
	public void init(String key) {
		jData = jjData.json(key);
		jText = jjText.json(key);
	}
	
	interface Addable {
		void addH(Induvidual i);
		void removeH(Induvidual i);
	}
	
	interface Pushable {
		void pushday();
	}
	
	interface Updatable extends Initable {
		void update16(Humanoid h, int updateR, boolean day, int updateI);
		
		@Override
		default void init(Induvidual h) {
			// TODO Auto-generated method stub
			
		}
	}
	
	interface Initable {
		void init(Induvidual h);
	}
	
	interface Disposable {
		void dispose(Humanoid h);
	}
	
	interface Updatable2 {
		void update(double ds);
	}
	
}
