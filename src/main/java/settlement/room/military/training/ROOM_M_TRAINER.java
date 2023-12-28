package settlement.room.military.training;

import java.io.IOException;

import game.GameDisposable;
import game.boosting.BOOSTING;
import game.boosting.BoostSpecs;
import init.D;
import init.race.RACES;
import settlement.entity.humanoid.Humanoid;
import settlement.path.finder.SFinderFindable;
import settlement.room.industry.module.IndustryUtil;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.RoomInstance;
import settlement.room.main.employment.RoomEmploymentSimple.EmployerSimple;
import settlement.room.main.util.RoomInitData;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle.StatTraining;
import snake2d.util.file.*;
import snake2d.util.sets.*;
import util.info.INFO;
import view.sett.ui.room.UIRoomModule;

public abstract class ROOM_M_TRAINER<T extends RoomInstance> extends RoomBlueprintIns<T>{

	private static final ArrayListGrower<ROOM_M_TRAINER<?>> all = new ArrayListGrower<>();
//	public static final double DEGRADE_RATE16 = 1.0/(4*16*16)*Config.BATTLE.TRAINING_DEGRADE;
	
	private static CharSequence ¤¤Speed = "¤Speed of:";
	
	
	static {
		new GameDisposable() {
			
			@Override
			protected void dispose() {
				all.clear();
			}
		};
		D.ts(ROOM_M_TRAINER.class);
	}
	
	int trainingLimit = 10000;
	public final int INDEX_TRAINING;
	public final int TRAINING_DAYS;
	public final double TRAINING_RATE;
	public final EmployerSimple emp = new EmployerSimple(employment());
	public BoostSpecs boosters;
	public final INFO tInfo;
	
	protected ROOM_M_TRAINER(int typeIndex, RoomInitData data, String key) throws IOException{
		super(typeIndex, data, key, data.m.CATS.MILITARY);
		tInfo = new INFO(data.text().json("TRAINING"));
		
		CharSequence name = info.name;
		CharSequence desc = ¤¤Speed + " " + info.name;
		
		Json d = data.data().json("TRAINING");
		
		pushBo(d, name, desc, type, true);
		BOOSTING.addToMaster("ROOM_TRAINING", bonus());
		
		
		TRAINING_DAYS = d.i("FULL_TRAINING_IN_DAYS");
		
		INDEX_TRAINING = all.add(this);
		TRAINING_RATE = 1.0/TRAINING_DAYS;
		boosters = new BoostSpecs(info.name, icon, false);
		boosters.push(d, null);
	}
	
	public StatTraining training() {
		return STATS.BATTLE().TRAINING_ALL.get(INDEX_TRAINING);
	}
	
	public void train(Humanoid a, RoomInstance room, double delta) {
		
		double b = delta*IndustryUtil.roomBonus(room, null);
		b *= bonus().get(a.indu());
		
		if (!STATS.BATTLE().basicTraining.isMax(a.indu()))
			STATS.BATTLE().basicTraining.incFraction(a.indu(), delta*STATS.BATTLE().basicTraining.max(a.indu())*0.25);
		else {
			
			
			b *= TRAINING_RATE;
			training().inc(a.indu(), b);
		}
	}
	
	public int trainingDays() {
		return (int) Math.ceil(TRAINING_DAYS/bonus().get(RACES.clP(null, null)));
	}

	@Override
	protected void saveP(FilePutter f) {
		
		f.i(trainingLimit);
	}

	@Override
	protected void loadP(FileGetter f) throws IOException {
		
		trainingLimit = f.i();
		
	}

	@Override
	protected void clearP() {
		
		trainingLimit = 10000;
	}

	public int limit() {
		return trainingLimit;
	}
	
	@Override
	public SFinderFindable service(int tx, int ty) {
		return null;
	}

	@Override
	protected void update(float ds) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void appendView(LISTE<UIRoomModule> mm) {
		mm.add(new Gui(this));
	}
	
	public static LIST<ROOM_M_TRAINER<?>> ALL(){
		return all;
	}
	
	public int employable() {
		int e = emp.employable();
		int l = trainingLimit-employment().employed();
		return Math.min(e, l);
	}
	
	
}
