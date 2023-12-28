package game.faction.player;

import java.io.IOException;
import java.util.Arrays;

import game.boosting.*;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import game.time.TIME;
import init.D;
import init.race.RACES;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import init.tech.TECH;
import init.tech.TECH.TechRequirement;
import init.tech.TECHS;
import settlement.main.SETT;
import settlement.room.knowledge.laboratory.ROOM_LABORATORY;
import settlement.room.knowledge.library.ROOM_LIBRARY;
import snake2d.util.file.*;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import util.data.DOUBLE;
import util.data.INT;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.info.INFO;
import view.interrupter.IDebugPanel;
import view.ui.message.MessageText;

public class PTech {

	private static CharSequence ¤¤low = "¤Knowledge low";
	private static CharSequence ¤¤lowBody = "¤There is not enough knowledge to maintain our current technologies. As a result, all bonuses from technologies are receiving a penalty, and some unlocked mechanics are now re-locked. Make sure your knowledge producing facilities are fully operational, or build more of them.";
	{D.t(this);}
	
	public final INFO info = new INFO(
			D.g("Technology"),
			D.g("desc", "Technologies can be obtained by spending tech points.")
			);
	

	private double pfrozen = 0;
	private final double frozenRate = 100.0/TIME.days().bitSeconds();
	private int[] level = new int[TECHS.ALL().size()];
	private double pPenalty = 1;
	private boolean forgetting = false;
	private double forgetTimer = 50;
	public static final double FORGET_THRESHOLD = 0.8;
	private double askTimer = -10;
	
	private final INT.IntImp allocated = new INT.IntImp() {
		
		final INFO i = new INFO(D.g("Allocated"), D.g("AllocatedD", "Knowledge that has been allocated into technologies"));
		
		@Override
		public INFO info(){
			return i;
		}
		
	};
	
	private final INT frozen = new INT() {
		
		final INFO i = new INFO(D.g("Frozen"), D.g("FrozenD", "Frozen knowledge comes from recently disabled technologies. This knowledge will slowly become available with time."));
		
		@Override
		public INFO info(){
			return i;
		}

		@Override
		public int get() {
			return (int) pfrozen;
		}

		@Override
		public int min() {
			return 0;
		}

		@Override
		public int max() {
			return Integer.MAX_VALUE;
		}
		
	};
	
	private final INT available = new INT() {
		
		final INFO i = new INFO(D.g("Available"), D.g("AvailableD", "Knowledge that is available to be spent on technologies.")) {
			@Override
			public void hover(GUI_BOX box) {
				super.hover(box);
				
				
				
				GBox b = (GBox) box;
				b.NL(8);
				for (ROOM_LABORATORY l : SETT.ROOMS().LABORATORIES) {
					b.textL(l.info.names);
					b.tab(6);
					b.add(GFORMAT.iIncr(b.text(), l.knowledge()));
					b.tab(9);
					b.add(SPRITES.icons().s.arrow_right);
					b.add(GFORMAT.i(b.text(), l.knowledgeCapacity()));
					b.NL();
				}
				b.NL(4);
				
				b.NL(8);
				for (ROOM_LIBRARY l : SETT.ROOMS().LIBRARIES) {
					b.textL(l.info.names);
					b.tab(6);
					GText t = b.text();
					t.add('*');
					b.add(GFORMAT.f1(t, 1+l.boost()));
					b.tab(9);
					b.add(SPRITES.icons().s.arrow_right);
					b.add(GFORMAT.f(b.text(), 1+l.projection()));
					b.NL();
				}
				b.NL(8);
				
				b.textLL(allocated.info().name);
				b.tab(6);
				b.add(GFORMAT.iIncr(b.text(), -allocated.get()));
				b.NL();
				b.textLL(frozen.info().name);
				b.tab(6);
				b.add(GFORMAT.iIncr(b.text(), -frozen.get()));
				
			};
		};
		
		@Override
		public INFO info(){
			return i;
		}

		@Override
		public int get() {
			return (int) (knowledgeMaintained()-allocated().get()-frozen().get());
		}

		@Override
		public int min() {
			return 0;
		}

		@Override
		public int max() {
			return Integer.MAX_VALUE;
		}
		
	};
	
	private final DOUBLE penalty = new DOUBLE() {
		
		final INFO i = new INFO(D.g("Penalty"), D.g("PenaltyD", "When your allocated knowledge exceeds your total knowledge, technologies drastically become less efficient. Either increase your knowledge pool, or disable technologies to steer clear of doom."));
		
		@Override
		public double getD() {
			return pPenalty;
		}
		
		@Override
		public INFO info(){
			return i;
		}
	};
	
	public DOUBLE penalty() {
		return penalty;
	}

	public final BoostSpecs boosters = new BoostSpecs(TECHS.¤¤name, UI.icons().s.vial, true);
	private final BoostCompound<TECH> bos;
	
	PTech(){


		IDebugPanel.add("unlockRooms", new ACTION() {
			
			@Override
			public void exe() {
				SETT.ROOMS().LABORATORIES.get(0).knowledgeAdd(1000000);
				for (int ti = 0; ti < TECHS.ALL().size(); ti++) {
					TECH t = TECHS.ALL().get(ti);
					if (t.lockers.all().size() > 0) {
						levelSet(t, t.levelMax);
					}
				}
			}
		});

		bos = new BoostCompound<TECH>(boosters, TECHS.ALL()) {

			private final double npcTech = tech();
			
			@Override
			protected BoostSpecs bos(TECH t) {
				BoostSpecs bos = new BoostSpecs(t.boosters.info.name, t.boosters.info.icon, false);
				for (BoostSpec s : t.boosters.all()) {
					double to = s.booster.isMul ? ((s.booster.to()-1)*t.levelMax + 1) : s.booster.to()*t.levelMax;
					BoosterImp b = new BoosterImp(t.boosters.info, s.booster.from(), to, s.booster.isMul) {
						@Override
						public double vGet(Faction f) {
							return s.booster.vGet(f);
						}
					};
					bos.push(b, s.boostable);
				}
				
				
				
				return bos;
			}

			@Override
			protected double getValue(TECH t) {
				return (double)pPenalty*level(t)/t.levelMax;
			}

			@Override
			protected double get(Boostable bo, FactionNPC f, boolean isMul) {
				return super.get(bo, f, isMul)*npcTech;
			}
			
			private double tech() {
				double techCost = 0;
				
				for (TECH t : TECHS.ALL()) {
					techCost += PTech.costTotal(t, t.levelMax);
				}
				
				double la = 0;
				for (ROOM_LABORATORY l : SETT.ROOMS().LABORATORIES)
					la = Math.max(la, l.knowledgePerStation());
				
				double li = 0;
				for (ROOM_LIBRARY l : SETT.ROOMS().LIBRARIES)
					li = Math.max(li, l.boostPerStation());
				
				double pop = 5000;
				
				double labs = (1+(li*pop))/(2*li);
				double libs = pop-labs;
				double know = labs*la*(1.0+libs*li);
				
				double d = CLAMP.d(know/techCost, 0, 1);
				
				return 1.0+d;
			}
		
		};
		
	}
	

	
	private void setBonuses() {
		allocated.set(0);
		for (TECH t : TECHS.ALL()) {
			int l = level(t);
			if (l > 0) {
				allocated.inc(costTotal(t, l));
			}
		}
		bos.clearChache();
		setPenalty();
	}
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			file.i(TECHS.ALL().size());
			file.i(BOOSTABLES.all().size());
			file.i(SETT.ROOMS().all().size());
			file.is(level);
			allocated.save(file);
			file.d(pfrozen);
			file.bool(forgetting);
			file.d(forgetTimer);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			int tS = file.i();
			int bS = file.i();
			int rz = file.i();
			if (tS != TECHS.ALL().size() || bS != BOOSTABLES.all().size() || rz != SETT.ROOMS().all().size()) {
				file.is(new int[tS]);
				Arrays.fill(level, 0);
				allocated.load(file);
				file.d();
				file.bool();
				file.d();
				
				allocated.set(0);
				pfrozen = 0;
				forgetting = false;
				forgetTimer = 50;
			}else {
				file.is(level);
				allocated.load(file);
				pfrozen = file.d();
				forgetting = file.bool();
				forgetTimer = file.d();
			}
			setBonuses();
			askTimer = -10;
			bos.clearChache();
		}
		
		@Override
		public void clear() {

		}
	};
	


	
	private void setPenalty() {
		if (FACTIONS.player() == null || FACTIONS.player().capitolRegion() == null) {
			pPenalty = 1;
			return;
		}
			
		double tot = knowledgeMaintained();
		double all = allocated().get()*FORGET_THRESHOLD;
		
		if (tot == 0) {
			pPenalty = available().get() < 0 ? 0 : 1;
		}else if (all > tot) {
			pPenalty = tot/all;
			pPenalty *= pPenalty;
		}else {
			pPenalty = 1;
		}	
	}
	
	public long knowledgeCapacity() {
		return cap;
	}
	
	public long knowledgeMaintained() {
		return main;
	}
	

	private long pknowledgeCapacity() {
		int am = 0;
		for (ROOM_LABORATORY l : SETT.ROOMS().LABORATORIES)
			am += l.knowledgeCapacity();
		double b = 1.0;
		for (ROOM_LIBRARY l : SETT.ROOMS().LIBRARIES)
			b += l.projection();
		am *= b;
		double s = BOOSTABLES.START().KNOWLEDGE.get(RACES.clP(null, null));
		am += Math.ceil(s);
		return (long) (am);
	}

	public long pknowledgeMaintained() {
		
		long am = 0;
		for (ROOM_LABORATORY l : SETT.ROOMS().LABORATORIES)
			am += l.knowledge();
		double b = 1.0;
		for (ROOM_LIBRARY l : SETT.ROOMS().LIBRARIES)
			b += l.boost();
		
		am *= b;
		am += BOOSTABLES.START().KNOWLEDGE.get(RACES.clP(null, null));
		return am;
	}
	
	private long cap,main;

	
	void update(double ds) {
		askTimer -= ds;
		
		if (askTimer <= 0) {
			cap = pknowledgeCapacity();
			main = pknowledgeMaintained();
			askTimer += 2.5;
		}
		
		if (pfrozen > 0) {
			double dfrocen = pfrozen/(TIME.secondsPerDay*4);
			dfrocen = Math.max(dfrocen, frozenRate);
			
			pfrozen -= dfrocen*ds;
			if (pfrozen < 0)
				pfrozen = 0;
			
			
		}
		setPenalty();
		forgetTimer+=ds;
		if (pPenalty < 1) {
			if (!forgetting && forgetTimer > 30) {
				forgetting = true;
				new MessageText(¤¤low, ¤¤lowBody).send();
				bos.clearChache();
				forgetTimer = 0;
			}else {
				
			}
		}else {
			forgetting = false;
		}
	}
	
	public INT allocated() {
		return allocated;
	}
	
	public INT frozen() {
		return frozen;
	}
	
	public double penaltyNext() {
		double tot = knowledgeMaintained();
		double all = allocated().get()-frozen.get();
		
		if (tot == 0) {
			return all <= 0 ? 1 : 0;
		}else if (all > tot) {
			double pPenalty = tot/all;
			return pPenalty *pPenalty;
		}else {
			return 1;
		}	
	}
	
	public INT available() {
		return available;
	}
	
	public int level(TECH tech) {
		return level[tech.index()];
	}
	
	public void levelSet(TECH tech, int level) {
		level = CLAMP.i(level, 0, tech.levelMax);
		if (level < level(tech)) {
			pfrozen += costTotal(tech)-costTotal(tech, level);
		}
		
		this.level[tech.index()] = level;
		setBonuses();
		
	}
	
	public int costLevel(TECH tech) {
		return costLevel(tech, level(tech));
	}
	
	public int costLevelNext(TECH tech) {
		return costLevel(tech, level(tech)+1);
	}
	
	public int costLevel(TECH tech, int level) {
		int am = tech.levelCost;
		if (level > 1) {
			am += Math.round(tech.levelCost*(Math.pow(tech.levelCostMulInc, level-1)-1));
			am += tech.levelCostInc*CLAMP.i(level-1, 0, level);
		}
		
		return am;
	}
	
	public int costTotal(TECH tech) {
		return costTotal(tech, level(tech));
	}
	
	public static int costTotal(TECH tech, int level) {
		
		if (tech.levelCostMulInc > 1) {
			
			double m = (Math.pow(tech.levelCostMulInc, level)-1)/(tech.levelCostMulInc-1);
			return (int) Math.round(tech.levelCost*(m));
		}else {
			int A = tech.levelCost;
			int B = tech.levelCostInc;
			int L = level;
			
			int am = A*L;
			am += (((L-1)*L)/2)*B;
			return am;
		}
		
	}
	
	public int costOfNextWithRequired(TECH tech) {
		return costLevelNext(tech) + costOfRequired(tech);
	}
	
	public int costOfRequired(TECH tech) {
		int am = 0;
		for (TechRequirement r : tech.requires()) {
			am += Math.max(costTotal(r.tech, r.level) - costTotal(r.tech, level(r.tech)), 0);
		}
		return am;
	}
	
}