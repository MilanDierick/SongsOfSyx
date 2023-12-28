package game.tourism;

import java.io.IOException;
import java.io.Serializable;

import game.time.TIME;
import init.race.Race;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import settlement.main.SETT;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.service.module.ROOM_SERVICER;
import settlement.room.service.module.RoomServiceAccess;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.Str.StringReusableSer;
import util.colors.GCOLOR;
import util.dic.DicTime;

public final class Review implements Serializable, SAVABLE{
	
	private static final long serialVersionUID = 1L;
	
	public Review() {
		
	}
	
	public final StringReusableSer name = new StringReusableSer(64);
	public final StringReusableSer desc = new StringReusableSer(128);
	public double score = 0;
	public int credits;
	
	private static final Str tmp = new Str(128);
	
	@Override
	public void save(FilePutter file) {
		name.save(file);
		desc.save(file);
		file.d(score);
		file.i(credits);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		name.load(file);
		desc.load(file);
		score = file.d();
		credits = file.i();
	}
	
	public void copyOther(Review other) {
		name.clear().add(other.name);
		desc.clear().add(other.desc);
		score = other.score;
		credits = other.credits;
	}
	
	public boolean has() {
		return name.length() > 0;
	}
	
	@Override
	public void clear() {
		name.clear();
		score = 0;
	}

	public void make(Induvidual indu, COORDINATE inn) {
		
		name.clear();
		name.s().add('/').s();
		name.add(STATS.APPEARANCE().name(indu));
		name.NL();
		name.add(DicTime.setDate(Str.TMP.clear(), (int) TIME.currentSecond()));
		
		
		
		tmp.clear();
		desc.clear();
		score = 0;
		Race race = indu.race();
		credits = 0;
		Text da = race.tourism().data;
		if (!SETT.ROOMS().INN.is(inn)) {
			desc.add(da.rating.get(0, indu, inn, null));
			desc.s();
			desc.add(da.inn.get(0, indu, inn, null));
			return;
		}
	
		{
			RoomBlueprintImp a = TOURISM.attraction(indu);
			double s = CLAMP.d((a.employment().employed()-TOURISM.MIN_EMPLOYEES)/(double)TOURISM.MAX_EMPLOYEES, 0, 1);
			score += s;
			tmp.add(da.attraction.get(s+RND.rFloat0(0.15), indu, inn, null));
			tmp.s();
		}
		
		setService(indu, tmp);
		tmp.s();
		
		ROOM_SERVICER in = (ROOM_SERVICER) SETT.ROOMS().INN.get(inn);
		score += in.quality();
		tmp.add(da.inn.get(0.35 + 0.65*in.quality(), indu, inn, null));
		
		score /= 3;
		
		credits = (int) (score*race.tourism().credits*TOURISM.CREDITS*RND.rFloat1(0.2));
		score += RND.rFloat0(0.2);
		score = CLAMP.d(score, 0, 1);
		
		desc.add(da.rating.get(score, indu, inn, null));
		desc.s();
		desc.add(tmp);
		
	}
	
	private void setService(Induvidual indu, Str tmp) {
		Text da = indu.race().tourism().data;
		
		RoomServiceAccess s = Updater.getService(indu);
		if (s != null) {
			if (s.stats().total(indu) == 0) {
				tmp.add(da.service.get(0, indu, null, s));
				tmp.s();
				return;
			}
			
		}
		
	}
	
	public int render(SPRITE_RENDERER r, int x1, int y1, int width) {
		
		{
			int icons = (int) (1 + 4*score);
			int x = x1 + width/2 - Icon.S*icons/2;
			for (int i = 0; i < icons; i++) {
				SPRITES.icons().s.star.render(r, x, y1);
				x+= Icon.S;
			}
			
			Str.TMP.clear().add(credits);
			x = x1+width-128;
			SPRITES.icons().s.money.render(r, x, y1);
			GCOLOR.T().H1.bind();
			UI.FONT().S.render(r, Str.TMP, x+6+Icon.M, y1);
			COLOR.unbind();
			
			y1 += Icon.S + 4;
			
		}
		
		GCOLOR.T().NORMAL2.bind();
		y1 += 4 + UI.FONT().M.render(r, desc, x1, y1, width, 1);
		GCOLOR.T().H2.bind();
		y1 += UI.FONT().S.render(r, name, x1+30, y1, width-30, 1);
		COLOR.unbind();
	
		
		
		return y1;
		
	}


	
}