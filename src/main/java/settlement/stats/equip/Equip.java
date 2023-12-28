package settlement.stats.equip;

import game.boosting.BoostSpecs;
import game.faction.FACTIONS;
import game.faction.FResources.RTYPE;
import game.time.TIME;
import init.paths.PATH;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.army.Div;
import settlement.entity.humanoid.HCLASS;
import settlement.entity.humanoid.Humanoid;
import settlement.stats.Induvidual;
import settlement.stats.StatsInit;
import settlement.stats.standing.StatStanding;
import settlement.stats.standing.StatStanding.StandingDef;
import settlement.stats.stat.*;
import settlement.stats.util.StatBooster.StatBoosterStat;
import snake2d.LOG;
import snake2d.util.file.Json;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.INDEXED;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.text.Str;
import util.data.INT_O.INT_OE;
import util.dic.DicMisc;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import util.info.INFO;

public abstract class Equip implements INDEXED, WearableResource{
	
	public final CharSequence sTarget;
	public final INFO targetInfo;
	
	public final RESOURCE resource;
	private final int index;
	public final double wearPerYear;
	private final double wearRateI;
	public final int equipMax;
	public final int arrivalAmount;
	public final int targetDefault;
	public final String eKey;
	protected final STAT stat;
	private final INT_OE<Induvidual> counter;
	
	Equip(String coll, String key, PATH path, LISTE<Equip> all, StatsInit init) {
		Json data = new Json(path.get(key));
		key = (coll + "_" + key).replace("__", "_");
		eKey = key;
		index = all.add(this);
		resource = RESOURCES.map().get(data);
		wearPerYear = data.d("WEAR_RATE", 0, 100);
		equipMax = data.i("MAX_AMOUNT", 1, 15);
		arrivalAmount = data.i("ARRIVAL_AMOUNT", 0, equipMax);
		targetDefault = data.i("DEFAULT_TARGET");
		StandingDef standing = new StandingDef(data);
		
		wearRateI = wearPerYear /16.0;
		sTarget = new Str(StatsEquip.¤¤Target).insert(0, resource.name).trim();
		{
			
			targetInfo = new INFO(new Str(StatsEquip.¤¤Level).insert(0, resource.name).trim(), StatsEquip.¤¤Level_desc);
		}
		
		stat = new STATData(key, init, init.count.new DataNibble(equipMax),  new StatInfo(resource.name, resource.names, resource.desc));
		
		stat.standing = new StatStanding(stat, 0, standing);
		stat.info().setInt();
		counter = init.count.new DataByte();
		
		push(stat.boosters, data);
		
	}
	
	protected void push(BoostSpecs boosters, Json data) {
		boosters.push(data, new StatBoosterStat(stat, true));
	}

	@Override
	public void set(Induvidual t, int i) {
		int old = stat.indu().get(t);
		if (i != old) {
			stat.indu().set(t, CLAMP.i(i, 0, max(t)));
			if (t.player() && t.added()) {
				FACTIONS.player().res().inc(resource, RTYPE.EQUIPPED, old-stat.indu().get(t));
			}
		}
	}
	
	@Override
	public void inc(Induvidual t, int am) {
		set(t, stat().indu().get(t) + am);
	}
	
	@Override
	public int get(Induvidual i) {
		return stat.indu().get(i);
	}
	
	@Override
	public int index() {
		return index;
	}

	void update16(Humanoid h, int updateI, int updateR, boolean day) {
		if (RND.rFloat() < wearRateI) {
			Induvidual i = h.indu();
			int am = stat.indu().get(i)-(counter.get(i)>>4);
			if (am > 0)
				counter.inc(i, am);
		}
		
	}
	
	@Override
	public int needed(Induvidual i) {
		int am = target(i)-get(i) + (counter.get(i)>>4);
		if (am < 0) {
			wearOut(i);
			am = target(i)-get(i);
			if (am < 0) {
				int c = counter.get(i)&0x0F;
				if (RND.rInt(16) < c)
					stat.indu().inc(i, -1);
				counter.set(i, 0);
				return target(i)-get(i);
			}
		}
		return am;
	}
	
	@Override
	public void wearOut(Induvidual i) {
		
		int c = counter.get(i);
		int am = c >> 4;
		c &= 0x0F;
		counter.set(i, c);
		if (am == 0)
			return;
		
		am = CLAMP.i(am, 0, stat.indu().get(i));
		stat.indu().inc(i, -am);
	}

	public STAT stat() {
		return stat;
	}
	
	protected abstract double value(Induvidual v);
	
	protected abstract double value(HCLASS c, Race r);
	
	protected abstract double value(Div v);



	public double wearRate() {
		return wearPerYear;
	}

	@Override
	public RESOURCE resource(Induvidual i) {
		return resource;
	}

	public RESOURCE resource() {
		return resource;
	}
	
//	public int arrivalAmount() {
//		return arrivalAmount;
//	}


	@Override
	public double wearPerYear(Induvidual i) {
		return wearPerYear;
	}
	
	protected void hoverP(GUI_BOX box) {
		GBox b = (GBox) box;
		box.title(resource.name);
		box.text(resource.desc);
		b.NL();
		b.textL(StatsEquip.¤¤Wear);
		b.tab(8);
		b.add(GFORMAT.f0(b.text(), -wearPerYear*16/TIME.years().bitConversion(TIME.days())));
		b.NL();
	}
	

	public void hover(GUI_BOX box) {
		hoverP(box);
		GBox b = (GBox) box;
		b.sep();
		stat.boosters.hover(b, 1.0, -1);
	}
	
	public void hover(GUI_BOX box, Div div) {
		hoverP(box);
		GBox b = (GBox) box;
		b.textLL(DicMisc.¤¤Equipped);
		b.tab(7);
		b.add(GFORMAT.fofkInv(b.text(), stat.div().getD(div)*equipMax, equipMax));
		
		b.sep();
		
		stat.boosters.hover(b, div);
	}

	public void hover(GUI_BOX box, HCLASS cl, Race r) {
		hoverP(box);
		GBox b = (GBox) box;
		b.textLL(DicMisc.¤¤Equipped);
		b.tab(7);
		b.add(GFORMAT.fofkInv(b.text(), stat.data(cl).getD(r)*equipMax, equipMax));
		
		b.sep();
		
		stat.boosters.hover(b, RACES.clP(r, cl));
	}
	
	public void hover(GUI_BOX box, Induvidual h) {
		hoverP(box);
		GBox b = (GBox) box;
		
		b.textLL(DicMisc.¤¤Equipped);
		b.tab(7);
		b.add(GFORMAT.iofkInv(b.text(), stat.indu().get(h), equipMax));
		
		b.sep();
		stat.boosters.hover(b, h);
		
	}
	

	public String eKey() {
		return eKey;
	}
	
	public static void main(String[] args) {
		
		double rate = 0.25;
		int ticksPerDay = 16;
		int daysPerYear = 16;
		int counterAm = 16;
		double chancePerTick = counterAm*rate/(ticksPerDay*daysPerYear);
		
		int start = 0;
		
		int iters = 10;
		
		for (int i = 0; i < ticksPerDay*16*iters; i++) {
			
			if (RND.rFloat() <= chancePerTick) {
				start++;
			}
			
		}
		
		LOG.ln((double)start/(iters*counterAm));
		
		
		
	}

}