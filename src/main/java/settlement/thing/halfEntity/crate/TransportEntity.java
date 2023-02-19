package settlement.thing.halfEntity.crate;

import java.io.IOException;

import init.C;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.work.AIModule_Work;
import settlement.main.SETT;
import settlement.thing.halfEntity.Factory;
import settlement.thing.halfEntity.HalfEntity;
import snake2d.Renderer;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.rnd.RND;
import util.gui.misc.GBox;
import util.rendering.ShadowBatch;

public final class TransportEntity extends HalfEntity {
	
	private static VectorImp vec = new VectorImp();
	
	private int hi = -1;
	private byte ri = -1;
	private byte ran;
	private byte di;
	private double mov;
	private int ox,oy;
	
	private static final int[] bumpOff = new int[128];
	
	static {
		for (int i = 0; i < bumpOff.length; i+=2) {
			bumpOff[i] = (int) (RND.rSign()*RND.rFloat()*2);
			bumpOff[i+1] = (int) (RND.rSign()*RND.rFloat()*2);
		}
	}
	
	
	public TransportEntity() {
		super(C.TILE_SIZE, C.TILE_SIZE);
	}
	
	@Override
	protected void save(FilePutter file) {
		file.i(hi);
		file.b(ri);
		file.b(ran);
		file.b(di);
		file.d(mov);
	}
	
	@Override
	protected HalfEntity load(FileGetter file) throws IOException {
		hi = file.i();
		ri = file.b();
		ran = file.b();
		di = file.b();
		mov = file.d();
		return this;
	}
	
	public RESOURCE res() {
		return ri == -1 ? null : RESOURCES.ALL().get(ri);
	}
	
	public Humanoid host() {
		ENTITY e = SETT.ENTITIES().getByID(hi);
		if (e != null && e instanceof Humanoid)
			return (Humanoid) e;
		return null;
	}

	public int carryAmount() {
		Humanoid h = host();
		if (h == null)
			return -1;
		return AIModule_Work.getTransportAmount(h);
	}


	boolean init(Humanoid h, int tx, int ty, RESOURCE res, byte ran) {
		body().moveC(tx*C.TILE_SIZE+C.TILE_SIZEH, ty*C.TILE_SIZE+C.TILE_SIZEH);
		hi = h.id();
		this.ran = ran;
		ri = res.bIndex();
		ox = (short) body().cX();
		oy = (short) body().cY();
		mov = 0;
		vec.set(body(), h.body());
		di = (byte) vec.dir().id();
		add();
		return true;
	}
	
	@Override
	protected void update(float ds) {
		
		Humanoid a = host();
		if (a == null) {
			remove();
			return;
		}
		
		int am = carryAmount();
		if (am < 0) {
			remove();
			return;
		}
		
		if (ox == a.body().cX() && oy == a.body().cY()) {
			return;
		}
		
		mov = vec.set(ox, oy, a.body().cX(), a.body().cY());

		if (mov > C.TILE_SIZE) {
			mov = 0;
			body().moveC(ox, oy);
			ox = a.body().cX();
			oy = a.body().cY();
		}
	}
	
	@Override
	public void render(Renderer r, ShadowBatch s, float ds, int x1, int y1) {
		
		int am = carryAmount();
		if (am < 0)
			return;
		x1 += C.TILE_SIZEH;
		y1 += C.TILE_SIZEH;
		vec.set(body().cX(), body().cY(), ox, oy);
		DIR dir = vec.dir();
		int dx = (int) (vec.nX()*mov);
		int dy = (int) (vec.nY()*mov);
		x1 += dx;
		y1 += dy;
		
		int bi = (int) (mov*2);
		bi %= bumpOff.length;
		bi &= ~1;


		int cx = (int) (x1);
		int cy = (int) (y1);
		SETT.ANIMALS().renderCaravan(r, s, mov/C.TILE_SIZE, cx, cy, null, 0, false, dir.id(), ran);
		cx = (int) (x1-dir.xN()*C.TILE_SIZE) + bumpOff[bi];
		cy = (int) (y1-dir.yN()*C.TILE_SIZE) + bumpOff[bi+1];
		SETT.ROOMS().TRANSPORT.renderCart(r, s, dir.id(), cx, cy,ran, res(), am, mov/C.TILE_SIZE);
		
		
		
	}


	@Override
	protected void removeAction() {

	}
	
	@Override
	protected Factory<TransportEntity> constructor() {
		return SETT.HALFENTS().transports;
	}

	
	
	@Override
	public void hoverInfo(GBox box) {
		
	}



	
}
