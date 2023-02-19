package settlement.entity.animal;

import static settlement.main.SETT.*;

import java.io.IOException;

import game.GAME;
import game.time.TIME;
import init.C;
import init.D;
import init.settings.S;
import settlement.entity.ENTITY;
import settlement.entity.ResolverTile;
import settlement.entity.animal.spawning.AnimalSpawnSpot;
import settlement.entity.humanoid.HPoll;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.food.pasture.PastureInstance;
import settlement.room.food.pasture.ROOM_PASTURE;
import settlement.room.main.Room;
import settlement.room.service.hygine.bath.ROOM_BATH;
import settlement.thing.ThingsCadavers.Cadaver;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.rnd.RND;
import util.gui.misc.GBox;
import util.rendering.ShadowBatch;

public class Animal extends ENTITY{
	
	private static CharSequence ¤¤Cub = "¤Cub";
	private static CharSequence ¤¤domestcated = "¤(Domesticated)";
	static {
		D.ts(Animal.class);
	}
	
	private final byte def;
	private boolean isBeeingHunted = false;
	float spriteTimer;
	final float ran;
	float damage = 0f;
	final ColorImp color = new ColorImp();
	private boolean domesticated;
	final static int lifeSpan = (int) (5.0*TIME.years().bitConversion(TIME.days()));
	
	private int birthDay = TIME.days().bitsSinceStart()-RND.rInt(lifeSpan);
	private byte upHour = (byte) RND.rInt(TIME.hours().bitsPerCycle());
	boolean cub = TIME.days().bitsSinceStart() - birthDay < 14;
	
	
	boolean inWater = false;
	private byte spotI;
	
	
	private State state;
	float stateTimer;
	int stateI = -1;
	private byte killSwitch = 0;
	private float nTimer = 0;
	
	public Animal(int x, int y, AnimalSpecies spec, AnimalSpawnSpot spot){
		
		this.def = (byte) spec.index();
		
		physics.initPosition(x, y, spec.hitBoxSize(), spec.hitBoxSize()); 
		physics.setMass(spec.mass()*RND.rFloat1(0.2));
		physics.setRestitution(0.2f);
		physics.setHeight(spec.heightOverGround() + RND.rFloat0(spec.heightOverGround()/4.0f));
		inWater = TERRAIN().WATER.isOpen(physics.tileC().x(), physics.tileC().y()) || ROOM_BATH.isPool(physics.tileC().x(), physics.tileC().y());
		speed.accelerationInit(spec.acceleration());
		speed.magnitudeMaxInit(spec.acceleration());
		speed.turnRandom();
		
		spotI = (byte) (spot != null ? spot.index() : -1);
		
		ran = RND.rFloat();
		
		int c = 60 + RND.rInt(60);
		color.set(c+RND.rInt0(8), c+RND.rInt0(8), c+RND.rInt0(8));
		
		speed.turnRandom();
		setState(State.STAND, RND.rFloat(1));	
		//ai.update(this, 0);
		add();
	}
	
	public Animal(FileGetter file) throws IOException{
		
		super.load(file);
		
		
		this.def = file.b();
		isBeeingHunted = file.bool();
		spriteTimer = file.f();
		ran = file.f();
		damage = file.f();
		color.load(file);
		domesticated = file.bool();
		birthDay = file.i();
		upHour = file.b();
		inWater = file.bool();
		cub = file.bool();
		spotI = file.b();
		
		state = State.all[file.i()];
		stateTimer = file.f();
		stateI = file.i();
		killSwitch = file.b();
		nTimer = file.f();
		if (upHour < 0) {
			upHour += TIME.hoursPerDay;
		}
	}
	
	@Override
	protected void save(FilePutter file) {
		super.save(file);
		file.b(def);
		file.bool(isBeeingHunted);
		file.f(spriteTimer);
		file.f(ran);
		file.f(damage);
		color.save(file);
		file.bool(domesticated);
		file.i(birthDay);
		file.b(upHour);
		file.bool(inWater);
		file.bool(cub);
		file.b(spotI);
		
		file.i(state.ordinal());
		file.f(stateTimer);
		file.i(stateI);
		file.b(killSwitch);
		file.f(nTimer);
		
	}
	
	void setState(State s, float duration) {
		s.activate(this, duration);
		state = s;
	}
	
	@Override
	protected boolean update(float ds) {

		int ox = physics.tileC().x();
		int oy = physics.tileC().y();
		physics.move(this, speed, ds);
		
		if (!domesticated()) {
			nTimer -= ds;
			
			if (nTimer < 0) {
				nTimer = RND.rFloat(20);
				ENTITY scared = null;;
				for (ENTITY e : SETT.ENTITIES().getInProximity(this, 5)) {
					if (e instanceof Animal) {
						if (scared != null)
							((Animal) e).scare(scared, false);
					}else if (!(e instanceof Humanoid) || HPoll.Handler.scaresAnimal((Humanoid) e)) {
						scare(e, false);
						scared = e;
					}
				}
			}
		}
		
		if (!state.update(this, ds)) {
			
			
			if (!speed.isZero()) {
				setState(State.STAND, 1);
			}else if (RND.rInt(4) == 0) {
				if (spot() != null &&  !domesticated()) {
					if (!spot().active()) {
						killSwitch ++;
						if (killSwitch >= 20) {
							kill(false, false);
							return false;
						}
						setState(State.WALK_RANDOM, 0.25f + RND.rFloat(1));	
					}else if (COORDINATE.tileDistance(physics.tileC(),spot()) > 8) {
						killSwitch ++;
						if (killSwitch >= 48) {
							kill(false, false);
							return false;
						}
						speed.turn2(this, spot().x()*C.TILE_SIZE, spot().y()*C.TILE_SIZE);
						setState(State.WALK_RANDOM, 1f + RND.rFloat(2));
						
					}else {
						killSwitch = 0;
						if (RND.rInt(8) == 0)
							setState(State.WALK_RANDOM, 4 + RND.rFloat(10));
						else
							setState(State.WALK_RANDOM, 0.25f + RND.rFloat(1));	
					}
					
					
				}else
					setState(State.WALK_RANDOM, 0.25f + RND.rFloat(1));	
			} else if (RND.rBoolean()){
				speed.turnRandom();
				setState(State.STAND, 5 + RND.rFloat(5));
				if (!domesticated || RND.oneIn(4))
					species().sounds.rnd(body(), 0.25 + RND.rFloat()*0.5);
			}else {
				setState(State.GRACE, 8+RND.rFloat()*8);
			}
		}
		
		ResolverTile.collide(this);
		
		if (isRemoved())
			return false;
		
		if (!physics.tileC().isSameAs(ox, oy)) {
			if (domesticated) {
				if (!(SETT.ROOMS().map.get(physics.tileC()) instanceof PastureInstance) || ROOM_PASTURE.isGate(ox, oy) || ROOM_PASTURE.isGate(physics.tileC().x(), physics.tileC().y())){
					Room r = ROOMS().map.get(ox, oy);
					if (r == null || !(r instanceof PastureInstance)) {
						helloMyNameIsInigoMontoyaYouKilledMyFatherPrepareToDie();
						return false;
					}
					for (DIR d : DIR.ALLC) {
						int dx = ox + d.x();
						int dy = oy + d.y();
						if (r.isSame(ox, oy, dx, dy) && !ROOM_PASTURE.isGate(dx, dy) && !SETT.PATH().solidity.is(dx, dy)) {
							physics.body().moveC((dx)*C.TILE_SIZE + C.TILE_SIZEH, (dy)*C.TILE_SIZE + C.TILE_SIZEH);
							break;
						}
					}
					
				}
			}
			
			inWater = TERRAIN().WATER.isOpenNonFrozen(physics.tileC().x(), physics.tileC().y()) || ROOM_BATH.isPool(physics.tileC().x(), physics.tileC().y());
		}

		if (!domesticated && !isRemoved()) {
			
			if (upHour == TIME.hours().bitCurrent()) {
				if (cub && TIME.days().bitsSinceStart()-birthDay < 14) {
					cub = false;
					PATH().finders.entity.report(this, 1);
				}
				
				if (TIME.days().bitCurrent()-birthDay > lifeSpan) {
					kill(false, false);
					return false;
				}
				

				if (upHour == 0) {
					upHour = (byte) (TIME.hours().bitsPerCycle()-1);
				}else {
					upHour --;
				}
			}
			
			if (damage > 1) {
				
				if (damage == 2) {
					kill(true, false);
					
				}else {
					kill(false, false);
				}
				return false;
			}
		}
		
		return true;
	}

	@Override
	public void render(Renderer r, ShadowBatch s, float ds, int offsetX, int offsetY) {
		state.sprite(this).render(this, false, r, s, ds, offsetX, offsetY);
	}
	
	public AnimalSpecies species(){
		return ANIMALS().species.getAt(def);
	}
	
	@Override
	public void hover(GBox box) {
		
		box.title(species().name);
		box.add(species().icon);
		box.NL();
		if (cub) {
			box.text(¤¤Cub);
			box.NL();
			box.text(species().desc);
			return;
		}
		
		box.text(species().desc);
		
		box.NL();
		if (domesticated()) {
			box.text(¤¤domestcated);
			return;
		}
		
		for (int i = 0; i < species().resources().size(); i++) {
			box.setResource(species().resources().get(i),	species().resAmount(i, physics.getMass()));
		}
		
		if (S.get().developer || S.get().debug) {
			box.NL();
			box.text(state.name());
			box.NL();
			box.add(box.text().add(spot() != null));
			box.NL();
			box.add(box.text().add(killSwitch));
		}
	}
	
	@Override
	public void click() {
		
	}
	
	public void scare(ENTITY other, boolean flee) {
		state.scare(this, other, flee);
	}

	@Override
	public void collide(ECollision coll) {
		
		if (!state.wantsToCollide(this, coll.momentum)) {
			state.collide(this, coll.other, coll.norX, coll.norY, 0);
			return;
		}
		state.collide(this, coll.other, coll.norX, coll.norY, coll.momentum);
		
		coll.momentum *= 1.0 + coll.pierceDamage*RND.rFloat();
		
		if (coll.momentum > 7*species().momTreshold) {
			damage += 2f;
			//Settlement.THINGS().gore.explode(this);
//			Settlement.ANIMALS().cadavers.gore(body().cX(), body().cY(), species());
		}else if(coll.momentum > 3.5*species().momTreshold) {
			coll.momentum -= 1.5*species().momTreshold;
			coll.momentum /= 0.5*species().momTreshold;
			damage += coll.momentum;
//			
//			health -= (momentum-1.5*momentumThreshold)/0.5*momentumThreshold;
//			if (health < 0) {
//				Settlement.ANIMALS().cadavers.normal(body().cX(), body().cY(), 10, 3, 1.0f, species(), speed.getD().id());
//			}
			
		}
		
	}
	
	@Override
	public void collideUnconnected() {
		state.collideUnwalkable(this);
	}
	
	@Override
	protected void meet(ENTITY other) {
		state.meet(this, other);
	}

	@Override
	public boolean collideTile(boolean broken, double norX, double norY, double momentum, int tx, int ty) {
		return state.collideTile(this, broken, norX, norY, momentum);
	}

	@Override
	public COLOR minimapColor() {
		return COLOR.WHITE2WHITE;
	}
	
	public Cadaver slaugher() {
		if (isRemoved())
			throw new RuntimeException();
		
		damage+= 0.25;
		if (damage > 1f)
			damage = 1f;
		
		return kill(false, true);
	}
	
	Cadaver kill(boolean gore, boolean hunted) {
		if (isRemoved())
			throw new RuntimeException();
		helloMyNameIsInigoMontoyaYouKilledMyFatherPrepareToDie();
		if (gore)
			SETT.THINGS().gore.explode(this, species().blood);
		if (TERRAIN().WATER.DEEP.is(physics.tileC()))
			return null;
		
		if (hunted) {
			if (!domesticated && (RND.oneIn(7) || cub)) {
				
			}
			return THINGS().cadavers.normal(body().cX(), body().cY(), physics.getMass(), damage, species(), speed.dir().id());		
		}
		
		if (cub) {
			return null;
		}
		if (gore) {
			return THINGS().cadavers.gore(body().cX(), body().cY(), species());
		}
		return THINGS().cadavers.normal(body().cX(), body().cY(), physics.getMass(), damage, species(), speed.dir().id());		
	}

	@Override
	protected void removeAction() {
		if (spot() != null)
			spot().deregisterAnimal();
		if (domesticated) {
			Room r = ROOMS().map.get(ssx(), ssy());
			if (r != null && r instanceof PastureInstance) {
				((PastureInstance) r).removeAnimal();
			}else {
				GAME.Notify("weird!");
			}
		}
	}

	@Override
	public boolean willCollideWith(ENTITY other) {
		if (domesticated())
			return false;
		return state.willCollideWith(this, other);
	}

	public boolean domesticated() {
		return domesticated;
	}
	
	public void domesticate() {
		reserveCancel();
		PATH().finders.entity.report(this, -1);
		domesticated = true;
	}
	
	public AnimalSpawnSpot spot() {
		if (spotI < 0)
			return null;
		return ANIMALS().spawn.all().get(spotI);
	}
	

	@Override
	protected void setCollideDamage(ECollision coll) {
		if (!cub && !domesticated())
			coll.pierceDamage = !(coll.other instanceof Animal) && !domesticated ? coll.dirDot*0.4-RND.rFloat() : 0;
	}

	@Override
	public double getDefenceSkill(double dirDot) {
		return (dirDot + 1)*0.2;
	}
	
	@Override
	protected double height() {
		return physics.getHeight();
	}

	@Override
	public void renderSimple(Renderer r, ShadowBatch shadows, float ds, int offsetX, int offsetY) {
		render(r, shadows, ds, offsetX, offsetY);
		
	}

	public boolean reservable() {
		return !cub && !domesticated && !isBeeingHunted && !isRemoved();
	}
	
	public boolean reserved() {
		return !cub && !domesticated && isBeeingHunted && !isRemoved();
	}
	
	public void reserve() {
		if (reservable()) {
			PATH().finders.entity.report(this, -1);
			isBeeingHunted = true;
			return;
		}
		throw new RuntimeException(!cub + " " + !domesticated + " " + !isBeeingHunted + " " + !isRemoved());
	}
	
	public void reserveCancel() {
		if (reserved()) {
			isBeeingHunted = false;
			PATH().finders.entity.report(this, 1);
		}
	}

	public boolean isBaby() {
		return cub;
	}

	@Override
	protected boolean collides() {
		return true;
	}



	
}
