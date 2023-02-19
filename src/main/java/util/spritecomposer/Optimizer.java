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

	private final TextureCoords.Imp coos = new TextureCoords.Imp();

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

	class Tile implements SAVABLE {

		final int startY, tilesX, size;
		private final byte[] offX1, widths, offY1, heights, opaques;
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
			offX1 = new byte[s];
			widths = new byte[s];
			offY1 = new byte[s];
			heights = new byte[s];
			opaques = new byte[s];
			tiles = s;
			tScroll = Integer.numberOfTrailingZeros(tilesX);
			tMask = tilesX - 1;
		}

		public void render(SPRITE_RENDERER r, int tile, int x1, int y1, int scale) {
			if (tile < 0)
				return;

			// if (opaques[tile] == 1) {
			// int tx = tile % tilesX;
			// int ty = tile / tilesX;
			// int px = (tx * size);
			// int py = startY + (ty * size);
			// CORE.renderer().renderTile(
			// x1, x1+scale*size, y1, y1+scale*size,
			// TextureCoords.Normal.get(px, py, size, size)
			// );
			// return;
			// }

			if (widths[tile] <= 0 || heights[tile] <= 0)
				return;

			int tx = tile & tMask;
			int ty = tile >> tScroll;

			int px = (tx * size);
			int py = startY + (ty * size);

			px += offX1[tile];
			py += offY1[tile];

			x1 = x1 + offX1[tile] * scale;
			int x2 = x1 + widths[tile] * scale;

			y1 = y1 + offY1[tile] * scale;
			int y2 = y1 + heights[tile] * scale;

			// if (opaques[tile] == 1) {
			// r.renderTileOpaque(x1, x2, y1, y2, TextureCoords.Normal.get(px, py,
			// widths[tile], heights[tile]));
			// }else {
			// r.renderTile(x1, x2, y1, y2, TextureCoords.Normal.get(px, py, widths[tile],
			// heights[tile]));
			// }

			r.renderSprite(x1, x2, y1, y2, TextureCoords.Normal.get(px, py, widths[tile], heights[tile]));

		}

		public void renderTextured(TextureCoords t, int tile, int x1, int y1, int scale) {
			if (tile < 0)
				return;

			if (widths[tile] <= 0 || heights[tile] <= 0)
				return;

			int tx = tile & tMask;
			int ty = tile >> tScroll;

			int px = (tx * size);
			int py = startY + (ty * size);

			px += offX1[tile];
			py += offY1[tile];

			x1 = x1 + offX1[tile] * scale;
			int x2 = x1 + widths[tile] * scale;

			y1 = y1 + offY1[tile] * scale;
			int y2 = y1 + heights[tile] * scale;

			coos.get(t.x1() + offX1[tile], t.y1() + offY1[tile], widths[tile], heights[tile]);

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
					TextureCoords.Normal.get(px, py, widths[tile], heights[tile]));

		}

		private void optimize(SnakeImage source) {

			int o = 0;
			int cropped = 0;

			for (int t = 0; t < tiles; t++) {

				searchx1: for (int x = 0; x < size; x++) {
					offX1[t] = (byte) x;
					for (int y = 0; y < size; y++) {
						int px = (t % tilesX) * size + x;
						int py = (t / tilesX) * size + y + startY;

						if ((source.rgb.get(px, py) & 0x000000FF) != 0)
							break searchx1;
					}
				}

				searchW: for (int x = size - 1; x >= 0; x--) {
					widths[t] = (byte) (x - offX1[t] + 1);
					for (int y = 0; y < size; y++) {
						int px = (t % tilesX) * size + x;
						int py = (t / tilesX) * size + y + startY;

						if ((source.rgb.get(px, py) & 0x000000FF) != 0)
							break searchW;
					}

				}

				search: for (int y = 0; y < size; y++) {
					offY1[t] = (byte) y;
					for (int x = 0; x < size; x++) {
						int px = (t % tilesX) * size + x;
						int py = (t / tilesX) * size + y + startY;

						if ((source.rgb.get(px, py) & 0x000000FF) != 0)
							break search;
					}

				}

				search: for (int y = size - 1; y >= 0; y--) {
					heights[t] = (byte) (y - offY1[t] + 1);
					for (int x = 0; x < size; x++) {
						int px = (t % tilesX) * size + x;
						int py = (t / tilesX) * size + y + startY;

						if ((source.rgb.get(px, py) & 0x000000FF) != 0)
							break search;
					}

				}

				opaques[t] = 1;
				o++;
				search: for (int x = 0; x < widths[t]; x++) {
					for (int y = 0; y < heights[t]; y++) {
						int px = (t % tilesX) * size + x + offX1[t];
						int py = (t / tilesX) * size + y + startY + offY1[t];

						if ((source.rgb.get(px, py) & 0x000000FF) != 0x0FF) {
							opaques[t] = 0;
							o--;
							break search;
						}
					}

				}
				
				if (offX1[t] != 0 || widths[t] != size || offY1[t] != 0 || heights[t] != size)
					cropped++;

			}
			LOG.ln("tile " + size + ": " + tiles + " cropped: " + cropped + " opaque: " + o + ", ");

		}

		@Override
		public void save(FilePutter file) {
			file.bs(offX1);
			file.bs(widths);
			file.bs(offY1);
			file.bs(heights);
			file.bs(opaques);

		}

		@Override
		public void load(FileGetter file) throws IOException {
			file.bs(offX1);
			file.bs(widths);
			file.bs(offY1);
			file.bs(heights);
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
