package settlement.stats.colls;

import java.io.IOException;
import java.util.Arrays;

import game.boosting.*;
import init.D;
import init.config.Config;
import init.race.Race;
import settlement.army.ArmyManager;
import settlement.army.Div;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.military.training.ROOM_M_TRAINER;
import settlement.stats.*;
import settlement.stats.StatsInit.StatDisposable;
import settlement.stats.stat.*;
import settlement.stats.util.StatBooster;
import settlement.stats.util.StatBooster.StatBoosterStat;
import snake2d.util.file.*;
import snake2d.util.misc.ACTION;
import snake2d.util.rnd.RND;
import snake2d.util.sets.*;
import util.data.INT_O.INT_OE;
import util.info.INFO;
import util.keymap.RCollection;

public class StatsBattle extends StatCollection {

	public final LIST<StatTraining> TRAINING_ALL;
	public final RCollection<StatTraining> TRAINING_MAP;
	public final INT_OE<Induvidual> basicTraining;
	
	public final STAT COMBAT_EXPERIENCE;

	public final STAT ENEMY_KILLS;
	public final StatObject<Div> DIV;
	public final StatProspect RECRUIT;
	public final STAT ROUTING;
	private final INT_OE<Induvidual> position;

	public StatsBattle(StatsInit init) {
		super(init, "BATTLE");
		D.t(this);
		position =  init.count.new DataShort();
		basicTraining = init.count.new DataNibble(new INFO(D.g("Basic", "Basic Training"), D.g("BasicD", "Basic training is acquired in any training rooms and is required to be able to join a division.")), 15);
		RECRUIT = new StatProspect(init);
		DIV = new StatDivision(init);

		
		
		COMBAT_EXPERIENCE = new STATData("COMBAT_EXPERIENCE", init, init.count.new DataNibble());
		
		LinkedList<StatTraining> li = new LinkedList<>();
		KeyMap<StatTraining> map = new KeyMap<>();
		for (ROOM_M_TRAINER<?> tt : ROOM_M_TRAINER.ALL()) {
			StatTraining t = new StatTraining(init, tt);
			li.add(t);
			map.put(t.room.key, t);
		}
		this.TRAINING_ALL = new ArrayList<StatsBattle.StatTraining>(li);
		this.TRAINING_MAP = new RCollection<StatsBattle.StatTraining>("TRAINING", map) {

			@Override
			public StatTraining getAt(int index) {
				return TRAINING_ALL.get(index);
			}

			@Override
			public LIST<StatTraining> all() {
				return TRAINING_ALL;
			}
		};
		
		ENEMY_KILLS = new STATData("ENEMY_KILLS", init,  init.count.new DataByte());
		
		ENEMY_KILLS.info().setInt();
		ROUTING = new STATData("ROUTING", init, init.count.new DataBit());
		
//		init.updatable.add(new StatUpdatableI() {
//			
//			/**
//			 * one level every 4 years
//			 */
//			private final double rate =  ROOM_BARRACKS.DEGRADE_RATE16*16;
//			
//			@Override
//			public void update16(Humanoid h, int updateR, boolean day, int updateI) {
//				if (!VIEW.b().isActive() && day) {
//					for (StatTraining s : TRAINING_ALL)
//						s.inc(h.indu(), -rate);
//				}
//			}
//		});
	}

	private final class StatDivision extends StatObject<Div> implements StatDisposable {

		final STAT stat;
		private final INT_OE<Induvidual> idiv;
		private final StatsInit init;
		public StatDivision(StatsInit init) {
			super(D.g("Division"), D.g("DivisionD", "The Army Division this subject belongs to."));
			this.init = init;
			idiv = init.count.new DataByte();
			INT_OE<Induvidual> b = new INT_OE<Induvidual>() {

				@Override
				public int get(Induvidual t) {
					return idiv.get(t) != 0 ? 1 :0;
				}

				@Override
				public int min(Induvidual t) {
					// TODO Auto-generated method stub
					return 0;
				}

				@Override
				public int max(Induvidual t) {
					// TODO Auto-generated method stub
					return 1;
				}

				@Override
				public void set(Induvidual t, int i) {
					// TODO Auto-generated method stub
					
				}

			};
			stat = new STATData("SOLDIERS", init, b);
			if (ArmyManager.DIVISIONS >= Short.MAX_VALUE)
				throw new RuntimeException();
			init.disposable.add(this);
		}

		@Override
		public Div get(Induvidual i) {
			if (idiv.get(i) != 0)
				return SETT.ARMIES().division((short) (idiv.get(i) - 1));
			return null;
		}

		@Override
		public void set(Humanoid h, Div d) {
			Induvidual i = h.indu();
			if (!i.added())
				throw new RuntimeException();
			if (d != null)
				RECRUIT.set(h, null);

			remove(h);

			if (d != null) {
				idiv.set(i, d.index() + 1);
			} else {
				idiv.set(i, 0);
			}
			add(h);
			
		}

		private void remove(Humanoid h) {

			Induvidual i = h.indu();

			for (int ai = 0; ai < init.addable.size(); ai++) {
				init.addable.get(ai).removeH(i);
			}

			Div old = get(h);
			if (old != null) {
				old.reporter.returnPosition((short) position.get(i));
			}
		}

		private void add(Humanoid a) {
			Induvidual i = a.indu();
			
			for (int ai = 0; ai < init.addable.size(); ai++) {
				init.addable.get(ai).addH(i);
			}

			Div now = get(a);
			if (now != null) {
				position.set(i, now.reporter.signUpAndGetPosition(a.body().cX(), a.body().cY(), i.race()));
			}
		}

		@Override
		public void dispose(Humanoid h) {
			set(h, null);

		}

		@Override
		public STAT stat() {
			return stat;
		}

	}

	public final class StatProspect extends StatObject<Div> implements StatDisposable{

		private final STATData stat;
		private final INT_OE<Induvidual>  idiv;
		private final short[] divs = new short[Config.BATTLE.DIVISIONS_PER_ARMY*2];

		public StatProspect(StatsInit init) {
			super(D.g("Recruit"),
					D.g("RecruitD", "The Army Division this subject will join when training is complete."));
			idiv = init.count.new DataByte();
			INT_OE<Induvidual> b = new INT_OE<Induvidual>() {

				@Override
				public int get(Induvidual t) {
					return idiv.get(t) != 0 ? 1 :0;
				}

				@Override
				public int min(Induvidual t) {
					// TODO Auto-generated method stub
					return 0;
				}

				@Override
				public int max(Induvidual t) {
					return 1;
				}

				@Override
				public void set(Induvidual t, int i) {
					// TODO Auto-generated method stub
					
				}

			};
			stat = new STATData("RECRUITS", init, b);
			
			init.savables.add(new SAVABLE() {
				
				@Override
				public void save(FilePutter file) {
					file.ss(divs);
				}
				
				@Override
				public void load(FileGetter file) throws IOException {
					file.ss(divs);
					Arrays.fill(divs, (short)0);
					for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
						if (e instanceof Humanoid) {
							Humanoid a = (Humanoid) e;
							if (get(a) != null)
								divs[get(a).index()] ++;
						}
					}
				}
				
				@Override
				public void clear() {
					Arrays.fill(divs, (short)0);
				}
			});
			init.disposable.add(this);
			
		}

		@Override
		public void set(Humanoid h, Div d) {

			Induvidual i = h.indu();
			if (!i.added())
				throw new RuntimeException();
			stat.removeH(h.indu());
			if (get(h) != null)
				divs[get(h).index()] --;
				
			
			if (d != null) {
				idiv.set(i, d.index() + 1);
			} else {
				idiv.set(i, 0);
			}
			stat.addH(h.indu());
			if (get(h) != null)
				divs[get(h).index()] ++;
		}
		
		@Override
		public void dispose(Humanoid h) {
			set(h, null);
		}
		
		public int inDiv(Div div) {
			return divs[div.index()];
		}

		@Override
		public Div get(Induvidual i) {
			if (idiv.get(i) != 0)
				return SETT.ARMIES().division((short) (idiv.get(i) - 1));
			return null;
		}

		@Override
		public STAT stat() {
			return stat;
		}

	}

	public int position(Induvidual i) {
		return position.get(i);
	}

	public static class StatTraining extends STATData {

		private final INT_OE<Induvidual> count;
		public final ROOM_M_TRAINER<?> room;
		public final int tIndex;
		public static final int MAX = 15;
		public static final double MAXI = 1.0/MAX;
		
		StatTraining(StatsInit init, ROOM_M_TRAINER<?> room) {
			
			super(room.key, init, init.count.new DataNibble(), new StatInfo(room.tInfo.name, room.tInfo.desc));
			count = init.count.new DataNibble();
			this.room = room;
			tIndex = room.INDEX_TRAINING;
			BOOSTING.connecter(new ACTION() {
				
				@Override
				public void exe() {
					
					for (BoostSpec b : room.boosters.all()) {
						
						StatBoosterStat v = new StatBooster.StatBoosterStat(StatTraining.this, false) {
							@Override
							public boolean has(java.lang.Class<? extends BOOSTABLE_O> b) {
								return b == Induvidual.class || b == Div.class;
							};
						};
						Booster bo = new BoosterWrap(v, b.booster.info, b.booster.to(), b.booster.isMul);
						StatTraining.this.boosters.push(bo, b.boostable);
					}
					
				}
			});
			
		}
		

		
		public boolean shouldTrain(Induvidual a, double target, boolean training) {
			
			double t = target;
			double i = indu().getD(a);
			if (t > i)
				return true;
			else if (t < i)
				return false;
			if (training && !count.isMax(a))
				return true;
			return false;
			
		}
		
		public void inc(Induvidual a, double am) {
			
			int sign = am < 0 ? -1 : 1;
			am = Math.abs(am);
			am *= 0x0F*indu().max(a);
			int iam = (int) am;
			if (RND.rFloat() < am-iam)
				iam++;
			iam *= sign;
			
			int c = count.get(a)+iam;
			while(c >= count.max(a)) {
				indu().inc(a, 1);
				c -= count.max(a);
			}
			while(c <= 0) {
				indu().inc(a, -1);
				c += count.max(a);
			}
			count.set(a, c);
			
		}
		
		
	}
	
	public abstract static class HDivStat {

		public HDivStat() {

		}

		protected abstract void returnPosition(short pos);

		protected abstract short signUpAndGetPosition(int x, int y, Race r);

	}

}
