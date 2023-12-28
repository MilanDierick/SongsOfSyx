package settlement.entity.animal;

import java.io.IOException;

import game.boosting.BOOSTABLES;
import game.boosting.BOOSTABLES.BDamage;
import init.C;
import init.biomes.*;
import init.paths.PATH;
import init.paths.PATHS;
import init.resources.*;
import init.resources.RBIT.RBITImp;
import init.sound.SOUND;
import init.sound.SoundSettlement;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import snake2d.Errors;
import snake2d.Renderer;
import snake2d.util.color.*;
import snake2d.util.file.Json;
import snake2d.util.sets.*;
import snake2d.util.sprite.TILE_SHEET;
import util.info.INFO;
import util.keymap.KEY_COLLECTION;
import util.keymap.RCollection;
import util.rendering.ShadowBatch;
import util.spritecomposer.*;

public class AnimalSpecies extends INFO implements INDEXED{
	
	public static final int SIZE = 24*C.SCALE;
	private final transient double massMin;
	private final transient double heightOverGround;
	private final transient double acceleration;
	final transient int hitboxSize;
	private final transient int spriteOff;
	public final transient TILE_SHEET sheet;
	public final transient SoundSettlement.Sound sounds;
	public final Icon icon;
	private final int index;
	public final boolean caravanable;
	public final COLOR color;
	private final LIST<RESOURCE> resources;
	public final RBIT rBit;
	private final double[] resAmounts;
	private final double[] climates;
	private final double[] terrains;
	
	public final double[] damage = new double[BOOSTABLES.BATTLE().DAMAGES.size()];
	
	public final boolean pack;
	public final boolean grazes;
	public final COLOR blood = new ColorImp(127, 15, 15);
	
	final double momTreshold;
	final double momTresholdFly;
	public final double caveLiving;
	
	public final double danger;
	
	static RCollection<AnimalSpecies> create() throws IOException {
		return new Collection();
	}
	
	private static class Collection extends  RCollection<AnimalSpecies> {

		private final ArrayList<AnimalSpecies> all;
		
		public Collection() throws IOException {
			super("ANIMAL");
			PATH gData = PATHS.INIT().getFolder("animal");
			PATH gText = PATHS.TEXT().getFolder("animal");
			PATH gSprite = PATHS.SPRITE().getFolder("animal");
			KeyMap<AnimalSpecies> sprites = new KeyMap<>();
			String[] files = gData.getFiles();
			all = new ArrayList<>(files.length);
			for (String key : gData.getFiles()) {
				Json data = new Json(gData.get(key));
				Json text = new Json(gText.get(key));
				String sKey = data.value("SPRITE");
				TILE_SHEET sheet;
				if (sprites.containsKey(sKey)) {
					sheet = sprites.get(sKey).sheet;
				}else {
					new ComposerThings.IInit(gSprite.get(sKey), 132, 366);
					
					sheet = new ComposerThings.ITileSheet() {
						
						@Override
						protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
							s.singles.init(0, 0, 1, 1, 2, 12, d.s24);
							for (int i = 0; i < 12; i++) {
								s.singles.setSkip(i * 2, 2).paste(3, true);
							}
							return d.s24.saveGame();
						}
					}.get();
				}
				AnimalSpecies s = new AnimalSpecies(all.size(), data, text, sheet);
				all.add(s);
				map.put(key, s);
				if (!sprites.containsKey(sKey))
					sprites.put(sKey, s);
			}
			
			for (AnimalSpecies s : all) {
				if (s.caravanable)
					return;
			}
			throw new Errors.DataError("At least one animal must be caravanable", gData.get());
			
			
		}
		

		
		@Override
		public AnimalSpecies getAt(int index) {
			return all.get(index);
		}

		@Override
		public LIST<AnimalSpecies> all() {
			return all;
		}
	}
	
	private AnimalSpecies(int index, Json data, Json text, TILE_SHEET sheet) throws IOException{
		super(text, null);
		this.index = index;
		icon = SPRITES.icons().get(data);
		caravanable = data.bool("CARAVAN");
		massMin = data.i("MASS", 1, 500); 
		acceleration = data.i("SPEED", 1, 31)*C.TILE_SIZE;
		heightOverGround = data.i("HEIGHT", 0, 50);
		hitboxSize = 11*C.SCALE;
		spriteOff = (24*C.SCALE - hitboxSize)/2;
		this.sheet = sheet;
		sounds = SOUND.sett().animal.get(data);
		color = new ColorImp(data);
		resources = RESOURCES.map().getMany(data);
		resAmounts = data.ds("RESOURCE_AMOUNT", resources.size());
		
		RBITImp bb = new RBITImp();
		for (RESOURCE res : resources) {
			bb.or(res);
		}
		this.rBit = bb;
		
		BOOSTABLES.BATTLE().DAMAGE_COLL . new KJson(data) {
			
			@Override
			protected void process(BDamage s, Json j, String key, boolean isWeak) {
				damage[s.index()] = j.d(key, 0, 10000);
			}
		};
		

		CLIMATES.MAP();
		climates = KEY_COLLECTION.fill(CLIMATES.MAP(), data, 1);
		terrains = KEY_COLLECTION.fill(TERRAINS.MAP(), data, 1);
		pack = data.bool("PACK");
		grazes = data.bool("GRAZES");
		danger = data.d("DANGER", 0, 1);
		momTreshold = acceleration*massMin*1.5;
		momTresholdFly = acceleration*massMin*2.0;
		caveLiving = data.d("LIVES_IN_CAVES", 0, 1);
	}

	public double occurence(CLIMATE c) {
		return climates[c.index()];
	}
	
	public double occurence(TERRAIN t) {
		return terrains[t.index()];
	}
	
	public double mass() {
		return massMin;
	}

	public double heightOverGround() {
		return heightOverGround;
	}

	public double acceleration() {
		return acceleration;
	}

	public int hitBoxSize() {
		return hitboxSize;
	}

	public int spriteOff() {
		return spriteOff;
	}

	public void renderCorpse(Renderer r, ShadowBatch shadows, float ds, int x, int y, int state, int rot, int ran, double statef, COLOR decay) {

		shadows.setHeight(2).setDistance2Ground(0);
		if (state == 0) {
			int t = Sprite.bodypart1;
			if ((ran & 1) == 1) {
				t += Sprite.NR;
			}
			t += rot;
			sheet.render(r, t, x, y);
			sheet.render(shadows, t, x, y);
		}else if(state == 1) {
			int t = Sprite.laying + rot;
			sheet.render(r, t, x, y);
			sheet.render(shadows, t, x, y);
			int bloodI = (int) ((statef)*Sprite.BLOOD.length);
			
			if (bloodI > 0) {
				OPACITY.O99.bind();
				sheet.renderTextured(Sprite.blood().getTexture(Sprite.BLOOD[bloodI-1]), t, x, y);
				OPACITY.unbind();
			}
		}else if(state == 2) {
			decay.bind();
			int t = Sprite.rotten + rot;
			sheet.render(r, t, x, y);
			sheet.render(shadows, t, x, y);
			COLOR.unbind();
			
		}else if(state == 3) {
			int t = Sprite.bones + rot;
			sheet.render(r, t, x, y);
			sheet.render(shadows, t, x, y);
		}else {
			throw new RuntimeException();
		}
		
		
	}

	@Override
	public int index() {
		return index;
	}
	
	public LIST<RESOURCE> resources(){
		return resources;
	}
	
	public int resAmount(int ri, double weight) {
		return (int)Math.ceil(resAmounts[ri]*weight*0.3);
	}
	
}
