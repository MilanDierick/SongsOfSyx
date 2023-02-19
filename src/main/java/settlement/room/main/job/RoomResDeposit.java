package settlement.room.main.job;

import static settlement.main.SETT.*;

import game.GAME;
import init.resources.RESOURCE;
import init.sound.SoundSettlement.Sound;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.misc.job.SETT_JOB;
import settlement.room.industry.module.ROOM_PRODUCER;
import settlement.room.main.RoomInstance;
import snake2d.SPRITE_RENDERER;
import snake2d.util.bit.Bit;
import snake2d.util.bit.Bits;
import snake2d.util.datatypes.*;
import snake2d.util.misc.CLAMP;
import util.rendering.ShadowBatch;

public abstract class RoomResDeposit implements SETT_JOB {

	private final static Bits[] AMOUNTS = new Bits[] {
		new Bits(0b0000_0000_0000_0000_0000_0000_0001_1111),
		new Bits(0b0000_0000_0000_0000_0000_0011_1110_0000),
		new Bits(0b0000_0000_0000_0000_0111_1100_0000_0000),
		new Bits(0b0000_0000_0000_1111_1000_0000_0000_0000),
		};

	private final static Bit[] RESERVED = new Bit[] {
		new Bit(0b0000_0000_0001_0000_0000_0000_0000_0000),
		new Bit(0b0000_0000_0010_0000_0000_0000_0000_0000),
		new Bit(0b0000_0000_0100_0000_0000_0000_0000_0000),
		new Bit(0b0000_0000_1000_0000_0000_0000_0000_0000),
	};

	protected Coo coo = new Coo();
	protected int data;
	protected final static String name = "Gettings raw materials";
	private ROOM_PRODUCER ins;



	protected RoomResDeposit() {

	}

	public RoomResDeposit get(int tx, int ty, RoomInstance i) {
		if (i != null && i.is(tx, ty) && is(tx, ty) && i instanceof ROOM_PRODUCER) {
			coo.set(tx, ty);
			data = ROOMS().data.get(tx, ty);
			ins = (ROOM_PRODUCER) i;
			return this;
		}
		return null;
	}

	protected abstract boolean is(int tx, int ty);

	public int amount(int res) {
		return AMOUNTS[res].get(data);
	}

	public boolean hasOneOfEach() {
		for (int i = 0; i < resAm(); i++) {
			if (amount(i) <= 0)
				return false;
		}
		return true;
	}

	public void withdrawOneOfEach() {
		int od = data;
		for (int i = 0; i < resAm(); i++) {
			if (amount(i) <= 0)
				throw new RuntimeException();
			data = AMOUNTS[i].inc(data, -1);
		}
		debug("W", od);
		save();
	}

	public void depositOneOfEach() {
		int od = data;
		for (int i = 0; i < resAm(); i++) {
			data = AMOUNTS[i].inc(data, 1);
		}
		debug("D", od);
		save();
	}

	private void debug(String w, int odata) {

	}

	private void amountSet(int a, int res) {
		data = AMOUNTS[res].set(data, a);
	}

	@Override
	public boolean jobReserveCanBe() {
		return jobResourceBitToFetch() != 0;
	}

	public void render(SPRITE_RENDERER r, ShadowBatch shadowBatch, int x, int y, int ran) {
		shadowBatch.setHeight(1).setDistance2Ground(0);
		for (int i = 0; i < resAm(); i++) {
			if (amount(i) > 0) {
				res(i).renderLaying(shadowBatch, x, y, ran, amount(i));
				res(i).renderLaying(r, x, y, ran, amount(i));
				ran = ran >> 3;
			}
		}
	}

	public boolean withDraw(int ri, int amount) {
		amountSet(amount(ri) - amount, ri);
		save();
		return amount(ri) > 0;
	}

	@Override
	public long jobResourceBitToFetch() {
		long l = 0;
		for (int i = 0; i < resAm(); i++) {
			if (!RESERVED[i].is(data)) {
				if (amount(i) < 10)
					l |= res(i).bit;
			}
		}
		return l;
	}
	
	@Override
	public int jobResourcesNeeded() {
		return 10;
	}

	@Override
	public double jobPerformTime(Humanoid skill) {
		return 0;
	}

	@Override
	public void jobStartPerforming() {
	}

	@Override
	public Sound jobSound() {
		return null;
	}
	
	@Override
	public RESOURCE jobPerform(Humanoid skill, RESOURCE res, int ram) {
		boolean has = true;
		
		for (int i = 0; i < resAm(); i++) {
			if (res(i) == res) {
				
				data = RESERVED[i].clear(data);
				ram = CLAMP.i(ram, 0, jobResourcesNeeded());
				amountSet(amount(i) + ram, i);
			}
			has &= amount(i) > 0;
		}

		save();
		if (has)
			hasCallback();
		return null;
	}

	protected abstract void hasCallback();

	@Override
	public void jobReserve(RESOURCE r) {
		for (int i = 0; i < resAm(); i++) {
			if (res(i) == r) {
				data = RESERVED[i].set(data);
				save();
				return;
			}
		}
		throw new RuntimeException("" + r);
	}

	@Override
	public boolean jobReservedIs(RESOURCE r) {
		for (int i = 0; i < resAm(); i++) {
			if (res(i) == r) {
				return RESERVED[i].is(data);
			}
		}
		//GAME.Notify("" + r + " " + coo.x() + " " + coo.y());
		return false;
	}

	@Override
	public void jobReserveCancel(RESOURCE r) {
		for (int i = 0; i < resAm(); i++) {
			if (res(i) == r) {
				if (RESERVED[i].is(data)) {
					data = RESERVED[i].clear(data);
					save();
				}
				return;
			}
		}
		GAME.Notify("" + r);

	}
	


	void save() {
		ROOMS().data.set((RoomInstance)ins, coo, data);
	}

	@Override
	public COORDINATE jobCoo() {
		return coo;
	}

	@Override
	public String jobName() {
		return name;
	}

	@Override
	public boolean jobUseTool() {
		return false;
	}
	
	public final RESOURCE res(int index) {
		return ins.industry().ins().get(index).resource;
	}
	
	public final int resAm() {
		return ins.industry().ins().size();
	}

	public void dispose() {
		for (int i = 0; i < resAm(); i++) {
			if (amount(i) > 0) {
				boolean unload = false;
				for (int di = 0; di < DIR.ALL.size(); di++) {
					int dx = coo.x() + DIR.ALL.get(di).x();
					int dy = coo.y() + DIR.ALL.get(di).y();
					if (SETT.PATH().connectivity.is(dx, dy)) {
						unload = true;
						THINGS().resources.create(dx, dy, res(i), amount(i));
						break;
					}
				}
				if (!unload) {
					THINGS().resources.create(coo, res(i), amount(i));
				}
			}
				
		}
		data = 0;
		save();
		

	}

}
