package settlement.entity.humanoid;

import static settlement.main.SETT.*;

import java.io.IOException;

import game.GAME;
import game.nobility.Nobility;
import game.time.TIME;
import init.C;
import init.boostable.BOOSTABLES;
import init.race.Race;
import init.sound.SOUND;
import settlement.army.Div;
import settlement.army.DivMorale;
import settlement.entity.ENTITY;
import settlement.entity.ResolverTile;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.spirte.HSprite;
import settlement.main.SETT;
import settlement.room.main.RoomInstance;
import settlement.room.service.hygine.bath.ROOM_BATH;
import settlement.stats.*;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import util.dic.DicMisc;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import util.rendering.ShadowBatch;
import view.main.VIEW;

public class Humanoid extends ENTITY{
	
	public final static int WORK_TICKS = 10;
	public static double WORK_PER_DAY = WORK_TICKS/16.0;
	public static double WORK_PER_DAYI = 1.0/WORK_PER_DAY;
	public static final double WALK_SPEED = 1.5;
	
	final AIManager ai;
	private final Induvidual induvidual;
	public byte spriteoff = (byte) RND.rInt(255);
	public float spriteTimer = 0;
	public float relTimer = 0;

	
	private float updateTimer = (float) (RND.rFloat()*HumanoidResource.updateDelta);
	private byte dayOfYear = (byte) TIME.days().bitCurrent();

	private byte updateI = (byte) RND.rInt(255);
	private byte dayRan = (byte) RND.rInt(256);;
	private float moveBonus;
	public boolean inWater;
	private byte office = -1;
	private byte leaveCause = -1;
	private byte mark;
	public static int TARGET_MAX = 10;
	
	public Humanoid(int x, int y, Race spec, HTYPE type, CAUSE_ARRIVE cause){
		
		induvidual = new Induvidual(type, spec, cause);
		
		physics.initPosition(x, y, spec.physics.hitBoxsize(), spec.physics.hitBoxsize()); 
		
		physics.setRestitution(0.2f);
		physics.setHeight(spec.physics.height() + RND.rFloat0(spec.physics.height()/4.0f));
		
		physics.setMass(BOOSTABLES.PHYSICS().MASS.get(induvidual));
		speed.accelerationInit(BOOSTABLES.PHYSICS().ACCELERATION.get(induvidual)*C.TILE_SIZE);
		speed.magnitudeMaxInit(BOOSTABLES.PHYSICS().SPEED.get(induvidual)*C.TILE_SIZE);
		speed.turnRandom();
		
		ai = new AIManager(this);
		
		
//		if (division() != null) {
//			division().reporter.reportPosition(POPSTATS().division.spot(this), body().cX(), body().cY());
//		}
		
		add();
		initTile(-1, -1);
		
		if (isRemoved()) {
			removeAction();
		}else {
			ai.init(this);
			if (type.player)
				GAME.stats().SUBJECTS.inc(1);
		}
		
		
	}
	
	public Humanoid(FileGetter file) throws IOException{
		super.load(file);
		ai = new AIManager(file);
		induvidual = new Induvidual(file);
		spriteoff = file.b();
		spriteTimer = file.f();
		relTimer = file.f();
		updateTimer = file.f();
		dayOfYear = file.b();
		updateI = file.b();
		dayRan = file.b();
		moveBonus = file.f();
		inWater = file.bool();
		office = file.b();
		leaveCause = file.b();
	}
	
	@Override
	protected void save(FilePutter file) {
		super.save(file);
		ai.save(file);
		induvidual.save(file);
		file.b(spriteoff);
		file.f(spriteTimer);
		file.f(relTimer);
		file.f(updateTimer);
		file.b(dayOfYear);
		file.b(updateI);
		file.b(dayRan);
		file.f(moveBonus);
		file.bool(inWater);
		file.b(office);
		file.b(leaveCause);
	}
	
	@Override
	public void render(Renderer r, ShadowBatch s, float ds, int offsetX, int offsetY) {

		int x = body().x1()+offsetX - race().appearance().off;
		int y = body().y1()+offsetY - race().appearance().off;
		
		x += -4 + (spriteoff & 0b0111);
		y += -4 + ((spriteoff>>3) & 0b0111);
		HSprite sprite = ai.sprite(this);
		sprite.render(this, ai, r, s, ds, x, y);
		

		
		
	}
	
	@Override
	public void renderSimple(Renderer r, ShadowBatch shadows, float ds, int offsetX, int offsetY) {
		int x = body().x1()+offsetX - race().appearance().off;
		int y = body().y1()+offsetY - race().appearance().off;
		
		x += -4 + (spriteoff & 0b0111);
		y += -4 + ((spriteoff>>3) & 0b0111);
		HSprite sprite = ai.sprite(this);
		sprite.renderSimple(this, ai, r, shadows, ds, x, y);
		
//		if (hovered && GSettings.get().devMode.isOn()) {
//			ai.path().render(r, offsetX, offsetY);
//		}
	}

	public Race race(){
		return induvidual.race();
	}
	
	private void initTile(int ox, int oy) {
		inWater = TERRAIN().WATER.isOpenNonFrozen(physics.tileC().x(), physics.tileC().y());
		if (inWater) {
			int d = SETT.WEATHER().temp.cold() < 0 ? -1 : 0;
			STATS.NEEDS().EXPOSURE.count.inc(indu(), d);
		}else if (ROOM_BATH.isPool(physics.tileC().x(), physics.tileC().y())) {
			inWater = true;
		}
		moveBonus = (float) (PATH().availability.get(physics.tileC()).movementSpeed * (1.0 - 0.5*STATS.NEEDS().INJURIES.count.getD(indu())));
	}
	
	@Override
	protected boolean update(float ds) {
		
		int cx = body().cX();
		int cy = body().cY();
		
		int ox = physics.tileC().x();
		int oy = physics.tileC().y();
//		int nx = ((int)ai.X) >> C.T_SCROLL;
//		int ny = ((int)ai.Y) >> C.T_SCROLL;
//		AISUB s = ai.plansub();
//		AISTATE ss = ai.state();
//		int sb = ai.subByte;
		
		
		physics.move(this, speed, ds*moveBonus);
		
		((HumanoidResource) ai).update(this, ds);

		if (ResolverTile.collide(this)) {
		}
		if (isRemoved())
			return false;
		
		if (!physics.tileC().isSameAs(ox,oy)) {
			initTile(ox, oy);
			if (RND.oneIn(18) && !ROOMS().map.is(physics.tileC())) {
				int x = physics.tileC().x() + RND.rInt0(9)/8;
				int y = physics.tileC().y() + RND.rInt0(9)/8;
				if (IN_BOUNDS(x, y) && !ROOMS().map.is(x,y)) {
					SETT.TILE_MAP().growth.tear(x, y);
//					if (TERRAIN().clearing.get(x, y).isEasilyCleared())
//						TERRAIN().clearing.get(x, y).clearAll(x, y);
					GRASS().currentI.increment(x, y, -1);
				}
			}
		}


		if (AIManager.dead != null) {
			kill(false, AIManager.dead);
			AIManager.dead = null;
			return false;
		}
		
		int uS = (int) updateTimer;
		updateTimer -= ds;
		int uSN = (int) updateTimer;
		
		if (uS != uSN) {
			if (!inWater && (uS & 0b001) == 0) {
				if (STATS.NEEDS().INJURIES.count.get(induvidual) > 0 && RND.rBoolean()) {
					SETT.THINGS().gore.bleed(this, race().appearance().colors.blood);
				}
			}
			if ((uS & 0b0111) == 0) {
				HEvent.Handler.exhaust(this);
			}else if((uS & 0b0111) == 1) {
				HEvent.Handler.checkMorale(this);
			}
		}
		
		if (updateTimer <= 0) {
			boolean day = false;
			
			if (dayOfYear != TIME.days().bitCurrent()) {
				
				int now = (int) (TIME.days().bitPartOf()*HumanoidResource.updatesPerDay);
				int db = getDayBreakTick();
				if (now >= db) {
					day = true;
					dayOfYear = (byte) TIME.days().bitCurrent();
				}
				
				
			}
			
			updateTimer += HumanoidResource.updateDelta;
			
			updateI++;
			if (day)
				dayRan = (byte) RND.rInt(256);
			((HumanoidResource) ai).update(this, updateI, day);
			if (isRemoved())
				return true;
			((HumanoidResource) induvidual).update(this, updateI&0x0FF, day);
			mark -= 1;
			
			physics.setMass(BOOSTABLES.PHYSICS().MASS.get(induvidual));
			speed.accelerationInit(Math.max(0.2, BOOSTABLES.PHYSICS().ACCELERATION.get(induvidual))*C.TILE_SIZE);
			speed.magnitudeMaxInit(Math.max(0.2, BOOSTABLES.PHYSICS().SPEED.get(induvidual))*C.TILE_SIZE);
		}
		
		
		if (AIManager.dead != null) {
			kill(AIManager.deadGore, AIManager.dead);
			AIManager.dead = null;
		}else if (division() != null && (body().cX() != cx || body().cY() != cy)) {
			division().reporter.reportPosition(divSpot(), body().cX(), body().cY());
		}
		return true;
	}

	@Override
	public void hover(GBox text) {
		VIEW.s().ui.subjects.hoverInfo(this, text);
		text.NL();
		text.add(GFORMAT.f(text.text(), ai.stateTimer));
		text.NL();
		text.text(""+tc());
	}
	
	
	@Override
	public void click() {
		VIEW.s().ui.subjects.showSingle(this);
	}
	
	@Override
	public boolean canBeClicked() {
		return VIEW.s().ui.subjects.canShow(this);
	}
	
	
	
	public boolean inflictDamage(double damage, double damageForce, CAUSE_LEAVE cause) {
		
		leaveCause = (byte) cause.index();
		
		double d = Math.max(damage, damageForce);
		
		if (d > 0.1) {
			SETT.THINGS().gore.bleed(this, race().appearance().colors.blood);
		}
		
		if (d > 0.2) {
			SETT.THINGS().gore.cloud(this, race().appearance().colors.blood);
		}
		
		if (damageForce > 1) {
			SOUND.sett().action.squish.rnd(physics.body());
			SETT.THINGS().gore.explode(this, race().appearance().colors.blood);
			STATS.NEEDS().INJURIES.count.setD(induvidual, 1.0);
			if (division() != null) {
				DivMorale.CASULTIES.incD(division(), 1);
			}
			kill(true, CAUSE_LEAVE.ALL().get(leaveCause));
			return false;
		}else {
			double inj = STATS.NEEDS().INJURIES.count.getD(induvidual);
			inj += damage;
			inj = CLAMP.d(inj, 0, 1);
			
			
			if (inj >= 1.0) {
				STATS.NEEDS().INJURIES.count.incD(induvidual, damage*RND.rFloat());
				if (division() != null) {
					DivMorale.CASULTIES.incD(division(), 1);
				}
				kill(false, CAUSE_LEAVE.ALL().get(leaveCause));
				return false;
			}else {
				STATS.NEEDS().INJURIES.count.setD(induvidual, inj);
			}
		}
		
		
		
		return true;
	}
	
	public void kill(boolean gore, CAUSE_LEAVE cause) {
		if (isRemoved())
			return;

		if (indu().hType() == HTYPE.ENEMY) {
			GAME.stats().ENEMIES_KILLED.inc(1);
		}
		
		STATS.POP().COUNT.reg(indu(), cause);
		
		if (cause.leavesCorpse) {
			SETT.THINGS().corpses.create(
					induvidual, body(), speed.dir(), 
					!gore, cause);
			if (!VIEW.b().isActive()) {
				if (indu().hType().player || RND.oneIn(5))
					STATS.EQUIP().drop(this);
				
			}
		}
		
		helloMyNameIsInigoMontoyaYouKilledMyFatherPrepareToDie();
		
	}

	@Override
	public void collide(ECollision coll){
		HEvent.Handler.collide(this, ai, coll);
	}
	
	@Override
	protected void meet(ENTITY other) {
		HEvent.Handler.meet(this, ai, other);
	}
	
	@Override
	protected boolean collides() {
		return HPoll.Handler.collides(this, ai);
	}
	
	@Override
	protected boolean willCollideWith(ENTITY other) {
		return HPoll.Handler.willCollideWith(this, ai, other);
		//return ai.collider(this).isColliding(ai, this, other);
	}
	
	@Override
	public boolean collideTile(boolean broken, double norX, double norY, double force, int tx, int ty) {
		return HEvent.Handler.collideTile(this, ai, norX, norY, force, broken, tx, ty);
		//return ai.collider(this).collideTile(this, ai, norX, norY, force, broken, tx, ty);
	}
	
	@Override
	public void collideUnconnected() {
		HEvent.Handler.collisionUnreachable(this);
	}

	@Override
	public COLOR minimapColor() {
		if (indu().hostile())
			return COLOR.RED100;
		return COLOR.GREEN80;
	}
	
	
	
	@Override
	protected void removeAction() {
		((HumanoidResource) ai).cancel(this);
		((HumanoidResource) induvidual).cancel(this);
		if (office() != null) {
			GAME.NOBLE().vacate(this, office);
		}
		
//		if (ai.subPathByte >= 0 && ai.subPathByte < FinderRoomService.all().size() && 
//				FinderRoomService.all().get(ai.subPathByte) != null 
//				&& FinderRoomService.all().get(ai.subPathByte) != Settlement.ROOMS().SERVICE.BATH.finder 
//				&& FinderRoomService.all().get(ai.subPathByte).get(ai.path.destX(), ai.path.destY()) != null 
//				&& FinderRoomService.all().get(ai.subPathByte).get(ai.path.destX(), ai.path.destY()).findableReservedIs())
//			ai.debug(this, FinderRoomService.all().get(ai.subPathByte) + " " + ai.path.toDebugString());

	}

	@Override
	protected void setCollideDamage(ECollision coll) {
		HPoll.Handler.collideDamage(this, ai, coll);
	}
	
	@Override
	public double getDefenceSkill(double dirDot) {
		return HPoll.Handler.defense(this, dirDot);
	}
	
	public Induvidual indu() {
		return induvidual;
	}
	
	public HAI ai() {
		return ai;
	}
	
	public CAUSE_LEAVE lastLeaveCause() {
		if (leaveCause == -1)
			return null;
		return CAUSE_LEAVE.ALL().get(leaveCause);
	}
	
	private int getDayBreakTick() {
		int db = (spriteoff&0b011);
		
		RoomInstance w = STATS.WORK().EMPLOYED.get(induvidual);
		if (w != null) {
			db += (int) (w.blueprintI().employment().getShiftStart()*0x0F);
			if ((induvidual.randomness() & 1) == 1 && w.blueprintI().employment().worksNights()) {
				db += 8;
				db &= 0x0F;
			}
		}else{
			db += 0.325*0x0F;
		}
		return db;
	}
	

	
	public void setDivision(Div div) {
		STATS.BATTLE().DIV.set(this, div);
		if (div != null)
			division().reporter.reportPosition(divSpot(), body().cX(), body().cY());
	}
	
	public void teleportAndInitInDiv() {
		if (division() == null)
			return;
		if (!division().settings.mustering())
			return;
		COORDINATE de = division().reporter.getPixel(divSpot());
		if (de == null)
			return;
		physics.body().moveC(de);
		ai.muster(this);
		SETT.ENTITIES().move(this);
		if (division() == null)
			return;
		speed.setDirCurrent(division().dir());
		division().reporter.reportPosition(divSpot(), body().cX(), body().cY());
		
	}
	
	public Div division() {
		return STATS.BATTLE().DIV.get(this);
	}
	
	public static abstract class HumanoidResource {

		public final static int updatesPerDay = 16;
		public final static double updateDelta = (TIME.secondsPerHour*TIME.hoursPerDay / updatesPerDay);
		public final static int byteDelta = 256 / updatesPerDay;
		public static CAUSE_LEAVE dead;
		public static boolean deadGore;

		protected abstract void update(Humanoid h, int updateI, boolean newDay);
		protected abstract void update(Humanoid h, float ds);
		protected abstract void cancel(Humanoid h);
		
		protected abstract void save(FilePutter file);
		
	}

	@Override
	protected double height() {
		return physics.getHeight()*ai.sprite(this).height;
	}

	public short divSpot() {
		return (short)STATS.BATTLE().position(indu());
	}

	public int dayRan() {
		return dayRan;
	}
	
	public Nobility office() {
		if (office == -1) {
			return null;
		}
		return GAME.NOBLE().ALL().get(office);
	}
	
	public void officeSet(Nobility n) {
		if (office() != null)
			throw new RuntimeException();
		HTypeSet(HTYPE.NOBILITY, CAUSE_LEAVE.OTHER, null);
		office = GAME.NOBLE().assign(this, n);
	}
	
	public void HTypeSet(HTYPE t, CAUSE_LEAVE leave, CAUSE_ARRIVE arr) {
		ai.changeType(this, t, leave, arr);
	}
	
	public void interrupt() {
		ai.overwrite(this, AI.plans().NOP);
	}
	
	public CharSequence title() {
		if (office() != null)
			return office().info().name;
		
		if (indu().hType().works) {
			if (STATS.WORK().EMPLOYED.get(indu()) == null)
				return DicMisc.¤¤Oddjobber;
			return STATS.WORK().EMPLOYED.get(indu()).blueprintI().employment().title;
		}else {
			return indu().hType().name;
		}
	}

	public void target(int amount) {
		mark += amount;
	}
	
	public int targets() {
		return mark;
	}
	
}
