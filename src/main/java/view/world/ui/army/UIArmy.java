package view.world.ui.army;

import java.util.Arrays;

import game.faction.FACTIONS;
import game.time.TIME;
import init.D;
import init.RES;
import init.resources.ArmySupply;
import init.resources.RESOURCES;
import init.settings.S;
import init.sprite.ICON;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.stats.STATS;
import settlement.stats.StatsEquippables.EQUIPPABLE_MILITARY;
import snake2d.MButt;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.STRING_RECIEVER;
import snake2d.util.sets.ArrayList;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.StringInputSprite;
import util.colors.GCOLOR;
import util.colors.GCOLORS_MAP;
import util.data.DOUBLE;
import util.dic.*;
import util.gui.misc.*;
import util.gui.misc.GMeter.GMeterSprite;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import view.common.ArmyPicker;
import view.interrupter.ISidePanel;
import view.keyboard.KEYS;
import view.main.VIEW;
import view.world.ui.army.UIArmyRecruit.BannerEditor;
import world.World;
import world.army.WARMYD;
import world.army.WARMYD.WArmySupply;
import world.army.WDIV;
import world.entity.army.WArmy;
import world.map.regions.Region;

final class UIArmy extends ISidePanel{

	static WArmy army;
	private boolean[] selected = new boolean[RES.config().BATTLE.DIVISIONS_PER_ARMY];
	private Button clicked;
	private boolean dragging;
	private ToolMove tool = new ToolMove();
	
	private BannerEditor banner = new BannerEditor() {
		
		@Override
		public void bannerSet(int bi) {
			for (int i = 0; i < selected.length; i++) {
				if (selected[i]) {
					if (army == null || army.divs() == null || army.divs().get(i) == null)
						continue;
					army.divs().get(i).bannerSet(bi);
				}
			}
		};
		
	};
	
	private static CharSequence ¤¤ConsumtionRate = "Yearly Consumption:";
	private static CharSequence ¤¤ConsumtionPerMan = "(-{0} per soldier)";
	private static CharSequence smorale = "({0} - 1)";
	private static CharSequence ¤¤MoraleDesc = "Morale is gained by keeping the army well supplied. the morale affects your army's performance on the battlefield.";
	private static CharSequence ¤¤HealthDesc = "Health is gained by keeping the army well supplied. Poor health will lead to desertion.";
	
	private final StringInputSprite name = new StringInputSprite(Region.nameSize, UI.FONT().H2) {
		@Override
		protected void change() {
			army.name.clear().add(text());
		};
	};
	
	public UIArmy() {
		
		D.t(this);
		titleSet(DicArmy.¤¤Army);
		
		section = new GuiSection() {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				super.render(r, ds);
				
				dragging &= MButt.LEFT.isDown();
				
			}
		};
		
		section.add(new GInput(name));
		
		{
			RENDEROBJ o = new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.i(text, WARMYD.quality().get(army));
				}
			}.hh(DicMisc.¤¤Power);
			section.addRelBody(8, DIR.S, o);
		}
		
		{
			DOUBLE d = new DOUBLE() {
				
				@Override
				public double getD() {
					return WARMYD.supplies().morale(army);
				}
			};
			GuiSection s = new GuiSection() {
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					b.title(DicArmy.¤¤Morale);
					
					b.add(GFORMAT.perc(b.text(), d.getD()));
					b.NL();
					b.text(¤¤MoraleDesc);
				}
				
			};
			
			s.add(SPRITES.icons().s.standard, 0, 0);
			
			GMeterSprite m = new GMeter.GMeterSprite(GMeter.C_REDGREEN, d, 200, 16);
			s.addRightC(8, m);
			
			section.addRelBody(8, DIR.S, s);
		}
		
		{
			DOUBLE d = new DOUBLE() {
				
				@Override
				public double getD() {
					return WARMYD.supplies().health(army);
				}
			};
			GuiSection s = new GuiSection() {
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					b.title(DicMisc.¤¤Health);
					
					b.add(GFORMAT.perc(b.text(), d.getD()));
					b.NL();
					b.text(¤¤HealthDesc);
				}
				
			};
			
			s.add(SPRITES.icons().s.pluses, 0, 0);
			
			GMeterSprite m = new GMeter.GMeterSprite(GMeter.C_REDGREEN, d, 200, 16);
			s.addRightC(8, m);
			
			section.addRelBody(8, DIR.S, s);
		}
		
		
		
		section.addRelBody(8, DIR.S, new GStat() {
			
			@Override
			public void update(GText text) {
				army.state().info(army, text);
			}
		}.r(DIR.N));
		
		
		section.add(new GButt.ButtPanel(SPRITES.icons().m.for_muster) {
			
			private UIArmyRecruit s = new UIArmyRecruit();
			
			@Override
			protected void clickA() {
				last().add(s, false);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.text(DicArmy.¤¤Recruit);
			}
			
		}, 0, section.getLastY2()+8);
		
		section.addRightC(4, new GButt.ButtPanel(new ICON.MEDIUM.Twin(SPRITES.icons().m.sword, SPRITES.icons().s.money)) {
			
			private UIArmyMerenary s = new UIArmyMerenary();
			
			@Override
			protected void clickA() {
				VIEW.inters().popup.show(s, this);
			}
			
		}.hoverTitleSet(DicArmy.¤¤Mercenaries).hoverInfoSet(D.g("mercenaryD", "Hiring Mercenaries is instant and they equip, feed and replenish themselves. They cost credits to hire and requires credits in upkeep.")));
		
		section.addRightC(16, new GButt.ButtPanel(new ICON.MEDIUM.Twin(SPRITES.icons().m.crossair, SPRITES.icons().m.anti)) {
			
			@Override
			protected void clickA() {
				army.stop();
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.text(DicArmy.¤¤Stop);
			}
			
			@Override
			protected void renAction() {
				activeSet(army.path().moving(army.body()));
			}
			
		});
		
		int am = 7;
		
		section.add(new GButt.ButtPanel(SPRITES.icons().m.cancel) {
			
			@Override
			protected void clickA() {
				army.disband();
				last().remove(UIArmy.this);
			}
			
			@Override
			protected void renAction() {
				activeSet(WARMYD.men(null).target().get(army) == 0);
			}
			
		}.hoverInfoSet(D.g("disbandD", "Disband entire army. (All divisions must first be disbanded)")), (am-1)*DivCard.WIDTH, section.getLastY1());
		
		{
			section.addRelBody(4, DIR.S, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.i(text, WARMYD.supplies().credits().get(army));
				}
				
				
			}.hh(SPRITES.icons().s.money).hoverInfoSet(D.g("CreditsD", "The amount of money needed to upkeep this army daily.")));
		}
		
		{
			GuiSection s = new GuiSection();
			
			int i = 0;
			
			for (ArmySupply q : RESOURCES.SUP().ALL()) {
				WArmySupply su = WARMYD.supplies().get(q);
				RENDEROBJ g = supply(su, false);
				
				s.add(g, (i%3)*(g.body().width()+16), (i/3)*(g.body().height()+4));
				i++;
				
			}
			
			section.addRelBody(8, DIR.S, s);
		}
		
		{
			GuiSection s = new GuiSection();
			
			int i = 0;
			
			for (EQUIPPABLE_MILITARY q : STATS.EQUIP().military_all()) {
				WArmySupply su = WARMYD.supplies().get(q);
				RENDEROBJ g = supply(su, true);
				
				s.add(g, (i%3)*(g.body().width()+16), (i/3)*(g.body().height()+4));
				i++;
				
			}
			
			section.addRelBody(8, DIR.S, s);
		}
		
		{
			GuiSection ctrl = new GuiSection();
			
			ctrl.add(new GButt.Panel(SPRITES.icons().m.place_brush) {
				
				@Override
				protected void clickA() {
					if (activeIs())
						VIEW.inters().popup.show(banner.view(), this);
					
				}
				
				@Override
				protected void renAction() {
					activeSet(false);
					for (boolean s : selected)
						if (s) {
							activeSet(true);
							return;
						}
				};
				
				
			}.hoverTitleSet(DicArmy.¤¤Banner));
			
			ctrl.addRightC(32, new GButt.Panel(SPRITES.icons().m.cancel) {
				
				@Override
				protected void clickA() {
					int off = 0;
					for (int i = 0; i < selected.length; i++) {
						if (selected[i]) {
							if (army == null || army.divs() == null || army.divs().get(i-off) == null)
								continue;
							army.divs().get(i-off).disband();
							off++;
							selected[i] = false;
						}
					}
					clicked = null;
					dragging = false;
				}
				
				@Override
				protected void renAction() {
					activeSet(false);
					for (boolean s : selected)
						if (s) {
							activeSet(true);
							return;
						}
				};
				
				
			}.hoverTitleSet(DicArmy.¤¤Disband).hoverInfoSet(D.g("disbandDivD", "Disband selected divisions")));
			ctrl.addRightC(2, new GButt.Panel(SPRITES.icons().m.arrow_right) {
				
				ArmyPicker p = new ArmyPicker() {
					
					@Override
					protected void pick(WArmy a) {
						int off = 0;
						for (int i = 0; i < selected.length; i++) {
							if (selected[i]) {
								army.divs().get(i-off).reassign(a);
								off++;
								selected[i] = false;
							}
						}
						VIEW.inters().popup.close();
					}
					
					@Override
					protected boolean canBePicked(WArmy a) {
						
						int am = 0;
						for (int i = 0; i < selected.length; i++) {
							if (selected[i]) {
								am++;
							}
						}
						
						return a != army && a.divs().size() + am < RES.config().BATTLE.DIVISIONS_PER_ARMY && Math.abs(a.ctx()-army.ctx()) + Math.abs(a.cty()-army.cty()) < 4;
						
					}
				};
				
				@Override
				protected void renAction() {
					activeSet(false);
					for (WArmy a : FACTIONS.player().kingdom().armies().all()) {
						if (a != army && Math.abs(a.ctx()-army.ctx()) + Math.abs(a.cty()-army.cty()) < 4) {
							activeSet(true);
							return;
						}
					}
				};
				
				@Override
				protected void clickA() {
					VIEW.inters().popup.show(p, this);
				};
				
				
			}.hoverTitleSet(DicArmy.¤¤Reassign).hoverInfoSet(D.g("MoveD", "Move this division to another army. The other army must be within 3 tiles.")));
			
			section.addRelBody(8, DIR.S, ctrl);
		}
		
		
		
		ArrayList<GuiSection> rows = new ArrayList<GuiSection>((int)Math.ceil(RES.config().BATTLE.DIVISIONS_PER_ARMY/(double)am));

		for (int i = 0; i < rows.max(); i++) {
			rows.add(new GuiSection());
		}
		
		for (int i = 0; i < RES.config().BATTLE.DIVISIONS_PER_ARMY; i++) {
			
			GuiSection s = rows.get(i/am);
			s.addRightC(2, new Button(i));
		}
		
		
		section.addRelBody(8, DIR.S, new GScrollRows(rows, HEIGHT-section.getLastY2()-8).view());
		
	}
	
	private RENDEROBJ supply(WArmySupply su, boolean equipment) {
		
		int w = 60;
		int h = 14;
		
		SPRITE s = new SPRITE.Imp(w, h) {
			
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				if (su.max(army) == 0) {
					GMeter.render(r, GMeter.C_GREEN_DARK, 0, X1, X2, Y1, Y2);
					return ;
				}
				
				double now = (double)su.used().get(army)/su.max(army);
				double needed = (double)su.current().get(army)/su.max(army);
				
				if (su.current().get(army) >= su.used().get(army))
					GMeter.render(r, GMeter.C_REDGREEN, needed, X1, X2, Y1, Y2);
				else
					GMeter.render(r, GMeter.C_REDORANGE, needed, X1, X2, Y1, Y2);
				
				X1 += 3 + now*(X2-X1);
				
				GCOLOR.UI().border().render(r, X1-1, X1+1, Y1, Y2);
				

			}
		};

		
		RENDEROBJ o = new GHeader.HeaderHorizontal(su.res.icon(), s) {
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				b.title(su.res.name);
				b.NL();
				
				int m = 6;
				
				b.textL(DicRes.¤¤Stored);
				b.tab(m);
				b.add(GFORMAT.i(b.text(), su.current().get(army)));
				b.NL();
				
				b.textL(DicMisc.¤¤Minimum);
				b.tab(m);
				b.add(GFORMAT.i(b.text(), su.used().get(army)));
				b.NL();
				
				b.textL(DicMisc.¤¤Max);
				b.tab(m);
				b.add(GFORMAT.i(b.text(), su.max(army)));
				b.NL();
				
				b.textL(¤¤ConsumtionRate);
				b.tab(m);
				b.add(GFORMAT.f(b.text(), su.used().get(army)*su.usedPerDay*TIME.years().bitConversion(TIME.days())));
				GText t = b.text();
				t.add(¤¤ConsumtionPerMan);
				t.insert(0, su.usedPerDay*TIME.years().bitConversion(TIME.days()), 4);
				b.add(t);
				b.NL();
				
				if (!equipment) {
					b.NL(8);
					
					b.textL(DicArmy.¤¤Morale);
					b.tab(m);
					b.add(GFORMAT.f1(b.text(), su.morale(army)));
					t = b.text();
					t.add(smorale);
					t.insert(0, 1-su.morale, 1);
					b.add(t);
					b.NL();
					
					b.textL(DicMisc.¤¤Health);
					b.tab(m);
					b.add(GFORMAT.f1(b.text(), su.health(army)));
					t = b.text();
					t.add(smorale);
					t.insert(0, 1-su.health, 1);
					b.add(t);
					b.NL();
				}
				
				
				
			}
		};
		
		if (S.get().developer) {
			
			GuiSection ss = new GuiSection() {
				
				STRING_RECIEVER rec = new STRING_RECIEVER() {
					
					@Override
					public void acceptString(CharSequence string) {
						try {
							double d = Double.parseDouble(""+string);
							su.current().set(army, (int) (su.max(army)*d));
						}catch(Exception e) {
							
						}
						
						
					}
				};
				
				@Override
				protected void clickA() {
					VIEW.inters().input.requestInput(rec, "set");
					super.clickA();
				}
			};
			ss.add(o);
			return ss;
		}else {
			return o;
		}
		
		
	}
	
	
	ISidePanel get(WArmy army) {
		UIArmy.army = army;
		name.text().clear().add(army.name);
		dragging = false;
		Arrays.fill(selected, false);
		clicked = null;
		VIEW.world().tools.place(tool, tool.config, false);
		return this;
	}
	
	@Override
	protected void update(float ds) {
		World.OVERLAY().hover(army.body(), GCOLORS_MAP.get(army.faction()), false, M);
		if (!army.added() && last() != null && last().added(this)) {
			last().remove(this);
		}
	}
	
	private class Button extends CLICKABLE.ClickableAbs {
		
		private final int ii;
		
		Button(int ii){
			this.ii = ii;
			body.setDim(DivCard.WIDTH, DivCard.HEIGHT);
		}

		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			activeSet(ii < army.divs().size());
			if (ii >= army.divs().size())
				return;
			selectedSet(selected[ii]);
			GCOLOR.UI().border().render(r, body);
			WDIV d = army.divs().get(ii);
			VIEW.world().UI.armies.divCard.renderArmy(r, body().x1(), body().y1(), d, army, d.men() > 0, isSelected, isHovered);
			
			if (dragging && isHovered && clicked != null && clicked != this && !KEYS.MAIN().UNDO.isPressed() && !KEYS.MAIN().MOD.isPressed()) {
				COLOR.GREEN100.render(r, body().x1()-2, body().x1()+2, body().y1(), body().y2());
				if (!MButt.LEFT.isDown()) {
					army.divs().insert(ii, clicked.ii);
					selected[clicked.ii] = selected[ii];
					selected[ii] = false;
				}
			}
			
			
			
		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			
			return super.hover(mCoo);
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			if (ii >= army.divs().size())
				return;
			GBox b = (GBox) text;
			WDIV d = army.divs().get(ii);
			d.hover(b);
		}
		
		@Override
		protected void clickA() {
			
			if (KEYS.MAIN().UNDO.isPressed() && clicked != null) {
				Arrays.fill(selected, false);
				if (clicked.ii < ii) {
					for (int i = clicked.ii; i <= ii; i++) {
						selected[i] = true;
					}
					
					
				}else {
					for (int i = ii; i <= clicked.ii; i++) {
						selected[i] = true;
					}
				}
				
				
			}else if(KEYS.MAIN().MOD.isPressed()) {
				selected[ii] = !selected[ii];
			}else {
				Arrays.fill(selected, false);
				selected[ii] = true;
				clicked = this;
				dragging = true;
			}
		}
		
	}
	
	
	
}
