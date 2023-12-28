package settlement.army.ai.general;

import java.io.IOException;

import game.time.TIME;
import init.C;
import init.config.Config;
import settlement.army.Div;
import settlement.army.ai.general.UtilLines.Line;
import settlement.army.ai.util.DivTDataStatus;
import settlement.main.SETT;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.*;
import snake2d.util.sets.ArrayList;

final class MDivs {

	public final ArrayList<MDiv> allDivs = new ArrayList<>(Config.BATTLE.DIVISIONS_PER_ARMY);
	public final ArrayList<MDiv> activeDivs = new ArrayList<>(Config.BATTLE.DIVISIONS_PER_ARMY);
	private final DivTDataStatus status = new DivTDataStatus();

	
	MDivs(Context context){
		for (int i = 0; i < Config.BATTLE.DIVISIONS_PER_ARMY; i++) {
			allDivs.add(new MDiv(context.army.divisions().get(i), this));
//			all.add(new LineDivs(i));
		}

	}
	
	void init() {
		activeDivs.clearSloppy();
		for (MDiv d : allDivs) {
			if (d.init()) {
				activeDivs.add(d);
			}
		}

	}

	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			file.i(activeDivs.size());
			for (MDiv d : activeDivs)
				file.i(d.div.indexArmy());
			for (MDiv d : allDivs)
				d.save(file);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			activeDivs.clearSloppy();
			int am = file.i();
			for (int i = 0; i < am; i++) {
				activeDivs.add(allDivs.get(file.i()));
			}
			for (MDiv d : allDivs)
				d.load(file);
		}
		
		@Override
		public void clear() {
			for (MDiv d : allDivs)
				d.clear();
			activeDivs.clearSloppy();
		}
	};
	
	final class MDiv implements SAVABLE{
		
		public final Div div;
		
		public MDiv(Div div, MDivs data){
			this.div = div;
		}
		
		public int lineBack;
		public int lineI = -1;
		double distance = 0;
		boolean isDeployed;
		int men;
		int tx;
		int ty;
		int destX;
		int destY;
		DIR destDir;
		private boolean wasInited = false;
		public double inPositionSince;
		public double projectiles;
		public double busyUntil = 0;
		boolean ranged = false;
		
		@Override
		public void save(FilePutter file) {
			file.i(lineBack);
			file.i(lineI);
			file.d(distance);
			file.bool(isDeployed);
			file.i(men);
			file.i(tx);
			file.i(ty);
			file.i(destX);
			file.i(destY);
			DIR.save(destDir, file);
			file.bool(wasInited);
			file.d(inPositionSince);
			file.d(projectiles);
			file.d(busyUntil);
			file.bool(ranged);
		}

		@Override
		public void load(FileGetter file) throws IOException {
			lineBack = file.i();
			lineI = file.i();
			distance = file.d();
			isDeployed = file.bool();
			men = file.i();
			tx = file.i();
			ty = file.i();
			destX = file.i();
			destY = file.i();
			destDir = DIR.load(file);
			wasInited = file.bool();
			inPositionSince = file.d();
			projectiles = file.d();
			busyUntil = file.d();
			ranged = file.bool();
		}

		@Override
		public void clear() {
			busyUntil = 0;
			inPositionSince = 0;
			projectiles = 0;
		}
		
		public Line line(UtilLines lines){
			if (lineI == -1)
				return null;
			return lines.line(lineI);
		}
		
		private boolean init() {
			lineI = -1;
			distance = 0;
			isDeployed = false;
			men = div.menNrOf();
			lineBack = 0;
			destX = -1;
			if (busyUntil > TIME.currentSecond())
				return false;
			
			if (div.order().active()) {
				div.order().status.get(status);
				tx = status.currentPixelCX()>>C.T_SCROLL;
				ty = status.currentPixelCY()>>C.T_SCROLL;
				ranged = div.settings.ammo() != null;
				if (men > 0 && SETT.IN_BOUNDS(tx, ty)) {
					if (!wasInited) {
						clear();
					}
					wasInited = true;
					return true;
					
				}
			}
			wasInited = false;
			return false;
			
		}


	}
	
}
