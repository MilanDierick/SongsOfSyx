package game.tourism;

import java.io.IOException;
import java.io.Serializable;

import game.time.TIME;
import init.race.Race;
import init.sprite.ICON;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.entity.humanoid.HCLASS;
import settlement.main.SETT;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.service.module.ROOM_SERVICER;
import settlement.room.service.module.RoomServiceDataAccess.ROOM_SERVICE_ACCESS_HASER;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.StatsNeeds.StatNeed;
import settlement.stats.StatsService.StatService;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LIST;
import snake2d.util.sets.Tuple;
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
		for (Tuple<StatNeed, LIST<ROOM_SERVICE_ACCESS_HASER>> n : TOURISM.self.needs) {
			if (n.a().getPrio(indu) > 0) {
				tmp.add(da.service.get(0, indu, null, n.b().rnd()));
				tmp.s();
				return;
			}
		}
		
		double v = 0;
		double m = 0;
		ROOM_SERVICE_ACCESS_HASER res = null;
		for (Tuple<StatNeed, LIST<ROOM_SERVICE_ACCESS_HASER>> n : TOURISM.self.needs) {
			for (ROOM_SERVICE_ACCESS_HASER ha : n.b()) {
				StatService ser = ha.service().stats();
				double t = ser.total(indu);
				if (t > v) {
					v = t;
					res = ha;
				}
				m = Math.max(ser.total().standing().definition(indu.race()).get(HCLASS.CITIZEN).max, m);
			}
		}
		
		if (res != null) {
			tmp.add(da.service.get(CLAMP.d(v+RND.rFloat0(0.15), 0, 1), indu, null, res));
			v = v*(res.service().stats().total().standing().definition(indu.race()).get(HCLASS.CITIZEN).max/m);
			
			score += v;
			tmp.s();
		}else
			tmp.add(da.service.get(0, indu, null, TOURISM.self.needs.rnd().b().rnd()));
		
	}
	
	public int render(SPRITE_RENDERER r, int x1, int y1, int width) {
		
		{
			int icons = (int) (1 + 4*score);
			int x = x1 + width/2 - ICON.SMALL.SIZE*icons/2;
			for (int i = 0; i < icons; i++) {
				SPRITES.icons().s.star.render(r, x, y1);
				x+= ICON.SMALL.SIZE;
			}
			
			Str.TMP.clear().add(credits);
			x = x1+width-128;
			SPRITES.icons().s.money.render(r, x, y1);
			GCOLOR.T().H1.bind();
			UI.FONT().S.render(r, Str.TMP, x+6+ICON.MEDIUM.SIZE, y1);
			COLOR.unbind();
			
			y1 += ICON.SMALL.SIZE + 4;
			
		}
		
		GCOLOR.T().NORMAL2.bind();
		y1 += 4 + UI.FONT().M.render(r, desc, x1, y1, width, 1);
		GCOLOR.T().H2.bind();
		y1 += UI.FONT().S.render(r, name, x1+30, y1, width-30, 1);
		COLOR.unbind();
	
		
		
		return y1;
		
	}


	
}