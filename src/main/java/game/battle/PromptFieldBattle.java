package game.battle;

import game.GAME;
import game.battle.Resolver.SideResult;
import init.C;
import init.RES;
import init.sprite.UI.UI;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.sprite.text.Text;
import util.dic.DicArmy;
import util.gui.misc.*;
import util.gui.panel.GPanelS;
import view.interrupter.Interrupter;
import view.main.VIEW;
import world.World;

final class PromptFieldBattle extends Interrupter implements Prompt{

	private final Conflict conflict;
	private Conflict.Side player;
	private Conflict.Side enemy;
	private final GuiSection s = new GuiSection();

	
	PromptFieldBattle(Conflict conflict, PromptUtil util){
		this.conflict = conflict;
		pin();
		persistantSet();
		
		s.add(new GStat() {
			
			@Override
			public void update(GText text) {
				text.setFont(UI.FONT().H2);
				text.lablify();
				if (conflict.sideA.garrison() != null) {
					text.clear().add(DicArmy.¤¤BattleOf);
					text.insert(0, conflict.sideA.garrison().name());
				}else if (World.REGIONS().getter.get(conflict.sideA.get(0).ctx(), conflict.sideA.get(0).cty()) != null) {
					text.clear().add(DicArmy.¤¤BattleOf);
					text.insert(0, World.REGIONS().getter.get(conflict.sideA.get(0).ctx(), conflict.sideA.get(0).cty()).name());
				}else {
					text.clear().add(DicArmy.¤¤Battle);
				}
				
			}
		}.r(DIR.C));
		
		GuiSection ss =  util.makeBattle(conflict);
		
		s.addDownC(16, ss);
		
		
		ss = new GuiSection();
	
		ss.add(new GButt.ButtPanel(DicArmy.¤¤Engage) {
			@Override
			protected void clickA() {
				new BattleState(conflict);
				hide();
			}
		}.hoverInfoSet(DicArmy.¤¤engageD));
		ss.addRightC(8, new GButt.ButtPanel(DicArmy.¤¤AutoResolve){
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				Text t = text.text();
				t.add(DicArmy.¤¤autoD);
				t.insert(0, player.victory ? DicArmy.¤¤Victory : (player.losses >= player.men ? DicArmy.¤¤Annihilation : DicArmy.¤¤Defeat));
				t.insert(1, player.losses);
				t.insert(2, enemy.losses);
				text.add(t);
			}
			
			@Override
			protected void clickA() {
				autoResolve();
				hide();
			}
		});
		ss.addRightC(8, new GButt.ButtPanel(DicArmy.¤¤Retreat) {
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				Text t = text.text();
				t.add(DicArmy.¤¤RetreatD);
				t.insert(0, player.retreatLosses);
				text.add(t);
			}
			
			@Override
			protected void clickA() {
				retreat();
				hide();
			}
		});

		s.addDownC(8, ss);
		s.pad(10, 10);

		GPanelS p = new GPanelS();
		p.inner().set(s);
		s.add(p);
		s.moveLastToBack();
		
		s.body().centerIn(C.DIM());
		s.body().moveY2(C.HEIGHT()-100);
		
	}

	void activivate() {
		VIEW.world().activate();
		if (conflict.sideA.isPlayer()) {
			player = conflict.sideA;
			enemy = conflict.sideB;
		}else {
			enemy = conflict.sideA;
			player = conflict.sideB;
		}
		if (player.size() > 0)
			VIEW.world().window.centererTile.set(player.get(0).ctx(), player.get(0).cty());
		else
			VIEW.world().window.centererTile.set(player.garrison().cx(), player.garrison().cy());
		VIEW.inters().manager.add(this);
		RES.sound().music.setBattle();
		
	}

	@Override
	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
		s.hover(mCoo);
		return true;
	}

	@Override
	protected void mouseClick(MButt button) {
		s.click();
		
	}

	@Override
	protected void hoverTimer(GBox text) {
		s.hoverInfoGet(text);
	}

	@Override
	protected boolean render(Renderer r, float ds) {
		
		s.render(r, ds);
		return true;
	}

	@Override
	protected boolean update(float ds) {
		GAME.SPEED.tmpPause();
		return false;
	}




	@Override
	public boolean isActive() {
		return isActivated();
	}

	@Override
	public boolean canSave() {
		return true;
	}
	
	private void retreat() {
		SideResult r = Resolver.autoRetreat(conflict.sideA);
		GAME.battle().promptt = new PromptResult(conflict, r, Resolver.autoRetreatVictor(conflict.sideB), GAME.battle().ui);
		Resolver.retreat(conflict.sideA, conflict.sideB);
	}
	
	private void  autoResolve() {
		SideResult a =  Resolver.autoFight(conflict.sideA);
		SideResult b = Resolver.autoFight(conflict.sideB);
		if (conflict.sideA.victory) {
			Resolver.retreat(conflict.sideB, conflict.sideA);
		}else {
			Resolver.retreat(conflict.sideA, conflict.sideB);
		}
		
		if (conflict.sideA.isPlayer()) {
			GAME.battle().promptt = new PromptResult(conflict, a, b, GAME.battle().ui);
		}
	}
	
}
