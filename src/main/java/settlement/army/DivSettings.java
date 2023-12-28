package settlement.army;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import init.C;
import settlement.army.formation.DIV_FORMATION;
import settlement.stats.STATS;
import settlement.stats.equip.EquipRange;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;

public final class DivSettings {

	public boolean running;
	public boolean guard = true;
	public DIV_FORMATION formation = DIV_FORMATION.LOOSE;
	private int speed = C.TILE_SIZE;
	public boolean fireAtWill;
	public boolean shouldFire;
	public boolean shouldbreak = true;
	public short ammoI = 0;
	private final Div div;
	private boolean mustering = false;
	private boolean mopping = false;
	public boolean charging = false;
	boolean isFighting = false;
	public byte enemyDirMask;
	public float power;
	public float powerRanged;
	public boolean musteringChange = true;
	
	DivSettings(Div div) {
		this.div = div;
	}
	
	public void run() {
		running = true;
	}
	
	public boolean runSpeed() {
		return running || charging;
	}
	
	public boolean threatAt(DIR d) {
		return DivMorale.PROJECTILES.getD(div) > 0 || (enemyDirMask != 0 && (threat(d) || threat(d.next(-1)) || threat(d.next(1))));
	}
	
	public boolean threat(DIR d) {
		if (d.isOrtho()) {
			return (enemyDirMask & d.mask()) != 0;
		}else {
			return ((enemyDirMask)>>4 & d.mask()) != 0;
		}
	}
	
	public boolean mustering() {
		return mustering;
	}
	
	public boolean moppingUp() {
		return mopping;
	}
	
	public boolean shouldFire() {
		return shouldFire && div.trajectory.hasAny() && ammo() != null;
	}
	
	public void musteringSet(boolean must) {
		div.current().init(div.menNrOf());
		div.order().update(div);
		mustering = must;
		musteringChange = true;
	}
	
	public void moppingSet(boolean must) {
		if (must) {
			musteringSet(must);
		}
		mopping = must;
	}
	
	public boolean isFighting() {
		return isFighting;
	}
	
	public EquipRange ammo() {
		if (STATS.EQUIP().RANGED().get(ammoI).stat().div().get(div) > 0 && STATS.EQUIP().RANGED().get(ammoI).ammunition.div().get(div) > 0)
			return STATS.EQUIP().RANGED().get(ammoI);
		for (int k = 0; k < STATS.EQUIP().RANGED().size(); k++) {
			EquipRange a = STATS.EQUIP().RANGED().get(k);
			if (a.stat().div().get(div) > 0 && a.ammunition.div().get(div) > 0) {
				ammoI = a.tIndex;
				return a;
			}
		}
		return null;
	}
	
	public void setBestAmmo() {
		int i = ThreadLocalRandom.current().nextInt(STATS.EQUIP().RANGED().size());
		for (int k = 0; k < STATS.EQUIP().RANGED().size(); k++) {
			EquipRange a = STATS.EQUIP().RANGED().getC(i+k);
			if (a.stat().div().get(div) > 0 && a.ammunition.div().get(div) > 0) {
				ammoI = a.tIndex;
			}
		}
	}
	
	public boolean fireAtWill() {
		if (ammo() == null) {
			fireAtWill = false;
		}
		return fireAtWill;
	}
	
	void save(FilePutter file) {
		file.bool(running);
		file.bool(guard);
		file.b((byte) formation.ordinal());
		file.i(speed);
		file.bool(fireAtWill);
		file.s(ammoI);
		file.bool(mustering);
		file.bool(mopping);
		file.bool(isFighting);
		file.bool(shouldFire);
		file.bool(charging);
		file.bool(shouldbreak);
		file.f(power);
	}

	void load(FileGetter file) throws IOException {
		running = file.bool();
		guard = file.bool();
		formation = DIV_FORMATION.all.get(file.b());
		speed = file.i();
		fireAtWill = file.bool();
		ammoI = file.s();
		mustering = file.bool();
		mopping = file.bool();
		isFighting = file.bool();
		shouldFire = file.bool();
		charging = file.bool();
		shouldbreak = file.bool();
		power = file.f();
		musteringChange = true;
	}
	
	void clear() {
		running = false;
		guard = true;
		fireAtWill = false;
		formation = DIV_FORMATION.LOOSE;
		ammoI = 0;
		mustering = false;
		mopping = false;
		isFighting = false;
		shouldFire = false;
		charging = false;
		shouldbreak = false;
		power = 0;
		musteringChange = true;
	}
	
}
