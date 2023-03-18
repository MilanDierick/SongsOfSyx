package world.map.terrain;

import static world.World.*;

import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.map.MAP_BOOLEANE;
import snake2d.util.rnd.*;

class GeneratorMountains{

	private final Polymap polly;
	private final MAP_BOOLEANE checker;
	private final VectorImp vec = new VectorImp();
	
	GeneratorMountains(HeightMap height){
		
		polly = new Polymap(TWIDTH(), THEIGHT(), (int) (60*(TWIDTH()/256.0)), 1.0);
		polly.checkInit();
		checker = polly.checker;
		
		int am = 3*TWIDTH()/256;
		
		for (int i = 0; i < am; i++) {
			makeMountain();
		}
		
		
		
		for (COORDINATE c : TBOUNDS()) {
			if (checker.is(c) || height.get(c) > 0.8) {
				MOUNTAIN().placeRaw(c.x(), c.y());
			}else {
				MOUNTAIN().clear(c.x(), c.y());
			}
		}
	}
	
	
	
	private void makeMountain() {
		
		int length = (int) (400 +RND.rFloat()*400);
		double sx = RND.rInt(TWIDTH());
		double sy = RND.rInt(THEIGHT());
		double oRot = RND.rFloat()*2;
		double rot = RND.rFloat();
		double rotD = RND.rFloat()*(1.0/(length/3));
		vec.setMagnitude(1.0);
		boolean making = RND.rBoolean();
		int lengthMax = (int) (100 + Math.pow(RND.rFloat(), 1.5)*120);
		double l = lengthMax;
		
		double bigTurnD = RND.rFloat()/500;
		double bigTurn = 0;
		
		for (int i = 0; i <= length; i++) {
			
			bigTurn += bigTurnD;
			vec.setAngle(oRot + Math.sin(rot*Math.PI*2)*0.2 + Math.sin(bigTurn*Math.PI*2));
			
			sx += vec.x();
			sy += vec.y();
			
			rot += rotD;
			
			l--;
			if (l <= 0) {
				lengthMax = 10 + RND.rInt(100);
				l = lengthMax;
				making = !making;
			}
			
			if (making) {
				double am = 0;
				double mid = lengthMax/2;
				if (l < mid) {
					am = l/mid; 
				}else if(l >= mid) {
					am = 1.0 - (l-mid)/mid; 
				}
				int rRad = 2 + (int) (am*8);
				if (rRad < 0)
					continue;
				int x = (int) sx;
				int y = (int) sy;
				
				if (IN_BOUNDS(x, y)) {
					MOUNTAIN().placeRaw(x, y);
					checker.set(x, y, true);
				}
				
				if (RND.oneIn(1)) {
					x = (int) sx + RND.rInt0((int) (2 + Math.pow(am, 1.5)*8));
					y = (int) sy + RND.rInt0((int) (2 + Math.pow(am, 1.5)*8));
					if (IN_BOUNDS(x, y)) {
						MOUNTAIN().placeRaw(x, y);
						checker.set(x, y, true);
					}
				}
			}else {
				
			}
			

			
			
			
			
			
		}
		
		
		
	}
	


}