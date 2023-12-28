package view.world.ui.battle;

import init.D;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.misc.ACTION;
import snake2d.util.sprite.text.Text;
import util.data.DOUBLE_O;
import util.data.GETTER.GETTER_IMP;
import util.dic.DicArmy;
import util.gui.misc.*;
import util.info.GFORMAT;
import world.battle.spec.*;

class UISiege {

	private static CharSequence ¤¤Lift = "¤Lift";
	private static CharSequence ¤¤LiftD = "¤Lift Siege";
	private static CharSequence ¤¤Wait = "Wait";
	private static CharSequence ¤¤WaitD = "Continue siege and wait. The fortifications of the settlement will decrease the longer you wait.";
	
	static {
		D.ts(UISiege.class);
	}
	
	private final GETTER_IMP<WBattleSide> player = new GETTER_IMP<>();
	private final GETTER_IMP<WBattleSide> enemy = new GETTER_IMP<>();
	private boolean hovRetreat = false;
	
	private WBattleSiege spec;
	
	private final DOUBLE_O<WBattleUnit> pLosses = new DOUBLE_O<WBattleUnit>() {

		@Override
		public double getD(WBattleUnit t) {
			if (hovRetreat)
				return 0;
			return t.losses;
		}
		
	};
	
	private final DOUBLE_O<WBattleUnit> eLosses = new DOUBLE_O<WBattleUnit>() {

		@Override
		public double getD(WBattleUnit t) {
			if (hovRetreat)
				return 0;
			return t.losses;
		}
		
	};


	private final GuiSection s = new GuiSection() {
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			super.render(r, ds);
			hovRetreat = false;
			
		};
	};

	
	UISiege(ACTION close, UIWBattlePrompt ui){
		
		s.add(new GStat() {
			
			@Override
			public void update(GText text) {
				text.setFont(UI.FONT().H2);
				text.lablify();
				text.clear().add(DicArmy.¤¤SiegeOf).insert(0, spec.besiged.info.name());
				
			}
		}.r(DIR.C));
		
		s.addRelBody(4, DIR.S, Util.balance(player, enemy));
		
		s.add(new GStat() {
			
			@Override
			public void update(GText text) {
				
				text.add('x').s();
				GFORMAT.f1(text, spec.fortifications);
			}
			
		}.hh(UI.icons().m.fortification).hoverTitleSet(DicArmy.¤¤Fort).hoverInfoSet(DicArmy.¤¤FortD), s.body().cX(), s.body().y2()+8);
		
		
		s.addRelBody(16, DIR.S, Util.confict(player, enemy, pLosses, eLosses));
		
		
		
		

		
		
		GuiSection ss = new GuiSection();
	
		GButt.ButtPanel bb;
		
		bb = new GButt.ButtPanel(DicArmy.¤¤Assault){
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				Text t = text.text();
				t.add(DicArmy.¤¤autoD);
				t.insert(0, spec.victory ? DicArmy.¤¤Victory : (player.get().losses >= player.get().men ? DicArmy.¤¤Annihilation : DicArmy.¤¤Defeat));
				t.insert(1, player.get().losses);
				t.insert(2, enemy.get().losses);
				text.add(t);
			}
			
			@Override
			protected void clickA() {
				close.exe();
				spec.auto();
			}
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
					boolean isHovered) {
				super.render(r, ds, isActive, isSelected, isHovered);
				if (!spec.victory) {
					OPACITY.O25.bind();
					COLOR.RED100.render(r, body, -4);
					OPACITY.unbind();
				}
			}
			
			
		};
		bb.body().setWidth(200);
		bb.icon(SPRITES.icons().s.sword);
		ss.addRightC(0, bb);
		
		bb = new GButt.ButtPanel(¤¤Wait){
			
			
			@Override
			protected void clickA() {
				close.exe();
			}

			@Override
			public boolean hover(COORDINATE mCoo) {
				
				if (super.hover(mCoo)) {
					hovRetreat = true;
					return true;
				}
				return false;
			}
			
		};
		bb.hoverInfoSet(¤¤WaitD);
		bb.body().setWidth(200);
		bb.icon(SPRITES.icons().s.clock);
		ss.addRightC(0, bb);
		
		
		
		bb = new GButt.ButtPanel(¤¤Lift) {
			
			@Override
			protected void clickA() {
				close.exe();
				spec.lift();
			}

			@Override
			public boolean hover(COORDINATE mCoo) {
				
				if (super.hover(mCoo)) {
					hovRetreat = true;
					return true;
				}
				return false;
			}
			
		};
		bb.hoverInfoSet(¤¤LiftD);
		bb.body().setWidth(200);
		bb.icon(SPRITES.icons().s.arrow_left);
		ss.addRightC(0, bb);
		
		
		

		s.addRelBody(16, DIR.S, ss);
	}
	
	GuiSection init(WBattleSiege spec) {
		this.spec = spec;
		player.set(spec.player);
		enemy.set(spec.enemy);
		return s;
	}
	
	
	
	
}
