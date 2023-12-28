package init.religion;

import java.io.IOException;

import game.boosting.*;
import game.boosting.BValue.BValueSimple;
import game.faction.Faction;
import game.faction.npc.NPCBonus;
import init.paths.PATHS;
import init.race.POP_CL;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import settlement.army.Div;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.colls.StatsReligion.StatReligion;
import settlement.stats.util.StatBooster;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.Json;
import snake2d.util.sets.INDEXED;
import util.info.INFO;
import world.regions.Region;
import world.regions.data.RD;

public final class Religion implements INDEXED{
	
	private final int index;
	public final String key;
	public final COLOR color;
	public final INFO info;
	public final CharSequence diety;
	private double[] liking;
	public final Icon icon;
	public final double inclination;
	public final Boostable boostable;
	public final BoostSpecs bworld;
	public final BoostSpecs bsett;
	
	Religion(String key, int index) throws IOException{
		this.key = key;
		this.index = index;
		Json d = json();
		Json t = new Json(PATHS.TEXT().getFolder("religion").get(key));
		info = new INFO(t);
		
		diety = t.text("DEITY");
		
		color = new ColorImp(d);
		icon = SPRITES.icons().get(d);
		inclination = d.d("DEFAULT_SPREAD");
		
		boostable = BOOSTING.push(key, 1, info.name, info.desc, icon, BoostableCat.RELIGION);
		bworld = new BoostSpecs(info.name, icon, false);
		bworld.push("BOOST_WORLD", d, new BoWorld(index));
		bsett = new BoostSpecs(info.name, icon, false);
		bsett.push("BOOST_CITY", d, new BoSett(index));
	}
	
	private Json json() {
		return new Json(PATHS.INIT().getFolder("religion").get(key));
	}
	
	public double opposition(Religion other) {
		return liking[other.index()];
	}

	void init() {
		liking = new double[Religions.ALL().size()];
		Religions.MAP().fill("OPPOSITION", liking, json(), 0, 100);
	}

	@Override
	public int index() {
		return index;
	}
	
	@Override
	public String toString() {
		return "["+index+"]" + key;
	}
	
	private static class BoWorld implements BValueSimple {

		private final int index;
		
		BoWorld(int index){
			this.index = index;
		}
		
		@Override
		public double vGet(Region reg) {
			return RD.RELIGION().all().get(index).current.getD(reg);
		}
		
		@Override
		public double vGet(Faction f) {
			return 0;
		}
		
		@Override
		public boolean has(Class<? extends BOOSTABLE_O> b) {
			return b == Region.class;
		}
		
	}
	
	private static class BoSett implements StatBooster {

		private final int index;
		
		BoSett(int index){
			this.index = index;
		}

		@Override
		public double vGet(POP_CL reg) {
			StatReligion rl = STATS.RELIGION().ALL.get(index);
			return rl.followers.data(reg.cl).getD(reg.race)*rl.temple_access.data(reg.cl).getD(reg.race)*rl.temple_quality.data(reg.cl).getD(reg.race);
		}

		
		@Override
		public double vGet(Div v) {
			StatReligion rl = STATS.RELIGION().ALL.get(index);
			return rl.followers.div().getD(v)*rl.temple_access.div().getD(v)*rl.temple_quality.div().getD(v);
		}
		
		@Override
		public double vGet(Induvidual v) {
			StatReligion rl = STATS.RELIGION().ALL.get(index);
			if (STATS.RELIGION().getter.get(v) == rl) {
				return STATS.RELIGION().TEMPLE_TOTAL.indu().getD(v);
			}
			return 0;
		}
		
		@Override
		public boolean has(Class<? extends BOOSTABLE_O> b) {
			return b == Induvidual.class || b == Div.class ||b == POP_CL.class;
		}

		@Override
		public double vGet(Faction f) {
			return 0;
		}

		@Override
		public double vGet(NPCBonus bonus) {
			StatReligion rl = STATS.RELIGION().ALL.get(index);
			if (STATS.RELIGION().getter.get(bonus.faction.court().king().roy().induvidual) == rl) {
				return 1.0;
			}
			return 0;
		}
		
		
	}
	
	
	
}