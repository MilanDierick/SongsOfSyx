package settlement.room.service.market;

import init.D;
import init.race.RACES;
import init.race.Race;
import init.race.RaceResources.RaceResource;
import settlement.entity.humanoid.HCLASS;
import settlement.room.home.HOMET;
import settlement.stats.STATS;
import settlement.stats.equip.*;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.data.GETTER;
import util.dic.DicMisc;
import util.dic.DicRes;
import util.gui.misc.*;
import util.info.GFORMAT;
import view.sett.ui.room.UIRoomModule.UIRoomModuleImp;

class Gui extends UIRoomModuleImp<MarketInstance, ROOM_MARKET> {
	
	private final CharSequence ¤¤Food = "¤Wares";
	private final CharSequence ¤¤Amount = "¤Amount";
	private final CharSequence ¤¤Incoming = "¤Incoming";
	
	Gui(ROOM_MARKET s) {
		super(s);
		D.t(this);
	}

	@Override
	protected void appendPanel(GuiSection section, GGrid grid, GETTER<MarketInstance> g, int x1, int y1) {
		
		GuiSection s = new GuiSection();
		int i = 0;
		for (RaceResource e : RACES.res().ALL) {
			
			GButt.BSection ss = new GButt.BSection() {
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					b.title(e.res.name);
					b.textLL(¤¤Amount).add(GFORMAT.iofkInv(b.text(), g.get().amount(e), g.get().maxAmount));
					b.NL();
					b.textLL(¤¤Incoming).add(GFORMAT.i(b.text(), g.get().jobReserved(e)));
					b.NL();
					
					b.sep();
					
					for (EquipCivic ee : STATS.EQUIP().civics()) {
						if (ee.resource == e.res) {
							b.textL(DicMisc.¤¤Equipped);
							b.NL();
							for (Race r : RACES.all()) {
								for (HCLASS cl : HCLASS.ALL) {
									if (ee.target(cl, r) > 0 && STATS.POP().POP.data(cl).get(r) > 0) {
										if (HOMET.get(cl, r) != null)
											b.add(HOMET.get(cl, r).icon);
									}
								}
								
							}
							b.NL();
						}
					}
					
					
					b.textL(STATS.HOME().materials.info().name);
					b.NL();
					for (Race r : RACES.all()) {
						for (HCLASS cl : HCLASS.ALL) {
							if (r.home().clas(cl).amount(e.res) > 0 && STATS.POP().POP.data(cl).get(r) > 0) {
								if (HOMET.get(cl, r) != null)
									b.add(HOMET.get(cl, r).icon);
							}
						}
					}
					
				}
				
				@Override
				protected void renAction() {
					selectedSet(g.get().uses(e));
				}
				
				@Override
				protected void clickA() {
					g.get().usesToggle(e);
				}
			};
			
			
			ss.addRightC(4, e.res.icon());
			
			ss.addRightC(4, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.i(text, g.get().amount(e));
				}
				
			});
			
			ss.body().incrW(48);
			ss.pad(4);
			
			s.add(ss, (i%3)*ss.body().width(), (i/3)*ss.body().height());
			i++;
			
		}
		
		s.addRelBody(2, DIR.N, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, g.get().amountTotal());
				
			}
		}.hh(¤¤Food));
		
		section.addRelBody(8, DIR.S, s);
		
	}
	
	@Override
	protected void hover(GBox b, MarketInstance i) {
		b.NL();
		b.textLL(¤¤Food).add(GFORMAT.i(b.text(), i.amountTotal()));	
		b.NL();
	}

	@Override
	protected void appendMain(GGrid gg, GGrid text, GuiSection sExtra) {
		GuiSection s = new GuiSection();
		int i = 0;
		int m = 6;
		
		for (RaceResource e : RACES.res().ALL) {
			RENDEROBJ r = new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.i(text, blueprint.amount(e.res));
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					b.title(e.res.name);
					b.textLL(DicRes.¤¤Stored).add(GFORMAT.i(b.text(), blueprint.amount(e.res)));
					b.NL();

					b.sep();
					
					
					
					for (EquipCivic ee : STATS.EQUIP().civics()) {
						if (ee.resource == e.res) {
							b.textL(DicMisc.¤¤Equipped);
							b.NL();
							for (Race r : RACES.all()) {
								for (HCLASS cl : HCLASS.ALL) {
									if (ee.target(cl, r) > 0 && STATS.POP().POP.data(cl).get(r) > 0) {
										if (HOMET.get(cl, r) != null)
											b.add(HOMET.get(cl, r).icon);
									}
								}
								
							}
							b.NL();
						}
					}
					
					
					b.textL(STATS.HOME().materials.info().name);
					b.NL();
					for (Race r : RACES.all()) {
						for (HCLASS cl : HCLASS.ALL) {
							if (r.home().clas(cl).amount(e.res) > 0 && STATS.POP().POP.data(cl).get(r) > 0) {
								if (HOMET.get(cl, r) != null)
									b.add(HOMET.get(cl, r).icon);
							}
						}
					}
					
					
//					for (Race r : RACES.all()) {
//						for (HCLASS cl : HCLASS.ALL) {
//							if (HOMET.get(cl, r) == null)
//								continue;
//							SPRITE icon = b.add(HOMET.get(cl, r).icon);
//							
//							for (RES_AMOUNT a : r.home().clas(cl).resources()) {
//								if (a.resource() == e.res) {
//									b.add(icon);
//									int ta = STATS.HOME().target(cl, r, a.resource());
//									int ma = STATS.HOME().max(cl, r, a.resource());
//									b.add(GFORMAT.iofkInv(b.text(), ta, ma));
//									b.NL();
//								}
//							}
//							
//							for (EquipCivic e : STATS.EQUIP().civics()){
//								b.add(icon);
//								b.add(GFORMAT.iofkInv(b.text(), e.target(cl, r), e.max()));
//								b.NL();
//							}
//							
//						}
//					}
					
					
					
					
					
				};
			}.hv(e.res.icon());
			
			s.add(r, (i%m)*42, (i/m)*48);
			i++;
			
		}
		
		s.add(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, blueprint.total);
				
			}
		}.hh(¤¤Food), 0, s.body().y1()-16);
		
		text.add(s);

		
	}
	
	

}
