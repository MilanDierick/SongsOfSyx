package settlement.stats;

import init.C;
import init.D;
import init.boostable.BOOSTABLES;
import settlement.entity.ENTITY;
import settlement.entity.ENTITY.ECollision;
import settlement.entity.EPHYSICS;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.main.Room;
import snake2d.util.datatypes.*;
import snake2d.util.gui.GuiSection;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;
import util.gui.misc.GButt;
import view.main.MessageSection;
import view.main.VIEW;
import view.sett.IDebugPanelSett;
import view.tool.PlacableSimple;

final class StatsWorkAccident {
	
	private final double acI = 1.0/(16*16);

	private final double RADIUS = C.TILE_SIZE*10;
	private final Rec bounds = new Rec(RADIUS*2);
	private final VectorImp tVec = new VectorImp();
	
	private final ECollision coll = new ECollision();
	
	private static CharSequence ¤¤Accident = "¤Accident!";
	private static CharSequence ¤¤AccidentD = "¤An accident has occurred. {0} subjects were injured and will seek out a hospital. There were {1} deaths.";
	private static CharSequence ¤¤Go = "¤Go to Site";
	
	static {
		D.ts(StatsWorkAccident.class);
	}
	
	public StatsWorkAccident() {
		IDebugPanelSett.add(new PlacableSimple("accident") {
			
			@Override
			public void place(int x, int y) {
				ENTITY e = SETT.ENTITIES().getAtPoint(x, y);
				if (e != null && e instanceof Humanoid) {
					pcreate((Humanoid) e);
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
	}
	
	void create(Humanoid h) {
		Room r = STATS.WORK().EMPLOYED.get(h);
		if (r != null && r.blueprint().employment() != null) {
			double ra = r.blueprint().employment().accidentsPerYear;
			double ch = CLAMP.d(ra*acI/(BOOSTABLES.CIVICS().ACCIDENT.get(h)), 0, 1);
			if (ch > RND.rFloat()) {
				pcreate(h);
			}
			
		}
	}
	
	private void pcreate(Humanoid h) {
		
		double cx = h.body().cX()+RND.rSign();
		double cy = h.body().cY()+RND.rSign();
		
		double mom = EPHYSICS.MOM_TRESHOLDI + RND.rFloat()*2*EPHYSICS.MOM_TRESHOLDI;

		h.inflictDamage(0.5, 0.5, CAUSE_LEAVE.ACCIDENT);
		
		bounds.moveC(cx, cy);
		SETT.THINGS().gore.debris((int)cx, (int)cy, 0, 0);
		
		int inj = 1;
		int deaths = 0;
		
		for (ENTITY e : SETT.ENTITIES().fill(bounds)) {
			
			double l = tVec.set(cx, cy, e.body().cX(), e.body().cY());
			if (l > RADIUS)
				continue;
			l = 1.0 - (l / RADIUS);
			e.speed.setRaw(e.speed.x()+tVec.nX()*C.TILE_SIZE*3*l, e.speed.y()+tVec.nY()*C.TILE_SIZE*3*l);
			coll.pierceDamage = 0;
			coll.dirDot = 1.0;
			coll.momentum = mom*e.physics.getMass();
			coll.norX = tVec.nX();
			coll.norY = tVec.nY();
			coll.leave = CAUSE_LEAVE.ACCIDENT;
			coll.other = null;
			if (e instanceof Humanoid) {
				Humanoid h2 = (Humanoid) e;
				h2.inflictDamage(RND.rFloat(), RND.rFloat(), CAUSE_LEAVE.ACCIDENT);
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
