package settlement.thing.projectiles;

import game.boosting.BOOSTABLES;
import init.C;
import settlement.army.Army;
import settlement.army.DivMorale;
import settlement.army.ai.util.ArmyAIUtil;
import settlement.entity.ENTITY;
import settlement.entity.ENTITY.ECollision;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.util.CAUSE_LEAVE;
import settlement.thing.projectiles.PData.Data;
import settlement.tilemap.terrain.Terrain.TerrainTile;
import snake2d.util.GEO;
import snake2d.util.datatypes.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;

final class Updater {

	private final SProjectiles p;
	private static final VectorImp vec = new VectorImp();
	private final double max = 1/64.0;
	
	Updater(SProjectiles p) {
		this.p = p;
	}

	void update(final int i, float dd) {

		float ds = (float) max;
		while(dd > 0) {
			
			dd-=max;
			Data d = p.data.data(i);

			d.dxSet(d.dx() - (Float.floatToIntBits(d.dx()) >>> 31) * ds * Trajectory.FRICTION);
			d.dySet(d.dy() - (Float.floatToIntBits(d.dy()) >>> 31) * ds * Trajectory.FRICTION);
			d.dzSet(d.dz() - Trajectory.G * ds);

			float z = d.z() + d.dz() * ds;
			if (z <= 0) {
				ds *= d.z() / (d.z() - z);
			}

			float x = d.x() + d.dx() * ds;
			float y = d.y() + d.dy() * ds;
			d.zSet(z);

			if (d.z() <= 0) {
				if (collide(i, d, x, y, p.data.type(i)))
					return;
				p.data.type(i).soundHit().rnd((int) x, (int) y, 1);
				p.data.type(i).impact(p.data.ref(i), x, y, d.dx(), d.dy(), d.dz());
				p.data.remove(i);
				
				return;
			} 
			if (collide(i, d, x, y, p.data.type(i)))
				return;
			
			if (!p.data.move(i, x, y))
				return;
			
		}
		
		

	}

	private final Rec rTile = new Rec(C.TILE_SIZE);
	private static final ECollision coll = new ECollision();
	
	private boolean collide(int e, Data d, float destX, float destY, Projectile type) {

		destY /= C.TILE_SIZE;
		destX /= C.TILE_SIZE;

		final double startX = d.x() / C.TILE_SIZE;
		final double startY = d.y() / C.TILE_SIZE;

		if ((int) destX == (int) startX && (int) destY == (int) startY && d.z() > 0)
			return false;

		double dx = destX - startX;
		double dy = destY - startY;
		double adx = Math.abs(dx);
		double ady = Math.abs(dy);
		double mag = 0;
		if (adx > ady) {
			mag = adx;
		} else {
			if (ady <= 0)
				mag = 1;
			else
				mag = ady;
		}

		dx /= mag;
		dy /= mag;

		double x = startX;
		double y = startY;

		
		while (mag > 0) {
			double dd = CLAMP.d(mag, 0, 1);
			double ox = x;
			double oy = y;
			x += dx * dd;
			y += dy * dd;
			mag -= 1;
			int tx = (int) x;
			int ty = (int) y;
			
			if (!SETT.IN_BOUNDS(tx, ty)) {
				p.data.remove(e);
				return true;
			}
			TerrainTile t = SETT.TERRAIN().get(tx, ty);
			int min = t.heightStart(tx, ty) * C.TILE_SIZE;
			int max = t.heightEnd(tx, ty) * C.TILE_SIZE;
			if (d.z()<= 0 ||  (d.z() > min && d.z() < max)) {
				
				p.data.type(e).soundHit().rnd((int) (x * C.TILE_SIZE), (int) (y * C.TILE_SIZE), 1);
				double mom = Math.sqrt(d.dx()*d.dx()+d.dy()*d.dy()+ d.dz()*d.dz())*p.data.type(e).mass(p.data.ref(e));
				double str = SETT.ARMIES().map.strength.get(tx, ty);
				if (p.data.live(e) && mom > str*RND.rFloat()) {
					SETT.ARMIES().map.breakIt(tx, ty);
					double ddd = (mom-str)/mom;
					d.dxSet(d.dx()*ddd);
					d.dySet(d.dy()*ddd);
					continue;
				}
				
				type.impact(p.data.ref(e),  x*C.TILE_SIZE, y*C.TILE_SIZE, d.dx(), d.dy(), d.dz());
				
				
				if (RND.oneIn(5)) {

					
					double cx = tx + 0.5;
					double cy = ty + 0.5;
					double prevX = x - dx * dd;
					double prevY = y - dy * dd;
					double ddx = Math.abs(cx - prevX);
					double ddy = Math.abs(cy - prevY);
					double r = RND.rFloat();
					if (ddx > ddy) {
						d.dxSet(-r * d.dx());
						d.dySet(r * d.dy());
					} else {
						d.dxSet(r * d.dx());
						d.dySet(-r * d.dy());
					}
					p.data.move(e, prevX * C.TILE_SIZE, prevY * C.TILE_SIZE);
					
				} else {

					p.data.remove(e);

				}
				return true;
			}
			
			int hel = (int) (t.heightEnt(tx, ty) * C.TILE_SIZE);
			int heh = hel + Trajectory.HIT_HEIGHT;
			if (d.z() >= hel && d.z() < heh) {
				rTile.moveC(x*C.TILE_SIZE, y*C.TILE_SIZE);
				for (ENTITY ent : SETT.ENTITIES().fill(rTile)) {
					if (intesects(ent, ox, oy, x, y)) {
						
						double ref = p.data.ref(e);
						p.data.type(e).soundHit().rnd((int) (x * C.TILE_SIZE), (int) (y * C.TILE_SIZE), 1);
						double l = d.dx()*d.dx() + d.dy()*d.dy() + d.dz()*d.dz();
						l = Math.sqrt(l);
						
						double mom = type.mass(ref) * l;
						double str = mom*C.ITILE_SIZE;

						double momExtra = Math.min(mom, ent.physics.getMass()*C.TILE_SIZE*8.0)*RND.rFloat();
						mom+= momExtra;
						
						
						
						double momExchange = ent.physics.getMass()*C.TILE_SIZE*4;
						
						DIR od = ent.speed.dir();
						vec.set(ox*C.TILE_SIZE, oy*C.TILE_SIZE, ent.body().cX(), ent.body().cY());
						
						
						{
						
							double sdot = vec.nX()*ent.speed.nX() + vec.nY()*ent.speed.nY();
							mom -= sdot*ent.speed.magnitude()*ent.physics.getMass();
							if (mom < 0)
								mom = 0;
							double ds = mom*ent.physics.getMassI();
							double nY = vec.nY()*ds;
							double nX = vec.nX()*ds;
							ent.speed.setRaw(ent.speed.x()+nX, ent.speed.y()+nY);
							
						}
						

						double dot = (1 -od.xN()*vec.nX() -od.yN()*vec.nY())*0.5;
						

						if (ent instanceof Humanoid) {
							Humanoid a = (Humanoid) ent;
							str/= 1 + dot*BOOSTABLES.BATTLE().DEFENCE.get(a.indu());
							if (a.division() != null)
								DivMorale.PROJECTILES.incD(a.division(), 1);
						}
						coll.damageStrength = str;

						for (int i = 0; i < coll.damage.length; i++) {
							double da = type.damage(i, p.data.ref(e));
							coll.damage[i] = da;
						}
						
						coll.dirDot = dot;
						coll.momentum = mom;
						coll.norX = -vec.nX();
						coll.norY = -vec.nY();
						coll.leave = CAUSE_LEAVE.SLAYED;
						coll.speedHasChanged = true;
						coll.other = null;
						ent.collide(coll);
						
						
						if (ent.isRemoved() && mom - momExchange > 0) {
							mom-= momExchange;
							mom/= type.mass(ref);
							double ddd = mom/l;
							d.dxSet(d.dx()*ddd);
							d.dySet(d.dy()*ddd);
						}else {
							type.impact(ref, x*C.TILE_SIZE, y*C.TILE_SIZE, d.dx(), d.dy(), d.dz());
							p.data.remove(e);
							return true;
						}
						
						
					}
				}
			}
		}
		
		return false;
	}
	
	static CharSequence test(Army ally, Trajectory traj, double h, double sx, double sy) {
		
		double time = traj.getTime(h);
		float vz = (float) traj.vz();
		float vx = (float) traj.vx();
		float vy = (float) traj.vy();
		double mag = vec.set(vx, vy);
		double length = Trajectory.getLength(mag, time);
		
		length *= C.ITILE_SIZE;
		
		double ds = time/length;
		int imax = (int) Math.ceil(length);
		
		
		double x = sx;
		double y = sy;
		
		for (int i = 0; i < imax; i++) {
			
			x += vx*ds;
			y += vy*ds;
			h += vz*ds;
			if (h <= 0)
				return null;
			int tx = ((int) x)>>C.T_SCROLL;
			int ty = ((int) y)>>C.T_SCROLL;
			
			if (!SETT.IN_BOUNDS(tx, ty)) {
				return null;
			}
			TerrainTile t = SETT.TERRAIN().get(tx, ty);
			int min = t.heightStart(tx, ty) * C.TILE_SIZE;
			int max = t.heightEnd(tx, ty) * C.TILE_SIZE;
			
			if (h > min && h < max) {
				if (imax - i < 10)
					return null;
				return SProjectiles.¤¤TERRAIN;
			}
			int eh = t.heightEnt(tx, ty) * C.TILE_SIZE;
			if (h >= eh && h <= eh+Trajectory.HIT_HEIGHT) {
				if (ArmyAIUtil.map().hasAlly.is(tx,  ty, ally)) {
					return SProjectiles.¤¤FRIENDLIES;
				}
				if (ArmyAIUtil.map().hasEnemy.is(tx,  ty, ally))
					return null;
				for (int di = 0; di < DIR.ORTHO.size(); di++) {
					DIR dir = DIR.ORTHO.get(di);
					if (ArmyAIUtil.map().hasAlly.is(tx,  ty, dir, ally)) {
						return SProjectiles.¤¤FRIENDLIES;
					}
				}
			}
			
			
			vx -= (Float.floatToIntBits(vx) >>> 31) * ds * Trajectory.FRICTION;
			vy -= (Float.floatToIntBits(vy) >>> 31) * ds * Trajectory.FRICTION;
			vz -= Trajectory.G*ds;
		}
		
		return null;
		
	}

	private boolean intesects(ENTITY e, double ox, double oy, double nx, double ny) {
		double x1 = ox;   
		double y1 = oy;
		double x2 = nx;   
		double y2 = ny;

		double w = e.body().width()*C.ITILE_SIZE;
		double x = e.body().x1()*C.ITILE_SIZE;
		double y = e.body().y1()*C.ITILE_SIZE;
		if (GEO.collides(x1, y1, x2, y2, x, y, x+w, y))
			return true;
		if (GEO.collides(x1, y1, x2, y2, x+w, y, x+w, y+w))
			return true;
		if (GEO.collides(x1, y1, x2, y2, x+w, y+w, x, y+w))
			return true;
		if (GEO.collides(x1, y1, x2, y2, x, y+w, x, y))
			return true;
		return false;
	}

}
