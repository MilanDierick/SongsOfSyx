package settlement.path;

import static settlement.main.SETT.*;

import java.io.IOException;

import init.sprite.SPRITES;
import settlement.main.*;
import settlement.main.SETT.SettResource;
import settlement.misc.util.DoubleGetter;
import settlement.misc.util.SettTileIsser;
import settlement.path.components.*;
import settlement.path.finder.FinderThread;
import settlement.path.finder.SFINDERS;
import settlement.room.main.throne.THRONE;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.TOGGLEBLE;
import util.rendering.ShadowBatch;
import view.sett.IDebugPanelSett;

public final class PATHING extends SettResource {

	public final CostMethods coster = new CostMethods();
	public final SFINDERS finders = new SFINDERS();
	
	private TOGGLEBLE performanceTest = new TOGGLEBLE.Imp();

	public final SettEntryPoints entryPoints = new SettEntryPoints();
	public final PlayerHuristics huristics = new PlayerHuristics();

	
	public final SCOMPONENTS comps = new SCOMPONENTS();
	public final AvailabilityMap availability = new AvailabilityMap(comps);
	public final FinderThread thread = new FinderThread(comps);
	public PATHING() {
		
		new ON_TOP_RENDERABLE() {
			{
				IDebugPanelSett.add("availability", new TOGGLEBLE.Imp() {
					@Override
					public void set(boolean bool) {
						if (bool)
							add();
						else
							remove();
						super.set(bool);
					}
				});
			}
			
			@Override
			public void render(Renderer r, ShadowBatch shadowBatch, RenderData data) {
				
				RenderData.RenderIterator i = data.onScreenTiles();
				COLOR.WHITE65.bind();
				while(i.has()) {
					if (cost.get(i.tile()) < 0)
						COLOR.RED100.bind();
					else if (cost.get(i.tile()) == 1)
						COLOR.BLUE100.bind();
					else
						COLOR.YELLOW100.bind();
					SPRITES.cons().BIG.dashed.render(r, 0x0F, i.x(), i.y());
					i.next();
				}
				COLOR.unbind();
				
			}
		};
		

		
		IDebugPanelSett.add("2100 paths/s", performanceTest);
	}
	
	public SFINDERS finders() {
		return finders;
	}
	
	@Override
	protected void save(FilePutter saveFile) {
		thread.stop();
		huristics.saver.save(saveFile);
		thread.start();
	}

	@Override
	protected void load(FileGetter saveFile) throws IOException {
		thread.stop();
		huristics.saver.load(saveFile);
	}
	
	@Override
	protected void clearBeforeGeneration(CapitolArea area) {
		thread.stop();
		huristics.saver.clear();
		comps.clear();
	}
	
//	private Coo start = new Coo();
	
	public boolean willUpdateTile(int tx, int ty) {
		return comps.zero.updating().is(tx, ty);
	}
	
	public boolean willUpdate() {
		return comps.zero.uping();
	}
	
	
	@Override
	protected void update(float ds) {
		thread.setStop();
		huristics.update(ds);
		finders.update(ds);
		thread.stop();
		comps.update();
		thread.start();
	}
	
	@Override
	protected void generate(CapitolArea area) {
		
	}
	
	@Override
	protected void init(boolean loaded) {
		thread.stop();
		availability.init();
		comps.init();
		thread.start();
	}
	

	/**
	 * Answers if a specific tile is solid from the players viewpoint
	 */
	public final SettTileIsser solidity = new SettTileIsser() {
		
		@Override
		public boolean is(int tile) {
			return availability.get(tile).player < 0; 
		}
	};
	
	/**
	 * Answers if a specific tile is reachable from the super components
	 */
	public final SettTileIsser reachability = new SettTileIsser() {
		
		@Override
		public boolean is(int tx, int ty) {
			SComponent c = comps.superComp.get(tx, ty);
			if (c != null && c.is(THRONE.coo()))
				return true;;
			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				c = comps.superComp.get(tx, ty, DIR.ORTHO.get(i));
				if (c != null && c.is(THRONE.coo())) {
					return true;
				}
			}
			return false;
		};
		
		@Override
		public boolean is(int tile) {
			throw new RuntimeException();
		}
	};
	
	
	/**
	 * Answers if a specific tile is connected to the super components
	 */
	public final SettTileIsser connectivity = new SettTileIsser() {
		
		@Override
		public boolean is(int tx, int ty) {
			SComponent c = comps.superComp.get(tx, ty);
			if (c != null && c.is(THRONE.coo()))
				return true;
			return false;
		}
		
		@Override
		public boolean is(int tile) {
			return is(tile%TWIDTH, tile/TWIDTH);
		}
	};
	
	
	public AVAILABILITY getAvailability(int x, int y) {
		if (!IN_BOUNDS(x, y))
			return AVAILABILITY.SOLID;
		return availability.get(x, y);
	}
	

	
	/**
	 * The player cost of the current tile
	 */
	public final DoubleGetter cost = new DoubleGetter(TWIDTH, THEIGHT) {
		
		@Override
		public double get(int tile) {
			return availability.get(tile).player; 
		}
	};
	
	public boolean isInTheNeighbourhood(int tx, int ty, int dx, int dy) {
		SComponent c = comps.levels.get(0).get(tx, ty);
		if (c == null)
			return false;
		SComponent d = comps.levels.get(0).get(dx, dy);
		if (d == null)
			return false;
		if (c == d)
			return true;
		SComponentEdge e = c.edgefirst();
		while (e != null) {
			if (e.to() == d)
				return true;
			e = e.next();
		}
		return false;
	}



	

	
}
