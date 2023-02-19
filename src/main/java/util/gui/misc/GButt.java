package util.gui.misc;

import init.C;
import init.sprite.ICON;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.*;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Font;
import snake2d.util.sprite.text.Text;
import util.colors.GCOLOR;
import util.info.INFO;

/**
 * A clickable button
 * @author mail__000
 *
 */
public abstract class GButt extends CLICKABLE.ClickableAbs{
	
	protected SPRITE label;
	
	protected GButt(SPRITE r){
		this.label = r;
	}

	public GButt replaceLabel(SPRITE label, DIR d){
		//d.reposition(body, label.getWidth(), label.getHeight());
		this.label = label;
		return this;
	}
	
	public GButt hoverSet(INFO info) {
		hoverTitleSet(info.name);
		hoverInfoSet(info.desc);
		return this;
	}
	
	public static class Base extends GButt{
		
		protected final LIST<SPRITE> sprite;
		protected int labelXOff = 0;
		protected int labelYOff = 0;
		
		public Base(LIST<SPRITE> sprite, SPRITE label){
			super(label);
			
			int w = sprite.get(0).width();
			int h = sprite.get(0).height();
			
			body.setWidth(w > label.width() ? w : label.width());
			body.setHeight(h > label.height() ? h : label.height());
			labelXOff = (w - label.width())/2;
			labelYOff = (h - label.height())/2;
			this.sprite = sprite;
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive,
				boolean isSelected, boolean isHovered) {
			renAction();
			int x = body.x1();
			int y = body.y1();
			
			if (isSelected && isHovered){
				sprite.get(3).render(r, x, y);
				COLOR.WHITE200.bind();
			}
			else if (isSelected){
				sprite.get(2).render(r, x, y);
				COLOR.WHITE150.bind();
			}else if (isHovered){
				sprite.get(1).render(r, x, y);
				COLOR.WHITE150.bind();
			}else if (isActive){
				sprite.get(0).render(r, x, y);
				COLOR.WHITE100.bind();
			}else{
				sprite.get(0).render(r, x, y);
				GCOLOR.T().INACTIVE.bind();
			}
			label.render(r, x + labelXOff, y + labelYOff);
			COLOR.unbind();
			
		}
	}
	

	
	public static class Panel extends GButt{
		
		private final SPRITE bg;
		
		public Panel(CharSequence label){
			super(UI.FONT().M.getText(label));
			bg = UI.PANEL().buttBG.get(0);
			body.setDim(bg);
		}
		
		public Panel(SPRITE label){
			super(label);
			
			if (label.width() >= UI.PANEL().buttBG.get(2).width()-4) {
				if (label.width() >= UI.PANEL().buttBG.get(1).width()-4) {
					bg = UI.PANEL().buttBG.get(0);
				}else {
					bg = UI.PANEL().buttBG.get(1);
				}
			}else {
				bg = UI.PANEL().buttBG.get(2);
			}
			body.setDim(bg);
				
			
		}
		
		public Panel(SPRITE label, CharSequence hovInfo){
			super(label);
			
			if (label.width() >= UI.PANEL().buttBG.get(2).width()-4) {
				if (label.width() >= UI.PANEL().buttBG.get(1).width()-4) {
					bg = UI.PANEL().buttBG.get(0);
				}else {
					bg = UI.PANEL().buttBG.get(1);
				}
			}else {
				bg = UI.PANEL().buttBG.get(2);
			}
			body.setDim(bg);
				
			hoverInfoSet(hovInfo);
		}


		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			renAction();
			
			if (isSelected ||isHovered) {
				if (isSelected & isHovered) {
					OPACITY.O25To50.bind();
				}else {
					OPACITY.O25.bind();
				}
				
				bg.render(r, body().x1(), body().y1());
				OPACITY.unbind();
			}
			
			if (!isActive)
				GCOLOR.T().INACTIVE.bind();
			else if (isSelected && isHovered)
				GCOLOR.T().HOVER_SELECTED.bind();
			else if (isSelected)
				GCOLOR.T().SELECTED.bind();
			else if (isHovered)
				GCOLOR.T().HOVERED.bind();
			else
				COLOR.WHITE100.bind();
			label.renderC(r, body());
			COLOR.unbind();
			

			
			
		}
	}
	
	public static class Icon extends GButt{
		
		public Icon(SPRITE label){
			super(label);
			body.setWidth(label.width());
			body.setHeight(label.height());
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive,
				boolean isSelected, boolean isHovered) {
			renAction();
			int x = body.x1();
			int y = body.y1();
			
			if (isSelected && isHovered){
				COLOR.WHITE200.bind();
			}
			else if (isSelected){
				COLOR.WHITE150.bind();
			}else if (isHovered){
				COLOR.WHITE150.bind();
			}else if (isActive){
				COLOR.WHITE100.bind();
			}else{
				GCOLOR.T().INACTIVE.bind();
			}
			label.render(r, x, y);
			COLOR.unbind();
			
		}
	}
	
	public static class Glow extends GButt{
		
		protected final SPRITE bg;
		private COLOR normal = COLOR.WHITE100;
		
		public Glow(SPRITE label){
			this(label, null);
			body.setHeight(body.height()+C.SG*6);
			body.incrW(4);
		}
		
		public Glow(CharSequence text){
			this((SPRITE)new Text(UI.FONT().S, text));
			body.setHeight(body.height()+C.SG*6);
		}
		
		public Glow(SPRITE label, SPRITE bg){
			super(label);
			body.setWidth(label.width());
			body.setHeight(label.height());
			if (bg != null) {
				if (bg.width() > body.width())
					body.setWidth(bg.width());
				if (bg.height() > body.height())
					body.setHeight(bg.height());
			}
			this.bg = bg;
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive,
				boolean isSelected, boolean isHovered) {
			renAction();
			int x = body.x1();
			int y = body.y1();
			if (bg != null) {
				bg.render(r, x, y);
				x += (body.width()-label.width())/2;
			}
			
			if (!isActive)
				GCOLOR.T().INACTIVE.bind();
			else if (isSelected && isHovered)
				GCOLOR.T().HOVER_SELECTED.bind();
			else if (isSelected)
				GCOLOR.T().SELECTED.bind();
			else if (isHovered)
				GCOLOR.T().HOVERED.bind();
			else
				normal.bind();
			y += (body.height()-label.height())/2;
			label.render(r, x, y);
			COLOR.unbind();
			
		}
		
		public void color(COLOR color) {
			this.normal = color;
		}
	}
	
	public static abstract class BText extends Glow{
		
		private final Text text;
		private DIR d;
		
		public BText(Font f, int max, DIR d){
			super((SPRITE)new Text(f, max), null);
			this.text = (Text) label;
			this.d = d;
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive,
				boolean isSelected, boolean isHovered) {
			renAction();
			
			update(text);
			text.adjustWidth();
			d.reposition(body, text.width(), text.height());
			
			super.render(r, ds, isActive, isSelected, isHovered);
			
		}
		
		protected abstract void update(Text text);
	}
	
	public static class Standalone extends Glow{
		
		public Standalone(SPRITE label){
			super(label, UI.PANEL().panelL.get(0));
		}
	}
	
	public static class Checkbox extends GButt{
		
		protected final LIST<ICON.SMALL> sprite = UI.PANEL().buttCheckbox;
		
		public Checkbox(){
			super(null);
			int w = sprite.get(0).width();
			int h = sprite.get(0).height();
			body.setDim(w,h);
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive,
				boolean isSelected, boolean isHovered) {
			renAction();
			int x = body.x1();
			int y = body.y1();
			render(r, x, y, isActive, isSelected, isHovered);
			
			
			
		}
		
		public static void render(SPRITE_RENDERER r, int x, int y, boolean isActive,
				boolean isSelected, boolean isHovered) {
			LIST<ICON.SMALL> sprite = UI.PANEL().buttCheckbox;
			if (isSelected && isHovered){
				sprite.get(3).render(r, x, y);
			}
			else if (isSelected){
				sprite.get(2).render(r, x, y);
			}else if (isHovered){
				sprite.get(1).render(r, x, y);
			}else if (isActive){
				sprite.get(0).render(r, x, y);
			}else{
				GCOLOR.T().INACTIVE.bind();
				sprite.get(0).render(r, x, y);
				COLOR.unbind();
			}
		}
	}
	
	public static class CheckboxTitle extends GButt{
		
		private static int labelXOff = (int) (ICON.SMALL.SIZE)/2;
		private final static String on = "turn off";
		private final static String off = "turn on";
		
		public CheckboxTitle(SPRITE label){
			super(label);
			body.setWidth(sprite(0).width() + labelXOff + label.width());
			body.setHeight(sprite(0).height() >= label.height() ? sprite(0).height() : label.height());
		}
		
		public CheckboxTitle(CharSequence text) {
			this((SPRITE)new GText(UI.FONT().S, text).lablify());
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive,
				boolean isSelected, boolean isHovered) {
			
			int y = body.y1();
			label.render(r, body.x1(), y+(body().height()-label.height())/2);
			y += (body().height()-sprite(0).height())/2;
			
			int x = body.x1()+labelXOff + label.width();
			
			renAction();
			if (!isActive){
				GCOLOR.T().INACTIVE.bind();
				sprite(0).render(r, x, y);
				COLOR.unbind();
			}else if (isSelected && isHovered){
				sprite(3).render(r, x, y);
			}
			else if (isSelected){
				sprite(2).render(r, x, y);
			}else if (isHovered){
				sprite(1).render(r, x, y);
			}else{
				sprite(0).render(r, x, y);
			}
			//label.render(r, x + sprite(0).width() + labelXOff, y+(body().height()-label.height())/2);
			
		}
		
		private static SPRITE sprite(int i) {
			return UI.PANEL().buttCheckbox.get(i);
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			if (hoverTitle != null)
				text.title(hoverTitle);
			if (hoverInfo != null)
				text.text(hoverInfo);
			else
				text.text(selectedIs() ? on : off);
		}
	}
	
	
	public static class BStat2 extends GButt {
		
		private final GStat stat;
		private COLOR color = COLOR.WHITE25;
		
		public BStat2(SPRITE icon, GStat stat) {
			super(icon);
			this.stat = stat;
			body.setWidth(icon.width() + stat.height()*4);
			body.setHeight(icon.height()+8);
		}
		
		public BStat2(CharSequence title, GStat stat) {
			super(new GText(UI.FONT().S, title).lablify());
			this.stat = stat;
			body.setWidth(label.width()+8 + stat.height()*4);
			body.setHeight(label.height()+stat.height()+8);
		}

		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			
			renAction();
			GCOLOR.UI().border().render(r, body());
			GCOLOR.UI().bg().render(r, body(), -3);
			
			if (isSelected)
				COLOR.WHITE85.render(r, body, -2);
			else if (isHovered)
				COLOR.WHITE50.render(r, body, -2);
			
			if (isHovered) {
				OPACITY.O100.bind();
			}else if(isActive) {
				OPACITY.O50.bind();
			}else {
				OPACITY.O012.bind();
			}
			
			if (isHovered || isSelected) {
				OPACITY.O100.bind();
			}else if(isActive) {
				OPACITY.O50.bind();
			}else {
				OPACITY.O012.bind();
			}
			color.render(r, body(), -4);
			OPACITY.unbind();
			stat.adjust();
			label.renderCY(r, body().x1()+4, body.cY());
			if (label instanceof GText) {
				
				stat.renderCY(r, body().x1()+4+label.width()+2, body().cY());
			}else {
				stat.renderCY(r, body().x1()+4+label.width()+2, body().cY());
			}
			
			
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			stat.hoverInfoGet((GBox) text);
			super.hoverInfoGet(text);
		}
		
		public BStat2 bg(COLOR c) {
			this.color = c;
			return this;
		}
		
		public void bgClear() {
			color = COLOR.WHITE25;
		}
		
		public BStat2 setWidth(int width) {
			this.body.setWidth(width);
			return this;
		}
	}
	
	public static class ButtPanel extends GButt{
		
		private final int M = 4;
		private COLOR color = COLOR.WHITE35;
		private SPRITE icon;
		private DIR align = DIR.C;
		
		public ButtPanel(CharSequence label){
			this(UI.FONT().H2.getText(label));
			
		}
		
		public ButtPanel(SPRITE label){
			super(label);
			body.setDim(this.label.width() + M*2, this.label.height() + M*2);
		}
		
		public ButtPanel setDim(int width, int height) {
			this.body.setDim(width, height);
			return this;
		}
		
		public ButtPanel setDim(int width) {
			this.body.setWidth(width);
			return this;
		}
		
		public ButtPanel pad(int x, int y) {
			this.body.incrW(x*2);
			this.body.incrH(y*2);
			return this;
		}


		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			renAction();
			COLOR border = GCOLOR.UI().border();
			border.render(r, body());
			GCOLOR.UI().bg().render(r, body(), -1);
			
			if (isSelected)
				COLOR.WHITE85.render(r, body, -2);
			else if (isHovered)
				COLOR.WHITE50.render(r, body, -2);
			
			GCOLOR.UI().bg().render(r, body(), -4);
			
			if (!isActive) {
				OPACITY.O0.bind();
			}else if (isHovered || isSelected) {
				OPACITY.O100.bind();
			}else {
				OPACITY.O35.bind();
				
			}
			color.render(r, body(), -4);
			OPACITY.unbind();
			
			
			if (!isActive)
				GCOLOR.T().INACTIVE.bind();
			else if (isSelected && isHovered)
				GCOLOR.T().HOVER_SELECTED.bind();
			else if (isSelected)
				GCOLOR.T().SELECTED.bind();
			else if (isHovered)
				GCOLOR.T().HOVERED.bind();
			else if (label instanceof Text) {
				GCOLOR.T().H1.bind();
			}else {
				COLOR.WHITE100.bind();
			}
			if (icon != null) {
				label.renderC(r, body().x1()+icon.width()+12+label.width()/2, body().cY());
				COLOR.unbind();
				icon.renderC(r, body().x1()+icon.width()/2+6, body().cY());
				
			}else {
				int dx = align.x()*(body().width()-label.width()-8)/2;
				int dy = align.y()*(body().height()-label.height()-8)/2;
				label.renderC(r, body.cX()+dx, body().cY()+dy);
				COLOR.unbind();
			}
			
			
			

			
			
		}
		
		public ButtPanel align(DIR d) {
			this.align = d;
			return this;
		}
		
		public ButtPanel bg(COLOR c) {
			this.color = c;
			return this;
		}
		
		public ButtPanel bgClear() {
			this.color = COLOR.WHITE35;
			return this;
		}
		
		public ButtPanel icon(SPRITE icon) {
			this.icon = icon;
			int w = icon.width()+16+label.width();
			if (body().width() < w)
				body().setWidth(w);
			
			return this;
		}
		
		@Override
		public Rec body() {
			return body;
		}
	}
	
	
	public static class ButtPanelCheck extends GButt{
		
		public ButtPanelCheck(){
			super(SPRITES.icons().s.cancel);
		}
		
		public ButtPanelCheck setDim(int width, int height) {
			this.body.setDim(width, height);
			return this;
		}
		
		public ButtPanelCheck setDim(int width) {
			this.body.setWidth(width);
			return this;
		}
		
		public ButtPanelCheck pad(int x, int y) {
			this.body.incrW(x*2);
			this.body.incrH(y*2);
			return this;
		}

		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			renAction();
			COLOR border = GCOLOR.UI().border();
			border.render(r, body());
			GCOLOR.UI().bg().render(r, body(), -1);
			
			if (isSelected)
				COLOR.WHITE85.render(r, body, -2);
			else if (isHovered)
				COLOR.WHITE50.render(r, body, -2);
			
			GCOLOR.UI().bg().render(r, body(), -4);
			
			
			label = SPRITES.icons().s.cancel;
			COLOR col = GCOLOR.UI().BAD.get(isActive, isSelected, isHovered);
			
			if (isSelected) {
				label = SPRITES.icons().s.allRight;
				col = GCOLOR.UI().GOOD.get(isActive, isSelected, isHovered);
			}
			col.bind();
			label.renderC(r, body.cX(), body().cY());
			COLOR.unbind();
			
			
		}
		
		@Override
		public Rec body() {
			return body;
		}
	}
	
	public static class BSection extends GuiSection {
		
		private boolean selectedIs;
		private boolean link = false;
		public static final int M = 2;
		private CharSequence hov = null;
		
		
		public BSection(int width, int height) {
			this.body().setWidth(width).setHeight(height);
		}
		
		public BSection() {

		}
		
		public BSection setAsLink() {
			link = true;
			return this;
		}
		
		public void pad() {
			super.pad(4);
		}
		
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			renAction();
			if (visableIs()) {
				
				renderBG(r, body(), activeIs(), hoveredIs(), selectedIs());
				
				boolean hov = hoveredIs();
				super.render(r, ds);
				
				if (!activeIs()) {
					OPACITY.O25.bind();
					COLOR.BLACK.render(r, body(), -1);
					OPACITY.unbind();
				}else if (hov && link) {
					SPRITES.icons().m.arrow_right.render(r, body().x2()-ICON.MEDIUM.SIZE-6, body().y1());
				}
				
				
			}
			
			
		}
		
		public static void renderBG(SPRITE_RENDERER r, RECTANGLE body, boolean isActive, boolean isHovered, boolean isSelected) {
			COLOR border = GCOLOR.UI().border();
			border.render(r, body);
			GCOLOR.UI().bg().render(r, body, -1);
			
			if (isSelected)
				COLOR.WHITE85.render(r, body, -2);
			else if (isHovered)
				COLOR.WHITE50.render(r, body, -2);
			
			GCOLOR.UI().bg().render(r, body, -4);
			
			if (!isActive) {
				OPACITY.O0.bind();
			}else if (isHovered || isSelected) {
				OPACITY.O100.bind();
			}else {
				OPACITY.O35.bind();
				
			}
			GCOLOR.UI().bg(isActive, isSelected, isHovered).render(r, body, -4);
			OPACITY.unbind();
			
		}
		
		protected void renAction() {
			
		}
		
		@Override
		public GuiSection selectedSet(boolean yes) {
			selectedIs = yes;
			return super.selectedSet(yes);
		}
		
		public void selectOnlythis(boolean yes) {
			selectedIs = yes;
		}
		
		@Override
		public boolean selectedIs() {
			return selectedIs;
		}
		
		@Override
		public GuiSection hoverInfoSet(CharSequence s) {
			hov = s;
			return this;
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			if (hov != null)
				text.text(hov);
			super.hoverInfoGet(text);
		}
		
	}
	
	

	
}
