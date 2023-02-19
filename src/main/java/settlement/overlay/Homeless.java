package settlement.overlay;

import init.RES;
import init.sprite.SPRITES;
import settlement.entity.EntityIterator;
import settlement.entity.humanoid.Humanoid;
import settlement.main.RenderData;
import settlement.main.RenderData.RenderIterator;
import settlement.main.SETT;
import settlement.room.home.HOME;
import settlement.room.main.Room;
import settlement.room.main.RoomInstance;
import settlement.stats.STATS;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.sets.LISTE;
import util.colors.GCOLORS_MAP;

final class Homeless extends Addable{

	
	
	Homeless(LISTE<Addable> all, String key, CharSequence name, CharSequence desc) {
		super(all, key, name, desc, true, false);
		exclusive = true;
	}

	@Override
	public void initBelow(RenderData data) {
		
		RES.flooder().init(this);
		
		iter.iterate();
	};
	
	private final EntityIterator.Humans iter = new EntityIterator.Humans() {
		
		@Override
		protected boolean processAndShouldBreakH(Humanoid a, int ie) {
			if (a.indu().clas().player) {
				if (STATS.HOME().GETTER.hasSearched.indu().get(a.indu()) == 1) {
					RoomInstance r = STATS.WORK().EMPLOYED.get(a.indu());
					if (r != null) {
						if (!RES.flooder().hasBeenPushed(r.mX(), r.mY())) {
							RES.flooder().close(r.mX(), r.mY(), ((RoomInstance)r).employees().employed());
							RES.flooder().setValue2(r.mX(), r.mY(), 1);
						}else {
							RES.flooder().setValue2(r.mX(), r.mY(), RES.flooder().getValue2(r.mX(), r.mY()));
						}
					}else {
						RES.flooder().close(a.tc().x(), a.tc().y(), 0);
					}
				}
			}
			return false;
		}
	};
	
	@Override
	public void finishBelow() {
		RES.flooder().done();
	};
	
	@Override
	public boolean render(Renderer r, RenderIterator it) {
		return false;
	}
	
	@Override
	public void renderBelow(Renderer r, RenderIterator it) {
		Room room = SETT.ROOMS().map.get(it.tx(), it.ty());
		
		if (room != null) {
			int mx = room.mX(it.tx(), it.ty());
			int my = room.mY(it.tx(), it.ty());
			if (RES.flooder().hasBeenPushed(mx, my) && RES.flooder().getValue(mx, my) > 1) {
				int tot = (int) RES.flooder().getValue(mx, my);
				int home = (int) RES.flooder().getValue2(mx, my);
				if (home == tot)
					GCOLORS_MAP.BAD.bind();
				else
					GCOLORS_MAP.SOSO.bind();
				
			}else if (room.blueprint().employment() != null){
				GCOLORS_MAP.bestOverlay.bind();
			}else {
				HOME h = HOME.get(it.tx(), it.ty(), this);
				if (h != null) {
					ColorImp.TMP.interpolate(COLOR.WHITE100, GCOLORS_MAP.GOOD2, (double)h.occupants()/h.occupantsMax());
					ColorImp.TMP.bind();
					h.done();
				}else {
					COLOR.WHITE10.bind();
				}
				
			}
			
			SPRITES.cons().BIG.filled.render(r, 0, it.x(), it.y());
			
		}else if (SETT.PATH().getAvailability(it.tx(), it.ty()).player > 0) {
			COLOR.WHITE10.bind();
			SPRITES.cons().BIG.filled.render(r, 0, it.x(), it.y());
		}

	}
	
}
