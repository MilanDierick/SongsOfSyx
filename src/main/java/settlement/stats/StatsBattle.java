package settlement.stats;

import java.io.IOException;
import java.util.Arrays;

import init.D;
import init.RES;
import init.boostable.BBoost;
import init.boostable.BOOSTABLES;
import init.race.Race;
import init.sprite.SPRITES;
import settlement.army.ArmyManager;
import settlement.army.Div;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.military.barracks.ROOM_BARRACKS;
import settlement.stats.Init.*;
import settlement.stats.STAT.STATData;
import snake2d.util.file.*;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import util.data.INT_O.INT_OE;
import view.main.VIEW;

public class StatsBattle extends StatCollection {

	public final LIST<StatTraining> TRAINING_ALL;
	public final StatTraining TRAINING_MELEE;
	public final StatTraining TRAINING_ARCHERY;
	
	
	public final STAT COMBAT_EXPERIENCE;

	public final STAT ENEMY_KILLS;
	public final StatObject<Div> DIV;
	public final StatProspect RECRUIT;
	public final STAT ROUTING;
	private final INT_OE<Induvidual> position;
	private final LIST<STAT> all;

	StatsBattle(Init init) {
		super(init, "BATTLE");
		D.t(this);
		position =  init.count.new DataShort();
		RECRUIT = new StatProspect(init);
		DIV = new StatDivision(init);

		COMBAT_EXPERIENCE = new STATData("COMBAT_EXPERIENCE", init, init.count.new DataNibble());
		TRAINING_MELEE = new StatTraining("COMBAT_TRAINING", init, 0, SPRITES.icons().s.sword);
		TRAINING_ARCHERY = new StatTraining("ARCHERY_TRAINING", init, 1, SPRITES.icons().s.bow);
		new StatsBoosts.StatBoosterStat(init, TRAINING_MELEE, new BBoost(BOOSTABLES.BATTLE().OFFENCE, 1.5, false));
		new StatsBoosts.StatBoosterStat(init, TRAINING_MELEE, new BBoost(BOOSTABLES.BATTLE().DEFENCE, 1.5, false));
		new StatsBoosts.StatBoosterStat(init, TRAINING_MELEE, new BBoost(BOOSTABLES.BATTLE().BLUNT_DAMAGE, 1.5, true));
		new StatsBoosts.StatBoosterStat(init, TRAINING_MELEE, new BBoost(BOOSTABLES.PHYSICS().STAMINA, 2.0, true));
		new StatsBoosts.StatBoosterStat(init, TRAINING_MELEE, new BBoost(BOOSTABLES.BATTLE().MORALE, 4.0, false));
		
		new StatsBoosts.StatBoosterStat(init, COMBAT_EXPERIENCE, new BBoost(BOOSTABLES.BATTLE().OFFENCE, 1.0, false));
		new StatsBoosts.StatBoosterStat(init, COMBAT_EXPERIENCE, new BBoost(BOOSTABLES.BATTLE().DEFENCE, 1.0, false));
		new StatsBoosts.StatBoosterStat(init, COMBAT_EXPERIENCE, new BBoost(BOOSTABLES.BATTLE().MORALE, 2.0, false));
		
		new StatsBoosts.StatBoosterStat(init, TRAINING_ARCHERY, new BBoost(BOOSTABLES.BATTLE().RANGED_SKILL, 1.0, false));

		ENEMY_KILLS = new STATData("ENEMY_KILLS", init,  init.count.new DataByte());
		new StatsBoosts.StatBoosterStat(init, ENEMY_KILLS, new BBoost(BOOSTABLES.BEHAVIOUR().SANITY, 0.5, true), 10);
		new StatsBoosts.StatBoosterStat(init, ENEMY_KILLS, new BBoost(BOOSTABLES.BEHAVIOUR().LAWFULNESS, 0.5, true), 10);
		
		ENEMY_KILLS.info().setInt();
		ROUTING = new STATData("ROUTING", init, init.count.new DataBit());

		all = makeStats(init);
		TRAINING_ALL = new ArrayList<>(TRAINING_MELEE, TRAINING_ARCHERY);
		
		init.updatable.add(new Updatable() {
			
			/**
			 * one level every 4 years
			 */
			private final double rate =  ROOM_BARRACKS.DEGRADE_RATE16*16;
			
			@Override
			public void update16(Humanoid h, int updateR, boolean day, int updateI) {
				if (!VIEW.b().isActive() && day) {
					for (StatTraining s : TRAINING_ALL)
						s.inc(h.indu(), -rate);
				}
			}
		});
	}

	private final class StatDivision extends StatObject<Div> implements Disposable {

		final STAT stat;
		private final INT_OE<Induvidual> idiv;

		public StatDivision(Init init) {
			super(D.g("Division"), D.g("DivisionD", "The Army Division this subject belongs to."));
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

			for (Addable r : STATS.addables()) {
				r.removeH(i);
			}

			Div old = get(h);
			if (old != null) {
				old.reporter.returnPosition((short) position.get(i));
			}
		}

		private void add(Humanoid a) {
			Induvidual i = a.indu();
			for (Addable r : STATS.addables()) {
				r.addH(i);
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

	public final class StatProspect extends StatObject<Div> implements Disposable{

		private final STATData stat;
		private final INT_OE<Induvidual>  idiv;
		private final short[] divs = new short[RES.config().BATTLE.DIVISIONS_PER_ARMY*2];

		public StatProspect(Init init) {
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
		public final int tIndex;
		public final SPRITE icon;
		
		StatTraining(String key, Init init, int tIndex, SPRITE icon) {
			super(key, init, init.count.new DataNibble());
			this.icon = icon;
			this.tIndex = tIndex;
			count = init.count.new DataNibble();
		}
		
		public boolean shouldTrain(Induvidual a, double target, boolean training) {
	
			double t = target*0xF;
			if (t > indu().get(a))
				return true;
			else if (t < indu().get(a))
				return false;
			if (training && !count.isMax(a))
				return true;
			return false;
			
		}
		
		public void inc(Induvidual a, double am) {
			
			int sign = am < 0 ? -1 : 1;
			am = Math.abs(am);
			am *= 0x0FF;
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

	@Override
	public LIST<STAT> all() {
		return all;
	}

}
