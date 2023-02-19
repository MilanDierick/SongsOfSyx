package launcher;


import java.util.ArrayList;

import snake2d.*;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Rec;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.ACTION;
import snake2d.util.sprite.SPRITE;

public class ScrollBox implements CLICKABLE{

	private final ArrayList<HOVERABLE> objects = new ArrayList<HOVERABLE>();
	private CLICKABLE hovered = null;
	private int first = 0;
	private int last = -1;
	private boolean visable = true;
	private final Bounds bounds = new Bounds();
	
	private CLICKABLE upButt;
	private CLICKABLE downButt;
	
	public ScrollBox(int height){
		bounds.setHeight(height);
	}
	
	public ScrollBox addNavButts(CLICKABLE up, CLICKABLE down){
		upButt = up;
		downButt = down;
		fixButts();
		return this;
	}
	
	private void fixButts(){
		
		if (upButt != null)
			upButt.activeSet(canUp());
		if (downButt != null)
			downButt.activeSet(canDown());
	}
	
	public void scrollUp(){
		if (!canUp())
			return;
		int dy = objects.get(last-1).body().height();
		first--;
		for (int i = 0; i < objects.size(); i++){
			objects.get(i).body().incrY(dy);
			if (objects.get(i).body().y2() <= bounds.y2())
				last = i;
		}
		fixButts();
		
	}
	
	public void scrollDown(){
		if (!canDown())
			return;
		int dy = -objects.get(first).body().height();
		first++;
		for (int i = 0; i < objects.size(); i++){
			objects.get(i).body().incrY(dy);
			if (objects.get(i).body().y2() <= bounds.y2())
				last = i;
		}
		fixButts();
	}
	
	public int size(){
		return objects.size();
	}
	
	public void centreAtIndex(int index){
		int currentCentre = first + (last - first)/2;
		while(currentCentre < index && canDown()){
			scrollDown();
			currentCentre = first + (last - first)/2;
		}
		while(currentCentre > index && canUp()){
			scrollUp();
			currentCentre = first + (last - first)/2;
		}
		fixButts();
	}
	
	public void centreAtGuiObj(CLICKABLE g){
		
		int i = 0;
		for (HOVERABLE o : objects){
			if (o == g){
				centreAtIndex(i);
				return;
			}
			i++;
		}
		fixButts();
	}
	
	public float getCentrePosition(){
		float eleHeight = last - first;
		float tot = objects.size() - eleHeight -1;
		if (tot <= 0)
			return 0;
		float res = first/tot;
		return res;
	}
	
	public void clear(){
		first = 0;
		last = -1;
		objects.clear();
		hovered = null;
		fixButts();
	}
	
	public boolean canUp(){
		return first > 0;
	}
	
	public boolean canDown(){
		return last < objects.size() -1;
	}
	
	public int add(CLICKABLE object){
		if (objects.isEmpty()){
			object.body().moveX1Y1(bounds.x1(), bounds.y1());
		}else{
			object.body().moveX1Y1(bounds.x1(), objects.get(objects.size()-1).body().y2());
		}
		
		int i = objects.size();
		objects.add(object);
		
		if (object.body().y2() <= bounds.y2())
			last = objects.size() - 1;
		
		if (object.body().width() > bounds.width())
			bounds.setWidth(object.body().width());
		fixButts();
		return i;
	}
	
	public void add(SPRITE s){
		HOVERABLE.Sprite r = new HOVERABLE.Sprite(s);
		if (objects.isEmpty()){
			r.body().moveX1Y1(bounds.x1(), bounds.y1());
		}else{
			r.body().moveX1Y1(bounds.x1(), objects.get(objects.size()-1).body().y2());
		}
		objects.add(r);
		
		if (r.body().y2() <= bounds.y2())
			last = objects.size() -1;
		fixButts();
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		
		double s = MButt.clearWheelSpin();
		while (s < 0 && canDown()){
			scrollDown();
			s++;
		}
		while (s > 0 && canUp()){
			scrollUp();
			s--;
		}
		
		for (int i = first; i <= last; i++){
			objects.get(i).render(r, ds);
		}
		
		
	}

	@Override
	public boolean hover(COORDINATE mCoo) {
		
		if (hovered != null && hovered.hover(mCoo)){
			return true;
		}else{
			hovered = null;
		}
		
		CLICKABLE g;
		
		for (int i = first; i <= last; i++){
			if (objects.get(i) instanceof CLICKABLE){
				g = (CLICKABLE) objects.get(i);
				if (g.hover(mCoo)){
					hovered = (CLICKABLE) objects.get(i);
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public boolean hoveredIs() {
		return (hovered != null && hovered.hoveredIs());
	}

	@Override
	public boolean click() {
		return (hovered != null && hovered.click());
	}

	@Override
	public void hoverInfoGet(GUI_BOX text) {
		if (hovered != null && hovered.hoveredIs())
				hovered.hoverInfoGet(text);
	}
	
	@Override
	public CLICKABLE hoverTitleSet(CharSequence s) {
		
		return null;
	}

	@Override
	public ScrollBox activeSet(boolean activate) {
		for (HOVERABLE g : objects){
			if (g instanceof CLICKABLE)
				((CLICKABLE)g).activeSet(activate);
		}
		return this;
	}

	@Override
	public ScrollBox hoverSoundSet(SoundEffect sound) {
		for (HOVERABLE g : objects){
			if (g instanceof CLICKABLE)
				((CLICKABLE)g).hoverSoundSet(sound);
		}
		return this;
	}

	@Override
	public ScrollBox clickSoundSet(SoundEffect sound) {
		for (HOVERABLE g : objects){
			if (g instanceof CLICKABLE)
				((CLICKABLE)g).clickSoundSet(sound);
		}
		return this;
	}

	@Override
	public ScrollBox selectedSet(boolean yes) {
		for (HOVERABLE g : objects){
			if (g instanceof CLICKABLE)
				((CLICKABLE)g).selectedSet(yes);
		}
		return this;
	}

	@Override
	public boolean selectedIs() {
		for (HOVERABLE g : objects){
			if (g instanceof CLICKABLE)
				if (((CLICKABLE)g).selectedIs())
					return true;
		}
		return false;
	}

	@Override
	public boolean activeIs() {
		return false;
	}

	@Override
	public ScrollBox visableSet(boolean yes) {
		visable = yes;
		return this;
	}

	@Override
	public boolean visableIs() {
		return visable;
	}

//	@Override
//	public void setHovered(boolean hovered) {
//		// TODO Auto-generated method stub
//		
//	}

	@Override
	public ScrollBox selectedToggle() {
		// TODO Auto-generated method stub
		return this;
	}
	
	private class Bounds extends Rec{

		private static final long serialVersionUID = 1L;

		@Override
		public Rec moveX1(double X1) {
			
			double dx = X1 - x1();
			for (HOVERABLE g : objects){
				g.body().incrX(dx);
			}
			return super.moveX1(X1);
		}
		
		@Override
		public Rec moveY1(double Y1) {
			double dy = Y1 - y1();
			for (HOVERABLE g : objects){
				g.body().incrY(dy);
			}
			return super.moveY1(Y1);
		}
	}

	@Override
	public Rec body() {
		return bounds;
	}

	@Override
	public CLICKABLE clickActionSet(ACTION f) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CLICKABLE selectTmp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CLICKABLE hoverInfoSet(CharSequence s) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
