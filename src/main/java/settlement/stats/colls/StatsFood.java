package settlement.stats.colls;

import static settlement.main.SETT.*;

import java.io.IOException;

import game.GAME;
import init.D;
import init.need.NEEDS;
import init.race.RACES;
import init.race.Race;
import init.resources.*;
import init.resources.RBIT.RBITImp;
import settlement.entity.humanoid.HCLASS;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.service.food.canteen.ROOM_CANTEEN;
import settlement.room.service.food.eatery.ROOM_EATERY;
import settlement.stats.*;
import settlement.stats.stat.*;
import snake2d.util.file.*;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.info.INFO;
import util.race.PERMISSION;

public class StatsFood extends StatCollection{
	
	public final STAT FOOD_PREFFERENCE;
	public final STAT FOOD_DAYS;
	public final STAT RATIONS;
	public final STAT DRINK;
	public final STAT STARVATION;
	
	private final RBITImp[] foodAllowed = new RBITImp[HCLASS.ALL.size()*RACES.all().size()];
	private final LIST<PERMISSION> food;
	
	public StatsFood(StatsInit init){
		super(init, "FOOD");
		D.gInit(this);
		
		STARVATION = new STATData("STARVATION", init, init.count.new DataBit());
		FOOD_PREFFERENCE = new STATData("FOOD_PREFFERENCE", init, init.count.new DataBit());
		for (int i = 0; i < foodAllowed.length; i++)
			foodAllowed[i] = new RBITImp().setAll();
		FOOD_DAYS = new STATImp("FOOD_DAYS", init) {
			
			private double am;
			private int lastT = -1;
			
			@Override
			public int dataDivider() {
				return 40;
			}

			@Override
			protected int getDD(HCLASS s, Race race) {
				if (GAME.updateI() == lastT)
					return (int) (am*pdivider(s, race, 0));
				
				lastT = GAME.updateI();
				
				double a = 0;
				for (int ei = 0; ei < RESOURCES.EDI().all().size(); ei++) {
					ResG r = RESOURCES.EDI().all().get(ei);
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
							needed += NEEDS.TYPES().HUNGER.rate.get(c.get(r))*STATS.POP().POP.data(c).get(r, 0)*RATIONS.decree().get(c).get(r);
							
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
		RATIONS = new STATData("FOOD_RATIONS", init, init.count.new DataNibble(3) {
			@Override
			public void set(Induvidual i, int v) {
				if (v < 0 || v > 3)
					GAME.Notify("" + v);
				super.set(i, v);
			}
		});
		RATIONS.addDecree(d);
		
		
		d = new StatDecree(init, 0, 4, D.g("DrinkT", "Target Drink rations"), 1);
		d.setInt();
		DRINK = new STATData("DRINK_RATIONS", init, init.count.new DataNibble(4));
		DRINK.addDecree(d);
		
		FOOD_DAYS.info().setInt();
		
		for (RBITImp b : foodAllowed)
			b.setAll();
		init.savables.add(new SAVABLE() {
			
			@Override
			public void save(FilePutter file) {
				for (RBITImp b : foodAllowed)
					b.save(file);
			}
			
			@Override
			public void load(FileGetter file) throws IOException {
				for (RBITImp b : foodAllowed)
					b.load(file);
			}
			
			@Override
			public void clear() {
				for (RBITImp b : foodAllowed)
					b.setAll();
			}
		});
		
		LIST<RESOURCE> perm = RESOURCES.EDI().res().join(RESOURCES.DRINKS().res());
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
						foodAllowed[cl.index()*RACES.all().size()+race.index].or(res);
					else
						foodAllowed[cl.index()*RACES.all().size()+race.index].clear(res);
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
					return (foodAllowed[cl.index()*RACES.all().size()+race.index].has(res));
				}
			});
		}
		this.food = food;
	}
	
	public PERMISSION foodAllowed(ResG e) {
		return food.get(e.index());
	}
	
	public PERMISSION drinkAllowed(ResG e) {
		return food.get(e.index());
	}
	
	public PERMISSION allowed(int index) {
		return food.get(index);
	}
	
	public RBIT fetchMask(Humanoid h) {
		return foodAllowed[h.indu().clas().index()*RACES.all().size() + h.race().index];
	}
	
//	private final StatUpdatableI updater = new StatUpdatableI() {
//		
//		@Override
//		public void update16(Humanoid h, int updateR, boolean day, int ui) {
//			Induvidual i = h.indu();
//			
//			
//			double hh = STATS.NEEDS().HUNGER.stat().indu().get(i);
//			
//			if (hh == STATS.NEEDS().HUNGER.stat().indu().max(i)) {
//				HumanoidResource.dead = CAUSE_LEAVE.STARVED;
//			}else if (hh > 0x20) {
//				if (STARVATION.indu().get(i) == 0) {
//					STARVATION.indu().set(i, 1);
//					FOOD_PREFFERENCE.indu().set(i, 0);
//					RATIONS.indu().set(i, 0);
//				}
//			}else {
//				STARVATION.indu().set(i, 0);
//			}
//			
//			if (STATS.NEEDS().THIRST.stat().indu().isMax(i)) {
//				DRINK.indu().set(i, 0);
//			}
//		}
//
//	};
	
	public void eat(Humanoid a, int level, double preference) {
		Induvidual i = a.indu();
		
		RATIONS.indu().set(i, level);
		FOOD_PREFFERENCE.indu().setD(i, preference);
	}
	
	public void eat(Humanoid a, ResG pref, short mealData) {
		Induvidual i = a.indu();
		if (Meal.amount(mealData) == 0) {
			FOOD_PREFFERENCE.indu().set(i, 0);
			RATIONS.indu().set(i, Meal.amount(mealData));
		}else {
			FOOD_PREFFERENCE.indu().set(i, Meal.get(mealData) == pref ? 1 : 0);
			RATIONS.indu().set(i, Meal.amount(mealData)-1);
		}
		//NEEDS.TYPES().HUNGER.stat().fix(i);
		
	}
	
}
