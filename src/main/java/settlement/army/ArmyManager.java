package settlement.army;


import java.io.IOException;

import game.Profiler;
import init.C;
import init.config.Config;
import settlement.army.formation.DivDeployerUser;
import settlement.main.CapitolArea;
import settlement.main.SETT;
import settlement.main.SETT.SettResource;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.updating.IUpdater;
import view.main.VIEW;

public class ArmyManager extends SettResource{

	public static final int ARMIES = 2;
	public static final int ARMIES_BITS = 0b011;
	public static final int DIVISIONS = ARMIES*Config.BATTLE.DIVISIONS_PER_ARMY;
	private final ArrayList<Div> divisions = new ArrayList<>(DIVISIONS);
	private final ArrayList<Army> armies = new ArrayList<>(ARMIES);
	public final DivDeployerUser placer;
	public final ArmyTrainingInfo info;
	public final ArmyDivOrder playerDivs;
	public final DivisionBanners banners = new DivisionBanners();
	public final TargetMap map = new TargetMap();
	
	public ArmyManager(SETT s) throws IOException{
		
		for (int i = 0; i < ARMIES; i++) {
			
			new Army(armies, divisions, Config.BATTLE.MEN_PER_DIVISION);
		}
		
		placer = new DivDeployerUser(armies, Config.BATTLE.MEN_PER_DIVISION) {
			@Override
			protected boolean blocked(int x, int y) {
				if (VIEW.b().state() != null && VIEW.b().state().deploying()) {
					return !VIEW.b().state().deploymentBounds().holdsPoint(x>>C.T_SCROLL, y>>C.T_SCROLL);
				}
				return false;
			}
		};
		info = new ArmyTrainingInfo();
		playerDivs = new ArmyDivOrder(player());

	}
	
	public ArmyTrainingInfo info() {
		return info;
	}
	
	@Override
	protected void clearBeforeGeneration(CapitolArea area) {
		
		for (Army t : armies)
			t.saver.clear();
		info.saver.clear();
		playerDivs.clear();
		banners.clear();
		
	}
	
	@Override
	protected void save(FilePutter file) {
		
		for (Army t : armies)
			t.saver.save(file);
		info.saver.save(file);
		playerDivs.save(file);
		banners.save(file);
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		for (Army t : armies)
			t.saver.load(file);
		info.saver.load(file);
		playerDivs.load(file);
		banners.load(file);
	}
	
	
	@Override
	protected void generate(CapitolArea area) {
		for (int i = 0; i < 4; i++) {
			SETT.ARMIES().divisions.get(i).info.men.set(50);
		}
	}
	
	private double ti = 0;
	@Override
	protected void update(float ds, Profiler profiler) {

		profiler.logStart(Div.class);
		uper.update(ds);
		profiler.logEnd(Div.class);
		
		for (Army a : armies())
			a.update(ds);
		ti += ds;
		if (ti > 0.1) {
			ti-= 0.1;
			SETT.ARMIES().info.update();
		}
	}
	
	private final IUpdater uper = new IUpdater(DIVISIONS, 0.1) {
		
		@Override
		protected void update(int i, double timeSinceLast) {
			divisions.get(i).update(timeSinceLast);
		}
	};
	
	public Army player() {
		return armies.get(0);
	}
	
	public Army enemy() {
		return armies.get(1);
	}

	public Div division(short armyDivisionID) {
		return divisions.get(armyDivisionID);
	}
	
	public LIST<Div> divisions(){
		return divisions;
	}
	
	public LIST<Army> armies(){
		return armies;
	}
	
	
}
