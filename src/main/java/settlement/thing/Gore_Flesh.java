package settlement.thing;

import java.io.IOException;

import init.C;
import settlement.entity.ESpeed;
import settlement.main.SETT;
import settlement.thing.THINGS.ThingFactory;
import settlement.thing.ThingsGore.Gore;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.*;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.TILE_SHEET;
import util.rendering.ShadowBatch;

public class Gore_Flesh extends Gore{

	private final Rec body = new Rec();
	private final ESpeed.Imp speed = new ESpeed.Imp();
	private final ColorImp col = new ColorImp();
	
	private int ran;
	float timer;
	boolean debris = false;
	static boolean debr = false;
	
	public Gore_Flesh(int index) {
		super(index);
		speed.magnitudeTargetSet(0);
		speed.accelerationInit(C.TILE_SIZE*5);
		
	}
	
	@Override
	protected void save(FilePutter f) {
		body.save(f);
		speed.save(f);
		f.i(ran);
		f.f(timer);
		f.bool(debris);
		col.save(f);
	}
	
	@Override
	protected void load(FileGetter f) throws IOException {
		body.load(f);
		speed.load(f);
		ran = f.i();
		timer = f.f();
		debris = f.bool();
		col.load(f);
	}
	
	@Override
	protected void  init(int cx, int cy, double sx, double sy, COLOR col) {
		body.setDim(sprite().size(), sprite().size());
		body.moveC(cx, cy); 
		this.col.set(col);
		ran = RND.rInt();
		double m = C.TILE_SIZE*2 + RND.rFloatP(2)*(C.TILE_SIZE*15);
		DEG.setRandom();
		speed.setRaw(sx + DEG.getCurrentX()*m, sy + DEG.getCurrentY()*m);
		timer = 120 + RND.rFloat(100);
		this.debris = debr;

	}
	
	void setDebris() {
		this.debris = true;
	}
	
	@Override
	protected boolean update(float ds) {
		
		if (speed.isZero()) {
			timer -= ds;
			if (timer < 0) {
				return false;
			}
			return true;
		}
		
		speed.magnitudeAdjust(ds, 2.0, 1);
		move(speed, ds, 0.5f, body, true);
		return true;
	}

	@Override
	public void render(Renderer r, ShadowBatch shadows,
			float ds, int offsetX, int offsetY) {
		int spriteI = sprite().tiles();
		bindCol(col, spriteI>>8);
		sprite().render(r, spriteI, body().x1()+offsetX, body().y1() + offsetY);
		shadows.setDistance2Ground(2).setHeight(0);
		sprite().render(shadows, spriteI, body().x1()+offsetX, body().y1() + offsetY);
		COLOR.unbind();
	}

	private final TILE_SHEET sprite() {
		if (debris)
			return SETT.THINGS().sprites.debris;
		return SETT.THINGS().sprites.flesh;
	}
	
	@Override
	public RECTANGLE body() {
		return body;
	}

	@Override
	protected int z() {
		return 100;
	}
	
	@Override
	public ThingFactory<?> factory() {
		return SETT.THINGS().gore.flesh;
	}
	
}
