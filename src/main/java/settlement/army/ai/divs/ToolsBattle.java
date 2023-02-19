package settlement.army.ai.divs;

import static settlement.army.ai.divs.Plans.Plan.*;

import init.C;
import settlement.army.Div;
import settlement.army.ai.util.ArmyAIUtil;
import settlement.army.ai.util.DivTDataStatus;
import settlement.army.formation.*;
import settlement.army.order.DivTDataInfo;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.sets.ArrayList;

final class ToolsBattle {

	private final Tools t;
	private static final VectorImp vec = new VectorImp();
	private final ArrayList<Div> res = new ArrayList<>(8);
	private final DivTDataStatus ostatus = new DivTDataStatus();
	private final DivTDataInfo oInfo = new DivTDataInfo();
	ToolsBattle(Tools t){
		this.t = t;
	}
	
	public VectorImp getThreatDirection() {
		
		double cx = 0;
		double cy = 0;
		int ci = 0;
		double ex = 0;
		double ey = 0;
		int ei = 0;
		res.clear();
		status.enemyCollisions(res);
		for (Div d : res) {
			d.order().status.get(ostatus);
			d.order().info.get(oInfo);
			ex += ostatus.currentPixelCX()*info.men;
			ey += ostatus.currentPixelCY()*info.men;
			ei+=info.men;
		}
		
		for (int i = 0; i < current.deployed(); i++) {
			cx += current.pixel(i).x();
			cy += current.pixel(i).y();
			ci++;
			
//			for (int di = 0; di < DIR.ALLC.size(); di++) {
//				DIR d = DIR.ALLC.get(di);
//				if (ArmyAIUtil.map().hasEnemy.is(current.tile(i), d, a)){
//					ex += ((current.tile(i).x()+d.x())<<C.T_SCROLL)+C.TILE_SIZEH;
//					ey += ((current.tile(i).y()+d.y())<<C.T_SCROLL)+C.TILE_SIZEH;
//					ei++;
//				}
//			}
		}
		
		if (ci == 0 || ei == 0) {
			return nextVec();
		}
		
		cx /= ci;
		cy /= ci;
		ex /= ei;
		ey /= ei;
		
		if (vec.set(cx,  cy, ex, ey) == 0)
			return nextVec();
		
		return vec;
		
	}
	
	private VectorImp nextVec() {
		DivFormation next = fallback();
		if (next == null) {
			vec.set(0, 1);
			return vec;
		}
		vec.set(next.dx(), next.dy());
		vec.rotate90().rotate90().rotate90();
		return vec;
	}

	private DivFormation fallback() {
		if (next.deployed() > 0)
			return next;
		if (dest.deployed() > 0)
			return dest;
		return null;
	}
	
	public DivFormation getBestGuard(DivPositionAbs current) {
		
		double cx = 0;
		double cy = 0;
		int ci = 0;
	
		for (int i = 0; i < current.deployed(); i++) {
			cx += current.pixel(i).x();
			cy += current.pixel(i).y();
			ci++;
		}
		if (ci == 0) {
			return next;
		}
		
		cx /= ci;
		cy /= ci;
		
		VectorImp dir = getThreatDirection();
		dir.rotate90();
		int rm = 0;
		{
			DivFormation ff = fallback();
			if (ff != null)
				rm = ff.width();
			else
				rm = (int) (info.men- Math.sqrt(info.men)*1.5)*settings.formation.size;
		}
		
		{
			int mm = rm/settings.formation.size;
			if (mm == 0 || info.men / mm <= 2) {
				rm = (info.men/3)*settings.formation.size;
			}
			
		}
			
		
		
		DivFormation f = t.deployer.deployArroundCentre(info.men, settings.formation, (int)cx, (int)cy, dir.nX(), dir.nY(), rm, a);
		if (f == null) {
			int bestI = 0;
			double bestDist = Integer.MAX_VALUE;
			for (int i = 0; i < current.deployed(); i++) {
				double d = current.pixel(i).tileDistanceTo(cx, cy);
				d /= 1 + Math.abs(i)/10.0;
				if (d < bestDist) {
					bestI = i;
					bestDist = d;
				}
			}
			cx = current.pixel(bestI).x();
			cy = current.pixel(bestI).y();
			f = t.deployer.deployArroundCentre(info.men, settings.formation, (int)cx, (int)cy, dir.nX(), dir.nY(), rm, a);
			if (f == null)
				return next;
		}
		
		double best = 0;
		double bestF = 0;
		
		dir.rotate90().rotate90().rotate90();
		
		for (int i = 2; i > -5; i--) {
			double v = getValue(f, i*dir.nX()*C.TILE_SIZEH, i*dir.nY()*C.TILE_SIZEH); 
			
			if (v == -1)
				break;

			if (v > best) {
				bestF = i;
				best = v;
			}
		}
		
		if (bestF != 0) {

		
			cx += bestF*dir.nX()*C.TILE_SIZEH;
			cy += bestF*dir.nY()*C.TILE_SIZEH;
			f = t.deployer.deployArroundCentre(info.men, settings.formation, (int)cx, (int)cy, f.dx(), f.dy(), rm, a);
		}
		
		return f;
		
	}
	
	public DivFormation getBestAttack(DivPositionAbs current) {
		
		double cx = 0;
		double cy = 0;
		int ci = 0;
	
		for (int i = 0; i < current.deployed(); i++) {
			cx += current.pixel(i).x();
			cy += current.pixel(i).y();
			ci++;
		}
		if (ci == 0) {
			return next;
		}
		
		cx /= ci;
		cy /= ci;
		
		VectorImp dir = getThreatDirection();
		dir.rotate90();
		int rm = 0;
		{
			DivFormation ff = fallback();
			if (ff != null)
				rm = ff.width();
			else
				rm = (int) (info.men- Math.sqrt(info.men)*1.5)*settings.formation.size;
		}
		{
			int mm = rm/settings.formation.size;
			if (info.men / mm <= 2) {
				rm = (info.men/3)*settings.formation.size;
			}
			
		}
		
		
		
		DivFormation f = t.deployer.deployArroundCentre(info.men, settings.formation, (int)cx, (int)cy, dir.nX(), dir.nY(), rm, a);
		if (f == null) {
			int bestI = 0;
			double bestDist = Integer.MAX_VALUE;
			for (int i = 0; i < current.deployed(); i++) {
				double d = current.pixel(i).tileDistanceTo(cx, cy);
				d /= 1 + Math.abs(i)/10.0;
				if (d < bestDist) {
					bestI = i;
					bestDist = d;
				}
			}
			cx = current.pixel(bestI).x();
			cy = current.pixel(bestI).y();
			f = t.deployer.deployArroundCentre(info.men, settings.formation, (int)cx, (int)cy, dir.nX(), dir.nY(), rm, a);
		}
		
		double best = 0;
		double bestF = 0;
		
		dir.rotate90().rotate90().rotate90();
		
		for (int i = 0; i < 5; i++) {
			double v = getValue2(f, i*dir.nX()*C.TILE_SIZEH, i*dir.nY()*C.TILE_SIZEH); 
			
			if (v == -1)
				break;

			if (v > best) {
				bestF = i;
				best = v;
			}
		}
		
		for (int i = 0; i > -5; i--) {
			double v = getValue2(f, i*dir.nX()*C.TILE_SIZEH, i*dir.nY()*C.TILE_SIZEH); 
			
			if (v == -1)
				break;

			if (v > best) {
				bestF = i;
				best = v;
			}
		}
		
		if (bestF != 0) {

		
			cx += bestF*dir.nX()*C.TILE_SIZEH;
			cy += bestF*dir.nY()*C.TILE_SIZEH;
			f = t.deployer.deployArroundCentre(info.men, settings.formation, (int)cx, (int)cy, f.dx(), f.dy(), rm, a);
		}
		
		return f;
		
	}

	public DivFormation getAdvanced(DivFormation f) {
		
		double best = 0;
		double bestF = 0;
		VectorImp dir = vec;
		dir.set(f.dx(), f.dy());
		dir.rotate90().rotate90().rotate90();
		COORDINATE c = t.div.getSafeCentrePixel(f);
		int cx = c.x();
		int cy = c.y();
		for (int i = 0; i <= 4; i++) {
			double v = getValue(f, i*dir.nX()*C.TILE_SIZEH, i*dir.nY()*C.TILE_SIZEH);
			if (v == -1)
				break;
			if (v > best) {
				bestF = i;
				best = v;
			}
		}
		if (best == 0)
			return null;
		
		
		if (bestF != 0) {
			cx += bestF*dir.nX()*C.TILE_SIZEH;
			cy += bestF*dir.nY()*C.TILE_SIZEH;
			f = t.deployer.deployArroundCentre(info.men, settings.formation, (int)cx, (int)cy, f.dx(), f.dy(), f.width(), a);
			return f;
		}
		
		return null;
		
	}
	
	public double getValue(DivFormation f, double dx, double dy) {
		
		boolean anyPass = false;
		
		double maxEdges = 0;
		double edge = 0;
		double mid = 0;
		
		for (int i = 0; i < f.deployed(); i++) {
			int fx = f.pixel(i).x();
			int fy = f.pixel(i).y();
			int tx = (int) (fx+dx);
			int ty = (int) (fy+dy);
			if (!DivPlacability.pixelIsBlocked(fx, fy, f.formation().size, a)) {
				anyPass = true;
			}

			if (ArmyAIUtil.map().hasEnemy.is(tx>>C.T_SCROLL, ty >>C.T_SCROLL, a)){
				if (f.isEdge(i))
					edge++;
				else
					mid++;
			}
			if (f.isEdge(i)) {
				maxEdges ++;	
			}
			
		}
		
		double res = 0;
		if (!anyPass) {
			res = -1;
		}else if (maxEdges == 0) {
			res = 1.0 / (1+mid);
		}else {
			res = 1.0 + (2*edge/(maxEdges+1))/(1+4*mid);
		}
		return res;
	}
	
	public double getValue2(DivFormation f, double dx, double dy) {
		
		boolean anyPass = false;
		
		double coll = 0;
		
		for (int i = 0; i < f.deployed(); i++) {
			int fx = f.pixel(i).x();
			int fy = f.pixel(i).y();
			int tx = (int) (fx+dx);
			int ty = (int) (fy+dy);
			if (!DivPlacability.pixelIsBlocked(fx, fy, f.formation().size, a)) {
				anyPass = true;
			}

			if (ArmyAIUtil.map().hasEnemy.is(tx>>C.T_SCROLL, ty >>C.T_SCROLL, a)){
				coll++;
			}
		
			
		}
		
		if (!anyPass)
			return -1;
		return coll;
	}
	

	

	
}
