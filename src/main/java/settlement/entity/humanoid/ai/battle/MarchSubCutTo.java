package settlement.entity.humanoid.ai.battle;

import init.C;
import settlement.army.Div;
import settlement.army.DivMorale;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import snake2d.util.datatypes.*;
import snake2d.util.misc.CLAMP;

final class MarchSubCutTo extends AISUB.Simple {

	private final AISUB inter = new AISUB.Simple() {

		@Override
		public AISubActivation activate(Humanoid a, AIManager d) {
			return super.activate(a, d, AI.STATES().STOP.activate(a, d, 0));
		}

		@Override
		protected AISTATE resume(Humanoid a, AIManager d) {
			return null;
		}
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			if (e.event == HEvent.COLLISION_TILE) {
				if (AI.modules().battle.tile.shouldattackTile(d, a, e.tx, e.ty)) {
					d.interrupt(a, e);
					d.overwrite(a, AI.modules().battle.tile.init(d, a, e.tx, e.ty));
				}else {
					d.interrupt(a, e);
					d.overwrite(a, inter.activate(a, d));
				}
				
				return false;
			}
			
			return InterBattle.listener.event(a, d, e);
		}
		
	};

	private static final VectorImp vec = new VectorImp();
	private final int distFar = (int) (C.TILE_SIZE*C.TILE_SIZE);
	private final int distClose = (int) (0.15*0.15*C.TILE_SIZE*C.TILE_SIZE);
	
	@Override
	protected AISTATE resume(Humanoid a, AIManager d) {
		d.subByte++;
		Div div = a.division();
		if (div == null) {
			if (d.subByte <= 1)
				return AI.STATES().STAND.activate(a, d, 0.05);
			return null;
		}
		COORDINATE dest = div.reporter.getPixel(a.divSpot());
		if (dest == null) {
			if (d.subByte == 1)
				return AI.STATES().STAND.activate(a, d, 0.05);
			return null;
		}
		if (Util.isInPosition(dest, a, d)) {
			a.speed.magnitudeInit(0);
			if (d.subByte == 1)
				return AI.STATES().STAND.activate(a, d, 0.05);
			return null;
		}
		dest = div.reporter.getPixel(a.divSpot());
		
		double speed = 0.4; 
		if (div.settings.charging)
			speed = 0.9;
		else if (div.settings.running)
			speed = 0.7;

		if (DivMorale.PROJECTILES.getD(div) > (div.menNrOf()>>1) || div.settings.isFighting())
			speed *= 0.75;
		int distX = dest.x()-a.physics.body().cX();
		int distY = dest.y()-a.physics.body().cY();
		double dist = distX*distX + distY*distY;
		if (dist > distFar)
			speed = CLAMP.d(speed + (double)(dist-distFar)/distFar, 0.2, 0.9);
		else if (dist < distClose) {
			speed = CLAMP.d(speed * (dist)/distClose, 0.1, speed);
		}else {
			
		}
		AISTATE s = AI.STATES().MOVE_TO.move(a, d, dest.x(), dest.y(), 0.05, speed*a.speed.magintudeMax());
		DIR dir = div.position().dir(a.divSpot());
		if (dir != null) {
			if (div.settings.threatAt(dir))
				a.speed.setDirCurrent(dir);
			else if(div.settings.isFighting()) {
				a.speed.setDirCurrent(div.position().dir());
			}
		}
		
		return s;
	}
	
	@Override
	public boolean event(Humanoid a, AIManager d, HEventData e) {
		if (e.event == HEvent.COLLISION_TILE) {
			
			if (AI.modules().battle.tile.shouldattackTile(d, a, e.tx, e.ty)) {
				
				d.overwrite(a, AI.modules().battle.tile.init(d, a, e.tx, e.ty));

			}else {
				d.interrupt(a, e);
				d.overwrite(a, inter.activate(a, d));
			}
			return false;
		}
		if (e.event == HEvent.MEET_ENEMY) {
			
			if (a.speed.magnitudeRelative() > 0.4) {
				
				Div div = a.division();
				if (div != null) {
					COORDINATE dest = div.reporter.getPixel(a.divSpot());
					if (dest != null) {
						double m = vec.set(a.physics.body(), dest.x(), dest.y());
						if (m > 0 && vec.nX()*a.speed.nX() + vec.nY()*a.speed.nY() > 0.6) {
							d.overwrite(a, AI.STATES().MOVE_TO.move(a, d, dest.x(), dest.y(), 0.05, 0.7*a.speed.magintudeMax()));
							return false;
						}
					}
				}
			}
			
			
		}
		return InterBattle.listener.event(a, d, e);
	}
	
}
