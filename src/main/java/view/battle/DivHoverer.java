package view.battle;

import init.D;
import init.boostable.*;
import init.config.Config;
import init.sprite.ICON;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.army.Div;
import settlement.army.DivMorale;
import settlement.army.DivMorale.DivMoraleFactor;
import settlement.army.order.DivTDataTask;
import settlement.stats.STAT;
import settlement.stats.STATS;
import settlement.stats.StatsEquippables.EQUIPPABLE_MILITARY;
import settlement.stats.StatsEquippables.StatEquippableRange;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.OPACITY;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import util.dic.DicArmy;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.info.GFORMAT;

public final class DivHoverer {


	private static CharSequence ¤¤Standing = "¤Standing";
	private static CharSequence ¤¤Moving = "¤Moving";
	private static CharSequence ¤¤Engaging = "¤Engaging";
	private static CharSequence ¤¤Firing = "¤Firing";
	private static CharSequence ¤¤Building = "¤Attacking Structure";
	private static CharSequence ¤¤Charging = "¤Charging";
	private static final DivTDataTask task = new DivTDataTask();
	
	
	static {
		D.ts(DivHoverer.class);
	}
	
	private final DivInfo info = new DivInfo();
	private final DivInfo tmp = new DivInfo();

	private final SPRITE hov = new SPRITE.Imp(info.body().width(), info.body().height()) {
		
		@Override
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
			info.body().moveX1Y1(X1, Y1);
			info.render(r, 0);
		}
	};
	
	public GuiSection get(Div div) {
		tmp.div = div;
		return tmp;
	}

	public void hoverInfo(GBox text, Div div) {
		info.div = div;
		text.add(hov);

	}

	static class DivInfo extends GuiSection {

		public Div div;

		public DivInfo() {



			add(new GStat() {

				@Override
				public void update(GText text) {
					
					text.add(div.menNrOf()).add('/').add(Config.BATTLE.MEN_PER_DIVISION);
				}
			}, 0, 0);

			addRightCAbs(64, new GStat() {

				@Override
				public void update(GText text) {
					text.lablifySub().add(div.info.race().info.names);
				}
			});

			addRightCAbs(180, new GStat() {

				@Override
				public void update(GText text) {
					div.order().task.get(task);
					switch (task.task()) {
					case ATTACK_MELEE:
						text.add(¤¤Engaging);
						break;
					case MOVE:
						text.add(¤¤Moving);
						break;
					case STOP:
						text.add(¤¤Standing);
						break;
					case ATTACK_RANGED:
						text.add(¤¤Firing);
						break;
					case FIGHTING:
						text.add(¤¤Engaging);
						break;
					case ATTACK_BUILDING:
						text.add(¤¤Building);
						break;
					case CHARGE:
						text.add(¤¤Charging);
						break;
					}

				}
			});
			
			add(new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.f0(text, STATS.BATTLE_BONUS().power(div));
				}
			}.hh(DicArmy.¤¤Power, DicArmy.¤¤PowerD), 0, getLastY2()+2);

			{
				int y1 = getLastY2() + 4;
				int i = 0;
				for (BOOSTABLE b : BOOSTABLES.military()) {
					add(new HOVERABLE.HoverableAbs(180, 24) {
						
						@Override
						protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
							double max = BOOSTABLES.player().max(b);
							double dd = b.get(div) / max;
							b.icon().renderC(r, body().x1()+8, body().cY());
							GMeter.render(r, GMeter.C_ORANGE, dd, body().x1()+20, body().x2(), body().cY()-10, body().cY()+10);
							Str.TMP.clear().add(b.name);
							UI.FONT().S.render(r, b.name, body().x1()+25, body().cY()-UI.FONT().S.height()/2);
						}

						@Override
						public void hoverInfoGet(GUI_BOX text) {
							text.title(b.name);
							text.text(b.desc);
							text.NL(8);
							BoostHoverer.hover(text, b, div);
							
						}

					}, 190 * (i % 3), y1 + 24 * (i / 3));
					i++;
				}
			}

			GuiSection ss = new GuiSection() {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					visableSet(div.menNrOf() > 0);
					if (!visableIs())
						return;
					super.render(r, ds);
				}
				
			};

			{
				int i = 0;
				for (EQUIPPABLE_MILITARY mm : STATS.EQUIP().military_all()) {
					
					ss.add(new GStat() {
						
						@Override
						public void update(GText text) {
							GFORMAT.f(text, mm.stat().div().getD(div) * mm.max());
						}
						
						@Override
						public void hoverInfoGet(GBox b) {
							mm.hover(b, div);
							if (mm instanceof StatEquippableRange) {
								StatEquippableRange s = (StatEquippableRange) mm;
								b.NL(8);
								
								b.textLL(s.ammunition.info().name);
								b.add(GFORMAT.f(b.text(), s.ammunition.div().getD(div)*s.max()));
							}
						};
						
					}.hh(mm.resource().icon()), (i%4)*120, (i/4)*26);
					if (mm instanceof StatEquippableRange) {
						ss.add(new SPRITE.Imp(24, 6) {
							
							StatEquippableRange s = (StatEquippableRange) mm;
							
							@Override
							public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
								
								
								GMeter.render(r, GMeter.C_ORANGE, s.ammunition.div().getD(div), X1, X2, Y1, Y2);
								
								if (s.ammunition.div().getD(div) <= 0) {
									OPACITY.O25TO100.bind();
									SPRITES.icons().s.clock.render(r, X2-ICON.SMALL.SIZE, Y2-ICON.SMALL.SIZE);
									OPACITY.unbind();
								}
							}
						}, (i%4)*120+32, 18+(i/4)*26);
						
					}
					
					i++;
				}
				

			}

			{
				int i = 0;
				int y1 = ss.getLastY2()+4;
				for (STAT s : STATS.BATTLE_BONUS().stats()) {
					ss.add(new GStat() {
						
						@Override
						public void update(GText text) {
							GFORMAT.perc(text, s.div().getD(div));
						}
						
						@Override
						public void hoverInfoGet(GBox b) {
							b.add(s.info());
						};
						
					}.hh(s.info().name), (i%2)*240, y1+(i/2)*18);
					i++;
				}
			}

			{
				
				ss.add(new GHeader(DicArmy.¤¤Morale), 0, ss.body().y2()+4);
				ss.addRightC(16, new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.perc(text, div.morale.get());
					}
				});
				ss.addRightCAbs(64, SPRITES.icons().m.arrow_right);
				ss.addRightC(16, new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.perc(text, div.morale.target());
					}
				});
				
				
				ss.add(new GText(UI.FONT().S, DicMisc.¤¤Multipliers).lablifySub(), 0,  ss.getLastY2()+2);
				
				int y1 = ss.getLastY2()+2;
				ss.add(new GStat() {
					
					@Override
					public void update(GText text) {

						GFORMAT.f0(text, DivMorale.BASE.getFactor(div));
					}
				}.hh(DivMorale.BASE.info()), 0, y1);
				
				
				int i = 1;
				for (DivMoraleFactor s : DivMorale.factors) {
					ss.add(new GStat() {
						
						@Override
						public void update(GText text) {
							GFORMAT.f1(text, s.getFactor(div));
						}
						
						@Override
						public void hoverInfoGet(GBox b) {
							b.add(s.info());
						};
						
					}.hh(s.info().name, 160), (i%2)*240, y1+(i/2)*18);
					i++;
				}
				
				ss.add(new GStat() {
					
					@Override
					public void update(GText text) {

						GFORMAT.f0(text, -DivMorale.SITUATION.getFactor(div));
					}
				}.hh(DivMorale.SITUATION.info()), 0, ss.getLastY2()+2);
				

			}
			ss.body().setWidth(240*2);
			
			add(ss, 0, getLastY2()+2);
			
			add(new GStat() {

				@Override
				public void update(GText text) {
					text.lablify().add(div.info.name());
				}
			}.r(), 0, -20);
		}

	}

}
