package view.ui.battle;

import game.boosting.BOOSTABLES;
import game.boosting.BOOSTABLES.BDamage;
import game.boosting.Boostable;
import init.C;
import init.race.POP_CL;
import init.race.RACES;
import init.sprite.UI.UI;
import settlement.army.Div;
import settlement.stats.STATS;
import settlement.stats.equip.EquipRange;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.Hoverable.HOVERABLE.HoverableAbs;
import snake2d.util.sprite.SPRITE;
import util.dic.DicArmy;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.gui.misc.GMeter.GMeterCol;
import util.info.GFORMAT;
import world.army.AD;
import world.army.util.DIV_STATS;

public final class UIDivStats {


	private static int width = 300;
	private static final int height = 20;
	
	public static class WDivStats {
		
		private DIV_STATS div;
		
		private final GuiSection ss = new GuiSection();
		
		public WDivStats(){

			pair(BOOSTABLES.BATTLE().OFFENCE, BOOSTABLES.BATTLE().DEFENCE);
			pair(BOOSTABLES.BATTLE().BLUNT_ATTACK, BOOSTABLES.BATTLE().BLUNT_DEFENCE);

			for (BDamage bb : BOOSTABLES.BATTLE().DAMAGES) {
				pair(bb.attack, bb.defence);
			}
			
			icon(BOOSTABLES.BATTLE().MORALE.icon);
			ss.addRightC(4, new GaugeBo(BOOSTABLES.BATTLE().MORALE, GMeter.C_BLUE, width, height));
			
			
//			add(ss, jj);
//			
			{
				
				icon(UI.icons().s.bow);
				
				HoverableAbs aa = new HoverableAbs(width, height) {
					
					@Override
					protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
						double def = 0;
						double curr = 0;
						double max = 0;
						EquipRange rr = best(div);
						if (rr != null) {
							def = rr.projectile.range(rr.ref(0, 0));
							curr = rr.projectile.range(rr.ref(div.equip(rr), AD.UTIL().boost(div, rr.boostable)/rr.boostable.max(POP_CL.class)));
							for (EquipRange e : STATS.EQUIP().RANGED()) {
								max = Math.max(max, e.projectile.range(1.0));
							}
							def /=C.TILE_SIZE;
							curr/= C.TILE_SIZE;
							max /= C.TILE_SIZE;
						}
						GMeter.renderDelta(r, def/max, curr/max, body.x1(), body().x2(), body.y1(), body.cY(), GMeter.C_YELLOW);
						if (rr != null) {
							def = AD.UTIL().power.rangedPower(rr, 0, 0);
							curr = AD.UTIL().power.rangedPower(rr, div.equip(rr), AD.UTIL().boost(div, rr.boostable)/rr.boostable.max(POP_CL.class));
						}
						max = AD.UTIL().power.maxRanged()+1;
						GMeter.renderDelta(r, def/max, curr/max, body.x1(), body().x2(), body.cY(), body.y2(), GMeter.C_ORANGE);
					}
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						GBox box = (GBox) text;
						box.title(DicArmy.¤¤Ammunition);
						EquipRange b = best(div);
						
						if (b != null) {
							//box.add(GFORMAT.f0(box.text(), b.ref(div.equip(b), BattleBoosts.res().get(b.boostable))));
							b.projectile.hover(box, null, b.ref(div.equip(b), AD.UTIL().boost(div, b.boostable)/b.boostable.max(POP_CL.class)));
						}
						
					}
				};
				ss.addRightC(4, aa);
				
			}
			
			GuiSection oo = new GuiSection();
			
			oo.addDown(8, simple(BOOSTABLES.PHYSICS().SPEED));
			oo.addDown(8, simple(BOOSTABLES.PHYSICS().MASS));
			oo.addDown(8, simple(BOOSTABLES.PHYSICS().STAMINA));
			
			ss.addRelBody(16, DIR.E, oo);
			ss.body().incrW(64);
//			
//			for (Boostable bo : BattleBoosts.OTHERS()) {
//				if (bo == BOOSTABLES.BATTLE().OFFENCE || bo == BOOSTABLES.BATTLE().DEFENCE)
//					continue;
//				
//				RENDEROBJ jj = new Pair(bo.icon, dwidth, new GaugeBo(bo, GMeter.C_BLUE));
//				add(ss, jj);
//			}
		}
		
		public GuiSection get(DIV_STATS div) {
			this.div = div;
			return ss;
		}
		
		private static EquipRange best(DIV_STATS div) {
			double max = 0;
			EquipRange b = null;
			for (EquipRange rr : STATS.EQUIP().RANGED()) {
				if (div.equip(rr) > 0) {
					double m = AD.UTIL().power.rangedPower(rr, div.equip(rr), AD.UTIL().boost(div, rr.boostable)/rr.boostable.max(POP_CL.class));
					if (m > max) {
						max = m;
						b = rr;
					}
				}
					
			}
			return b;
		}
		
		private void pair(Boostable a, Boostable b) {
			
			icon(a.icon);
			ss.addRight(4, new GaugeBo(a, GMeter.C_YELLOW, width/2, height));
			ss.addRight(0, new GaugeBo(b, GMeter.C_ORANGE, width/2, height));
		}
		
		private void icon(SPRITE icon) {
			ss.add(icon, 0,  ss.getLastY2());
		}
		
		private HOVERABLE simple(Boostable bo) {
			return new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.fRel(text, AD.UTIL().boost(div, bo), bo.get(div.race()));
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					hoverI(div, bo, b);
				};
				
			}.hh(bo.icon);
		}
		
		public static void hoverI(DIV_STATS st, Boostable bo, GUI_BOX text) {
			GBox b = (GBox) text;
			b.title(bo.name);
			b.text(bo.desc);
			b.sep();
			
			double tot = AD.UTIL().boost(st, bo);
			double race = bo.get(st.race());
			double fac = bo.get(st.faction());
			b.textLL(RACES.name());
			b.tab(7);
			b.add(GFORMAT.f(b.text(), race));
			b.NL(2);
			b.textLL(DicMisc.¤¤Faction);
			b.tab(7);
			b.add(GFORMAT.f0(b.text(), fac));
			b.NL(2);
			b.textLL(DicMisc.¤¤Settings);
			b.tab(7);
			b.add(GFORMAT.f0(b.text(), tot-race-fac));
			b.NL(8);
			b.textLL(DicMisc.¤¤Total);
			b.tab(7);
			b.add(GFORMAT.f0(b.text(), tot));
		}
		
		private class GaugeBo extends Gauge {

			private final Boostable bo;
			
			GaugeBo(Boostable bo, GMeterCol col, int width, int height){
				super(col, width, height);
				this.bo = bo;
			}
			

			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				double max = bo.max(Div.class);
				double rr = bo.get(div.race());
				double d = AD.UTIL().boost(div, bo);
				
				render(r, rr, d, max);
			}


			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				hoverI(div, bo, text);
			}

			
			
		}
		
	}
	
	public static class DivStats {
		
		private Div div;
		
		private final GuiSection ss = new GuiSection();
		
		public DivStats(){

			pair(BOOSTABLES.BATTLE().OFFENCE, BOOSTABLES.BATTLE().DEFENCE);
			pair(BOOSTABLES.BATTLE().BLUNT_ATTACK, BOOSTABLES.BATTLE().BLUNT_DEFENCE);

			for (BDamage bb : BOOSTABLES.BATTLE().DAMAGES) {
				pair(bb.attack, bb.defence);
			}
			
			icon(BOOSTABLES.BATTLE().MORALE.icon);
			ss.addRightC(4, new GaugeBo(BOOSTABLES.BATTLE().MORALE, GMeter.C_BLUE, width, height));
			
			
//			add(ss, jj);
//			
			{
				
				icon(UI.icons().s.bow);
				
				HoverableAbs aa = new HoverableAbs(width, height) {
					
					@Override
					protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
						double def = 0;
						double curr = 0;
						double max = 0;
						EquipRange rr = best(div);
						if (rr != null) {
							def = rr.projectile.range(rr.ref(0, 0));
							curr = rr.projectile.range(rr.ref(rr.stat().div().getD(div), rr.boostable.get(div)/rr.boostable.max(POP_CL.class)));
							for (EquipRange e : STATS.EQUIP().RANGED()) {
								max = Math.max(max, e.projectile.range(1.0));
							}
							def /=C.TILE_SIZE;
							curr/= C.TILE_SIZE;
							max /= C.TILE_SIZE;
						}
						GMeter.renderDelta(r, def/max, curr/max, body.x1(), body().x2(), body.y1(), body.cY(), GMeter.C_YELLOW);
						if (rr != null) {
							def = AD.UTIL().power.rangedPower(rr, 0, 0);
							curr = AD.UTIL().power.rangedPower(rr, rr.stat().div().getD(div), rr.boostable.get(div)/rr.boostable.max(POP_CL.class));
						}
						max = AD.UTIL().power.maxRanged()+1;
						GMeter.renderDelta(r, def/max, curr/max, body.x1(), body().x2(), body.cY(), body.y2(), GMeter.C_ORANGE);
					}
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						GBox box = (GBox) text;
						box.title(DicArmy.¤¤Ammunition);
						EquipRange b = best(div);
						
						if (b != null) {
							//box.add(GFORMAT.f0(box.text(), b.ref(div.equip(b), BattleBoosts.res().get(b.boostable))));
							b.projectile.hover(box, null, b.ref(b.stat().div().getD(div), b.boostable.get(div)/b.boostable.max(POP_CL.class)));
						}
						
					}
				};
				ss.addRightC(4, aa);
				
			}
			
			GuiSection oo = new GuiSection();
			
			oo.addDown(8, simple(BOOSTABLES.PHYSICS().SPEED));
			oo.addDown(8, simple(BOOSTABLES.PHYSICS().MASS));
			oo.addDown(8, simple(BOOSTABLES.PHYSICS().STAMINA));
			
			ss.addRelBody(16, DIR.E, oo);
			ss.body().incrW(64);
//			
//			for (Boostable bo : BattleBoosts.OTHERS()) {
//				if (bo == BOOSTABLES.BATTLE().OFFENCE || bo == BOOSTABLES.BATTLE().DEFENCE)
//					continue;
//				
//				RENDEROBJ jj = new Pair(bo.icon, dwidth, new GaugeBo(bo, GMeter.C_BLUE));
//				add(ss, jj);
//			}
		}
		
		public GuiSection get(Div div) {
			this.div = div;
			return ss;
		}
		
		private static EquipRange best(Div div) {
			double max = 0;
			EquipRange b = null;
			for (EquipRange rr : STATS.EQUIP().RANGED()) {
				if (rr.stat().div().getD(div) > 0) {
					double m = AD.UTIL().power.rangedPower(rr, rr.stat().div().getD(div), rr.boostable.get(div)/rr.boostable.max(POP_CL.class));
					if (m > max) {
						max = m;
						b = rr;
					}
				}
					
			}
			return b;
		}
		
		private void pair(Boostable a, Boostable b) {
			
			icon(a.icon);
			ss.addRight(4, new GaugeBo(a, GMeter.C_YELLOW, width/2, height));
			ss.addRight(0, new GaugeBo(b, GMeter.C_ORANGE, width/2, height));
		}
		
		private void icon(SPRITE icon) {
			ss.add(icon, 0,  ss.getLastY2());
		}
		
		private HOVERABLE simple(Boostable bo) {
			return new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.fRel(text, bo.get(div), bo.get(div.info.race()));
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					bo.hover(b, div, false);
				};
				
			}.hh(bo.icon);
		}
		
		private class GaugeBo extends Gauge {

			private final Boostable bo;
			
			GaugeBo(Boostable bo, GMeterCol col, int width, int height){
				super(col, width, height);
				this.bo = bo;
			}
			

			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				double max = bo.max(Div.class);
				double rr = bo.get(div.info.race());
				double d = bo.get(div);
				
				render(r, rr, d, max);
			}


			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				bo.hover(text, div, false);
			}

			
			
		}
		
	}

	

	private static abstract class Gauge extends HoverableAbs {
		
		public final GMeterCol col;
		private final GText tt = new GText(UI.FONT().S, 4);
		
		Gauge(GMeterCol col, int width, int height){
			super(width, height);
			this.col = col;
		}

		protected void render(SPRITE_RENDERER r, double def, double current, double max) {
			
			int X1 = body.x1();
			int X2 = body.x2();
			int Y1 = body.y1();
			int Y2 = body.y2();
			GMeter.renderDelta(r, def/max, current/max, X1, X2, Y1, Y2, col);
			
			tt.clear();
			GFORMAT.f(tt, current, 1);
			tt.adjustWidth();
			OPACITY.O35.bind();
			
			X2 -= 4;
			X1 = X2-tt.width()-8;
			Y1 += ((Y2-Y1)-tt.height())/2;
			Y2 = Y1 + tt.height();
			
			COLOR.BLACK.render(r, X1, X2, Y1, Y2);
			OPACITY.unbind();
			tt.render(r, X1+4, Y1);
			
		}

		
		
	}
	
	

	
	

	

	
}
