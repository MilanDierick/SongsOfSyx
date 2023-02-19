package settlement.path;

import static settlement.main.SETT.*;

import java.io.IOException;
import java.util.Arrays;

import snake2d.Path.COST;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.*;
import snake2d.util.map.MAP_DOUBLE;
import util.updating.IUpdater;

public final class PlayerHuristics implements COST{

	private final byte[] counts = new byte[TAREA];
	public final MAP_DOUBLE getter = new MAP_DOUBLE() {
		
		@Override
		public double get(int tx, int ty) {
			return get(tx+ty*TWIDTH);
		}
		
		@Override
		public double get(int tile) {
			return I*(counts[tile] & 0x0FF);
		}
	};
	
	private final IUpdater updater = new IUpdater(TAREA, 4*256) {
		
		@Override
		protected void update(int i, double timeSinceLast) {
			if (counts[i] != 0)
				counts[i] = (byte) ((counts[i]& 0x0FF) / 16);
		}
	};
	
	public PlayerHuristics() {

		
	}
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			file.bs(counts);
			updater.save(file);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			file.bs(counts);
			updater.load(file);
		}
		
		@Override
		public void clear() {
			Arrays.fill(counts, (byte)0);
			updater.clear();
		}
	};
	
	public void set(int tx, int ty) {
		int i = tx+ty*TWIDTH;
		if (counts[i] != -1)
			counts[i] = (byte) ((counts[i] & 0x0FF) + 1);
	}

	void update(double ds) {
		
		updater.update(ds);
	}
	
	public double getCost(int fromX, int fromY, DIR d) {
		return getCost(fromX, fromY, fromX+d.x(), fromY+d.y());
	}
	
	private static double I = 1.0/512.0;
	
	@Override
	public double getCost(int fromX, int fromY, int toX, int toY) {

		AVAILABILITY a = PATH().getAvailability(toX, toY);
		if (a.player < 0) {
			return BLOCKED;
		}
		if (fromX != toX && fromY != toY) {
			if (PATH().getAvailability(fromX, toY).player <= -1 || PATH().getAvailability(toX, fromY).player <= -1) {
				return SKIP;
			}
		}
		
		int i = toX + toY*TWIDTH;
		double pen = 1.0 + (counts[i] != 0 ? (counts[i] & 0x0FF)*I : 0.0);
		
		return (a.player*pen + PATH().getAvailability(fromX, fromY).from);
		
	}
	
	
	
}
