package util.rendering;

import settlement.entity.EPHYSICS.Solid;
import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.sprite.TextureCoords;

public abstract class ShadowBatch implements SPRITE_RENDERER{

	public abstract ShadowBatch setHeight(int height);
	public abstract ShadowBatch setDistance2Ground(double height);
	public abstract ShadowBatch setDistance2GroundUI(double height);
	
	public abstract void set(Solid e);
	public abstract ShadowBatch setSoft();
	public abstract ShadowBatch setHard();
	public abstract ShadowBatch setPrev();
	
	public static class Real extends ShadowBatch{
		
		private double dD = 127;
		private int iterations;
		
		
		private int startX;
		private int startY;
		private double x;
		private double y;
		private double dx;
		private double dy;
		private boolean bx;
		private int lastHeight = -1;
		private final byte SoftShadow = 127;
		private final byte fullShadow = -1;
		private byte streangth = SoftShadow;
		private byte prev = streangth;
		private int zoomout;
		
		public void init(int zoomout, double dx, double dy){
			
			x = dx;
			y = dy;
			
			bx = Math.abs(x) > Math.abs(y);
			this.zoomout = zoomout;
		}
		
		@Override
		public ShadowBatch setHeight(int height){
			
			if (height == lastHeight)
				return this;
			
			lastHeight = height;
			
			
			dx = x*height;
			dy = y*height;
			
			if (bx){
				iterations = (int) Math.abs(dx);
			}else{
				iterations = (int) Math.abs(dy);
			}
			
			iterations = (int) Math.ceil((double)iterations);
			//iterations/= C.SCALE;
			iterations = 1 + (iterations >> (zoomout));
			
			
			dx/=iterations;
			dy/=iterations;
			
			dD = (127.0/iterations);
			return this;
		}
		
		@Override
		public ShadowBatch setDistance2Ground(double height){
			startX = (int) (height*x);
			startY = (int) (height*y);
			return this;
		}
		
		@Override
		public void set(Solid e){
			setHeight((int) e.getHeight());
			setDistance2Ground(e.getZ());
		}
		
		@Override
		public void renderSprite(int x1, int x2, int y1, int y2, TextureCoords texture) {
			x1 += startX;
			x2 += startX;
			y1 += startY;
			y2 += startY;
			
			int ix;
			int iy;
			double j = 0;
			if (startX + startY == 0)
				j++;
			CORE.renderer().shadowDepthSet(streangth);
			while(j <= iterations){
				ix = (int)(dx*j);
				iy = (int)(dy*j);
				
				CORE.renderer().renderShadow(x1+ix, x2+ix, y1+iy, y2+iy, texture, (byte)((j)*dD));
				j++;
			}
		}

		@Override
		public ShadowBatch setSoft() {
			prev = streangth;
			streangth = SoftShadow;
			return this;
		}

		@Override
		public ShadowBatch setHard() {
			prev = streangth;
			streangth = fullShadow;
			return this;
		}

		@Override
		public ShadowBatch setDistance2GroundUI(double height) {
			startX = (int) (height);
			startY = (int) (height);
			return this;
		}
		
		@Override
		public ShadowBatch setPrev() {
			streangth = prev;
			CORE.renderer().shadowDepthSet(streangth);
			return this;
		}
		
	}
	
	public static final ShadowBatch DUMMY = new Dummy();
	
	public static class Dummy extends ShadowBatch{

		@Override
		public ShadowBatch setHeight(int height) {
			// TODO Auto-generated method stub
			return this;
		}

		@Override
		public ShadowBatch setDistance2Ground(double height) {
			// TODO Auto-generated method stub
			return this;
			
		}

		@Override
		public void set(Solid e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public ShadowBatch setSoft() {
			// TODO Auto-generated method stub
			return this;
		}

		@Override
		public ShadowBatch setHard() {
			// TODO Auto-generated method stub
			return this;
		}

		@Override
		public ShadowBatch setDistance2GroundUI(double height) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void renderSprite(int x1, int x2, int y1, int y2, TextureCoords texture) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public ShadowBatch setPrev() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	

}
