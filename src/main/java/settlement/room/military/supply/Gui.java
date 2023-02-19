package settlement.room.military.supply;

import game.faction.FACTIONS;
import init.D;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.main.SETT;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.misc.CLAMP;
import util.data.GETTER;
import util.data.INT;
import util.dic.DicMisc;
import util.dic.DicRes;
import util.gui.misc.*;
import util.gui.slider.GGaugeMutable;
import util.info.GFORMAT;
import view.sett.ui.room.UIRoomModule.UIRoomModuleImp;
import world.army.WARMYD;
import world.army.WARMYD.WArmySupply;

class Gui extends UIRoomModuleImp<SupplyInstance, ROOM_SUPPLY> {
	
	{D.gInit(this);}
	
	Gui(ROOM_SUPPLY s) {
		super(s);
	}

	@Override
	protected void appendPanel(GuiSection section, GGrid grid, GETTER<SupplyInstance> g, int x1, int y1) {
		
		section.add(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.iofk(text, g.get().amount(), (long) g.get().blueprintI().constructor.storage.get(g.get()));
			}
		}.hv(D.g("stored")), 50, section.getLastY2());
		
		section.addRightC(50, new GButt.CheckboxTitle(D.g("fetch")) {
			
			@Override
			protected void clickA() {
				g.get().fetch(!g.get().fetch());
			}
			
			@Override
			protected void renAction() {
				selectedSet(g.get().fetch());
			}
			
		}.hoverInfoSet(D.g("fetchDesc", "The supply workers will fetch from warehouses normally. Toggle this to also fetch from warehouses that have fetch enabled.")));
		
		section.add(new GHeader(D.g("Fill")).hoverInfoSet(D.g("fillD", "Sets a limit for how much this depot may store")), section.body().x1(), section.body().y2()+4);
		GGaugeMutable slider = new GGaugeMutable(new INT.INTE() {
			
			@Override
			public int min() {
				return 1;
			}
			
			@Override
			public int max() {
				return 10;
			}
			
			@Override
			public int get() {
				return g.get().fill;
			}
			
			@Override
			public void set(int t) {
				g.get().fill = (short) CLAMP.i(t, min(), max());
			}
		}, 180);
		
		section.addRightC(8, slider);
		
		section.addRelBody(8, DIR.S, new GHeader(DicRes.¤¤Supplies).hoverInfoSet(D.g("SuppliesD", "Various supplies needed for the army. That can boost it in various ways.")));
		
		grid = new GGrid(section, 4, section.body().y2());
		
		boolean[] used = new boolean[RESOURCES.ALL().size()];
		
		for (WArmySupply res : WARMYD.supplies().all) {
			RESOURCE r = res.res;
			if (used[r.index()])
				continue;
			used[r.index()] = true;
			GButt b = new GButt.ButtPanel(r.icon()) {
				@Override
				protected void renAction() {
					selectedSet(g.get().resource() == r);
				}
				@Override
				protected void clickA() {
					g.get().setResource(res.res);
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					b.title(r.name);
//					b.text(res.info().desc);
					b.NL(8);
					b.textL(SETT.ROOMS().STOCKPILE.info.name);
					b.add(GFORMAT.iofkInv(b.text(), SETT.ROOMS().STOCKPILE.tally().amountTotal(r), SETT.ROOMS().STOCKPILE.tally().spaceTotal(r)));
					b.NL(4);
					
					b.textL(DicMisc.¤¤Needed);
					b.add(GFORMAT.i(b.text(), res.needed(FACTIONS.player())));
					
				}
			};
			
			b.body.setWidth(80);
			b.hoverInfoSet(r.name);
			grid.add(b);
		}
		
	}


	
	@Override
	protected void hover(GBox box, SupplyInstance i) {
		if (i.resource() != null) {
			box.add(i.resource().icon());
			box.add(GFORMAT.iofk(box.text(), i.amount(), (int)i.blueprintI().constructor.storage.get(i)));
		}
		super.hover(box, i);
	}
	
	
	

}
