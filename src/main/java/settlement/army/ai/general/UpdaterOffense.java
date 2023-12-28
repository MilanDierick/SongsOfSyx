package settlement.army.ai.general;

import java.io.IOException;

import game.time.TIME;
import init.config.Config;
import init.sprite.SPRITES;
import settlement.army.ai.general.MDivs.MDiv;
import snake2d.LOG;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.*;
import util.data.BOOLEAN;
import util.rendering.RenderData.RenderIterator;

class UpdaterOffense extends Updater {

	private final LIST<BOOLEAN> attack;
	private final LIST<BOOLEAN> bombard;
	private final Context context;
	
	private final ArrayList<MDiv> divsToDeploy = new ArrayList<>(Config.BATTLE.DIVISIONS_PER_ARMY);
	private int state;
	private long millis = 0;
	private double artilleryTime;
	private int wait;
	private boolean shouldRange = true;
	
	private boolean time = false;
	
	public UpdaterOffense(Context context, Updater.States s){
		this.context = context;
		attack = attack(s);
		bombard = bombard(s);
	}
	
	@Override
	public void clear() {
		state = 0;
		artilleryTime = TIME.currentSecond() + TIME.secondsPerDay;
		shouldRange = true;
		wait = 0;
		divsToDeploy.clearSloppy();
	}
	
	@Override
	public void save(FilePutter file) {
		file.i(state);
		file.d(artilleryTime);
		file.bool(shouldRange);
		file.i(wait);
		file.i(divsToDeploy.size());
		for (MDiv d : divsToDeploy)
			file.i(d.div.indexArmy());
	}

	@Override
	public void load(FileGetter file) throws IOException {
		state = file.i();
		artilleryTime = file.d();
		shouldRange = file.bool();
		wait = file.i();
		int am = file.i();
		divsToDeploy.clearSloppy();
		for (int i = 0; i < am; i++) {
			divsToDeploy.add(context.divs.allDivs.get(file.i()));
		}
		
	}
	
	@Override
	public void update() {
		
		if (TIME.currentSecond() > artilleryTime) {
			if (shouldRange) {
				clear();
				shouldRange = false;
			}
		}
		
		LIST<BOOLEAN> states = shouldRange ? this.bombard : this.attack;

		if (state >= states.size()) {
			state = 0;
			return;
		}
		
		if (wait -- > 0)
			return;
		
		
		if (time) {
			millis = System.currentTimeMillis();
			int s = state;
			if (!states.get(state).is())
				state++;
			millis = System.currentTimeMillis()-millis;
			LOG.ln(s + " " + millis);
		}else {
			if (!states.get(state).is())
				state++;
		}
		
		return;
		
	}
	

	
	@Override
	public void render(Renderer r, RenderIterator it) {
//		if (blockedMap.is(it.tile())) {
//			COLOR.ORANGE100.bind();
//			SPRITES.cons().BIG.outline.render(r, 0, it.x(), it.y());
//		}
		
//		if (centre.is(it.tile())) {
//			COLOR.RED100.bind();
//			SPRITES.cons().BIG.outline.render(r, 0, it.x(), it.y());
//		}
		
		if (context.blob.is(it.tile())) {
			COLOR.ORANGE100.bind();
			SPRITES.cons().BIG.outline.render(r, 0, it.x(), it.y());
		}
		context.preLines.render(r, it);
	}
	
	private LIST<BOOLEAN> attack(Updater.States s){
		ArrayListGrower<BOOLEAN> states = new ArrayListGrower<>();
		states.add(new BOOLEAN() {
			
			@Override
			public boolean is() {
				s.stepBombard.bombard();
				return false;
			}
		});
		states.add(new BOOLEAN() {
			
			@Override
			public boolean is() {
				context.divs.init();
				return false;
			}
		});
		states.add(new BOOLEAN() {
			
			@Override
			public boolean is() {
				context.blockedMap.clear();
				return false;
			}
		});
		states.add(new BOOLEAN() {

			@Override
			public boolean is() {
				context.blob.update(32);
				return false;
			}
		});	
		states.add(new BOOLEAN() {
			
			@Override
			public boolean is() {
				return context.preLines.update();
			}
		});
		states.add(new BOOLEAN() {
			
			@Override
			public boolean is() {
				divsToDeploy.clearSloppy();
				return false;
			}
		});
		states.add(new BOOLEAN() {
			
			@Override
			public boolean is() {
				if (divsToDeploy.size() > 0) {
					context.divDeployer.deploy(divsToDeploy, context.blockedMap);
					wait = 5;
					return true;
				}
				return s.stepMoveToLine.setDivsToLine(context.divs, context.preLines, divsToDeploy, context.blockedMap);
			}
		});
		states.add(new BOOLEAN() {
			
			@Override
			public boolean is() {
				if (divsToDeploy.size() > 0) {
					context.divDeployer.deploy(divsToDeploy, context.blockedMap);
					wait = 5;
					return true;
				}
				return s.stepMoveToLine.setDivsToLineRanged(context.divs, context.preLines, divsToDeploy, context.blockedMap);
			}
		});
		states.add(new BOOLEAN() {
			
			@Override
			public boolean is() {
				divsToDeploy.clearSloppy();
				divsToDeploy.add(context.divs.activeDivs);
				return false;
			}
		});
		states.add(new BOOLEAN() {

			@Override
			public boolean is() {
				return s.stepLineCharge.charge(context,  divsToDeploy, context.blob);
			}
		});
		states.add(new BOOLEAN() {
			
			@Override
			public boolean is() {
				s.attackEnemyNear.attackEnemies(context.divs, context.blob);
				return false;
			}
		});
		states.add(new BOOLEAN() {
			
			@Override
			public boolean is() {
				divsToDeploy.clearSloppy();
				return false;
			}
		});
		states.add(new BOOLEAN() {
			
			@Override
			public boolean is() {
				if (divsToDeploy.size() > 0) {
					context.divDeployer.deploy(divsToDeploy, context.blockedMap);
					return true;
				}
				return s.stepMoveToThrone.setToThrone(divsToDeploy, context.blockedMap, context.divs);
			}
		});

		states.add(new BOOLEAN() {
			
			@Override
			public boolean is() {
				s.stepRunner.run(context.divs.activeDivs);
				return false;
			}
		});
		
		return states;
	}
	
	private LIST<BOOLEAN> bombard(Updater.States s){
		ArrayListGrower<BOOLEAN> states = new ArrayListGrower<>();
		states.add(new BOOLEAN() {
			
			@Override
			public boolean is() {
				s.stepBombard.bombard();
				return false;
			}
		});
		states.add(new BOOLEAN() {
			
			@Override
			public boolean is() {
				context.divs.init();
				return false;
			}
		});
		states.add(new BOOLEAN() {
			
			@Override
			public boolean is() {
				context.blockedMap.clear();
				return false;
			}
		});
		states.add(new BOOLEAN() {

			@Override
			public boolean is() {
				context.blob.update(64);
				return false;
			}
		});	
		states.add(new BOOLEAN() {
			
			@Override
			public boolean is() {
				return context.preLines.update();
			}
		});
		states.add(new BOOLEAN() {
			
			@Override
			public boolean is() {
				divsToDeploy.clearSloppy();
				return false;
			}
		});
		states.add(new BOOLEAN() {
			
			@Override
			public boolean is() {
				if (divsToDeploy.size() > 0) {
					context.divDeployer.deploy(divsToDeploy, context.blockedMap);
					wait = 10;
					return true;
				}
				return s.stepMoveToLine.setDivsToLine(context.divs, context.preLines, divsToDeploy, context.blockedMap);
			}
		});
		states.add(new BOOLEAN() {
			
			@Override
			public boolean is() {
				if (divsToDeploy.size() > 0) {
					context.divDeployer.deploy(divsToDeploy, context.blockedMap);
					wait = 10;
					return true;
				}
				return s.stepMoveToLine.setDivsToLineRanged(context.divs, context.preLines, divsToDeploy, context.blockedMap);
			}
		});
		states.add(new BOOLEAN() {
			
			@Override
			public boolean is() {
				context.blob.update(48);
				return false;
			}
		});
		
		//move them to throne as well here
		
		states.add(new BOOLEAN() {

			@Override
			public boolean is() {
				for (MDiv d : context.divs.activeDivs) {
					if (context.blob.is(d.tx, d.ty)) {
						artilleryTime = 0;
						break;
					}
				}
				return false;
			}
		});
		

		states.add(new BOOLEAN() {
			
			@Override
			public boolean is() {
				
				double dist = 0;
				int am = 0;
				for (MDiv d : context.divs.activeDivs) {
					am++;
					if (d.isDeployed) {
						dist += d.distance;
						
					}
				}
				if (am > 0 && dist/am <= 16) {
					if (!context.shouldRange.shouldRange(context)) {
						clear();
						artilleryTime = -1;
					}
				}
				
				return false;
			}
		});
		
		return states;
	}



}
