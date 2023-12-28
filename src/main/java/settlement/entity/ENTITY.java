package settlement.entity;

import static settlement.main.SETT.*;

import java.io.IOException;

import game.boosting.BOOSTABLES;
import settlement.main.SETT;
import settlement.stats.util.CAUSE_LEAVE;
import snake2d.LOG;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.*;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import util.rendering.ShadowBatch;
import view.sett.SETT_HOVERABLE;

public abstract class ENTITY implements BODY_HOLDER, SETT_HOVERABLE{
	
	int handlerId = -1;
	public final ESpeed.Imp speed = new ESpeed.Imp();
	public final EPHYSICS.Solid physics = new EPHYSICS.Solid();
	transient ENTITY next;
	transient ENTITY prev;
	short gx = -1,gy = -1;
	
	
	/**
	 * renders this entity at its current position with the adjustment of the offsets.
	 * @param ds seconds passed
	 * @param offsetX
	 * @param offsetY
	 */
	public abstract void render(Renderer r, ShadowBatch shadows, float ds, int offsetX, int offsetY);
	
	public abstract void renderSimple(Renderer r, ShadowBatch shadows, float ds, int offsetX, int offsetY);
	
	@Override
	public final RECTANGLE body() {
		return physics.body();
	}
	

	/**
	 * 
	 * @param other - if it's with another entity
	 * @param pierceDamage - the damage as returned by collideDamage, or piercing damage
	 * @param norX - the direction from other entity to you
	 * @param norY - the direction from other entity to you
	 * @param speedDot - the dot product 0-1 of the direction of collision and your speeds direction before the collision
	 * @param force - the exchanged force, expressed in your own mass.
	 * @return the momentum absorbed
	 */
	public abstract void collide(ECollision coll);
	
	protected abstract void meet(ENTITY other);
	protected abstract boolean willCollideWith(ENTITY other);
	protected abstract boolean collides();
	
	protected abstract void setCollideDamage(ECollision coll, ECollision result);
	
	public abstract double getDefenceSkill(double dirDot);
	
	/**
	 * Notifies this entity of a collision with a solid tile.
	 * @param norX
	 * @param norY
	 * @param momentum. The force applied
	 */
	public abstract boolean collideTile(boolean broken, double norX, double norY, double momentum, int tx, int ty);
	
	public abstract void collideUnconnected();
	
	public abstract COLOR minimapColor();
	
	/**
	 * The main logical update of the entity. Returns false if the entity is no more.
	 * @param ds
	 * @return
	 */
	protected abstract boolean update(float ds);
	
	public final boolean isRemoved(){
		return handlerId == -1;
	}
	
	protected final void add(boolean collide){
		ENTITIES().add(this, collide);
	}

	public final void helloMyNameIsInigoMontoyaYouKilledMyFatherPrepareToDie() {
		if (!isRemoved())
			SETT.ENTITIES().remove(this);
	}
	
	protected abstract void removeAction();
	
	public final int id() {
		return handlerId;
	}
	
	protected abstract double height();
	
	public COORDINATE tc() {
		return physics.tileC();
	}
	
	public final int ssx() {
		return gx;
	}
	
	public final int ssy() {
		return gy;
	}
	
	protected void save(FilePutter file) {
		file.i(handlerId);
		file.s(gx); 
		file.s(gy);
		speed.save(file);
		physics.save(file);
	}
	
	protected final void load(FileGetter file) throws IOException {
		handlerId = file.i();
		gx = file.s();
		gy = file.s();
		speed.load(file);
		physics.load(file);
	}
	
	public static class ECollision {
		public ENTITY other;
		public CAUSE_LEAVE leave;
		
		public double damageStrength = 0;
		public final double[] damage = new double[BOOSTABLES.BATTLE().DAMAGES.size()];
		public double momentum;
		/**
		 * The x direction from other entity to you.
		 */
		public double norX;
		/**
		 * The y direction from other entity to you.
		 */
		public double norY;
		/**
		 * the dot product 0-1 of the direction of collision and your speeds direction before the collision
		 */
		public double dirDot;
		public double dirDotOther;
		public boolean speedHasChanged;
		
		public void debug() {
			LOG.ln("other " + other);
			LOG.ln("leave " + leave);
			LOG.ln("mom " + momentum);
			LOG.ln("strength " + damageStrength);
			for (int i = 0; i < damage.length; i++) {
				LOG.ln(BOOSTABLES.BATTLE().DAMAGES.get(i).key  + " " + damage[i]);
				
			}
			LOG.ln("dir " + dirDot);
		}
	}
	
}
