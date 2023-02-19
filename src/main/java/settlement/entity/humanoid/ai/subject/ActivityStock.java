package settlement.entity.humanoid.ai.subject;

import static settlement.main.SETT.*;

import init.C;
import init.D;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.entity.humanoid.ai.subject.AIModule_Subject.Activity;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;

final class ActivityStock extends Activity{

	private static CharSequence ¤¤watchingStocks = "Watching a prisoner";
	
	static {
		D.ts(ActivityStock.class);
	}
	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		return start.set(a, d);
	}
	

	private final Resumer start = new Resumer(¤¤watchingStocks) {
		
		
		
		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			if (PATH().finders.stocks.request(a, d.path))
				return AI.SUBS().STAND.activateRndDir(a, d, 4);
			return null;
		}
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			if (AIModules.current(d) != null && AIModules.current(d).moduleCanContinue(a, d)) {
				if (PATH().finders.stocks.checkAndSetRequest(a.tc().x(), a.tc().y(), d.path)) {
					return walk.set(a, d);
				}
				return AI.SUBS().STAND.activateRndDir(a, d, 1+RND.rInt(4));
			}
			return null;
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
		}
	};
	
	private final Resumer walk = new Resumer(¤¤watchingStocks) {

		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			d.planByte1 = 16;
			
			
			if (d.path.isSuccessful()) {
				d.planTile.set(d.path.destX(), d.path.destY());
				return AI.SUBS().walkTo.around(a, d, d.path.destX(), d.path.destY());
			}
			return null;
			
		}
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			
			if (!shouldMove(a, a.tc().x(), a.tc().y()))
				return watch.set(a, d);
			
			DIR dir =  DIR.get(a.tc(), d.planTile);
			
			for (int i = 0; i < 8; i++) {
				dir = dir.next(i);
				int dx = a.tc().x() + dir.x();
				int dy = a.tc().y() + dir.y();
				if (isSpot(dx, dy)) {
					if (!shouldMove(a, dx, dy)) {
						return AI.SUBS().walkTo.cooFull(a, d, dx, dy);
					}
				}
			}
			
			return watch.set(a, d);
			
		}
		
		private boolean shouldMove(Humanoid a, int cx, int cy) {
			for (ENTITY e : SETT.ENTITIES().getAtTile(cx, cy)){
				if (e != a && e instanceof Humanoid && e.speed.magnitude() == 0)
					return true;
			}
			return false;
		}
		
		private boolean isSpot(int tx, int ty) {
			if (!SETT.IN_BOUNDS(tx, ty))
				return false;
			if (SETT.ROOMS().map.is(tx, ty))
				return false;
			AVAILABILITY av = PATH().availability.get(tx,ty);
			if (av.player >= 0 && av.player < AVAILABILITY.Penalty && av.from == 0) {
				return true;
			}
			return false;
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			 
		}
		
	};
	
	private final Resumer watch = new Resumer(¤¤watchingStocks) {
		
		private final AISUB th = new AISUBS.Throw() {
			
			@Override
			public int destY(Humanoid a, AIManager d) {
				return d.planTile.y()*C.TILE_SIZE + C.TILE_SIZEH;
			}
			
			@Override
			public int destX(Humanoid a, AIManager d) {
				return d.planTile.x()*C.TILE_SIZE + C.TILE_SIZEH;
			}
		};
		
		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			d.planByte1 = 16;
			return res(a, d);
			
		}
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			
			d.planByte1 --;
			if (d.planByte1 <= 0 || !SETT.ROOMS().STOCKS.isStock(d.planTile.x(), d.planTile.y()))
				return null;
			
			DIR dir=  DIR.get(a.tc(), d.planTile);
			if (RND.oneIn(4)) {
				dir = dir.next(-1 + RND.rInt(2));
				a.speed.setDirCurrent(dir);
				return AI.SUBS().STAND.activateTime(a, d, 2 + RND.rInt(4));
			}
			
			a.speed.setDirCurrent(dir);
			
			if (RND.oneIn(4) && SETT.ROOMS().STOCKS.isUsed(d.planTile.x(), d.planTile.y()))
				return th.activate(a, d);
			
			return AI.SUBS().STAND.activateTime(a, d, 2 + RND.rInt(4));
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			 
		}
		
	};

}
