package game.events;

import java.io.IOException;
import java.util.Arrays;

import game.boosting.BOOSTABLES;
import game.events.EVENTS.EventResource;
import game.faction.FACTIONS;
import game.time.TIME;
import init.C;
import init.D;
import settlement.entity.ENTITY;
import settlement.entity.ENTITY.ECollision;
import settlement.entity.EPHYSICS;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.main.*;
import settlement.stats.STATS;
import settlement.stats.util.CAUSE_LEAVE;
import snake2d.util.MATH;
import snake2d.util.datatypes.*;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.gui.GuiSection;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;
import util.gui.misc.GButt;
import view.main.VIEW;
import view.sett.IDebugPanelSett;
import view.tool.PlacableSimple;
import view.ui.message.MessageSection;

public class EventAccident extends EventResource{
	
	private final double RADIUS = C.TILE_SIZE*10;
	private final Rec bounds = new Rec(RADIUS*2);
	private final VectorImp tVec = new VectorImp();
	
	private final ECollision coll = new ECollision();
	
	private static CharSequence ¤¤Accident = "¤Accident!";
	private static CharSequence ¤¤AccidentD = "¤An accident has occurred. {0} subjects were injured and will seek out a hospital. There were {1} deaths.";
	private static CharSequence ¤¤Go = "¤Go to Site";
	
	
	private double[] timers = new double[SETT.ROOMS().all().size()];
	private double timer = 0;
	private final double acI = (timers.length/(TIME.secondsPerDay*16.0));
	
	static {
		D.ts(EventAccident.class);
	}
	
	public EventAccident() {
		IDebugPanelSett.add(new PlacableSimple("event: accident") {
			
			@Override
			public void place(int x, int y) {
				ENTITY e = SETT.ENTITIES().getAtPoint(x, y);
				if (e != null && e instanceof Humanoid) {
					create((Humanoid) e);
				}
			}
			
			@Override
			public CharSequence isPlacable(int x, int y) {
				ENTITY e = SETT.ENTITIES().getAtPoint(x, y);
				if (e != null && e instanceof Humanoid) {
					return null;
				}
				return E;

			}
		});
		clear();
	}
	
	@Override
	protected void update(double ds) {
		int i = (int) timer;
		timer += ds;
		if (i != (int) timer) {
			RoomBlueprint b = SETT.ROOMS().all().get(i);
			if ((b instanceof RoomBlueprintIns<?>)) {
				RoomBlueprintIns<?> ins = (RoomBlueprintIns<?>) b;
				if (b != null && b.employment() != null) {
					double c = acI*b.employment().accidentsPerYear*b.employment().employed();
					c /= 1.0 + BOOSTABLES.CIVICS().ACCIDENT.get(FACTIONS.player());
					c = CLAMP.d(c, 0, 1);
					timers[i] -= c;
					
					if (timers[i] <= 0) {
						create(ins);
					}
				}
			}
				
			
		}
		
		if (timer >= timers.length)
			timer -= timers.length;
		
	}

	@Override
	protected void save(FilePutter file) {
		file.ds(timers);
		file.d(timer);
		
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		file.ds(timers);
		timer = file.d();
		
	}

	@Override
	protected void clear() {
		Arrays.fill(timers, 0);
		timer = 0;
	}
	
	public boolean create(RoomBlueprintIns<?> b){
		
		if (b.employment().employed() >= 0) {
			timers[b.index()] = 1;
			return false;
		}
		
		if (!MATH.isWithin(TIME.days().bitPartOf(), b.employment().getShiftStart()+0.1, b.employment().getShiftStart()+0.6)) {
			return false;
		}
		
		int emp = RND.rInt(b.employment().employed());
		for (int i = 0; i < b.instancesSize(); i++) {	
			RoomInstance ins = b.getInstance((i));
			if (ins.employees().employed() > 0) {
				emp-= ins.employees().employed();
				
				if (emp <= 0) {
					return create(ins);
				}
			}
			
		}
		
		return false;
		
		
	}
	
	public boolean create(RoomInstance ins){
		
		for (Humanoid h : ins.employees().employees()) {
			Room r = STATS.WORK().EMPLOYED.get(h);
			if (r != null && r.blueprint().employment() != null && r == SETT.ROOMS().map.get(h.tc())) {
				timers[ins.blueprint().index()] = 1;
				create(h);
				return true;
			}
		}
		return false;
		
	}
	
	public void poll(Humanoid h) {
		Room r = STATS.WORK().EMPLOYED.get(h);
		if (r != null && r.blueprint().employment() != null && r == SETT.ROOMS().map.get(h.tc())) {
			double ra = r.blueprint().employment().accidentsPerYear;
			double ch = CLAMP.d(ra*acI/(BOOSTABLES.CIVICS().ACCIDENT.get(h.indu())), 0, 1);
			if (ch > RND.rFloat()) {
				create(h);
			}
			
		}
	}
	
	public void create(Humanoid h) {
		
		Room r = STATS.WORK().EMPLOYED.get(h);
		double cx = h.body().cX()+RND.rSign();
		double cy = h.body().cY()+RND.rSign();
		
		double mom = EPHYSICS.MOM_TRESHOLDI + RND.rFloat()*2*EPHYSICS.MOM_TRESHOLDI;

		h.inflictDamage(1.0, CAUSE_LEAVE.ACCIDENT);
		
		
		
		bounds.moveC(cx, cy);
		SETT.THINGS().gore.debris((int)cx, (int)cy, 0, 0);
		
		int inj = 1;
		int deaths = 0;
		
		
		
		for (ENTITY e : SETT.ENTITIES().fill(bounds)) {
			
			if (SETT.ROOMS().map.get(e.tc()) != r)
				continue;
			
			double l = tVec.set(cx, cy, e.body().cX(), e.body().cY());
			if (l > RADIUS)
				continue;
			l = 1.0 - (l / RADIUS);
			e.speed.setRaw(e.speed.x()+tVec.nX()*C.TILE_SIZE*3*l, e.speed.y()+tVec.nY()*C.TILE_SIZE*3*l);
			
			coll.dirDot = 1.0;
			coll.momentum = mom*e.physics.getMass();
			coll.damageStrength = 0;
			coll.norX = tVec.nX();
			coll.norY = tVec.nY();
			coll.leave = CAUSE_LEAVE.ACCIDENT;
			coll.other = null;
			if (e instanceof Humanoid) {
				Humanoid h2 = (Humanoid) e;
				h2.inflictDamage(l*RND.rFloat()*2.0,  CAUSE_LEAVE.ACCIDENT);
				if (e.isRemoved()) {
					deaths ++;
					continue;
				}
			}
			e.collide(coll);
			if (e.isRemoved())
				deaths ++;
			else
				inj++;
		}
		
		new M(¤¤Accident, inj, deaths, h).send();
		
	}
	
	private static class M extends MessageSection {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final int inj;
		private final int deaths;
		private int cx,cy;
		
		public M(CharSequence title, int inj, int deaths, Humanoid h) {
			super(title);
			this.inj = inj;
			this.deaths = deaths;
			cx = h.tc().x();
			cy = h.tc().y();
		}

		@Override
		protected void make(GuiSection section) {
			
			Str s = Str.TMP;
			s.clear();
			s.add(¤¤AccidentD);
			s.insert(0, inj);
			s.insert(1, deaths);
			paragraph(s);
			
			GButt.ButtPanel p = new GButt.ButtPanel(¤¤Go) {
				
				@Override
				protected void clickA() {
					VIEW.s().activate();
					VIEW.s().getWindow().centererTile.set(cx, cy);
					VIEW.messages().hide();
				}
				
			};
			
			section.addRelBody(8, DIR.S, p);
			
		}
		
		
	}

}
