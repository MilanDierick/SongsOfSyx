package view.sett.ui.standing;

import init.race.Race;
import settlement.entity.humanoid.HCLASS;
import settlement.stats.STATS;
import settlement.stats.stat.SETT_STATISTICS;
import settlement.stats.stat.STAT;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TextureCoords;
import util.colors.GCOLOR;
import util.gui.misc.GBox;
import util.gui.table.GStaples;

class UtilGraph implements SPRITE {

	private final GStaples staples = new GStaples(STATS.DAYS_SAVED) {
		
		@Override
		protected void hover(GBox box, int stapleI) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		protected double getValue(int stapleI) {
			int fromZero = STATS.DAYS_SAVED-stapleI-1;
			if (!valuev && global instanceof STAT) {
				STAT s = (STAT) global;
				if (s.standing().max(c, race, fromZero) > 0) {
					return s.standing().get(c, race, global.data(c).getD(race, fromZero));
				}
			}
			
			return global.data(c).getD(race, fromZero);
		}
		
		@Override
		protected void setColor(ColorImp col, int stapleI, double value) {
			int fromZero = STATS.DAYS_SAVED-stapleI-1;
			
			if (!valuev && global instanceof STAT) {
				STAT s = (STAT) global;
				if (s.standing().max(c, race, fromZero) > 0) {
					col.set(GCOLOR.UI().NEUTRAL.normal);
					return;
				}
			}
			col.set(COLOR.WHITE65);
		};
		
		@Override
		protected void setColorBg(ColorImp col, int stapleI, double value) {
			
			col.set(COLOR.WHITE05);
		};
		
	};
	private SETT_STATISTICS global;
	private HCLASS c;
	private Race race;
	private boolean valuev;
	
	UtilGraph(){
		
		staples.body().setDim(250, 64);
	}
	
	SPRITE init(HCLASS c, SETT_STATISTICS global, boolean isInt){
		return init(c, global, isInt, CitizenMain.current, false);
	}
	
	SPRITE init(HCLASS c, SETT_STATISTICS global, boolean isInt, Race race, boolean isValue){
		valuev = isValue;
		this.c = c;
		this.global = global;
		this.race = race;
		return this;
	}

	@Override
	public int width() {
		return staples.body().width();
	}

	@Override
	public int height() {
		return staples.body().height();
	}

	@Override
	public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
		staples.body().moveX1Y1(X1, Y1);
		staples.render(r, 0);
	}

	@Override
	public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
		// TODO Auto-generated method stub
		
	}
	
}
