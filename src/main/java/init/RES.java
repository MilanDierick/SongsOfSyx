package init;

import java.io.IOException;

import game.GAME;
import game.GameSaver;
import game.boosting.BOOSTING;
import init.biomes.*;
import init.disease.DISEASES;
import init.need.NEEDS;
import init.race.RACES;
import init.religion.Religions;
import init.resources.RESOURCES;
import init.sound.SOUND;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.main.SETT;
import settlement.room.main.Room;
import snake2d.*;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.misc.Debugger;
import snake2d.util.misc.Debugger.Formatter;
import snake2d.util.sets.ArrayCooShort;

public class RES {

	private static Data data;
	
	public final class Data {
		
		final SPRITES sprites;
		final SOUND sound;
		final Debugger debugger;
		final RLoadPrinter loader;
		final GameSaver saver;
		final CircleCooIterator circleIterator;
		final AreaTmp areaTmp = new AreaTmp();
		final ArrayCooShort coos = new ArrayCooShort(Room.MAX_SIZE+1);
		//final PathThreadManager pathManager;
		final PathUtilOnline pathOnline;
		final RANMAP ran1 = new RANMAP();
		final RANMAP ran2 = new RANMAP();
		

		private final SlaveThread general1 = new SlaveThread("Battle-General", 1.0/60);
		private final SlaveThread general2 = new SlaveThread("Battle-Divs", 1.0/60);
		private final SlaveThread general3 = new SlaveThread("Battle-util", 1.0/60);
		private final SlaveThread general4 = new SlaveThread("Battle-traject", 1.0/60);
		TextureHolder texture;
		
		private Data() throws IOException{
			RES.data = this;

			CORE.checkIn();
			UI.init();
			CORE.checkIn();
			sprites = new SPRITES(this);
			CORE.checkIn();
			BOOSTING.init(null);

			CLIMATES.init();
			TERRAINS.init();
			new NEEDS();
			new RACES();
			BUILDING_PREFS.init();
			new DISEASES();
			RESOURCES.init();
			Religions.init();
			CORE.checkIn();

			debugger = new Debugger(UI.FONT().M);
			sound = new SOUND(this);
			
			loader = new RLoadPrinter();
			//pathManager = new PathThreadManager(1, C.SETTLE_TSIZE, C.SETTLE_TSIZE);
			

			
			
			saver = new GameSaver();
			
			pathOnline = new PathUtilOnline(SETT.TWIDTH);
			circleIterator = new CircleCooIterator(120, pathOnline.getFlooder());
			debugger.add(debugger.new Value(general1.name, 0, Formatter.PERCENTAGE) {
				
				@Override
				protected double getValue() {
					return general1.getUtilization();
				}
			});
			debugger.add(debugger.new Value(general2.name, 0, Formatter.PERCENTAGE) {
				
				@Override
				protected double getValue() {
					return general2.getUtilization();
				}
			});
			debugger.add(debugger.new Value(general3.name, 0, Formatter.PERCENTAGE) {
				
				@Override
				protected double getValue() {
					return general3.getUtilization();
				}
			});
			debugger.add(debugger.new Value(general4.name, 0, Formatter.PERCENTAGE) {
				
				@Override
				protected double getValue() {
					return general4.getUtilization();
				}
			});
			debugger.add(debugger.new Value("Ents", 0, Formatter.Amount) {
				
				@Override
				protected double getValue() {
					if (SETT.ENTITIES() == null)
						return 0;
					return SETT.ENTITIES().size();
				}
			});
			debugger.add(debugger.new Value("Speed", 0, Formatter.Amount) {
				
				double t;
				double am;
				
				@Override
				protected double getValue() {
					t += GAME.SPEED.speed();
					am ++;
					if (am > 30) {
						t = t/am;
						am = 1;
					}
					return t / am;
				}
			});
			
//			debugger.add(debugger.new Value("pathLoad", 0, Formatter.PERCENTAGE){
//				protected double getValue(){
//					return pathManager.getLoadPercent();
//				}
//			});
			
			new DebugReloader();
		}
		
	}
	
	public RES() throws IOException{
		new Data();
		
	}
	
	public void dispose() {
		data = null;
	}
	
	
	public static SOUND sound(){
		return data.sound;
	}
	
	public static Debugger debugger(){
		return data.debugger;
	}
	
	public static RLoadPrinter loader(){
		return data.loader;
	}
	
	public static GameSaver saver(){
		return data.saver;
	}
	
//	public static PathThreadManager pathFinder(){
//		return data.pathManager;
//	}
	
	public static Flooder flooder(){
		return data.pathOnline.getFlooder();
	}
	
	public static PathUtilOnline.Marker marker(){
		return data.pathOnline.marker;
	}
	
	public static PathUtilOnline.Filler filler(){
		return data.pathOnline.filler;
	}
	
	public static PathUtilOnline.AStar astar(){
		return data.pathOnline.astar;
	}
	
	public static PathUtilOnline pathTools(){
		return data.pathOnline;
	}
	
	public static CircleCooIterator circle() {
		return data.circleIterator;
	}
	
	public static SlaveThread generalThread1() {
		return data.general1;
	}
	
	public static SlaveThread generalThread2() {
		return data.general2;
	}
	
	public static SlaveThread generalThread3() {
		return data.general3;
	}
	
	public static SlaveThread generalThread4() {
		return data.general4;
	}
	
	public static ArrayCooShort coos() {
		return data.coos;
	}
	
	public static RANMAP ran1() {
		return data.ran1;
	}
	
	public static RANMAP ran2() {
		return data.ran2;
	}
	
	public static AreaTmp AREA() {
		return data.areaTmp;
	}


}
