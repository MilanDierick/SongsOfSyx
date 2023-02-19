package settlement.room.home.house;

import java.io.IOException;

import init.C;
import init.race.home.RaceHomeClass;
import init.sprite.SPRITES;
import init.sprite.game.*;
import settlement.main.RenderData.RenderIterator;
import settlement.main.SETT;
import settlement.room.home.house.Sprite.Rot;
import settlement.room.main.Room;
import settlement.room.main.furnisher.FurnisherItem;
import settlement.room.sprite.RoomSprite;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.Rec;
import snake2d.util.rnd.RND;
import util.rendering.ShadowBatch;

class Sprites {

	private final Carpet carpet = new Carpet();
	private final HomeHouse house = new HomeHouse();
	
	Sprites() throws IOException{
		
	}
	
	public final Sprite bedN = new Sprite(house) {

		@Override
		public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
				boolean isCandle) {
			
			int ran = it.ran();
			Sheets a = sp().bedTop.get(house);
			render1x1(ran, a, r, s, data, it, degrade);

			return false;
		}

		@Override
		public byte getData(int tx, int ty, int rx, int ry, FurnisherItem item, int itemRan) {
			house.useAndReserve(tx, ty);
			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				DIR d = DIR.ORTHO.get(i);
				if (house.sprite.get(tx, ty, d) == bedS) {
					return (byte) (i);
				}
			}
			return 0;
		}
	};

	public final Sprite bedS = new Sprite(house, true, true, true) {

		@Override
		public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
				boolean isCandle) {
			
			DIR dir = DIR.ORTHO.get((data & 0b011));
			it.ranOffset(dir.x(), dir.y());
			int ran = it.ran();
			Sheets a = sp().bedBottom.get(house);
			render1x1(ran, a, r, s, data, it, degrade);
			return false;
		}

		@Override
		public byte getData(int tx, int ty, int rx, int ry, FurnisherItem item, int itemRan) {
			house.useAndReserve(tx, ty);
			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				DIR d = DIR.ORTHO.get(i);
				if (house.sprite.is(tx, ty, d, bedN)) {
					return (byte) (i);
				}
			}
			return 0;
		}
	};

	static void render1x1(int ran, Sheets a, SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
		if (a != null) {
			SheetPair sh = a.get(ran);
			if (sh != null) {
				sh.d.color(ran).bind();
				ran = ran >> 4;
				int i = SheetType.s1x1.tile(sh.s, sh.d, 0, sh.d.frame(ran, 1.0), (data & 0b011));
				ran = ran >> 4;
				sh.s.render(sh.d, it.x(), it.y(), it, r, i, ran, 0);
				COLOR.unbind();
				sh.s.renderShadow(sh.d, it.x(), it.y(), it, s, i, ran);
			}
		}
	}
	
	public final Sprite nSta = new Sprite(house) {

		@Override
		public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
				boolean isCandle) {
			Sheets a = sp().nightStand.get(house);
			render1x1(it.ran(), a, r, s, data, it, degrade);

			return false;
		}

		@Override
		public byte getData(int tx, int ty, int rx, int ry, FurnisherItem item, int itemRan) {
			return 0;
		}
	};
	
	public final Sprite tabl = new Sprite(house) {

		@Override
		public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
				boolean isCandle) {
			Sheets a = sp().table.get(house);
			if (a != null) {
				int vv = (data >> 4)&0x0F;
				SheetPair sh = a.get(vv);
				if (sh != null) {
					sh.d.color(vv).bind();
					int ran = it.ran();
					int t = SheetType.sCombo.tile(sh.s, sh.d, data&0x0F, sh.d.frame(ran, 1.0), 0);
					sh.s.render(sh.d, it.x(), it.y(), it, r, t, ran, 0);
					COLOR.unbind();
					sh.s.renderShadow(sh.d, it.x(), it.y(), it, s, t, ran);
				}
			}

			return false;
		}
		
		
		@Override
		public byte getData(int tx, int ty, int rx, int ry, FurnisherItem item, int itemRan) {
			int m = 0;
			for (DIR d : DIR.ORTHO) {
				if (house.sprite.get(tx, ty, d) == this)
					m |= d.mask();
			}
			return (byte) (m | ((itemRan&0x0F) << 4));
		}
	};

	public final Sprite stor = new Rot(house) {

		@Override
		Sheets a(RaceHomeClass it) {
			return it.storage.get(house);
		}

	};

	public final Sprite chai = new Rot(house, true, false, true) {

		@Override
		Sheets a(RaceHomeClass it) {
			return it.chair.get(house);
		}
		
		@Override
		public byte getData(int tx, int ty, int rx, int ry, FurnisherItem item, int itemRan) {
			
			Room room = SETT.ROOMS().map.get(tx, ty);
			

			int r = RND.rInt(4);
			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				DIR d = DIR.ORTHO.getC(i+r);
				if (!room.isSame(tx, ty, tx+d.x(), ty+d.y())) {
					return (byte) ((i+2)%4);
				}
				
			}
			house.useAndReserve(tx, ty);
			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				DIR d = DIR.ORTHO.get(i);
				if (house.sprite.get(tx, ty, d) == SETT.ROOMS().HOMES.HOME.constructor.sp.tabl) {
					return (byte) (i);
				}
			}
			
			return (byte) RND.rInt(4);
		}
	};

	private final Sprite nic1Top = new Rot(house) {

		@Override
		Sheets a(RaceHomeClass it) {
			return it.nickTop1.get(house);
		}

	};
	
	public final Sprite nic1 = new Rot(house) {

		@Override
		Sheets a(RaceHomeClass it) {
			return it.nick1.get(house);
		}
		
		@Override
		public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
			nic1Top.render(r, s, data, it, degrade, false);
		};

	};

	public final Sprite nic2 = new Rot(house) {

		@Override
		Sheets a(RaceHomeClass it) {
			return it.nick2.get(house);
		}

	};

	public final Sprite _mat = new Rot(house,false, false, false) {

		@Override
		Sheets a(RaceHomeClass it) {
			return it.mat.get(house);
		}
	};

	public final RoomSprite theDummy = new RoomSprite.Dummy() {

		@Override
		public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {

			if (SETT.ROOMS().HOMES.HOME.is(it.tile())) {
				s.setSoft();
				s.setDistance2Ground(0).setHeight(0);
				COLOR.BLACK.render(s, it.x(), it.x() + C.TILE_SIZE, it.y(), it.y() + C.TILE_SIZE);
				s.setPrev();
				if (SETT.ROOMS().fData.tile.get(it.tx(), it.ty()).data() == 2)
					return;
				house.useAndReserve(it.tx(), it.ty());
				
				if (house.occupants() > 0) {
					RoomSprite sp = house.sprite.get(it.tx(), it.ty());
					if (sp != null)
						sp.renderAbove(r, s, data, it, degrade);
				}
//				if (!ldata.body().holdsPoint(VIEW.s().getWindow().tile())) {
//					int m = 0;
//					for (DIR d : DIR.ORTHO) {
//						if (ldata.is(it.tx(), it.ty(), d))
//							m |= d.mask();
//					}
//					walls.render(it.x(), it.y(), it, r, m, 0, 0);
//				}

			}
		}

		@Override
		public boolean render(SPRITE_RENDERER r, ShadowBatch shadowBatch, int data, RenderIterator it, double degrade,
				boolean isCandle) {
			if (SETT.ROOMS().HOMES.HOME.is(it.tile())) {
				house.useAndReserve(it.tx(), it.ty());
				if (house.occupants() > 0) {
					RaceHomeClass stage = house.race().home().clas(house.occupant(0));
					if (stage.floor(house) != SETT.FLOOR().getter.get(it.tx(), it.ty())) {
						if (stage.floor(house) == null)
							SETT.FLOOR().clearer.clear(it.tx(), it.ty());
						else
							stage.floor(house).placeFixed(it.tx(), it.ty());
					}
					
					RoomSprite s = house.sprite.get(it.tx(), it.ty());
					if (s != null)
						s.render(r, shadowBatch, data, it, degrade, isCandle);
					shadowBatch.setSoft();
					shadowBatch.setHeight(16).setDistance2Ground(4);
					COLOR.BLACK.render(shadowBatch, it.x(), it.x() + C.TILE_SIZE, it.y(), it.y() + C.TILE_SIZE);
					shadowBatch.setPrev();
				}else if (SETT.FLOOR().getter.get(it.tx(), it.ty()) != SETT.ROOMS().HOMES.HOME.constructor.flooring)
					SETT.ROOMS().HOMES.HOME.constructor.flooring.placeFixed(it.tx(), it.ty());
				
				
				


				
			}

			return false;

		}

		@Override
		public void renderBelow(SPRITE_RENDERER r, ShadowBatch shadow, int data, RenderIterator it, double degrade) {
			if (SETT.ROOMS().HOMES.HOME.is(it.tile())) {
				house.useAndReserve(it.tx(), it.ty());
				if (house.occupants() > 0) {
					RoomSprite s =  house.sprite.get(it.tx(), it.ty());
					if (s != null)
						s.renderBelow(r, shadow, data, it, degrade);

					renderCarpet(r, shadow, data, it, degrade);
				}
				
				

			}
		}

		private void renderCarpet(SPRITE_RENDERER r, ShadowBatch shadow, int data, RenderIterator it, double degrade) {

			DIR dd = house.dir(it.tx(), it.ty());
			
			Room room = SETT.ROOMS().map.get(it.tile());
			int rx = it.tx()-room.x1(it.tx(), it.ty());
			int ry = it.ty()-room.y1(it.tx(), it.ty());
			int c = carpet.get(rx, ry, house.it());
			if (c == 0)
				return;
			
			Sheets a =  house.race().home().clas( house.occupant(0)).carpet.get(house);
			if (a == null)
				return;
			
			int ran = it.ran(room.x1(it.tx(), it.ty())+c, room.y1(it.tx(), it.ty()));
			SheetPair ts = a.get(ran);
			
			
			int t = 0;
			for (DIR d : DIR.ORTHO) {
				if (carpet.get(rx, ry, d, house.it()) == c)
					t |= d.mask();
			}
			
			
			ran = ran >> 4;
			t = SheetType.sCombo.tile(ts.s, ts.d, t, ts.d.frame(ran, 1.0), 0);
			
			int dx = C.TILE_SIZEH / 2 + (ran & C.T_MASK) / 2;
			if (dd.x() > 0)
				dx = 0;
			else if (dd.x() < 0) {
				dx = C.TILE_SIZEH + C.TILE_SIZEH / 2;
			}
			ran = ran >> C.T_SCROLL;
			int dy = C.TILE_SIZEH / 2 + (ran & C.T_MASK) / 2;
			if (dd.y() > 0)
				dy = 0;
			else if (dd.y() < 0) {
				dy = C.TILE_SIZEH + C.TILE_SIZEH / 2;
			}
			ts.s.render(ts.d, it.x() - dx, it.y() - dy, it, r, t, ran, 0);

		}
		
		@Override
		public void renderPlaceholder(SPRITE_RENDERER r, int x, int y, int data, int tx, int ty, int rx, int ry, FurnisherItem item) {
			if (item.get(rx, ry).data() == 2)
				return;
				
			int m = 0;
			for (DIR d : DIR.ORTHO)
				if (item.is(rx, ry, d))
					m |= d.mask();
			SPRITES.cons().BIG.outline.render(r, m, x, y);
		};

		private final Rec tmp = new Rec();

		@Override
		public byte getData(int tx, int ty, int rx, int ry, FurnisherItem item, int itemRan) {
			byte m = 0;

			int w = item.group().item(0, item.rotation).width();
			int h = item.group().item(0, item.rotation).height();
			tmp.setDim(w, h);

			tmp.moveX1Y1(w * (rx / w), h * (ry / h));

			for (DIR d : DIR.ORTHO) {
				if (tmp.holdsPoint(rx, ry, d))
					m |= d.mask();
			}
			return m;
		}

	};

	final SpriteConfigs sp = new SpriteConfigs();

	final class SpriteConfigs {

		final SpriteConfig[][] sprites = new SpriteConfig[4][];

		public SpriteConfigs() {
			sprites[0] = mirror(new SpriteConfig[] {
				new SpriteConfig(new Sprite[][] { 
					{ bedN, bedS, nic2 }, 
					{ nSta, null, nic1 }, 
					{ stor, _mat, chai }, }),
				new SpriteConfig(new Sprite[][] { 
					{ nic2, bedS, bedN }, 
					{ nic1, null, nSta }, 
					{ chai, _mat, stor }, }),
				new SpriteConfig(new Sprite[][] { 
					{ bedN, nSta, nic2 }, 
					{ bedS, null, nic1 }, 
					{ stor, _mat, chai }, }),
				new SpriteConfig(new Sprite[][] { 
					{ nic2, nSta, bedN }, 
					{ nic1, null, bedS }, 
					{ stor, _mat, chai }, }),
				new SpriteConfig(new Sprite[][] { 
					{ nic2, stor, nSta }, 
					{ chai, null, bedS }, 
					{ nic1, _mat, bedN }, }),
				new SpriteConfig(new Sprite[][] { 
					{ nic2, nic1, stor }, 
					{ bedS, null, chai }, 
					{ bedN, _mat, nSta }, }),

			});

			sprites[1] = mirror(new SpriteConfig[] {
				new SpriteConfig(new Sprite[][] {
					{ bedN, bedS, bedS, bedN, stor },
					{ nSta, null, null, null, nSta },
					{ stor, nic1, _mat, chai, nic2 }, }),
				new SpriteConfig(new Sprite[][] {
					{ bedN, nSta, nic1, nic1, bedN },
					{ bedS, null, null, null, bedS },
					{ nic2, chai, _mat, chai, stor }, }),
				new SpriteConfig(new Sprite[][] {
					{ nic2, chai, nic1, chai, stor },
					{ nic1, null, null, null, nSta },
					{ bedN, bedS, _mat, bedS, bedN }, }),
				new SpriteConfig(new Sprite[][] {
					{ null, nic1, nic1, bedS, bedN },
					{ chai, null, null, null, nSta },
					{ bedN, bedS, _mat, chai, stor }, }),
				new SpriteConfig(new Sprite[][] {
					{ bedN, bedS, nSta, bedS, bedN },
					{ chai, null, null, null, chai },
					{ stor, nic1, _mat, nic1, nic2 }, }),
				new SpriteConfig(new Sprite[][] {
					{ stor, nic1, nic1, chai, nSta },
					{ bedS, null, null, null, bedS },
					{ bedN, nic2, _mat, chai, bedN }, }),
				new SpriteConfig(new Sprite[][] {
					{ nSta, nic1, chai, chai, bedN },
					{ bedS, null, null, null, bedS },
					{ bedN, nic1, _mat, nic2, stor }, }),
				new SpriteConfig(new Sprite[][] {
					{ null, chai, nic1, bedS, bedN },
					{ chai, null, null, null, nSta },
					{ bedN, bedS, _mat, nic1, stor }, }),
				new SpriteConfig(new Sprite[][] {
					{ bedN, bedN, bedS, chai, nic2 },
					{ bedS, null, null, null, chai },
					{ nSta, nic1, _mat, nic1, stor }, }),

			});

			sprites[2] = mirror(new SpriteConfig[] {
				new SpriteConfig(new Sprite[][] {
					{ bedN, bedS, nic1, bedS, bedN },
					{ nSta, null, null, null, chai },
					{ chai, null, null, null, nic2 },
					{ bedN, bedS, null, null, chai },
					{ bedN, bedS, _mat, nSta, stor }, }),
				new SpriteConfig(new Sprite[][] {
					{ stor, chai, nic1, nic1, nic2 },
					{ chai, null, null, null, chai },
					{ nSta, null, null, null, nSta },
					{ bedN, bedS, null, bedS, bedN },
					{ bedN, bedS, _mat, bedS, bedN }, }),  
				new SpriteConfig(new Sprite[][] {
					{ bedN, bedS, chai, chai, nSta },
					{ bedN, null, null, null, nic2 },
					{ bedS, null, null, null, stor },
					{ nSta, null, null, null, bedS },
					{ bedN, bedS, _mat, nic1, bedN }, }), 
				new SpriteConfig(new Sprite[][] {
					{ stor, bedN, bedN, bedN, nSta },
					{ nic1, bedS, bedS, bedS, bedN },
					{ nic1, null, null, null, bedS },
					{ chai, null, null, null, chai },
					{ nic2, nSta, _mat, chai, stor }, }), 
				new SpriteConfig(new Sprite[][] {
					{ bedN, chai, nic1, chai, bedN },
					{ bedS, null, null, null, bedS },
					{ nSta, null, null, null, nSta },
					{ bedS, null, null, null, bedS },
					{ bedN, stor, _mat, nic2, bedN }, }), 
				new SpriteConfig(new Sprite[][] {
					{ nSta, nic2, chai, bedS, bedN },
					{ nic1, null, null, null, bedN },
					{ nic1, null, null, null, bedS },
					{ chai, null, null, null, bedS },
					{ stor, chai, _mat, nSta, bedN }, }), 
				new SpriteConfig(new Sprite[][] {
					{ bedN, nic2, bedN, nSta, bedN },
					{ bedS, null, bedS, null, bedS },
					{ chai, null, null, null, nic1 },
					{ nSta, null, null, null, chai },
					{ bedN, bedS, _mat, chai, stor }, }), 
			
			});

			sprites[3] = mirror(new SpriteConfig[] {
				new SpriteConfig(new Sprite[][] {
					{ nic2, nic1, nic1, nic1, nic2 },
					{ stor, null, null, null, nSta },
					{ bedN, bedS, null, bedS, bedN },
					{ bedN, bedS, null, bedS, bedN },
					{ nSta, null, null, null, stor },
					{ chai, null, null, null, stor },
					{ chai, null, null, null, bedN },
					{ nic1, null, null, null, bedS },
					{ nic2, nic1, _mat, nic1, nic2 }, }),
				new SpriteConfig(new Sprite[][] {
					{ bedN, stor, nSta, bedS, bedN },
					{ bedS, null, null, null, nSta },
					{ nic1, chai, chai, null, bedN },
					{ nic1, tabl, tabl, null, bedS },
					{ nic1, chai, chai, null, nic2 },
					{ nSta, null, null, null, nic1 },
					{ bedN, bedS, null, null, nic1 },
					{ bedN, bedS, null, null, nic2 },
					{ bedN, bedS, _mat, nSta, stor }, }),
				new SpriteConfig(new Sprite[][] {
					{ nSta, bedN, bedN, bedN, nSta },
					{ nic1, bedS, bedS, bedS, nic1 },
					{ stor, null, null, null, chai },
					{ chai, null, null, null, chai },
					{ nic1, chai, chai, null, bedN },
					{ nic2, tabl, tabl, null, bedS },
					{ nic1, chai, chai, null, nSta },
					{ bedS, null, null, null, bedS },
					{ bedN, nSta, _mat, nic1, bedN }, }),
				new SpriteConfig(new Sprite[][] {
					{ stor, chai, nSta, chai, stor },
					{ nic1, null, null, null, nic2 },
					{ bedN, bedS, null, bedS, bedN },
					{ bedN, bedS, null, bedS, bedN },
					{ bedN, bedS, null, chai, stor },
					{ bedN, bedS, null, tabl, nic1 },
					{ bedN, bedS, null, chai, nic1 },
					{ nic1, null, null, null, nic1 },
					{ stor, nSta, _mat, chai, nic2 }, }),
				new SpriteConfig(new Sprite[][] {
					{ nSta, chai, chai, bedS, bedN },
					{ chai, null, null, null, nSta },
					{ stor, chai, null, bedS, bedN },
					{ nic1, tabl, null, bedS, bedN },
					{ nic1, chai, null, bedS, bedN },
					{ nic2, null, null, bedS, bedN },
					{ nic1, null, null, null, nSta },
					{ stor, null, null, bedS, bedN },
					{ nSta, chai, _mat, bedS, bedN }, }),
				new SpriteConfig(new Sprite[][] {
					{ nic2, chai, chai, nic1, nSta },
					{ nic1, null, null, null, nic2 },
					{ bedN, bedS, null, nSta, stor },
					{ bedN, bedS, null, null, stor },
					{ bedN, bedS, null, chai, nic1 },
					{ bedN, bedS, null, tabl, nic1 },
					{ bedN, bedS, null, chai, nic1 },
					{ chai, null, null, bedS, bedN },
					{ nSta, chai, _mat, bedS, bedN }, }),
				new SpriteConfig(new Sprite[][] {
					{ nic2, nSta, bedN, bedN, nSta },
					{ nic1, null, bedS, bedS, nic2 },
					{ nic1, null, null, null, nic1 },
					{ bedS, null, null, bedS, bedN },
					{ bedN, chai, null, bedS, bedN },
					{ nSta, tabl, null, null, nSta },
					{ bedN, chai, null, null, nic1 },
					{ bedS, null, null, bedS, bedN },
					{ nSta, chai, _mat, bedS, bedN }, }),
				
			
			});
			
			

		}
		
		private SpriteConfig[] mirror(SpriteConfig[] o) {
			
			SpriteConfig[] res = new SpriteConfig[o.length*2];
			
			for (int i = 0; i < o.length; i++) {
				res[i] = o[i];
				Sprite[][] org = o[i].spri;
				
				int h = org.length;
				int w = org[0].length;
				
				Sprite[][] nn = new Sprite[h][w];
				
				for (int y = 0; y < h; y++) {
					for (int x = 0; x < w; x++) {
						nn[y][w-x-1] = org[y][x];
					}
				}
				
				res[i+o.length] = new SpriteConfig(nn);
			}
			
			return res;
			
		}
	}
	

		

}
