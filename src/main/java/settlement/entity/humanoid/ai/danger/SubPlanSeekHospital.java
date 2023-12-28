package settlement.entity.humanoid.ai.danger;

import game.GAME;
import init.C;
import init.disease.DISEASE;
import init.sound.SOUND;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AIPLAN.PLANRES.Resumer;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.misc.util.FSERVICE;
import settlement.room.health.hospital.ROOM_HOSPITAL;
import settlement.stats.STATS;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;

class SubPlanSeekHospital {

	private final Resumer start;
	
	private final  ROOM_HOSPITAL b = SETT.ROOMS().HOSPITAL;
	
	AISubActivation init(Humanoid a, AIManager d) {
		return start.set(a, d);
	}
	
	public SubPlanSeekHospital(AIPLAN.PLANRES p) {
		
		Resumer rest = p.new Resumer(b.service().verb) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				d.planByte1 = (byte) (4 + RND.rInt(4));
				return AI.SUBS().LAY.activateTime(a, d, 15);
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				FSERVICE s = b.service().service(d.planTile.x(), d.planTile.y());
				if (s == null)
					return null;
				SOUND.sett().action.pain.rnd(a.body());
				
				d.planByte1 --;
				if (d.planByte1 > 0)
					return AI.SUBS().LAY.activateTime(a, d, 60);
				
				return fix(a, d);
				
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				FSERVICE s = b.service().service(d.planTile.x(), d.planTile.y());
				if (s != null && s.findableReservedIs())
					s.consume();
			}
		};
		
		Resumer lay = p.new Resumer(b.service().verb) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				d.planByte1 = (byte) (4 + RND.rInt(4));
				return AI.SUBS().LAY.activateTime(a, d, 25);
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				FSERVICE s = b.service().service(d.planTile.x(), d.planTile.y());
				if (s == null)
					return null;
				SOUND.sett().action.pain.rnd(a.body());
				double liveChance = b.recoverRate(d.planTile.x(), d.planTile.y());
				if (STATS.NEEDS().INJURIES.inDanger(a.indu())) {
					
					if (!STATS.NEEDS().INJURIES.willDie(a.indu(), liveChance)) {
						STATS.NEEDS().INJURIES.setNonDanger(a.indu());
					}
				}else {
					DISEASE di = STATS.NEEDS().disease.getter.get(a.indu());
					if (di == null)
						return fix(a, d);
					
					if (!STATS.NEEDS().disease.willDie(a.indu(), liveChance)) {
						STATS.NEEDS().disease.cure(a);
						return rest.set(a, d);
					}
					
				}
				
				return AI.SUBS().LAY.activateTime(a, d, 25);
				
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				FSERVICE s = b.service().service(d.planTile.x(), d.planTile.y());
				if (s != null && s.findableReservedIs())
					s.consume();
			}
		};
		
		start = p.new Resumer(b.service().verb) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				AISubActivation s = AI.SUBS().walkTo.service(a, d, b.service().finder, Integer.MAX_VALUE);
				if (s != null) {
					d.planTile.set(d.path.destX(), d.path.destY());
				}
				return s;
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				FSERVICE s = b.service().service(d.planTile.x(), d.planTile.y());
				if (s == null || !s.findableReservedIs())
					return null;
				int x = d.planTile.x()*C.TILE_SIZE + C.TILE_SIZEH;
				int y = d.planTile.y()*C.TILE_SIZE + C.TILE_SIZEH;
				DIR dir = SETT.ROOMS().HOSPITAL.layCoo(d.planTile.x(), d.planTile.y());
				x += dir.x()*(C.TILE_SIZEH-2);
				y += dir.y()*(C.TILE_SIZEH-2);
				a.physics.body().moveC(x, y);
				a.speed.setDirCurrent(dir);
				return lay.set(a, d);
				
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
	
			}
		};
		

		
	}
	
	private AISubActivation fix(Humanoid a, AIManager d) {
		FSERVICE s = b.service().service(d.planTile.x(), d.planTile.y());
		if (s != null && s.findableReservedIs()) {
			s.consume();
			for (DIR dir : DIR.ORTHO) {
				if (!SETT.PATH().solidity.is(a.tc(), dir)) {
					int x = (a.tc().x()+dir.x())*C.TILE_SIZE + C.TILE_SIZEH;
					int y = (a.tc().y()+dir.y())*C.TILE_SIZE + C.TILE_SIZEH;
					a.physics.body().moveC(x, y);
					GAME.count().CURED.inc(1);
					return null;
				}
			}
		}
		GAME.count().CURED.inc(1);
		return null;
	}
	
	



}
