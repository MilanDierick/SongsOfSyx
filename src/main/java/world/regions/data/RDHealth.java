package world.regions.data;

import game.boosting.*;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.time.TIME;
import init.D;
import init.sprite.UI.UI;
import snake2d.util.misc.ACTION;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;
import util.dic.DicMisc;
import view.ui.message.MessageText;
import world.regions.Region;
import world.regions.data.RD.RDInit;
import world.regions.data.RD.RDUpdatable;
import world.regions.data.RDOutput.RDResource;
import world.regions.data.RData.RDataE;

public class RDHealth extends RDataE {

	private static CharSequence ¤¤name = "¤Health";
	private static CharSequence ¤¤desc = "¤Health must be maintained in a region, else there is a risk of disease.";
	
	static CharSequence ¤¤epidemic = "¤Outbreak";
	private static CharSequence ¤¤epidemicD = "¤The region of {0} has suffered from low health and as a result there has been an outbreak of disease. While the epidemic is lasting, the region will suffer big penalties across the board. You must increase the health in order to save the settlement.";
	
	
	static {
		D.ts(RDHealth.class);
	}
	
	public final Boostable boostablee;
	public final RDataE outbreak;
	private final INT_OE<Region> timer;
	
	
	private static double dTime = 1.0/(TIME.secondsPerDay*2);
	
	public CharSequence eDesc(Region reg) {
		Str.TMP.clear().add(¤¤epidemicD).insert(0, reg.info.name());
		return Str.TMP;
	}
	
	RDHealth(RDInit init) {
		super(init.count.new DataByte(), init, ¤¤name);
		boostablee = BOOSTING.push("HEALTH", 0, ¤¤name, ¤¤desc, UI.icons().s.heart, BoostableCat.WORLD);
		
		new RBooster(new BSourceInfo(DicMisc.¤¤Population, UI.icons().s.human), 1, -10, false) {

			@Override
			public double get(Region t) {
				return RD.RACES().popSize(t);
			}
			
			
		}.add(boostablee);
		
		outbreak = new RDataE(init.count.new DataBit(), init, ¤¤epidemic);
		timer = init.count.new DataNibble();
		
		init.connectable.add(new ACTION() {
			
			@Override
			public void exe() {
				RBooster bo = new RBooster(new BSourceInfo(¤¤epidemic, UI.icons().s.death), 1, 0, true) {
					
					@Override
					protected double get(Region reg) {
						return outbreak.get(reg);
					}
				};
				bo.add(RD.TAX().boost);
				for (RDResource o : RD.OUTPUT().all) {
					bo.add(o.boost);
				}
				bo = new RBooster(new BSourceInfo(¤¤epidemic, UI.icons().s.death), 1, 0.25, true) {
					
					@Override
					protected double get(Region reg) {
						return outbreak.get(reg);
					}
				};
				bo.add(RD.RACES().capacity);
			}
		});
		
		init.upers.add(new RDUpdatable() {
			
			@Override
			public void update(Region reg, double time) {
				
				if (outbreak.get(reg) == 1 && get(reg) > 0) {
					timer.incD(reg, -time*TIME.secondsPerDayI);
					if (timer.get(reg) == 0) {
						outbreak.set(reg, 0);
					}
				}
				
				double d = increase(reg)*dTime*time;
				moveTo(reg, d, d < 0 ? 0 : 255);
				
				if (reg.faction() == FACTIONS.player() && get(reg) == 0 && outbreak.get(reg) == 0 && !reg.capitol()) {
					outbreak.set(reg, 1);
					timer.set(reg, 2 + RND.rInt(4));
					new MessageText(¤¤epidemic).paragraph(eDesc(reg)).send();
				}
					
				
			}
			
			public double increase(Region reg) {
				return (int)255*(boostablee.get(reg)*10)/10.0;
			}
			
			@Override
			public void init(Region reg) {
				setD(reg, 1.0);
			}
		});
		
		new RD.RDOwnerChanger() {
			
			@Override
			public void change(Region reg, Faction oldOwner, Faction newOwner) {
				if (newOwner == FACTIONS.player())
					setD(reg, 1.0);
			}
		};
	}

}
