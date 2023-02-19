package settlement.army.formation;

import java.io.IOException;

import init.C;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.file.*;

public class DivPositionAbs implements SAVABLE{
	
	private int deployed = 0;	
	private final int half;
	private final int[] coos;
	private final Coo coo = new Coo();
	
	DivPositionAbs(int maxMen) {
		coos = new int[maxMen*2];
		half = maxMen;
	}
	
	public COORDINATE tile(int i) {
		if (i >= deployed)
			return null;
		coo.set(coos[i]>>C.T_SCROLL, coos[i+half]>>C.T_SCROLL);
		return coo;
	}

	public COORDINATE pixel(int i) {
		if (i >= deployed)
			return null;
		coo.set(coos[i], coos[i+half]);
		return coo;
	}
	
	public void init(int men) {
		this.deployed = men;
	}
	
	public void set(int i, int x, int y){
		coos[i] = x;
		coos[i+half] = y;
	}
	
	@Override
	public void save(FilePutter file) {
		file.i(deployed);
		file.is(coos);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		deployed = file.i();
		file.is(coos);
	}
	
	@Override
	public void clear() {
		deployed = 0;
	}

	public int deployed() {
		return deployed;
	}

	public void copyy(DivPositionAbs pos) {
		this.deployed = pos.deployed;
		for (int i = 0; i < deployed; i++) {
			coos[i] = pos.coos[i];
			coos[i+half] = pos.coos[i+half];
		}
	}
	
}
