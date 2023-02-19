package settlement.environment;

import static settlement.main.SETT.*;

import java.io.IOException;
import java.util.Arrays;

import game.time.TIME;
import settlement.environment.ENVIRONMENT.EnvResource;
import settlement.main.CapitolArea;
import settlement.main.SETT;
import settlement.path.finder.SFinderFindable;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import util.updating.IUpdater;

public final class SEService extends EnvResource{

	private final long[] day;
	private final long[] tries;
	private final long[] successes;
	public final static int quad = 16;
	private final int W = SETT.TWIDTH/quad;
	private final int H = SETT.THEIGHT/quad;;
	private final IUpdater upper;
	
	SEService() {
		
		int am = W*H;
		day = new long[am];
		tries = new long[am];
		successes = new long[am];
		upper = new IUpdater(am, TIME.days().bitSeconds()*2) {
			
			@Override
			protected void update(int i, double timeSinceLast) {
				int m = 1;
				for (int k = 0; k < SFinderFindable.all().size(); k++) {
					if ((day[i]<<k) != (TIME.days().bitsSinceStart() & 1)) {
						tries[i] &= ~m;
						successes[i] &= ~m;
					}
					m = m << 1;
				}
			}
		};
	}
	
	public void report(COORDINATE c, SFinderFindable access, boolean success) {
		report(c.x(), c.y(), access, success);
	}
	
	public void report(int tx, int ty, SFinderFindable access, boolean success) {
		if (!IN_BOUNDS(tx, ty))
			return;
		int m = 1 << access.index;
		int i = getI(tx, ty);
		if (!success || (tries[i] & m) == 0) {
			day[i] &= ~m;
			day[i] |= (TIME.days().bitsSinceStart()&1);
			tries[i] |= m;
			if (success)
				successes[i] |= m;
			else
				successes[i] &= ~m;
		}
	}
	
	public boolean has(SFinderFindable access, int tx, int ty) {
		int i = getI(tx, ty);
		int m = 1 << access.index;
		return (tries[i] & m) != 0;
	}
	
	public boolean is(SFinderFindable access, int tx, int ty) {
		int i = getI(tx, ty);
		int m = 1 << access.index;
		return (successes[i] & m) != 0;
	}
	
	private int getI(int tx, int ty) {
		tx /= quad;
		ty /= quad;
		return tx+ty*W;
	}

	@Override
	protected void update(double ds) {
		upper.update(ds);
	}

	@Override
	protected void generate(CapitolArea area) {
		Arrays.fill(tries, 0);
		Arrays.fill(successes, 0);
		Arrays.fill(day, 0);
	}

	@Override
	protected void save(FilePutter file) {
		file.ls(day);
		file.ls(tries);
		file.ls(successes);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		file.ls(day);
		file.ls(tries);
		file.ls(successes);
	}
	
}
