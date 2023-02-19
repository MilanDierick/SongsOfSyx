package settlement.thing.projectiles;

import game.GameDisposable;
import init.C;
import init.RES;
import init.boostable.BOOSTABLES;
import init.sound.SoundSettlement.Sound;
import settlement.entity.ENTITY;
import settlement.entity.ENTITY.ECollision;
import settlement.main.SETT;
import settlement.stats.CAUSE_LEAVE;
import snake2d.Renderer;
import snake2d.util.datatypes.Rec;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.file.Json;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayListResize;
import snake2d.util.sets.INDEXED;
import util.dic.DicMisc;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import util.rendering.ShadowBatch;

public abstract class Projectile implements INDEXED{

	static final ArrayListResize<Projectile> ALL = new ArrayListResize<>(8, Byte.MAX_VALUE);
	static {
		new GameDisposable() {
			
			@Override
			protected void dispose() {
				ALL.clear();
			}
		};
	}
	
	public final byte index;
	public final double velocity;
	public final double accuracy;
	public final double reloadSpeed;
	public final Sound soundRelease;
	public final Sound soundHit;
	public final double mass,pierce,areaAttack;

	
	public Projectile(Json json) {
		if (json.has("PROJECTILE"))
			json = json.json("PROJECTILE");
		if (!ALL.hasRoom())
			json.error("Too many projectiles declared! max is " + Byte.MAX_VALUE, "PROJECTILE");
		index = (byte) ALL.add(this);
		velocity = json.d("TILE_SPEED", 0.5, 250)*C.TILE_SIZE;
		soundRelease = RES.sound().settlement.action.getByKey("SOUND_RELEASE", json);
		reloadSpeed = json.d("RELOAD_PER_MINUTE", 0.01, 10000);
		soundHit = RES.sound().settlement.action.getByKey("SOUND_HIT", json);
		accuracy = json.d("ACCURACY", 0.01, 1);
		mass = json.d("MASS", 0.01, 100);
		pierce = json.d("PIERCE_DAMAGE", 0, 100000);
		areaAttack = json.dTry("TILE_RADIUS_DAMAGE", 0, 10000, 0)*C.TILE_SIZE;
	}
	
	@Override
	public int index() {
		return index;
	}
	
	public double mass(int level) {
		return mass;
	}

	public double pierce(int level) {
		return pierce;
	}

	public double areaAttack(int level) {
		return areaAttack;
	}
	
	public abstract void render(Renderer r, ShadowBatch s, double x, double y, int h, int ran, double dx, double dy, double dz, float ds, int zoomout);
	
	private static final Rec pixels = new Rec();
	private static final VectorImp iVec = new VectorImp();
	private static final VectorImp tVec = new VectorImp();
	private static ECollision coll = new ECollision();
	
	public void impact(int level, double cx, double cy, double dx, double dy, double dz) {
		double areaAttack = areaAttack(level);
		if (areaAttack <= 0)
			return;
		
		if (dx == 0 && dy == 0 && dx == 0)
			return;
		
		double mass = mass(level);
		double mag = iVec.set(dx, dy)*mass;
		
		SETT.THINGS().gore.debris((int)cx, (int)cy, dx*0.5, dy*0.5);
		SETT.GRASS().current.increment((int)cx, (int)cy, -0.5);
		
		pixels.setDim(areaAttack*2);
		pixels.moveC(cx, cy);
		
		for (ENTITY e : SETT.ENTITIES().fill(pixels)) {
			double l = tVec.set(cx, cy, e.body().cX(), e.body().cY());
			if (l > areaAttack)
				continue;
			l = 1.0 - (l / areaAttack);
			double pX = dz*tVec.nX()*mass;
			double pY = dz*tVec.nY()*mass;
			double dot = iVec.nX()*tVec.nX() + iVec.nY()*tVec.nY();
			if (dot > 0) {
				pX += mag*iVec.nX()*dot;
				pY += mag*iVec.nY()*dot;
			}
			pX*= l*RND.rFloat1(0.1);
			pY*= l*RND.rFloat1(0.1);
			double m = tVec.set(pX, pY);
			e.speed.setRaw(e.speed.x()+pX*e.physics.getMassI(), e.speed.y()+pY*e.physics.getMassI());
			coll.pierceDamage = pierce(level);
			coll.dirDot = dot;
			coll.momentum = m;
			coll.norX = tVec.nX();
			coll.norY = tVec.nY();
			coll.leave = CAUSE_LEAVE.SLAYED;
			e.collide(coll);
		}
		
		for (int tdy = (int) -areaAttack; tdy <= areaAttack; tdy+=C.TILE_SIZE) {
			for (int tdx = (int) -areaAttack; tdx <= areaAttack; tdx+=C.TILE_SIZE) {
				int x = (int) (cx + tdx);
				int y = (int) (cy + tdy);
				double l = tVec.set(cx, cy, x, y);
				if (l > areaAttack)
					continue;
				l = 1.0 - (l / areaAttack);
				l*= mag;
				double str = SETT.ARMIES().map.strength.get(x>>C.T_SCROLL, y>>C.T_SCROLL);
				if (l*RND.rFloat() > str) {
					SETT.ARMIES().map.breakIt(x>>C.T_SCROLL, y>>C.T_SCROLL);
				}
			}
		}
		
		
	}
	
	public void hover(GUI_BOX box, double bonusVel, double bonusAcc) { 
		GBox b = (GBox) box;
		b.textL(DicMisc.¤¤Range);
		b.tab(6);
		b.add(GFORMAT.f(b.text(), Trajectory.range(0, velocity*bonusVel)/C.TILE_SIZE));
		b.NL();
		
		b.textL(DicMisc.¤¤Mass);
		b.tab(6);
		b.add(GFORMAT.f(b.text(), mass));
		b.NL();
	
		b.textL(BOOSTABLES.BATTLE().PIERCE_DAMAGE.name);
		b.tab(6);
		b.add(GFORMAT.f(b.text(), pierce));
		b.NL();
		
		b.textL(DicMisc.¤¤Accuracy);
		b.tab(6);
		b.add(GFORMAT.perc(b.text(), 1.0-(1.0-accuracy)/(1+bonusAcc)));
		b.NL();
	}
	
}
