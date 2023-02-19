package world.map.buildings.camp;

import init.sprite.SPRITES;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.data.INT.INTE;
import util.gui.misc.GButt;
import util.gui.slider.GAllocator;
import view.tool.PLACABLE;
import view.tool.PlacableSimpleTile;
import view.world.IDebugPanelWorld;
import world.World;

final class Placer extends PlacableSimpleTile{

	private WCampType type;
	private final ArrayList<CLICKABLE> ss;
	private final INTE inte;
	public Placer(LIST<WCampType> types) {
		super("camps place");
		type = types.get(0);
		
		GuiSection s = new GuiSection();
		for (WCampType t : types) {
			s.addRightC(0, new GButt.ButtPanel(SPRITES.icons().m.cancel) {
				
				@Override
				protected void clickA() {
					type = t;
				}
				
				@Override
				protected void renAction() {
					label =  t.race.appearance().icon;
					selectedSet(type == t);
				}
				
			}.hoverInfoSet(t.race.info.names));
		}
		
		inte = new INTE() {
			
			int i = 0;
			
			@Override
			public int min() {
				return 0;
			}
			
			@Override
			public int max() {
				return 8;
			}
			
			@Override
			public int get() {
				return i;
			}
			
			@Override
			public void set(int t) {
				i = t;
			}
		};
		
		s.addRelBody(2, DIR.S, new GAllocator(COLOR.RED100, inte, 6, 16));
		
		ss = new ArrayList<CLICKABLE>(s);
		
		IDebugPanelWorld.add(this);
		
	}

	@Override
	public CharSequence isPlacable(int tx, int ty) {
		return null;
	}

	@Override
	public void place(int tx, int ty) {
		World.BUILDINGS().camp.create(tx, ty, type, inte.getD());
		
	}
	
	@Override
	public LIST<CLICKABLE> getAdditionalButt() {
		return ss;
	}
	
	@Override
	public PLACABLE getUndo() {
		return World.BUILDINGS().nothing.placer;
	}

}
