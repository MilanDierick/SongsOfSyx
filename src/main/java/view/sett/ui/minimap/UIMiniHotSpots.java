package view.sett.ui.minimap;

import java.io.IOException;

import init.C;
import init.D;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.ShortCoo;
import snake2d.util.file.*;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.StringInputSprite;
import util.colors.GCOLOR;
import util.data.INT.INTE;
import util.gui.misc.*;
import util.gui.panel.GFrame;
import util.gui.slider.GTarget;
import view.main.VIEW;
import view.sett.ui.minimap.UIMinimap.Expansion;
import view.subview.GameWindow;
import view.tool.PlacableSingle;

final class UIMiniHotSpots extends Expansion{


	private final int max = 32;
	private final int width = (int) (Icon.M*1.5);
	private final GText text = new GText(UI.FONT().S, 20);
	private final ColorImp colorImp = new ColorImp();

	private int state = 0;
	private final GameWindow window;
//	private final Stack<HotspotData> free = new Stack<>(max);
//	final ArrayList<HotspotData> added = new ArrayList<>(max);
	final HotspotData[] butts = new HotspotData[max];
	private final Panel panel = new Panel();
	private static CharSequence ¤¤order = "¤ORDER";
	private static CharSequence ¤¤set = "¤Set Hotspot";
	private static CharSequence ¤¤setLong = "¤Sets a hotspot that can easily be navigated to with a single click.";
	static {
		D.ts(UIMiniHotSpots.class);
	}
	
	protected UIMiniHotSpots(int index, int y1, GameWindow window) {
		super(index);

		for (int i = 0; i < max; i++) {
			butts[i] = new HotspotData();
		}
		this.window = window;
		add(new View(y1));
	}
	

	
	private final PlacableSingle placer = new PlacableSingle(¤¤set) {
		
		@Override
		public void placeFirst(int tx, int ty) {
			for (HotspotData d : butts) {
				if (!d.active) {
					d.set(tx, ty);
					break;
				}
			}
			VIEW.s().tools.placer.deactivate();
		}
		
		@Override
		public CharSequence isPlacable(int tx, int ty) {
			return null;
		}
		
		@Override
		public SPRITE getIcon() {
			return SPRITES.icons().m.crossair;
		}; 
	};
	
	
	private final class Button extends CLICKABLE.ClickableAbs{

		private HotspotData d;
		
		Button(){
			body.setWidth(width).setHeight(Icon.L);
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			COLOR.WHITE25.render(r, body().x1(), body.x2(), body().y1(), body.y1()+1);
			colorImp.set(d.color);
			if (isHovered || isSelected)
				colorImp.shadeSelf(1.5);
			colorImp.render(r, body().x1(), body.x2(), body().y1(), body.y2()-1);
			COLOR.WHITE25.render(r, body().x1(), body.x2(), body().y2()-1, body.y2());
			
			if (isHovered || isSelected)
				COLOR.WHITE15.render(r, body().x1(), body().x2(), body().y1()+6, body().y2()-6);
			else
				COLOR.WHITE10.render(r, body().x1(), body().x2(), body().y1()+6, body().y2()-6);
			
			if (d.name.length() > 0) {
				text.clear();
				text.add(d.name, 0, 2);
				text.adjustWidth();
				text.renderC(r, body);
			}
			
			
			
		}
		
		void set(HotspotData d) {
			this.d = d;
		}
		
		@Override
		protected void clickA() {
			panel.init(d);
			VIEW.inters().popup.show(panel, this);
			window.centererTile.set(d.tile);
			
		}
		
		
		
	}
	
	final class HotspotData implements SAVABLE{
		final ShortCoo tile = new ShortCoo();
		final ColorImp color = new ColorImp();
		final Str name = new Str(20);
		boolean active;
		
		HotspotData(){

		}
		
		void setPosition(int position) {
			int i = 0;
			for (HotspotData d : butts) {
				if (i == position) {
					if (d == this)
						return;
					if (!d.active)
						return;
					state++;
					int tx = d.tile.x();
					int ty = d.tile.y();
					ColorImp.TMP.set(d.color);
					Str.TMP.clear().add(d.name);
					
					d.tile.set(tile);
					d.color.set(color);
					d.name.clear().add(name);
					
					tile.set(tx, ty);
					color.set(ColorImp.TMP);
					name.clear().add(Str.TMP);
				}
				if (d.active)
					i++;
				
			}
			
		}
		
		void set(int tx, int ty) {
			active = true;
			tile.set(tx, ty);
			color.set(RND.rInt(127), RND.rInt(127), RND.rInt(127));
			
			name.clear().add('?');
			state++;
			
		}
		
		void remove() {
			if (active) {
				active = false;
				state++;
			}
		}
		
		@Override
		public void save(FilePutter file) {
			file.bool(active);
			file.i(tile.x());
			file.i(tile.y());
			
			color.save(file);
			name.save(file);
			
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			active = file.bool();
			tile.set(file.i(), file.i());
			color.load(file);
			name.load(file);
		}
		
		@Override
		public void clear() {
			active = false;
		}
	}

	@Override
	public void save(FilePutter file) {
		for (HotspotData b : butts) {
			b.save(file);
		}
			
	}

	@Override
	public void load(FileGetter file) throws IOException {
		state++;
		for (HotspotData b : butts) {
			b.load(file);
		}
		
	}

	@Override
	public void clear() {
		for (HotspotData b : butts) {
			b.clear();
		}
		state++;
	}
	
	public CLICKABLE get(int y1) {
		return new View(y1);
	}
	
	private class View extends GuiSection {
		
		private final GuiSection section = new GuiSection();
		private final CLICKABLE toggle;
		private int bi = 0;
		private final Button[] buttons;
		
		View(int y1){
			buttons = new Button[max];
			for (int i = 0; i < max; i++)
				buttons[i] = new Button();
	
			body().setWidth(Icon.M*1.5+GFrame.MARGIN*2).setHeight(C.HEIGHT()-y1);
			body().moveX2(C.WIDTH());
			body().moveY1(y1);
			
			
			section.merge(section);
			
			toggle = new GButt.Panel(SPRITES.icons().m.crossair) {

				@Override
				protected void clickA() {
					for (HotspotData d : butts) {
						if (!d.active) {
							VIEW.s().tools.place(placer);
							return;
						}
					}
				};
			}.hoverInfoSet(¤¤setLong);
			
			toggle.body().moveY1(body().y1()+10);
			toggle.body().centerX(this);
			add(toggle);
			section.body().moveY1(toggle.body().y1());
			section.body().centerX(body());
			add(section);
		}
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			if (bi != state) {
				section.clear();
				int i = 0;
				for (HotspotData b : butts) {
					if (b.active) {
						buttons[i].set(b);
						section.addDownC(0, buttons[i]);
						i++;
					}
				}
				section.body().centerX(toggle);
				section.body().moveY1(toggle.body().y2()+8);
				bi = state;
			}
			if (visableIs()) {
				GCOLOR.UI().panBG.render(r, body());
				super.render(r, ds);
				GCOLOR.UI().borderH(r, body(), 0);
			}
			
		}
		
	
		
	}
	
	private class Panel extends GuiSection{
		
		GInput name;
		private HotspotData data;
		
		void init(HotspotData data) {
			this.data = data;
			name.text().clear().add(data.name);
			//name.focus();
		}
		
		Panel(){
			
			name = new GInput(new StringInputSprite(20, UI.FONT().M) {
				@Override
				protected void change() {
					
					data.name.clear().add(text());
				};
			});
			add(name, 0, 0);
			
			addRightC(20, new GButt.Panel(SPRITES.icons().m.trash) {
				@Override
				protected void clickA() {
					data.remove();
					VIEW.inters().popup.close();
				}
			});
			
			addRelBody(C.SG*8, DIR.S, new GColorPicker(false) {
				
				@Override
				public ColorImp color() {
					return data.color;
				}
			});
			
			INTE order = new INTE() {
				
				@Override
				public int min() {
					return 0;
				}
				
				@Override
				public int max() {
					int i = 0;
					for (HotspotData d : butts) {
						if (d.active)
							i++;
					}
					return i-1;
				}
				
				@Override
				public int get() {
					int i = 0;
					for (HotspotData d : butts) {
						if (d == data)
							return i;
						if (d.active)
							i++;
					}
					return -1;
				}
				
				@Override
				public void set(int t) {
					data.setPosition(t);
					for (HotspotData d : butts) {
						if (t == 0 && d.active) {
							init(d);
							break;
						}
						if (d.active)
							t--;
					}
				}
			};
			
			GTarget t = new GTarget(40, false, true, order);
			
			addRelBody(C.SG*8, DIR.S, new GText(UI.FONT().H2, ¤¤order).toUpper().lablify());
			addRelBody(C.SG*2, DIR.S, t);
		}
		
	}
	
}
