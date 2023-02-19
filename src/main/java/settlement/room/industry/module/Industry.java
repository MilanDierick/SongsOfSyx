package settlement.room.industry.module;



import java.io.IOException;
import java.util.Arrays;

import game.GAME;
import game.time.TIME;
import init.boostable.BOOSTABLE;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.entity.humanoid.Humanoid;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.RoomInstance;
import settlement.room.main.furnisher.FurnisherStat;
import snake2d.util.file.*;
import snake2d.util.sets.*;
import util.data.DOUBLE_O.DOUBLE_OE;
import util.data.DataOL;
import util.data.INT_O.INT_OE;
import util.info.INFO;
import util.statistics.HISTORY_INT;
import util.statistics.HistoryInt;

public class Industry implements SAVABLE, IndustryRate {

	private final ArrayList<IndustryResource> ins;
	private final ArrayList<IndustryResource> outs;
	
	private final int dataL;
	private final BOOSTABLE bonus;
	private final ArrayList<RoomBoost> stats;
	
	public final RoomBlueprintImp blue;
	
	private int[] inMap = new int[RESOURCES.ALL().size()];
	private int[] outMap = new int[RESOURCES.ALL().size()];
	
	private final INT_OE<ROOM_PRODUCER> pday;
	
	public Industry(RoomBlueprintImp blue, 
			RESOURCE in, double inRate,
			RESOURCE out, double outRate,
			RoomBoost[] stats, 
			BOOSTABLE bonus) {
		this(
				blue, new RESOURCE[] {in},
				new double[] {inRate},
				new RESOURCE[] {out},
				new double[] {outRate},
				stats,
				bonus
						);
		
	}
	
	public Industry(RoomBlueprintImp blue, Json json, RoomBoost stats, BOOSTABLE bonus) {
		this(blue, json, new RoomBoost[] {stats}, bonus);
		
	}
	
	public Industry(RoomBlueprintImp blue, Json json, RoomBoost[] stats, BOOSTABLE bonus) {
		this(blue, res(json, "IN"), rates(json, "IN"), res(json, "OUT"), rates(json, "OUT"), stats, bonus);
	}
	
	
	private static RESOURCE[] res(Json json, String key) {
		json = json.json("INDUSTRY");
		if (!json.has(key))
			return new RESOURCE[0];
		json = json.json(key);
		LIST<String> keys = json.keys();
		RESOURCE[] rr = new RESOURCE[keys.size()];
		int i = 0;
		for (String k : keys)
			rr[i++] = RESOURCES.map().get(k, json);
		return rr;
	}
	
	private static double[] rates(Json json, String key) {
		json = json.json("INDUSTRY");
		if (!json.has(key))
			return new double[0];
		json = json.json(key);
		LIST<String> keys = json.keys();
		double[] rr = new double[keys.size()];
		int i = 0;
		for (String k : keys)
			rr[i++] = json.d(k, 0, 100000);
		return rr;
	}
	
	public Industry(RoomBlueprintImp blue, 
			RESOURCE[] ins, double[] inRates,
			RESOURCE[] outs, double[] outRates,
			RoomBoost[] stats, 
			BOOSTABLE bonus) {
		if (ins == null || (ins.length > 0 && ins[0] == null)) {
			ins = new RESOURCE[0];
			inRates = new double[0];
		}
		if (outs == null || (outs.length > 0 && outs[0] == null)) {
			outs = new RESOURCE[0];
			outRates = new double[0];
		}
		if (stats == null || (stats.length > 0 && stats[0] == null)) {
			stats = new FurnisherStat[0];
		}
		
		DataOL<ROOM_PRODUCER> data = new DataOL<ROOM_PRODUCER>() {

			@Override
			protected long[] data(ROOM_PRODUCER t) {
				return t.productionData();
			}
		
		};
		
		pday = data .new DataNibble();
		
		this.blue = blue;
		this.ins = new ArrayList<IndustryResource>(ins.length);
		for (int i = 0; i < ins.length; i++)
			this.ins.add(new IndustryResourceIn(data, i, ins[i], inRates[i]));
		this.outs = new ArrayList<IndustryResource>(outs.length);
		for (int i = 0; i < outs.length; i++)
			this.outs.add(new IndustryResourceOut(data, i, outs[i], outRates[i]));
		this.stats = new ArrayList<>(stats);
		this.bonus = bonus;
		dataL = data.longCount();
		Arrays.fill(inMap, -1);
		Arrays.fill(outMap, -1);
		
		int ii = 0;
		for (IndustryResource r : ins()) {
			inMap[r.resource.index()] = ii++;
		}
		ii = 0;
		for (IndustryResource r : outs()) {
			outMap[r.resource.index()] = ii++;
		}
	}
	
	public IndustryResource in(RESOURCE res) {
		return ins().get(inMap[res.index()]);
	}
	
	public IndustryResource out(RESOURCE res) {
		return outs().get(outMap[res.index()]);
	}

	public void updateRoom(ROOM_PRODUCER r) {
		
		if (pday.get(r) != (TIME.days().bitCurrent()&0x0F)) {
			pday.set(r,  (TIME.days().bitCurrent()&0x0F));
			boolean year = TIME.days().bitsSinceStart()%TIME.years().bitConversion(TIME.days()) == 0;
			for (IndustryResource i : ins) {
				int v = (int) i.day.getD(r);
				i.dayPrev.set(r, v);
				i.day.incD(r, -v);
				if (year) {
					i.yearPrev.set(r, i.year.get(r));
					i.year.set(r, 0);
				}	
			}
			for (IndustryResource i : outs) {
				int v = (int) i.day.getD(r);
				i.dayPrev.set(r, v);
				i.day.incD(r, -v);
				if (year) {
					i.yearPrev.set(r, i.year.get(r));
					i.year.set(r, 0);
				}	
			}
		}
	}

	public LIST<IndustryResource> outs(){
		return outs;
	}
	
	public LIST<IndustryResource> ins(){
		return ins;
	}
	
	@Override
	public BOOSTABLE bonus() {
		return bonus;
	}
	
	@Override
	public LIST<RoomBoost> boosts(){
		return stats;
	}

	@Override
	public void save(FilePutter file) {
		for (IndustryResource r : ins)
			r.save(file);
		for (IndustryResource r : outs)
			r.save(file);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		for (IndustryResource r : ins)
			r.load(file);
		for (IndustryResource r : outs)
			r.load(file);
	}

	@Override
	public void clear() {
		for (IndustryResource r : ins)
			r.clear();
		for (IndustryResource r : outs)
			r.clear();
	}

	public long[] makeData() {
		return new long[dataL];
	}
	
	public abstract class IndustryResource implements INDEXED{
		
		protected final HistoryInt history = new HistoryInt(12, TIME.years(), false);
		public final RESOURCE resource;
		public final double rate;
		public final double rateSeconds;
		private final int li;
		public final INT_OE<ROOM_PRODUCER> year; 
		public final INT_OE<ROOM_PRODUCER> yearPrev; 
		public final DOUBLE_OE<ROOM_PRODUCER> day; 
		public final INT_OE<ROOM_PRODUCER> dayPrev; 
		
		private IndustryResource(DataOL<ROOM_PRODUCER> data, int li, RESOURCE res, double rate) {
			this.resource = res;
			this.rate = rate;
			rateSeconds = Humanoid.WORK_PER_DAYI*rate/TIME.secondsPerDay;
			this.li = li;
			year = data.new DataInt(); 
			yearPrev = data.new DataInt(); 
			day = data.new DataFloat();
			dayPrev = data.new DataInt();
		}
		
		public HISTORY_INT history() {
			return history;
		}
		
		void save(FilePutter file) {
			history.save(file);
		}

		void load(FileGetter file) throws IOException {
			history.load(file);
		}

		void clear() {
			history.clear();
		}
		
		public abstract int inc(ROOM_PRODUCER r, double amount);
		
		public int work(Humanoid skill, ROOM_PRODUCER r, double workSeconds) {
			double e = getEffort(skill, r, workSeconds);
			int a = inc(r, e);
			return a;
		}
		
		public int incDay(ROOM_PRODUCER r) {
			return inc(r, rate);
		}
		
		protected abstract double getEffort(Humanoid skill, ROOM_PRODUCER r, double workSeconds);

		
		@Override
		public int index() {
			return li;
		}
		
	}
	
	public final class IndustryResourceIn extends IndustryResource{
		
		
		IndustryResourceIn(DataOL<ROOM_PRODUCER> data, int li, RESOURCE res, double rate) {
			super(data, li, res, rate);
		}
		
		@Override
		public int inc(ROOM_PRODUCER r, double amount) {
			int old = (int) day.getD(r);
			day.incD(r, amount);
			int now = (int) day.getD(r);
			int d = now-old;
			GAME.player().res().outConsumed.inc(resource, d);
			year.inc(r, d);
			history.inc(d);
			return d;
		}
		
		@Override
		protected double getEffort(Humanoid skill, ROOM_PRODUCER r, double workSeconds) {
			return IndustryUtil.calcConsumptionRate(rateSeconds*workSeconds, Industry.this, (RoomInstance)r);
		}

	}
	
	public final class IndustryResourceOut extends IndustryResource{
		
		
		IndustryResourceOut(DataOL<ROOM_PRODUCER> data, int li, RESOURCE res, double rate) {
			super(data, li, res, rate);
		}
		
		@Override
		public int inc(ROOM_PRODUCER r, double amount) {
			if (!Double.isFinite(amount)) {
				GAME.Warn(""+amount);
				return 0;
			}
			if (!Double.isFinite(day.getD(r))) {
				day.setD(r, 0);
			}
			
			int old = (int) day.getD(r);
			day.incD(r, amount);
			int now = (int) day.getD(r);
			int d = now-old;
			if (d != 0) {
				GAME.player().res().inProduced.inc(resource, d);
				year.inc(r, d);
				history.inc(d);
				GAME.stats().CRAFTED.inc(1);
			}
			return d;
		}
		
		@Override
		protected double getEffort(Humanoid skill, ROOM_PRODUCER r, double workSeconds) {
			return IndustryUtil.calcProductionRate(rateSeconds*workSeconds, skill, Industry.this, (RoomInstance)r);
		}

	}
	
	public interface RoomBoost {
		public INFO info();
		public double get(RoomInstance r);
		public default double min() {
			return 0;
		}
		public default double max() {
			return 1.0;
		}
	}



}
