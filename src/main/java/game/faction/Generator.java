package game.faction;

import init.paths.PATHS;
import init.sprite.BitmapSprite;
import snake2d.util.color.COLOR;
import snake2d.util.file.Json;
import snake2d.util.rnd.RND;

final class Generator {

	public Generator() {
		
		
		setColors();
		
		
		
		Json json = new Json(PATHS.NAMES().get("Factions"));
		String[] s = json.texts("NAMES", 1, 10000);
		for (int i = 0; i < s.length; i++) {
			int k = RND.rInt(s.length);
			String n = s[i];
			s[i] = s[k];
			s[k] = n;
		}
		
		int k = 0;
		for (Faction f : FACTIONS.all()) {
			f.appearence().name().clear().add(s[k++]);
			k %= s.length;
		}
		
		{
			
			

			short[][] datas = BitmapSprite.read(PATHS.SPRITE().getFolder("ui").get("FactionBanners"));

			for (int i = 0; i < datas.length; i++) {
				int i2 = RND.rInt(datas.length);
				short[] old = datas[i];
				datas[i] = datas[i2];
				datas[i2] = old;
			}
			
			int ki = 0;
			for (Faction f : FACTIONS.all()) {
				f.banner().sprite.paint(datas[ki++]);
				ki = ki % datas.length;
			}
			
		}
		
		
		
	}
	
	private void setColors() {
		COLOR[] cols = COLOR.generateUnique(FACTIONS.MAX);
		
		for (int i = 0; i < cols.length; i++) {
			int k = RND.rInt(cols.length);
			COLOR n = cols[i];
			cols[i] = cols[k];
			cols[k] = n;
		}
		
		int i = 0;
		for (Faction f : FACTIONS.all()) {
			
			f.banner().colorFG().set(cols[(i + cols.length/2)%cols.length]);
			f.banner().colorBG().set(cols[i%cols.length]);
			i++;
		}
	}
	
}
