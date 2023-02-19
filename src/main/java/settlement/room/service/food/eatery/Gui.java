package settlement.room.service.food.eatery;

import init.D;
import init.resources.Edible;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.data.BOOLEAN;
import util.data.GETTER;
import util.gui.misc.*;
import util.info.GFORMAT;
import view.sett.ui.room.UIRoomModule.UIRoomModuleImp;

class Gui extends UIRoomModuleImp<EateryInstance, ROOM_EATERY> {
	
	private final CharSequence ¤¤Food = "¤Food";
	private final CharSequence ¤¤Amount = "¤Amount";
	private final CharSequence ¤¤Incoming = "¤Incoming";
	private final CharSequence ¤¤Consumed = "¤Consumed";
	
	Gui(ROOM_EATERY s) {
		super(s);
		D.t(this);
	}

	@Override
	protected void appendPanel(GuiSection section, GGrid grid, GETTER<EateryInstance> g, int x1, int y1) {
		
		GuiSection s = new GuiSection();
		int i = 0;
		for (Edible e : RESOURCES.EDI().all()) {
			
			GButt.BSection ss = new GButt.BSection() {
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					b.title(e.resource.name);
					b.textLL(¤¤Amount).add(GFORMAT.iofkInv(b.text(), g.get().amount(e), g.get().maxAmount));
					b.NL();
					b.textLL(¤¤Incoming).add(GFORMAT.i(b.text(), g.get().jobReserved(e)));
					b.NL();
					b.textLL(¤¤Consumed).add(GFORMAT.i(b.text(), (int) -blueprint.industry().ins().get(e.index()).year.get(g.get())));
					b.NL();
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
			
			ss.add(UI.PANEL().checkSprite(new BOOLEAN() {
				
				@Override
				public boolean is() {
					return g.get().uses(e);
				}
			}), 0, 0);
			
			ss.addRightC(4, e.resource.icon());
			
			ss.addRightC(4, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.i(text, g.get().amount(e));
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					b.title(e.resource.name);
					b.textLL(¤¤Food).add(GFORMAT.i(b.text(), g.get().amount(e)));					
				};
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
	protected void hover(GBox b, EateryInstance i) {
		b.NL();
		b.textLL(¤¤Food).add(GFORMAT.i(b.text(), i.amountTotal()));	
		b.NL();
	}

	@Override
	protected void appendMain(GGrid gg, GGrid text, GuiSection sExtra) {
		GuiSection s = new GuiSection();
		GChart cc = new GChart();
		int i = 0;
		for (Edible e : RESOURCES.EDI().all()) {
			RENDEROBJ r = new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.i(text, blueprint.amount(e));
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					b.title(e.resource.name);
					b.textLL(¤¤Amount).add(GFORMAT.i(b.text(), blueprint.amount(e)));
					b.NL(8);
					b.textLL(¤¤Consumed).add(GFORMAT.i(b.text(), (int) -blueprint.industry().ins().get(e.index()).history().get()));
					b.NL();
					cc.clear();
					cc.add(blueprint.industry().ins().get(e.index()).history());
					b.add(cc);
					
				};
			}.hv(e.resource.icon());
			
			s.add(r, (i%4)*42, (i/4)*48);
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
