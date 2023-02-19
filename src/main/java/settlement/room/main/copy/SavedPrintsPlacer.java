package settlement.room.main.copy;

import init.sprite.SPRITES;
import settlement.main.SETT;
import settlement.room.main.TmpArea;
import settlement.room.main.construction.ConstructionInit;
import settlement.room.main.copy.SavedPrints.SavedPrint;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.furnisher.FurnisherItem;
import settlement.room.main.placement.PLACEMENT;
import settlement.room.main.placement.UtilWallPlacability;
import settlement.room.main.util.RoomState;
import settlement.tilemap.TBuilding;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.*;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.gui.misc.GButt;
import view.main.VIEW;
import view.tool.PlacableFixed;
import view.tool.PlacableFixedImp;

public final class SavedPrintsPlacer {

	private SavedPrint blue;

	private final BSwap swap;
	private TBuilding structure;
	private GuiSection sSelect = new GuiSection();
	private boolean w = true;
	
	private final ArrayList<CLICKABLE> walls = new ArrayList<CLICKABLE>(
		new GButt.Panel(SPRITES.icons().m.wall) {
			
			private String s = "Include walls";
			@Override
			protected void clickA() {
				w = !w;
				
			};
			@Override
			protected void renAction() {
				selectedSet(w);
			};
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.text(s);
			};
		},
		new GButt.Panel(SPRITES.icons().m.wall) {
		
			@Override
			protected void clickA() {
				VIEW.inters().popup.show(sSelect, this);
				
			};
			@Override
			protected void renAction() {
				activeSet(w);
				replaceLabel(structure.iconCombo, DIR.C);
			};
		}
	);
	
	SavedPrintsPlacer(BSwap swap){
		this.swap = swap;
		for (TBuilding b : SETT.TERRAIN().BUILDINGS.all()) {
			sSelect.addDown(0, new GButt.Panel(b.iconCombo) {
				
				@Override
				protected void clickA() {
					structure = b;
					VIEW.inters().popup.close();
				}
				
			});
		}
		
	}
	
	public void place(SavedPrint blue) {
		this.blue = blue;
		structure = blue.structure;
		swap.init(blue.blue);
		VIEW.s().tools.place(nextStep);
	}

	private final PlacableFixed nextStep = new PlacableFixedImp(null, 4, 1) {
		
		private final Coo cTmp = new Coo();
		

		
		@Override
		public void place(final int tx, final int ty, int rrx, int rry) {
			
			TmpArea tmp = SETT.ROOMS().tmpArea(this);
			
			COORDINATE r = getSourceTile(rrx, rry);
			
			int rx = r.x();
			int ry = r.y();
			
			Furnisher furnisher = swap.current().constructor();
			
			if(w && furnisher.mustBeIndoors() && structure != null && !blue.isRoom(rx, ry)) {
				if(blue.isWall(rx, ry) && UtilWallPlacability.wallShouldBuild.is(tx, ty)) {
					UtilWallPlacability.wallBuild(tx, ty, structure);
				}else if(blue.isRoof(rx, ry) && UtilWallPlacability.openingShouldBuild.is(tx, ty)) {
					UtilWallPlacability.openingBuild(tx, ty, structure);
				}
			}
			
			if (rrx != 0 || rry != 0) {
				tmp.clear();
				return;
			}
			
			
			int w = width();
			int h = height();
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					COORDINATE c = getSourceTile(x, y);
					rx = c.x();
					ry = c.y();
					if (!blue.isRoom(rx, ry))
						continue;
					tmp.set(tx+x, ty+y);
				}
			}
			
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					COORDINATE c = getSourceTile(x, y);
					rx = c.x();
					ry = c.y();
					if (!blue.isRoom(rx, ry))
						continue;
					FurnisherItem it = blue.item(rx, ry, furnisher.blue());
					if (it == null)
						continue;
					c = rotate(-it.firstX(), -it.firstY());
					int dx = c.x();
					int dy = c.y();
					
					FurnisherItem it2 = it.group.item(it.variation(), (it.rotation + rot())%it.group.rotations());
					r = getSourceItemOff(it2, x+tx+dx, y+ty+dy);
					int x1 = r.x();
					int y1 = r.y();
					
					
					SETT.ROOMS().fData.itemSet(x1, y1, it2, tmp.room());
				}
			}
			
			ConstructionInit init = new ConstructionInit(0, furnisher, structure, 0, RoomState.DUMMY);
			SETT.ROOMS().construction.createClean(tmp, init);;
		}
		
		@Override
		public CharSequence placable(int tx, int ty, int rx, int ry) {
			COORDINATE c = getSourceTile(rx, ry);
			if (!blue.isRoom(c.x(), c.y()))
				return null;
			Furnisher furnisher = swap.current().constructor();
			CharSequence s = PLACEMENT.placable(tx, ty, furnisher.blue(), true);
			if (s != null)
				return s;
			return furnisher.placable(tx, ty);
		}
		
		@Override
		public int width() {
			int wi = (rot() & 1) == 1 ? blue.height : blue.width;
			return wi;
		}
		
		@Override
		public int height() {
			int h = (rot() & 1) == 0 ? blue.height : blue.width;
			return h;
		}
		
		private COORDINATE getSourceTile(int rx, int ry) {
			switch (rot()) {
			case 0:
				cTmp.set(rx, ry);
				break;
			case 1:
				cTmp.ySet(blue.height-rx-1);
				cTmp.xSet(ry);
				break;
			case 2:
				cTmp.ySet(blue.height-ry-1);
				cTmp.xSet(blue.width-rx-1);
				break;
			case 3:
				cTmp.ySet(rx);
				cTmp.xSet(blue.width-ry-1);
				break;
			default:
				throw new RuntimeException();
			}
			return cTmp;
		}
		
		private COORDINATE getSourceItemOff(FurnisherItem i, int rx, int ry) {
			
			switch (rot()) {
			case 0:
				cTmp.set(rx, ry);
				break;
			case 1:
				cTmp.ySet(ry);
				cTmp.xSet(rx-i.width()+1);
				break;
			case 2:
				cTmp.ySet(ry-i.height()+1);
				cTmp.xSet(rx-i.width()+1);
				break;
			case 3:
				cTmp.ySet(ry-i.height()+1);
				cTmp.xSet(rx);
				break;
			default:
				throw new RuntimeException();
			}
			return cTmp;
		}
		
		private COORDINATE rotate(int rx, int ry) {
			for (int i = 0; i < rot(); i++) {
				int newX = -ry;
				int newY = rx;
				rx = newX;
				ry = newY;
			}
			cTmp.set(rx, ry);
			return cTmp;
		}
		
		@Override
		public void renderPlaceHolder(SPRITE_RENDERER r, int mask, int x, int y, int tx, int ty, int rx, int ry, boolean isPlacable, boolean areaIsPlacable) {
			COORDINATE cr = getSourceTile(rx, ry);
			rx = cr.x();
			ry = cr.y();
			Furnisher furnisher = swap.current().constructor();
			if (blue.isRoom(rx, ry)) {
				
				if (blue.isSoldid(rx, ry))
					SPRITES.cons().BIG.filled.render(r, mask, x, y);
				else
					SPRITES.cons().BIG.dashed.render(r, mask, x, y);
			}else if(w && furnisher.mustBeIndoors() && structure != null) {
				if(blue.isWall(rx, ry) && UtilWallPlacability.wallShouldBuild.is(tx, ty)) {
					SPRITES.cons().BIG.filled.render(r, 0, x, y);
				}else if(blue.isRoof(rx, ry) && UtilWallPlacability.openingShouldBuild.is(tx, ty)) {
					SPRITES.cons().BIG.outline_dashed.render(r, 0, x, y);
				}
			}
		};
		
		@Override
		public LIST<CLICKABLE> getAdditionalButt() {
			Furnisher furnisher = swap.current().constructor();
			if (furnisher.mustBeIndoors()) {
				return swap.wrap(walls);
			}
			return swap.wrap(null);
		}
		
		@Override
		public CharSequence name() {
			Furnisher furnisher = swap.current().constructor();
			return furnisher.blue().info.name;
		}
	};
	
}
