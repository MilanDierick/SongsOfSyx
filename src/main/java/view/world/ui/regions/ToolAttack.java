package view.world.ui.regions;

import game.GAME;
import game.faction.FACTIONS;
import init.C;
import init.D;
import init.sprite.SPRITES;
import snake2d.util.datatypes.COORDINATE;
import util.dic.DicArmy;
import view.main.VIEW;
import view.tool.PlacableSimpleTile;
import view.tool.ToolConfig;
import world.World;
import world.entity.WEntity;
import world.entity.army.WArmy;
import world.map.regions.REGIOND;
import world.map.regions.Region;

class ToolAttack extends PlacableSimpleTile {

	private static CharSequence ¤¤noSoldiers = "¤Region has no soldiers to attack with.";
	private static CharSequence ¤¤noRange = "¤Too far away to attack.";
	
	public ToolAttack() {
		super(DicArmy.¤¤Attack, "");
		D.t(this);
	}

	@Override
	public CharSequence isPlacable(int tx, int ty) {
		
		if (COORDINATE.tileDistance(tx,  ty, UIRegion.reg.cx(), UIRegion.reg.cy()) >= Region.attackRange)
			return ¤¤noRange;
		
		if (REGIOND.MILITARY().soldiers.get(UIRegion.reg) == 0) {
			return ¤¤noSoldiers;
		}

		for (WEntity e : World.ENTITIES().fill(tx*C.TILE_SIZE+C.TILE_SIZEH, ty*C.TILE_SIZE+C.TILE_SIZEH)) {
			if (e instanceof WArmy && FACTIONS.rel().enemy(e.faction(), UIRegion.reg.faction())) {
				World.OVERLAY().hover(e);
				if (COORDINATE.tileDistance(e.ctx(),  e.cty(), UIRegion.reg.cx(), UIRegion.reg.cy()) >= Region.attackRange)
					return ¤¤noRange;
				
				
				VIEW.mouse().setReplacement(SPRITES.icons().m.sword);
				return null;
			}
		}
		
		return E;
	}
	
	@Override
	public void place(int tx, int ty) {
		
		for (WEntity e : World.ENTITIES().fill(tx*C.TILE_SIZE+C.TILE_SIZEH, ty*C.TILE_SIZE+C.TILE_SIZEH)) {
			if (e instanceof WArmy && FACTIONS.rel().enemy(e.faction(), UIRegion.reg.faction())) {
				GAME.battle().attack(UIRegion.reg, (WArmy)e);
			}
		}
		
	}
	
	final ToolConfig config = new ToolConfig() {
		
		@Override
		public void deactivateAction() {

		};
		
		@Override
		public void update(boolean UIHovered) {
			if (!VIEW.world().panels.added(VIEW.world().UI.region.detail))
				VIEW.world().tools.place(null, null, false);
		};
		
		@Override
		public boolean back() {
			VIEW.world().panels.remove(VIEW.world().UI.region.detail);
			return true;
		};
	
	};

}
