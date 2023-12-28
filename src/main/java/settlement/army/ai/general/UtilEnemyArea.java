package settlement.army.ai.general;

import java.io.IOException;

import init.C;
import init.config.Config;
import settlement.army.Div;
import settlement.army.ai.util.DivTDataStatus;
import settlement.main.SETT;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.*;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.*;

/**
 * A thing that takes all enemy divs and flood fills them in a fancy way to create blobs surrounding them.
 * @author Jake
 *
 */
class UtilEnemyArea extends Bitmap2D{

	private final Context context;
	private final double tileRange = 32;
	private final Node[] nodes = new Node[Config.BATTLE.DIVISIONS_PER_ARMY];
	private final ArrayList<Node> anodes = new ArrayList<>(Config.BATTLE.DIVISIONS_PER_ARMY);
	private final DivTDataStatus status = new DivTDataStatus();
	private final Node[][] nmap = new Node[(int) Math.ceil(SETT.TWIDTH/tileRange)][(int) Math.ceil(SETT.THEIGHT/tileRange)];
	private final Rec rec = new Rec();

	public UtilEnemyArea(Context context) {
		super(SETT.TILE_BOUNDS, false);
		for (int i = 0; i < nodes.length; i++)
			nodes[i] = new Node();
		this.context = context;
	}
	
	void update(int range) {
	
		clear();

		for (int di = 0; di < Config.BATTLE.DIVISIONS_PER_ARMY; di++) {
			
			
			Div d = context.army.enemy().divisions().get(di);
			if (d.order().active()) {
				d.order().status.get(status);
				int tx = status.currentPixelCX()>>C.T_SCROLL;
				int ty = status.currentPixelCY()>>C.T_SCROLL;
				Node n = nodes[di];
				n.next = null;
				n.coo.set(tx, ty);
				anodes.add(n);
			}
		}
		
		
		
		if (anodes.size() > 0) {
			fill(context, anodes, range);
		}
	}
	
	@Override
	public void save(FilePutter file) {
		super.save(file);
		rec.save(file);
	}
	
	@Override
	public void load(FileGetter file) throws IOException {
		super.load(file);
		rec.load(file);
	}
	
	@Override
	public void clear() {
		anodes.clearSloppy();
		rec.setDim(1, 1).moveX1Y1(0, 0);
		super.clear();
	}
	
	public RECTANGLE area() {
		return rec;
	}
	
	private void fill(Context context, LIST<Node> nodes, int range) {
		
		
		if (context.army.men() == 0)
			return;
		
		
		
		
		
		for (int y = 0; y < nmap.length; y++) {
			for (int x = 0; x < nmap.length; x++) {
				nmap[y][x] = null;
			}
		}
		
		Flooder f = context.flooder.getFlooder();
		f.init(this);
		
		rec.setDim(1, 1).moveX1Y1(nodes.get(0).coo);
		
		for (Node n : nodes) {
			add(n, f);
		}
	
		while(f.hasMore()) {
			PathTile t = f.pollSmallest();
			
			if (t.getValue() > range) {
				f.pushSloppy(t.x(), t.y(), t.getValue());
				break;
			}
			set(t, true);
			rec.unify(t.x(), t.y());
			
			if (t.getValue() < 12) {
				for (int i = 0; i < DIR.ALL.size(); i++) {
					int dx = t.x()+DIR.ALL.get(i).x();
					int dy = t.y()+DIR.ALL.get(i).y();
					if (body().holdsPoint(dx, dy) && !SETT.PATH().solidity.is(dx, dy))
						if (f.pushSmaller(dx, dy, t.getValue()+1)!=null)
							f.setValue2(dx, dy, t.getValue2());
				}
			}else {
				for (int i = 0; i < DIR.ORTHO.size(); i++) {
					int dx = t.x()+DIR.ORTHO.get(i).x();
					int dy = t.y()+DIR.ORTHO.get(i).y();
					if (body().holdsPoint(dx, dy) && !SETT.PATH().solidity.is(dx, dy))
						if (f.pushSmaller(dx, dy, t.getValue()+1)!=null)
							f.setValue2(dx, dy, t.getValue2());
				}
			}
			
			
			
		}
	
		
		blurr();
		
		f.done();
	}
	
	private void blurr() {
		Flooder f = context.flooder.getFlooder();
		while(f.hasMore()) {
			PathTile t = f.pollSmallest();
			
			double v = 0;
			
			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				int dx = t.x()+DIR.ORTHO.get(i).x();
				int dy = t.y()+DIR.ORTHO.get(i).y();
				if (body().holdsPoint(dx, dy) && !SETT.PATH().solidity.is(dx, dy) && is(dx, dy)) {
					v += 0.5;
				}
			}
			if (v > 1.0) {
				rec.unify(t.x(), t.y());
				set(t, true);
			}
			
		}
	}
	
	private void add(Node n, Flooder f) {
		
		if (!SETT.IN_BOUNDS(n.coo))
			return;
		
		f.pushSloppy(n.coo, 0);
		for (int di = 0; di < DIR.ALLC.size(); di++) {
			DIR d = DIR.ALLC.get(di);
			int x = (int) (n.coo.x() + d.xN()*tileRange);
			int y = (int) (n.coo.y() + d.yN()*tileRange);
			if (SETT.IN_BOUNDS(x, y)) {
				int nx = (int) (x/tileRange);
				int ny = (int) (y/tileRange);
				Node other = nmap[ny][nx];
				add(n, other, f);
			}
		}
		
		int x = (int) (n.coo.x()/tileRange);
		int y = (int) (n.coo.y()/tileRange);
		
		n.next = nmap[y][x];
		nmap[y][x] = n;
		
		
		
	}
	
	private final double tileRange2 = tileRange*tileRange;
	
	private void add(Node n, Node other, Flooder f){
		
		while(other != null) {
			
			Node o = other;
			other = other.next;
			double dx = o.coo.x()-n.coo.x();
			double dy = o.coo.y()-n.coo.y();
			if (dx*dx+dy*dy > tileRange2) {
				continue;
			}
			double step = Math.max(Math.abs(dx), Math.abs(dy));
			if (step <= 0)
				continue;
			
			dx /= step;
			dy /= step;
			
			if (testLine(n, step, dx, dy))
				addLine(n, f, step, dx, dy);
				
				
			
		}
	}
	
	private boolean testLine(Node n, double step, double dx, double dy) {
		double x = n.coo.x()+0.5;
		double y = n.coo.y()+0.5;
		for (double d = 0; d < step; d++) {
			
			int fx = (int) x;
			int fy = (int) y;
			x += dx;
			y += dy;
			int tx = (int) x;
			int ty = (int) y;
			if (fx == tx && fy == ty)
				continue;
			if (!SETT.IN_BOUNDS(tx, ty))
				return false;
			if (SETT.PATH().solidity.is(tx, ty) || SETT.PATH().solidity.is(fx, ty) || SETT.PATH().solidity.is(tx, fy))
				return false;
		}
		return true;
	}
	
	private void addLine(Node n, Flooder f, double step, double dx, double dy) {
		double x = n.coo.x()+0.5;
		double y = n.coo.y()+0.5;
		
		
		
		for (double d = 0; d < step; d++) {
			x += dx;
			y += dy;
			int tx = (int) x;
			int ty = (int) y;
			f.pushSloppy(tx, ty, 0);
		}
	}
	
	private static class Node {
		
		Node next;
		final Coo coo = new Coo();
		
		
		Node(){

		}
		
		
	}

}
