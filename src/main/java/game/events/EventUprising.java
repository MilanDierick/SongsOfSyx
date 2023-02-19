package game.events;

import java.io.IOException;

import game.events.EVENTS.EventResource;
import game.faction.FACTIONS;
import game.time.TIME;
import init.D;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.HTYPE;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.main.throne.THRONE;
import settlement.stats.STATS;
import settlement.stats.standing.STANDINGS;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;
import view.main.MessageText;
import view.sett.IDebugPanelSett;

public final class EventUprising extends EventResource{

	private static CharSequence ¤¤warningA = "¤Slave Submission!";
	private static CharSequence ¤¤warningAD = "¤Slaves are acting a bit out of order lately and submission seems to be low. Perhaps it would be a good thing to free a few to raise the spirits";
	
	
	private static CharSequence ¤¤warning = "¤Slave Warning!";
	private static CharSequence ¤¤warningD = "¤Rumour has it that our wretched slaves feel mistreated. Some battlegear has also mysteriously gone missing. Might be a good time to deploy our troops close to the throne, just in case they think of something...";
	
	private static CharSequence ¤¤riot = "¤Slave Uprising!";
	private static CharSequence ¤¤riotD = "¤May the gods help us, the slaves are rising up to their masters! They claim to have had enough of your mistreatment and are now bent on ending your rule. Should they reach the throne, they will part with a good chunk of our riches and resources, and we shall be forever disgraced. Call in the troops and smite them, while there still is time!";
	private static CharSequence ¤¤amount = "¤{0} slaves have joined the uprising.";
	private static CharSequence ¤¤OverD = "¤The slave uprising has been defeated and your people rejoice at your might. Time to acquire new ones.";
	private static CharSequence ¤¤Over = "¤Uprising crushed!";
	
	private static CharSequence ¤¤LooseD = "¤The filthy slaves have captured the throne through low cunning. The wretches have plundered our stores and out treasury before deserting their master. May the gods help us through this winter.";
	private static CharSequence ¤¤Loose = "¤Slaves triumph!";
	


	private final double updateD = 5;
	private final double speed = updateD/TIME.secondsPerDay;
	
	private double tt = 0;
	private double acc = 0;
	private int state;
	private int amountTotal;
	public final UprisingSpots spots = new UprisingSpots();
	
	static {
		D.ts(EventUprising.class);
	}
	
	EventUprising(){
		
		IDebugPanelSett.add("Slave Uprising", new ACTION() {
			
			@Override
			public void exe() {
				riot();
			}
		});
		
		clear();

	}
	
	
	@Override
	protected void save(FilePutter file) {
		file.i(state);
		file.i(amountTotal);
		file.d(acc);
		spots.save(file);
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		state = file.i();
		amountTotal = file.i();
		acc = file.d();
		spots.load(file);
		acc = 0;
	}
	
	@Override
	protected void clear() {
		state = 0;
		amountTotal = 0;
		spots.clear();
		acc = 0;
	}
	
	@Override
	protected void update(double ds) {
		
		
		if (state == 0) {
			if (SETT.ARMIES().enemy().men() > 0)
				return;
			if (STATS.POP().pop(HTYPE.SLAVE) <= 0)
				return;
			
			tt -= ds;
			if (tt > 0)
				return;
			tt += updateD;
			
			double chance = Math.max(STANDINGS.SLAVE().current(), STANDINGS.SLAVE().target());
			if (chance >= 0.8) {
				acc -= speed*(chance-0.8)/0.2;
				acc = CLAMP.d(acc, 0, 1);
				return;
			}
			chance = Math.pow(chance, 0.5);
			
			double old = acc;
			acc += speed*chance;
			
			if (acc > 1) {
				acc = 0;
				riot();
			}else if (old < 0.8 && acc > 0.8) {
				new MessageText(¤¤warningA, ¤¤warningAD).send();
			}
			
		}else if (state == 1) {
			
			if (spots.update(ds)) {
				state = 2;
				Str t = Str.TMP;
				t.clear();
				t.add(¤¤amount).insert(0, amountTotal);
				acc = 0;
				new MessageText(¤¤riot, ¤¤riotD).paragraph(t).send();
			}
			
		}else if (state == 2) {
			spots.update(ds);
			if (!spots.hasMore()) {
				state = 3;
			}
		}else if (state == 3) {
			if (SETT.ARMIES().enemy().men() == 0) {
				for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
					if (e instanceof Humanoid) {
						Humanoid h = (Humanoid) e;
						if (h.indu().hType() == HTYPE.ENEMY) {
							h.HTypeSet(HTYPE.SLAVE, null, null);
						}
					}
				}
				new MessageText(¤¤Over, ¤¤OverD).send();
				state = 0;
				return;
			}
			
			COORDINATE c = THRONE.coo();
			boolean enemy = false;
			boolean player = false;
			for (int x = c.x()-3; x < c.x()+3; x++) {
				for (int y = c.y()-3; y < c.y()+3; y++) {
					
					for (ENTITY e : SETT.ENTITIES().getAtTile(x, y)){
						if (e instanceof Humanoid) {
							if (((Humanoid)e).indu().hType() == HTYPE.ENEMY  && ((Humanoid)e).division() != null) {
								if (((Humanoid)e).indu().hType() == HTYPE.ENEMY  && ((Humanoid)e).division() != null)
									enemy |= true;
								if (((Humanoid)e).indu().hType().player  && ((Humanoid)e).division() != null)
									player |= true;
								
							}
						}
					}
				}
			}
			if (enemy && !player)
				loose();
			return;
			
			
			
			
		}
		
		
		
		
	}
	
	private void loose() {
		acc = 0;
		double am = 0.25 + 0.05*RND.rInt(6);
		
		SETT.ROOMS().STOCKPILE.removeFromEverywhere(am, -1l, FACTIONS.player().res().outTribute);
		
		int creds = FACTIONS.player().credits().credits() > 0 ? (int) (FACTIONS.player().credits().credits()*0.75) : 0;
		FACTIONS.player().credits().tribute.OUT.inc(creds);
		state = 0;
		
		for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
			if (e instanceof Humanoid) {
				Humanoid h = (Humanoid) e;
				if (h.indu().hType() == HTYPE.ENEMY) {
					h.helloMyNameIsInigoMontoyaYouKilledMyFatherPrepareToDie();
				}
			}
		}
		new MessageText(¤¤Loose, ¤¤LooseD).send();
	}
	
	private void riot() { 
		
		if (state != 0)
			return;
		
		SETT.ARMIES().enemy().initMorale();
		
		int am = (int) (STATS.POP().pop(HTYPE.SLAVE)*(0.2 + RND.rFloat()*0.8));
		am = spots.riot(am);

		if (am == 0)
			return;
		
		amountTotal = am;
		state = 1;
		new MessageText(¤¤warning, ¤¤warningD).send();
	}
	
}
