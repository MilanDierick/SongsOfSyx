
package settlement.room.infra.inn;

import game.faction.FACTIONS;
import game.faction.FCredits.CTYPE;
import game.tourism.Review;
import init.D;
import settlement.room.main.RoomInstance;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.SPRITE;
import util.data.GETTER;
import util.data.INT.INTE;
import util.dic.*;
import util.gui.misc.*;
import util.gui.slider.GTarget;
import util.gui.table.GStaples;
import util.gui.table.GTableSorter.GTFilter;
import util.gui.table.GTableSorter.GTSort;
import util.info.GFORMAT;
import view.main.VIEW;
import view.sett.ui.room.UIRoomBulkApplier;
import view.sett.ui.room.UIRoomModule.UIRoomModuleImp;

class Gui extends UIRoomModuleImp<InnInstance, ROOM_INN> {

	private static CharSequence ¤¤Guestbook = "Guestbook";
	
	static {
		D.ts(Gui.class);
	}
	
	Gui(ROOM_INN s) {
		super(s);
	}

	@Override
	protected void appendPanel(GuiSection section, GGrid grid, GETTER<InnInstance> g, int x1, int y1) {
		
		GuiSection s = new GuiSection();
		
		s.add(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.iIncr(text, g.get().earnings);
			}
			@Override
			public void hoverInfoGet(GBox b) {
				GText t = b.text();
				DicTime.setYears(t, -1);
				b.add(t);
				b.add(GFORMAT.iIncr(b.text(), g.get().earningsLast));
			};
			
		}.hv(DicRes.¤¤Earnings));
		
		
		section.addRelBody(32, DIR.S, s);
		
		
		INTE in = new INTE() {
			int i = 0;
			@Override
			public int min() {
				return 0;
			}
			
			@Override
			public int max() {
				for (int i = 1; i < g.get().reviews.length; i++) {
					if (!g.get().reviews[i].has())
						return i-1;
				}
				return g.get().reviews.length-1;
			}
			
			@Override
			public int get() {
				return i;
			}
			
			@Override
			public void set(int t) {
				i = t;
			}
		};
		
		section.addRelBody(4, DIR.S, new GHeader(¤¤Guestbook));
		GTarget t = new GTarget(100, (SPRITE)null, false, true, new GStat() {
			
			@Override
			public void update(GText text) {
				if (in.max() == 0)
					GFORMAT.iofk(text, in.get(), in.max());
				else
					GFORMAT.iofk(text, in.get()+1, in.max()+1);
				text.normalify();
			}
		}, in);
		section.addRelBody(4, DIR.S, t);
		
		
		RENDEROBJ o = new RENDEROBJ.RenderImp(350, 500) {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				Review rev = g.get().reviews[in.get()];
				if (rev != null) {
					rev.render(r,  body().x1(), body().y1(), body().width());
				}
			}
		};
		
		section.addRelBody(16, DIR.S, o);

	}

	@Override
	protected void appendMain(GGrid grid, GGrid text, GuiSection sExtra) {
		
		GuiSection s = new GuiSection();
		
		final int am = FACTIONS.player().credits().get(CTYPE.TOURISM).IN.historyRecords();
		GStaples chart = new GStaples(am) {
			
			@Override
			protected void hover(GBox box, int stapleI) {
				int ago = am-1-stapleI;
				GText t = box.text();
				DicTime.setAgo(t, ago*FACTIONS.player().credits().get(CTYPE.TOURISM).IN.time().bitSeconds());
				box.textLL(t);
				box.NL();
				box.add(GFORMAT.iIncr(box.text(), FACTIONS.player().credits().get(CTYPE.TOURISM).IN.get()));
				
			}
			
			@Override
			protected double getValue(int stapleI) {
				return FACTIONS.player().credits().get(CTYPE.TOURISM).IN.get(am-1-stapleI);
			}
		};
		chart.body().setDim(240, 58);
		
		s.add(chart);
		s.addRelBody(4, DIR.N, new GHeader(DicRes.¤¤Earnings));
		
		s.addRelBody(4, DIR.S, new GButt.ButtPanel(DicMisc.¤¤Tourists) {
			
			@Override
			protected void clickA() {
				VIEW.UI().tourists.activate();
			};
			
		}.pad(4, 1));
		
		text.add(s);
		
	}

	@Override
	protected void appendTableButt(GuiSection s, GETTER<RoomInstance> ins) {


	}

	@Override
	protected void hover(GBox box, InnInstance i) {
		InnInstance ii = i;
		
		box.NL();
		box.textLL(DicRes.¤¤Earnings);
		box.add(GFORMAT.iIncr(box.text(), ii.earnings));
		box.NL();
		
		
	}

	@Override
	protected void appendTableFilters(LISTE<GTFilter<RoomInstance>> filters,
			LISTE<GTSort<RoomInstance>> sorts, LISTE<UIRoomBulkApplier> appliers) {
		
	}

}
