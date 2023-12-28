package view.world.ui.army;

import game.time.TIME;
import init.D;
import init.resources.ArmySupply;
import init.resources.RESOURCES;
import init.settings.S;
import init.sprite.SPRITES;
import settlement.stats.STATS;
import settlement.stats.equip.EquipBattle;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.STRING_RECIEVER;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.dic.*;
import util.gui.misc.*;
import util.info.GFORMAT;
import view.main.VIEW;
import world.army.AD;
import world.army.ADSupply;
import world.entity.army.WArmy;

class ArmyInfo extends GuiSection{


	private static CharSequence ¤¤MoraleDesc = "Morale is gained by keeping the army well supplied. the morale affects your army's performance on the battlefield.";
	private static CharSequence ¤¤HealthDesc = "Health is gained by keeping the army well supplied. Poor health will lead to desertion.";
	
	private static CharSequence ¤¤ConsumtionRate = "Yearly Consumption:";
	private static CharSequence ¤¤ConsumtionPerMan = "(-{0} per soldier)";
	private static CharSequence smorale = "({0} - 1)";
	private static CharSequence ¤¤CreditsD = "The amount of money needed to upkeep this army daily.";
	private static CharSequence ¤¤EquipmentD = "Equipment determines how well your army will be equipped when they do battle. They have no effect on health or morale. An army needs the equipment that has been supplied, but also a surplus to last the army when it is in hostile lands and can't be supplied.";
	private static CharSequence ¤¤SupplyD = "Supplies are essential to drafted divisions and divisions from your city. EAch supply has a minimum storage needed, but also a surplus to last the army when away from supply routes. Each supply affects health or morale or both. Army supply depots are needed in your city to ensure your armies are well supplied.";
	
	
	static {
		D.ts(ArmyInfo.class);
	}
	
	public static GuiSection info(GETTER<WArmy> army) {
		GuiSection ss = new GuiSection();
		
		ss.add(new GStat() {
			
			@Override
			public void update(GText text) {
				army.get().state().info(army.get(), text);
				text.lablifySub();
			}
		}.r(DIR.NW));
		
		ss.add(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.perc(text, AD.supplies().morale(army.get()));
			}
		}.hh(SPRITES.icons().s.standard).hoverTitleSet(DicArmy.¤¤Morale).hoverInfoSet(¤¤MoraleDesc), 0, ss.body().y2()+2);
		ss.addRightC(80, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.perc(text, AD.supplies().health(army.get()));
			}
		}.hh(SPRITES.icons().s.pluses).hoverTitleSet(DicMisc.¤¤Health).hoverInfoSet(¤¤HealthDesc));
		
		ss.addRightC(80, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, (int)AD.UTIL().power.get(army.get()));
				if (S.get().developer) {
					text.s();
					GFORMAT.i(text, (int)AD.power().get(army.get()));
				}
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				AD.UTIL().power.hover(army.get(), b);
			};
			
		}.hh(SPRITES.icons().s.fist));
		
		
		ss.addRightC(80, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, AD.men(null).get(army.get()));
			}
			@Override
			public void hoverInfoGet(GBox b) {
				b.title(DicArmy.¤¤Soldiers);
				b.add(GFORMAT.iofkInv(b.text(), AD.men(null).get(army.get()), AD.men(null).target().get(army.get())));
				
			};
			
		}.hh(SPRITES.icons().s.human));
		
		ss.body().incrW(64);
		
		return ss;
	}
	
	public static GuiSection supplies(GETTER<WArmy> army){
		GuiSection s = new GuiSection();
		
		int i = 0;
		
		for (ArmySupply q : RESOURCES.SUP().ALL()) {
			ADSupply su = AD.supplies().get(q);
			RENDEROBJ g = supply(army, su, false);
			
			s.add(g, (i%4)*(g.body().width()+16), (i/4)*(g.body().height()+4));
			i++;
		}
		

		for (EquipBattle q : STATS.EQUIP().BATTLE_ALL()) {
			ADSupply su = AD.supplies().get(q);
			RENDEROBJ g = supply(army, su, true);
			
			s.add(g, (i%4)*(g.body().width()+16), (i/4)*(g.body().height()+4));
			i++;
			
		}
		
		RECTANGLE ee = s.getLast();
		
		s.add(new GStat() {
		
			@Override
			public void update(GText text) {
				GFORMAT.i(text, AD.supplies().credits().get(army.get()));
			}
			
			
		}.hh(SPRITES.icons().s.money).hoverInfoSet(¤¤CreditsD), (i%4)*(ee.width()+16), (i/4)*(ee.height()+4));
		i++;
		
		return s;
	}
	
	private static RENDEROBJ supply(GETTER<WArmy> army, ADSupply su, boolean equipment) {
		
		int w = 60;
		int h = 14;
		
		SPRITE s = new SPRITE.Imp(w, h) {
			
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				if (su.max(army.get()) == 0) {
					GMeter.render(r, GMeter.C_GREEN_DARK, 0, X1, X2, Y1, Y2);
					return ;
				}
				
				double now = (double)su.used().get(army.get())/su.max(army.get());
				double needed = (double)su.current().get(army.get())/su.max(army.get());
				
				if (su.current().get(army.get()) >= su.used().get(army.get()))
					GMeter.render(r, equipment ? GMeter.C_BLUE : GMeter.C_REDGREEN, needed, X1, X2, Y1, Y2);
				else
					GMeter.render(r, equipment ? GMeter.C_GRAY : GMeter.C_REDORANGE, needed, X1, X2, Y1, Y2);
				
				X1 += 3 + now*(X2-X1);
				
				GCOLOR.UI().border().render(r, X1-1, X1+1, Y1, Y2);
				

			}
		};

		
		RENDEROBJ o = new GHeader.HeaderHorizontal(su.res.icon(), s) {
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				b.title(su.res.name);
				b.text(equipment ? ¤¤EquipmentD : ¤¤SupplyD);
				
				b.sep();
				
				int m = 6;
				
				b.textL(DicRes.¤¤Stored);
				b.tab(m);
				b.add(GFORMAT.i(b.text(), su.current().get(army.get())));
				b.NL();
				
				b.textL(DicMisc.¤¤Minimum);
				b.tab(m);
				b.add(GFORMAT.i(b.text(), su.used().get(army.get())));
				b.NL();
				
				b.textL(DicMisc.¤¤Max);
				b.tab(m);
				b.add(GFORMAT.i(b.text(), su.max(army.get())));
				b.NL();
				
				b.textL(¤¤ConsumtionRate);
				b.tab(m);
				b.add(GFORMAT.f0(b.text(), -su.used().get(army.get())*su.usedPerDay*TIME.years().bitConversion(TIME.days())));
				GText t = b.text();
				t.add(¤¤ConsumtionPerMan);
				t.insert(0, su.usedPerDay*TIME.years().bitConversion(TIME.days()), 1);
				b.add(t);
				b.NL();
				
				if (!equipment) {
					b.NL(8);
					
					b.textL(DicArmy.¤¤Morale);
					b.tab(m);
					b.add(GFORMAT.f1(b.text(), su.morale(army.get())));
					t = b.text();
					t.add(smorale);
					t.insert(0, 1-su.morale, 1);
					b.add(t);
					b.NL();
					
					b.textL(DicMisc.¤¤Health);
					b.tab(m);
					b.add(GFORMAT.f1(b.text(), su.health(army.get())));
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
							su.current().set(army.get(), (int) (su.max(army.get())*d));
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
	
}
