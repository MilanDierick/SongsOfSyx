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
import world.WORLD;
import world.army.AD;
import world.entity.WEntity;
import world.entity.army.WArmy;
import world.entity.army.WArmyState;
import world.regions.Region;

class ToolMove extends PlacableSimpleTile {

	private boolean can = false;
	private Region bReg = null;
	private WArmy incept = null;
	
	public ToolMove() {
		super(DicArmy.¤¤Move, "");
		// TODO Auto-generated constructor stub
	}

	@Override
	public CharSequence isPlacable(int tx, int ty) {
		
		can = false;
		bReg = null;
		incept = null;
		
		if (AD.men(null).get(Army.army) == 0) {
			return DicArmy.¤¤MoveCant;
		}
		
		if (Army.army.ctx() == tx && Army.army.cty() == ty)
			return E;
		
		
		if (!WORLD.FOW().is(tx, ty)) {
			for (WEntity e : WORLD.ENTITIES().fill(tx*C.TILE_SIZE+C.TILE_SIZEH, ty*C.TILE_SIZE+C.TILE_SIZEH)) {
				if (e instanceof WArmy && e != Army.army) {
					incept = (WArmy) e;
					if (WORLD.PATH().path(Army.army.ctx(), Army.army.cty(), tx, ty, Army.army.path().treaty()) == null) {
						return DicMisc.¤¤Unreachable;
					}
					can = true;
					WORLD.OVERLAY().hoverEntity(e);
					VIEW.mouse().setReplacement(SPRITES.icons().m.crossair);
					VIEW.hoverBox().text(DicArmy.¤¤Intercept);
					return null;
				}
			}
		}
		
		
		Region reg = WORLD.REGIONS().map.centre.get(tx, ty);
		if (reg != null && WArmyState.canBesiege(Army.army, reg)) {
			WORLD.OVERLAY().hoverBox(reg);
			VIEW.mouse().setReplacement(SPRITES.icons().m.sword);
			if (Army.army.besigeTile(reg) != null) {
				VIEW.hoverBox().text(DicArmy.¤¤Besiege);
				bReg = reg;
				can = true;
				return null;
			}else {
				return DicMisc.¤¤Unreachable;
			}
		};
		
		if (WORLD.PATH().path(Army.army.ctx(), Army.army.cty(), tx, ty, Army.army.path().treaty()) == null) {
			return DicMisc.¤¤Unreachable;
		}
		
		can = true;
		VIEW.mouse().setReplacement(SPRITES.icons().m.crossair);
		VIEW.hoverBox().text(DicArmy.¤¤Move);
		
		return null;
	}


	@Override
	public void renderOverlay(GameWindow window) {
		
		WORLD.OVERLAY().hoverArmy(Army.army);

	}
	
	@Override
	public void renderPlaceHolder(SPRITE_RENDERER r, int tx, int ty, int cx, int cy, boolean isPlacable) {
		
		if (bReg != null || incept != null)
			return;
		
		super.renderPlaceHolder(r, tx, ty, cx, cy, isPlacable);
	}
	
	@Override
	public void place(int tx, int ty) {
		
		if (!can)
			return;
		
		if (incept != null) {
			Army.army.intercept(incept);
			return;
		}else if (bReg != null) {
			Army.army.besiege(bReg);
		}else {
			Army.army.setDestination(tx, ty);
		}
		
		
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
			VIEW.world().panels.remove(VIEW.world().UI.armies.army);
			return true;
		};
		
		
		
	
	};

}
