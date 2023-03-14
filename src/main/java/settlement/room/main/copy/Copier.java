package settlement.room.main.copy;

import static settlement.main.SETT.*;

import game.GAME;
import init.C;
import init.D;
import init.sprite.ICON.MEDIUM;
import init.sprite.SPRITES;
import settlement.job.Job;
import settlement.main.SETT;
import settlement.room.main.*;
import settlement.room.main.construction.ConstructionInit;
import settlement.room.main.furnisher.*;
import settlement.room.main.placement.PLACEMENT;
import settlement.room.main.placement.UtilWallPlacability;
import settlement.room.main.util.RoomAreaWrapper;
import settlement.tilemap.TBuilding;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.*;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.*;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import view.main.VIEW;
import view.tool.*;

class Copier extends PlacableSingle{

	private static CharSequence ¤¤name = "¤Room Copier";
	private static CharSequence ¤¤IncludeWalls = "¤Include Walls";
	private static CharSequence ¤¤desc = "¤Copies already planned rooms";
	private static CharSequence ¤¤indoor = "¤This room requires to be built indoors and you must pick a structure type.";

	static {
		D.ts(Copier.class);
	}
	private static RoomAreaWrapper wrap = new RoomAreaWrapper();
	private ROOMA room;
	private TBuilding structure;
	private boolean w = true;
	private final BSwap swap;
	
	
	private final GuiSection buttonsIndoor = new GuiSection();
	
	{
		D.gInit(this);
		
		for (TBuilding t : SETT.TERRAIN().BUILDINGS.all()) {
			
			
			CLICKABLE c = new GButt.Panel(t.iconCombo, t.desc) {
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					b.title(t.name);
					b.text(t.desc);
					b.NL();
					b.setResource(t.resource, t.resAmount);
				}
				
				@Override
				protected void clickA() {
					structure = t;
					VIEW.inters().popup.close();
				}
				
				@Override
				protected void renAction() {
					selectedSet(structure == t);
				}
			};
			buttonsIndoor.addDownC(0, c);
		}
		
	}
	
	private final LIST<CLICKABLE> walls = new ArrayList<CLICKABLE> (
		new GButt.Panel(SPRITES.icons().m.wall) {
			
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
				text.text(¤¤IncludeWalls);
			};
			
		},
		new GButt.Panel(SPRITES.icons().m.cancel) {
			@Override
			protected void clickA() {
				VIEW.inters().popup.show(buttonsIndoor, this);
			}
			@Override
			protected void renAction() {
				replaceLabel(structure == null ? SETT.TERRAIN().BUILDINGS.all().get(0).iconCombo : structure.iconCombo, DIR.C);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.text(¤¤indoor);
			};
			
		}
	);
	
	public Copier(BSwap s) {
		super(¤¤name, ¤¤desc);
		this.swap = s;
	}

	private RoomBlueprintImp pppp;
	
	@Override
	public CharSequence isPlacable(int tx, int ty) {
		
		Room r = SETT.ROOMS().map.get(tx, ty);
		if (r != null && r.constructor() != null && r.constructor().usesArea() && r.constructor().canBeCopied()) {
			if (pppp == null)
				pppp = r.constructor().blue();
			return GAME.player().locks.unlockText(r.blueprint());
		}
		
			
		return E;
	}
	
	@Override
	public void placeInfo(GBox b, int tiles) {
		if (pppp != null)
			b.text(pppp.info.name);
		super.placeInfo(b, tiles);
		pppp = null;
	}

	@Override
	public void placeFirst(int tx, int ty) {
		Room room = ROOMS().map.get(tx, ty);
		wrap.done();
		this.room = wrap.init(room, tx, ty);
		
		structure = SETT.ROOMS().construction.structure(tx, ty); 
		

		nextStep.rotSet(0);
		swap.init(ROOMS().map.get(tx, ty).constructor().blue());
		VIEW.s().tools.place(nextStep, config);
		wrap.done();
	}
	
	@Override
	public boolean expandsTo(int fromX, int fromY, int toX, int toY) {
		return ROOMS().map.get(fromX, fromY) != null && ROOMS().map.get(fromX, fromY).isSame(fromX, fromY, toX, toY);
	}
	
	@Override
	public MEDIUM getIcon() {
		return SPRITES.icons().m.copy;
	}
	
	private final ToolConfig config = new ToolConfig() {
		
		@Override
		public boolean back() {
			VIEW.s().tools.place(Copier.this);
			return false;
		};
		
		@Override
		public void addUI(LISTE<RENDEROBJ> uis) {
			VIEW.s().tools.placer.addStandardButtons(uis, true);
		};
		
	};
	
	private final PlacableFixed nextStep = new PlacableFixedImp(Copier.this.name(), 4, 1) {
		
		private final Coo cTmp = new Coo();
		
		private boolean update() {
			Room r = SETT.ROOMS().map.get(room.mX(), room.mY());
			
			if (r == null)
				return false;
			if (r.constructor() == null)
				return false;
			if (structure == null){
				if (r.constructor().mustBeIndoors()) {
					structure = ConstructionInit.findStructure(room.mX(), room.mY());
				}else {
					structure = null;
				}
			}
			wrap.done();
			room = wrap.init(r, room.mX(), room.mY());
			
			return true;
		}
		
		@Override
		public void place(final int tx, final int ty, int rx, int ry) {
			
			update();
			
			Furnisher furnisher = swap.current().constructor();
			
			if (furnisher.mustBeIndoors() && w) {
				
				COORDINATE c = getSourceTile(rx, ry);
				if (room.is(c)) {
					for (int i = 0; i < DIR.NORTHO.size(); i++) {
						DIR d = DIR.NORTHO.get(i);
						c = getSourceTile(rx+d.x(), ry+d.y());
						if (!room.is(c)) {
						
							
							if (UtilWallPlacability.wallisReal.is(c)) {
								if (UtilWallPlacability.wallShouldBuild.is(tx+d.x(), ty+d.y()))
									UtilWallPlacability.wallBuild(tx+d.x(), ty+d.y(), structure);
							}
							else if(UtilWallPlacability.openingIsReal.is(c)) {
								if (UtilWallPlacability.openingShouldBuild.is(tx+d.x(), ty+d.y()))
									UtilWallPlacability.openingBuild(tx+d.x(), ty+d.y(), structure);
							}
						}
					}
				}
				
			}
			
			if (rx != 0 || ry != 0) {
				return;
			}
			
			TmpArea tmp = SETT.ROOMS().tmpArea(this);
			
			
			int w = width();
			int h = height();
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					COORDINATE c = getSourceTile(x, y);
					if (!room.is(c))
						continue;
					tmp.set(tx+x, ty+y);
				}
			}
			
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {

					COORDINATE c = getSourceTile(x, y);
					int sx = c.x();
					int sy = c.y();
					if (!room.is(sx, sy))
						continue;
					FurnisherItem it = SETT.ROOMS().fData.item.get(sx, sy);
					if (!SETT.ROOMS().fData.isMaster.is(sx, sy))
						continue;
					
					c = getOrigionalDelta(sx, sy);
					int x1 = c.x()+tx;
					int y1 = c.y()+ty;
					
					c = getSourceItemOff(it, it.firstX(), it.firstY());
					x1-= c.x();
					y1 -= c.y();
					
					int rot = (it.rotation+rot()%it.group.rotations());
					rot %= it.group.rotations();		
					it = it.group.item(it.variation(), rot);
					
					SETT.ROOMS().fData.itemSet(x1, y1, it, tmp.room());
				}
			}
			
			Room r = SETT.ROOMS().map.get(room.mX(), room.mY());
			ConstructionInit init = new ConstructionInit(0, furnisher, structure, 0, r.makeState(room.mX(), room.mY()));
			
			SETT.ROOMS().construction.createClean(tmp, init);
			
			return;
			
		}
		
		@Override
		public CharSequence placable(int tx, int ty, int rx, int ry) {
			if (!update()) {
				VIEW.s().tools.place(null);
				return E;
			}
				
			COORDINATE c = getSourceTile(rx, ry);
			if (!room.is(c))
				return null;
			Furnisher furnisher = swap.current().constructor();
			CharSequence s = PLACEMENT.placable(tx, ty, furnisher.blue(), true);
			if (s != null)
				return s;
			return furnisher.placable(tx, ty);
		}
		
//		@Override
//		public void hoverDesc(GBox box) {
//			
//			Furnisher furnisher = swap.current().constructor();
//			int[] am = Deleter.getResources(room, furnisher, SETT.ROOMS().map.get(room.mX(), room.mY()).upgrade());
//			for (int i = 0; i < furnisher.res)
//			
//			super.hoverDesc(box);
//		}
//		
//		private final static int[] amounts = new int[Furnisher.MAX_RESOURCES];
//		
//		@Override
//		public void hoverDesc(GBox box) {
//			if (!update()) {
//				return;
//			}
//			AREA r = room;
//			Furnisher furnisher = swap.current().constructor();
//			int upgrade = SETT.ROOMS().map.get(room.mX(), room.mY()).upgrade();
//			
//			for (int i = 0; i < amounts.length; i++) {
//				amounts[i] = 0;
//			}
//			for (COORDINATE c : r.body()) {
//				
//				if (!r.is(c))
//					continue;
//				
//				if (ROOMS().fData.isMaster.is(c)) {
//					FurnisherItem it = ROOMS().fData.item.get(c);
//					for (int i = 0; i < furnisher.resources(); i++) {
//						amounts[i] += it.cost(i, upgrade);
//					}
//				}
//				
//				if (UtilWallPlacability.openingShouldBuild.is(c)) {
//					
//				}
//			}
//			
//			for (int i = 0; i < furnisher.resources(); i++) {
//				amounts[i] += Math.ceil(r.area()*furnisher.areaCost(i, upgrade));
//				double mm = amounts[i]*0.75;
//				amounts[i] = (int) mm;
//				
//				if (mm-amounts[i] > RND.rFloat())
//					amounts[i] ++;
//				
//				
//			}
//			return amounts;
//		}
		
		@Override
		public int width() {
			int wi = (rot() & 1) == 1 ? room.body().height() : room.body().width();
			return wi;
		}
		
		@Override
		public int height() {
			int h = (rot() & 1) == 0 ? room.body().height() : room.body().width();
			return h;
		}
		
		private COORDINATE getSourceTile(int rx, int ry) {
			switch (rot()) {
			case 0:
				cTmp.set(room.body().x1()+rx, room.body().y1()+ry);
				break;
			case 1:
				cTmp.ySet(room.body().y2()-rx-1);
				cTmp.xSet(room.body().x1()+ry);
				break;
			case 2:
				cTmp.ySet(room.body().y2()-ry-1);
				cTmp.xSet(room.body().x2()-rx-1);
				break;
			case 3:
				cTmp.ySet(room.body().y1()+rx);
				cTmp.xSet(room.body().x2()-ry-1);
				break;
			default:
				throw new RuntimeException();
			}
			return cTmp;
		}
		
		private COORDINATE getOrigionalDelta(int rx, int ry) {
			int dx = rx - room.body().x1();
			int dy = ry - room.body().y1();
			switch (rot()) {
			case 0:
				cTmp.set(dx, dy);
				break;
			case 1:
				cTmp.ySet(dx);
				cTmp.xSet(room.body().height()-dy-1);
				break;
			case 2:
				cTmp.ySet(room.body().height()-dy-1);
				cTmp.xSet(room.body().width()-dx-1);
				break;
			case 3:
				cTmp.ySet(room.body().width()-dx-1);
				cTmp.xSet(dy);
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
				cTmp.ySet(rx);
				cTmp.xSet(i.height()-ry-1);
				break;
			case 2:
				cTmp.ySet(i.height()-ry-1);
				cTmp.xSet(i.width()-rx-1);
				break;
			case 3:
				cTmp.ySet(i.width()-rx-1);
				cTmp.xSet(ry);
				break;
			default:
				throw new RuntimeException();
			}
			return cTmp;
		}
		
		@Override
		public void renderPlaceHolder(SPRITE_RENDERER r, int mask, int x, int y, int tx, int ty, int rx, int ry, boolean isPlacable, boolean areaIsPlacable) {
			COORDINATE c = getSourceTile(rx, ry);
			if (!room.is(c)) {
				
				return;
			
			}
				
			if (isPlacable && areaIsPlacable) {
				if (!JOBS().planMode.is()) {
					Job.CACTIVE.bind();
				} else {
					Job.CDORMANT.bind();
				}
			}
			
			
			
			
			FurnisherItemTile tile = ROOMS().fData.tile.get(c);

			if (tile == null || tile.sprite() == null || !tile.isBlocker()) {
				SPRITES.cons().BIG.dashed.render(r, mask, x, y);
			}else {
				SPRITES.cons().BIG.filled.render(r, mask, x, y);
			}
			
			Furnisher furnisher = swap.current().constructor();
			if (furnisher.mustBeIndoors() && w) {
				for (int i = 0; i < DIR.NORTHO.size(); i++) {
					DIR d = DIR.NORTHO.get(i);
					c = getSourceTile(rx+d.x(), ry+d.y());
					if (!room.is(c)) {
						if (UtilWallPlacability.wallisReal.is(c) && UtilWallPlacability.wallShouldBuild.is(tx+d.x(), ty+d.y()))
							SPRITES.cons().BIG.filled.render(r, 0, x+d.x()*C.TILE_SIZE, y+d.y()*C.TILE_SIZE);
						else if(UtilWallPlacability.openingIsReal.is(c) && UtilWallPlacability.openingShouldBuild.is(tx+d.x(), ty+d.y()))
							SPRITES.cons().BIG.outline_dashed.render(r, 0, x+d.x()*C.TILE_SIZE, y+d.y()*C.TILE_SIZE);
					}
				}	
			}
			
			COLOR.unbind();
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
