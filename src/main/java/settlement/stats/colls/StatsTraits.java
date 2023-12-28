package settlement.stats.colls;

import java.io.IOException;
import java.util.Arrays;

import game.boosting.BOOSTABLE_O;
import game.boosting.BoostSpecs;
import game.faction.npc.NPCBonus;
import init.D;
import init.paths.PATH;
import init.race.*;
import init.sprite.UI.UI;
import settlement.army.ArmyManager;
import settlement.army.Div;
import settlement.entity.humanoid.HCLASS;
import settlement.stats.*;
import settlement.stats.StatsInit.Addable;
import settlement.stats.StatsInit.StatInitable;
import settlement.stats.colls.StatsTraits.StatTrait;
import settlement.stats.util.StatBooster;
import snake2d.Errors;
import snake2d.util.file.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.*;
import snake2d.util.sprite.text.Str;
import util.data.BOOLEANO.BOOLEAN_OBJECTE;
import util.data.INT_O.INT_OE;
import util.info.INFO;
import util.keymap.RCollection;

public final class StatsTraits extends RCollection<StatTrait>{

	private final ArrayList<StatTrait> all;
	public final INFO info;
	
	private static CharSequence ¤¤boost = "¤Trait: {0}";
	
	public StatsTraits(StatsInit init){
		super("TRAITS");

		PATH gData = init.pd.getFolder("trait");
		PATH gText = init.pt.getFolder("trait");
		D.t(this);
		info = new INFO(
				D.g("Trait"), 
				D.g("Traits"), 
				D.g("TraitDesc", "Traits can manifest themselves in individual subjects and in turn change properties of said person"), null);
		
		String[] files = gData.getFiles();
		if (files.length > 64)
			new Errors.DataError("Too many traits declared. Maximum is 64", gData.get());
		
		all = new ArrayList<StatTrait>(files.length);
		
		for (String k : files) {
			map.put(k, new StatTrait(init, all, k, new Json(gData.get(k)), new Json(gText.get(k))));
		}
		
		for (StatTrait t : all) {
			Json j = new Json(gData.get(t.key));
			t.disables = getManyByKey("DISABLES_OTHERS", j);
		}
		
		init.initable.add(new StatInitable() {

			@Override
			public void init(Induvidual h) {
				
				if (all.size() == 0)
					return;
				
				int i = RND.rInt(all.size());
				for (int ii = 0; ii < all.size(); ii++) {
					StatTrait t = all.get((i+ii)%all.size());
					double c = t.occurence(h.race());
					
					if (RND.rFloat() < c) {
						t.set(h, true);
						for (StatTrait d : t.disables) {
							d.set(h, false);
						}
					}
				}
			}
			
		});

	}

	@Override
	public StatTrait getAt(int index) {
		return all.get(index);
	}

	@Override
	public LIST<StatTrait> all() {
		return all;
	}
	
	public static final class StatTrait extends INFO implements INDEXED, BOOLEAN_OBJECTE<Induvidual>{

		private final int index;
		public final String key;
		public final BoostSpecs boosters;
		public final double defaultOccurence;
		private final Data data;
		private LIST<StatTrait> disables;
		
		StatTrait(StatsInit init, LISTE<StatTrait> all, String key, Json data, Json text){
			super(text);
			this.index = all.add(this);
			this.key = key;
			defaultOccurence = data.d("OCCURENCE", 0, 1);
			this.data = new Data(init);
			boosters = new BoostSpecs(new Str(¤¤boost).insert(0, name), UI.icons().s.alert, true);
			boosters.push(data, new StatBooster() {

				@Override
				public double vGet(Induvidual indu) {
					return (is(indu) ? 1 : 0);
				}

				@Override
				public double vGet(Div div) {
					double p = div.menNrOf();
					double v = StatTrait.this.data.ddata[div.index()];
					if (p == 0)
						return 0;
					return CLAMP.d(v/p, 0, 1);
				}

				@Override
				public double vGet(NPCBonus bonus) {
					return vGet(bonus.faction.court().king().roy().induvidual);
				}
				
				@Override
				public boolean has(Class<? extends BOOSTABLE_O> b) {
					return StatBooster.super.has(b) && b != NPCBonus.class;
				}

				@Override
				public double vGet(POP_CL reg, int daysBack) {
					return getD(reg.cl, reg.race);
				}
				
			});
			
		}
		
		@Override
		public INFO info() {
			return this;
		}
		
		@Override
		public int index() {
			return index;
		}
		
		public int get(HCLASS c, Race r) {
			int ci = c == null ? HCLASS.ALL.size() : c.index();
			int ri = r == null ? RACES.all().size() : r.index;
			return data.gdata[ci][ri];
		}
		
		public double getD(HCLASS c, Race r) {
			double p = STATS.POP().POP.data(c).get(r);
			double v = get(c, r);
			if (p == 0)
				return CLAMP.d(v, 0, 1);
			return CLAMP.d(v/p, 0, 1);
		}
		
		
		public double occurence(Race race) {
			return race.stats().traitOccurence(this);
		}

		@Override
		public boolean is(Induvidual t) {
			return data.indu.get(t) == 1;
		}

		@Override
		public BOOLEAN_OBJECTE<Induvidual> set(Induvidual t, boolean b) {
			if (b != is(t)) {
				data.removePrivate(t);
				data.indu.set(t, b ? 1 :0);
				data.addPrivate(t);
			}
			
			return this;
		}
		
		public LIST<StatTrait> disables(){
			return disables;
		}
		
		private static class Data implements Addable, SAVABLE{
			
			private final INT_OE<Induvidual> indu;
			private int[][] gdata = new int[HCLASS.ALL.size()+1][RACES.all().size()+1]; 
			private int[] ddata = new int[ArmyManager.DIVISIONS];
			
			Data(StatsInit init) {
				indu = init.count.new DataBit();
				init.addable.add(this);
				init.savables.add(this);
			}

			@Override
			public void save(FilePutter file) {
				file.is(gdata);
				file.is(ddata);
			}

			@Override
			public void load(FileGetter file) throws IOException {
				file.is(gdata);
				file.is(ddata);
			}

			@Override
			public void clear() {
				for (int[] is : gdata)
					Arrays.fill(is, 0);
				Arrays.fill(ddata, 0);
			}

			@Override
			public void addPrivate(Induvidual i) {
				if (i.player()) {
					gdata[i.clas().index()][RACES.all().size()] += indu.get(i);
					gdata[HCLASS.ALL.size()][RACES.all().size()] += indu.get(i);
				}
				gdata[i.clas().index()][i.race().index] += indu.get(i);
			}

			@Override
			public void removePrivate(Induvidual i) {
				if (i.player()) {
					gdata[i.clas().index()][RACES.all().size()] -= indu.get(i);
					gdata[HCLASS.ALL.size()][RACES.all().size()] -= indu.get(i);
				}
				gdata[i.clas().index()][i.race().index] -= indu.get(i);
				
			}
			
			
		}

		public final INT_OE<Induvidual> stat = new INT_OE<Induvidual>() {
			@Override
			public int get(Induvidual t) {
				return data.indu.get(t);
			}

			@Override
			public int min(Induvidual t) {
				return 0;
			}

			@Override
			public int max(Induvidual t) {
				return 1;
			}

			@Override
			public void set(Induvidual t, int i) {
				StatTrait.this.set(t, i == 1);
			}
		};
		

		
		
		
	}
	
}
