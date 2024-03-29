package settlement.army;

import java.io.IOException;

import game.GAME;
import game.boosting.*;
import init.C;
import init.config.Config;
import init.race.Race;
import settlement.army.ai.fire.DivTrajectory;
import settlement.army.ai.util.DivTDataStatus;
import settlement.army.formation.DivFormation;
import settlement.army.formation.DivPosition;
import settlement.army.order.DivTData;
import settlement.main.SETT;
import settlement.stats.colls.StatsBattle.HDivStat;
import settlement.stats.equip.EquipRange;
import snake2d.util.datatypes.*;
import snake2d.util.file.*;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.Bitmap1D;
import util.gui.misc.GBox;
import view.main.VIEW;
import world.army.AD;

public final class Div implements BOOSTABLE_O{

	private final short index;
	private final short armyIndex;
	private final Army army;
	private final DivFormation positions;
	private final DivMen men;
	public final DivTargets targets;
	public final DivMorale morale = new DivMorale(this);
	
	private double nextOrderTime = 0;
	private byte state = 0;
	private final DivPosition current;
	public double speed = 0.4*C.TILE_SIZE;;
	public double aRef = 0;
	public final DivInfo info;
	public final DivSettings settings = new DivSettings(this);
	public final DivTrajectory trajectory = new DivTrajectory();
	
	private short ni = -1;
	private short ii = -1;
	private short ti = -1;
	
	
	public final DivReporter reporter = new DivReporter();

	
	Div(ArrayList<Div> all, ArrayList<Div> armyAll, Army army, int maxMen){
		this.index = (short) all.add(this);
		armyIndex = (short) armyAll.add(this);
		this.army = army;
		positions = new DivFormation(maxMen);
		men = new DivMen(maxMen);
		
		current = new DivPosition(maxMen);
		targets = new DivTargets(this);
		info = new DivInfo(this);
	}
	
	public DivMen men() {
		return men;
	}
	

	
	public DivFormation position() {
		return positions;
	}

	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			positions.save(file);
			men.save(file);
			file.d(nextOrderTime);
			file.d(speed);
			current.save(file);
			morale.saver.save(file);
			targets.saver().save(file);
			info.saver.save(file);
			settings.save(file);
			reporter.unreachablem.save(file);
			file.s(reporter.unreachable);
			trajectory.save(file);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			positions.load(file);
			men.load(file);
			nextOrderTime = file.d();
			speed = file.d();
			current.load(file);
			morale.saver.load(file);
			targets.saver().load(file);
			info.saver.load(file);
			settings.load(file);
			reporter.unreachablem.load(file);
			reporter.unreachable = file.s();
			trajectory.load(file);
		}
		
		@Override
		public void clear() {
			positions.clear();
			men.clear();
			current.clear();
			morale.saver.clear();
			targets.saver().clear();
			info.saver.clear();
			settings.clear();
			reporter.unreachablem.clear();
			reporter.unreachable = 0;
			trajectory.clear();
			speed = 0.4*C.TILE_SIZE;
		}
	};
	
	public short index() {
		return index;
	}
	
	public short indexArmy() {
		return armyIndex;
	}
	
	public Army army() {
		return army;
	}
	
	public Army armyEnemy() {
		return SETT.ARMIES().armies().getC(army.index()+1);
	}
	
	public int menNrOf() {
		return men.men();
	}
	
	public int deployed() {
		return positions.deployed();
	}
	
	public DIR dir() {
			
		return positions.dir();
	}
	
	public void initPosition() {
		order().dest.get(positions);
		order().next.set(positions);
	}
	
	public final class DivReporter extends HDivStat{
		
		private final Bitmap1D unreachablem = new Bitmap1D(Config.BATTLE.MEN_PER_DIVISION, false);
		private short unreachable;
		
		public COORDINATE getTile(short spot) {
			return positions.tile(men.getSpot(spot));
		}
		
		public COORDINATE getPixel(short spot) {
			return positions.pixel(men.getSpot(spot));
		}
		
		public RECTANGLE body() {
			return positions.body();
		}
		
		public COORDINATE getDestTile() {
			return positions.centreTile();
		}
		
		public void reportPosition(short spot, int x, int y) {
			current.set(men.getSpot(spot), x, y);
			nextOrderTime = 0.1;
		}
		
		@Override
		public short signUpAndGetPosition(int x, int y, Race r) {
			if (menNrOf() == 0 && army() != SETT.ARMIES().player()) {
				info.race.set(r);
				morale.init();
			}
			nextOrderTime = 10;
			army.menInc(1);
			short sp = men.getNewSpot();
			reportPosition(sp, x, y);
			return sp;
		}
		
		@Override
		public void returnPosition(short pos) {
			army.menInc(-1);
			men.returnSpot(pos);
			nextOrderTime = 10;
			if (men.men() == 0)
				settings.musteringSet(false);
			reportReachable(pos, true);
			nextOrderTime = 0.1;
		}
		
		public void reportReachable(short spot, boolean reachable) {
			if (unreachablem.get(spot) == true)
				unreachable --;
			unreachablem.set(spot, !reachable);
			if (!reachable)
				unreachable ++;
			nextOrderTime = 0.1;
		}
		
		public int unreachable() {
			return unreachable;
		}
		
	}
	
	public DivTData order() {
		return SETT.ARMY_AI().orders.get(index);
	}


	
	private final static DivTDataStatus statustmp = new DivTDataStatus();
	
	void update(double ds) {
		
		
		if (settings.mustering() || settings.musteringChange) {
			settings.musteringChange = false;
			current.init(menNrOf());
			order().update(this);
			if (order().next.isNew(ni)) {
				ni = (short) order().next.setI();
				order().next.get(positions);

				state++;
			}
			if (order().status.isNew(ii)) {
				ii = (short) order().status.setI();
				order().status.get(statustmp);
				settings.isFighting = statustmp.enemyCollisions() > 0;
				settings.power = (float) AD.UTIL().power.get(this);
				EquipRange e = settings.ammo();
				if (e != null)
					settings.powerRanged = (float) AD.UTIL().power.rangedPower(e, e.stat().div().getD(this), e.boostable.get(this));
				else
					settings.powerRanged = 0;
			}
			if (order().trajectory.isNew(ti)) {
				ti = (short) order().trajectory.setI();
				order().trajectory.get(trajectory);
			}
			
		}
		
		
		
		speed = 0.4; 
		if (settings.charging)
			speed = 0.9;
		else if (settings.running)
			speed = 0.7;
		
		nextOrderTime += ds;
		if (nextOrderTime > 1) {
			morale.update(ds);
			nextOrderTime = 0;
			
			speed *= BOOSTABLES.PHYSICS().SPEED.get(this)*C.TILE_SIZE;
			EquipRange am = settings.ammo();
			if (am != null)
				aRef = am.boostable.get(this);
			else
				settings.powerRanged = 0;
			
			if (DivMorale.PROJECTILES.getD(this) > (menNrOf()>>1) || settings.isFighting())
				speed *= 0.75;
		}
		
		
	}

	public byte state() {
		return state;
	}
	
	public void hoverInfo(GBox text) {
		VIEW.b().hoverer.hoverInfo(text, this);
	}
	
	public DivPosition current() {
		return current;
	}

	public void debug() {
		String s = System.lineSeparator();
		String res = "";
		res += "Div: " + index + " " + armyIndex + s;
		res += "Men: " + menNrOf() + " Deployed:" + deployed() + s;
		res += "Current: " + current.deployed() + " " + " " + s;
		
		GAME.Notify(res);
	}

	@Override
	public double boostableValue(Boostable bo, BValue v) {
		return v.vGet(this);
	}
	
}
