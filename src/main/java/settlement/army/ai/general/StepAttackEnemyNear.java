package settlement.army.ai.general;

import java.util.Arrays;

import game.time.TIME;
import init.C;
import init.config.Config;
import settlement.army.Div;
import settlement.army.ai.general.MDivs.MDiv;
import settlement.army.ai.util.ArmyAIUtil;
import settlement.army.ai.util.DivTDataStatus;
import settlement.army.order.DivTDataTask;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.tilemap.terrain.TFortification;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.DIR;
import snake2d.util.sets.ArrayList;

final class StepAttackEnemyNear {
	
	private final Context context;
	
	public StepAttackEnemyNear(Context context) {
		this.context = context;
	}
	
	private int[] attacked = new int[Config.BATTLE.DIVISIONS_PER_ARMY];
	private ArrayList<Div> res = new ArrayList<>(16);
	private final DivTDataStatus status = new DivTDataStatus();
	
	public boolean attackEnemies(MDivs data, UtilEnemyArea blob) {
		
		
		
		Flooder f = context.flooder.getFlooder();
		f.init(this);
		f.pushSloppy(context.getDestCoo(), 0);
		Arrays.fill(attacked, 0);
		
		for (MDiv d : data.activeDivs) {
			if (!d.isDeployed && d.busyUntil < TIME.currentSecond()) {
				if (d.div.settings.isFighting()) {
					d.div.order().status.get(status);
					if (status.enemyCollisions() > 0) {
						continue;
					}
				}
				f.pushSloppy(d.tx, d.ty, 0);
				f.setValue2(d.tx, d.ty, d.div.indexArmy());
			}
			
		}
		
		
		while(f.hasMore()) {
			
			PathTile t = f.pollSmallest();
			
			if (!blob.is(t))
				continue;
			
			MDiv div = data.allDivs.get((int) t.getValue2());
			
			if (div.isDeployed)
				continue;
			
			if (t.getValue() > 32)
				break;
			
			ArmyAIUtil.map().getEnemy(res, t.x(), t.y(), context.army);
			
			if (res.size() > 0) {
				
				for (Div d : res) {
					if (d.menNrOf()*4 > attacked[d.indexArmy()]) {
						attack(div, d);
						break;
					}
					
					
				}
				res.clearSloppy();
			}
			
			for (int di = 0; di < DIR.ALL.size(); di++) {
				DIR dir = DIR.ALL.get(di);
				int dx = t.x()+dir.x();
				int dy = t.y()+dir.y();
				if (SETT.IN_BOUNDS(dx, dy)) {
					double cost = cost(context, dx, dy);
					
					if (cost > 0) {
						if (!dir.isOrtho()) {
							cost = Math.max(cost, cost(context, dx, t.y()));
							cost = Math.max(cost, cost(context, t.x(), dy));
						}
						if (f.pushSmaller(dx, dy, t.getValue() + dir.tileDistance()*cost, t) != null) {
							f.setValue2(dx,  dy, t.getValue2());
						}
					}
				}
				
			}
		}
		f.done();
		return false;
	}
	
	private final DivTDataTask task = new DivTDataTask();
	
	private void attack(MDiv mDiv, Div d) {
		if (d.settings.ammo() != null) {
			task.attackRanged(d);
		}else
			task.attackMelee(d);
		mDiv.div.order().task.set(task);
		mDiv.isDeployed = true;
		attacked[d.indexArmy()] += mDiv.men;
	}


	public static double cost(Context context, int dx, int dy) {
		
		AVAILABILITY a = SETT.PATH().availability.get(dx, dy);
		if (a.isSolid(context.army) || SETT.TERRAIN().get(dx, dy) instanceof TFortification) {
			return 3 + SETT.ARMIES().map.strength.get(dx, dy)/(C.TILE_SIZE*10);
		}else {
			
			double res = 1;//ArmyAIUtil.map().hasEnemy.is(dx, dy, c.army) ? 1 : 10;
			double s = SETT.ENV().environment.SPACE.get(dx, dy);
			if (s < 0.5)
				return res + 2 + a.movementSpeedI;
			return res + a.movementSpeedI;
		}
	}
	
	
		
}
