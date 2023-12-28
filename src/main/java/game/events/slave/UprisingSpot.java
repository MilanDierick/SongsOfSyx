package game.events.slave;

import static settlement.main.SETT.*;

import java.io.IOException;

import game.VERSION;
import init.C;
import init.config.Config;
import init.race.Race;
import settlement.army.Div;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.*;
import settlement.main.SETT;
import snake2d.util.datatypes.*;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;

final class UprisingSpot extends Coo{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int signedUp;
	int amountTotal;
	int race;


	private UprisingSpot(){
		
	}
	
	static UprisingSpot make(int mx, int my, int amountTotal, Race race) {
		UprisingSpot s = new UprisingSpot();
		s.amountTotal = amountTotal;
		s.race = race.index;
		if (setStart(s, mx, my, 32))
			return s;
		return null;
	}
	
	static UprisingSpot make(FileGetter g) throws IOException {
		UprisingSpot s = new UprisingSpot();
		s.load(g);
		return s;
	}

	void makeDiv(Div d, int id) {
		
		if (!isPlacable(x(), y())) {
			 if (!setStart(this, x(), y(), 16)) {
				 clear();
				 return;
			 }
		}
		d.settings.musteringSet(true);
		
		
		int am = CLAMP.i(signedUp, 0, Config.BATTLE.MEN_PER_DIVISION-d.menNrOf());
		int w = (int) Math.ceil(Math.sqrt(signedUp)/2);
		
		for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
			if (am == 0)
				break;
			if (e instanceof Humanoid) {
				Humanoid a = (Humanoid) e;
				if (a.race().index == race && HPoll.Handler.isSlaveReadyForUprising(a) == id) {
					a.HTypeSet(HTYPE.ENEMY, null, null);
					a.setDivision(d);
					am--;
					signedUp--;
					amountTotal--;
				}
			}
		}
		DIR dir = DIR.NW;
		
		int x1 = (x()+dir.x()*w) *C.TILE_SIZE+C.TILE_SIZEH;
		int y1 = (y()+dir.y()*w)*C.TILE_SIZE+C.TILE_SIZEH;
		dir = DIR.NE;
		int x2 = x1 + dir.x()*w*C.TILE_SIZE;
		int y2 = y1 + dir.y()*w*C.TILE_SIZE;
		
		ARMIES().placer.deploy(d, x1, x2, y1, y2);
		
		

	}
	
	@Override
	public void save(FilePutter file) {
		file.i(signedUp);
		file.i(amountTotal);
		file.i(race);
		super.save(file);
	}
	
	@Override
	public void load(FileGetter file) throws IOException {
		signedUp = file.i();
		amountTotal = file.i();
		if (!VERSION.versionIsBefore(64, 27))
			race = file.i();
		super.load(file);
	}
	
	@Override
	public void clear() {
		signedUp = 0;
		amountTotal = 0;
		super.clear();
	}
	
	
	static boolean setStart(COORDINATEE res, int sx, int sy, int dist) {
		for (int i = 0; i < 100000; i++) {
			int distX = RND.rInt(dist+1);
			int distY = dist+1 - distX;
			int tx = (int) (sx + RND.rSign()*(distX + RND.rInt(1+i)));
			int ty = (int) (sy + RND.rSign()*(distY + RND.rInt(1+i)));
			if (SETT.ENV().environment.SPACE.get(tx, ty) == 1 && isPlacable(tx, ty)) {
				res.set(tx, ty);
				return true;
			}
		}
		return false;
	}
	
	private static boolean isPlacable(int cx, int cy) {
		
		int amount = Config.BATTLE.MEN_PER_DIVISION;
		int w = (int) Math.ceil(Math.sqrt(amount));
		
		for (int y = 0; y < w; y++) {
			for (int x = 0; x < w; x++) {
				if (!placable(cx-w/2+x, cy-w/2+y))
					return false;
			}
		}
		return true;
	}
	
	private static boolean placable(int tx, int ty) {

		if (!IN_BOUNDS(tx, ty))
			return false;
		
		if (PATH().solidity.is(tx, ty) || !PATH().reachability.is(tx, ty)) {
			return false;
		}
	
		return true;
	}

	


	
}
