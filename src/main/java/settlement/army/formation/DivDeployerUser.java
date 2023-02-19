package settlement.army.formation;

import init.C;
import init.RES;
import settlement.army.*;
import settlement.army.formation.DivDeployer.DivDeployB;
import settlement.army.order.DivTDataTask;
import settlement.main.RenderData;
import settlement.main.SETT;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

public class DivDeployerUser {

	private final ArrayList<DivDeployB> all;
	private final ArrayList<DivDeployB> selection;
	public final DivDeployer deployer;
	private final int clampM = ~((C.TILE_SIZE-1)>>2);
	
	private ArrayList<DivDeployB> tmp = new ArrayList<>(ArmyManager.DIVISIONS);
	private ArrayList<DivDeployB> selected = new ArrayList<>(ArmyManager.DIVISIONS);
	private final VectorImp vec = new VectorImp();
	private static final ArrayList<Div> tmp2 = new ArrayList<>(1);
	private final DivFormation fTmp = new DivFormation();
	private final DivTDataTask task = new DivTDataTask();
	public DivDeployerUser(LIST<Army> armies, int max) {
		int size = ArmyManager.DIVISIONS;
		all = new ArrayList<>(size);
		selection = new ArrayList<>(size);
		for (Army a : armies) {
			for (Div d : a.divisions()) {
				DivDeployB dep = new DivDeployB();
				dep.div = d;
				all.add(dep);
			}
		}
		this.deployer = new DivDeployer(RES.pathTools(), max) {
			@Override
			protected boolean isDeployable(int px, int py) {
				return !blocked(px, py);
			};
		};
	}
	
	protected boolean blocked(int x, int y) {
		return false;
	}
	
	public void render(SPRITE_RENDERER ren, LIST<Div> divs, int x1, int x2, int y1, int y2, RenderData data) {
		x1 += (~clampM+1)/2;
		y1 += (~clampM+1)/2;
		x2 += (~clampM+1)/2;
		y2 += (~clampM+1)/2;
		
		x1 &= clampM;
		y1 &= clampM;
		x2 &= clampM;
		y2 &= clampM;
		
		selection.clear();
		for (Div d : divs) {
			if (d.menNrOf() == 0)
				continue;
			DivDeployB dep = all.get(d.index());
			dep.div = d;
			selection.add(dep);
		}
		
		LIST<DivDeployB> result = init(selection, x1, x2, y1, y2);
		
		for (DivDeployB b : result) {
			DivFormation d = deployer.deploy(b.div.menNrOf(),  b.div.settings.formation, b.x1, b.y1, b.dx, b.dy, b.width, SETT.ARMIES().player());
			DivRenderer.render(ren, d, data);
		}
	}
	
	public void render(SPRITE_RENDERER ren, DivFormation d, RenderData data) {
		DivRenderer.render(ren, d, data);
	}

	public static void addSecretBlocker(MAP_BOOLEAN block) {
		
	}
	
	public void deploy(LIST<Div> divs, int x1, int x2, int y1, int y2) {
		selection.clear();
		
		x1 += (~clampM+1)/2;
		y1 += (~clampM+1)/2;
		x2 += (~clampM+1)/2;
		y2 += (~clampM+1)/2;
		
		x1 &= clampM;
		y1 &= clampM;
		x2 &= clampM;
		y2 &= clampM;
		
		for (Div d : divs) {
			if (d.menNrOf() == 0)
				continue;
			DivDeployB dep = all.get(d.index());
			dep.div = d;
			selection.add(dep);
		}
		
		LIST<DivDeployB> result = init(selection, x1, x2, y1, y2);
		
		for (DivDeployB b : result) {
			DivFormation d = deployer.deploy(b.div.menNrOf(), b.div.settings.formation, b.x1, b.y1, b.dx, b.dy, b.width, SETT.ARMIES().player());
			
			if (d != null) {
				
				b.div.order().dest.set(d);
				task.move();
				b.div.order().task.set(task); 
				if (b.div.position().deployed() == 0) {
					b.div.position().copy(d);
				}
			}
			
			
		}
		
	}
	
	public void deploy(Div div, int x1, int x2, int y1, int y2) {
		tmp2.clear();
		tmp2.add(div);
		deploy(tmp2, x1, x2, y1, y2);
	}
	
	public void deploy(Div div, int dx, int dy) {

		
		div.order().dest.get(fTmp);
		DivFormation d = deployer.deploy(
				div.menNrOf(), 
				fTmp.formation(), 
				fTmp.start().x()+dx, fTmp.start().y()+dy, 
				fTmp.dx(), fTmp.dy(), fTmp.width(), SETT.ARMIES().player());
		
		if (d != null && d.deployed() != 0) {
			task.move();
			div.order().dest.set(d);
			div.order().task.set(task); 
			if (div.position().deployed() == 0) {
				div.position().copy(d);
			}
		}
		
	
	}

	public boolean isBlocked(int x, int y, int tileSize) {
		return DivPlacability.pixelIsBlocked(x, y, tileSize, SETT.ARMIES().player()) && !blocked(x, y);
	}
	
	private LIST<DivDeployB> init(LIST<DivDeployB> divs, int x1, int x2, int y1, int y2) {

		
		selected.clear();
		double distFull = vec.set(x1, y1, x2, y2);
		{
//			int baseSize = C.TILE_SIZEH;
//			int steps = (int) Math.ceil(distFull / baseSize);
//			double stepX = vec.nX() * baseSize;
//			double stepY = vec.nY() * baseSize;
//
//			steps = getSteps(steps, stepX, stepY, x1, y1, baseSize);
//			distFull = steps * baseSize;
			selected.clear();
			if (divs.size() * C.TILE_SIZE > distFull) {
				return selected;
			}
		}
		
		
		
		double menTotal = 0;
		{
			
			tmp.clear();
			for (DivDeployB d : divs) {
				menTotal += d.div.menNrOf();
				if (d.div.menNrOf() == 0)
					continue;
				tmp.add(d);

			}
		}
		{
			while (tmp.size() > 0) {
				double smallesD = Double.MAX_VALUE;
				int s = -1;
				for (int i = 0; i < tmp.size(); i++) {
					RECTANGLE d = tmp.get(i).div.position().body();
					double ddx = d.cX() - x1;
					double ddy = d.cY() - y1;
					double dist = Math.sqrt(ddx * ddx + ddy * ddy);
					if (dist < smallesD) {
						smallesD = dist;
						s = i;
					}
				}
				DivDeployB dr = tmp.get(s);
				selected.add(dr);
				tmp.remove(s);
			}
		}
		if (selected.isEmpty())
			return selected;
		{
			DIV_FORMATION lastF = selected.get(0).div.settings.formation;
			double distGaps = 0;
			for (DivDeployB d : selected) {
				if (d.div.settings.formation != lastF) {
					lastF = d.div.settings.formation;
					distGaps++;
				}
			}
			distFull-= distGaps*C.TILE_SIZEH;
		}

		
		
		double dx = x1;
		double dy = y1;
		double extra = 0;
		DIV_FORMATION lastF = selected.get(0).div.settings.formation;
		for (DivDeployB d : selected) {
			if (d.div.settings.formation != lastF) {
				lastF = d.div.settings.formation;
				dx += C.TILE_SIZEH*vec.nX();
				dy += C.TILE_SIZEH*vec.nY();
			}
			double dist = distFull*(d.div.menNrOf() / menTotal);
			dist += extra;
			extra = dist-(d.div.settings.formation.size * (int)(dist/d.div.settings.formation.size));
	
			d.width = (int) (dist);
			d.x1 = (int) dx;
			d.y1 = (int) dy;
			d.dx = vec.nX();
			d.dy = vec.nY();
		
			dx += (d.div.settings.formation.size * (int)(dist/d.div.settings.formation.size))*vec.nX();
			dy += (d.div.settings.formation.size * (int)(dist/d.div.settings.formation.size))*vec.nY();
		}
		


		return selected;
	}
	
	public void stop(LIST<Div> divs) {
//		for (Div d : divs) {
//			d.orders().lock(5);
//			if (d.orders().current().deployed() > 0 && d.orders().destination().deployed() > 0) {
//				d.order().destination.set(d.position());
//				d.orders().destination().copy(d.orders().current());
//				d.orders().path.clear();
//			}
//			d.orders().unlock();
//		}
	}


}
