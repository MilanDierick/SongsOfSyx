package game.events;

import static settlement.main.SETT.*;

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
import settlement.stats.CAUSE_ARRIVE;
import settlement.stats.STATS;
import snake2d.LOG;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import snake2d.util.sets.Tuple.TupleD;
import snake2d.util.sprite.text.Str;
import view.main.MessageText;
import view.sett.IDebugPanelSett;

public final class EventDisease extends EventResource{

	private double timer = 1;
	private final double peopleI = 1.0/1000;
	private final LIST<TupleD<CAUSE_ARRIVE>> li;
	private DISEASE disease = DISEASES.random();
	private DISEASE currentStrain = DISEASES.random();
	private int dayOfStrain = -12;
	
	
	private static CharSequence ¤¤title = "Outbreak!";
	private static CharSequence ¤¤desc = "Dreadful news! A case of {0} has been discovered. Let us hope it doesn't spread.";
	
	static {
		D.ts(EventDisease.class);
	}
	
	EventDisease() {
		LinkedList<TupleD<CAUSE_ARRIVE>> li = new LinkedList<>();
		for (CAUSE_ARRIVE a : CAUSE_ARRIVE.ALL())
			if (a.fromoutside)
				li.add(new TupleD<CAUSE_ARRIVE>(a));
		this.li = li;
		
		IDebugPanelSett.add("Event: infect", new ACTION() {
			
			@Override
			public void exe() {
				LOG.ln(timer);
				timer = 0;
			}
		});
		
		reset();
	}
	
	@Override
	protected void save(FilePutter file) {
		file.d(timer);
		file.i(disease.index());
		file.i(currentStrain.index());
		file.i(dayOfStrain);
		
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		timer = file.d();
		disease = DISEASES.all().getC(file.i());
		currentStrain = DISEASES.all().getC(file.i());
		dayOfStrain = file.i();
	}
	
	@Override
	protected void clear() {
		reset();
		newPeople();
	}
	
	@Override
	protected void update(double ds) {
		
		double d = DISEASES.climate(SETT.ENV().climate());
		d *= 0.5 + 0.5*STATS.NEEDS().DIRTINESS.stat.data(null).getD(null);
		d*= 1.0 + 10*CLAMP.d((double)THINGS().corpses.added()/(STATS.POP().POP.data().get(null)+1)-0.01, 0, 1);
		//d*= (double)CLAMP.d STATS.POP().POP.data().get(null)/2000;
		timer -= ds*d;
		int np = newPeople();
		timer -= peopleI*np;
		
		
		
		if (timer < 0) {
			
			if (SETT.INVADOR().invading()) {
				timer += 0.1;
				return;
			}
			
			reset();
			
			double chance = currentStrain.spread * (0.25 + 0.75*CLAMP.d(STATS.POP().POP.data(null).get(null)/4000.0, 0, 1));
			
			int am = 0;
			ENTITY[] ee = SETT.ENTITIES().getAllEnts();
			Humanoid patienZero = null;;
			for (int i = 0; i < ee.length; i++) {
				if (ee[i] != null && ee[i] instanceof Humanoid) {
					Humanoid a = (Humanoid) ee[i];
					if (a.indu().player()) {
						double c = chance/(0.1+BOOSTABLES.PHYSICS().HEALTH.get(a));
						if (RND.rFloat() < c) {
							STATS.NEEDS().disease.incubate(a, currentStrain);
							am ++;
							if (RND.oneIn(am))
								patienZero = a;
						}
					}
				}
			}
			
			if (am > 0) {
				
				STATS.NEEDS().disease.infect(patienZero, disease);
				
				MessageText t = new MessageText(¤¤title);
				t.paragraph(Str.TMP.clear().add(¤¤desc).insert(0, disease.info.name));
				t.paragraph(disease.info.desc);
				t.send();
				
			}
			
			
		}
		
		
	}
	
	private void reset() {
		timer = TIME.secondsPerDay*16;
		timer *= 1 + RND.rFloat()*3;
		dayOfStrain = TIME.days().bitsSinceStart();
		currentStrain = disease;
		disease = DISEASES.random();
	}
	
	private int newPeople() {
		double am = 0;
		
		for (TupleD<CAUSE_ARRIVE> a : li) {
			double p = STATS.POP().COUNT.enters().get(a.a.index()).statistics(null).get(null);
			am += CLAMP.d(p-a.d, 0, 100000);
			a.d = p;
		}
		
		return (int) am;
	}

	
}
