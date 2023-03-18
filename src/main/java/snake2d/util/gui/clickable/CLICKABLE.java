package snake2d.util.gui.clickable;

import snake2d.*;
import snake2d.util.datatypes.*;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;

public abstract interface CLICKABLE extends HOVERABLE {
	
	
	
	public CLICKABLE hoverSoundSet(SoundEffect sound);

	@Override
	public CLICKABLE hoverInfoSet(CharSequence s);

	public CLICKABLE clickSoundSet(SoundEffect sound);

	public CLICKABLE selectedSet(boolean yes);
	
	public CLICKABLE selectTmp();

	public CLICKABLE selectedToggle();

	@Override
	public CLICKABLE visableSet(boolean yes);

	public CLICKABLE clickActionSet(ACTION f);
	
	public boolean click();
	public boolean activeIs();
	public CLICKABLE activeSet(boolean activate);
	public boolean selectedIs();

	@Override
	public CLICKABLE hoverTitleSet(CharSequence s);
	
	public abstract class ClickableAbs implements CLICKABLE {

		private byte repTimer = 0;
		
		public static SoundEffect defaultHoverSound = null;
		public static SoundEffect defaultClickSound = null;

		public final Rec body = new Rec();

		protected boolean isHovered = false;
		private boolean isVisable = true;
		private boolean isSelected = false;
		private boolean isActive = true;
		private boolean wasHovered = false;
		private boolean tmpSelect = false;
		private boolean repetative;

		private SoundEffect hoverSound = defaultHoverSound;
		private SoundEffect clickSound = defaultClickSound;
		protected CharSequence hoverInfo = null;
		protected CharSequence hoverTitle = null;
		protected ACTION clickAction;

		protected ClickableAbs() {

		}
		
		protected ClickableAbs(int width, int height) {
			body.setWidth(width).setHeight(height);
		}

		@Override
		public boolean activeIs() {
			return isActive;
		}

		@Override
		public boolean hover(COORDINATE mCoo) {
			if (!isVisable)
				return false;


			if (mCoo.isWithinRec(body())) {
				isHovered = true;
				if (!isActive)
					return true;
				
				if (!wasHovered) {
					repTimer = 0;
					wasHovered = true;
					if (hoverSound != null)
						hoverSound.play(false);
				}

			} else {
				isHovered = false;
				wasHovered = false;
				repTimer = 0;
			}
			if (isRepetative() && isHovered && MButt.LEFT.isDown()) {
				if (repTimer < 120)
					repTimer++;
				boolean click = false;
				if (repTimer > 80) {
					click = true;
				}else if(repTimer > 60) {
					click = (repTimer & 0b01) == 0;
				}else if(repTimer > 40) {
					click = (repTimer & 0b11) == 0;
				}else if(repTimer > 20) {
					click = (repTimer & 0b111) == 0;
				}
				
				if (click) {
					if (clickAction != null)
						clickAction.exe();
					clickA();
				}
				
			}
			
			return isHovered;
		}

		@Override
		public boolean hoveredIs() {
			return isHovered;
		}

		@Override
		public void hoverInfoGet(GUI_BOX text) {
			if (hoverInfo != null){
				text.text(hoverInfo);
			}
			if (hoverTitle != null)
				text.title(hoverTitle);
		}

		@Override
		public boolean click() {
			if (isVisable && isHovered && isActive) {
				clickA();
				if (clickSound != null)
					clickSound.play(false);
				if (clickAction != null)
					clickAction.exe();
				
				repTimer = 0;
				
				return true;
			}
			return false;
		}
		
		protected void clickA() {
			
		}

		@Override
		public boolean visableIs() {
			return isVisable;
		}

		@Override
		public RECTANGLEE body() {
			return body;
		}

		protected void renAction() {
			
		}
		
		@Override
		public final void render(SPRITE_RENDERER r, float ds) {
			renAction();
			if (isVisable)
				render(r, ds, isActive, isSelected | tmpSelect, isHovered);
			isHovered = false;
			tmpSelect = false;

		}

		protected abstract void render(SPRITE_RENDERER r, float ds,
				boolean isActive, boolean isSelected, boolean isHovered);


		@Override
		public CLICKABLE hoverSoundSet(SoundEffect sound) {
			this.hoverSound = sound;
			return this;

		}

		@Override
		public CLICKABLE hoverInfoSet(CharSequence s) {
			this.hoverInfo = s;
			return this;
		}

		@Override
		public CLICKABLE hoverTitleSet(CharSequence s) {
			this.hoverTitle = s;
			return this;
		}
		
		@Override
		public CLICKABLE clickSoundSet(SoundEffect sound) {
			this.clickSound = sound;
			return this;
		}

		@Override
		public CLICKABLE activeSet(boolean activate) {
			this.isActive = activate;
			return this;
		}

		@Override
		public CLICKABLE selectedSet(boolean yes) {
			this.isSelected = yes;
			return this;
		}
		
		@Override
		public CLICKABLE selectTmp() {
			tmpSelect = true;
			return this;
		}

		@Override
		public ClickableAbs selectedToggle() {
			isSelected ^= true;
			return this;
		}

		@Override
		public CLICKABLE visableSet(boolean yes) {
			this.isVisable = yes;
			return this;
		}

		@Override
		public boolean selectedIs() {
			return isSelected || tmpSelect;
		}

		@Override
		public CLICKABLE clickActionSet(ACTION f) {
			this.clickAction = f;
			return this;
		}
		
		public final boolean isRepetative() {
			return repetative;
		}
		public CLICKABLE repetativeSet(boolean repetative) {
			this.repetative = repetative;
			return this;
		}

	}
	

	
	public class Switcher implements CLICKABLE {

		private CLICKABLE c;
		
		public Switcher(CLICKABLE c) {
			this.c = c;
		}
		
		public void set(CLICKABLE c) {
			set(c, DIR.NW);
		}
		
		public void set(CLICKABLE c, DIR d) {
			int dw = (this.c.body().width() - c.body().width())/2;
			int dh = (this.c.body().height() - c.body().height())/2;
			c.body().moveC(this.c.body().cX(), this.c.body().cY());
			c.body().incrX(dw*d.x());
			c.body().incrY(dh*d.y());
			this.c = c;
		}
		
		public CLICKABLE get() {
			return c;
		}
		
		@Override
		public boolean activeIs() {
			return c.activeIs();
		}

		@Override
		public boolean hover(COORDINATE mCoo) {
			return c.hover(mCoo);
		}

		@Override
		public boolean hoveredIs() {
			return c.hoveredIs();
		}

		@Override
		public void hoverInfoGet(GUI_BOX text) {
			c.hoverInfoGet(text);
		}

		@Override
		public boolean click() {
			return c.click();
		}

		@Override
		public boolean visableIs() {
			return c.visableIs();
		}

		@Override
		public RECTANGLEE body() {
			return c.body();
		}
		
		@Override
		public final void render(SPRITE_RENDERER r, float ds) {
			c.render(r, ds);
		}

		@Override
		public CLICKABLE hoverSoundSet(SoundEffect sound) {
			c.hoverSoundSet(sound);
			return this;
		}

		@Override
		public CLICKABLE hoverInfoSet(CharSequence s) {
			c.hoverInfoSet(s);
			return this;
		}
		
		@Override
		public CLICKABLE hoverTitleSet(CharSequence s) {
			c.hoverTitleSet(s);
			return this;
		}

		@Override
		public CLICKABLE clickSoundSet(SoundEffect sound) {
			c.clickSoundSet(sound);
			return this;
		}

		@Override
		public CLICKABLE activeSet(boolean activate) {
			c.activeSet(activate);
			return this;
		}

		@Override
		public CLICKABLE selectedSet(boolean yes) {
			c.selectedSet(yes);
			return this;
		}
		
		@Override
		public CLICKABLE selectTmp() {
			c.selectTmp();
			return this;
		}

		@Override
		public CLICKABLE selectedToggle() {
			c.selectedToggle();
			return this;
		}

		@Override
		public CLICKABLE visableSet(boolean yes) {
			c.visableSet(yes);
			return this;
		}

		@Override
		public boolean selectedIs() {
			return c.selectedIs();
		}

		@Override
		public CLICKABLE clickActionSet(ACTION f) {
			c.clickActionSet(f);
			return this;
		}

	}
	
	public abstract class Holder implements CLICKABLE {

		protected abstract CLICKABLE get();

		@Override
		public boolean activeIs() {
			if (get() == null)
				return false;
			return get().activeIs();
		}

		@Override
		public boolean hover(COORDINATE mCoo) {
			if (get() == null)
				return false;
			return get().hover(mCoo);
		}

		@Override
		public boolean hoveredIs() {
			if (get() == null)
				return false;
			return get().hoveredIs();
		}

		@Override
		public void hoverInfoGet(GUI_BOX text) {
			if (get() == null)
			get().hoverInfoGet(text);
		}

		@Override
		public boolean click() {
			if (get() == null)
				return false;
			return get().click();
		}

		@Override
		public boolean visableIs() {
			if (get() == null)
				return false;
			return get().visableIs();
		}

		@Override
		public RECTANGLEE body() {
			if (get() == null)
				return Rec.TEMP.set(0, 0, 0, 0);
			return get().body();
		}
		
		@Override
		public final void render(SPRITE_RENDERER r, float ds) {
			if (get() != null)
				get().render(r, ds);
		}

		@Override
		public CLICKABLE hoverSoundSet(SoundEffect sound) {
			if (get() == null)
				return null;
			return get().hoverSoundSet(sound);
		}

		@Override
		public CLICKABLE hoverInfoSet(CharSequence s) {
			if (get() == null)
				return null;
			return get().hoverInfoSet(s);
		}
		
		@Override
		public CLICKABLE hoverTitleSet(CharSequence s) {
			if (get() == null)
				return null;
			return get().hoverTitleSet(s);
		}

		@Override
		public CLICKABLE clickSoundSet(SoundEffect sound) {
			if (get() == null)
				return null;
			return get().clickSoundSet(sound);
		}

		@Override
		public CLICKABLE activeSet(boolean activate) {
			if (get() == null)
				return null;
			return get().activeSet(activate);
		}

		@Override
		public CLICKABLE selectedSet(boolean yes) {
			if (get() == null)
				return null;
			return get().selectedSet(yes);
		}
		
		@Override
		public CLICKABLE selectTmp() {
			if (get() == null)
				return null;
			return get().selectTmp();
		}

		@Override
		public CLICKABLE selectedToggle() {
			if (get() == null)
				return null;
			return get().selectedToggle();
		}

		@Override
		public CLICKABLE visableSet(boolean yes) {
			if (get() == null)
				return null;
			return get().visableSet(yes);
		}

		@Override
		public boolean selectedIs() {
			if (get() == null)
				return false;
			return get().selectedIs();
		}

		@Override
		public CLICKABLE clickActionSet(ACTION f) {
			if (get() == null)
				return null;
			return get().clickActionSet(f);
		}

	}
	
	public abstract class Wrapper implements CLICKABLE {

		protected abstract RENDEROBJ get();
		public Rec body = new Rec();
		private static Coo tmp = new Coo();
		
		public Wrapper() {
			
		}
		
		public Wrapper(RENDEROBJ obj) {
			body.set(obj.body());
		}
		
		private HOVERABLE hov() {
			if (get() != null && get() instanceof HOVERABLE)
				return (HOVERABLE) get();
			return null;
		}
		
		private CLICKABLE cli() {
			if (get() != null && get() instanceof CLICKABLE)
				return (CLICKABLE) get();
			return null;
		}
		
		@Override
		public boolean activeIs() {
			if (cli() == null)
				return false;
			return cli().activeIs();
		}

		@Override
		public boolean hover(COORDINATE mCoo) {
			if (hov() == null)
				return false;
			hov().body().moveX1Y1(body);
			if (mCoo.isWithinRec(body)) {
				if (mCoo.isWithinRec(hov().body())) {
					return hov().hover(mCoo);
				}
				
				tmp.set(get().body().cX(), get().body().cY());
				return hov().hover(tmp);
			}else {
				hov().hover(mCoo);
			}
			return false;
		}

		@Override
		public boolean hoveredIs() {
			if (hov() == null)
				return false;
			return hov().hoveredIs();
		}

		@Override
		public void hoverInfoGet(GUI_BOX text) {
			if (hov() != null)
				hov().hoverInfoGet(text);
		}

		@Override
		public boolean click() {
			if (cli() == null)
				return false;
			return cli().click();
		}

		@Override
		public boolean visableIs() {
			if (get() == null)
				return false;
			return get().visableIs();
		}

		@Override
		public RECTANGLEE body() {
			return body;
		}
		
		@Override
		public final void render(SPRITE_RENDERER r, float ds) {
			if (get() != null) {
				get().body().moveX1Y1(body);
				get().render(r, ds);
			}
			renAction();
		}
		
		protected void renAction() {
			
		}

		@Override
		public CLICKABLE hoverSoundSet(SoundEffect sound) {
			if (cli() == null)
				return null;
			return cli().hoverSoundSet(sound);
		}

		@Override
		public CLICKABLE hoverInfoSet(CharSequence s) {
			if (hov() == null)
				return null;
			hov().hoverInfoSet(s);
			return cli();
		}
		
		@Override
		public CLICKABLE hoverTitleSet(CharSequence s) {
			if (hov() == null)
				return null;
			hov().hoverTitleSet(s);
			return cli();
		}

		@Override
		public CLICKABLE clickSoundSet(SoundEffect sound) {
			if (cli() == null)
				return null;
			return cli().clickSoundSet(sound);
		}

		@Override
		public CLICKABLE activeSet(boolean activate) {
			if (cli() == null)
				return null;
			return cli().activeSet(activate);
		}

		@Override
		public CLICKABLE selectedSet(boolean yes) {
			if (cli() == null)
				return null;
			return cli().selectedSet(yes);
		}
		
		@Override
		public CLICKABLE selectTmp() {
			if (cli() == null)
				return null;
			return cli().selectTmp();
		}

		@Override
		public CLICKABLE selectedToggle() {
			if (cli() == null)
				return null;
			return cli().selectedToggle();
		}

		@Override
		public CLICKABLE visableSet(boolean yes) {
			if (cli() == null)
				return null;
			return cli().visableSet(yes);
		}

		@Override
		public boolean selectedIs() {
			if (cli() == null)
				return false;
			return cli().selectedIs();
		}

		@Override
		public CLICKABLE clickActionSet(ACTION f) {
			if (cli() == null)
				return null;
			return cli().clickActionSet(f);
		}

	}
	
	public class Pair extends ClickableAbs {

		private final RENDEROBJ a;
		private final RENDEROBJ b;
		final int offax,offay,offbx,offby;
		private HOVERABLE h;
		
		public Pair(RENDEROBJ a, RENDEROBJ b, DIR align, int margin){
			int dy = (a.body().height()+b.body().height())/2 + margin;
			int dx = (a.body().width()+b.body().width())/2 + margin;
			int sx = a.body().cX();
			int sy = a.body().cY();
			b.body().moveC(sx+dx*align.x(), sy+dy*align.y());
			this.a = a;
			this.b = b;
			body.set(a);
			body.unify(b.body());
			offax = a.body().x1()-body.x1();
			offay = a.body().y1()-body.y1();
			offbx = b.body().x1()-body.x1();
			offby = b.body().y1()-body.y1();
		}
		
		public Pair(RENDEROBJ a, RENDEROBJ b){
			this.a = a;
			this.b = b;
			body.set(a);
			body.unify(b.body());
			offax = a.body().x1()-body.x1();
			offay = a.body().y1()-body.y1();
			offbx = b.body().x1()-body.x1();
			offby = b.body().y1()-body.y1();
		}

		@Override
		public boolean hover(COORDINATE mCoo) {
			if (!visableIs())
				return false;
			h = null;
			if (a instanceof HOVERABLE && ((HOVERABLE) a).hover(mCoo)) {
				h = (HOVERABLE) a;
				if (b instanceof HOVERABLE)
					((HOVERABLE) b).hover(mCoo);
			}else if (b instanceof HOVERABLE && ((HOVERABLE) b).hover(mCoo)) {
				h = (HOVERABLE) b;
				if (b instanceof HOVERABLE)
					((HOVERABLE) b).hover(mCoo);
			}
			isHovered = h != null || mCoo.isWithinRec(body);
			return isHovered;
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			a.body().moveX1Y1(body);
			a.body().incrX(offax).incrY(offay);
			b.body().moveX1Y1(body);
			b.body().incrX(offbx).incrY(offby);
			a.render(r, ds);
			b.render(r, ds);
		}
		
		@Override
		public boolean click() {
			if (h != null && h instanceof CLICKABLE) {
				((CLICKABLE) h).click();
				return true;
			}
			return super.click();
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			if (h != null && h instanceof HOVERABLE)
				((HOVERABLE) h).hoverInfoGet(text);
			super.hoverInfoGet(text);
		}



	}
	

}
