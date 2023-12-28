package settlement.stats.equip;

import java.io.IOException;

import init.paths.PATH;
import init.race.RACES;
import init.race.Race;
import settlement.army.Div;
import settlement.entity.humanoid.HCLASS;
import settlement.stats.Induvidual;
import settlement.stats.StatsInit;
import snake2d.util.file.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.LISTE;

public class EquipCivic extends Equip {
	
	private final int[][] tars = new int[HCLASS.ALL().size()][RACES.all().size()];
	
	EquipCivic(String key, PATH path, LISTE<Equip> all, LISTE<EquipCivic> type, StatsInit init) {
		super("CIVIC", key, path, all, init);
		type.add(this);
		SAVABLE s = new SAVABLE() {

			@Override
			public void save(FilePutter file) {
				file.isE(tars);
			}

			@Override
			public void load(FileGetter file) throws IOException {
				file.isE(tars);
			}

			@Override
			public void clear() {
				for (int[] is : tars) {
					for (int i = 0; i < is.length; i++)
						is[i] = targetDefault;
				}
			}
		};
		
		s.clear();
		
		init.savables.add(s);
		stat.info().setOpinion(StatsEquip.¤¤more, null);
		
	}

	@Override
	public int target(Induvidual h) {
		return CLAMP.i(tars[h.hType().CLASS.index()][h.race().index], 0, max());
	}
	
	public int target(HCLASS c, Race type) {
		if (type == null) {
			int m = 0;
			for (int ri = 0; ri < RACES.all().size(); ri++) {
				Race r = RACES.all().get(ri);
				m = Math.max(m, target(c, r));
			}
			return m;
		}
		return CLAMP.i(tars[c.index()][type.index], 0, max());
	}
	
	public void targetSet(int target, HCLASS c, Race type) {
		if (type == null) {
			for (int ri = 0; ri < RACES.all().size(); ri++) {
				Race r = RACES.all().get(ri);
				targetSet(target, c, r);
			}
			return;
		}
		target = CLAMP.i(target, 0, equipMax);
		tars[c.index()][type.index] = target;
	}
	
	
	public int max() {
		return equipMax;
	}
	
	@Override
	public double value(HCLASS c, Race type) {
		return stat.data(c).getD(type);
	}

	@Override
	protected double value(Induvidual v) {
		return stat.indu().getD(v);
	}

	@Override
	protected double value(Div v) {
		return stat.div().getD(v);
	}

	@Override
	public int max(Induvidual i) {
		return equipMax;
	}



	
}