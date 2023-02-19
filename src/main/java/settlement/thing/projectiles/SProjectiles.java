package settlement.thing.projectiles;

import java.io.IOException;

import init.C;
import init.D;
import settlement.army.Army;
import settlement.army.Div;
import settlement.main.*;
import settlement.main.SETT.SettResource;
import snake2d.Renderer;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.rnd.RND;
import util.dic.DicMisc;
import util.rendering.ShadowBatch;

public class SProjectiles extends SettResource {


	final Map map = new Map(SETT.PWIDTH, SETT.PHEIGHT);
	final PData data = new PData(map);
	private final Updater updater = new Updater(this);
	private final PRenderer ren = new PRenderer(this);
	
	public static CharSequence ¤¤OUT_OF_RANGE = "¤Out of range!";
	public static CharSequence ¤¤FRIENDLIES = "¤Ally subjects are in the trajectory and might get hit!";
	public static CharSequence ¤¤TERRAIN = "¤Trajectory blocked by terrain";
	
	static {
		D.ts(SProjectiles.class);
	}
	
	public SProjectiles() {

		
		new Test();
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		map.clear();
		data.saver.load(file);
		
	}
	
	@Override
	public void save(FilePutter file) {
		data.saver.save(file);
	}
	
	@Override
	protected void clearBeforeGeneration(CapitolArea area) {
		map.clear();
		data.saver.clear();
	}

	public void launch(int x, int y, int height, double dx, double dy, double dz, Projectile type, int level) {
		data.create(x, y, height, dx, dy, dz, type, level);
		
	}
	
	public void launch(int x, int y, int height, Trajectory t, Projectile type, int level) {
		data.create(x, y, height, t.vx(), t.vy(), t.vz(), type, level);
	}
	
	public void launchDummy(int x, int y, int height, Trajectory t, Projectile type, int level) {
		int i = data.create(x, y, height, t.vx(), t.vy(), t.vz(), type, level);
		if (i != -1)
			data.live(i, false);
	}
	
	public void launch(int x, int y, int height, Trajectory t, Projectile type, double ran, int level) {
		data.create(x, y, height, t.vx()*RND.rFloat1(ran), t.vy()*RND.rFloat1(ran), t.vz()*RND.rFloat1(ran), type, level);
	}
	
	@Override
	public void update(float ds) {

		for (int i = 0; i < data.last(); i++) {
			updater.update(i, ds);
		}
	}

	public void renderAbove(Renderer r, ShadowBatch s, float ds, int zoomout, RenderData renData) {
		
		ren.renderAbove(r, s, ds, zoomout, renData);
		
	}
	

	
	private static final Trajectory traj = new Trajectory();
	
	public static CharSequence problem(Div dd, Div target) {
		
		int tx = target.reporter.body().cX();
		int ty = target.reporter.body().cY();
		return problem(traj, dd, tx, ty);
	}
	
	public static CharSequence problem(Trajectory work, Div dd, int destX, int destY) {
		
		int startX = dd.reporter.body().cX();
		int startY = dd.reporter.body().cY();
		int fx = startX >> C.T_SCROLL;
		int fy = startY >> C.T_SCROLL;
		if (!SETT.IN_BOUNDS(fx, fy))
			return DicMisc.¤¤Problem;
		
		int tx = destX >> C.T_SCROLL;
		int ty = destY >> C.T_SCROLL;
		if (!SETT.IN_BOUNDS(tx, ty))
			return DicMisc.¤¤Problem;
		
		int h = SETT.TERRAIN().get(fx, fy).heightEnt(fx, fy)*C.TILE_SIZE + Trajectory.RELEASE_HEIGHT;
		h -= SETT.TERRAIN().get(tx, ty).heightEnt(tx, ty)*C.TILE_SIZE + Trajectory.HIT_HEIGHT/2;
		
		double speed = dd.settings.ammo().speed(dd);
		
		if (speed <= 0)
			return DicMisc.¤¤Problem;
		
		CharSequence problem = ¤¤OUT_OF_RANGE;
		
		if (work.calcLow(h, startX, startY, destX, destY, speed)) {
			for (int di = 0; di < DIR.NORTHO.size(); di++) {
				DIR d = DIR.ORTHO.get(di);
				int x = startX + d.x()*dd.reporter.body().width()/2;
				int y = startY + d.y()*dd.reporter.body().height()/2;
				problem = trajectoryProblem(dd.army(), work, x, y);
				if (problem == null)
					return null;
			}
		}else if (work.calcHigh(h, startX, startY, destX, destY, speed)) {
			for (int di = 0; di < DIR.NORTHO.size(); di++) {
				DIR d = DIR.ORTHO.get(di);
				int x = startX + d.x()*dd.reporter.body().width()/2;
				int y = startY + d.y()*dd.reporter.body().height()/2;
				problem = trajectoryProblem(dd.army(), work, x, y);
				if (problem == null)
					return null;
			}
		}
		
		return problem;
		
	}
	
	public static CharSequence problem(Army a, Trajectory work, int startX, int startY, int destX, int destY, double speed) {
		

		int fx = startX >> C.T_SCROLL;
		int fy = startY >> C.T_SCROLL;
		if (!SETT.IN_BOUNDS(fx, fy))
			return DicMisc.¤¤Problem;
		
		int tx = destX >> C.T_SCROLL;
		int ty = destY >> C.T_SCROLL;
		if (!SETT.IN_BOUNDS(tx, ty))
			return DicMisc.¤¤Problem;
		
		int h = SETT.TERRAIN().get(fx, fy).heightEnt(fx, fy)*C.TILE_SIZE + Trajectory.RELEASE_HEIGHT;
		h -= SETT.TERRAIN().get(tx, ty).heightEnt(tx, ty)*C.TILE_SIZE + Trajectory.HIT_HEIGHT/2;
		
		CharSequence problem = ¤¤OUT_OF_RANGE;
		
		if (work.calcLow(h, startX, startY, destX, destY, speed)) {
			problem = trajectoryProblem(a, work, startX, startY);
		}
		
		if (problem != null && work.calcHigh(h, startX, startY, destX, destY, speed)) {
			problem = trajectoryProblem(a, work, startX, startY);
		}
		
		return problem;
		
	}
	
	public static CharSequence trajectoryProblem(Army a, Trajectory traj, int sx, int sy) {
		int tx = sx>>C.T_SCROLL;
		int ty = sy>>C.T_SCROLL;
		int h = SETT.TERRAIN().get(tx, ty).heightEnt(tx, ty)*C.TILE_SIZE + Trajectory.RELEASE_HEIGHT;
		return Updater.test(a, traj, h, sx, sy);
	}
	
	public static int releaseHeight(int tx, int ty) {
		return SETT.TERRAIN().get(tx, ty).heightEnt(tx, ty)*C.TILE_SIZE + Trajectory.RELEASE_HEIGHT;
	}
	
	public static int hitHeight(int tx, int ty) {
		return SETT.TERRAIN().get(tx, ty).heightEnt(tx, ty)*C.TILE_SIZE + Trajectory.HIT_HEIGHT/2;
	}

	

}
