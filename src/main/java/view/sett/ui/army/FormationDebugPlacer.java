package view.sett.ui.army;

import static settlement.main.SETT.*;

import init.C;
import init.config.Config;
import init.race.RACES;
import init.sprite.ICON;
import init.sprite.ICON.MEDIUM;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.army.Army;
import settlement.army.Div;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.HTYPE;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.STAT;
import settlement.stats.STATS;
import settlement.stats.StatsEquippables.EQUIPPABLE_MILITARY;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.*;
import util.data.INT;
import util.data.INT.INTE;
import util.gui.misc.GText;
import util.gui.slider.GAllocator;
import util.gui.slider.GTarget;
import view.tool.PlacableFixedImp;

final class FormationDebugPlacer extends PlacableFixedImp{

	private static final int SIZES = 5;
	private final Army team;
	private final int[] widths = new int[SIZES];
	private final int[] height = new int[SIZES];
	private Div div;
	private final ArrayList<Div> divs = new ArrayList<>(1);
	private final LIST<CLICKABLE> butts;
	
	private final INT.IntImp race = new INT.IntImp(0, RACES.all().size()-1);
	private final LIST<Butt> stats;
	
	public FormationDebugPlacer(Army team) {
		super(team == ARMIES().player() ? "Place Division" : "Place Division Enemy", 1, SIZES);
		this.team = team;
		
		for (int i = 0; i < SIZES; i++) {
			
			double s = (i+1)*Config.BATTLE.MEN_PER_DIVISION/SIZES;
			int w = (int) Math.ceil(Math.sqrt(s));
			int h = (int) Math.ceil(s/w);
			widths[i] = w;
			height[i] = h;
			
		}
		
		LinkedList<Butt> stats = new LinkedList<>();
		
		GuiSection ss = new GuiSection();
		int si = 0;
		
		for (STAT s : STATS.BATTLE().TRAINING_ALL) {
			Butt b = new Butt(s);
			stats.add(b);
			ss.add(b, (si%5)*150, (si/5)*25);
			si++;
		}
		
		for (EQUIPPABLE_MILITARY bb : STATS.EQUIP().military_all()) {
			Butt b = new Butt(bb.stat());
			stats.add(b);
			ss.add(b, (si%5)*150, (si/5)*25);
			si++;
		}
		this.stats = stats;
		CLICKABLE rr = new GTarget(64, false, true, new RENDEROBJ.RenderImp(ICON.MEDIUM.SIZE) {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				RACES.all().getC(race.get()).appearance().icon.render(r, body());
			}
		}, race);
		ss.add(rr, (si%5)*150, (si/5)*25);
		si++;
		
		butts = new ArrayList<CLICKABLE>(ss);
		
		
		
	}
	
	
	@Override
	public MEDIUM getIcon() {
		return SPRITES.icons().m.for_loose;
	}

	
	private static class Butt extends GuiSection implements INTE {

		private int value = 0;
		private final STAT stat;
		
		public Butt(STAT stat) {
			
			this.stat = stat;
			GText s = new GText(UI.FONT().S, stat.info().name);
			s.setMaxChars(4);
			add(s, 0, 0);
			addRightCAbs(30, new GAllocator(COLOR.ORANGE100, this, 4, 16));
			hoverInfoSet(stat.info().name);
		}
		
		@Override
		public int min() {
			return 0;
		}
		
		@Override
		public int max() {
			return stat.indu().max(null);
		}
		
		@Override
		public int get() {
			return value;
		}
		
		@Override
		public void set(int t) {
			value = t;
		}
		
	}

	
	@Override
	public void place(int tx, int ty, int rx, int ry) {
		place(tx, ty);
	}
	
	@Override
	public void afterPlaced(int tx1, int ty1) {
		div.settings.musteringSet(true);
		div.info.race.set(RACES.all().get(race.get()));
		divs.clear();
		divs.add(div);
		int x1 = tx1;
		int y1 = ty1;
		int x2 = x1 + width();
		x1 = x1*C.TILE_SIZE + C.TILE_SIZEH;
		y1 = y1*C.TILE_SIZE + C.TILE_SIZEH;
		x2 = x2*C.TILE_SIZE + C.TILE_SIZEH;
		ARMIES().placer.deploy(divs, x1, x2, y1, y1);
		div.initPosition();
		for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
			if (e instanceof Humanoid) {
				Humanoid h = (Humanoid) e;
				if (h.division() == div)
					h.teleportAndInitInDiv();
			}
		}
	}
	
	private void place(int tx, int ty) {
		
		if (div.menNrOf() < Config.BATTLE.MEN_PER_DIVISION) {
			HTYPE t = team != ARMIES().player() ? HTYPE.ENEMY : HTYPE.SUBJECT;
			Humanoid h = new Humanoid(tx*C.TILE_SIZE+C.TILE_SIZEH, ty*C.TILE_SIZE+C.TILE_SIZEH, RACES.all().get(race.get()), t, null);
			h.setDivision(div);
			for (Butt b : stats) {
				b.stat.indu().set(h.indu(), b.value);
			}
		}
		
	}
		

	private boolean setDiv() {
		for (Div d : team.divisions()) {
			if (d.menNrOf() == 0) {
				div = d;
				
				div.info.men.set(Config.BATTLE.MEN_PER_DIVISION);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public CharSequence placableWhole(int tx1, int ty1) {
		if (!setDiv())
			return E;
		return null;
	}

	@Override
	public CharSequence placable(int tx, int ty, int rx, int ry) {
		if (!IN_BOUNDS(tx, ty))
			return E;
		
		if (PATH().solidity.is(tx, ty)) {
			return E;
		}
		if (ENTITIES().hasAtTile(tx, ty))
			return E;
		
		return null;
	}


	@Override
	public int width() {
		return widths[size()];
	}

	@Override
	public int height() {
		return height[size()];
	}

	@Override
	public LIST<CLICKABLE> getAdditionalButt() {
		return butts;
	}
	
}
