package settlement.room.main;

import java.io.IOException;
import java.util.Arrays;

import game.GameDisposable;
import game.boosting.*;
import game.faction.Faction;
import game.faction.npc.NPCBonus;
import game.time.TIME;
import init.D;
import init.race.POP_CL;
import init.sprite.UI.UI;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.util.file.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sprite.text.Str;
import util.updating.IUpdater;
import view.ui.message.MessageText;

public final class BonusExp implements SAVABLE{

	private static CharSequence ¤¤name = "¤Experience";
	private static CharSequence ¤¤mGainedTitle = "¤Experience Gained";
	
	private static CharSequence ¤¤mGainedBody = "¤We now employ over {0} in our {1}, and as a result, this combined experience is boosting performance. Boosting will continue to increase up until {2} employees.";
	
	private static CharSequence ¤¤mLostTitle = "¤Experience Lost";
	
	
	private static CharSequence ¤¤mLostBody = "¤Since the employees of our {0} have plummeted, performance boosts from experience has been lost.";
	
	static {
		D.ts(BonusExp.class);
	}

	
	private final int[] currents;
	private byte[] sent; 

	
	BonusExp() {

		currents = new int[all.size()];
		sent = new byte[all.size()];
		
		
	}
	
	
	private final IUpdater up = new IUpdater(all.size(), TIME.secondsPerDay) {
		
		@Override
		protected void update(int index, double timeSinceLast) {
			
			RoomExperienceBonus bo = all.get(index);
			
			int am = bo.blue.employment().employed();
			
			if (currents[index] < bo.minEmployed && am >= bo.minEmployed && (sent[index] & 1) == 0) {
				MessageText m = new MessageText(¤¤mGainedTitle);
				Str s = Str.TMP;
				s.clear();
				s.add(¤¤mGainedBody);
				s.insert(0, bo.minEmployed);
				s.insert(1, bo.blue.info.names);
				s.insert(2, bo.maxEmployed);
				m.paragraph(s);
				m.send();
				sent[index] |= 1;
			}else if (currents[index] >= bo.minEmployed && am < bo.minEmployed && (sent[index] & 2) == 0) {
				MessageText m = new MessageText(¤¤mLostTitle);
				
				Str s = Str.TMP;
				s.clear();
				s.add(¤¤mLostBody);
				s.insert(0, bo.blue.info.names);
				
				m.paragraph(s);
				m.send();
				sent[index] |= 2;
			}
			currents[index] = am;
			
			
			
		}
	};
	
	void update(double ds) {
		up.update(ds);
	}

	@Override
	public void save(FilePutter file) {
		file.isE(currents);
		up.save(file);
		
	}

	@Override
	public void load(FileGetter file) throws IOException {
		file.isE(currents);
		up.load(file);
	}

	@Override
	public void clear() {
		Arrays.fill(currents, 0);
	}

	private static final ArrayListGrower<RoomExperienceBonus> all =  new ArrayListGrower<>();
	static {
		new GameDisposable() {
			
			@Override
			protected void dispose() {
				all.clear();
			}
		};
	}
	
	
	public static class RoomExperienceBonus {
		
		
		
		public final double bonus;
		public final int maxEmployed;
		public final int minEmployed;
		private final double ie;
		public final RoomBlueprintImp blue;
		public final Boostable boostable;
		
		public RoomExperienceBonus(RoomBlueprintImp blue, Json data, Boostable boostable){
			all.add(this);
			this.blue = blue;
			this.boostable = boostable;
			
			
			int ma = 600;
			double bo = 0.5;
			
			if (data.has("EXPERIENCE_BONUS")) {
				data = data.json("EXPERIENCE_BONUS");
				ma = data.i("MAX_EMPLOYEES");
				bo = data.d("BONUS");
			}
			
			maxEmployed = ma;
			minEmployed =  100*(int)Math.ceil((ma/10)/100.0);
			bonus = bo;
			ie = 1.0/maxEmployed;
			BoosterImp bos = new BoosterImp(new BSourceInfo(¤¤name, UI.icons().s.clock), 0, bonus, false) {

				
				@Override
				public double vGet(NPCBonus bonus) {
					return bonus.getD(boostable);
				}
				
				@Override
				public double vGet(POP_CL reg) {
					return CLAMP.d((blue.employment().employed()-minEmployed)*ie, 0, 1.0);
				}
				
				@Override
				public double vGet(Induvidual indu) {
					if (STATS.WORK().EMPLOYED.get(indu) != null && STATS.WORK().EMPLOYED.get(indu).blueprintI() == blue) {
						return CLAMP.d((blue.employment().employed()-minEmployed)*ie, 0, 1.0);
					}
					return 0;
				}
				
				@Override
				public boolean has(Class<? extends BOOSTABLE_O> b) {
					return b == Induvidual.class || b == NPCBonus.class || b == NPCBonus.class;
				}

				@Override
				public double vGet(Faction f) {
					// TODO Auto-generated method stub
					return 0;
				}
			};
			
			bos.add(boostable);
			
		}
		
	}

}
