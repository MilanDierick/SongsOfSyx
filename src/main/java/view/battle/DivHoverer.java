package view.battle;

import init.D;
import init.config.Config;
import settlement.army.Div;
import settlement.army.order.DivTDataTask;
import settlement.stats.STATS;
import settlement.stats.equip.EquipBattle;
import settlement.stats.equip.EquipRange;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.*;
import util.info.GFORMAT;
import view.ui.battle.UIDivStats;

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
		private final UIDivStats.DivStats stat = new UIDivStats.DivStats();
		
		public DivInfo() {

			add(new GStat() {

				@Override
				public void update(GText text) {
					text.lablify().add(div.info.name());
				}
			}.r(), 0, 0);

			addDown(2, new GStat() {

				@Override
				public void update(GText text) {
					
					text.add(div.menNrOf()).add('/').add(Config.BATTLE.MEN_PER_DIVISION);
				}
			});

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
			body().incrW(100);
			

			{
				GuiSection ss = new GuiSection();
				int i = 0;
				for (EquipBattle mm : STATS.EQUIP().BATTLE_ALL()) {
					
//					if (mm.target(div) == 0)
//						continue;
					if (mm instanceof EquipRange)
						continue;
					ss.add(new GStat() {
						
						@Override
						public void update(GText text) {
							GFORMAT.f(text, mm.stat().div().getD(div) * mm.max(), 1);
						}
						
						@Override
						public void hoverInfoGet(GBox b) {
							mm.hover(b, div);
						};
						
					}.hh(mm.resource().icon()), (i%4)*120, (i/4)*26);
					
					i++;
				}
				ss.body().incrW(48);
				addRelBody(8, DIR.S, ss);

			}
			{
				int i = 0;
				GuiSection ss = new GuiSection();
				for (EquipRange mm : STATS.EQUIP().RANGED()) {
					
					
					if (!(mm instanceof EquipRange))
						continue;
					
					ss.add(new GStat() {
						
						@Override
						public void update(GText text) {
							GFORMAT.f(text, mm.stat().div().getD(div) * mm.max(), 1);
						}
						
						@Override
						public void hoverInfoGet(GBox b) {
							mm.hover(b, div);
						};
						
					}.hh(mm.resource().icon()), (i%4)*120, (i/4)*26);
					if (mm instanceof EquipRange) {
						ss.add(new SPRITE.Imp(64, 6) {
							
							EquipRange s = (EquipRange) mm;
							
							@Override
							public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
								
								
								GMeter.render(r, GMeter.C_ORANGE, s.ammunition.div().getD(div), X1, X2, Y1, Y2);
							}
						}, (i%4)*120, 22+(i/4)*30);
						
					}
					
					i++;
				}
				ss.body().incrW(48);
				add(ss, getLastX1(), getLastY2()+4);

			}
			
			
			
			addRelBody(8, DIR.S, stat.get(null));
			
			
		}

		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			stat.get(div);
			super.render(r, ds);
		}
		
	}

}
