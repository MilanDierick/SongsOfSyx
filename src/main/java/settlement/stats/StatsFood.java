package settlement.stats;

import static settlement.main.SETT.*;

import java.io.IOException;
import java.util.Arrays;

import game.GAME;
import init.D;
import init.boostable.BBoost;
import init.boostable.BOOSTABLES;
import init.race.RACES;
import init.race.Race;
import init.resources.*;
import settlement.entity.humanoid.HCLASS;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.Humanoid.HumanoidResource;
import settlement.main.SETT;
import settlement.room.service.food.canteen.ROOM_CANTEEN;
import settlement.room.service.food.eatery.ROOM_EATERY;
import settlement.stats.Init.Updatable;
import snake2d.util.file.*;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.info.INFO;
import util.race.PERMISSION;

public class StatsFood extends StatCollection{
	
	public final STAT STARVATION;
	public final STAT FOOD_PREFFERENCE;
	public final STAT FOOD_DAYS;
	public final STAT RATIONS;
	public final STAT DRINK;
	public final LIST<STAT> all;
	
	
	private final long[] foodAllowed = new long[HCLASS.ALL.size()*RACES.all().size()];
	private final LIST<PERMISSION> food;
	
	StatsFood(Init init){
		super(init, "FOOD");
		D.gInit(this);
		STARVATION = new STAT.STATData("STARVATION", init, init.count.new DataBit());
		FOOD_PREFFERENCE = new STAT.STATData("FOOD_PREFFERENCE", init, init.count.new DataBit());
		
		FOOD_DAYS = new STAT.STATImp("FOOD_DAYS", init) {
			
			private double am;
			private int lastT = -1;
			
			@Override
			public int dataDivider() {
				return 40;
			}

			@Override
			int getDD(HCLASS s, Race race) {
				if (GAME.updateI() == lastT)
					return (int) (am*pdivider(s, race, 0));
				
				lastT = GAME.updateI();
				
				double a = 0;
				for (int ei = 0; ei < RESOURCES.EDI().all().size(); ei++) {
					Edible r = RESOURCES.EDI().all().get(ei);
					a += ROOMS().STOCKPILE.tally().amountTotal(r.resource);
				}
				
				for (int ri = 0; ri < SETT.ROOMS().EATERIES.size(); ri++) {
					ROOM_EATERY e = SETT.ROOMS().EATERIES.get(ri);
					a += e.totalFood();
				}
				
				for (int ri = 0; ri <  SETT.ROOMS().CANTEENS.size(); ri++) {
					ROOM_CANTEEN e = SETT.ROOMS().CANTEENS.get(ri);
					a += e.totalFood();
				}
				
				double needed = 0;
				
				for (int ci = 0; ci < HCLASS.ALL().size(); ci++) {
					HCLASS c = HCLASS.ALL.get(ci);
					if (c.player) {
						for (int ri = 0; ri < RACES.all().size(); ri++) {
							Race r = RACES.all().get(ri);
							needed += BOOSTABLES.RATES().HUNGER.get(c, r)*STATS.POP().POP.data(c).get(r, 0)*RATIONS.decree().get(c).get(r);
							
						}
					}
				}
				
				
				
				if (needed == 0)
					am =  a > 0 ? 1 : 0;
				else
					am = (a / needed);
				return (int) (am*pdivider(s, race, 0));
			}
		};
		FOOD_DAYS.info().setInt();
		FOOD_DAYS.info().setMatters(true, false);
		
		StatDecree d = new StatDecree(init, 1, 4, D.g("RationsT", "Target Food rations. Increases health."), 1);
		d.setInt();
		RATIONS = new STAT.STATData("FOOD_RATIONS", init, init.count.new DataNibble(3) {
			@Override
			public void set(Induvidual i, int v) {
				if (v < 0 || v > 3)
					GAME.Notify("" + v);
				super.set(i, v);
			}
		});
		RATIONS.addDecree(d);
		new StatsBoosts.StatBoosterStat(init, RATIONS, new BBoost(BOOSTABLES.PHYSICS().HEALTH, 1, false));
		
		
		d = new StatDecree(init, 0, 4, D.g("DrinkT", "Target Drink rations"), 1);
		d.setInt();
		DRINK = new STAT.STATData("DRINK_RATIONS", init, init.count.new DataNibble(4));
		DRINK.addDecree(d);
		
		FOOD_DAYS.info().setInt();
		
		init.updatable.add(updater);
		
		this.all = makeStats(init);
		
		
		Arrays.fill(foodAllowed, -1);
		init.savables.add(new SAVABLE() {
			
			@Override
			public void save(FilePutter file) {
				file.ls(foodAllowed);
			}
			
			@Override
			public void load(FileGetter file) throws IOException {
				file.ls(foodAllowed);
			}
			
			@Override
			public void clear() {
				Arrays.fill(foodAllowed, -1);
			}
		});
		
		LIST<RESOURCE> perm = RESOURCES.EDI().res().join(RESOURCES.DRINKS().all);
		ArrayList<PERMISSION> food = new ArrayList<>(perm.size());
		for (RESOURCE res : perm) {
			food.add(new PERMISSION() {
				
				@Override
				public void set(HCLASS cl, Race race, boolean value) {
					if (race == null) {
						for (int ri = 0; ri < RACES.all().size(); ri++) {
							set(cl, RACES.all().get(ri), value);
						}
						return;
					}
					if (value)
						foodAllowed[cl.index()*RACES.all().size()+race.index] |= res.bit;
					else
						foodAllowed[cl.index()*RACES.all().size()+race.index] &= ~res.bit;
				}
				
				@Override
				public INFO info() {
					return res;
				}
				
				@Override
				public boolean get(HCLASS cl, Race race) {
					if (race == null) { 
						for (int ri = 0; ri < RACES.all().size(); ri++) {
							if (get(cl, RACES.all().get(ri)))
								return true;
						}
						return false;
					}
					return (foodAllowed[cl.index()*RACES.all().size()+race.index] & res.bit) != 0l;
				}
			});
		}
		this.food = food;
	}
	
	public PERMISSION foodAllowed(Edible e) {
		return food.get(e.index());
	}
	
	public PERMISSION drinkAllowed(Edible e) {
		return food.get(e.index());
	}
	
	public PERMISSION allowed(int index) {
		return food.get(index);
	}
	
	public long fetchMask(Humanoid h) {
		return foodAllowed[h.indu().clas().index()*RACES.all().size() + h.race().index];
	}
	
	private final Updatable updater = new Updatable() {
		
		@Override
		public void update16(Humanoid h, int updateR, boolean day, int ui) {
			Induvidual i = h.indu();
			
			
			double hh = STATS.NEEDS().HUNGER.stat.indu().get(i);
			
			if (hh == STATS.NEEDS().HUNGER.stat.indu().max(i)) {
				HumanoidResource.dead = CAUSE_LEAVE.STARVED;
			}else if (hh > 0x20) {
				if (STARVATION.indu().get(i) == 0) {
					STARVATION.indu().set(i, 1);
					FOOD_PREFFERENCE.indu().set(i, 0);
					RATIONS.indu().set(i, 0);
				}
			}else {
				STARVATION.indu().set(i, 0);
			}
			
			if (STATS.NEEDS().THIRST.stat.indu().isMax(i)) {
				DRINK.indu().set(i, 0);
			}
		}

	};
	
	public void eat(Humanoid a, int level, double preference) {
		Induvidual i = a.indu();
		STATS.NEEDS().HUNGER.fix(i);
		FOOD_PREFFERENCE.indu().setD(i, preference);
	}
	
	public void eat(Humanoid a, Edible pref, short mealData) {
		Induvidual i = a.indu();
		if (Meal.amount(mealData) == 0) {
			FOOD_PREFFERENCE.indu().set(i, 0);
			RATIONS.indu().set(i, Meal.amount(mealData));
		}else {
			FOOD_PREFFERENCE.indu().set(i, Meal.get(mealData) == pref ? 1 : 0);
			RATIONS.indu().set(i, Meal.amount(mealData)-1);
		}
		STATS.NEEDS().HUNGER.fix(i);
		
	}

	@Override
	public LIST<STAT> all() {
		return all;
	}

	
}
