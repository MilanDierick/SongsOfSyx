package launcher;

import java.io.IOException;

import init.paths.PATHS;
import snake2d.SPRITE_RENDERER;
import snake2d.TextureHolder;
import snake2d.util.color.*;
import snake2d.util.file.SnakeImage;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.sprite.*;
import snake2d.util.sprite.text.Font;
import snake2d.util.sprite.text.Text;
import util.gui.misc.GButt;
import util.spritecomposer.FontReader;

class Resources {

	static Sounds Sounds;
	static Sprites Sprites;
	static GUI Gui;
	
	Resources(){
		Sounds = new Sounds();
		Sprites = new Sprites();
		Gui = new GUI();
	}
	
	static void nullify() {
		Sounds = null;
		Sprites = null;
		Gui = null;
	}
	
	static class Sounds{
		
		Sounds(){
			
			GButt.defaultHoverSound = null; 
			GButt.defaultClickSound = null;
			
		}
		
	}
	
	static class Sprites extends SpriteSheet{
		
		final SPRITE[] clouds;
		final SPRITE logo;
		final BigSprite syxMap;
		
		final SPRITE[] smallPanel;
		final SPRITE[] arrowUpDown;
		
		
		final SPRITE checkBox1;
		final SPRITE checkBox2;
		final SPRITE[] arrowLR;
		final SPRITE[] social;
		
		final COLOR colorh = new ColorImp(20, 128, 20);
		final Font font;
		
		final SPRITE kickstarter;
		
		public Sprites() {
			super(2);
			SnakeImage i;
			try {
				i = new SnakeImage(PATHS.BASE().LAUNCHER.get("Launcher"), 1024, 1024);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
			smallPanel = new SPRITE[] {
				getSprite(0, 450, 0, 33),
				getSprite(0, 450, lastY2+1, 20),
				getSprite(0, 450, lastY2+1, 23),
			};
			setScale(2);
			arrowUpDown = getVerticalSpriteArray(1009, 14, 100, 18, 2);
			
			checkBox1	= getSprite(980, 14, 107, 14);
			checkBox2	= getSprite(lastX2, 14, lastY1, 14);
			arrowLR = new SPRITE[2];
			arrowLR[0] 	= getSprite(972, 18, lastY2+1, 14);
			arrowLR[1] 	= getSprite(lastX2, 18, lastY1, 14);
			social = new SPRITE[] {
				getSprite(880, 22, 120, 16),
				getSprite(lastX2+1, 22, lastY1, 16),
				getSprite(lastX2+1, 22, lastY1, 16),
				getSprite(lastX2+1, 22, lastY1, 16)
			};
			font = new FontReader(" !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~").get(0, 136, i, PATHS.BASE().LAUNCHER.get("Launcher"));
			
			
			logo = getSprite(451, 418, 1, 56);
			clouds = getHorizontalSpriteArray(451, 60, 58, 60, 6);
			kickstarter = getSprite(899, 72, 48, 71);;
			setScale(4);
			syxMap = getBigSprite(0, 1023, 154, 870);
			
			
			new TextureHolder(i, null, 1008, 137, 16, 16);
			
		}

	}
	
	final static class GUI {

		final COLOR c_hover = new ColorShifting(new ColorImp(127,127,65),
				new ColorImp(110,90,45));
		final COLOR c_selected = new ColorImp(80, 110, 65);
		final COLOR c_hover_selected = new ColorImp(100, 128, 80);
		final COLOR c_inactive = COLOR.BROWN;
		final COLOR c_unclickable = new ColorImp(110,90,45);
		final COLOR c_label = new ColorImp(127,127,65);

		static abstract class Button extends CLICKABLE.ClickableAbs {

			private final SPRITE s;

			private Button(SPRITE s) {
				this.s = s;
				body.setWidth(s.width()).setHeight(s.height());
			}

			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isActive,
					boolean isSelected, boolean isHovered) {
				if (!isActive)
					Gui.c_inactive.bind();
				else if (isHovered && isSelected)
					Gui.c_hover_selected.bind();
				else if (isHovered)
					Gui.c_hover.bind();
				else if (isSelected)
					Gui.c_selected.bind();
				
				s.render(r, body.x1(), body.y1());
				COLOR.unbind();

			}

			static class Sprite extends Button {

				Sprite(SPRITE s) {
					super(s);
				}

			}

			static class Text extends Button {

				Text(String text) {
					super(new snake2d.util.sprite.text.Text(Resources.Sprites.font, text).setScale(1.5));
				}

			}

		}
		
		abstract static class RText extends LSprite{
			
			private RText(SPRITE s){
				super(s);
			}
			
			protected static class Header  extends RText{
				
				Header(String t){
					super(new Text(Resources.Sprites.font, t).setScale(2));
					getColor().set(Gui.c_label);
				}
				
			}
			protected static class HeaderSmall  extends RText{
				
				HeaderSmall(String t){
					super(new Text(Resources.Sprites.font, t).setScale(1));
					getColor().set(Gui.c_label);
				}
				
			}
			protected static class Normal  extends RText{
				
				Normal(String t){
					super(new Text(Resources.Sprites.font, t).setScale(2));
					getColor().set(Gui.c_unclickable);
				}
			}
			
		}

	}
	
}
