package world.map.regions;


import java.io.IOException;

import init.D;
import init.boostable.BOOSTABLES;
import init.race.RACES;
import init.race.Race;
import init.sprite.ICON;
import init.sprite.SPRITES;
import snake2d.util.file.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.data.INT_O.INT_OE;
import world.map.regions.REGIOND.RResource;
import world.map.regions.RegionFactor.RegionFactorImp;

public final class RegionCivics extends RResource {

	
	
	public final RegionDecreeCivic infrastructure;
	
	public final RegionDecreeCivic law;
	public final RegionDecreeCivic sanitation;
	public final RegionDecreeCivic entertainment;
	public final RegionDecreeCivic education;
	
	private final INT_OE<Region> knowledgeP;
	public final RegionFactors knowledge;
	public final RegionFactors health_target;
	public final INT_OE<Region> health;
	
	public final LIST<RegionDecreeCivic> all;
	

	
	
	private final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			
		}
		
		@Override
		public void clear() {
			
		}
	};
	
	RegionCivics(RegionInit init) {
		
		D.gInit(this);
		
		knowledgeP = init.count. new DataShort();
		knowledge = new RegionFactors(
				D.g("Knowledge"), D.g("KnowledgeD", "The amount of knowledge this region stores that can be used in unlocking technology.")) {
			@Override
			public double getD(Region t) {
				return knowledgeP.get(t);
			}
		};
		
		new RegionFactorImp(knowledge, BOOSTABLES.BEHAVIOUR().LAWFULNESS) {

			@Override
			public double next(Region r) {
				double d = 0;
				double am = 0;
				for (int ri = 0; ri < RACES.all().size(); ri++) {
					Race ra = RACES.all().get(ri);
					d += BOOSTABLES.BEHAVIOUR().LAWFULNESS.race(ra)*REGIOND.RACE(ra).targetPop(r);
					am += REGIOND.RACE(ra).targetPop(r);
				}
				
			
				return d/am;
			}
			
			@Override
			public double getD(Region r) {
				double d = 0;
				double am = 0;
				for (int ri = 0; ri < RACES.all().size(); ri++) {
					Race ra = RACES.all().get(ri);
					d += BOOSTABLES.BEHAVIOUR().LAWFULNESS.race(ra)*REGIOND.RACE(ra).population.get(r);
					am += REGIOND.RACE(ra).population.get(r);
				}
			
				return d/am;
			}
			
		};
		new RegionFactorImp(knowledge, REGIOND.POP().total.info()) {

			@Override
			public double getD(Region r) {
				double d = 0;
				for (int ri = 0; ri < RACES.all().size(); ri++) {
					Race ra = RACES.all().get(ri);
					d += REGIOND.RACE(ra).population.get(r);
				}
			
				return d;
			}

			@Override
			public double next(Region r) {
				double d = 0;
				for (int ri = 0; ri < RACES.all().size(); ri++) {
					Race ra = RACES.all().get(ri);
					d += REGIOND.RACE(ra).targetPop(r);
				}
				return d;
			}
			
		};
		
		
		health_target = new RegionFactors(D.g("Health"), D.g("HealthD", "Having your peoples in good health decreases the chances of disease"));
		health = init.count. new DataByte(health_target.info());
		
		infrastructure = new RegionDecreeCivic(init, null, 
				D.g("Infrastructure"),
				D.g("InfrastructureD", "Infrastructure improves living conditions for all and thus increases maximum carrying capacity of the region. It also increases production"),
				SPRITES.icons().m.infra
				);
		infrastructure.connect(init.tmpProduction, 1.05);
		infrastructure.connect(REGIOND.POP().capacity, 1.1);
		infrastructure.connect(init.tmpGrowth, 1.1);
		
		
		law = new RegionDecreeCivic(init, null, 
				D.g("Law"),
				D.g("LawD", "Law increases loyalty significantly, but decreases production slightly."),
				SPRITES.icons().m.law
				);
		law.connect(init.tmpProduction, 0.90);
		law.connect(init.tmpGrowth, 0.90);
		law.connect(init.tmpLoyalty, 1.2);
		
		sanitation = new RegionDecreeCivic(init, null, 
				D.g("Sanitation"),
				D.g("SanitationD", "Sanitation decreases chances of epidemics and increases growth."),
				SPRITES.icons().m.sanitation
				);
		sanitation.connect(REGIOND.POP().capacity, 1.05);
		sanitation.connect(init.tmpGrowth, 1.15);
		sanitation.connect(health_target, 0, 0.1);
		
		entertainment = new RegionDecreeCivic(init, null, 
				D.g("Entertainment"),
				D.g("EntertainmentD", "Entertainment increases happiness, but decreases industry output slightly."),
				SPRITES.icons().m.entertainment
				);
		entertainment.connect(init.tmpProduction, 0.95);
		entertainment.connect(init.tmpLoyalty, 1.1);
		
		education = new RegionDecreeCivic(init, null, 
				D.g("Education"),
				D.g("EducationD", "Education contributes to your knowledge points and makes subjects more productive, while decreasing growth slightly."),
				SPRITES.icons().m.book
				);
		education.connect(knowledge, 0, 0.0125);
		education.connect(init.tmpGrowth, 0.95);
		education.connect(init.tmpProduction, 1.025);
		
		
		all = new ArrayList<>(infrastructure, law, sanitation, entertainment, education);
		
	}

	@Override
	void update(Region r, double ds) {
		{
			double t = knowledge.next(r);
			double n = knowledgeP.get(r);
			double d = (t-n)/32;
			
			if (d < 0) {
				if (d > -100)
					d = -100;
			}else if (d > 0 && d < 100)
				d = 100;
			
			n += d;
			n = CLAMP.d(n, 0, t);
			knowledgeP.set(r, (int)n);
		}
		{
			double t = health_target.getD(r)*255;
			double n = health.get(r);
			double d = (t-n)/2;
			
			if (d < 0) {
				if (d > -1)
					d = -1;
			}else if (d > 0 && d < 1)
				d = 1;
			
			n += d;
			n = CLAMP.d(n, 0, t);
			health.set(r, (int)n);
		}
		
		
		
	}
	
	@Override
	SAVABLE saver() {
		return saver;
	}
	
	public final class RegionDecreeCivic extends RegionDecree.RegionDecreeImp{
		
		public final ICON icon;
		
		RegionDecreeCivic(RegionInit init, INT_OE<Region> total, CharSequence name, CharSequence desc, ICON.MEDIUM icon) {
			super(init.count. new DataNibble(8) , 1, name, desc);
			this.icon = icon;
		}

		
	}

	@Override
	void remove(Region r, FRegions old) {
		
	}

	@Override
	void add(Region r, FRegions newR) {
		
	}

	@Override
	void generateInit(Region r) {
		// TODO Auto-generated method stub
		
	}

	
	
}
