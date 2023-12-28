package world.battle;

import init.config.Config;
import init.sprite.UI.UI;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.sets.ArrayList;
import view.main.VIEW;
import world.battle.spec.WBattleUnit;

class PUnitFactory {
	
	private final ArrayList<UU> all = new ArrayList<UU>(Config.BATTLE.DIVISIONS_PER_ARMY*2);
	private int i;
	
	public PUnitFactory() {
		while(all.hasRoom())
			all.add(new UU());
	}
	
	public WBattleUnit next(SideUnit u) {
		UU res = all.get(i);
		i++;
		res.init(u);
		return res;
	}
	
	void clear() {
		i = 0;
	}
	
	private static class UU extends WBattleUnit {

		private SideUnit u;
		
		UU(){
			
		}
		
		void init (SideUnit u) {
			this.u = u;
			men = u.men;
			icon = u.faction() != null ? u.faction().banner().MEDIUM : UI.icons().m.rebellion;
			name.clear();
			if (u.a() != null) {
				name.add(u.a().name);
			}else if (u.r() != null) {
				name.add(u.r().info.name());
			}
			losses = 0;
			lossesRetreat = 0;
		}
		
		@Override
		public void hover(GUI_BOX box) {
			if (u == null)
				return;
			if (u.a() != null) {
				VIEW.world().UI.armies.hover(box, u.a());
			}else if (u.r() != null)
				VIEW.world().UI.regions.hoverGarrison(u.r(), box);
		}
		
		
	}
	
	
}