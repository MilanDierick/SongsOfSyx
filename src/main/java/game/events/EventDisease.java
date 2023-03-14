package game.events;

import java.io.IOException;

import game.events.EVENTS.EventResource;
import game.time.TIME;
import init.D;
import init.boostable.BOOSTABLES;
import init.disease.DISEASE;
import init.disease.DISEASES;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.health.HEALTH;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;
import view.main.MessageText;
import view.sett.IDebugPanelSett;

public final class EventDisease extends EventResource{

	private final double maxTime = 16*TIME.secondsPerDay;
	private double timer = 1;
	private DISEASE currentStrain = null;
	private double spread = 0;
	
	private static CharSequence ¤¤title = "Outbreak!";
	private static CharSequence ¤¤desc = "Dreadful news! A case of {0} has been discovered. Let us hope it doesn't spread. You might want to issue a curfew to prevent the spread.";
	
	static {
		D.ts(EventDisease.class);
	}
	
	EventDisease() {
		IDebugPanelSett.add("Event: infect", new ACTION() {
			
			@Override
			public void exe() {
				outbreak(0.1+RND.rFloat()*0.5, DISEASES.randomEpidemic(SETT.ENV().climate()));
			}
		});
		reset();
	}
	
	@Override
	protected void save(FilePutter file) {
		file.d(timer);
		file.d(spread);
		file.i(currentStrain.index());
		
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		timer = file.d();
		spread = file.d();
		currentStrain = DISEASES.all().getC(file.i());
	}
	
	@Override
	protected void clear() {
		reset();
	}
	
	@Override
	protected void update(double ds) {
		
		if (STATS.POP().POP.data().get(null) < DISEASES.SQUALOR_POPULATION_MIN) {
			timer += ds;
			if (timer > maxTime)
				timer = maxTime;
			return;
		}
		
		double d = HEALTH.rate().getD();
		d *= d;
		d = 1.0-d;
		
		d*= DISEASES.EPIDEMIC_CHANCE;
		
		timer -= ds*d;
		
		if (timer < 0) {
			
			if (SETT.INVADOR().invading()) {
				timer += 0.1;
				return;
			}
			
			outbreak(d, currentStrain);
			
			
		}
		
		
	}
	
	public void outbreak(double spread, DISEASE strain) {
		int am = 0;
		double aveHealth = BOOSTABLES.PHYSICS().HEALTH.get(null, null);
		Humanoid patienZero = null;;
		
		ENTITY[] ee = SETT.ENTITIES().getAllEnts();
		for (int i = 0; i < ee.length; i++) {
			if (ee[i] != null && ee[i] instanceof Humanoid) {
				Humanoid a = (Humanoid) ee[i];
				if (a.indu().player()) {
					double c = spread*(BOOSTABLES.PHYSICS().HEALTH.get(a)/aveHealth);
					if (RND.rFloat() < c) {
						STATS.NEEDS().disease.incubate(a, strain);
						am ++;
						if (RND.oneIn(am))
							patienZero = a;
						
					}
				}
			}
		}
		
		if (am > 1) {
			
			STATS.NEEDS().disease.infect(patienZero, strain);
			
			MessageText t = new MessageText(¤¤title);
			t.paragraph(Str.TMP.clear().add(¤¤desc).insert(0, strain.info.name));
			t.paragraph(strain.info.desc);
			t.send();
			reset();
		}
	}
	
	private void reset() {
		timer = maxTime;
		timer *= 1 + RND.rFloat()*2;
		currentStrain = DISEASES.randomEpidemic(SETT.ENV().climate());
		spread = 0.1 + 0.6*RND.rExpo();
	}

	
}
