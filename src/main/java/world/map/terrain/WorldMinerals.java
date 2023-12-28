package world.map.terrain;

import static world.WORLD.*;

import java.io.IOException;
import java.util.Arrays;

import game.Profiler;
import init.resources.Minable;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.map.MAP_OBJECTE;
import snake2d.util.sets.*;
import util.dic.DicMisc;
import util.gui.misc.GButt;
import view.tool.*;
import world.WORLD.WorldResource;

public class WorldMinerals extends WorldResource implements MAP_OBJECTE<Minable>{

	private final Bitsmap1D ids = new Bitsmap1D(-1, 5, TAREA());
	private final int[] totals = new int[RESOURCES.minables().all().size()+1];
	
	public final PLACABLE placer;
	
	
	
	public WorldMinerals() throws IOException{

		
		
		
		
		placer = new PlacableMulti(DicMisc.¤¤Minerals, "", UI.icons().m.pickaxe.big) {
			final LinkedList<CLICKABLE> butts = new LinkedList<>();
			Minable min = RESOURCES.minables().all().get(0);
			
			{
				for (Minable m : RESOURCES.minables().all()) {
					butts.add(new GButt.ButtPanel(m.resource.icon()) {
						@Override
						protected void clickA() {
							min = m;
						};
						@Override
						protected void renAction() {
							selectedSet(min == m);
						};
					}.hoverTitleSet(m.name));
				}
				
				butts.add(new GButt.ButtPanel(UI.icons().m.cancel) {
					@Override
					protected void clickA() {
						min = null;
					};
					@Override
					protected void renAction() {
						selectedSet(min == null);
					};
				}.hoverTitleSet(DicMisc.¤¤Clear));
			}
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				WorldMinerals.this.set(tx, ty, min);
				
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				return null;
			}
			
			@Override
			public LIST<CLICKABLE> getAdditionalButt() {
				return butts;
			}
		};
		
	}
	
	@Override
	protected void load(FileGetter f) throws IOException {
		ids.load(f);
		Arrays.fill(totals, 0);
		for (COORDINATE c : TBOUNDS()) {
			Minable m = get(c);
			if (m != null) {
				totals[m.index()+1] ++;
				totals[0] ++;
			}
		}
	}

	@Override
	protected void save(FilePutter f){
		ids.save(f);
	}
	
	@Override
	protected void clear() {
		ids.clear();
		Arrays.fill(totals, 0);
	}
	
	@Override
	protected void update(float ds, Profiler prof) {
		
	}
	
	@Override
	public Minable get(int tx, int ty){
		if (!IN_BOUNDS(tx, ty))
			return null;
		return get(tx+ty*TWIDTH());
	}
	
	@Override
	public Minable get(int tile) {
		int i = ids.get(tile)-1;
		if (i >= 0)
			return RESOURCES.minables().getAt(i);
		return null;
	}

	@Override
	public void set(int tile, Minable object) {
		Minable m = get(tile);
		if (m != null) {
			totals[m.index()+1] --;
			totals[0] --;
		}
		
		if (object == null)
			ids.set(tile, 0);
		else {
			ids.set(tile, object.index()+1);
		}
		if (object != null) {
			totals[object.index()+1] ++;
			totals[0] ++;
		}
	}

	@Override
	public void set(int tx, int ty, Minable object) {
		if (IN_BOUNDS(tx, ty))
			set(tx+ty*TWIDTH(), object);
	}
	
	public int total(Minable m) {
		if (m == null)
			return totals[0];
		return totals[m.index+1];
	}
	
}
