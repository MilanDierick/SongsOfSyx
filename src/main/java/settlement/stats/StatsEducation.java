package settlement.stats;

import java.io.IOException;

import init.boostable.*;
import init.race.RACES;
import init.race.Race;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.RoomEmploymentSimple;
import settlement.stats.STAT.STATData;
import settlement.stats.StatsBoosts.StatBoosterStat;
import snake2d.util.file.*;
import snake2d.util.rnd.RND;
import snake2d.util.sets.Bitmap1D;
import snake2d.util.sets.LIST;
import util.data.BOOLEAN_OBJECT.BOOLEAN_OBJECTE;
import util.data.INT_O;
import util.data.INT_O.INT_OE;
import util.info.INFO;

public class StatsEducation extends StatCollection {

	private final LIST<STAT> all;
	private final INT_OE<Induvidual> data;
	private final INT_OE<Induvidual> dataType;
	private final STATData dEDUCATION;
	private final STATData dINDOCTRINATION;
	public final double educateChildMax = 0.6;
	public final STAT EDUCATION;
	public final STAT INDOCTRINATION;
	private final Bitmap1D policyData = new Bitmap1D(RACES.all().size(), false);
	private final INFO policyInfo;

	StatsEducation(Init init) {
		super(init, "EDUCATION");

		data = init.count.new DataByte();
		dataType = init.count.new DataBit();

		dEDUCATION = new STAT.STATData("EDUCATION", init, make(0));
		EDUCATION = dEDUCATION;
		dINDOCTRINATION = new STAT.STATData("INDOCTRINATION", init, make(1));
		INDOCTRINATION = dINDOCTRINATION;

		init.savables.add(new SAVABLE() {

			@Override
			public void save(FilePutter file) {
				policyData.save(file);

			}

			@Override
			public void load(FileGetter file) throws IOException {
				policyData.load(file);

			}

			@Override
			public void clear() {
				policyData.clear();
			}
		});

		policyInfo = new INFO(init.jText.json("POLICY"));

		for (BOOSTABLERoom br : BOOSTABLES.ROOMS().rooms()) {
			if (br.room instanceof RoomBlueprintIns<?>) {
				RoomEmploymentSimple e = ((RoomBlueprintIns<?>)br.room).employment();
				if (e != null) {
					if (e.educationFactor > 0) {
						new StatBoosterStat(init, EDUCATION, new BBoost(br, e.educationFactor, false));
					}
					if (e.indoctorFactor > 0) {
						
						new StatBoosterStat(init, INDOCTRINATION, new BBoost(br, e.indoctorFactor, false));
					}
					
				}
			}
			
			
		}
		
		all = makeStats(init);

	}

	@Override
	public LIST<STAT> all() {
		return all;
	}

	public void educate(Induvidual i, double speed) {
		STAT target = EDUCATION;
		STAT other = INDOCTRINATION;
		if (policyIndoctor.is(i.race())) {
			target = INDOCTRINATION;
			speed *= 5;
			other = EDUCATION;
		}
		double am = EDUCATION.indu().max(i) * speed * BOOSTABLES.RATES().LEARNING_SKILL.get(i);
		int a = (int) am;
		if (RND.rFloat() < am - a)
			a++;

		if (other.indu().get(i) > 0) {
			other.indu().inc(i, -a * 4);
		} else {
			target.indu().inc(i, a);
		}
	}
	
	public INT_O<Induvidual> TOTAL(){
		return data;
	}

	public boolean canEducateChild(Induvidual i) {
		return data.getD(i) < educateChildMax;
	}
	
	public double total(Induvidual i) {
		return data.getD(i);
	}

	public BOOLEAN_OBJECTE<Race> policyIndoctor = new BOOLEAN_OBJECTE<Race>() {

		@Override
		public INFO info() {
			return policyInfo;
		}

		@Override
		public boolean is(Race f) {
			if (f == null) {
				for (Race r : RACES.all()) {
					if (policyData.get(r.index()))
						return true;
				}
				return false;
			}
			return policyData.get(f.index());
		}

		@Override
		public BOOLEAN_OBJECTE<Race> set(Race f, boolean b) {
			if (f == null) {
				policyData.setAll(b);
			} else
				policyData.set(f.index, b);
			return this;
		};

	};

	private INT_OE<Induvidual> make(int c) {
		return new INT_OE<Induvidual>() {

			@Override
			public int get(Induvidual t) {
				return dataType.get(t) == c ? data.get(t) : 0;
			}

			@Override
			public int min(Induvidual t) {
				return 0;
			}

			@Override
			public int max(Induvidual t) {
				return data.max(t);
			}

			@Override
			public void set(Induvidual t, int i) {
				if (get(t) == i)
					return;
				(c == 0 ? dINDOCTRINATION : dEDUCATION).indu().set(t, 0);
				dataType.set(t, c);
				data.set(t, i);
			}
		};
	}

}
