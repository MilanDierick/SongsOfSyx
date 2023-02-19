package settlement.thing.pointlight;

import init.C;
import snake2d.Renderer;
import snake2d.util.bit.BitsLong;
import snake2d.util.rnd.RND;
import util.rendering.ShadowBatch;

final class Light {

	private static Light l = new Light();
	
	private final BitsLong tx = 		new BitsLong(0x0000000000000FFFl);
	private final BitsLong ty = 		new BitsLong(0x0000000000FFF000l);
	private final BitsLong offX = 		new BitsLong(0x00000000FF000000l);
	private final BitsLong offY = 		new BitsLong(0x000000FF00000000l);
	private final BitsLong model = 		new BitsLong(0x00003F0000000000l);
	private final BitsLong hidden = 	new BitsLong(0x0000800000000000l);
	private final BitsLong random = 	new BitsLong(0xFFFF000000000000l);
	private long data;
	
	static Light init(long data) {
		l.data = data;
		return l;
	}
	
	static long make(long tx, long ty, long offX, long offY, LightModel model, boolean sprite) {
		
		long random = RND.rInt();
		if (random < 0)
			random = -1;
		long m = model.index;
		long r = 0;
		r = l.tx.set(r, tx);
		r = l.ty.set(r, ty);
		r = l.offX.set(r, offX&0x0FF);
		r = l.offY.set(r, offY&0x0FF);
		r = l.model.set(r, m);
		r = l.random.set(r, random&l.model.mask);


		return r;
		
	}
	
	long hide(boolean h) {
		long hi = h ? 1 : 0;
		return l.hidden.set(data, hi);
		
		
	}
	
	
	public short tx() {
		return (short) tx.get(data);
	}
	
	public short ty() {
		return (short) ty.get(data);
	}

	public void renderBelow(Renderer r, ShadowBatch s, float ds, int offsetX, int offsetY) {
		
		int cX = (int)(tx.get(data) << C.T_SCROLL);
		int cY = (int)(ty.get(data) << C.T_SCROLL);
		cX += C.TILE_SIZEH + (byte)offX.get(data);
		cY += C.TILE_SIZEH + (byte)offY.get(data);
		
		LightModel m = LightModel.all.get((int)model.get(data));
		m.renderSprite(cX+offsetX, cY+offsetY, (int)random.get(data));
		
	}

	public void render(Renderer r, ShadowBatch s, float ds, int offsetX, int offsetY) {
		
		if (hidden.get(data) == 0) {
			int cX = (int)(tx.get(data) << C.T_SCROLL);
			int cY = (int)(ty.get(data) << C.T_SCROLL);
			cX += C.TILE_SIZEH + (byte)offX.get(data);
			cY += C.TILE_SIZEH + (byte)offY.get(data);
			
			LightModel m = LightModel.all.get((int)model.get(data));
			//m.renderFlame(cX+offsetX, cY+offsetY, (int)random.get(data));
			m.register(r, (int)random.get(data), cX, cY, offsetX, offsetY);
		}
		
		
	}
	
	
	
	
	
	
	
	
	
}
