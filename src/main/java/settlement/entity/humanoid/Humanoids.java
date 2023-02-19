package settlement.entity.humanoid;

import static settlement.main.SETT.*;

import java.io.IOException;

import init.C;
import init.race.RACES;
import init.race.Race;
import init.sprite.ICON.MEDIUM;
import init.sprite.SPRITES;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.ai.main.AI;
import settlement.main.CapitolArea;
import settlement.main.SETT;
import settlement.main.SETT.SettResource;
import settlement.stats.CAUSE_ARRIVE;
import settlement.stats.CAUSE_LEAVE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.sets.*;
import util.gui.misc.GButt.Panel;
import view.sett.IDebugPanelSett;
import view.tool.PLACABLE;
import view.tool.PlacableSimple;

public final class Humanoids extends SettResource{
	
	{AI.init();}

	
	@Override
	protected void save(FilePutter file) {

	}
	
	@Override
	protected void load(FileGetter file) throws IOException {

	}
	
	@Override
	protected void clearBeforeGeneration(CapitolArea area) {

	}
	
	public Humanoids(){
		
		
		final LinkedList<PLACABLE> all = new LinkedList<PLACABLE>();
		HTYPE[] tt = new HTYPE[] {
			HTYPE.SUBJECT, HTYPE.PRISONER, HTYPE.SLAVE, HTYPE.CHILD, HTYPE.DERANGED, HTYPE.ENEMY, HTYPE.RIOTER
		};
		for (HTYPE t : tt) {
			for (Race r : RACES.all()) {
				Placer p = new Placer(r, t);
				all.add(p);
			}
		}
		
		PlacableSimple death = new PlacableSimple("kill", "") {
			
			private CAUSE_LEAVE cause = CAUSE_LEAVE.AGE;
			ArrayList<CLICKABLE> butts = new ArrayList<CLICKABLE>(CAUSE_LEAVE.ALL().size());
			
			
			{
				for (CAUSE_LEAVE l : CAUSE_LEAVE.ALL()) {
					
					butts.add(new Panel(SPRITES.icons().s.dot) {
						@Override
						protected void clickA() {
							cause = l;
						};
						
						@Override
						protected void renAction() {
							selectedSet(cause == l);
						};
					}.hoverInfoSet(l.desc));
				}
			}
			
			
			@Override
			public PLACABLE getUndo() {
				return null;
			}
			
			@Override
			public MEDIUM getIcon() {
				return SPRITES.icons().m.cancel;
			}

			
			@Override
			public void place(int x, int y) {
				for (ENTITY e : ENTITIES().getAtPointL(x, y)) {
					if (e instanceof Humanoid) {
						((Humanoid) e).kill(false, cause);
						return;
					}
				}
			}
			
			@Override
			public CharSequence isPlacable(int x, int y) {
				for (ENTITY e : ENTITIES().getAtPointL(x, y)) {
					if (e instanceof Humanoid) {
						return null;
					}
				}
				return E;
			}
			
			@Override
			public LIST<CLICKABLE> getAdditionalButt() {
				return butts;
			}
		};
		
		all.add(death);
		
		PlacableSimple explode = new PlacableSimple("explode", "") {
			
			private CAUSE_LEAVE cause = CAUSE_LEAVE.SLAYED;
			
			
			@Override
			public PLACABLE getUndo() {
				return null;
			}
			
			@Override
			public MEDIUM getIcon() {
				return SPRITES.icons().m.cancel;
			}

			
			@Override
			public void place(int x, int y) {
				for (ENTITY e : ENTITIES().getAtPointL(x, y)) {
					if (e instanceof Humanoid) {
						((Humanoid) e).inflictDamage(10, 10, cause);
						return;
					}
				}
			}
			
			@Override
			public CharSequence isPlacable(int x, int y) {
				for (ENTITY e : ENTITIES().getAtPointL(x, y)) {
					if (e instanceof Humanoid) {
						return null;
					}
				}
				return E;
			}

		};
		
		all.add(explode);
		
		IDebugPanelSett.add("humanoids", new ArrayList<PLACABLE>(all));
		
		
		
	}
	
	public Humanoid create(Race r, int tx, int ty, HTYPE t, CAUSE_ARRIVE cause) {
		if (!SETT.IN_BOUNDS(tx, ty))
			throw new RuntimeException(tx + " " + ty);
		int x = tx*C.TILE_SIZE + C.TILE_SIZEH;
		int y = ty*C.TILE_SIZE + C.TILE_SIZEH;
		return new Humanoid(x, y, r, t, cause);
	}
	
	private class Placer extends PlacableSimple{

		private final Race r;
		private final HTYPE f;
		
		private Placer(Race r, HTYPE f) {
			super(r.info.name + " " + f.name, "");
			this.r = r;
			this.f = f;
		}
		
		@Override
		public MEDIUM getIcon() {
			return r.appearance().icon;
		}

		@Override
		public void place(int x, int y) {
			if (isPlacable(x, y) == null){
				new Humanoid(x,y,r, f, CAUSE_ARRIVE.IMMIGRATED);
				return;
			}
		}

		@Override
		public CharSequence isPlacable(int x, int y) {
			int x1 = (x-r.physics.hitBoxsize()/2)/C.TILE_SIZE;
			int x2 = (x+r.physics.hitBoxsize()/2)/C.TILE_SIZE;
			int y1 = (y-r.physics.hitBoxsize()/2)/C.TILE_SIZE;
			int y2 = (y+r.physics.hitBoxsize()/2)/C.TILE_SIZE;
			if (!IN_BOUNDS(x1, y1) ||!IN_BOUNDS(x2, y2))
				return E;
			return !PATH().solidity.is(x1, y1) && !PATH().solidity.is(x2, y1) &&
					!PATH().solidity.is(x1, y2) && !PATH().solidity.is(x2, y2) &&
					ENTITIES().getAtPoint(x,y) == null ? null : E;
		}

		@Override
		public PLACABLE getUndo() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	
}
