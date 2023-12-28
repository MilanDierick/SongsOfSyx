package settlement.stats.health;

import game.boosting.*;
import game.faction.Faction;
import game.faction.npc.NPCBonus;
import game.faction.npc.ruler.Royalty;
import game.time.TIME;
import init.D;
import init.race.*;
import init.sprite.UI.UI;
import settlement.army.Div;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.SPRITE;
import util.dic.DicArmy;
import world.regions.Region;

final class Bonuses {

	Bonuses(){
		
		D.t(this);
		
		Boostable bo = BOOSTABLES.CIVICS().HYGINE;
		
		new Bos(D.g("EntriesIntoCity", "New arrivals to city"), UI.icons().s.human, 0.5) {
			
			@Override
			public double vGet(POP_CL reg, int daysBAck) {
				return CLAMP.d(STATS.POP().COUNT.newEntries(), 0, 1);
			}
			
			@Override
			public double vGet(Div div) {
				return STATS.POP().COUNT.newEntries();
			}
			
			@Override
			public double vGet(Induvidual indu) {
				return STATS.POP().COUNT.newEntries();
			}
		}.add(bo);

		new Bos(STATS.ENV().UNBURRIED.info().name, UI.icons().s.death, 0.25) {
			
			@Override
			public double vGet(POP_CL reg, int daysBAck) {
				return CLAMP.d(STATS.ENV().UNBURRIED.data(reg.cl).getD(reg.race, daysBAck), 0, 1);
			}
			
			@Override
			public double vGet(Div div) {
				return vGet(RACES.clP(null, null));
						
			}
			
			@Override
			public double vGet(Induvidual indu) {
				return vGet(RACES.clP(null, null));
			}
		}.add(bo);
		
		new Bos(DicArmy.¤¤Besiege, UI.icons().s.death, 0.1) {
			
			final double bi = 1.0/8*TIME.secondsPerDay;
			
			@Override
			public double vGet(POP_CL reg, int daysBAck) {
				return CLAMP.d(SETT.ENTRY().besigeTime()-TIME.secondsPerDay*bi, 0, 1);
			}
			
			@Override
			public double vGet(Div div) {
				return vGet(RACES.clP(null, null));
						
			}
			
			@Override
			public double vGet(Induvidual indu) {
				return vGet(RACES.clP(null, null));
			}
		}.add(bo);
		
	}
	
	private abstract static class Bos extends Booster {

		private final double v;
		
		public Bos(CharSequence name, SPRITE icon, double v) {
			super(new BSourceInfo(name, icon), true);
			this.v = v;
		}

		@Override
		public double vGet(Region reg) {
			return 0;
		}

		@Override
		public double vGet(Faction f) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public double vGet(Royalty roy) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public double vGet(Race race) {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public double vGet(POP_CL reg) {
			return vGet(reg, 0);
		}
		
		@Override
		public double vGet(NPCBonus f) {

			return 0;
		}

		@Override
		public boolean has(Class<? extends BOOSTABLE_O> b) {
			return b == POP_CL.class || b == Induvidual.class || b == Div.class;
		}

		@Override
		public double from() {
			return 1.0;
		}

		@Override
		public double to() {
			return v;
		}



		
	}
	
}
