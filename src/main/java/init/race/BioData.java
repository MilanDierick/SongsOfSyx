package init.race;

import game.time.TIME;
import init.D;
import init.boostable.BOOSTABLES;
import init.race.Bio.BIO_LINE;
import settlement.army.Div;
import settlement.entity.ENTITY;
import settlement.entity.animal.Animal;
import settlement.entity.humanoid.HCLASS;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.home.HOME;
import settlement.room.main.RoomEmploymentSimple;
import settlement.stats.CAUSE_ARRIVE;
import settlement.stats.STATS;
import settlement.stats.StatsTraits.StatTrait;
import snake2d.util.MATH;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import snake2d.util.sets.LISTE;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.StrInserter;
import util.dic.DicGeo;
import util.dic.DicMisc;
import world.World;
import world.map.regions.Region;

final class BioData {

	final LinkedList<BIO_LINE> descs = new LinkedList<>();
	final LinkedList<BIO_LINE> houseP = new LinkedList<>();
	
	private static CharSequence ¤¤poor = "¤poor";
	private static CharSequence ¤¤good = "¤good";
	private static CharSequence ¤¤excellent = "¤excellent";
	
	private static final Inserts inserts = new Inserts();
	
	static {
		D.ts(BioData.class);
	}
	
	BioData(Json json){
		
		new BioLine(descs, json, "INFO_GENERAL");;
		
		new BioLine(descs, json, "INFO_TITLE").nlSet();
		
		new BioLine(descs, json, "INFO_GENERAL2");
		
		new BioLine(descs, json, "TRAIT") {
			
			@Override
			protected boolean use(Humanoid a) {
				for (StatTrait i : STATS.TRAITS().all())
					if (i.is(a.indu()))
						return true;
				return false;
			}
		};
		
		new BioLine(descs, json, "TRAIT_NONE") {
			@Override
			protected boolean use(Humanoid a) {
				for (StatTrait i : STATS.TRAITS().all())
					if (i.is(a.indu()))
						return false;
				return true;
			}
		};
		
		new Origin(descs, json, "ORIGIN_NATIVE", CAUSE_ARRIVE.BORN);
		new Origin(descs, json, "ORIGIN_IMMI", CAUSE_ARRIVE.IMMIGRATED);
		new Origin(descs, json, "ORIGIN_FREED", CAUSE_ARRIVE.EMANCIPATED);
		new Origin(descs, json, "ORIGIN_PAROLE", CAUSE_ARRIVE.PAROLE);
		new Origin(descs, json, "ORIGIN_SOLDIER", CAUSE_ARRIVE.SOLDIER_RETURN);
		new Origin(descs, json, "ORIGIN_INSANE", CAUSE_ARRIVE.CURED);
		
		houseP.add(new BioLine(descs, json, "HOME") {
			@Override
			protected boolean use(Humanoid a) {
				if (a.indu().clas() == HCLASS.NOBLE)
					return false;
				HOME h = STATS.HOME().GETTER.get(a, this);
				if (h == null)
					return false;
				h.done();
				
				return h.occupants() > 1;
			};
		});
		
		houseP.add(new BioLine(descs, json, "HOME_ALONE") {
			@Override
			protected boolean use(Humanoid a) {
				if (a.indu().clas() == HCLASS.NOBLE)
					return false;
				HOME h = STATS.HOME().GETTER.get(a, this);
				if (h == null)
					return false;
				h.done();
				return h.occupants() == 1;
			};
		});
		
		houseP.add(new BioLine(descs, json, "HOME_NOBLE") {
			@Override
			protected boolean use(Humanoid a) {
				return a.indu().clas() == HCLASS.NOBLE && STATS.HOME().GETTER.has(a);
			};
		});
		
		houseP.add(new BioLine(descs, json, "HOME_NONE_WORK") {
			@Override
			protected boolean use(Humanoid a) {
				return STATS.HOME().GETTER.hasSearched.indu().isMax(a.indu()) && !STATS.HOME().GETTER.has(a) && STATS.WORK().EMPLOYED.get(a) != null;
			};
		});
		
		houseP.add(new BioLine(descs, json, "HOME_NONE") {
			@Override
			protected boolean use(Humanoid a) {
				return STATS.HOME().GETTER.hasSearched.indu().isMax(a.indu()) && !STATS.HOME().GETTER.has(a) && STATS.WORK().EMPLOYED.get(a) == null;
			};
		});
		
		houseP.add(new BioLine(descs, json, "HOME_NONE_SEARCH") {
			@Override
			protected boolean use(Humanoid a) {
				return !STATS.HOME().GETTER.hasSearched.indu().isMax(a.indu()) && !STATS.HOME().GETTER.has(a);
			};
		});
		
		new BioLine(descs, json, "DIVISION") {
			@Override
			protected boolean use(Humanoid a) {
				return STATS.BATTLE().DIV.get(a) != null;
			};
		};
		
		new BioLine(descs, json, "DIVISION_RECRUIT") {
			@Override
			protected boolean use(Humanoid a) {
				return STATS.BATTLE().DIV.get(a) == null && STATS.BATTLE().RECRUIT.get(a) != null;
			};
		};
		
		new BioLine(descs, json, "DIVISION_NONE") {
			@Override
			protected boolean use(Humanoid a) {
				return STATS.BATTLE().DIV.get(a) == null && STATS.BATTLE().RECRUIT.get(a) == null;
			};
		};
		
		new BioLine(descs, json, "WORK_NOBLE") {
			@Override
			protected boolean use(Humanoid a) {
				return !STATS.WORK().WORK_TIME.indu().isMax(a.indu()) && a.indu().clas() == HCLASS.NOBLE;
			};
		};
		
		new BioLine(descs, json, "WORK_EMPLOYED") {
			@Override
			protected boolean use(Humanoid a) {
				return !STATS.WORK().WORK_TIME.indu().isMax(a.indu()) && a.indu().clas() != HCLASS.NOBLE && STATS.WORK().EMPLOYED.get(a) != null;
			};
		};
		
		new BioLine(descs, json, "WORK_UNEMPLOYED") {
			@Override
			protected boolean use(Humanoid a) {
				return !STATS.WORK().WORK_TIME.indu().isMax(a.indu()) && a.indu().clas() != HCLASS.NOBLE && STATS.WORK().EMPLOYED.get(a) == null;
			};
		};
		
		new BioLine(descs, json, "WORK_LEISURE") {
			@Override
			protected boolean use(Humanoid a) {
				return STATS.WORK().WORK_TIME.indu().isMax(a.indu());
			};
		};
		
		new Friend(descs, json, "FRIEND") {
			@Override
			protected boolean use(Humanoid a) {
				if (super.use(a)) {
					Humanoid b = (Humanoid) STATS.POP().FRIEND.get(a.indu());
					return a.race().pref().other(b.indu().race()) >= 0.5;
				}
				return false;
			};
		};
		
		new Friend(descs, json, "FRIEND_ENEMY") {
			@Override
			protected boolean use(Humanoid a) {
				if (super.use(a)) {
					Humanoid b = (Humanoid) STATS.POP().FRIEND.get(a.indu());
					return a.race().pref().other(b.indu().race()) < 0.5;
				}
				return false;
			};
		};
		
		new BioLine(descs, json, "FRIEND_OTHER") {
			@Override
			protected boolean use(Humanoid a) {
				return STATS.POP().FRIEND.get(a.indu()) != null && (STATS.POP().FRIEND.get(a.indu()) instanceof Animal);
			};
		};
		
		new BioLine(descs, json, "DREAMS");

	}
	
	public static class BioLine implements BIO_LINE{
		
		protected final CharSequence[] strings;
		protected boolean nl = false;
		protected final int index;
		private static Str str = new Str(256);
		BioLine(LISTE<BIO_LINE> all, Json json, String key){
			strings = strings(json, key);
			index = all.add(this);
		}
		
		protected CharSequence[] strings(Json json, String key) {
			return json.texts(key);
		}

		protected boolean use(Humanoid a) {
			return true;
		}
		
		protected BioLine nlSet() {
			nl = true;
			return this;
		}
		
		@Override
		public final CharSequence get(Humanoid a) {
			if (strings.length == 0)
				return null;
			if (!use(a))
				return null;
			int ran = a.indu().ran(index*5);
			CharSequence s = strings[MATH.mod((int)ran, strings.length)];
			str.clear().add(s);
			for (init.race.BioData.Inserts.IInsert in : inserts.all) {
				in.insert(a, str);
			}
			return str;
		}
		
		@Override
		public boolean nl() {
			return nl;
		}
		
	}
	
	private static class Origin extends BioLine {

		private final CAUSE_ARRIVE ca;
		
		Origin(LISTE<BIO_LINE> all, Json json, String key, CAUSE_ARRIVE ca) {
			super(all, json, key);
			this.ca = ca;
		}
		
		@Override
		protected boolean use(Humanoid a) {
			if (a.indu().clas() != HCLASS.CITIZEN)
				return false;
			if (STATS.POP().COUNT.arrive.get(a.indu()) != ca)
				return false;
			return true;
		}
		
		
	}
	
	private static class Friend extends BioLine {
		
		Friend(LISTE<BIO_LINE> all, Json json, String key) {
			super(all, json, key);
		}
		
		@Override
		protected boolean use(Humanoid a) {
			return STATS.POP().FRIEND.get(a.indu()) != null && STATS.POP().FRIEND.get(a.indu()) instanceof Humanoid;
		}
	}
	
	private static class Inserts {
		
		private LinkedList<IInsert> all = new LinkedList<>();
		
		Inserts(){
			new IInsert("NAME") {
				@Override
				public void set(Humanoid t, Str str) {
					str.add(STATS.APPEARANCE().name(t.indu()));
				}
			}; 
			new IInsert("RACE") {
				@Override
				public void set(Humanoid t, Str str) {
					str.add(t.race().info.name);
				}
			}; 
			new IInsert("RACES") {
				@Override
				public void set(Humanoid t, Str str) {
					str.add(t.race().info.names);
				}
			}; 
			new IInsert("CLASS") {
				@Override
				public void set(Humanoid t, Str str) {
					str.add(t.indu().clas().name);
				}
			}; 
			new IInsert("WORKPLACE") {
				@Override
				public void set(Humanoid t, Str str) {
					if (STATS.WORK().EMPLOYED.get(t.indu()) != null)
						str.add(STATS.WORK().EMPLOYED.get(t.indu()).name());
				}
			}; 
			new IInsert("RACE_POSSESSIVE") {
				@Override
				public void set(Humanoid t, Str str) {
					str.add(t.race().info.namePosessive);
				}
			}; 
			new IInsert("RACE_POSSESSIVE") {
				@Override
				public void set(Humanoid t, Str str) {
					str.add(t.race().info.namePosessive);
				}
			}; 
			new IInsert("AGE") {
				@Override
				public void set(Humanoid a, Str str) {
					int i = (int)(STATS.POP().AGE.indu().get(a.indu())/TIME.years().bitConversion(TIME.days()));
					str.add(i);
				}
			}; 
			new IInsert("TITLE") {
				@Override
				public void set(Humanoid t, Str str) {
					str.add(t.title());
				}
			}; 
			new IInsert("HEALTH") {
				@Override
				public void set(Humanoid a, Str str) {
					double def = BOOSTABLES.PHYSICS().HEALTH.defAdd;
					if (BOOSTABLES.PHYSICS().HEALTH.get(a) < def)
						str.add(¤¤poor);
					else if (BOOSTABLES.PHYSICS().HEALTH.get(a) < 1.5)
						str.add(¤¤good);
					else
						str.add(¤¤excellent);
				}
			}; 
			new IInsert("WEIGHT") {
				@Override
				public void set(Humanoid a, Str str) {
					str.add(a.physics.getMass(), 1);
				}
			}; 
			new IInsert("HEIGHT") {
				@Override
				public void set(Humanoid a, Str str) {
					str.add(a.physics.getHeight(), 1);
				}
			}; 
			new IInsert("TRAITS") {
				@Override
				public void set(Humanoid a, Str str) {
					int am = 0;
					for (StatTrait i : STATS.TRAITS().all())
						if (i.is(a.indu()))
							am++;
					if (am > 1) {
						for (StatTrait i : STATS.TRAITS().all())
							if (i.is(a.indu())) {
								str.add(i.info().name);
								am --;
								if (am == 1)
									str.s().add(DicMisc.¤¤and).s();
								else if (am > 1)
									str.add(',').s();
							}
					}else {
						for (StatTrait i : STATS.TRAITS().all())
							if (i.is(a.indu()))
								str.add(i.info().name);
					}
				}
			}; 
			new IInsert("RELIGION") {
				@Override
				public void set(Humanoid a, Str str) {
					str.add(STATS.RELIGION().getter.get(a.indu()).info.name);
				}
			}; 
			new IInsert("RND_REGION") {
				@Override
				public void set(Humanoid a, Str str) {
					int ran = (int) a.indu().randomness2();
					ran = MATH.mod(ran, World.TAREA());
					int x = ran%World.TWIDTH();
					int y = ran/World.THEIGHT();
					
					Region r = World.REGIONS().getter.get(x, y);
					if (r == null || r.isWater()) {
						outer:
						for (int i = 0; i < World.TWIDTH(); i++) {
							for (DIR d : DIR.ALL) {
								r = World.REGIONS().getter.get(x+d.x()*i, y+d.y()*i);
								if (r != null && !r.isWater()) {
									break outer;
								}
							}
						}
					}
					if (r != null && !r.isWater())
						str.add(r.name());
					else {
						str.add('?');
					}
				}
			};
			new IInsert("HOME_TYPE") {
				@Override
				public void set(Humanoid a, Str str) {
					HOME h = STATS.HOME().GETTER.get(a, this);
					if (h != null) {
						str.add(h.nameHome());
						h.done();
					}
				}
			}; 
			new IInsert("HOME_LOCATION") {
				@Override
				public void set(Humanoid a, Str str) {
					HOME h = STATS.HOME().GETTER.get(a, this);
					if (h != null) {
						DIR d = DIR.get(SETT.TWIDTH/2, SETT.THEIGHT/2, h.body().cX(), h.body().cY());
						if (COORDINATE.tileDistance(SETT.TWIDTH/2, SETT.THEIGHT/2, h.body().cX(), h.body().cY()) < 150)
							d = DIR.C;
						str.add(DicGeo.get(d));
						h.done();
					}
				}
			}; 
			new IInsert("HOME_MATES") {
				@Override
				public void set(Humanoid a, Str str) {
					HOME h = STATS.HOME().GETTER.get(a, this);
					if (h != null) {
						int am = 1;
						
						for (int i = 0; i < h.occupants(); i++) {
							if (h.occupant(i) != a) {
								am++;
								if (h.occupants() == 2 || am == h.occupants())
									str.add(STATS.APPEARANCE().name(h.occupant(i).indu()));
								else if(h.occupants() > 2 && am == h.occupants()-1)
									str.s().add(DicMisc.¤¤and).s().add(STATS.APPEARANCE().name(h.occupant(i).indu()));
								else
									str.add(',').s().add(STATS.APPEARANCE().name(h.occupant(i).indu()));
							}
						}
						h.done();
					}
				}
			}; 
			new IInsert("DIVISION") {
				@Override
				public void set(Humanoid a, Str str) {
					Div d = STATS.BATTLE().DIV.get(a);
					if (d == null)
						d = STATS.BATTLE().RECRUIT.get(a);
					if (d != null)
						str.add(d.info.name());
				}
			}; 
			new IInsert("FRIEND_NAME") {
				@Override
				public void set(Humanoid a, Str str) {
					
					ENTITY b = STATS.POP().FRIEND.get(a.indu());
					if (b instanceof Humanoid)
						str.add(STATS.APPEARANCE().name(((Humanoid)b).indu()));
					else if (b instanceof Animal)
						str.add(((Animal)b).species().name);
				}
			}; 
			new IInsert("FRIEND_TITLE") {
				@Override
				public void set(Humanoid a, Str str) {
					
					ENTITY b = (Humanoid) STATS.POP().FRIEND.get(a.indu());
					if (b instanceof Humanoid)
						str.add(((Humanoid)b).title());
				}
			}; 
			new IInsert("FRIEND_LOC") {
				@Override
				public void set(Humanoid a, Str str) {
					
					ENTITY b = (Humanoid) STATS.POP().FRIEND.get(a.indu());
					if (b != null) {
						DIR d = DIR.get(SETT.TWIDTH/2, SETT.THEIGHT/2, b.tc().x(), b.tc().y());
						if (COORDINATE.tileDistance(SETT.TWIDTH/2, SETT.THEIGHT/2, b.tc().x(), b.tc().y()) < 150)
							d = DIR.C;
						str.add(DicGeo.get(d));
					}
				}
			}; 
			new IInsert("FRIEND_RACE") {
				@Override
				public void set(Humanoid a, Str str) {
					
					ENTITY b = (Humanoid) STATS.POP().FRIEND.get(a.indu());
					if (b instanceof Humanoid)
						str.add(((Humanoid)b).race().info.name);
					else if (b instanceof Animal)
						str.add(((Animal)b).species().name);
				}
			}; 
			new IInsert("RND_PROFESSION") {
				@Override
				public void set(Humanoid a, Str str) {
					RoomEmploymentSimple r = null;
					double m = 0;
					for (RoomEmploymentSimple e : SETT.ROOMS().employment.ALLS()) {
						if (a.race().pref().getWork(e) > m) {
							r = e;
							m = a.race().pref().getWork(e);
						}
					}
					int i = (int) a.indu().randomness2() & 0b1111;
					if (i > 0)
						for (RoomEmploymentSimple e : SETT.ROOMS().employment.ALLS()) {
							
							if (Math.abs(m-a.race().pref().getWork(e)) < 0.1) {
								r = e;
								i--;
								if (i <= 0)
									break;
							}
						}
					str.add(r.title);
				}
			}; 
			
		}

		abstract class IInsert extends StrInserter<Humanoid>{

			public IInsert(String key) {
				super(key);
				all.add(this);
			}
		}
		
	}
	

	
	
	
	
	

	
}
