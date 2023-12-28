package settlement.army.ai.general;

import java.io.IOException;

import settlement.army.Army;
import settlement.army.formation.DivDeployer;
import settlement.main.SETT;
import settlement.room.main.throne.THRONE;
import snake2d.PathUtilOnline;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.*;
import snake2d.util.sets.Bitmap2D;

final class Context implements SAVABLE{

	public final Army army = SETT.ARMIES().enemy();
	public final PathUtilOnline flooder = new PathUtilOnline(SETT.TWIDTH);
	public final DivDeployer deployer = new DivDeployer(flooder);
	public final MDivs divs = new MDivs(this);
	public final Bitmap2D blockedMap = new Bitmap2D(SETT.TILE_BOUNDS, false);
	public final UtilEnemyArea blob = new UtilEnemyArea(this);
	public final UtilLines preLines = new UtilLines(this);
	public final UtilDivMap finder = new UtilDivMap();
	public final UtilDeployer divDeployer = new UtilDeployer(this);
	public final UtilShouldRange shouldRange = new UtilShouldRange();
	public Context() {

	}
	
	public COORDINATE getDestCoo() {
		return THRONE.coo();
	}

	@Override
	public void save(FilePutter file) {
		file.mark(divs);
		divs.saver.save(file);
		blockedMap.save(file);
		file.mark(blob);
		blob.save(file);
		file.mark(preLines);
		preLines.saver.save(file);
		file.mark(preLines);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		file.check(divs);
		divs.saver.load(file);
		blockedMap.load(file);
		file.check(blob);
		blob.load(file);
		file.check(preLines);
		preLines.saver.load(file);
		file.check(preLines);
	}

	@Override
	public void clear() {
		divs.saver.clear();
		blockedMap.clear();
		blob.clear();
		preLines.saver.clear();
	}
	
}
