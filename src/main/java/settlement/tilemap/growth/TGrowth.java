package settlement.tilemap.growth;

import static settlement.main.SETT.*;

import java.io.IOException;
import java.util.Arrays;

import settlement.main.SETT;
import settlement.room.food.pasture.ROOM_PASTURE;
import settlement.tilemap.TileMap;
import settlement.tilemap.terrain.*;
import settlement.tilemap.terrain.TForest.Tree;
import settlement.tilemap.terrain.Terrain.TerrainTile;
import snake2d.util.bit.Bits;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.map.MAP_DOUBLEE;
import snake2d.util.map.MAP_INTE;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.*;

public final class TGrowth extends TileMap.Resource {

	public final Bitmap2D permanent = new Bitmap2D(SETT.TILE_BOUNDS, false);
	private final byte[] data = new byte[TAREA];

	public final LIST<Grower> all;
	public final LIST<Grower> growable;
	
	public final Grower nothing;
	public final Grower tree;
	public final Grower flower;
	public final Grower bush;
	public final Grower mushroom;

	private boolean growing = false;

	public TGrowth(Terrain topology) {
		LinkedList<Grower> all = new LinkedList<>();
		nothing = new Grower(all) {

			@Override
			void grow(int tx, int ty, double max) {

			}

			@Override
			public double currentAmount(int tx, int ty) {
				return 0;
			}

			@Override
			public void setRoots(int tx, int ty, double amount) {
				
			}
		};

		tree = new Grower(all) {
			@Override
			public void setRoots(int tx, int ty, double am) {
				TERRAIN().TREES.SMALL.placeRaw(tx, ty);
				TERRAIN().TREES.amount.DM.set(tx, ty, 1.0);
			
			}

			@Override
			public double currentAmount(int tx, int ty) {
				return 0.8+RND.rFloat()*0.2;
			}

			@Override
			void grow(int tx, int ty, double max) {
				if (max < 0) {
					if (TERRAIN().TREES.isTree(tx, ty)) {
						TERRAIN().TREES.amount.increment(tx, ty, -1);
						if (TERRAIN().NADA.is(tx, ty)) {
							TERRAIN().BUSH.placeFixed(tx, ty);
						}
					} else if (TERRAIN().BUSH.is(tx, ty) && RND.oneIn(4)) {
						TERRAIN().NADA.placeFixed(tx, ty);
					}
				}else if (TERRAIN().TREES.isTree(tx, ty)) {
					if (RND.oneIn(4))
						TERRAIN().TREES.amount.increment(tx, ty, 1);
				} else if (TERRAIN().BUSH.is(tx, ty)) {
					if (max > 0.2 && RND.oneIn(32)) {
						TERRAIN().TREES.SMALL.placeFixed(tx, ty);
						TERRAIN().TREES.amount.set(tx, ty, 1);
					}
				} else if (TERRAIN().NADA.is(tx, ty)) {
					TERRAIN().BUSH.placeFixed(tx, ty);
				}

			}
		};

		flower = new Grower(all) {
			@Override
			public void setRoots(int tx, int ty, double am) {
				TERRAIN().FLOWER.placeRaw(tx, ty);
				TERRAIN().FLOWER.amount.set(tx, ty, 1+(int)(am*(TERRAIN().FLOWER.amount.max-1)));
			}

			@Override
			public double currentAmount(int tx, int ty) {
				return TERRAIN().FLOWER.amount.DM.get(tx, ty);
			}

			@Override
			void grow(int tx, int ty, double max) {
				double d = TERRAIN().FLOWER.amount.DM.get(tx, ty);
				if (max < d) {
					if (SETT.TERRAIN().FLOWER.is(tx, ty)) {
						SETT.TERRAIN().FLOWER.amount.increment(tx, ty, -1);
					}
				}else if (max > d) {
					if (TERRAIN().NADA.is(tx, ty)) {
						if (RND.oneIn(12)) {
							SETT.TERRAIN().FLOWER.amount.increment(tx, ty, 1);
						}
					}else if (SETT.TERRAIN().FLOWER.is(tx, ty))
						SETT.TERRAIN().FLOWER.amount.increment(tx, ty, 1);
				}
					
			}
		};

		bush = new Grower(all) {
			@Override
			public void setRoots(int tx, int ty, double am) {
				TERRAIN().BUSH.placeRaw(tx, ty);
				
			}

			@Override
			public double currentAmount(int tx, int ty) {
				return 0.8+RND.rFloat()*0.2;
			}

			@Override
			void grow(int tx, int ty, double max) {
				if (max < 0 && TERRAIN().BUSH.is(tx, ty) && RND.oneIn(4))
					TERRAIN().NADA.placeFixed(tx, ty);
				else if (max > 0 && TERRAIN().NADA.is(tx, ty) && RND.oneIn(8))
					TERRAIN().BUSH.placeFixed(tx, ty);
			}
		};

		mushroom = new Grower(all) {
			@Override
			public void setRoots(int tx, int ty, double am) {
				TERRAIN().MUSHROOM.placeRaw(tx, ty);
				
			}

			@Override
			public double currentAmount(int tx, int ty) {
				return 0.8+RND.rFloat()*0.2;
			}

			@Override
			void grow(int tx, int ty, double max) {
				if (max < 0 && TERRAIN().MUSHROOM.is(tx, ty))
					TERRAIN().NADA.placeFixed(tx, ty);
				else if (max > 0 && TERRAIN().NADA.is(tx, ty) && RND.oneIn(4)) {
					TERRAIN().MUSHROOM.placeFixed(tx, ty);
				}
			}
		};

		LinkedList<Grower> gg = new LinkedList<>();
		for (TGrowable g : topology.GROWABLES) {

			gg.add(new Grower(all) {

				@Override
				void grow(int tx, int ty, double max) {
					if (max <= 0)
						return;
					if (TERRAIN().NADA.is(tx, ty) && RND.oneIn(16)) {
						g.placeFixed(tx, ty);

					} else if (g.is(tx, ty) && RND.oneIn(2)) {
						int s = g.size.get(tx, ty);
						if (max_amount.get(tx, ty) > s)
							g.size.increment(tx, ty, 1);
						if (!SETT.WEATHER().growthRipe.cropsAreRipe()) {
							int am = g.resource.get(tx, ty);
							if (am < s) {
								am += 1 + (RND.oneIn(s) ? 0 : 1);
								g.resource.set(tx, ty, am);
							}
						}
					}
				}

				@Override
				public double currentAmount(int tx, int ty) {
					return g.size.DM.get(tx, ty);
				}

				@Override
				public void setRoots(int tx, int ty, double amount) {
					
					g.placeRaw(tx, ty);
					g.size.set(tx, ty, 1 + (int)((g.size.max-1)*amount));
					
				}
			});
		}

		this.growable = new ArrayList<Grower>(gg);
		this.all = new ArrayList<Grower>(all);
		
	}
	
	public boolean tear(int tx, int ty) {
		Grower g = get(tx, ty);
		if (g != null && g != tree) {
			g.grow(tx, ty, -1);
		}else if (GRASS().currentI.get(tx, ty) > 0) {
			GRASS().currentI.increment(tx, ty, -1);
			return true;
		}
		return false;
	}
	
	public double growMaxAmount(int tx, int ty) {

		double f = SETT.FERTILITY().baseD.get(tx, ty);
		if (permanent.is(tx, ty)) {
			f*= 4;
		}else
			f *= 0.25;
		
		f += 1.5*SETT.ENV().environment.WATER_SWEET.get(tx, ty);
		f = CLAMP.d(f, 0, 1);
		
		double am = max_amount.get(tx, ty);
		am -= (1.0-f);
		//return max_amount.get(tx, ty);
		return CLAMP.d(am, -1, 1);
	}

	public void updateTileDay(int tx, int ty, int now) {
		if (ROOMS().map.is(tx, ty)) {
			if (ROOMS().map.is(tx, ty) && ROOMS().map.get(tx, ty).blueprint() instanceof ROOM_PASTURE
					&& ROOMS().fData.item.get(tx, ty) == null) {
				GRASS().grow(tx, ty, now);
			}
			return;
		}
		TerrainTile t = TERRAIN().get(tx, ty);
		if ((t.clearing().isStructure() && !t.roofIs()) && !(TERRAIN().get(tx, ty) instanceof TFence))
			return;
		if (JOBS().getter.has(tx, ty))
			return;
		if (FLOOR().getter.is(tx, ty))
			return;
		
		
		GRASS().grow(tx, ty);
		GROUND().adjust(now, tx, ty);

		if (!growing)
			return;

		Grower g = all.get(type.get(now));
		if (g == nothing)
			return;

		double m = growMaxAmount(tx, ty);

		g.grow(tx, ty, m);
	}

	@Override
	protected void update(float ds) {
		growing = SETT.WEATHER().growth.getD() * SETT.WEATHER().moisture.getD() * 4 > 1.0;
	}

	@Override
	protected void save(FilePutter saveFile) {
		permanent.save(saveFile);
		saveFile.bs(data);
	}

	@Override
	protected void load(FileGetter saveFile) throws IOException {
		permanent.load(saveFile);
		saveFile.bs(data);
	}

	@Override
	protected void clearAll() {
		permanent.clear();
		Arrays.fill(data, (byte) 0);
	}

//	public void generate(GeneratorUtil util) {
//		new Generator(util);
//	}

	public Grower get(int tx, int ty) {
		TerrainTile t = SETT.TERRAIN().get(tx, ty);
		if (t instanceof TGrowable) {
			return growable.get(((TGrowable) t).growable.index);
		} else if (t instanceof Tree) {
			return tree;
		} else if (t instanceof TBush) {
			return bush;
		} else if (t instanceof TFlower) {
			return flower;
		} else if (t instanceof TMushroom) {
			return mushroom;
		}
		return nothing;
	}
	
	public Grower type(int tx, int ty) {
		return all.get(type.get(tx, ty));
	}

	private final MAP_INTE type = new MAP_INTE.INT_MAPEImp(TWIDTH, THEIGHT) {

		private final Bits bits = new Bits(0b0001_1111);

		@Override
		public int get(int tile) {
			return bits.get(data[tile]);
		}

		@Override
		public MAP_INTE set(int tile, int value) {
			data[tile] = (byte) bits.set(data[tile], value);
			return this;
		}
	};

	public final MAP_DOUBLEE max_amount = new MAP_DOUBLEE() {

		private final double di = 1.0 / 0b1000;
		private final Bits bits = new Bits(0b1110_0000);

		@Override
		public double get(int tile) {
			return (1 + bits.get(data[tile])) * di;
		}

		@Override 
		public double get(int tx, int ty) {
			return get(tx + ty * TWIDTH);
		}

		@Override
		public MAP_DOUBLEE set(int tile, double value) {
			int i = (int) Math.round(value * 0b1000);
			i -= 1;
			i = CLAMP.i(i, 0, 0b0111);
			data[tile] = (byte) bits.set(data[tile], i);
			return this;
		}

		@Override
		public MAP_DOUBLEE set(int tx, int ty, double value) {
			return set(tx + ty * TWIDTH, value);
		}
	};

	public static abstract class Grower {

		protected final int index;

		Grower(LISTE<Grower> all) {
			index = all.add(this);
		}

		abstract void grow(int tx, int ty, double fertility);

		public abstract double currentAmount(int tx, int ty);
		
		public abstract void setRoots(int tx, int ty, double amount);
		
		public void set(int tx, int ty) {
			SETT.TILE_MAP().growth.type.set(tx, ty, index);
		}
	}
	

	


}
