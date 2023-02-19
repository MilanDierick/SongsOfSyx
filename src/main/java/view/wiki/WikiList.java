package view.wiki;

import init.C;
import init.sprite.UI.UI;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayList;
import snake2d.util.sprite.text.StringInputSprite;
import util.data.GETTER;
import util.gui.misc.*;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import view.main.VIEW;

class WikiList extends GuiSection{

	private final StringInputSprite filter = new StringInputSprite(20, UI.FONT().M){
		@Override
		protected void change() {
			filter();
		};
	}.placeHolder("Search");
	private final GInput fc = new GInput(filter);
	private final ArrayList<Article> all;
	private final ArrayList<Article> filtered;
	public final static int width = 400;
	private final GTableBuilder builder;
	
	WikiList(ArrayList<Article> all){
		int cats = 0;
		CharSequence  lastCat = null;
		for (Article e : all) {
			if (lastCat == null || !e.category.equals(lastCat)) {
				lastCat = e.category;
				cats++;
			}
		}
		this.all = new ArrayList<>(all.size()+cats);
		lastCat = null;
		for (Article e : all) {
			if (lastCat == null || !e.category.equals(lastCat)) {
				this.all.add((Article) null);
				lastCat = e.category;
			}
			this.all.add(e);
		}
		filtered = new ArrayList<>(this.all);
		
		
		fc.body().centerX(0, width-8);
		add(fc, 4, 0);
		
		builder = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				return filtered.size();
			}
			
			@Override
			public void click(int index) {
				Article e = filtered.get(index);
				if (e != null)
					VIEW.inters().wiki.set(e);
				
			}
			
			@Override
			public boolean selectedIs(int index) {
				Article a = filtered.get(index);
				if (a == null)
					return false;
				return (VIEW.inters().wiki.added().size() > 0 && VIEW.inters().wiki.added().get(0) == a);
				
			}
		};
		
		builder.column(null, width-C.SG*24-8, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new GStat() {
					@Override
					public void update(GText text) {
						Article e = filtered.get(ier.get());
						if (e == null) {
							text.lablify().s().add(filtered.get(ier.get()+1).category).setFont(UI.FONT().H2);
						}else {
							text.normalify().s().s().add(e.title).setFont(UI.FONT().M);
						}
						text.setMaxWidth(width);
						
					}
				}.increase().r();
			}
		});
		

		
		RENDEROBJ o = builder.createHeight(C.HEIGHT()-Top.HEIGHT-10-fc.body.height(), true);
		

		
		o.body().moveY1(fc.body().y2()+5);
		add(o);
		pad(4, 3);
		
	}
	
	void setList(Article a) {
		filter.text().clear();
		filter();
		
		for (int i = 0; i < all.size(); i++) {
			if (all.get(i) == a) {
				builder.set(i-5);
				return;
			}
			
		}
		
	}
	
	void set(Article e) {
		filter.text().clear();
		filter();
		if (e == null)
			fc.focus();
	}
	
	private void filter() {
		filtered.clear();
		CharSequence f = filter.text();
		if (f.length() == 0) {
			filtered.add(all);
			return;
		}
		
		for (int i = 0; i < all.size(); i++) {
			Article e = all.get(i);
			if (e == null)
				continue;
			CharSequence t = e.key;
			if (testFilter(f, t)) {
				if (filtered.isEmpty() || !filtered.get(filtered.size()-1).category.equals(e.category))
					filtered.add((Article)null);
				filtered.add(all.get(i));
			}
		}
		
	}
	
	private boolean testFilter(CharSequence filter, CharSequence key) {

		
		outer:
		for (int ti = 0; ti < key.length(); ti++) {
			if (Character.toLowerCase(filter.charAt(0)) == Character.toLowerCase(key.charAt(ti))) {
				for (int i = 1; i < filter.length(); i++) {
					int k = i+ti;
					if (k >= key.length())
						return false;
					if (Character.toLowerCase(filter.charAt(i)) != Character.toLowerCase(key.charAt(k)))
						continue outer;
				}
				return true;
			}
		}
		
		return false;
	}
	
}
