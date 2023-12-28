


package settlement.room.infra.transport;

import init.D;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.SPRITES;
import settlement.main.SETT;
import settlement.room.main.Room;
import settlement.room.main.RoomInstance;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import util.data.GETTER;
import util.gui.common.UIPickerRes;
import util.gui.misc.*;
import util.info.GFORMAT;
import view.main.VIEW;
import view.sett.ui.room.UIRoomModule.UIRoomModuleImp;
import view.tool.PLACABLE;
import view.tool.PlacableSingle;

class Gui extends UIRoomModuleImp<TransportInstance, ROOM_TRANSPORT> {

	private static CharSequence ¤¤TransportTo = "¤Destination";
	private static CharSequence ¤¤TransportToD = "¤The Transport Target room this transport is transporting to.";
	
	private static CharSequence ¤¤TransportToSet = "¤Set Destination";
	private static CharSequence ¤¤TransportToSetD = "¤Set the Transport Target room this transport will transport to. Must be a warehouse, or a hauler.";
	private static CharSequence ¤¤NotSet = "¤Not Set!";
	private static CharSequence ¤¤NotSetD = "¤Destination not set or invalid!";
	private static CharSequence ¤¤NotSpace = "¤The destination does not have the capacity to receive a transport.";
	
	Gui(ROOM_TRANSPORT s) {
		super(s);
	}

	@Override
	protected void appendPanel(GuiSection section, GGrid grid, GETTER<TransportInstance> g, int x1, int y1) {

		D.t(this);
		GuiSection s = new GuiSection();
		
		s.hoverInfoSet(¤¤TransportToD);
		s.add(new GHeader(¤¤TransportTo));
		GButt.BSection butt = new GButt.BSection(140, 24) {
			@Override
			protected void clickA() {
				ROOM_DELIVERY_INSTANCE r = g.get().destination();
				if (r != null)
					VIEW.s().getWindow().centererTile.set(((RoomInstance)r).body().cX(), ((RoomInstance)r).body().cY());
			};
		};
		
		RENDEROBJ rr = new GStat() {
			
			@Override
			public void update(GText text) {
				ROOM_DELIVERY_INSTANCE r = g.get().destination();
				if (r == null){
					text.add(¤¤NotSet);
					text.errorify();
				}else {
					text.normalify();
					text.add(((RoomInstance)r).body().cX()).add(':').add(((RoomInstance)r).body().cY());
				}
			}
		}.r(DIR.N);
		rr.body().centerIn(butt);
		butt.add(rr);

		s.addRightC(20, butt);
		
		PLACABLE p = new PlacableSingle(¤¤TransportToSet) {
			
			@Override
			public void placeFirst(int tx, int ty) {
				
				g.get().destinationSet(tx, ty);
				VIEW.s().tools.place(null);
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty) {
				Room r = SETT.ROOMS().map.get(tx, ty);
				if (r != null && r instanceof ROOM_DELIVERY_INSTANCE)
					return null;
				return E;
			}
			
			@Override
			public boolean expandsTo(int fromX, int fromY, int toX, int toY) {
				return SETT.ROOMS().map.is(fromX, fromY) && SETT.ROOMS().map.get(fromX, fromY).isSame(fromX, fromY, toX, toY);
			}

		};
		
		CLICKABLE pick = new GButt.ButtPanel(SPRITES.icons().m.crossair) {
			@Override
			protected void clickA() {
				VIEW.s().tools.place(p);
			}
			
		}.hoverInfoSet(¤¤TransportToSetD);
		
		s.addRightC(100, pick);
		
		section.addRelBody(8, DIR.S, s);
		
		section.addRelBody(8, DIR.S, new GButt.Checkbox( D.g("fetch")) {
			
			@Override
			protected void clickA() {
				g.get().fetch = !g.get().fetch;
			}
			
			@Override
			protected void renAction() {
				selectedSet(g.get().fetch);
			}
			
		}.hoverInfoSet(D.g("fetchDesc", "Enabled means that not only scattered resources will be fetched, but also resources in warehouses, who have their fetch disabled.")));
		
		
		section.addRelBody(8, DIR.S, new GHeader(Cart.¤¤Transporting));
		
		
		
		section.addRelBody(2, DIR.S, new UIPickerRes() {
			
			@Override
			protected void select(RESOURCE r, int li) {
				g.get().resourceSet(r);
			}
			
			@Override
			protected RESOURCE getResource() {
				return g.get().resource();
			}
		});
		
		section.addRelBody(8, DIR.S, new RENDEROBJ.RenderImp(200, 100) {
			
			private GBox box = new GBox();
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				box.clear();
				hov(box, g.get());
				box.renderWithout(r, body().cX()-box.width()/2, body.y1());
			}
		});
		
	}
	
	@Override
	protected void hover(GBox box, TransportInstance i) {
		super.hover(box, i);
		hov(box, i);
	}
	
	private void hov(GBox box, TransportInstance i) {
		if (i.resource() == null)
			return;
		
		int livestock = 0;
		int tended = 0;
		int jobs = 0;
		
		int am = 0;
		int tot = 0;
		int away = 0;
		
		for (COORDINATE c : i.body()) {
			
			if (i.is(c) && blueprint.cart.getJob(c.x(), c.y()) != null) {
				jobs ++;
				if (blueprint.cart.job.tending.getD() == 1.0)
					tended ++;
				
				livestock += blueprint.cart.job.wlivestock.get() > 0 ? 1 :0;
			}else if(i.is(c) && blueprint.cart.getStorage(c.x(), c.y()) != null) {
				if (blueprint.cart.storage.saway.get() == 1)
					away++;
				tot += blueprint.cart.storage.samount.max();
				am += blueprint.cart.storage.samount.get(); 
			}
		}
		
		box.textL(Cart.¤¤organise);
		box.tab(5).add(GFORMAT.iofkInv(box.text(), tended, jobs));
		box.NL();
		box.textL(RESOURCES.LIVESTOCK().names);
		box.tab(5).add(GFORMAT.iofkInv(box.text(), livestock, jobs));
		box.NL();
		box.textL(Cart.¤¤Transporting);
		box.tab(5).add(GFORMAT.iofkInv(box.text(), away, jobs));
		box.NL();
		
		if (i.resource() == null)
			return;
		
		box.add(i.resource().icon());
		box.tab(5).add(GFORMAT.iofkInv(box.text(), am, tot));
		box.NL();
	}
	
	@Override
	protected void problem(TransportInstance i, GBox box) {
		
		
		if (i.destination() == null)
			box.error(¤¤NotSetD);
		else if (i.searchedAllDests)
			box.error(¤¤NotSpace);
		else if (i.resource() != null && i.destination() != null && i.destination().getDeliveryCrate(i.resource().bit, CLAMP.i(32, 0, i.destination().deliverCapacity())) == null)
			box.error(¤¤NotSpace);
		super.problem(i, box);
	}

}
