package snake2d.util.color;

import java.io.IOException;
import java.io.Serializable;

import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.file.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.TextureCoords;


public class ColorImp implements COLOR, Serializable, SAVABLE{

	public static final ColorImp TMP = new ColorImp(); 
	
	private static final long serialVersionUID = 1L;
	private final static TextureCoords.Imp texture = new TextureCoords.Imp();
	private static short width;
	private static short height;
	
	/**
	 * called once to specify where white is on spritesheet
	 * @param wX1
	 * @param wX2
	 * @param wY1
	 * @param wY2
	 */
	public static void setSPRITE(int wX1, int wY1, int w, int h){
		texture.get(wX1, wY1, w, h);
		width = (short) w;
		height = (short) h;
	}

	
	private byte red;
	private byte green;
	private byte blue;
	
	public ColorImp(){
		this(127,127,127);
	}
	
	public ColorImp(int red, int green, int blue){
		
		this.setRed(red);
		this.setGreen(green);
		this.setBlue(blue);
		
	}
	
	public ColorImp(Json json) {
		this(json, "COLOR");
	}
	
	public static LIST<ColorImp> cols(Json json){
		return cols(json, "COLOR");
	}
	
	public static LIST<ColorImp> cols(Json json, String key){
		if (!json.has(key))
			throw new RuntimeException();
		if (json.jsonIs(key)) {
			json = json.json(key);
			if (json.has("R") && json.has("B") && json.has("G"))
				return new ArrayList<>(new ColorImp(json, key));
			else {
				COLOR from = new ColorImp(json, "FROM");
				COLOR to = new ColorImp(json, "TO");
				return new ArrayList<ColorImp>(COLOR.interpolate(from, to, json.i("GENERATE", 0, 1024)));
			}
		}else if(json.jsonsIs(key)) {
			Json[] js = json.jsons(key);
			ArrayList<ColorImp> res = new ArrayList<ColorImp>(js.length);
			for (int i = 0; i < js.length; i++)
				res.add(new ColorImp(js[i]));
			return res;
		}else if (json.arrayIs(key)) {
			return new ArrayList<>(new ColorImp(json, key));
		}else
			throw new RuntimeException();
		
		
	}
	
	public ColorImp(Json json, String key) {
		if (json.has(key) && json.jsonIs(key)) {
			json = json.json(key);
		}
		
		if (json.has("R") && json.has("G") && json.has("B")) {
			set(json.i("R", 0, 511)/2, json.i("G", 0, 511)/2, json.i("B", 0, 511)/2);
		}else {
			int[] cols = json.is(key);
			if (cols.length != 3)
				json.error("Wrong dimension of color. Should be 3 components", "COLOR");
			set(cols[0]/2, cols[1]/2, cols[2]/2);
		}
		
	}
	
	public ColorImp(COLOR c){
		set(c);
	}
	
	public ColorImp set(COLOR c){
		if (c instanceof ColorShifting) {
			c.bind();
			COLOR.unbind();
		}
		this.red = c.red();
		this.blue = c.blue();
		this.green = c.green();
		return this;
	}
	
	public static void unBind(){
		CORE.renderer().setNormalColor();
	}
	
	@Override
	public byte red() {
		return red;
	}

	public ColorImp set(int r, int g, int b) {
		setRed(r);
		setGreen(g);
		setBlue(b);
		return this;
	}
	
	public ColorImp setAll(int i) {
		setRed(i);
		setGreen(i);
		setBlue(i);
		return this;
	}
	
	public ColorImp setRed(int red) {
		this.red = (byte) red;
		return this;
	}

	@Override
	public byte green() {
		return green;
	}

	public ColorImp setGreen(int green) {
		this.green = (byte) green;
		return this;
	}

	@Override
	public byte blue() {
		return blue;
	}

	public ColorImp setBlue(int blue) {
		this.blue = (byte) blue;
		return this;
	}

	public ColorImp setComp(int comp, int c) {
		switch(comp) {
		case 0 : setRed(c); break;
		case 1 : setGreen(c); break;
		case 2 : setBlue(c); break;
		default: throw new RuntimeException(""+comp);
		}
		return this;
	}
	

	
	
	public ColorImp setAmount(double amount, double max){
		blue = 0;
		double ratio = (amount/max);
		
		if (ratio < 0.5f){
			green = (byte) (128f *ratio*2f);
			red = (byte)128;
		}else if(ratio <= 1){
			if (ratio > 1f){
				ratio = 1f;
			}
			green = (byte)128;
			red = (byte)(128 - ((ratio-0.5f)*2f*128f));
		}else{
			if (ratio > 2f){
				ratio = 2f;
			}
			red = 0;
			green = (byte)128;
			blue = (byte) ((ratio-1)*128);
		}
		return this;
	}
	
	@Override
	public int width() {
		return width;
	}

	@Override
	public int height() {
		return height;
	}

	@Override
	public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
		bind();
		r.renderSprite(X1, X2, Y1, Y2, texture);
		//white.render(X1, X2, Y1, Y2);
		unBind();
	}

	@Override
	public void render(SPRITE_RENDERER r, int X1, int Y1) {
		bind();
		r.renderSprite(X1, X1+width, Y1, Y1+width, texture);
		unBind();
	}
	
	@Override
	public String toString() {
		return "color: (r:" + Byte.toUnsignedInt(red) + ", g:" + Byte.toUnsignedInt(green) + ", b:" + Byte.toUnsignedInt(blue) + ")";
	}
	
	@Override
	public void render(SPRITE_RENDERER r, RECTANGLE rec) {
		render(r, rec.x1(), rec.x2(), rec.y1(), rec.y2());
	}

	public ColorImp interpolate(COLOR c1, COLOR c2, double part) {

		part = CLAMP.d(part, 0, 1);
		
		if (Double.isNaN(part))
			part = 0;
		
		int r = (int) Math.round((c1.red()& 0x0FF)*(1.0-part) + (c2.red()& 0x0FF)*part);
		int g = (int) Math.round((c1.green()& 0x0FF)*(1.0-part) + (c2.green()& 0x0FF)*part);
		int b = (int) Math.round((c1.blue()& 0x0FF)*(1.0-part) + (c2.blue()& 0x0FF)*part);
		
		setRed(r);
		setGreen(g);
		setBlue(b);
		return this;
	}
	
	public void multiply(COLOR other) {
		
		double i = (double)127;
		
		double r = (red()& 0x0FF)*i;
		double g = (green()& 0x0FF)*i;
		double b = (blue()& 0x0FF)*i;
		
		double r1 = (other.red()& 0x0FF)*i;
		double g1 = (other.green()& 0x0FF)*i;
		double b1 = (other.blue()& 0x0FF)*i;
		
		setRed(CLAMP.i((int) ((r*r1)*255), 0, 255));
		setGreen(CLAMP.i((int) ((g*g1)*255), 0, 255));
		setBlue(CLAMP.i((int) ((b*b1)*255), 0, 255));
		
	}
	
	public ColorImp  shadeSelf(double shade) {
		set((int)((red()&0x0FF)*shade), (int)((green()&0x0FF)*shade), (int)((blue()&0x0FF)*shade));
		return this;
	}
	
	public ColorImp  add(int am) {
		set((int)((red()&0x0FF)+am), (int)((green()&0x0FF)+am), (int)((blue()&0x0FF)+am));
		return this;
	}
	
	public ColorImp saturateSelf(double amount) {
		
		double r = (red() & 0x0FF);
		double g = (green() & 0x0FF);
		double b = (blue() & 0x0FF);
		

		double min = 255.0;
		double max = 0;
		
		if (r < min)
			min = r;
		if (r > max)
			max = r;
		if (g < min)
			min = g;
		if (g > max)
			max = g;
		if (b < min)
			min = b;
		if (b > max)
			max = b;
		
		double lum = (min+max)/2.0;
		
		
		int red = (int) (lum + (r-lum)*amount);
		int green = (int) (lum + (g-lum)*amount);
		int blue = (int) (lum + (b-lum)*amount);
		set(red, green, blue);
		return this;
		
	}

	@Override
	public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
		CORE.renderer().renderTextured(X1, X2, Y1, Y2, 
				texture, ColorImp.texture);
		
	}

	@Override
	public void save(FilePutter file) {
		file.i((red&0x0FF) | ((green<<8)&0x0FF00) | ((blue<<16)&0x0FF0000));
	}

	@Override
	public void load(FileGetter file) throws IOException {
		int i = file.i();
		red = (byte) (i & 0x0FF);
		green = (byte) ((i>>8) & 0x0FF);
		blue = (byte) ((i>>16) & 0x0FF);
	}

	@Override
	public void clear() {
		
		
	}

	public void randomize(double d) {
		red = (byte) CLAMP.i((int) (red+RND.rFloat()*d*255), 0, 255);
		green = (byte) CLAMP.i((int) (green+RND.rFloat()*d*255), 0, 255);
		blue = (byte) CLAMP.i((int) (blue+RND.rFloat()*d*255), 0, 255);
	}
}
