package snake2d.util.sets;
import java.io.IOException;

import snake2d.util.datatypes.*;
import snake2d.util.file.*;
import snake2d.util.map.MAP_BOOLEANE;

public class Bitmap2D implements MAP_BOOLEANE, BODY_HOLDER, SAVABLE{
	
	private final Bitmap1D data;
	private final boolean outof;
	private final Rec body;
	private final int width;
	
	public Bitmap2D(int width, int height, boolean outof){
		this.body = new Rec(width, height);
		this.data = new Bitmap1D(width*height, outof);
		this.width = width;
		this.outof = outof;
	}
	
	public Bitmap2D(DIMENSION body, boolean outof){
		this.body = new Rec(body.width(), body.height());
		this.width = body.width();
		this.data = new Bitmap1D(body.width()*body.height(), outof);
		this.outof = outof;
	}

	@Override
	public boolean is(int tile) {
		return data.get(tile);
	}

	@Override
	public boolean is(int tx, int ty) {
		if (body.holdsPoint(tx, ty))
			return data.get(tx+ty*width);
		return outof;
	}

	@Override
	public MAP_BOOLEANE set(int tile, boolean value) {
		data.set(tile, value);
		return this;
	}

	@Override
	public MAP_BOOLEANE set(int tx, int ty, boolean value) {
		if (body.holdsPoint(tx, ty))
			data.set(tx+ty*width, value);
		return this;
	}

	@Override
	public RECTANGLE body() {
		return body;
	}

	@Override
	public void save(FilePutter file) {
		data.save(file);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		data.load(file);
	}

	@Override
	public void clear() {
		data.clear();
	}


	
}
