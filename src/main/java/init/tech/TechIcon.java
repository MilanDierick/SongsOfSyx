package init.tech;

import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.sprite.SPRITE;

final class TechIcon {

	public static SPRITE icon(TECH t) {
		if (t.lockers.all().size() == 1) {
			SPRITE bg = new SPRITE.Scaled(t.lockers.all().get(0).lockable.icon, 2);
			return get(bg, UI.icons().m.lock);
		}else if (t.lockers.all().size() > 1) {
			SPRITE bg = new SPRITE.Imp(Icon.HUGE) {

				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					int w = (X2-X1)/2;
					int h = (Y2-Y1)/2;
					for (int i = 0; i < t.lockers.all().size() && i < 4; i++) {
						int dx = (i%2)*w;
						int dy = (i/2)*h;
						t.lockers.all().get(i).lockable.icon.render(r, X1+dx, X1+w+dx, Y1+dy, Y1+h+dy);
					}
				}
				
			};
			return get(bg, UI.icons().m.lock);
		}else if (t.boosters.all().size() == 1) {
			SPRITE bg = new SPRITE.Scaled(t.boosters.all().get(0).boostable.icon, 2);
			return get(bg, UI.icons().m.plus);
			
		}else if (t.boosters.all().size() > 1) {
			SPRITE bg = new SPRITE.Imp(Icon.HUGE) {

				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					int w = (X2-X1)/2;
					int h = (Y2-Y1)/2;
					for (int i = 0; i < t.boosters.all().size() && i < 4; i++) {
						int dx = (i%2)*w;
						int dy = (i/2)*h;
						t.boosters.all().get(i).boostable.icon.render(r, X1+dx, X1+w+dx, Y1+dy, Y1+h+dy);
					}
				}
				
			};
			return get(bg, UI.icons().m.plus);
		}
		
		return UI.icons().s.cancel;
	}
	
	private static SPRITE get(SPRITE bg, SPRITE fg) {
		
		return new SPRITE.Imp(Icon.HUGE) {

			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				bg.renderC(r, X1+(X2-X1)/2, Y1+(Y2-Y1)/2);
				fg.render(r, X2-fg.width()+8, Y1-8);
			}
			
		};
		
	}
	
}
