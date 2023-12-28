package game.events.tutorial;

import java.util.Random;

import game.faction.FACTIONS;
import init.race.appearence.RPortrait;
import init.sprite.UI.UI;
import settlement.entity.humanoid.HTYPE;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.Rec;
import snake2d.util.gui.GuiSection;
import snake2d.util.sprite.SPRITE;
import util.data.INT_O.INT_OE;
import util.gui.misc.GText;
import view.ui.message.MessageSection;

class MessTut extends MessageSection{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String desc;
	private final String goal;
	private final Rec hilight;
	private static Induvidual indu;
	
	public MessTut(CharSequence title, CharSequence desc, CharSequence goal, Rec highlight) {
		super(title);
		this.desc = ""+desc;
		this.goal = goal == null ? null : ""+goal;
		this.hilight = highlight;
	}

	@Override
	protected void make(GuiSection section) {
		paragraph(desc);
		
		section.addRelBody(16, DIR.S, new SPRITE.Imp(100, 16) {
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				if (hilight != null) {
					highlight(X1 + (X2-X1)/2, Y1 + (Y2-Y1)/2, r, hilight);
				}
			}
		});
		
		if (goal != null) {
			GText t = new GText(UI.FONT().M, goal);
			t.setMaxWidth(WIDTH);
			t.lablifySub();
			section.addRelBody(0, DIR.S,t);
		}
		
		if (indu == null || indu.race() != FACTIONS.player().race()) {
			indu = new Induvidual(HTYPE.ENEMY, FACTIONS.player().race());
			STATS.APPEARANCE().gender.set(indu, 0);
			Random ran = new Random();
			ran.setSeed(55);
			for (INT_OE<Induvidual> ii : STATS.RAN().all()) {
				ii.set(indu, ran.nextInt());
			}
			STATS.NEEDS().DIRTINESS.set(indu, 0);
		}
		
		section.addRelBody(16, DIR.N, new SPRITE.Imp(RPortrait.P_WIDTH*2, RPortrait.P_HEIGHT*2) {
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				STATS.APPEARANCE().portraitRender(r, indu, X1, Y1, 2);
			}
		});
	}
	
	private void highlight(int cx, int cy, SPRITE_RENDERER r, Rec o) {
		
		COLOR c = COLOR.RED2RED;

		c.renderFrame(r, o, 0, 8);
		
		int xx = cx;
		{
			int WI = WIDTH/2 + 100;
			
			if (o.cX() < cx) {
				xx = cx-WI;
				c.render(r, xx-4, cx+4, cy-4, cy+4);
				
			}else {
				xx = cx+WI;
				c.render(r, cx-4, xx+4, cy-4, cy+4);
			}
		}
		
		if (o.y2() < cy) {
			c.render(r, xx-4, xx+4, cy-4, o.cY()+4);
		}else {
			c.render(r, xx-4, xx+4, o.cY()-4, cy+4);
		}
		
		if (xx < o.x1()) {
			c.render(r, xx-4, o.x1(),  o.cY()-4,  o.cY()+4);
			
		}else {
			c.render(r, o.x2(), xx+4, o.cY()-4,  o.cY()+4);
			
		}
		
		
//		
//		if (o.cX() < cx) {
//			c.render(r, o.x2()+4, cx + 4, o.cY()-4, o.cY()+4);
//		}else {
//			c.render(r, o.x1()-4, cx + 4, o.cY()-4, o.cY()+4);
//		}
//		
		
		
		
		
		OPACITY.unbind();
	}

}
