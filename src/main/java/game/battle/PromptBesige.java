package game.battle;

import game.GAME;
import game.battle.Resolver.SideResult;
import init.*;
import init.sprite.UI.UI;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.sprite.text.Text;
import util.dic.DicArmy;
import util.dic.DicTime;
import util.gui.misc.*;
import util.gui.panel.GPanelS;
import view.interrupter.Interrupter;
import view.main.VIEW;

final class PromptBesige extends Interrupter implements Prompt{

	private final Conflict conflict;
	private Conflict.Side player;
	private Conflict.Side enemy;
	private final GuiSection s = new GuiSection();
	private double time;
	private final PromptConquer conquer;

	
	PromptBesige(Conflict conflict, PromptUtil util){
		this.conflict = conflict;
		this.conquer = new PromptConquer(util);
		pin();
		persistantSet();
		D.gInit(this);
		s.add(new GStat() {
			
			@Override
			public void update(GText text) {
				text.setFont(UI.FONT().H2);
				text.lablify();
				text.clear().add(DicArmy.¤¤SiegeOf).insert(0, conflict.sideB.garrison().name());
				
			}
		}.r(DIR.C));
		
		s.addRelBody(8, DIR.S, new GStat() {
			
			@Override
			public void update(GText text) {
				DicTime.setAgo(text, time);
			}
		}.hv(D.g("Time", "Time Besieged:")).hoverInfoSet(D.g("TimeD", "The longer you hold the siege, the weaker the garrison will become.")));
		

		GuiSection ss = util.makeBattle(conflict);
		
		s.addRelBody(16, DIR.S, ss);
		
		
		ss = new GuiSection();
	

		ss.add(new GButt.ButtPanel(DicArmy.¤¤Assault){
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
		ss.addRightC(8, new GButt.ButtPanel(D.g("Wait")) {
			
			@Override
			protected void clickA() {
				hide();
			}
		}.hoverInfoSet(D.g("WaitD", "Wait and continue the siege. the enemy will weaken the longer you wait until they surrender.")));
		
		ss.addRightC(8, new GButt.ButtPanel(DicArmy.¤¤Retreat) {
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				
			}
			
			@Override
			protected void clickA() {
				conflict.sideA.get(0).stop();
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

	Prompt activate(double time) {
		this.time = time;
		VIEW.world().activate();
		if (conflict.sideA.isPlayer()) {
			player = conflict.sideA;
			enemy = conflict.sideB;
		}else {
			enemy = conflict.sideA;
			player = conflict.sideB;
		}
		VIEW.world().window.centererTile.set(player.get(0).ctx(), player.get(0).cty());
		VIEW.inters().manager.add(this);
		RES.sound().music.setBattle();
		return this;
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
	
	private void  autoResolve() {
		SideResult a =  Resolver.autoFight(conflict.sideA);
		SideResult b =  Resolver.autoFight(conflict.sideB);
		
		if (conflict.sideA.victory) {
			Resolver.retreat(conflict.sideB, conflict.sideA);
		}else {
			Resolver.retreat(conflict.sideA, conflict.sideB);
		}
		
		if (conflict.sideA.isPlayer()) {
			if (conflict.sideA.victory) {
				GAME.battle().promptt = conquer.open(conflict, conflict.sideB.garrison(), false);
			}else
				GAME.battle().promptt = new PromptResult(conflict, a, b, GAME.battle().ui);
		}
	}
	
}
