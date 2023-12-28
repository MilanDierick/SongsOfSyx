package view.sett.ui.bottom;

import game.faction.FACTIONS;
import init.C;
import init.D;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import settlement.main.SETT;
import settlement.room.main.*;
import settlement.room.main.copy.SavedPrints.SavedPrint;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.STRING_RECIEVER;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.SPRITE;
import util.data.GETTER;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.gui.panel.GPanel;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import view.main.VIEW;
import view.tool.*;

class UISavedRooms extends GuiSection{

	private static CharSequence ¤¤not = "¤A room must be selected, that can be furnished.";
	private static CharSequence ¤¤save = "¤Save room blueprint";
	private static CharSequence ¤¤name = "¤Saved room blueprints";
	private static CharSequence ¤¤saved = "¤Saved!";
	
	static {
		D.ts(UISavedRooms.class);
	}
	
	UISavedRooms(){
		
		add(new GHeader(¤¤name));
		Placer placer = new Placer();
		
		addRightC(48, new GButt.ButtPanel(SPRITES.icons().m.crossair) {
			
			@Override
			protected void clickA() {
				VIEW.s().tools.place(placer, placer.config);
				super.clickA();
			}
			
		}.hoverInfoSet(¤¤save));
		
		
		GTableBuilder sc = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				return SETT.ROOMS().copy.prints.all().size();
			}
		};
		
		sc.column(null, new Row(null).body().width(), new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new Row(ier);
			}
		});
		
		addRelBody(8, DIR.S, sc.create(12, false));
		
		
		
	}
	
	static class Row extends GButt.BSection implements STRING_RECIEVER{
		
		private final GETTER<Integer> ier;
		
		Row(GETTER<Integer> ier){
			this.ier = ier;
			
			add(new RENDEROBJ.Sprite(Icon.S) {
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					setSprite(p().blue.iconBig().small);
					super.render(r, ds);
				};
				
			});
			
			addRightC(8, new GStat() {
				
				@Override
				public void update(GText text) {
					text.clear().add(p().name, 32);
				}
			});
			
			addRightC(230, new GStat() {
				
				@Override
				public void update(GText text) {
					text.add(p().width).add('x').add(p().height);
				}
			});
			
			addRightC(64, new GButt.Glow(SPRITES.icons().s.admin) {
				@Override
				protected void clickA() {
					VIEW.inters().input.requestInput(Row.this, DicMisc.¤¤rename);
					super.clickA();
				} 
			}.hoverInfoSet(DicMisc.¤¤rename));
			
			addRightC(8, new GButt.Glow(SPRITES.icons().s.cancel) {
				@Override
				protected void clickA() {
					SETT.ROOMS().copy.prints.remove(p());
				}
				
				@Override
				protected void renAction() {
					activeSet(true);
				}
				
			}.hoverInfoSet(DicMisc.¤¤delete));
			
			
			pad(4, 2);
		}
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			super.render(r, ds);
			//activeSet(FACTIONS.player().locks.unlockText(p().blue) == null);
		}
		
		private SavedPrint p() {
			if (ier.get() >= SETT.ROOMS().copy.prints.all().size())
				return null;
			return SETT.ROOMS().copy.prints.all().get(ier.get());
		}
		
		@Override
		protected void clickA() {
			if (!p().blue.reqs.passes(FACTIONS.player()))
				return;
			
			SETT.ROOMS().copy.savedPlacer.place(p());
			VIEW.inters().popup.close();
		}

		@Override
		public void acceptString(CharSequence string) {
			SavedPrint p = p();
			if (p != null && string != null && string.length() > 0)
				SETT.ROOMS().copy.prints.rename(p, string);
			
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			if (!isHoveringAHoverElement()) {
				p().blue.reqs.hover(text, FACTIONS.player());
			}
				
		}
		
		
		
	}
	
	class Placer extends PlacableSingle{

		RoomInstance last;
		private double timer;
		
		RENDEROBJ rr = new RENDEROBJ.RenderImp(64, 24) {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				
				
				if (last != null) {
					last.blueprintI().iconBig().render(r, body.x1(), body.x1()+24, body.y1(), body.y1()+24);
					timer +=ds;
					if(timer < 2)
						COLOR.WHITE202WHITE100.bind();
					UI.FONT().H2.render(r, ¤¤saved, body.x1()+32, body.y1());
				}
			}
		};
		
		final ToolConfig config = new ToolConfig() {
		
			final GPanel panel = new GPanel();
			@Override
			public void addUI(LISTE<RENDEROBJ> uis) {
				panel.setTitle(¤¤save);
				panel.inner().set(rr);
				panel.body().moveY1(80);
				panel.body().centerX(C.DIM());
				rr.body().centerIn(panel.inner());
				
				uis.add(panel);

				uis.add(rr);
			}
			
			@Override
			public void activateAction() {
				last = null;
			};
			
			
		};
		
		public Placer() {
			super(¤¤save);
		}

		@Override
		public CharSequence isPlacable(int tx, int ty) {
			Room r = SETT.ROOMS().map.get(tx, ty);
			if (r == null)
				return PlacableMessages.¤¤ROOM_MUST;
			if (!(r instanceof RoomInstance))
				return ¤¤not;
				
			if (r.blueprint() instanceof RoomBlueprintImp) {
				RoomBlueprintImp b = (RoomBlueprintImp) r.blueprint();
				if (b.constructor() == null || !b.constructor().usesArea()) {
					return ¤¤not;
				}
				return null;
			}
			return ¤¤not;
		}

		@Override
		public void placeFirst(int tx, int ty) {
			Room r = SETT.ROOMS().map.get(tx, ty);
			SETT.ROOMS().copy.prints.push((RoomInstance) r);
			last = (RoomInstance) r;
			timer = 0;
		}
		
		@Override
		public SPRITE getIcon() {
			return SPRITES.icons().m.crossair;
		}
		
		@Override
		public boolean expandsTo(int fromX, int fromY, int toX, int toY) {
			return isPlacable(toX, toY) == null && SETT.ROOMS().map.get(fromX, fromY).isSame(fromX, fromY, toX, toY); 
		}
		
	
	}
	
}
