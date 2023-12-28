package game.faction.diplomacy;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import game.faction.npc.ruler.ROpinions;
import init.D;
import init.sprite.UI.UI;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import util.data.BOOLEAN.BOOLEANImp;
import util.data.GETTER;
import util.dic.DicGeo;
import util.info.INFO;
import view.main.VIEW;
import world.WORLD;
import world.map.pathing.WRegSel;
import world.map.pathing.WRegs.RDist;
import world.map.pathing.WTREATY;

public final class Deal {

	private static Deal tmp;
	
	private final GETTER.GETTER_IMP<FactionNPC> f = new GETTER.GETTER_IMP<FactionNPC>();
	public final DealParty player = new DealParty(this, new GETTER.GETTER_IMP<Faction>(FACTIONS.player()));
	public final DealParty npc = new DealParty(this, f);
	

	public static CharSequence ¤¤tradeCancel = "Cancel Trade";
	public static CharSequence ¤¤aid = "Military Aid";
	public static CharSequence ¤¤aidD = "Declare war on each other's enemies.";

	static {
		D.ts(Deal.class);
	}
	
	private ArrayListGrower<DealBool> bools = new ArrayListGrower<>();
	
	public DealBool trade = new DealBool(DicGeo.¤¤Trade, DicGeo.¤¤TradeD, UI.icons().s.urn.twin(UI.icons().s.chevron(DIR.W)), false) {
		@Override
		public boolean possible() {
			if (FACTIONS.DIP().war.is(FACTIONS.player(), f.get()))
				return false;
			
			if (!FACTIONS.pRel().tradersPotential().contains(f.get()))
				return false;
			return !FACTIONS.DIP().trades(FACTIONS.player(), f.get());
		}

		@Override
		public double value() {
			double d = WORLD.PATH().distance(f.get().capitolRegion(), FACTIONS.player().capitolRegion());
			if (d == 0)
				return 0;
			return -(d/128.0) * f.get().stockpile.creditScore()*f.get().res().get(null)*0.0125 * 2.0 * ROpinions.tradeCost(f.get());
			
		}

		@Override
		public void execute() {
			FACTIONS.DIP().trade(FACTIONS.player(), f.get(), true);
		}
	};
	public DealBool tradeCancel = new DealBool(¤¤tradeCancel, DicGeo.¤¤TradeD, UI.icons().s.urn.twin(UI.icons().s.cancel.createColored(COLOR.REDISH)), true) {
		
		@Override
		public boolean possible() {
			return FACTIONS.DIP().trades(FACTIONS.player(), f.get());
		}
		
		@Override
		public double value() {
			return -f.get().stockpile.credit()*0.1;
		}
		
		@Override
		public void execute() {
			FACTIONS.DIP().trade(FACTIONS.player(), f.get(), false);
		}
		
	};
	
	public DealBool aid = new DealBool(¤¤aid, ¤¤aidD, UI.icons().s.sword, false) {

		@Override
		public boolean possible() {
			if (FACTIONS.DIP().war.is(FACTIONS.player(), f.get()))
				return false;
			for (RDist d : WORLD.PATH().tmpRegs.all(f.get().capitolRegion(), WTREATY.NEIGHBOURS(null), WRegSel.CAPITOLS(f.get()))) {
				if (FACTIONS.DIP().war.is(d.reg.faction(), FACTIONS.player()))
					return true;
			}
			for (RDist d : WORLD.PATH().tmpRegs.all(FACTIONS.player().capitolRegion(), WTREATY.NEIGHBOURS(null), WRegSel.CAPITOLS(f.get()))) {
				if (FACTIONS.DIP().war.is(d.reg.faction(), f.get()))
					return true;
			}
			return false;
		}

		@Override
		public double value() {
			double get = DWar.joinWarValue(f.get(), FACTIONS.player())-DWar.joinWarValue(FACTIONS.player(), f.get());
			return get*DealValues.netValue(Deal.this, f.get(), f.get())* ROpinions.tradeCost(f.get());
		}

		@Override
		public void execute() {
			for (RDist d : WORLD.PATH().tmpRegs.all(f.get().capitolRegion(), WTREATY.NEIGHBOURS(null), WRegSel.CAPITOLS(f.get()))) {
				if (FACTIONS.DIP().war.is(d.reg.faction(), FACTIONS.player()))
					FACTIONS.DIP().war.set(f.get(), d.reg.faction(), true);
			}
			for (RDist d : WORLD.PATH().tmpRegs.all(FACTIONS.player().capitolRegion(), WTREATY.NEIGHBOURS(null), WRegSel.CAPITOLS(f.get()))) {
				if (FACTIONS.DIP().war.is(d.reg.faction(), f.get()))
					FACTIONS.DIP().war.set(f.get(), d.reg.faction(), true);
			}
			
		}
		
	};
	public DealBool peace = new DealBool(DicGeo.¤¤peace, DicGeo.¤¤peaceD, UI.icons().s.sword.twin(UI.icons().s.cancel.createColored(COLOR.REDISH)), false) {

		@Override
		public boolean possible() {
			return FACTIONS.DIP().war.is(FACTIONS.player(), f.get());
		}

		@Override
		public double value() {
			double p = DWar.peaceValue(f.get());
			
			if (p < 0) {
				return p*DealValues.netValue(Deal.this, FACTIONS.player(), f.get());
			}else
				return p*DealValues.netValue(Deal.this, f.get(), f.get());
		}

		@Override
		public void execute() {
			FACTIONS.DIP().war.set(f.get(), FACTIONS.player(), false);
		}
		
	};

	public Deal(){
		
	}
	
	public void setFactionAndClear(FactionNPC faction) {
		f.set(faction);
		for (DealBool b : bools)
			b.set(false);
		player.clear();
		npc.clear();
	}
	
	public boolean canBeAccepted() {
		return hasDeal() && (valueCredits() >=0 || can);
	}

	public double execute() {
		
		
		
		double v = opinionChangeD();
		for (DealBool b : bools) {
			if (b.is())
				b.execute();
		}
		
		player.execute(f.get());
		npc.execute(FACTIONS.player());
		
		ROpinions.makeDeal(f.get(), v);
		if (peace.is())
			ROpinions.makePeace(f.get());
		return v;
	}
	
	public abstract class DealBool extends BOOLEANImp {
		
		public final INFO info;
		public final SPRITE icon;
		public final boolean isOKAlone;
		
		DealBool(CharSequence name, CharSequence desc, SPRITE icon, boolean isOkAlone){
			info = new INFO(name, desc);
			this.icon = icon;
			bools.add(this);
			this.isOKAlone = isOkAlone;
		}
		
		public abstract boolean possible();
		
		public abstract double value();
		
		public abstract void execute();
	}
	
	public FactionNPC faction() {
		return f.get();
	}
	
	public LIST<DealBool> bools(){
		return bools;
	}
	
	int dupI = -1;
	private int cvalue;
	private boolean can = false;
	
	public double valueCredits() {
		
		if (dupI == VIEW.RI())
			return cvalue;
		
		can = false;
		dupI = VIEW.RI();
		
		
		double d = player.value(FACTIONS.player(), f.get());
		double n = npc.value(f.get(), f.get());
		
		double bb = 0;
		for (DealBool b : bools)
			if (b.is())
				bb += b.value();
		cvalue = (int) (bb+d-n);
		
		if (d == 0 && n == 0) {
			for (DealBool b : bools)
				if (b != tradeCancel && b.is())
					return cvalue;
			if (!tradeCancel.is())
				return cvalue;
			can = true;
		}else {
			can = cvalue >= 0;
		}
		
		
		return cvalue;
	}
	
	public boolean hasDeal() {
		
		if (player.hasValue() || npc.hasValue())
			return true;
		for (DealBool b : bools)
			if (b.is())
				return true;
		return false;
	}
	
	public double valueCreditsD() {
		
		return valueCredits()/f.get().stockpile.credit();
	}
	
//	public double valueGoodwillCredits() {
//		double v = ROpinions.goodWill(f.get());
//		if (v < 0) {
//			return 0;
//		}
//		double w = (FACTIONS.player().power()+1)/(f.get().power()+1);
//		w = CLAMP.d(w, 0, 1);
//		return v * w * f.get().stockpile.credit();
//	}
	
	public double opinionChange() {
		double c = opinionChangeD();
		return ROpinions.dealIncrease(f.get(), c);
	}
	
	public double opinionChangeD() {
		
		double c = valueCredits();
		
		double fc = f.get().stockpile.credit();
		double pp = (FACTIONS.player().power()+1);
		double fp = (f.get().power()+1);
		
		double playerWorth = fc*pp/fp;
		
		c /= playerWorth;
		return CLAMP.d(c, -1, 1);
	}
	
	public static Deal TMP() {
		if (tmp == null)
			tmp = new Deal();
		return tmp;
	}

	
}
