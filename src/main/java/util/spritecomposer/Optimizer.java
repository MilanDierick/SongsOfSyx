package util.spritecomposer;

import java.io.IOException;

import snake2d.*;
import snake2d.util.file.*;
import snake2d.util.sprite.TextureCoords;

final class Optimizer implements SAVABLE {

	private static Optimizer i;

	final Tile s8;
	final Tile s16;
	final Tile s24;
	final Tile s32;

	private final TextureCoords coos = new TextureCoords();

	Optimizer(FileGetter g, SnakeImage source) throws IOException{
		i = this;
		int y1 = 0;
		s8 = new Tile(y1, source.width, 8, g.lastInt(5));
		y1 += 8*(s8.rows);
		s16 = new Tile(y1, source.width, 16, g.lastInt(4));
		y1 += 16*(s16.rows);
		s32 = new Tile(y1, source.width, 32, g.lastInt(3));
		y1 += 32*(s32.rows);
		s24 = new Tile(y1, source.width, 24, g.lastInt(2));
		optimize(source);
	}
	
	static Tile get(int tileSize) {
		if (i == null)
			return null;
		switch (tileSize) {
		case 8:
			return i.s8;
		case 16:
			return i.s16;
		case 24:
			return i.s24;
		case 32:
			return i.s32;
		default:
			throw new RuntimeException("" + tileSize);
		}
	}

	private static void optimize(SnakeImage source) {
		
		LOG.ln("TILE OPTIMIZING");
		i.s8.optimize(source);
		i.s16.optimize(source);
		i.s24.optimize(source);
		i.s32.optimize(source);

	}

	final class Tile implements SAVABLE {

		final int startY, tilesX, size;
		private final byte[] data;
		private final byte[] opaques;
		private final static int offX1 = 0, widths = 1, offY1 = 2, heights=3;
		private final int tiles;
		private final int tScroll;
		private final int tMask;
		private final int rows;

		private Tile(int startY, int width, int size, int rows) {
			this.rows = rows;
			this.startY = startY;
			{
				int s = 1;
				while (s * 2 * size <= width) {
					s *= 2;
				}
				tilesX = s;
			}
			this.size = size;
			int s = tilesX * rows;
			data = new byte[4*s];
			opaques = new byte[s];
			tiles = s;
			tScroll = Integer.numberOfTrailingZeros(tilesX);
			tMask = tilesX - 1;
		}

		public final void render(SPRITE_RENDERER r, int tile, int x1, int y1, int scale) {
			if (tile < 0)
				return;

			final int dtile = tile*4;
			
			final int wi = data[widths+dtile];
			final int hi = data[heights+dtile];
			
			if (wi <= 0 ||hi <= 0)
				return;

			int tx = tile & tMask;
			int ty = tile >> tScroll;

			int px = (tx * size);
			int py = startY + (ty * size);
			
			final int ox = data[offX1+dtile];
			final int oy = data[offY1+dtile];
			
			px += ox;
			py += oy;

			x1 = x1 + ox * scale;
			int x2 = x1 + wi * scale;

			y1 = y1 + oy * scale;
			int y2 = y1 + hi * scale;

			r.renderSprite(x1, x2, y1, y2, TextureCoords.Normal.get(px, py, wi, hi));

		}

		public final void renderTextured(TextureCoords t, int tile, int x1, int y1, int scale) {
			if (tile < 0)
				return;

			final int dtile = tile*4;
			final int wi = data[widths+dtile];
			final int hi = data[heights+dtile];
			
			if (wi <= 0 ||hi <= 0)
				return;

			int tx = tile & tMask;
			int ty = tile >> tScroll;

			int px = (tx * size);
			int py = startY + (ty * size);
			
			final int ox = data[offX1+dtile];
			final int oy = data[offY1+dtile];
			
			px += ox;
			py += oy;

			x1 = x1 + ox * scale;
			int x2 = x1 + wi * scale;

			y1 = y1 + oy * scale;
			int y2 = y1 + hi * scale;

			coos.get(t.x1 + ox, t.y1 + oy, wi, hi);

			//
			// if (opaques[tile] == 1) {
			// CORE.renderer().renderTileOpaque(
			// x1, x2, y1, y2,
			// coos,
			// TextureCoords.Normal.get(px, py, widths[tile], heights[tile])
			// );
			//
			// return;
			// }else {
			// CORE.renderer().renderTextured(
			// x1, x2, y1, y2,
			// coos,
			// TextureCoords.Normal.get(px, py, widths[tile], heights[tile])
			// );
			// }

			CORE.renderer().renderTextured(x1, x2, y1, y2, coos,
					TextureCoords.Normal.get(px, py, wi, hi));

		}

		private final void optimize(SnakeImage source) {

			int o = 0;
			int cropped = 0;

			for (int t = 0; t < tiles; t++) {

				int tile = t*4;
				
				searchx1: for (int x = 0; x < size; x++) {
					data[offX1+tile] = (byte) x;
					for (int y = 0; y < size; y++) {
						int px = (t % tilesX) * size + x;
						int py = (t / tilesX) * size + y + startY;

						if ((source.rgb.get(px, py) & 0x000000FF) != 0)
							break searchx1;
					}
				}

				searchW: for (int x = size - 1; x >= 0; x--) {
					data[widths+tile] = (byte) (x - data[offX1+tile] + 1);
					for (int y = 0; y < size; y++) {
						int px = (t % tilesX) * size + x;
						int py = (t / tilesX) * size + y + startY;

						if ((source.rgb.get(px, py) & 0x000000FF) != 0)
							break searchW;
					}

				}

				search: for (int y = 0; y < size; y++) {
					data[offY1+tile] = (byte) y;
					for (int x = 0; x < size; x++) {
						int px = (t % tilesX) * size + x;
						int py = (t / tilesX) * size + y + startY;

						if ((source.rgb.get(px, py) & 0x000000FF) != 0)
							break search;
					}

				}

				search: for (int y = size - 1; y >= 0; y--) {
					data[heights+tile] = (byte) (y - data[offY1+tile] + 1);
					for (int x = 0; x < size; x++) {
						int px = (t % tilesX) * size + x;
						int py = (t / tilesX) * size + y + startY;

						if ((source.rgb.get(px, py) & 0x000000FF) != 0)
							break search;
					}

				}

				opaques[t] = 1;
				o++;
				search: for (int x = 0; x < data[widths+tile]; x++) {
					for (int y = 0; y < data[heights+tile]; y++) {
						int px = (t % tilesX) * size + x + data[offX1+tile];
						int py = (t / tilesX) * size + y + startY + data[offY1+tile];

						if ((source.rgb.get(px, py) & 0x000000FF) != 0x0FF) {
							opaques[t] = 0;
							o--;
							break search;
						}
					}

				}
				
				if (data[offX1+tile] != 0 || data[widths+tile] != size || data[offY1+tile] != 0 || data[heights+tile] != size)
					cropped++;

			}
			LOG.ln("tile " + size + ": " + tiles + " cropped: " + cropped + " opaque: " + o + ", ");

		}

		@Override
		public void save(FilePutter file) {
			file.bs(data);
			file.bs(opaques);

		}

		@Override
		public void load(FileGetter file) throws IOException {
			file.bs(data);
			file.bs(opaques);
		}

		@Override
		public void clear() {
			// TODO Auto-generated method stub

		}

	}

	@Override
	public void save(FilePutter file) {
		s8.save(file);
		s16.save(file);
		s24.save(file);
		s32.save(file);

	}

	@Override
	public void load(FileGetter file) throws IOException {
		s8.load(file);
		s16.load(file);
		s24.load(file);
		s32.load(file);
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

}
