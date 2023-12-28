package settlement.room.water;

import java.io.IOException;

import game.time.TIME;
import init.RES;
import init.sprite.SPRITES;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.room.main.*;
import settlement.room.main.furnisher.*;
import settlement.room.main.util.RoomInit;
import settlement.room.main.util.RoomInitData;
import settlement.room.sprite.*;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import snake2d.util.sprite.TILE_SHEET;
import util.colors.GCOLOR;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;
import util.spritecomposer.*;
import util.spritecomposer.ComposerThings.ITileSheet;

final class PumpConstructor extends Furnisher{

	private final Pump blue;
	
	final FurnisherStat workers;
	
	final static int B_WORK = 4;
	final static int B_CANAL = -1;
	
	final FurnisherItemTile ou;
	
	private static CharSequence ¤¤GroundD = "Pumps work best on ground water. That is, close to natural bodies of water.";
	
	protected PumpConstructor(Pump blue, RoomInitData init)
			throws IOException {
		super(init, 1, 1);
		this.blue = blue;
				
		
		workers = new FurnisherStat.FurnisherStatEmployees(this);

		Json js = init.data().json("SPRITES");
		SPump spump = new SPump(init);
		
		
		RoomSprite1x1 sBottom = new RoomSprite1x1(js, "WORKDONG_1X1") {
			
			@Override
			protected boolean joins(int tx, int ty, int rx, int ry, DIR d, FurnisherItem item) {
				return item.sprite(rx, ry) instanceof RoomSpriteCombo;
			};
		};
		
		RoomSprite[] sWork = new RoomSprite1x1[3];
		
		String[] ss = new String[] {
			"A",
			"B",
			"C",
		};
		
		for (int i = 0; i < 3; i++) {
			final int up = i;
			sWork[i] = new RoomSprite1x1(js, "WORK_" + ss[i] + "_1X1") {
				
				@Override
				public byte getData2(int tx, int ty, int rx, int ry, FurnisherItem item, int itemRan) {
					return sBottom.getData(tx, ty, rx, ry, item, itemRan);
				}
				
				@Override
				protected boolean joins(int tx, int ty, int rx, int ry, DIR d, FurnisherItem item) {
					return item.sprite(rx, ry) instanceof RoomSpriteCombo;
				};
				
				@Override
				public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
						boolean isCandle) {
					
					animationSpeed = 0;
					PumpInstance ins = blue.get(it.tx(), it.ty());
					
					if (ins != null) {
						if (ins.upgrade() > up) {
							animationSpeed = ins.aniSpeed();
						}else {
							if (blue.job.working(SETT.ROOMS().data.get(it.tile()))) {
								animationSpeed = 1.0;
							}
						}
					}
					
					return super.render(r, s, data, it, degrade, isCandle);
				}
				
				@Override
				public void renderBelow(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
					sBottom.render(r, s, getData2(it), it, degrade, false);
				}
			};
		}
		
		RoomSpriteCombo sBody = new RoomSpriteCombo(js, "FRAME_COMBO") {
			
			final RoomSprite1x1 top = new RoomSprite1x1(js, "FRAME_TOP_1X1");
			
			@Override
			public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				
				if (!SETT.ROOMS().fData.candle.is(it.tile()) && (RES.ran2().get(it.tile()) & 0b11) == 0) {
					top.render(r, s, getData2(it), it, degrade, false);
				}
			}
			
			@Override
			public byte getData2(int tx, int ty, int rx, int ry, FurnisherItem item, int itemRan) {
				return top.getData(tx, ty, rx, ry, item, itemRan);
			}
			
		};
		
		RoomSprite sOut = new RoomSprite() {
			
			@Override
			public void renderBelow(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				SETT.ROOMS().WATER.sprite.renderBelow(r, s, it, DIR.ORTHO.get(data).perpendicular().mask(), false);
			}
			
			@Override
			public void renderPlaceholder(SPRITE_RENDERER r, int x, int y, int data, int tx, int ty, int rx, int ry,
					FurnisherItem item) {
				DIR dir = DIR.ORTHO.get(data);
				SPRITES.cons().ICO.arrows.get(dir.orthoID()).render(r, x, y);
			}

			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
					boolean isCandle) {
				int flow = blue.is(it.tile()) ? DIR.ORTHO.get(data).mask() : 0;
				SETT.ROOMS().WATER.sprite.render(r, s, it,  DIR.ORTHO.get(data).perpendicular().mask(), flow, false);
				return false;
			}

			@Override
			public byte getData(int tx, int ty, int rx, int ry, FurnisherItem item, int itemRan) {
				return (byte) item.rotation;
			}

			@Override
			public int sData() {
				return 0;
			}
		};
		RoomSprite1x1 sPipe = new RoomSprite1x1(js, "PIPE_1X1") {
			
			@Override
			protected boolean joins(int tx, int ty, int rx, int ry, DIR d, FurnisherItem item) {
				return item.sprite(rx, ry) instanceof RoomSpriteCombo;
			};
		};

		RoomSprite s__ = null;
		
		final FurnisherItemTile w1 = new FurnisherItemTile(this, true, sWork[0], AVAILABILITY.SOLID, false).setData(B_WORK);
		final FurnisherItemTile w2 = new FurnisherItemTile(this, false, sWork[1], AVAILABILITY.SOLID, false).setData(B_WORK+1);
		final FurnisherItemTile w3 = new FurnisherItemTile(this, false, sWork[2], AVAILABILITY.SOLID, false).setData(B_WORK+2);		
		
		ou = new FurnisherItemTile(this, false, sOut, AVAILABILITY.SOLID, false).setData(B_CANAL);
		final FurnisherItemTile pi = new FurnisherItemTile(this, false, sPipe, AVAILABILITY.SOLID, false);
		
		
		final FurnisherItemTile bo = new FurnisherItemTile(this, false, sBody, AVAILABILITY.SOLID, false);
		final FurnisherItemTile b1 = new FurnisherItemTile(this, false, spump.sprite(0, js), AVAILABILITY.SOLID, false);
		final FurnisherItemTile b2 = new FurnisherItemTile(this, false, spump.sprite(1, js), AVAILABILITY.SOLID, false);
		final FurnisherItemTile b3 = new FurnisherItemTile(this, false, spump.sprite(2, js), AVAILABILITY.SOLID, false);
		final FurnisherItemTile __ = new FurnisherItemTile(this, false, s__, AVAILABILITY.ROOM, false);
	
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{__,__,pi,ou,pi,__,__},
			{__,w2,bo,b1,bo,w2,__},
			{__,w1,bo,b2,bo,w1,__},
			{__,w3,bo,b3,bo,w3,__},
			{__,__,pi,pi,pi,__,__},
		}, 1);
		
		
		flush(1, 3);
	}

	public boolean isJob(int tx, int ty) {
		PumpInstance ins = blue.get(tx, ty);
		
		if (ins != null) {
			
			int dd = SETT.ROOMS().fData.tileData.get(tx, ty)-B_WORK;
			return ins.upgrade() <= dd;
		}
		return false;
	}

	@Override
	public boolean usesArea() {
		return false;
	}

	@Override
	public boolean mustBeIndoors() {
		return false;
	}

	@Override
	public Room create(TmpArea area, RoomInit init) {
		return new PumpInstance(blue, area, init);
	}

	@Override
	public RoomBlueprintImp blue() {
		return blue;
	}
	
	@Override
	public void placeInfo(GBox box, FurnisherItem item, int x1, int y1) {
		
		double v = value(x1, y1, item.width(), item.height());
		if (v >= 1) {
			box.add(box.text().color(GCOLOR.T().IGREAT).add(PumpGui.¤¤GroundWater));
			box.tab(6);
			box.add(GFORMAT.perc(box.text(), 1.0));
			box.NL();
			box.text(¤¤GroundD);
		}else {
			box.add(box.text().warnify().add(PumpGui.¤¤GroundWater));
			box.tab(6);
			box.add(GFORMAT.perc(box.text(), v));
			box.NL();
			box.add(box.text().warnify().add(¤¤GroundD));
		}
		
	}
	
	public static double value(int x1, int y1, int width, int height) {
		double hi = 0;
		for (int dy = 0; dy < height; dy++) {
			for (int dx = 0; dx < width; dx++) {
				hi = Math.max(hi, SETT.FERTILITY().baseD.get(x1+dx, y1+dy));
				if (SETT.TERRAIN().WATER.groundWater.is(x1+dx, y1+dy)) {
					return 1.0;
				}
			}
		}
		
		return 0.1 + hi*0.5;
	}

	
	
	static final class SPump {
		
		private final TILE_SHEET sheet;
		
		private final static int animations = 5;
		private final static int shadow = animations*2*4;
		
		SPump(RoomInitData init) throws IOException{
			sheet = new ITileSheet(init.gSprite.get("PUMP"), 220, 76) {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.full2.init(0, 0, animations, 2, 1, 2, d.s16);
					for (int r = 0; r < 2; r++) {
						for (int i = 0; i < animations; i++) {
							s.full2.setVar(i+animations*r).setSkip(1, 0);
							s.full2.paste(3, true);
						}
						for (int i = 0; i < animations; i++) {
							s.full2.setVar(i+animations*r).setSkip(1, 1);
							s.full2.paste(3, true);
						}
					}
					return d.s16.saveGame();
					
				}
			}.get();
			
		}
		
		public RoomSprite sprite(int ii, Json js) throws IOException {
			return new RoomSpriteCombo(js, "FRAME_COMBO") {
	
				
				
				@Override
				public byte getData2(int tx, int ty, int rx, int ry, FurnisherItem item, int itemRan) {
					return (byte) item.rotation;
				}
				
				@Override
				public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
					
					int rot = getData2(it);
					
					
					int frames = (animations-1)*2;
					int ani = (int) (TIME.currentSecond()*6.0)%frames;
					if (ani >= animations)
						ani = frames-ani;
					int tile = 0;
					if (ii == 0) {
						tile = rot + ani*4;
					}else if (ii == 1) {
						tile = rot + animations*4 + ani*4;
					}else {
						rot = (rot+2)%4;
						ani = animations-ani-1;
						tile = rot + ani*4;
					}
					sheet.render(r, tile, it.x(), it.y());
					s.setHeight(14).setDistance2Ground(0);
					sheet.render(s, tile+shadow, it.x(), it.y());
				}
				
			};
		}
		
	}





}
