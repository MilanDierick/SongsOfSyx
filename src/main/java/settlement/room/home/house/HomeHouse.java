package settlement.room.home.house;

import static settlement.main.SETT.*;

import game.GAME;
import init.race.Race;
import init.resources.RES_AMOUNT;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.home.*;
import settlement.room.home.HomeSettings.HomeSetting;
import settlement.room.main.Room;
import settlement.room.main.furnisher.FurnisherItem;
import settlement.room.main.util.RoomAreaWrapper.RoomWrap;
import settlement.room.sprite.RoomSprite;
import settlement.stats.STATS;
import snake2d.util.bit.Bits;
import snake2d.util.datatypes.*;
import snake2d.util.map.MAP_OBJECT;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;

public final class HomeHouse extends RoomWrap implements HOME{

	private FurnisherItem it;
	private final Coo service = new Coo();
	
	

	private final LivingDataD lit = 		new LivingDataD(this, 1, 0b0000_0000_0000_0000_0000_0000_0000_0001);
	private final LivingDataD upgrade = 	new LivingDataD(this, 1, 0b0000_0000_0000_0000_0000_0000_0000_1110);
	private final LivingDataD isolation = 	new LivingDataD(this, 1, 0b0000_0000_0000_0000_0000_1111_1111_0000);
	private final LivingDataD renderTimer = new LivingDataD(this, 1, 0b0000_0000_0000_1111_1111_0000_0000_0000);
	private final LivingDataD am = 			new LivingDataD(this, 1, 0b0000_0011_1111_0000_0000_0000_0000_0000);
	private final LivingDataD amOdd = 		new LivingDataD(this, 1, 0b1111_1100_0000_0000_0000_0000_0000_0000);
	private final LivingDataD setting = 	new LivingDataD(this, 2, 0x0000FFFF);
	private final LivingDataD litTimer = 	new LivingDataD(this, 2, 0xFF000000);
	private final LivingDataD random = 		new LivingDataD(this, 2, 0x00FF0000);
	
//	private final LivingDataD random = 		new LivingDataD(this, 1, 0x0000FFFF);
//	private final LivingDataD lit = 		new LivingDataD(this, 1, 0x00010000);
//	private final LivingDataD upgrade = 	new LivingDataD(this, 1, 0x00F00000);
//	private final LivingDataD isolation = 	new LivingDataD(this, 1, 0xFF000000);
//	private final LivingDataD setting = 	new LivingDataD(this, 2, 0x0000FFFF);
//	private final LivingDataD am = 			new LivingDataD(this, 2, 0x00FF0000);
//	private final LivingDataD amOdd = 		new LivingDataD(this, 2, 0xFF000000);
//	private final LivingDataD renderTimer = new LivingDataD(this, 3, 0x0000FFFF);
//	private final LivingDataD litTimer = 	new LivingDataD(this, 3, 0xFFFF0000);
	private final LivingDataD[] resources = new LivingDataD[8];
	private final LivingDataD[] occupants = new LivingDataD[32];
	
	HomeHouse() {
		for (int i = 0; i < resources.length; i++) {
			int m = 0x0F << (i)*4;
			resources[i] = new LivingDataD(this, 3, m);
		}
		
		for (int i = 0; i < occupants.length; i++) {
			occupants[i] = new LivingDataD(this, 4+i, 0xFFFFFFFF);
		}
	}
	
	Object user;
	StackTraceElement[] els;
	
	HomeHouse useAndReserve(int tx, int ty){
		
		Room r = blue().get(tx, ty);
		if (r == null || !(r instanceof InstanceHome))
			throw new RuntimeException();
		FurnisherItem it = SETT.ROOMS().fData.item.get(tx, ty);
		if (it == null)
			throw new RuntimeException();
		if (this.it != it | super.init(r, tx, ty)) {
			this.it = it;
			int x = 0;
			int y = it.group.item(0, 0).height()/2;
			
			for (int i = 0; i < it.rotation; i++) {
				int newX = -y;
				int newY = x;
				x = newX;
				y = newY;
			}
			x = CLAMP.i(body().x1()+it.width()/2+x, body().x1(), body().x2()-1);
			y = CLAMP.i(body().y1()+it.height()/2+y, body().y1(), body().y2()-1);
			
			service.set(x, y);
		}
		return this;
	}

	HomeHouse create() {
		
		for (COORDINATE c : body()) {
			if (is(c)) {
				SETT.ROOMS().data.set(ROOMS().map.rooma.get(c), c, 0);
			}
		}
		
		int ran = RND.rInt();

		{
			int r = RND.rInt(blue().constructor.sp.sp.sprites[it.group.index()].length);
			random.set(r);
		}
		
		for (COORDINATE c : body()) {
			if (is(c)) {
				int m = 0;
				Sprite s = sprite.get(c.x(), c.y());
				if (s != null) {
					m = s.getData(c.x(), c.y(), c.x()-body().x1(), c.y()-body().y1(), it, ran);
				}
				SETT.ROOMS().fData.spriteData.set(c.x(), c.y(), m);
				SETT.PATH().availability.updateAvailability(c.x(), c.y());
				SETT.ROOMS().extraBit.set(service, false);
			}
		}
		SETT.ROOMS().extraBit.set(service, true);
		add();
		blue().odd.update(service.x(), service.y());
		
		return this;
	}
	
	void dispose() {
		for (int i = 0; i < occupants(); i++) {
			Humanoid a = occupant(i);
			STATS.HOME().GETTER.set(a, null);
			i--;
		}
		remove();
		it = null;
	}
	
	@Override
	public HOME vacate(Humanoid h) {
		remove();
		for (int oi = 0; oi < occupants(); oi++) {
			Humanoid o = occupant(oi);
			if (o == h) {
				occupants[oi].set(0);
				am.inc(-1);
				if (STATS.WORK().EMPLOYED.get(o) == null)
					amOdd.inc(-1);
				for (int k = oi+1; k < occupantsMax(); k++) {
					occupants[k-1].set(occupants[k].get());
				}
				add();
				if (am.get() == 0)
					turnOffLight();
				SETT.ROOMS().HOMES.HOME.odd.update(mX(), mY());
				return this;
			}
		}
		
		throw new RuntimeException(h + " " + am.get() + " " + occupantsMax());
	}
	
	@Override
	public HOME occupy(Humanoid h) {
		
		if (am.get() >= occupantsMax()) {
			if (amOdd.get() == 0)
				throw new RuntimeException(h + " " + am.get() + " " + occupantsMax());
			vacateOddjobber();
		}
		remove();
		if (h != SETT.ENTITIES().getByID(h.id()))
			throw new RuntimeException(""+h.id());
		occupants[am.get()].set(h.id());
		am.inc(1);
		if (STATS.WORK().EMPLOYED.get(h) == null)
			amOdd.inc(1);
		add();
		return this;
	}
	
	private void vacateOddjobber() {
		if (amOdd.get() == 0)
			return;
		
		for (int i = 0; i < am.get(); i++) {
			Humanoid o = occupant(i);
			if (STATS.WORK().EMPLOYED.get(o) == null) {
				STATS.HOME().GETTER.set(o, null);
				return;
			}
		}
		throw new RuntimeException(am.get() + " " + amOdd.get());
	}
	
	public HOME use() {
		litTimer.set((GAME.updateI()>>8)&0x0FF);
		if (lit.get() == 1) {
			return this;
		}
		lit.set(1);
		for (COORDINATE c : body()) {
			if (is(c)) {
				RoomSprite s = sprite.get(c.x(), c.y());
				if (s != null) {
					if (s == blue().constructor.sp.nSta && !SETT.LIGHTS().is(c.x(), c.y()))
						SETT.LIGHTS().candle(c.x(), c.y(), 0);
				}
			}
		}
		
		return this;
	}
	

	
	private void turnOffLight() {
		lit.set(0);
		for (COORDINATE c : body()) {
			if (is(c)) {
				RoomSprite s = sprite.get(c.x(), c.y());
				if (s != null) {
					if (s == blue().constructor.sp.nSta && SETT.LIGHTS().is(c.x(), c.y()))
						SETT.LIGHTS().remove(c.x(), c.y());
				}
			}
		}
	}
	
	@Override
	public Humanoid occupant(int oi) {
		if (oi < am.get()) {
			int i = occupants[oi].get();
			ENTITY e = SETT.ENTITIES().getByID(i);
			if (e != null && e instanceof Humanoid) {
				return (Humanoid) e;
			}else
				throw new RuntimeException(oi + " " + body() + " " + e);
		}
		return null;
	}
	
	@Override
	public int occupants() {
		return am.get();
	}
	
	public int occupantsOdd() {
		return amOdd.get();
	}
	
	@Override
	public COORDINATE service() {
		return service;
	}
	
	final MAP_OBJECT<Sprite> sprite = new MAP_OBJECT<Sprite>() {

		@Override
		public Sprite get(int tile) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Sprite get(int tx, int ty) {
			
			int ri = random.get();
			ri %= blue().constructor.sp.sp.sprites[it.group.index()].length;
			
			SpriteConfig sp = blue().constructor.sp.sp.sprites[it.group.index()][ri];
			int dx = tx-body().x1();
			int dy = ty-body().y1();
			return sp.get(it.rotation).get(dx, dy);
		}
	
	};

	public DIR dir(int tx, int ty) {
		return DIR.ORTHO.get(it.rotation);
	}
	
	@Override
	public HomeSetting availability() {
		if (am.get()-amOdd.get() >= occupantsMax())
			return null;
		return psetting();
	}
	
	private HomeSetting psetting() {
		if (occupants() > 0)
			return SETT.ROOMS().HOMES.settings.specific(HOMET.get(occupant(0)));
		return setting();
	}
	
	public HomeSetting setting() {
		return sFiddler.get(setting.get());
	}
	
	private void remove() {

		SETT.ROOMS().HOMES.report(-am.get(), -occupantsMax(), psetting());
		if (availability() != null) {
			SETT.PATH().comps.data.home.reportAbsence(service.x(), service.y(), availability());
			
		}

	}
	
	private void add() {
		SETT.ROOMS().HOMES.report(am.get(), occupantsMax(), psetting());
		if (availability() != null) {
			SETT.PATH().comps.data.home.reportPresence(service.x(), service.y(), availability());
			
		}
	}
	
	private final DirCoo dcoo = new DirCoo();
	
	public static class DirCoo extends Coo {
		
		private static final long serialVersionUID = 1L;
		public DIR dir;
		public boolean isLay;
		
	}

	@Override
	public int occupantsMax() {
		return SETT.ROOMS().HOMES.HOME.constructor.maxOccupants[it.group.index()][upgrade.get()];
	}
	

	@Override
	public int resourceAm(int ri) {
		resCount();
		return resources[ri].get();
		
	}
	
	private void resCount() {
		unuse();
		if (occupants() == 0)
			return;
		if (renderTimer.get() != ((GAME.updateI() >> 8)&0x0FF)) {
			renderTimer.set((GAME.updateI() >> 8)&0x0FF);
			int ri = 0;
			for (@SuppressWarnings("unused") RES_AMOUNT a : occupant(0).race().home().clas(occupant(0)).resources()) {
				double am = 0;
				for (int i = 0; i < occupants(); i++) {
					am += STATS.HOME().current(occupant(i), ri);
				}
				resources[ri].set((int)Math.ceil(am/occupants()));
				ri++;
			}
		}
	}
	
	private void unuse() {
		if (lit.get() == 0) {
			return;
		}
		
		if (Bits.getDistance(litTimer.get(), GAME.updateI()>>8, 0x0FF) > 0x0F){
			turnOffLight();
			
		}
		litTimer.set((GAME.updateI()>>8)&0x0FF);
	}
	
	public DirCoo getService(int tx, int ty) {
		Sprite s = sprite.get(tx, ty);
		if (s != null && s.service) {
			dcoo.dir = s.dir(SETT.ROOMS().fData.spriteData.get(tx, ty));
			dcoo.set(tx, ty);
			dcoo.isLay = s == blue().constructor.sp.bedS;
			return dcoo;
		}
		return null;
	}
	
	FurnisherItem it() {
		return it;
	}
	
	public DirCoo findService(Humanoid h) {
		
		int rx = body().x1()+RND.rInt(body().width());
		int ry = body().y1()+RND.rInt(body().height());
		
		for (int y = 0; y < body().height(); y++) {
			for (int x = 0; x < body().width(); x++) {
				
				DirCoo c = getService(rx, ry);
				if (c != null && SETT.ENTITIES().getAtTileSingle(rx, ry) == null) {
					return c;
				}
				rx++;
				if (rx >= body().x2()) {
					rx = body().x1();
					ry++;
					if (ry >= body().y2()) {
						ry = body().y1();
					}
				}
			}
		}
		return null;
		
	}

	@Override
	public HOME resUpdate() {
		renderTimer.set(0);
		return this;
	}
	
	@Override
	public double isolation() {
		return isolation.getD();
	}
	
	public HomeHouse isolationSet(double am) {
		isolation.setD(am);
		return this;
	}
	
	public static ROOM_HOME blue() {
		return SETT.ROOMS().HOMES.HOME;
	}

	@Override
	public Race race() {
		return occupant(0).race();
	}

	@Override
	public HOME done() {
		blue().houses.ret(this);	
		return this;
	}

	@Override
	public CharSequence nameHome() {
		return it.group.name;
	}

	public void setUpgrade(int upgrade) {
		if (upgrade < 0 || upgrade > 2)
			throw new RuntimeException("" + upgrade);
		if (this.upgrade.get() == upgrade)
			return;
		
		int am = SETT.ROOMS().HOMES.HOME.constructor.maxOccupants[it.group.index()][upgrade];
		int d = occupants()-am;

		for (int i = 0; i < d; i++) {
			Humanoid o = occupant(0);
			STATS.HOME().GETTER.set(o, null);
		}
		remove();
		this.upgrade.set(upgrade);
		add();
	}
	
	public int upgrade() {
		return this.upgrade.get();
	}
	
	public final HomeSettings.SettingFiddler sFiddler = new HomeSettings.SettingFiddler() {
		
		@Override
		protected void setIndexOnly(int settingIndex) {
			setting.set(settingIndex);
		}

		@Override
		protected int getCurrentSettingIndex() {
			return setting.get();
		}

		@Override
		protected void setAndReport(int settingIndex, HomeSetting setting) {
			for (int i = 0; i < am.get(); i++) {
				Humanoid o = occupant(i);
				if (!setting.is(o)) {
					i--;
					STATS.HOME().GETTER.set(o, null);
				}
			}
			remove();
			HomeHouse.this.setting.set(settingIndex);
			add();
		}
	};
	
}
