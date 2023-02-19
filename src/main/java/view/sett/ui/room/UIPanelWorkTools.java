package view.sett.ui.room;

import init.C;
import init.boostable.BOOSTABLES;
import init.sprite.SPRITES;
import settlement.main.SETT;
import settlement.room.main.*;
import settlement.room.main.category.RoomCategories.RoomCategoryMain;
import settlement.stats.StatsEquippables.StatEquippableWork;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.data.INT.INTE;
import util.gui.misc.*;
import util.gui.slider.GAllocator;
import util.info.GFORMAT;
import view.interrupter.ISidePanel;
import view.sett.ui.room.UIPanelUtil.RoomRow;

final class UIPanelWorkTools extends ISidePanel {
	
	
	public UIPanelWorkTools(StatEquippableWork work) {
		
		section.add(new GStat() {

			@Override
			public void update(GText text) {
				int a = SETT.ROOMS().STOCKPILE.tally().amountTotal(work.resource);
				a += work.stat().data().get(null);
				
				int am = 0;
				for (RoomEmployment p : SETT.ROOMS().employment.ALL()) {
					if (work.target(p) > 0) {
						am += work.target(p)*p.target.get();
					}
				}
				GFORMAT.iofkInv(text, a, am);
			}
		}.increase().r(DIR.N));
		
		RENDEROBJ q = new UIPanelUtil.BlueprintList(HEIGHT - section.body().y2() - C.SG * 16) {
			
			@Override
			RENDEROBJ row(RoomBlueprintIns<?> bb) {
				if (bb.employment() == null || work.max(bb.employment()) <= 0) {
					return null;
				}
				
				RoomRow r = new RoomRow(bb) {
					@Override
					public void hoverInfoGet(snake2d.util.gui.GUI_BOX text) {
						super.hoverInfoGet(text);
						GBox b = (GBox) text;
						b.NL(8);
						b.textL(BOOSTABLES.INFO().name);
						b.add(GFORMAT.f0(b.text(), work. maxBoost(bb.employment())/work.max(bb.employment())));
					};
				};
				
				r.addRelBody(8, DIR.E, new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.i(text, bb.employment().neededWorkers()*work.target(bb.employment()));
					}
				});
				
				INTE in = new INTE() {
					
					@Override
					public int min() {
						return 0;
					}
					
					@Override
					public int max() {
						return work.max(bb.employment());
					}
					
					@Override
					public int get() {
						return work.target(bb.employment());
					}
					
					@Override
					public void set(int t) {
						work.targetSet(bb.employment(), t);
					}
				};
				
				r.addRelBody(48, DIR.E, new GAllocator(COLOR.ORANGE100.makeSaturated(0.7), in, 6, 16));
				r.body().incrW(420-r.body().width());
				r.pad(6, 0);
				return r;
			}
			
			@Override
			protected void addToCat(GuiSection s, RoomCategoryMain cat) {
				
				
				RENDEROBJ sss = new GStat() {

					@Override
					public void update(GText text) {
						int needed = 0;
						for (RoomBlueprintImp b : cat.all()) {
							if (b instanceof RoomBlueprintIns<?>) {
								RoomBlueprintIns<?> bb = (RoomBlueprintIns<?>) b;
								if (bb.employment() != null && work.target(bb.employment()) > 0) {
									needed += bb.employment().neededWorkers()*work.target(bb.employment());
								}
							}
						}
						GFORMAT.i(text, needed);
					}
				}.r(DIR.N);
				sss.body().centerX(s);
				sss.body().moveY1(s.getLastY2());
				s.add(sss);
				
				RENDEROBJ r = new GButt.Glow(SPRITES.icons().s.magnifier) {
					
					@Override
					protected void clickA() {
						for (int bi = 0; bi < cat.all().size(); bi++) {
							RoomBlueprintImp b = cat.all().get(bi);
							if (b instanceof RoomBlueprintIns<?>) {
								
								RoomBlueprintIns<?> bb = (RoomBlueprintIns<?>) b;
								if (bb.employment() != null) {
									work.targetSet(bb.employment(), work.target(bb.employment())+1);
								}
							}
						}
					}
					
				}.hoverInfoSet(UIPanelWorkPrio.¤¤Adjust);
				
				r.body().moveX1(s.body().x2()+4);
				r.body().moveCY(s.body().cY()-16);
				s.add(r);
				
				r = new GButt.Glow(SPRITES.icons().s.minifier) {
					
					@Override
					protected void clickA() {
						for (int bi = 0; bi < cat.all().size(); bi++) {
							RoomBlueprintImp b = cat.all().get(bi);
							if (b instanceof RoomBlueprintIns<?>) {
								
								RoomBlueprintIns<?> bb = (RoomBlueprintIns<?>) b;
								if (bb.employment() != null) {
									work.targetSet(bb.employment(), work.target(bb.employment())-1);
								}
							}
						}
					}
					
				}.hoverInfoSet(UIPanelWorkPrio.¤¤Adjust);
				
				s.addDownC(8, r);
			}
		};
		
		section.addRelBody(8, DIR.S, q);
		
		
		titleSet(work.resource.names);
	}

}
