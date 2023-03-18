package settlement.army.ai.general;

import init.C;
import init.config.Config;
import settlement.army.ai.general.Groups.GroupLine;
import settlement.army.ai.util.ArmyAIUtil;
import settlement.main.SETT;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.sets.ArrayList;

final class GroupCharger {

	private final Context c;
	private final ArrayList<GDiv> chargers = new ArrayList<>(Config.BATTLE.DIVISIONS_PER_ARMY);
	private final VectorImp vec = new VectorImp();
	
	public GroupCharger(Context c) {
		this.c = c;
	}
	
	public void charge(GroupLine l) {
		
		int canCharge = 0;
		int total = l.width;
		chargers.clear();
		
		for (GDiv d : c.divs) {
			if (l.divsAll.get(d.index())) {
				if (test(l, d)) {
					chargers.add(d);
					canCharge += c.form.width()/C.TILE_SIZE;
				}
			}
		}
		
		
		if (canCharge == 0 || canCharge < total/4)
			return;
		
		for (GDiv d : chargers) {
			d.timeout(10);
			c.task.charge();
			d.div().order().task.set(c.task);
		}
		
		
	}
	
	private boolean test(GroupLine l, GDiv div) {
		div.init();
		if (!div.active)
			return false;
		div.div().order().next.get(c.form);
		if (c.form.deployed() == 0)
			return false;
		if (c.form.dx()*l.v.nX() + c.form.dy()*l.v.nY() < 0.75)
			return false;
		vec.set(c.form.dx(), c.form.dy());
		vec.rotate90().rotate90().rotate90();
		
		int ww = c.form.width()/C.TILE_SIZE;
		for (int w = 0; w < ww; w += 3) {
			int sx = (int) (c.form.start().x()/C.TILE_SIZE+c.form.dx()*w);
			int sy = (int) (c.form.start().y()/C.TILE_SIZE+c.form.dy()*w);
			for (int d = 2; d <= 24; d++) {
				int x = (int) (sx + vec.nX()*d);
				int y = (int) (sy + vec.nY()*d);
				if (!SETT.IN_BOUNDS(x, y))
					break;
				if (SETT.PATH().availability.get(x, y).isSolid(c.army))
					break;
				if (ArmyAIUtil.map().hasAlly.is(x, y, c.army))
					break;
				if (ArmyAIUtil.map().hasEnemy.is(x, y, c.army))
					return true;
			}
			
		}
		return false;
		
	}
	
}
