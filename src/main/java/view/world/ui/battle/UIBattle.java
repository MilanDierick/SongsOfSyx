package view.world.ui.battle;

import game.GAME;
import init.D;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import settlement.main.SETT;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.text.Text;
import util.data.DOUBLE_O;
import util.data.GETTER;
import util.data.GETTER.GETTER_IMP;
import util.dic.DicArmy;
import util.gui.misc.*;
import world.WORLD;
import world.battle.spec.*;
import world.regions.Region;

class UIBattle {

	private static CharSequence ¤¤Artill = "¤Artillery pieces";
	
	static {
		D.ts(UIBattle.class);
	}
	
	
	private final GETTER_IMP<WBattleSide> player = new GETTER_IMP<>();
	private final GETTER_IMP<WBattleSide> enemy = new GETTER_IMP<>();
	
	private boolean hovRetreat = false;
	private boolean hovFight = false;
	
	private WBattleSpec spec;
	
	private final DOUBLE_O<WBattleUnit> pLosses = new DOUBLE_O<WBattleUnit>() {

		@Override
		public double getD(WBattleUnit t) {
			if (hovRetreat)
				return t.lossesRetreat;
			if (hovFight)
				return 0;
			return t.losses;
		}
		
	};
	
	private final DOUBLE_O<WBattleUnit> eLosses = new DOUBLE_O<WBattleUnit>() {

		@Override
		public double getD(WBattleUnit t) {
			if (hovRetreat)
				return 0;
			if (hovFight)
				return 0;
			return t.losses;
		}
		
	};
	
	private final GuiSection s = new GuiSection() {
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			hovRetreat = false;
			hovFight = false;
			return super.hover(mCoo);
		};
		
	};

	
	UIBattle(ACTION close, UIWBattlePrompt ui){
		
		RENDEROBJ o = new GStat(UI.FONT().H2) {

			@Override
			public void update(GText text) {
				
				Region reg = WORLD.REGIONS().map.get(player.get().coo);
				if (reg != null) {
					text.add(DicArmy.¤¤BattleOf);
					text.insert(0,reg.info.name());
				}else {
					text.add(DicArmy.¤¤Battle);
				}
				text.lablify();
			}
			
		}.r(DIR.N);
		s.add(o);

		s.addRelBody(8, DIR.S, Util.balance(player, enemy));
		
		{
			GuiSection row = new GuiSection();
			row.add(new Art(player));
			row.addRightC(0, new Art(enemy));
			s.addRelBody(8, DIR.S, row);
		}
		
		s.addRelBody(2, DIR.S, Util.confict(player, enemy, pLosses, eLosses));
		
		
		GuiSection ss = new GuiSection();
		GButt.ButtPanel bb;
		
		bb = new GButt.ButtPanel(DicArmy.¤¤Engage) {
			@Override
			protected void clickA() {
				close.exe();
				spec.engage();
				
			}
			
			@Override
			public boolean hover(COORDINATE mCoo) {
				
				if (super.hover(mCoo)) {
					hovFight = true;
					return true;
				}
				return false;
			}
		};
		bb.hoverInfoSet(DicArmy.¤¤engageD);
		bb.icon(UI.icons().s.sword);
		bb.body.setWidth(200);
		ss.addRightC(0, bb);

		bb = new GButt.ButtPanel(DicArmy.¤¤AutoResolve){
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
			protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
					boolean isHovered) {
				super.render(r, ds, isActive, isSelected, isHovered);
				if (spec.victory) {
					OPACITY.O25.bind();
					COLOR.ORANGE100.render(r, body, -4);
					OPACITY.unbind();
				}
			}
			
			@Override
			protected void clickA() {
				close.exe();
				spec.auto();
			}
		};
		bb.icon(UI.icons().s.cog);
		bb.body.setWidth(200);
		ss.addRightC(0, bb);
		
		bb = new GButt.ButtPanel(DicArmy.¤¤Retreat){
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				
				if (player.get().lossesRetreat >= player.get().men)
					text.text(DicArmy.¤¤RetreatCant);
				else {
					Text t = text.text();
					t.add(DicArmy.¤¤RetreatD);
					t.insert(0, player.get().lossesRetreat);
					text.add(t);
				}
				
				
			}
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
					boolean isHovered) {
				super.render(r, ds, isActive, isSelected, isHovered);
				if (player.get().lossesRetreat >= player.get().men) {
					OPACITY.O25.bind();
					COLOR.RED100.render(r, body, -4);
					OPACITY.unbind();
				}
			}
			
			@Override
			protected void clickA() {
				if (player.get().lossesRetreat >= player.get().men) {
					return;
				}
				close.exe();
				spec.retreat();
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
		bb.icon(UI.icons().s.arrow_left);
		bb.body.setWidth(200);
		ss.addRightC(0, bb);
		
		
		s.addRelBody(8, DIR.S, ss);
		
	}
	
	GuiSection init(WBattleSpec spec) {
		this.spec = spec;
		this.player.set(spec.player);
		this.enemy.set(spec.enemy);
		return s;
	}
	

	private static class Art extends HOVERABLE.HoverableAbs {

		private final GETTER<WBattleSide> g;
		
		Art(GETTER<WBattleSide> g){
			super((Util.width)/2, Icon.L);
			this.g = g;
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
			
			if (g.get().artilleryPieces <= 0)
				return;
			
			int d = (body().width()-50)/g.get().artilleryPieces;
			d = CLAMP.i(d, 1, 16);

			
			for (int i = 0; i < g.get().artilleryPieces; i++) {
				SETT.ROOMS().ARTILLERY.get(0).icon.small.render(r, body.x1()+i*d, body().y1());
			}
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			if (g.get().artilleryPieces > 0) {
				text.title(¤¤Artill);
				text.NL();
				text.add(text.text().add(g.get().artilleryPieces));
			}
		}
		
	}
	

	
	
	
}
