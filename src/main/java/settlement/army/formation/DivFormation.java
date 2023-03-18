package settlement.army.formation;

import java.io.IOException;

import init.config.Config;
import settlement.army.order.Copyable;
import settlement.main.SETT;
import snake2d.PathUtilOnline.Filler;
import snake2d.util.datatypes.*;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;

public class DivFormation extends DivPositionAbs implements Copyable<DivFormation>, BODY_HOLDER{

	private DIR faceDir = DIR.N;
	private final Rec bounds = new Rec();
	final Coo start = new Coo();
	private double dx;
	private double dy;
	private int width;

	private DIV_FORMATION ts = DIV_FORMATION.LOOSE;
	private int centreI = -1;
	private boolean hasExtraRoom = false;

	private final byte[] dirMasks;
	
	public DivFormation() {
		this(Config.BATTLE.MEN_PER_DIVISION);
	}
	
	public DivFormation(int maxMen) {
		super(maxMen);
		dirMasks = new byte[maxMen];
	}


	public DIR dir() {
		return faceDir;
	}
	
	@Override
	public void save(FilePutter file) {
		super.save(file);
		file.i(faceDir.id());
		bounds.save(file);
		start.save(file);
		file.i(centreI);
		file.d(dx);
		file.d(dy);
		file.i(ts.ordinal());
		file.bs(dirMasks);
		file.bool(hasExtraRoom);
		file.i(width);
		
	}

	@Override
	public void load(FileGetter file) throws IOException {
		super.load(file);
		faceDir = DIR.ALL.get(file.i());
		bounds.load(file);
		start.load(file);
		centreI = file.i();
		dx = file.d();
		dy = file.d();
		ts = DIV_FORMATION.all.get(file.i());
		file.bs(dirMasks);
		hasExtraRoom = file.bool();
		width = file.i();
	}
	
	@Override
	public void clear() {
		super.clear();
		bounds.set(SETT.PWIDTH+1, -1, SETT.PHEIGHT+1, -1);
		ts = DIV_FORMATION.LOOSE;
		hasExtraRoom = false;
		centreI = -1;
	}


	public void deployInit(DIR face, int x1, int y1, double dx, double dy, DIV_FORMATION ts, int width) {
		clear();
		this.start.set(x1, y1);
		this.dx = dx;
		this.dy = dy;
		this.faceDir = face;
		this.ts = ts;
		this.width = width;
	}
	
	public void deploy(int x, int y) {
		int t = ts.sizeH;
		bounds.unify(x-t, y-t);
		bounds.unify(x+t, y-t);
		bounds.unify(x-t, y+t);
		bounds.unify(x+t, y+t);
		
		set(deployed(), x, y);
		init(deployed()+1);
	}
	
	
	public void deployFinish(Filler f) {
		
		if (deployed() == 0)
			return;
		
		int xx = 0;
		int yy = 0;
		for (int i = 0; i < deployed(); i++) {
			COORDINATE p = pixel(i);
			xx+=p.x();
			yy+=p.y();
		}
		
		xx/=deployed();
		yy/=deployed();
		
		int cx = xx;
		int cy = yy;
		
		double dist = Double.MAX_VALUE;
		int distI = -1;
		for (int i = 0; i < deployed(); i++) {
			COORDINATE p = pixel(i);
			int dx = p.x()-cx;
			int dy = p.y()-cy;
			double d = Math.sqrt(dx*dx+dy*dy);
			if (d < dist) {
				dist = d;
				distI = i;
			}
		}
		
		if (distI == -1)
			throw new RuntimeException();
		
		this.centreI = distI;
		
		setDirs(f);
	}
	
	public void setHasExtraRoom() {
		hasExtraRoom = true;
	}
	
	@Override
	public void copy(DivFormation o) {
		super.copyy(o);
		faceDir = o.faceDir;
		bounds.set(o.bounds);
		start.set(o.start);
		dx = o.dx;
		dy = o.dy;
		ts = o.ts;
		width = o.width;
		for (int i = 0; i < dirMasks.length; i++) {
			dirMasks[i] = o.dirMasks[i];
		}
		hasExtraRoom = o.hasExtraRoom;
		this.centreI = o.centreI;
	}

	@Override
	public RECTANGLE body() {
		return bounds;
	}
	
	public DIV_FORMATION formation() {
		return ts;
	}
	
	public COORDINATE start() {
		return start;
	}
	
	public double dx() {
		return dx;
	}
	
	public double dy() {
		return dy;
	}
	
	public int width() {
		return width;
	}
	
	public int dirMaskOrtho(int i) {
		return dirMasks[i] & 0x0F;
	}
	
	public DIR dir(int i) {
		int di = (dirMasks[i]>>4) & 0x0F;
		if (di < 2 || di-2 >= DIR.ALL.size())
			return null;
		return DIR.ALL.get(di-2);
	}
	
	public boolean isEdge(int i) {
		int di = (dirMasks[i]>>4) & 0x0F;
		return di != 0;
	}
	
	public boolean hasExtraRoom() {
		return hasExtraRoom;
	}
	
	private void setDirs(Filler f) {
		int o = faceDir.id();
		proj.init(this);
		f.init(this);
		
		for (int i = 0; i < deployed(); i++) {
			COORDINATE t = proj.projectTile(this, i);
			f.fill(t);
		}
		
		for (int i = 0; i < deployed(); i++) {
			
			int mo = 0;
			int mn = 0;
			COORDINATE t = proj.projectTile(this, i); 
			
			for (int di = 0; di < DIR.ORTHO.size(); di++) {
				DIR d = DIR.ORTHO.get(di);
				if (f.isser.is(t, d)) {
					mo |= d.next(o).mask();
				}
			}

			if (mo != 0x0F) {
				mn  = 1;
				double dx = 0;
				double dy = 0;
				double dd = 0;
				for (int di = 0; di < DIR.ALL.size(); di++) {
					DIR d = DIR.ALL.get(di);
					if (!f.isser.is(t, d)) {
						d = d.next(o);
						dx += d.x();
						dy += d.y();
						dd ++;									
					}
				}
				dx /= dd;
				dy /= dd;
				if (dd > 0 && dx != 0 || dy != 0)
					mn += DIR.get(dx, dy).id()+1;
			}
			
			mo = mo | (mn<<4);
			dirMasks[i] = (byte) mo;
			
		}
		
		
		f.done();
	}
	
	private final DivPosProjector proj = new DivPosProjector();
	
	private final class DivPosProjector {

		private double angle;
		
		private final VectorImp vec = new VectorImp();
		private final Coo coo = new Coo();
		
		private DivPosProjector() {
			
		}
		
		public int init(DivFormation pos){
			vec.set(pos.dx, pos.dy);
			angle = Math.atan2(vec.nX(), vec.nY())-Math.PI/2;
			return -(int)(angle / Math.PI/2);
		}
		
		public COORDINATE projectTile(DivFormation pos, int i) {


			COORDINATE p = pos.pixel(i);
			double x = p.x();
			double y = p.y();
			
			
			
			double length = vec.set(pos.start().x(), pos.start().y(), x,y);
			double ang = Math.atan2(vec.nX(), vec.nY());
			if (pos.start.isSameAs(p)) {
				ang = angle;
				length = 0;
			}
			
			ang -= angle;
			

			x = pos.ts.sizeH + Math.sin(ang)*length;
			y = pos.ts.sizeH + Math.cos(ang)*length;
			
			int ty = (int) (y/pos.ts.size);
			int tx = (int) (x/pos.ts.size);
		
			tx += SETT.TWIDTH/2;
			ty += SETT.THEIGHT/2;
			
			coo.set(tx, ty);
			return coo;
			
		}

		
	}

	public boolean isSameAs(DivFormation o) {
		return start.isSameAs(o.start) && dx == o.dx && dy == o.dy && deployed() == o.deployed() && width == o.width && ts == o.ts && centrePixel().isSameAs(o.centrePixel());
	}

	public COORDINATE centreTile() {
		if (deployed() == 0)
			return null;
		return tile(centreI);
	}
	
	public COORDINATE centrePixel() {
		if (deployed() == 0)
			return null;
		return pixel(centreI);
	}
	
}
