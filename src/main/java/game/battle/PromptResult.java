package game.battle;

import game.GAME;
import game.battle.Resolver.Result;
import game.battle.Resolver.SideResult;
import game.faction.FACTIONS;
import init.C;
import init.D;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.entity.humanoid.HTYPE;
import settlement.main.SETT;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import util.data.DOUBLE_O;
import util.dic.DicArmy;
import util.gui.misc.*;
import util.gui.panel.GPanelL;
import view.interrupter.Interrupter;
import view.main.VIEW;
import world.World;
import world.army.WARMYD;
import world.army.WARMYD.WArmySupply;
import world.entity.army.WArmy;
import world.entity.caravan.Shipment;

final class PromptResult extends Interrupter implements Prompt{

	private final GuiSection s = new GuiSection();

	private static CharSequence ¤¤victoryD = "¤The gods have smiled upon your name. Victory is ours and our foe has been beaten.";
	private static CharSequence ¤¤DefeatD = "¤A dark day in the annals. The enemy has snatched victory through deceit and low cunning. We must call in new armies to deal with this pathetic beggar.";
	private static CharSequence ¤¤RetreatD = "¤Our army has retreated to fight another day.";
	private static CharSequence ¤¤RetreatDefeat = "¤Our army attempted to retreat, but was destroyed in the process.";
	
	private final SideResult result;
	private final SideResult enemy;
	private final Conflict conflict;
	
	private int[] enslave = new int[RACES.all().size()];
	
	private final DOUBLE_O<RESOURCE> rgetter = new DOUBLE_O<RESOURCE>() {

		@Override
		public double getD(RESOURCE t) {
			if (result.res == Result.VICTORY)
				return enemy.lost[t.index()] + result.lost[t.index()];
			else
				return -result.lost[t.index()];
		}
		
	};
	
	static {
		D.ts(PromptResult.class);
	}
	
	PromptResult(Conflict conflict, SideResult result, SideResult enemy, PromptUtil util){
		
		this.result = result;
		this.enemy = enemy;
		
		CharSequence title = null;
		CharSequence body = null;
		this.conflict = conflict;
		
		if (result.res == Result.VICTORY) {
			title = DicArmy.¤¤Victory;
			body = ¤¤victoryD;
		}else if (result.res == Result.DEFEAT) {
			title = DicArmy.¤¤Defeat;
			body = ¤¤DefeatD;
			
		}else {
			title = DicArmy.¤¤Retreat;
			int am = 0;
			for (int i : result.deaths)
				am+= i;
			body = am >= conflict.sideA.men ? ¤¤RetreatDefeat : ¤¤RetreatD;
		}
		
		GText t = new GText(UI.FONT().M, body);
		t.setMaxWidth(500);
		t.adjustWidth();
		RENDEROBJ r = t.r(DIR.C);
		s.add(r);
		
		s.addDownC(16, util.result(conflict, result, enemy));
		
		s.addDownC(16,  util.spoils(rgetter));
		
		if (result.res == Result.VICTORY) {
			
			DOUBLE_O<Race> getter = new DOUBLE_O<Race>() {

				@Override
				public double getD(Race t) {
					return enemy.captured[t.index];
				}
				
			};
			
			s.addDownC(16, util.slaves(enslave, getter));
		}
		
		GPanelL panel = new GPanelL();
		
		s.pad(8);
		
		panel.getInnerArea().set(s);
		panel.setTitle(title);
		
		GuiSection butts = new GuiSection();
		butts.add(new GButt.Panel(SPRITES.icons().m.ok) {
			
			@Override
			protected void clickA() {
				accept();
			}
			
		});
		
		panel.centreNavButts(butts);
		
		s.add(butts);
		
		s.add(panel);
		s.moveLastToBack();
		
		s.body().centerIn(C.DIM());
		s.body().moveX2(C.DIM().x2()-50);
		
		pin();
		persistantSet();
		
		VIEW.inters().manager.add(this);
		VIEW.world().uiManager.clear();
		VIEW.world().panels.clear();
		
		
		
	}
	


	void accept() {
		if (result.res == Result.VICTORY) {
			for (Race r : RACES.all()) {
				if (enslave[r.index] > 0) {
					SETT.ENTRY().add(r,  HTYPE.PRISONER, enslave[r.index]);
				}
			}
			Resolver.retreat(conflict.sideB, conflict.sideA);
		}else {
			Resolver.retreat(conflict.sideA, conflict.sideB);
		}
		
		int [] gained = new int[RESOURCES.ALL().size()];
		
		for (RESOURCE res : RESOURCES.ALL())
			gained[res.index()] = CLAMP.i((int)rgetter.getD(res), 0, Integer.MAX_VALUE);
		
		double[] needs = new double[WARMYD.supplies().all.size()];
		
		for (WArmy a : conflict.sideA) {
			for (WArmySupply s : WARMYD.supplies().all) {
				needs[s.index()] += s.needed(a);
			}
		}
		
		for (WArmy a : conflict.sideA) {
			for (WArmySupply s : WARMYD.supplies().all) {
				double n = s.needed(a);
				if (n == 0)
					continue;
				int am = (int) Math.ceil((n*rgetter.getD(s.res)/needs[s.index()]));
				am = CLAMP.i(am, 0, gained[s.res.index()]);
				am = CLAMP.i(am, 0, (int)n);
				s.current().inc(a, am);
				gained[s.res.index()] -= am;
			}
		}
		
		Shipment s = null;
		
		for (RESOURCE res : RESOURCES.ALL()) {
			if (gained[res.index()] > 0) {
				if (s == null) {
					int tx,ty;
					if (conflict.sideA.garrison() != null) {
						tx = conflict.sideA.garrison().cx();
						ty = conflict.sideA.garrison().cy();
					}else {
						tx = conflict.sideA.get(0).ctx();
						ty = conflict.sideA.get(0).cty();
					}
					s = World.ENTITIES().caravans.createSpoils(tx, ty, FACTIONS.player().capitolRegion());
				}
				if (s != null) {
					s.load(res, gained[res.index()]);
				}
				
			}
		}
		
		hide();
	}
	
	@Override
	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
		s.hover(mCoo);
		return true;
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
		
		s.render(r, ds);
		return true;
	}

	@Override
	protected boolean update(float ds) {
		GAME.SPEED.tmpPause();
		return false;
	}
	
	@Override
	protected void deactivateAction() {
		
	}

	@Override
	public boolean isActive() {
		return isActivated();
	}

	@Override
	public boolean canSave() {
		return false;
	}

	
}
