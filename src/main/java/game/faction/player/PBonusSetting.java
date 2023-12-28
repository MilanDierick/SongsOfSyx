package game.faction.player;

import java.io.IOException;

import game.boosting.*;
import game.faction.FACTIONS;
import game.faction.Faction;
import init.D;
import init.sprite.UI.UI;
import snake2d.LOG;
import snake2d.util.file.*;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;

final class PBonusSetting implements ACTION, SAVABLE{

	private static CharSequence ¤¤name = "¤Setting";
	private final KeyMap<Double> boosts;
	static {
		D.ts(PBonusSetting.class);
	}

	protected PBonusSetting(KeyMap<Double> boosts) {
		this.boosts = boosts;
		BOOSTING.connecter(this);

		
	}

	@Override
	public void exe() {
		for (String k : boosts.keys()) {
			LIST<Boostable> bb = BOOSTING.get(k);
			
			if (bb == null) {
				LOG.err(k);
				continue;
			}
			
			for (Boostable b : bb) {
				
				Booster bo = new BoosterImp(new BSourceInfo(¤¤name, UI.icons().s.alert), boosts.get(k), false) {
					
					@Override
					public double vGet(Faction f) {
						if (f == FACTIONS.player())
							return 1.0;
						return 0;
					}
				};
				
				bo.add(b);
				
			}
			
			
		}
		
	}

	@Override
	public void save(FilePutter file) {
		file.i(boosts.size());
		for (String s : boosts.keysSorted()) {
			file.chars(s);
			file.d(boosts.get(s));
		}
	}

	@Override
	public void load(FileGetter file) throws IOException {
		boosts.clear();
		int am = file.i();
		for (int i = 0; i < am; i++) {
			String k = file.chars();
			double v = file.d();
			boosts.put(k, v);
		}
		BOOSTING.connecter(this);
		
	}

	@Override
	public void clear() {
		boosts.clear();
	}

}
