package settlement.entity.humanoid.spirte;

import static settlement.entity.humanoid.spirte.HSpriteConst.*;

import game.GAME;
import game.time.TIME;
import init.C;
import init.race.appearence.RAddon;
import init.race.appearence.RExtras;
import init.resources.RESOURCE;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.colls.StatsAppearance;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.sprite.TILE_SHEET;
import util.rendering.ShadowBatch;

public abstract class HSprite {

	public void render(Induvidual in2, double spriteTimer, double movementTimer, boolean inWater, DIR dirr, RESOURCE carried, int resAm, Renderer r, ShadowBatch s, float ds, int x, int y) {
		
	}
	public void render(Humanoid a, AIManager d, Renderer r, ShadowBatch s, float ds, int x, int y) {
		
	}
	public final double time;
	protected boolean isDone(Humanoid a) {
		return true;
	}
	public abstract void tick(Humanoid a, float ds);
	public final double height;
	
	HSprite(double time) {
		this(time, 1);
	}
	
	HSprite(double time, double height) {
		this.time = time;
		this.height = height;
	}
	
	static class Standing extends HSprite {
		
		private boolean carries;
		private final int[] torsos;
		private final static int[] feets = new int[] { IFEET_NONE, IFEET_RIGHT, IFEET_RIGHT2, IFEET_RIGHT, IFEET_NONE, IFEET_LEFT, IFEET_LEFT2, IFEET_LEFT };
		private final static int[] swim = new int[] {
			ITORSO_STILL, ITORSO_LEFT, ITORSO_LEFT2, ITORSO_LEFT3, ITORSO_LEFT2, ITORSO_LEFT,ITORSO_STILL,ITORSO_RIGHT,ITORSO_RIGHT2,ITORSO_RIGHT3,ITORSO_RIGHT2,ITORSO_RIGHT};
		
		final double fps;
		
		Standing(double fps, boolean carries, int... torsos) {
			super(fps > 0 ? torsos.length/fps : 0);
			this.fps = fps;
			this.carries = carries;
			if (torsos.length == 0)
				throw new RuntimeException();
			this.torsos = torsos;
		}

		protected int getDir(Humanoid a, float timer) {
			return a.speed.dir().id();
		}
		
		protected int getFeet(Induvidual a, double movementTimer, int dir, float ds) {
			double s =  movementTimer;
			if (s <= 0)
				return IFEET_NONE + dir;
			int rt = (int) movementTimer;
			rt %= feets.length;
			if (rt < 0)
				rt = 0;
			return feets[(int)rt] + dir;
		}
		
		@Override
		public void tick(Humanoid a, float ds) {
			a.spriteTimer += ds*fps;
		}
		
		@Override
		protected boolean isDone(Humanoid a) {
			return a.spriteTimer >= torsos.length;
		}
		
		protected void renderExtra(Induvidual a, DIR dir, Renderer r, ShadowBatch s, double timer, int x, int y) {
			
		}

		@Override
		public void render(Induvidual in2, double spriteTimer, double movementTimer, boolean inWater, DIR dirr, RESOURCE carried, int resAm, Renderer r, ShadowBatch s, float ds, int x, int y) {
			
			StatsAppearance ap = STATS.APPEARANCE();
			
			spriteTimer = Math.abs(spriteTimer) % torsos.length;
			
			if (inWater) {
				renderWater(in2, dirr, r, s, ds, x, y);
				return;
			}
			
			TILE_SHEET sp = in2.race().appearance().sheet(in2).sheet.sheet;
			RExtras ex = in2.race().appearance().extra;
			int dir = dirr.id();
			
			//FEET
			ap.colorLegs(in2).bind();
			int feet = getFeet(in2, movementTimer, dir, ds);
			if (feet >= 0) {
				sp.render(r, feet, x, y);
			}
			COLOR.unbind();

			boolean ca = (carries && carried != null);
			int torso = ca ? ITORSO_CARRY : torsos[(int)spriteTimer];
			torso += dir;
			
			//EXTRA
			renderExtra(in2, dirr, r, s, spriteTimer, x, y);
			
			//TORSO
			OPACITY.unbind();
			ap.colorSkin(in2).bind();
			sp.render(r, torso, x, y);

			if (STATS.POP().NAKED.get(in2) == 0) {
				ap.colorClothes(in2).bind();
				sp.render(r, ITUNIC+dir, x, y);
				
			}
			COLOR.unbind();
			
			
			//CARRY RESOURCE
			if (ca) {
				RESOURCE w = carried;
				if (resAm > 4){
					int cx = x + sp.size() / 2;
					int cy = y + sp.size() / 2;
					
					int dx = cx + (int) (sp.size()*0.5*dirr.xN());
					int dy = cy + (int) (sp.size()*0.5*dirr.yN());
					int ti = GAME.intervals().get05() & 0b011;
					ex.trolly.render(r, TROLLY[ti] + dir, dx-ex.trolly.size()/2, dy-ex.trolly.size()/2);
					s.setDistance2Ground(8).setHeight(0);
					ex.trolly.render(s, TROLLY[ti] + dir, dx-ex.trolly.size()/2, dy-ex.trolly.size()/2);
					int am = resAm;
					if (am >= 8)
						am = 7;
					w.renderLaying(r, dx-C.TILE_SIZEH, dy -C.TILE_SIZEH, 0b1111, am);
				}else
					w.renderCarried(r, x + in2.race().appearance().off + in2.race().physics.hitBoxsize() / 2,
						y + in2.race().appearance().off + in2.race().physics.hitBoxsize() / 2, dirr);
			}

			for (RAddon add : in2.race().appearance().types.get(ap.gender.get(in2)).addonsBelow) {
				add.renderStanding(r, dir, x, y, in2, false);
			}

			//HEAD
			ap.colorSkin(in2).bind();
			sp.render(r, dir + IHEAD, x, y);

			
			for (RAddon add : in2.race().appearance().types.get(ap.gender.get(in2)).addonsAbove) {
				add.renderStanding(r, dir, x, y, in2, false);
			}
			
			COLOR.unbind();

			//GRIT  - WOUNDS
			OPACITY.O75.bind();
			filth(in2, torso, x, y);
			blood(in2, torso, x, y);

			OPACITY.unbind();
			
			//SHADOWS
			s.setHeight(10).setDistance2Ground(0);
			in2.race().appearance().sheet(in2).sheet.sheet.render(s, dir + ISHADOW, x, y);
			
		}
		
		@Override
		public void render(Humanoid a, AIManager d, Renderer r, ShadowBatch s, float ds, int x, int y) {
			
			double sp =  a.speed.magnitudeRelative();
			if (sp > 0) {
				a.relTimer += ds * 14* sp;
			}else
				a.relTimer = 0;
			
			Induvidual in2 = a.indu();
			double spriteTimer = a.spriteTimer;
			double movementTimer = a.relTimer;
			boolean inWater = a.inWater && a.physics.getZ() == 0;
			DIR dirr = DIR.ALL.get(getDir(a, a.spriteTimer));
			RESOURCE carried = d.resourceCarried();
			int resAm = d.resourceA();
			
			render(in2, spriteTimer, movementTimer, inWater, dirr, carried, resAm, r, s, ds, x, y);
			
		}
		
		private void renderWater(Induvidual in2, DIR dirr, Renderer r, ShadowBatch s, float ds, int x, int y) {
			StatsAppearance ap = STATS.APPEARANCE();
			
			int tt = (int)(TIME.currentSecond()*5+STATS.RAN().get(in2, 6));
			tt &= 0x0FF;
			tt = tt%swim.length;
			
			
			TILE_SHEET sp = in2.race().appearance().sheet(in2).sheet.sheet;
			int dir = dirr.id();
			
			int torso = swim[tt];
			torso += dir;
			
			//TORSO
			ap.colorSkin(in2).bind();
			sp.render(r, torso, x, y);

			if (STATS.POP().NAKED.get(in2) == 0) {
				ap.colorClothes(in2).bind();
				sp.render(r, ITUNIC+dir, x, y);
				COLOR.unbind();
			}

			for (RAddon add : in2.race().appearance().types.get(ap.gender.get(in2)).addonsBelow) {
				add.renderStanding(r, dir, x, y, in2, false);
			}

			//HEAD
			ap.colorSkin(in2).bind();
			sp.render(r, dir + IHEAD, x, y);

			for (RAddon add : in2.race().appearance().types.get(ap.gender.get(in2)).addonsAbove) {
				add.renderStanding(r, dir, x, y, in2, false);
			}
			COLOR.unbind();

			OPACITY.O75.bind();
			water(in2, dir, torso, x, y);
			OPACITY.unbind();
			
		}
		
		@Override
		public void renderSimple(Humanoid a, AIManager d, Renderer r, ShadowBatch s, float ds, int x, int y) {
			Induvidual in2 = a.indu();
			StatsAppearance ap = STATS.APPEARANCE();
			
			a.spriteTimer = Math.abs(a.spriteTimer) % torsos.length;
			
			TILE_SHEET sp = a.race().appearance().sheet(in2).sheet.sheet;
			RExtras ex = a.race().appearance().extra;
			int dir = getDir(a, a.spriteTimer);
			
			
			boolean ca = (carries && d.resourceCarried() != null);
			int torso = ca ? ITORSO_CARRY : torsos[(int)a.spriteTimer];
			torso += dir;
			
			//TORSO
			ap.colorSkin(in2).bind();


			if (STATS.POP().NAKED.get(in2) == 0) {
				ap.colorClothes(in2).bind();
			}
			sp.render(r, torso, x, y);
			
			
			
			//CARRY RESOURCE
			if (ca) {
				RESOURCE w = d.resourceCarried();
				if (d.resourceA() > 4){
					int cx = x + sp.size() / 2;
					int cy = y + sp.size() / 2;
					
					int dx = cx + (int) (sp.size()*0.5*a.speed.dir().xN());
					int dy = cy + (int) (sp.size()*0.5*a.speed.dir().yN());
					int ti = GAME.intervals().get05() & 0b011;
					ex.trolly.render(r, TROLLY[ti] + dir, dx-ex.trolly.size()/2, dy-ex.trolly.size()/2);
					s.setDistance2Ground(8).setHeight(0);
					ex.trolly.render(s, TROLLY[ti] + dir, dx-ex.trolly.size()/2, dy-ex.trolly.size()/2);
					int am = d.resourceA();
					if (am >= 8)
						am = 7;
					w.renderLaying(r, dx-C.TILE_SIZEH, dy -C.TILE_SIZEH, 0b1111, am);
				}
			}
			
			for (RAddon add : in2.race().appearance().types.get(ap.gender.get(in2)).addonsAbove) {
				add.renderStanding(r, dir, x, y, in2, false);
			}
			
			//SHADOWS
			if (!a.inWater) {
				s.setHeight(10).setDistance2Ground(0);
				a.race().appearance().sheet(in2).sheet.sheet.render(s, dir + ISHADOW, x, y);
			}
			
		}
		
	}

	public abstract void renderSimple(Humanoid a, AIManager ai, Renderer r, ShadowBatch shadows, float ds, int x, int y);
	
}
