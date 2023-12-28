package settlement.army.ai.util;

import java.util.Arrays;

import init.C;
import settlement.army.Div;
import settlement.army.DivMorale;
import settlement.army.formation.DivFormation;
import settlement.army.formation.DivPosition;
import settlement.main.SETT;
import settlement.room.main.ROOMS;
import settlement.room.military.artillery.ArtilleryInstance;
import settlement.room.military.artillery.ROOM_ARTILLERY;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListResize;

final class Updater {

	private final DivPosition pos = new DivPosition();
	private final DivFormation form = new DivFormation();
	private final ArrayList<Div> list = new ArrayList<>(16);
	
	Updater(){

	}
	
	void init(ArmyAIUtil u) {
		
		u.map.clear();
		u.quads.clear();
		u.space.clear();
		for (DivTDataStatus s : u.statuses) {
			s.clear();
		}
		
		setInPosition(u);
		addToMaps(u);
		setStats(u);
		addArtillery(u);
		
	}

	private void setInPosition(ArmyAIUtil u) {
		for (short k = 0; k < u.statuses.length; k++) {
			Div d = SETT.ARMIES().division(k);
			if (!d.order().active())
				continue;
			d.order().next.get(form);
			d.order().current.get(pos);
			int am = 0;
			for (int i = 0; i < form.deployed() && i < pos.deployed(); i++) {
				
				if (form.pixel(i).tileDistanceTo(pos.pixel(i)) < C.TILE_SIZE)
					am++;
			}
			u.statuses[k].inPosition = (short) am;
		}
	}
	
	
	private void addToMaps(ArmyAIUtil u) {
		for (short i = 0; i < u.statuses.length; i++) {
			Div d = SETT.ARMIES().division(i);
			
			
			if (!d.order().active())
				continue;
			d.order().next.get(form);
			d.order().current.get(pos);
			u.map.add(i, pos);
			u.space.add(d, pos);
			
			int xx = 0;
			int yy = 0;
			int am = 0;
			
			for (int pi = 0; pi < pos.deployed(); pi++) {
				xx += pos.pixel(pi).x();
				yy += pos.pixel(pi).y();
				am++;
			}
			
			if (am == 0) {
				u.statuses[i].cx = -1;
				u.statuses[i].cy = -1;
			}else {
				xx /= am;
				yy /= am;
				
				int best = -1;
				int bestV = Integer.MAX_VALUE;
				
				for (int pi = 0; pi < pos.deployed(); pi++) {
					int dist = Math.abs(xx-pos.pixel(pi).x()) + Math.abs(yy-pos.pixel(pi).y());
					if (dist < bestV) {
						best = pi;
						bestV = dist;
					}
				}
				
				xx = pos.pixel(best).x();
				yy = pos.pixel(best).y();
			}
			
			u.statuses[i].cx = xx;
			u.statuses[i].cy = yy;
			u.quads.add(d, xx, yy);
		}
	}

	private final VectorImp vec = new VectorImp();
	private final double[] threatsides = new double[DIR.ALLC.size()];
	
	private void setStats(ArmyAIUtil u) {
		
		for (short i = 0; i < u.statuses.length; i++) {
			
			Div d = SETT.ARMIES().division(i);
			if (!d.order().active()) {
				DivMorale.IN_FORMATION.setD(d, 0);
				DivMorale.SITUATION.setD(d, 0);
				DivMorale.FLANKS.setD(d, 0);
				continue;
			}
			DivTDataStatus s = u.statuses[i];
			
			byte threatDirs = 0;
			double inFormation = 0;
			double flanks = 0;
			double enemyThreats = 0;
			double friends = 0;
			Arrays.fill(threatsides, 0);
			s.isFighting = false;
			
			
			list.clear();
			u.quads.getNearest(list, s.currentPixelCX(), s.currentPixelCY(), 256*C.TILE_SIZE, d.armyEnemy(), d);
			double distMax = C.TILE_SIZE*64;
			distMax *= distMax;
			double distMin = distMax*0.5;
			
			for (int ii = 0; ii < list.size(); ii++) {
			
				Div e = list.get(ii);
				int dx = u.statuses[e.index()].currentPixelCX()-s.currentPixelCX();
				int dy = u.statuses[e.index()].currentPixelCY()-s.currentPixelCY();
				double dist = dx*dx+dy*dy;
				
				if (dist < distMax) {
					double threat = Math.max(e.settings.power*(1.0 - Math.sqrt(dist/distMax)), 0);
					if (dist < distMin) {
						DIR dir = DIR.get(dx, dy);
						if (dir.isOrtho())
							threatDirs |= dir.mask();
						else
							threatDirs |= dir.mask()<<4;
						threatsides[dir.id()] += threat;
					}
					
					
					
					
					
					
					enemyThreats += threat;
					
				}
				
				s.enemiesClosestSet(list.get(ii).index());
			}
			
			
			list.clear();
			u.quads.getNearest(list, s.currentPixelCX(), s.currentPixelCY(), 300*C.TILE_SIZE, d.army(), d);
			
			for (int ii = 0; ii < list.size(); ii++) {
				Div e = list.get(ii);
				s.friendlyClosestSet(e.index());
				
				int dx = u.statuses[e.index()].currentPixelCX()-s.currentPixelCX();
				int dy = u.statuses[e.index()].currentPixelCY()-s.currentPixelCY();
				double dist = dx*dx+dy*dy;
				
				if (dist < distMax) {
					friends += Math.max(e.settings.power*(1.0 - Math.sqrt(dist/distMax)), 0);
				}
				
				
			}
			
			{
				double enemies = threatsides[DIR.C.id()];
				for (int di = 0; di < DIR.ALL.size(); di++) {
					enemies += threatsides[di];
					for (int di2 = 0; di2 < DIR.ALL.size(); di2++) {
						DIR da = DIR.ALL.get(di);
						DIR db = DIR.ALL.get(di2);
						double dot = da.xN()*db.xN() + da.xN()*db.xN();
						//double dist = COORDINATE.properDistance(da.xN(), da.yN(), db.xN(), db.yN());
						if (dot < 0 && threatsides[di] >  threatsides[di2]) {
							enemies += threatsides[di2]*-dot*2;
						}
					}
				}
				enemyThreats = enemies;
				
				friends += d.settings.power;
				
				
				if (friends > 0) {
					enemyThreats /= friends;
				}
				enemyThreats = CLAMP.d(enemyThreats, 0, 100000);
			}
			
			
			{
				d.order().next.get(form);
				if (form.deployed() != 0) {
					d.order().current.get(pos);
					double am = Math.min(form.deployed(), pos.deployed());
					for (int pi = 0; pi < am; pi++) {
						double dist = form.pixel(pi).tileDistanceTo(pos.pixel(pi));
						if (dist < C.TILE_SIZE)
							inFormation += 1;
						else
							inFormation += CLAMP.d(1-(dist-C.TILE_SIZE)/(C.TILE_SIZE), 0, 1);
					}
					if (am > 0) {
						inFormation /= am;
					}
					inFormation = CLAMP.d(inFormation, 0, 1);
					
					for (int pi = 0; pi < pos.deployed(); pi++) {
						if (u.map.hasEnemy.is(pos.tile(pi), d.army())) {
							if (vec.set(form.centrePixel(), pos.pixel(pi)) == 0) {
								flanks++;
							}else {
								double dot = vec.nX()*form.dir().xN() + vec.nY()*form.dir().yN();
								if (dot < 0)
									flanks += -dot*10;
							}
							s.isFighting = true;
						}
						
					}
					if (form.deployed() > 0)
						flanks /= form.deployed();
					
					DivMorale.IN_FORMATION.setD(d, CLAMP.d(inFormation, 0, 1));
					DivMorale.SITUATION.setD(d, CLAMP.d(enemyThreats, 0, 10000));
					DivMorale.FLANKS.setD(d, CLAMP.d(flanks, 0, 1));
				}
			}
			
			
			
			d.settings.enemyDirMask = threatDirs;
			
			
			
		}
	}
	
	private final ArrayListResize<ArtilleryInstance> arts = new ArrayListResize<>(256, ROOMS.ROOM_MAX);
	
	private void addArtillery(ArmyAIUtil u) {
		
		for (int bi = 0; bi < SETT.ROOMS().ARTILLERY.size(); bi++) {
			ROOM_ARTILLERY ab = SETT.ROOMS().ARTILLERY.get(bi);
			arts.clearSoft();
			ab.threadInstances(arts);
			for (ArtilleryInstance ins : arts) {
				u.quads.addArtillery(ins);
			}
		}
	}
	
	
}
