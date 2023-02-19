package init.sprite;

import java.io.IOException;
import java.nio.file.Path;

import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.*;
import snake2d.util.map.MAP_BOOLEANE;

public class BitmapSprite implements MAP_BOOLEANE, SAVABLE {

	public static final int WIDTH = 12;
	public static final int HEIGHT = 12;
	public static final int AREA = WIDTH * HEIGHT;

	private final short[] data = new short[AREA / 16];

	public BitmapSprite() {

	}

	public void paint(short[] data) {
		for (int i = 0; i < this.data.length; i++)
			this.data[i] = data[i];
	}

	@Override
	public boolean is(int tx, int ty) {
		if (tx >= WIDTH || tx < 0 || ty >= HEIGHT || ty < 0)
			return false;
		return is(tx + ty * HEIGHT);
	}

	@Override
	public boolean is(int tile) {
		int i = tile / 16;
		int k = tile % 16;
		int ki = 1 << k;
		return (data[i] & ki) == ki;
	}

	@Override
	public MAP_BOOLEANE set(int tx, int ty, boolean value) {
		set(tx + ty * WIDTH, value);
		return this;
	}

	@Override
	public MAP_BOOLEANE set(int tile, boolean value) {
		int i = tile / 16;
		int k = tile % 16;
		int ki = 1 << k;
		if (value)
			data[i] |= ki;
		else
			data[i] &= ~ki;
		return this;
	}

	@Override
	public void save(FilePutter file) {
		file.ss(data);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		file.ss(data);
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	public void scaled(SPRITE_RENDERER r, int sx, int sy, int scale, COLOR foreground, COLOR borderN, COLOR borderS) {

		OPACITY.O99.bind();
		int hs = scale / 2;

		for (int y = -1; y <= HEIGHT; y++) {
			for (int x = -1; x <= WIDTH; x++) {
				if (is(x, y)) {
					foreground.render(r, sx + x * scale, sx + x * scale + scale, sy + y * scale,
							sy + y * scale + scale);
				} else {
					for (DIR d : DIR.ORTHO) {
						if (is(x, y, d)) {
							int dx = hs * ((1 + d.x()) / 2);
							int dy = hs * ((1 + d.y()) / 2);
							COLOR c = d.x() < 0 || d.y() < 0 ? borderN : borderS;
							c.render(r, sx + x * scale + dx, sx + x * scale + dx + hs * (1 + Math.abs(d.y())),
									sy + y * scale + dy, sy + y * scale + dy + hs * (1 + Math.abs(d.x())));
						}
					}

					for (DIR d : DIR.NORTHO) {
						if (is(x, y, d)) {
							int dx = hs * ((1 + d.x()) / 2);
							int dy = hs * ((1 + d.y()) / 2);
							COLOR c = d.x() < 0 || d.y() < 0 ? borderN : borderS;
							c.render(r, sx + x * scale + dx, sx + x * scale + dx + hs, sy + y * scale + dy,
									sy + y * scale + dy + hs);
						}
					}
				}

			}
		}
		OPACITY.unbind();
	}
	
	public static short[][] read(Path path) {
		SnakeImage im = new SnakeImage(path);
		int w = (im.width-2)/(WIDTH+2);
		int h = (im.height-2)/(HEIGHT+2);
		
		short[][] datas = new short[w*h][AREA/16];
		
		int di = 0;
		for (int fy = 0; fy < h; fy ++) {
			for (int fx = 0; fx < w; fx ++) {
				int sx = 2 +fx*(WIDTH+2);
				int sy = 2 +fy*(HEIGHT+2);
				int shortI = 0;
				int bitI = 0;
				for (int y = 0; y < HEIGHT; y++) {
					for (int x = 0; x < WIDTH; x++) {
						int px = sx+x;
						int py = sy+y;
						if (((im.rgb.get(px, py) >> 8)&0x00FFFFFF) == 0) {
							datas[di][shortI] |= (1<<bitI); 
						}
						bitI++;
						if (bitI >= 16) {
							bitI = 0;
							shortI ++;
						}
					}
				}
				di++;
			}
		}
		im.dispose();
		
		return datas;
	}

}
