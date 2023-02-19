package game.events;

import java.io.IOException;
import java.io.Serializable;

import game.GAME;
import game.events.EVENTS.EventResource;
import game.faction.FACTIONS;
import game.time.TIME;
import init.D;
import init.RES;
import init.boostable.BOOSTABLES;
import init.race.RACES;
import init.race.Race;
import init.sprite.UI.UI;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.StatsEquippables.StatEquippableBattle;
import settlement.stats.standing.STANDINGS;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sprite.text.Str;
import util.data.BOOLEAN_OBJECT;
import util.data.DOUBLE;
import util.dic.DicRes;
import util.gui.misc.GButt;
import util.gui.misc.GText;
import util.info.INFO;
import view.main.Message;
import view.main.MessageText;
import view.world.IDebugPanelWorld;
import world.World;
import world.army.WARMYD;
import world.army.WARMYD.WArmySupply;
import world.army.WDivRegional;
import world.entity.WPathing;
import world.entity.army.WArmy;
import world.map.regions.*;

public class EventRaider extends EventResource{

	private static CharSequence ¤¤protection = "¤Protection?";
	private static CharSequence ¤¤pay = "¤Pay Up";
	private static CharSequence ¤¤decline = "¤Decline Offer";
	private static CharSequence ¤¤protectionD = "¤Hello friend. We are a band of noble people, keeping the peace in the region of {0}. As you know, there are evil forces out in the world, bent on destruction and conquest. We however, are lovers of peace, and would like to offer our protective services. But soldiers have to eat, so we kindly ask you to ship us {1} {2} and lets say 10% of your goods in order for us to keep the peace. It's quite the bargain. We await your reply and looking forward to hearing from you within 4 days.";
	private static CharSequence ¤¤proInfo = "¤Our scouts report these people have a force somewhere around {0} soldiers with {1}% training and {2}% equipment. Paying the sum is a safe bet. Declining might lead to trouble.";
	private static CharSequence ¤¤Rebels = "¤Rebels!";
	private static CharSequence ¤¤HasSpawned = "¤The Rebel army has spawned in the region of {0}. Deal with them before they have time to equip themselves!";
	private static CharSequence ¤¤protectionFail = "¤Hello again, you must have forgot to pay. Very unwise. Never mind, we will now have to come and collect what is owed ourselves. I will see you soon.";
	private static CharSequence ¤¤Rebellion = "¤This region has had enough of your mistreatment and have declared independency from your tyrannical rule.";
	private static CharSequence ¤¤Chance = "¤Raid Chance"; 
	private static CharSequence ¤¤ChanceD = "¤The chances of getting raided yearly. Raids spawn from a neighbouring rebel region. If there are none, chances will be low. This chance goes up with your population, riches, and size of your kingdom. It goes down by with your garrison size and victories, although when they occur, they will be better equipped and trained to take you on. Raiders can be payed off to avoid a confrontation."; 
	private final ArrayList<Timer> awaiting = new ArrayList<>(16);
	
	private double timer = -TIME.secondsPerDay*20;
	
	static {
		D.ts(EventRaider.class);
	}
	
	EventRaider(){
		
		IDebugPanelWorld.add("Spawn raid", new ACTION() {
			
			@Override
			public void exe() {
				for (int i = 0; i < Regions.MAX; i++) {
					if (target(World.REGIONS().getByIndex(i)) != null) {
						spawn(World.REGIONS().getByIndex(i));
						MessageText m = new MessageText(¤¤Rebels, ¤¤Rebels);
						m.paragraph(Str.TMP.clear().add(¤¤HasSpawned).insert(0, World.REGIONS().getByIndex(i).name()));
						m.send();
						return;
					}
				}
			}
		});
		
		IDebugPanelWorld.add("Spawn protection", new ACTION() {
			
			@Override
			public void exe() {
				Region r = FACTIONS.player().kingdom().realm().regions().rnd();
				Region target = target(r);
				int men = getRebelAmount(target);
				double training = RND.rExpo();
				double gear = RND.rExpo();
				
				Timer timer = new Timer(r, 10000, (int)men, training, gear);
				new MProtection(timer).send();;
			}
		});

	}
	
	@Override
	protected void save(FilePutter file) {
		file.d(timer);
		awaiting.save(file);
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		timer = file.d();
		awaiting.load(file);
	}
	
	@Override
	protected void clear() {
		awaiting.clear();
		timer = -TIME.secondsPerDay*20;
	}
	
	public final DOUBLE CHANCE = new DOUBLE() {
		
		private final INFO info = new INFO(¤¤Chance, ¤¤ChanceD);
		private int upI = -200;
		private double cache = 0;
		@Override
		public double getD() {
			if (GAME.updateI() - upI > 200) {
				upI = GAME.updateI();
				
				
				double c = CLAMP.d(1.0/BOOSTABLES.CIVICS().RAIDING.get(null, null), 0, 10);
				
				
				cache = getChance(FACTIONS.player().capitolRegion())*c*TIME.years().bitConversion(TIME.days());
			}
			return cache;
		}
		
		@Override
		public INFO info() {
			return info;
		};
	};
	
	@Override
	protected void update(double ds) {
		if (awaiting.size() > 0 && awaiting.get(0).hasExpired()) {
			Timer t = awaiting.remove(0);
			
			if (spawn(World.REGIONS().getByIndex(t.regionI), t.size, t.training, t.gear) != null) {
				MessageText m = new MessageText(¤¤Rebels, ¤¤protectionFail);
				m.paragraph(Str.TMP.clear().add(¤¤HasSpawned).insert(0, World.REGIONS().getByIndex(t.regionI).name()));
				SETT.INVADOR().increaseWins();
				m.send();
			}
			timer = 0;
			return;
		}
		
		if (awaiting.size() == 0) {
			double c = CLAMP.d(1.0/BOOSTABLES.CIVICS().RAIDING.get(null, null), 0, 10);
			if (c < 1)
				c*= c*c;
			timer += ds*c*1.5;
			if (timer > TIME.secondsPerDay) {
				timer -= TIME.secondsPerDay;
				Region r = FACTIONS.player().kingdom().realm().regions().rnd();
				update(r);
			}
		}
	}

	

	
//	private double getChance(Region target) {
//		double chance = 0.125;
//		
//		if (STATS.POP().POP.data().get(null) <= 50)
//			return 0;
//		
//		if (FACTIONS.player().credits().credits() < 0)
//			return 0;
//		
//		chance *= Math.pow(CLAMP.d((STATS.POP().POP.data().get(null)-50)/600.0, 0, 1), 0.5);
//		chance *= 0.1 + 0.9*STATS.GOVERN().RICHES.data().getD(null);
//		chance *= 0.5 + 0.1*(FACTIONS.player().kingdom().realm().regions().size()-1);
//		Region origin = WPathing.findAdjacentRegion(target, rebelFinder);
//		chance /= 1 + 0.25*SETT.INVADOR().wins();
//		
//		if (origin == null) {
//			chance*= 0.05;
//			origin = target;
//		}
//		
//		if (chance == 0)
//			return 0;
//		
//		double garrison = getRebelAmount(target);
//		chance *= 1.0 - Math.pow(CLAMP.d(4*garrison/RES.config().BATTLE.MEN_PER_ARMY, 0, 1), 0.5);
//		return chance;
//	}
	
	private double getChance(Region target) {
		double chance = 0.125*0.5;
		
		if (STATS.POP().POP.data().get(null) <= 50)
			return 0;
		
		if (FACTIONS.player().credits().credits() < 0)
			return 0;
		chance *= Math.pow(CLAMP.d((STATS.POP().POP.data().get(null)-50)/1500.0, 0, 1), 0.5);
		chance *= 0.5 + 0.5*STATS.GOVERN().RICHES.data().getD(null);
		chance *= 0.8 + 0.05*(FACTIONS.player().kingdom().realm().regions().size()-1);
		chance *= 1.0 - 0.2*CLAMP.i(SETT.INVADOR().wins(), 0, 4);
		chance *= STANDINGS.CITIZEN().current();
		
		Region origin = WPathing.findAdjacentRegion(target, rebelFinder);
		if (origin == null) {
			chance*= 0.05;
			origin = target;
		}
		
		if (chance == 0)
			return 0;
		
		double armyValue = 4.0*getRebelAmount(target)/(STATS.POP().POP.data().get(null) + 1.0);
		armyValue = 1.0 - 0.8*CLAMP.d(armyValue, 0, 1);
		chance *= armyValue;
		return chance;
	}
	
	private void update(Region target) {
		
		
		
		if (target.area() == 0)
			return;
		if (target.faction() != null && target.faction() != FACTIONS.player())
			return;
		if (target.isWater())
			return;
		
		double chance = getChance(target);
		
		if (chance == 0)
			return;
		Region origin = WPathing.findAdjacentRegion(target, rebelFinder);
		if (origin == null)
			origin = target;
		for (Timer t : awaiting) {
			if (origin.index() == t.regionI)
				return;
		}
		
		if (!awaiting.hasRoom())
			return;
		
		if (chance < RND.rFloat())
			return;
		
		
		double garrison = getRebelAmount(target);


		garrison *= 1 + 0.1*RND.rFloat()*SETT.INVADOR().wins();
		
		garrison *= 1 + RND.rFloat()*0.1;
		if (garrison < 10)
			garrison = 10;

		
		double q = 0.1 + SETT.INVADOR().wins()/10.0;
		q = CLAMP.d(q, 0, 1);
		
		double training = CLAMP.d(q*RND.rFloat1(0.2), 0, 1);
		double gear = CLAMP.d(q*RND.rFloat1(0.2), 0, 1);
		
		
		int ransom = (int) (100*STATS.POP().POP.data().get(null)*RND.rFloat1(0.2));
		
		Timer timer = new Timer(origin, ransom, (int)garrison, training, gear);
		new MProtection(timer).send();;
		this.timer -= TIME.secondsPerDay *16;
	}
	
	private int getRebelAmount(Region target) {
		int men = REGIOND.MILITARY().soldiers.get(target);
		if (target == FACTIONS.player().capitolRegion()) {
			men += (STATS.POP().POP.data().get(null)-men)/30;
		}
		for (WArmy a2 : FACTIONS.player().kingdom().armies().all()) {
			if (a2.region() == target) {
				men += WARMYD.men(null).get(a2);
			}else if(a2.region() != null && a2.region().faction() == FACTIONS.player())
				men += 0.5* WARMYD.men(null).get(a2);
			else
				men += 0.25* WARMYD.men(null).get(a2);
		}

		if (men < 10)
			men = 10;
		
		return men;
	}
	
	public boolean tryRebel(Region region) {
		
		if (!World.ARMIES().rebels().canCreate())
			return false;
		
		int men = 0;
		
		for (WArmy a2 : FACTIONS.player().kingdom().armies().all()) {
			if (a2.region() == region) {
				men += WARMYD.men(null).get(a2);
			}else if(a2.region().faction() == FACTIONS.player())
				men += 0.25* WARMYD.men(null).get(a2);
			else
				men += 0.125* WARMYD.men(null).get(a2);
		}
		
		int rmen = (int) (REGIOND.POP().total().get(region)*(0.025 + RND.rFloat()*0.025));
		
		if (rmen < men)
			return false;
		
		if (men == 0)
			men += 30 + RND.rInt(20);
		
		double dmen = CLAMP.d(RES.config().BATTLE.MEN_PER_ARMY/(double)rmen, 0, 1);

		COORDINATE c = WPathing.random(region);
		
		WArmy a = World.ARMIES().createRebel(c.x()-1, c.y()-1);
		if (a == null)
			return false;
		
		if (region.realm() != null && region.faction().capitolRegion() != region)
			REGIOND.OWNER().realm.set(region, null);
		
		double raceTot = 0;
		Race biggest = RACES.all().get(0);
		int b = 0;
		for (Race r : RACES.all()) {
			raceTot += REGIOND.RACE(r).population.get(region);
			if (REGIOND.RACE(r).population.get(region) > b) {
				biggest = r;
			}
		}
		
		double menLeft = 0;
		
		for (Race r : RACES.all()) {
		
			menLeft += dmen*men*REGIOND.RACE(r).population.get(region)/raceTot;
			
			while (menLeft > 2 && a.divs().canAdd()) {
				
				int am = CLAMP.i((int)menLeft, 0, RES.config().BATTLE.MEN_PER_DIVISION);
				WDivRegional d = World.ARMIES().regional().create(r, (double)am/RES.config().BATTLE.MEN_PER_DIVISION, 0,0, a);
				d.randomize(RND.rExpo(), (int)(RND.rExpo()*15));
				d.menSet(d.menTarget());
				menLeft -= am;
				
			}
		}
		
		if (WARMYD.men(null).get(a) < men) {
			WDivRegional d = World.ARMIES().regional().create(biggest, (double)CLAMP.d((double)(men-WARMYD.men(null).get(a))/RES.config().BATTLE.MEN_PER_DIVISION, 0.1, 1), 0,0, a);
			d.randomize(RND.rExpo(), (int)(RND.rExpo()*15));
			
		}
		
		a.name.clear().add(biggest.info.armyNames.rnd());
		
		for (WArmySupply s : WARMYD.supplies().all) {
			s.current().set(a, s.max(a));
		}
		
		for (StatEquippableBattle bb : STATS.EQUIP().military()) {
			WARMYD.supplies().get(bb).current().set(a, 0);
		}
		
		MessageText m = new MessageText(¤¤Rebels, ¤¤Rebellion);
		m.paragraph(Str.TMP.clear().add(¤¤HasSpawned).insert(0, region.name()));
		m.send();
		return true;
	}
	
	private Region target(Region start) {
		if (start == null || start.area() == 0) {
			return null;
		}
		if (!World.ARMIES().rebels().canCreate())
			return null;
		
		for (WArmy a : World.ARMIES().rebels().all()) {
			if (a.region() == start)
				return null;
		}
		
		if (start.faction() != FACTIONS.player()) {
			return WPathing.findAdjacentRegion(start.cx(), start.cy(), playerFinder);
		}else {
			return start;
		}
	}
	
	private WArmy spawn(Region start, int men, double training, double gear) {
		COORDINATE c = WPathing.random(start);
		
		WArmy a = World.ARMIES().createRebel(c.x()-1, c.y()-1);
		if (a == null) {
			GAME.Notify(c.x() + " " + c.y());
			return null;
		}
		
		double raceTot = 0;
		Race biggest = RACES.all().get(0);
		int b = 0;
		for (Race r : RACES.all()) {
			raceTot += REGIOND.RACE(r).population.get(start);
			if (REGIOND.RACE(r).population.get(start) > b) {
				biggest = r;
			}
		}
		
		double menLeft = 0;
		for (Race r : RACES.all()) {
		
			menLeft += men*REGIOND.RACE(r).population.get(start)/raceTot;
			
			while (menLeft > 2 && a.divs().canAdd()) {
				
				int am = CLAMP.i((int)menLeft, 0, RES.config().BATTLE.MEN_PER_DIVISION);
				WDivRegional d = World.ARMIES().regional().create(r, (double)am/RES.config().BATTLE.MEN_PER_DIVISION, 0,0, a);
				d.randomize(gear, (int)(training*15));
				d.menSet(d.menTarget());
				menLeft -= a.divs().get(a.divs().size()-1).menTarget();
			}
		}
		
		
		a.name.clear().add(biggest.info.armyNames.rnd());
		
		for (WArmySupply s : WARMYD.supplies().all) {
			s.current().set(a, s.max(a));
		}
		
		return a;
	}
	
	public WArmy spawn(Region start) {
		COORDINATE c = WPathing.random(start);
		
		WArmy a = World.ARMIES().createRebel(c.x()-1, c.y()-1);
		if (a == null) {
			GAME.Notify(c.x() + " " + c.y());
			return null;
		}
		
		Region target = WPathing.findAdjacentRegion(start.cx(), start.cy(), playerFinder);

		if (target == null)
			return null;
		
		int men = REGIOND.MILITARY().soldiers.get(target);
		
		for (WArmy a2 : FACTIONS.player().kingdom().armies().all()) {
			if (a2.region() == target) {
				men += WARMYD.men(null).get(a2);
			}else if(a2.region().faction() == FACTIONS.player())
				men += 0.25* WARMYD.men(null).get(a2);
			else
				men += 0.125* WARMYD.men(null).get(a2);
		}
		
		men *= 1.0 + RND.rExpo();
		
		if (men < 30)
			men = 30 + RND.rInt(20);
		
		
		men = CLAMP.i(men, 0, RES.config().BATTLE.MEN_PER_ARMY);
		
		double raceTot = 0;
		Race biggest = RACES.all().get(0);
		int b = 0;
		for (Race r : RACES.all()) {
			raceTot += REGIOND.RACE(r).population.get(start);
			if (REGIOND.RACE(r).population.get(start) > b) {
				biggest = r;
			}
		}
		
		double menLeft = 0;
		
		for (Race r : RACES.all()) {
		
			menLeft += men*REGIOND.RACE(r).population.get(start)/raceTot;
			
			while (menLeft > 2 && a.divs().canAdd()) {
				
				int am = CLAMP.i((int)menLeft, 0, RES.config().BATTLE.MEN_PER_DIVISION);
				
				WDivRegional d = World.ARMIES().regional().create(r, (double)am/RES.config().BATTLE.MEN_PER_DIVISION, 0,0, a);
				d.randomize(RND.rExpo(), (int)(RND.rExpo()*15));
				d.menSet(d.menTarget());
				menLeft -= am;
				
			}
		}
		
		a.name.clear().add(biggest.info.armyNames.rnd());
		
		for (WArmySupply s : WARMYD.supplies().all) {
			s.current().set(a, s.max(a));
		}
		
		return a;
		
		
	}
	
	private final BOOLEAN_OBJECT<Region> playerFinder = new BOOLEAN_OBJECT<Region>() {

		@Override
		public boolean is(Region t) {
			return t.faction() == FACTIONS.player();
		}
		
	};
	
	private final BOOLEAN_OBJECT<Region> rebelFinder = new BOOLEAN_OBJECT<Region>() {

		@Override
		public boolean is(Region t) {
			return !t.isWater() && t.faction() == null;
		}
		
	};
	
	private static class MProtection extends Message{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final Timer timer;
		
		public MProtection(Timer timer) {
			super(¤¤protection);
			this.timer = timer;
		}

		@Override
		protected RENDEROBJ makeSection() {
			GuiSection section = new GuiSection();
			
			Str t = Str.TMP;
			t.clear().add(¤¤protectionD);
			t.insert(0, World.REGIONS().getByIndex(timer.regionI).name());
			t.insert(1, timer.credits);
			t.insert(2, DicRes.¤¤Currs);
			
			section.add(new GText(UI.FONT().M, ""+t).setMaxWidth(WIDTH),0,0);
			
			
			t.clear().add(¤¤proInfo);
			t.insert(0, timer.size);
			t.insert(1, (int)(100*timer.training));
			t.insert(2, (int)(100*timer.gear));
			
			section.addDown(8, new GText(UI.FONT().M, ""+t).setMaxWidth(WIDTH));
			
			
			section.addRelBody(48, DIR.S, new GButt.ButtPanel(¤¤pay) {
				
				@Override
				protected void renAction() {
					activeSet(!timer.hasExpired());
				}
				
				@Override
				protected void clickA() {
					FACTIONS.player().credits().tribute.OUT.inc(timer.credits);
					SETT.ROOMS().STOCKPILE.removeFromEverywhere(0.1, -1l, FACTIONS.player().res().outTribute);
					for (Timer t : GAME.events().raider.awaiting)
						if (t.regionI == timer.regionI && t.day == timer.day) {
							GAME.events().raider.awaiting.iteratorRemoveCurrent();
							timer.day = -10;
							close();
							return;
						}
					timer.day = -10;
					close();
				}
				
			});
			
			section.addRelBody(4, DIR.S, new GButt.ButtPanel(¤¤decline) {
				
				@Override
				protected void renAction() {
					activeSet(!timer.hasExpired());
				}
				
				@Override
				protected void clickA() {
					close();
				}
				
			});
			
			
			return section;
		}
		
	}
	
	private static class Timer implements Serializable{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final int regionI;
		private final int credits;
		private int day;
		private int size;
		private double training;
		private double gear;
		
		public Timer(Region r, int cash, int size, double training, double gear) {
			credits = cash;
			regionI = r.index();
			GAME.events().raider.awaiting.add(this);
			day = TIME.days().bitsSinceStart();
			this.size = size;
			this.training = training;
			this.gear = gear;
		}
		
		public boolean hasExpired() {
			return TIME.days().bitsSinceStart() - day >= 4;
		}
		
	}

	


	
}
