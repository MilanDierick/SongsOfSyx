package view.world.ui.army;

import init.C;
import init.sprite.SPRITES;
import snake2d.SPRITE_RENDERER;
import util.dic.DicArmy;
import util.dic.DicMisc;
import view.main.VIEW;
import view.subview.GameWindow;
import view.tool.PlacableSimpleTile;
import view.tool.ToolConfig;
import world.World;
import world.army.WARMYD;
import world.entity.WEntity;
import world.entity.WPathing;
import world.entity.army.WArmy;
import world.entity.army.WArmyState;
import world.map.regions.Region;

class ToolMove extends PlacableSimpleTile {

	
	public ToolMove() {
		super(DicArmy.¤¤Move, "");
		// TODO Auto-generated constructor stub
	}

	@Override
	public CharSequence isPlacable(int tx, int ty) {
		
		if (WARMYD.men(null).get(UIArmy.army) == 0) {
			return DicArmy.¤¤MoveCant;
		}
		
		if (WPathing.regPath(World.REGIONS().getter.get(UIArmy.army.ctx(), UIArmy.army.cty()), World.REGIONS().getter.get(tx, ty), UIArmy.army.cost(), Integer.MAX_VALUE) == null)
			return DicMisc.¤¤Unreachable;
		
		for (WEntity e : World.ENTITIES().fill(tx*C.TILE_SIZE+C.TILE_SIZEH, ty*C.TILE_SIZE+C.TILE_SIZEH)) {
			if (e instanceof WArmy && e != UIArmy.army) {
				World.OVERLAY().hover(e);
				VIEW.mouse().setReplacement(SPRITES.icons().m.crossair);
				VIEW.hoverBox().text(DicArmy.¤¤Intercept);
				return null;
			}
		}
		
		Region reg = World.REGIONS().getter.get(tx, ty);
		if (reg != null && Math.abs(reg.cx()-tx) <= 1 && Math.abs(reg.cy()-ty) <= 1) {
			if (WArmyState.canBesiege(UIArmy.army, reg)) {
				World.OVERLAY().hoverRegion(reg);
				VIEW.mouse().setReplacement(SPRITES.icons().m.sword);
				VIEW.hoverBox().text(DicArmy.¤¤Besiege);
			}
			
		};
		
		VIEW.mouse().setReplacement(SPRITES.icons().m.crossair);
		VIEW.hoverBox().text(DicArmy.¤¤Move);
		
		return null;
	}


	@Override
	public void renderOverlay(int x, int y, SPRITE_RENDERER r, float ds, GameWindow window) {
		
		World.OVERLAY().moveArmy(UIArmy.army);
		
		
		super.renderOverlay(x, y, r, ds, window);
	}
	
	@Override
	public void place(int tx, int ty) {
		
		for (WEntity e : World.ENTITIES().fill(tx*C.TILE_SIZE+C.TILE_SIZEH, ty*C.TILE_SIZE+C.TILE_SIZEH)) {
			if (e instanceof WArmy && e != UIArmy.army) {
				UIArmy.army.intercept((WArmy) e);
				return;
			}
		}
		
		Region reg = World.REGIONS().getter.get(tx, ty);
		if (reg != null && Math.abs(reg.cx()-tx) <= 1 && Math.abs(reg.cy()-ty) <= 1) {
			if (WArmyState.canBesiege(UIArmy.army, reg)) {
				UIArmy.army.besiege(reg);
				return;
			}
			
		};
		
		UIArmy.army.setDestination(tx, ty);
		
	}
	
	final ToolConfig config = new ToolConfig() {
		
		@Override
		public void deactivateAction() {

		};
		
		@Override
		public void update(boolean UIHovered) {
			if (!VIEW.world().panels.added(VIEW.world().UI.armies.army))
				VIEW.world().tools.place(null, null, false);
		};
		
		@Override
		public boolean back() {
//			VIEW.world().panels.remove(VIEW.world().UI.armies.army);
			return true;
		};
		
		
		
	
	};

}
