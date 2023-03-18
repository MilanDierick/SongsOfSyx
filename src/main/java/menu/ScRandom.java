package menu;

import game.GameConRandom;
import init.D;
import init.paths.PATHS;
import init.sprite.UI.UI;
import menu.GUI.COLORS;
import menu.screens.Screener;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.Json;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.data.GETTER.GETTER_IMP;
import util.data.INT.IntImp;
import util.gui.misc.GText;
import util.info.INFO;

class ScRandom extends GuiSection implements SCREEN{

	private final GameConRandom spec;
	
	final CharSequence ¤¤name = "¤random game";
	final CharSequence ¤¤normal = "¤normal";
	final CharSequence ¤¤easy = "¤easy";
	final CharSequence ¤¤hard = "¤hard";
	final CharSequence ¤¤titleDisabled = "¤Title unlocking will be disabled.";
	
	ScRandom(Menu menu){
		
		D.t(this);
		
		Screener screen = new Screener(¤¤name, GUI.labelColor) {
			
			@Override
			protected void back() {
				menu.switchScreen(menu.main);
			}
		};
		
		add(screen);
		
		
		spec = new GameConRandom();
		
		CLICKABLE b = new Screener.ScreenButton(D.g("go!")) {
			@Override
			protected void clickA() {
				menu.start(spec);
			}
		};
		
		screen.addButt(b);
		
		GuiSection options = new GuiSection(); 
		
		{
			RENDEROBJ r = advantage();
			r.body().moveY1(0);
			r.body().moveCX(-340);
			options.add(r);
		}
		

		options.body().centerIn(body());
		
		add(options);
		
	}

	
	private GuiSection advantage() {
		GETTER_IMP<CharSequence> hText = new GETTER_IMP<>();
		GuiSection s = new GuiSection();
		s.add(new HOVERABLE.Sprite(UI.FONT().H2.getText(D.g("Difficulty")), COLORS.label), 0, 0);
		
		String[] boos;
		double[] values;
		
		boos = new String[] {
			"FURNITURE",
			"RAIDING",
			"MAINTENANCE",
			"SPOILAGE",
		};
		values = new double[] {
			-0.5, -0.25, 0, 0.25, 0.5, 1, 2
		};
		addLine(s, boos, values, "CIVIC", hText);
		
		boos = new String[] {
			"HUNGER",
		};
		values = new double[] {
			0.5, 0.25, 0, -0.1, -0.25,
		};
		addLine(s, boos, values, "RATES", hText);
		
		boos = new String[] {
			"HAPPINESS",
		};
		values = new double[] {
			-0.2, -0.1, 0, 0.05, 0.1, 0.2,
		};
		addLine(s, boos, values, "BEHAVIOUR", hText);
		
		s.add(new RENDEROBJ.RenderImp(300, UI.FONT().M.height()) {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				COLOR.YELLOW100.bind();
				for (Double i : spec.BOOSTS.all()) {
					if (i > 0) {
						UI.FONT().M.render(r, ¤¤titleDisabled, body().x1(), body().y1(), body().width(), 1);
						break;
					}
				}
				
				COLOR.unbind();
				
			}
		}, s.getLastX1()-120, s.getLastY2()+16);
		
		s.add(new RENDEROBJ.RenderImp(300, 100) {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				COLOR.WHITE150.bind();
				CharSequence s = hText.get();
				hText.set(null);
				if (s != null) {
					UI.FONT().M.render(r, s, body().x1(), body().y1(), body().width(), 1);
				}
				COLOR.unbind();
				
			}
		}, s.getLastX1(), s.getLastY2()+32);
		return s;
	}
	
	private void addLine(GuiSection s, String[] boos, double[] values, String coll, GETTER_IMP<CharSequence> hText) {
		Json jText = new Json(PATHS.TEXT_CONFIG().get("BOOSTABLE")).json(coll);
		int z = 0;
		for (int i = 0; i < values.length; i++) {
			if (values[i] == 0)
				z = i;
		}
		
		final int zero = z;
		for (String bo : boos) {
			IntImp intt = new IntImp(0, values.length-1) {
				@Override
				public void set(int t) {
					super.set(t);
					spec.BOOSTS.put(coll+"_"+bo, values[t]);
				}
			};
			intt.set(zero);
			INFO info = new INFO(jText.json(bo));
			CLICKABLE c = new GUI.OptionLine(intt, info.name) {
				private CharSequence desc = info.desc;
				@Override
				protected void setValue(GText str) {
					
					str.clear();
					if (intt.get() < zero) {
						str.clear().add(¤¤hard);
						str.errorify();
						for (int i = 0; i < zero-intt.get(); i++) {
							str.add('+');
						}
					}else if (intt.get() > zero) {
						str.clear().add(¤¤easy);
						str.normalify2();
						for (int i = zero; i < intt.get(); i++) {
							str.add('+');
						}
					}else {
						str.color(COLOR.WHITE85);
						str.add(¤¤normal);
					}
				}
				@Override
				public boolean hover(COORDINATE mCoo) {
					if (super.hover(mCoo)) {
						hText.set(desc);
						return true;
					}
					return false;
				}
			};
			s.addDown(2, c);
		}
	}
	
	@Override
	public boolean back(Menu menu) {
		menu.switchScreen(menu.main);
		return true;
	}
	

	
}
