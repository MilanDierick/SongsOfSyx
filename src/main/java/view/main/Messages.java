package view.main;

import java.io.IOException;

import game.GAME;
import game.time.TIME;
import init.C;
import init.D;
import init.paths.PATHS;
import init.sprite.ICON;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.*;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.*;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.KeyMap;
import snake2d.util.sprite.text.Text;
import util.data.GETTER;
import util.dic.DicTime;
import util.gui.misc.*;
import util.gui.panel.GPanelL;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import view.interrupter.*;

public final class Messages {

	private final Message[] all = new Message[256];
	private int size = 0;
	private int unread = 0;
	private final List list;
	private final IMessage imess;
	private final InterManager manager;
	private final KeyMap<String> hideMap = new KeyMap<>();
	private final ArrayList<Message> queued = new ArrayList<>(16);
	
	Messages(InterManager manager){
		this.manager = manager;
		list = new List();
		imess = new IMessage();
		IDebugPanel.add("Send message", new ACTION() {
			@Override
			public void exe() {
				debug();
			}
		});
		read();
		
		
	}
	
	private static final String fn = "BlockedMessages";
	
	private static CharSequence ¤¤delete = "¤Delete all read messages";
	private static CharSequence ¤¤PauseD = "¤Pause and show this kind of message upon arrival.";
	private static CharSequence ¤¤title = "¤Title";
	private static CharSequence ¤¤Arrived = "¤Arrived";
	private static CharSequence ¤¤Messages = "¤Messages";
	static {
		D.ts(Messages.class);
	}
	
	private void read() {
		
		if (!PATHS.local().PROFILE.exists(fn))
			return;
		try {
			for (String s : new Json(PATHS.local().PROFILE.get(fn)).values("M")) {
				if (s != null && !s.equals("null"))
					hideMap.put(s, s);
			}
		} catch (Exception e) {
			e.printStackTrace(System.out);
			hideMap.clear();
		}
		
	}
	
	private void flush() {
		
		try {
			String[] vv = new String[hideMap.all().size()];
			int i = 0;
			for (String s : hideMap.all()) {
				vv[i++] = s;
			}
			JsonE j = new JsonE();
			j.add("M", vv);
			
			if (!PATHS.local().PROFILE.exists(fn))
				PATHS.local().PROFILE.create(fn);
			j.save(PATHS.local().PROFILE.get(fn));
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void debug() {
		
		MessageText t = new MessageText("Debugging");
		t.paragraph("hello!");
		t.paragraph("goodbye!");
		t.send();
		
		t = new MessageText("Debugging Long");
		t.paragraph("hello again!");
		for (int i = 0; i < 10; i++)
			t.paragraph("You're taking too long to make this game. I'm so glad you're making this game Jake, isn't it a lot of fun! Could you add Godzilla monsters please, I so much want them. Until next time, ta ta!");
		
		t.send();
	}
	
	boolean add(Message m) {
		
		remove(all.length-1);
		size++;
		for (int i = all.length-1; i > 0; i--) {
			all[i] = all[i-1];
		}
		all[0] = m;
		m.section = m.makeSection();
		unread ++;
		
		if (hideMap.containsKey(m.key)) {
			m.isRead = true;
			unread--;
			return false;
		}
		
		if (imess.isActivated()) {
			if (queued.hasRoom())
				queued.add(m);
		}else
			imess.act(m);
		return true;
	}
	
	private void remove(int index) {
		
		if (all[index] == null)
			return;
		if(!all[index].isRead)
			unread--;
		size --;
		all[index] = null;
		for (int i = index; i < all.length-1; i++) {
			all[i] = all[i+1];
		}
		all[all.length-1] = null;
	}
	
	GButt.Panel getButt() {
		GButt.Panel b = new GButt.Panel(SPRITES.icons().m.openscroll) {
			private Text nr = new Text(UI.FONT().M, 10);
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
					boolean isHovered) {
				isActive = size > 0;
				activeSet(isActive);
				isSelected = list.isActivated();
				super.render(r, ds, isActive, isSelected, isHovered);
				
				if (unread > 0) {
					nr.clear().add(unread).adjustWidth();
					if (!isHovered && TIME.currentSecond() - all[0].currentSecond < 3) {
						COLOR.WHITE2WHITE.bind();
						int x = body().x1() + (body.width() - ICON.MEDIUM.SIZE)/2;
						int y = body().y1() + (body.height() - ICON.MEDIUM.SIZE)/2;
						SPRITES.icons().m.openscroll.render(r, x, y);
					}
					
					int x = body().x1() + (body.width() - nr.width())/2;
					int y = body().y1() + (body.height() - nr.height())/2;
					COLOR.WHITE100.bind();
					nr.render(r, x-1, y-1);
					COLOR.RED50.bind();
					nr.render(r, x, y);
					COLOR.unbind();
				}
			}
			
			@Override
			protected void clickA() {
				if (size > 0)
					list.act();
			}
		};
		b.hoverInfoSet(¤¤Messages);
		return b;
	}
	
	public int size() {
		return size;
	}
	
	public boolean activated() {
		return list.isActivated();
	}
	
	public int unread() {
		return unread;
	}
	
	public double currentSecond() {
		return all[0].currentSecond;
	}
	
	public void activate() {
		if (size > 0)
			list.act();
	}
	
	public void hide() {
		imess.close.exe();
	}
	
	public void hideAll() {
		while (imess.isActivated())
			imess.close.exe();
	}

	void save(FilePutter saveFile) {
		
		saveFile.mark(this);
		saveFile.object(all);
		saveFile.i(size);

	}
	
	void load(FileGetter saveFile) throws IOException{
		saveFile.check(this);
		Message[] m = (Message[]) saveFile.object();
		unread = 0;
		for (int i =0; i < all.length; i++) {
			if (i <= m.length && m[i] != null) {
				all[i] = m[i];
				all[i].section = all[i].makeSection();
				if (!all[i].isRead) {
					unread++;
				}
			}else {
				all[i] = null;
			}
		}
		size = saveFile.i();
	}

	
	private class List extends Interrupter {

		private final GuiSection section;
		private boolean removed = false;

		
		protected List() {

			GTableBuilder builder = new GTableBuilder() {
				
				@Override
				public int nrOFEntries() {
					return size;
				}
				
				@Override
				public void click(int index) {
					if (removed) {
						removed = false;
						return;
					}
					Message message = all[index];
					if (message != null)
						imess.act(message);
				}
			};
			
			builder.column(¤¤title, 200, new GRowBuilder() {
				@Override
				public RENDEROBJ build(GETTER<Integer> ier) {
					return new GStat() {
						
						@Override
						public void update(GText text) {
							Message m = all[ier.get()];
							if (m == null)
								return;
							if (m.title().length() >= 20) {
								text.add(m.title(), 0, 20);
								text.add('.').add('.').add('.');
							}else {
								text.add(m.title());
							}
							if (m.isRead) {
								text.color(COLOR.WHITE65);
							}else {
								text.color(COLOR.WHITE100);
							}
						}
					}.r(DIR.NW);
				}
			});
			
			builder.column(¤¤Arrived, 200, new GRowBuilder() {
				@Override
				public RENDEROBJ build(GETTER<Integer> ier) {
					return new GStat() {
						
						@Override
						public void update(GText text) {
							Message m = all[ier.get()];
							if (m == null)
								return;
							int t = (int) (TIME.currentSecond() - m.currentSecond);
							DicTime.setAgo(text, t);

							if (m.isRead) {
								text.color(COLOR.WHITE65);
							}else {
								text.color(COLOR.WHITE100);
							}
						}
					}.r(DIR.NW);
				}
			});
			
			builder.column("", ICON.MEDIUM.SIZE*2, new GRowBuilder() {
				@Override
				public RENDEROBJ build(GETTER<Integer> ier) {
					return new GButt.Glow(SPRITES.icons().m.minus) {
						@Override
						protected void clickA() {
							Message m = all[ier.get()];
							if (m == null)
								return;
							
							remove(ier.get());
							removed = true;
						}
					}.hoverInfoSet("Delete");
				}
			});
	
			
			section = builder.create(15, true);
			
			CLICKABLE b = new GButt.ButtPanel(¤¤delete) {
				@Override
				protected void clickA() {
					int unI = 0;
					int nSize = size;
					
					for (int i = 0; i < size; i++) {
						if (all[i].isRead) {
							nSize--;
							all[i] = null;
							continue;
						}
						Message m = all[i];
						all[i] = null;
						all[unI] = m;
						unI ++;
					}
					
					size = nSize;
				}
				
				@Override
				protected void renAction() {
					activeSet(size - unread > 0);
				}
			};
			b.body().centerX(section).moveY1(section.body().y2()+10);
			section.add(b);
			
			GPanelL p = new GPanelL();
			p.set(section.body());
			
			p.setCloseAction(new ACTION() {
				@Override
				public void exe() {
					hide();
				}
			});
			p.body().centerY(C.DIM());
			p.body().centerX(C.WIDTH()/2, C.WIDTH());
			section.body().centerIn(p);
			section.add(p);
			section.moveLastToBack();
			RENDEROBJ title = new GText(UI.FONT().H2, ¤¤Messages).lablify().r(DIR.C);
			p.centreTitle(title);
			section.add(title);
			
			
			
		}

		void act(){
			show(manager);
		}
		
		@Override
		protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
			section.hover(mCoo);
			return true;
		}

		@Override
		protected void mouseClick(MButt button) {
			if (button == MButt.LEFT)
				section.click();
			if (button == MButt.RIGHT)
				hide();
		}

		@Override
		protected void hoverTimer(GBox text) {
			section.hoverInfoGet(text);
		}

		@Override
		protected boolean render(Renderer r, float ds) {
			section.render(r, ds);
			return true;
		}

		@Override
		protected boolean update(float ds) {
			// TODO Auto-generated method stub
			return true;
		}
		
	}
	
	private class IMessage extends Interrupter {

		private final GPanelL panel = new GPanelL();
		private Message m;
		
		private final GButt.CheckboxTitle show = new GButt.CheckboxTitle("!") {
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.text(¤¤PauseD);
			};
			
			@Override
			protected void renAction() {
				selectedSet(!hideMap.containsKey(m.key));
			};
			
			@Override
			protected void clickA() {
				if (hideMap.containsKey(m.key)) {
					hideMap.remove(m.key);
				}else {
					hideMap.put(m.key, m.key);
				}
				flush();
			};
		};
		
		ACTION close = new ACTION() {
			@Override
			public void exe() {
				hide();
				if (m == null)
					return;
				if (!m.isRead)
					unread--;
				m.isRead = true;
				if (!queued.isEmpty()) {
					act(queued.removeLast());
				}
			}
		};
		

		void act(Message m){
			if (VIEW.b().isActive())
				return;
			this.m = m;
			panel.getInnerArea().set(m.section);
			panel.getInnerArea().setWidth(Math.max(panel.getInnerArea().width(), 500));
			panel.getInnerArea().incrH(20);
			panel.body().centerIn(C.DIM());
			m.section.body().centerX(panel.getInnerArea());
			m.section.body().moveY1(panel.getInnerArea().y1());
			show.body.moveX2(m.section.body().x2()-8);
			show.body.moveY1(panel.getInnerArea().y2()-18);
			panel.setCloseAction(close);
			if (m.title() != null && m.title().length() > 2)
				panel.setTitle(m.title());
			if (GAME.SPEED.speedTarget() > 0)
				GAME.SPEED.speedSet(1);
			show(manager);
		}
		
		@Override
		protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
			panel.hover(mCoo);
			if (m.section instanceof HOVERABLE)
				((HOVERABLE)m.section).hover(mCoo);
			show.hover(mCoo);
			return true;
		}

		@Override
		protected void mouseClick(MButt button) {
			if (button == MButt.LEFT) {
				panel.click();
				if (m.section instanceof CLICKABLE)
					((CLICKABLE)m.section).click();
				show.click();
			}else if (button == MButt.RIGHT)
				close.exe();
		}

		@Override
		protected void hoverTimer(GBox text) {
			if (panel.hoveredIs())
				panel.hoverInfoGet(text);
			if (m.section instanceof HOVERABLE)
				((HOVERABLE)m.section).hoverInfoGet(text);
			if (show.hoveredIs())
				show.hoverInfoGet(text);
		}

		@Override
		protected boolean render(Renderer r, float ds) {
			panel.render(r, ds);
			m.section.render(r, ds);
			show.render(r, ds);
			return true;
		}

		@Override
		protected boolean update(float ds) {
			return false;
		}
		
	}
	
}
