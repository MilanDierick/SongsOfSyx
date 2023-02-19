package game.faction.player;

import java.io.IOException;
import java.util.Arrays;

import game.time.TIME;
import init.D;
import init.boostable.*;
import init.boostable.BOOSTER_COLLECTION.BOOSTER_COLLECTION_IMP;
import init.boostable.BOOSTER_COLLECTION.SIMPLE;
import snake2d.util.file.*;
import snake2d.util.sprite.text.Str;
import util.updating.IUpdater;
import view.main.MessageText;

final class PBonusExp extends BOOSTER_COLLECTION_IMP implements SIMPLE, SAVABLE{

	private static CharSequence ¤¤name = "¤Experience";
	private static CharSequence ¤¤mGainedTitle = "¤Experience Gained";
	
	private static CharSequence ¤¤mGainedBody = "¤We now employ over {0} in our {1}, and as a result, this combined experience is boosting the performance by {2}%.";
	private static CharSequence ¤¤mGainedNext = "¤Next Experience boost is at {0} employees.";
	private static CharSequence ¤¤mGainedMax = "¤This is the most that can be gained though experience.";
	
	private static CharSequence ¤¤mLostTitle = "¤Experience Lost";
	
	
	private static CharSequence ¤¤mLostBody = "¤Since the employees of our {0} have plummeted, performance boosts from experience has been lost.";
	
	static {
		D.ts(PBonusExp.class);
	}
	
	private final int[] levels = new int[] {
		0,
		25,
		100,
		300,
		600,
	};
	private final double[] boosts = new double[] {
		0,
		0.08,
		0.16,
		0.3,
		0.6,
	};
	
	private final int[] currents = new int[BOOSTABLES.all().size()];
	private final IUpdater up = new IUpdater(BOOSTABLES.ROOMS().all().size(), TIME.secondsPerDay) {
		
		@Override
		protected void update(int index, double timeSinceLast) {
			BOOSTABLERoom b = (BOOSTABLERoom) BOOSTABLES.ROOMS().all().get(index);
			if (b.room.employment() == null)
				return;
			int am = b.room.employment().employed();
			int level = 0;
			for (int i = 0; i < levels.length; i++) {
				if (am > levels[i])
					level = i;
				else
					break;
			}
			
			if (level > currents[b.index] && level > 0) {
				currents[b.index] = level;
				MessageText m = new MessageText(¤¤mGainedTitle);
				Str s = Str.TMP;
				s.clear();
				s.add(¤¤mGainedBody);
				s.insert(0, levels[level]);
				s.insert(1, b.room.info.names);
				s.insert(2, (int)(100*boosts[level]));
				m.paragraph(s);
				
				s.clear();
				if (level < levels.length-1) {
					s.add(¤¤mGainedNext);
					s.insert(0, levels[level+1]);
					m.paragraph(s);
				}else {
					m.paragraph(¤¤mGainedMax);
				}
				m.send();
			}else if (level > 0 && level < currents[b.index] && am < levels[currents[b.index]]/2) {
				currents[b.index] = level;
				MessageText m = new MessageText(¤¤mLostTitle);
				
				Str s = Str.TMP;
				s.clear();
				s.add(¤¤mLostBody);
				s.insert(0, b.room.info.names);
				
				m.paragraph(s);
				m.send();
			}
			
			
		}
	};
	
	protected PBonusExp() {
		super(¤¤name);
		for(BOOSTABLE b :BOOSTABLES.ROOMS().all()) {
			maxAdd[b.index] = boosts[boosts.length-1];
		}
	}
	
	void update(double ds) {
		up.update(ds);
	}

	@Override
	public double add(BOOSTABLE b) {
		return boosts[currents[b.index]];
	}

	@Override
	public double mul(BOOSTABLE b) {
		return 1;
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

}
