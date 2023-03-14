package view.world.generator.tools;

import init.D;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.StringInputSprite;
import util.colors.GCOLOR;
import util.data.INT.INTE;
import util.gui.misc.*;
import util.gui.slider.GSliderInt;
import util.info.GFORMAT;
import world.WorldGen;
import world.WorldGen.WorldGenMapType;

public class UIWorldGenerateTerrain extends GuiSection{

	private static CharSequence ¤¤MapType = "choose map type";
	private static CharSequence ¤¤Random = "Random";
	
	private static CharSequence ¤¤latitude = "¤latitude";
	private static CharSequence ¤¤nort = "¤northern";
	private static CharSequence ¤¤south = "¤southern";
	
	private static CharSequence ¤¤seed = "¤Random Seed";
	private static CharSequence ¤¤seedgenerate = "¤Generate based on seed.";

	static {
		D.ts(UIWorldGenerateTerrain.class);
	}

	private final WorldGen spec;

	
	public UIWorldGenerateTerrain(WorldGen spec){

		this.spec = spec;
		
		{
			GuiSection s = new GuiSection();
			int gi = 0;
			final int xs = 6;
			final int mx = 2;
			final int my = 2;
			s.addGrid(new RMapTypeNone(), gi++, xs, mx, my);
			
			for (WorldGenMapType t : WorldGenMapType.getAll())
				s.addGrid(new RMapType(t), gi++, xs, mx, my);
			
			s.addRelBody(8, DIR.N, new GHeader(¤¤MapType));
			
			addRelBody(16, DIR.S, s);
			
		}
		
		{
			addRelBody(16, DIR.S, new GHeader(¤¤latitude));
			
			INTE lat = new INTE() {
				
				@Override
				public int min() {
					return 0;
				}
				
				@Override
				public int max() {
					return 100;
				}
				
				@Override
				public int get() {
					return (int) Math.round(spec.lat*100);
				}
				
				@Override
				public void set(int t) {
					spec.lat = t/100.0;
				}
			};
			
			CLICKABLE gg =  new GSliderInt(lat, 180, true) {
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					b.add(GFORMAT.percGood(b.text(), lat.getD()));
				}
			};
			
			addRelBody(8, DIR.S, gg);
			
			addRightC(16, new GText(UI.FONT().S, ¤¤nort));
			
			GText t = new GText(UI.FONT().S, ¤¤south);
			
			int y1 = getLastY1();
			
			add(t, gg.body().x1()-16-t.width(), y1);
			
		}
		
		{
			addRelBody(16, DIR.S, new GHeader(¤¤seed));
			
			
			GInput seed = new GInput(new StringInputSprite(10, UI.FONT().M) {
				@Override
				protected void acceptChar(char c) {
					if (c >= '0' && c <= '9')
						super.acceptChar(c);
				}
			}) {
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
					GCOLOR.UI().bg().render(r, body);
					super.render(r, ds, isActive, isSelected, isHovered);
				}
			};
			seed.text().clear().add(spec.seed);
			addRelBody(16, DIR.S, seed);
			
			
			addRightC(4, new GButt.Standalone(SPRITES.icons().m.arrow_right){
				@Override
				protected void clickA() {
					int se = RND.seed();
					CharSequence s = seed.text();
					if ((s.length() > 9))
						s = (""+s).substring(0, 9);
					try {
						se = Integer.parseInt("" + s);
						spec.seed = se;
						RND.setSeed(se);
						//GAME.world().regenerate();
					}catch(Exception e) {
						seed.text().clear().add('1');
					}
					
				};

			}.hoverInfoSet(¤¤seedgenerate));
			
		}
		
		
		
		
		
		
	}
	
	private class RMapType extends ClickableAbs{

		private final WorldGenMapType type;
		
		public RMapType(WorldGenMapType type) {
			body.setDim(WorldGenMapType.DIM+10, WorldGenMapType.DIM+10);
			this.type = type;
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			isSelected |= type.name.equals(spec.map);
			GCOLOR.UI().border().render(r, body);
			GCOLOR.UI().bg(isActive, isSelected, isHovered).render(r, body, -1);
			type.render(r, body().x1()+5, body().y1()+5, 1);
		}
		
		@Override
		protected void clickA() {
			spec.map = type.name;
		}
	}
	
	private class RMapTypeNone extends ClickableAbs{

		private final GText text;
		
		public RMapTypeNone() {
			body.setDim(WorldGenMapType.DIM+10, WorldGenMapType.DIM+10);
			text = new GText(UI.FONT().M, ¤¤Random);
			text.setMultipleLines(true);
			text.setMaxWidth(WorldGenMapType.DIM);
			text.adjustWidth();
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			isSelected |= spec.map == null;
			GCOLOR.UI().border().render(r, body);
			GCOLOR.UI().bg(isActive, isSelected, isHovered).render(r, body, -1);
			text.renderC(r, body);
		}
		
		@Override
		protected void clickA() {
			spec.map = null;
		}
	}
	
	
}
