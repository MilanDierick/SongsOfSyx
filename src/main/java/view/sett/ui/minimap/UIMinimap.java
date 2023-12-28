package view.sett.ui.minimap;

import java.io.IOException;

import init.C;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.*;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.gui.misc.GBox;
import view.interrupter.InterManager;
import view.interrupter.Interrupter;
import view.keyboard.KEYS;
import view.subview.GameWindow;
import view.ui.top.UIPanelTop;

public class UIMinimap extends Interrupter implements SAVABLE{

	final UIMinimapMapPanel map;
	
	final UIMiniResources resources;
	final UIMiniHotSpots hs;
	final UIMiniRaces species;

	final ViewMiniMap minimap;
	final UIMinimapButtons buttons;
	
	private final Expansion[] all;
	private boolean[] visable;
	private int[] widths = new int[4];
	
	public UIMinimap(UIPanelTop top, InterManager i, int y1, boolean hotspots, boolean resources, boolean nobility, boolean races, boolean env, GameWindow w) {
		desturberSet().persistantSet().pin();
		
		minimap = new ViewMiniMap(this, i, w);
		
		
		
		
		

		map = new UIMinimapMapPanel(w);
		if (top == null) {
			map.body().moveY1(0);
		}else {
			map.body().moveY1(UIPanelTop.HEIGHT);
		}
		map.body().moveX2(C.WIDTH());
		map.body().moveY1(y1);
		
		{
			int y = map.body().y2() + UIMinimapButtons.height;
			this.resources = new UIMiniResources(1,y);
			this.hs = new UIMiniHotSpots(2, y, w);
			this.species = new UIMiniRaces(3, y);
		}
		
		
		buttons = new UIMinimapButtons(0, map.body().width(), this, w, resources, hotspots, nobility, races, env);
		buttons.body().moveX2(C.WIDTH());
		buttons.body().moveY1(map.body().y2());
		
		
		
		all = new Expansion[] {
			this.species,
			this.resources,
			this.hs,
		};
		visable = new boolean[] {
			false,
			false,
			false,
			false,
		};
		this.hs.visableSet(hotspots);
		this.resources.visableSet(resources);
		this.species.visableSet(races);
		
		update(0);
		show(i);
	}

	
	public void open() {
		minimap.showMin();
	}

	public void clearOverlay() {
		buttons.clearOverlay();
	}
	
	@Override
	protected void hoverTimer(GBox text) {
		for (Expansion e : all) {
			e.hoverInfoGet(text);
		}
		buttons.hoverInfoGet(text);
	}

	@Override
	protected boolean render(Renderer r, float ds) {

		map.render(r, ds);
		int w = 0;
		for (Expansion e : all) {
			if (e.visableIs()) {
				w+= e.body().width();
				e.render(r, ds);
			}
			
		}
		buttons.render(r, ds);
	
		if (w > 0)
			manager().viewPort().incrW(-w);
		
		return true;
	}




	@Override
	protected void mouseClick(MButt button) {
		hs.click();
		if (button == MButt.LEFT) {
			map.click();
			buttons.click();
			for (Expansion e : all) {
				e.click();
			}
		}
		
	}
	
	@Override
	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {

		
		if (map.hover(mCoo) || buttons.hover(mCoo))
			return true;
		boolean h = false;
		for (Expansion e : all) {
			
			if (e.hover(mCoo))
				h = true;
		}
		
		return h;
	}

	@Override
	protected boolean update(float ds) {	
		boolean changed = false;
		int i = 0;
		for (RENDEROBJ r : all) {
			if (r.visableIs() != visable[i]) {
				changed = true;
				visable[i] = r.visableIs();
			}
			if (widths[i] != r.body().width()) {
				widths[i] = r.body().width();
				changed = true;
			}
			i++;
		}
		
		if (changed) {
			int x2 = C.WIDTH();
			for (i = all.length-1; i >= 0; i--) {
				if (all[i].visableIs()) {
					all[i].body().moveX2(x2);
					x2 = all[i].body().x1();
				}
			}
		}
		
		if (KEYS.MAIN().MINIMAP.consumeClick()) {
			minimap.show();
		}
		return true;
	}	

	@Override
	public void save(FilePutter file) {
		int i = 0;
		for (RENDEROBJ r : all) {
			i |= r.visableIs() ? 1 :0;
			i = i<<1;
		}
		file.i(i);
		hs.save(file);
		resources.save(file);
	}


	@Override
	public void load(FileGetter file) throws IOException {
		int i = file.i();
		int k = all.length;
		for (RENDEROBJ r : all) {
			r.visableSet(((i>>k)&1) == 1);
			k--;
		}
		hs.load(file);
		resources.load(file);
	}


	@Override
	public void clear() {
		for (int i = 0; i < visable.length; i++)
			visable[i] = true;
		hs.clear();
	}

	static abstract class Expansion extends GuiSection implements SAVABLE{

		
		Expansion(int index){
			
		}
		
	}

}
