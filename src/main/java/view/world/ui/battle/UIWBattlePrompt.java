package view.world.ui.battle;

import game.GAME;
import init.C;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GuiSection;
import snake2d.util.misc.ACTION;
import util.gui.misc.GBox;
import util.gui.panel.GPanel;
import view.interrupter.Interrupter;
import view.main.VIEW;
import world.battle.spec.*;
import world.battle.spec.WBattleResult.RTYPE;

public class UIWBattlePrompt {
	
	private final Inter inter = new Inter();
	
	private final UIBattle fieldBattle = new UIBattle(inter, this);
	private final UISiege sei = new UISiege(inter, this);
	
	public UIWBattlePrompt(){
		
	}
	
	public boolean isBusty() {
		return inter.isActivated();
	}
	
	public void prompt(WBattleSpec battle) {
		if (inter.isActivated())
			return;
		inter.set(fieldBattle.init(battle), true, battle.player.coo.x(), battle.player.coo.y());
	}
	
	public void prompt(WBattleSiege siege) {
		if (inter.isActivated())
			return;
		inter.set(sei.init(siege), true, siege.besiged.cx(), siege.besiged.cy());
		
	}
	
	public void result(WBattleSiege.Result siege, boolean surrender) {
		inter.set(new UIVictorySiege(inter, siege), false, siege.besiged.cx(), siege.besiged.cy());
	}
	
	public void result(WBattleResult battle, boolean retreat) {
		if (battle.result == RTYPE.VICTORY)
			if (retreat)
				inter.set(new UIVictoryRetreat(inter, battle), false,  battle.player.coo.x(), battle.player.coo.y());
			else
				inter.set(new UIVictory(inter, battle), false,  battle.player.coo.x(), battle.player.coo.y());
		else
			inter.set(new UIDefeat(inter, battle, battle.result == RTYPE.RETREAT), false,  battle.player.coo.x(), battle.player.coo.y());
	}

	
	
	
	private static class Inter extends Interrupter implements ACTION{
		
		private GuiSection s;
		private boolean canSave;
		private final GPanel panel = new GPanel();
		
		Inter(){
			pin();
			persistantSet();
			panel.setBig();
		}
		
		@Override
		protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
			s.hover(mCoo);
			panel.hover(mCoo);
			return true;
		}
		
		void set(GuiSection s, boolean canSave, int cx, int cy) {
			if (isActivated())
				throw new RuntimeException();
			
			VIEW.world().uiManager.clear();
			VIEW.world().panels.clear();
			this.s = s;
			this.canSave = canSave;
			panel.inner().set(s);
			panel.inner().centerIn(C.DIM());
			panel.inner().moveY2(C.HEIGHT()-100);
			
			s.body().centerIn(panel.inner());
			VIEW.world().activate();
			VIEW.world().window.setZoomout(0);
			VIEW.world().window.centererTile.set(cx, cy+200/C.TILE_SIZE);
			show(VIEW.inters().manager);
		}

		@Override
		protected void mouseClick(MButt button) {
			if (button == MButt.LEFT)
				s.click();
			
		}

		@Override
		protected void hoverTimer(GBox text) {
			s.hoverInfoGet(text);
		}

		@Override
		protected boolean render(Renderer r, float ds) {
			panel.render(r, ds);
			s.render(r, ds);
			return true;
		}

		@Override
		protected boolean update(float ds) {
			GAME.SPEED.tmpPause();
			return false;
		}


		@Override
		public boolean canSave() {
			return canSave;
		}

		@Override
		public void exe() {
			hide();
		}

		
	}
}
