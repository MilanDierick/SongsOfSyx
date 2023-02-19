package settlement.room.main;

import static settlement.main.SETT.*;

import java.io.IOException;
import java.util.Arrays;

import init.biomes.CLIMATE;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.util.RoomInitData;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.map.MAP_OBJECT;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayListResize;
import snake2d.util.sets.LIST;

public abstract class RoomBlueprintIns<T extends RoomInstance> extends RoomBlueprintImp{

	private final ArrayListResize<T> all = new ArrayListResize<T>(200, ROOMS.ROOM_MAX);
	private int totalArea = 0;
	int averageDegrade = 0;
	private final RoomEmploymentSimple employment;
	int roomNameI = 1;
	private long[] stats = new long[16];
	private static long statL = 1000;
	

	protected RoomBlueprintIns(int typeIndex, RoomInitData data, String key, RoomCategorySub cat, ACTION wiki) {
		super(data, typeIndex, key, cat, wiki);
		if (data.data().has("WORK"))
			employment = new RoomEmployment(this, data);
		else if (data.data().has("EMPLOYMENT")){
			employment = new RoomEmploymentSimple("EMPLOYMENT", this, data);
		}else {
			employment = null;
		}
	}
	
	protected RoomBlueprintIns(int typeIndex, RoomInitData data, String key, RoomCategorySub cat) {
		super(data, typeIndex, key, cat);
		if (data.data().has("WORK"))
			employment = new RoomEmployment(this, data);
		else if (data.data().has("EMPLOYMENT")){
			employment = new RoomEmploymentSimple("EMPLOYMENT", this, data);
		}else {
			employment = null;
		}
	}

	@SuppressWarnings("unchecked")
	protected void removeInstance(RoomInstance rem) {
		totalArea -= rem.area();
		if (degrades())
			averageDegrade -= (int)Math.ceil((100*rem.getDegrade()));
		all.remove((T)rem);
		if (rem.stats != null)
			for (int i = 0; i < rem.stats.length; i++) {
				this.stats[i] -= (long)(rem.stats[i]*statL);
			}
	}
	
	@SuppressWarnings("unchecked")
	protected void addInstance(RoomInstance t) {
		totalArea += t.area();
		if (degrades())
			averageDegrade += (int)Math.ceil((100*t.getDegrade()));
		all.add((T) t);
		if (t.stats != null)
			for (int i = 0; i < t.stats.length; i++) {
				this.stats[i] += (long)(t.stats[i]*statL);
			}
	}

	@Override
	protected final void save(FilePutter saveFile) {
		if (employment != null)
			employment.save(saveFile);
		saveFile.object(all);
		saveFile.i(roomNameI);
		saveFile.i(totalArea);
		saveFile.i(averageDegrade);
		saveFile.ls(stats);
		saveP(saveFile);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected final void load(FileGetter saveFile) throws IOException{
		all.clear();
		if (employment != null)
			employment.load(saveFile);
		all.add((ArrayListResize<T>)saveFile.object());	
		roomNameI = saveFile.i();
		totalArea = saveFile.i();
		averageDegrade = saveFile.i();
		saveFile.ls(stats);
		loadP(saveFile);
		
	}
	
	@Override
	protected void clear() {
		if (employment != null)
			employment.clear();
		roomNameI = 1;
		totalArea = 0;
		averageDegrade = 0;
		Arrays.fill(stats, 0);
		all.clear();
		clearP();
		return;
		
	}
	
	protected abstract void saveP(FilePutter f);
	
	protected abstract void loadP(FileGetter f) throws IOException;
	
	protected abstract void clearP();
	
	@SuppressWarnings("unchecked")
	@Override
	public final T get(int tx, int ty) {
		Room r = ROOMS().map.get(tx, ty);
		if (r != null && r.blueprint() == this)
			return (T) ROOMS().map.get(tx, ty);
		return null;
	}
	
	public final MAP_OBJECT<T> getter = new MAP_OBJECT<T>() {

		@SuppressWarnings("unchecked")
		@Override
		public T get(int tile) {
			Room r = ROOMS().map.get(tile);
			if (r != null && r.blueprint() == RoomBlueprintIns.this)
				return (T) r;
			return null;
		}

		@Override
		public T get(int tx, int ty) {
			if (IN_BOUNDS(tx, ty))
				return get(tx+ty*TWIDTH);
			return null;
		}
	
	
	};
	
	public final T getInstance(int nr) {
		return all.get(nr);
	}
	
	public final int instancesSize() {
		return all.size();
	}
	
	public final LIST<T> all(){
		return all;
	}
	
	public final RoomEmployment employmentExtra() {
		if (employment instanceof RoomEmployment)
			return (RoomEmployment) employment;
		return null;
	}
	
	@Override
	public final RoomEmploymentSimple employment() {
		return employment;
	}
	
	public boolean degrades() {
		return true;
	}
	
	public final int totalArea() {
		return totalArea;
	}
	
	public double degradeAverage() {
		if (instancesSize() == 0)
			return 0;
		return averageDegrade/(100.0*instancesSize());
	}
	
	

	
	@Override
	public boolean isAvailable(CLIMATE c) {
		return true;
	}
	
	public double getStat(int statIndex) {
		if (instancesSize() == 0) {
			this.stats[statIndex] = 0;
			return 0;
		}
		return this.stats[statIndex]/((double)statL*instancesSize());
	}
	
}
