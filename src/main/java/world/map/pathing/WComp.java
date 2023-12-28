package world.map.pathing;

import java.io.IOException;

import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import world.WORLD;

final class WComp {

	public final short id,x,y;
	private final short[] toDist;
	
	WComp(int id, int x, int y, short[] dists){
		this.id = (short) id;
		this.x = (short) x;
		this.y = (short) y;
		toDist = dists;
	}
	
	WComp(FileGetter file) throws IOException {
		id = file.s();
		x = file.s();
		y = file.s();
		toDist = new short[file.i()];
		file.ss(toDist);
	}
	
	public int edges() {
		return toDist.length >> 1;
	}
	
	public WComp edge(int ei) {
		return WORLD.PATH().COMPS.getByID(toDist[(ei<<1)]);
	}
	
	public int dist(int ei) {
		return toDist[(ei<<1)+1];
	}

	public void save(FilePutter file) {
		file.s(id);
		file.s(x);
		file.s(y);
		file.i(toDist.length);
		file.ss(toDist);
	}

	
}
