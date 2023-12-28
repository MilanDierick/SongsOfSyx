package snake2d;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import java.nio.IntBuffer;

import org.lwjgl.opengl.GL11;

import snake2d.VboSorter.Counts;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;

class VboParticles extends VboAbs{

	private final byte byteZero = 0;
	private final byte byteFull = -1;
	private final int[] size = new int[255];
	private final Shader shader;
	private final VboSorter sorter;
	private final IntBuffer sBuff;
	private int layer = 0;
	
	static VboParticles getDebug(SETTINGS sett) {
		Shader shader = new Shader(sett.getNativeWidth(), sett.getNativeHeight(), "Particle_debug", null, "Particle_debug");
		return new VboParticles(shader);
	}

	static VboParticles getForTexture(int width, int height) {
		Shader shader = new Shader(width-0.5, height+0.5, "Particle_texture", null, "Particle_texture");
		return new VboParticles(shader);
	}
//
	static VboParticles getDeffered(SETTINGS sett) {
		Shader shader = new Shader(sett.getNativeWidth(), sett.getNativeHeight(), "Particle", null, "Particle");
		return new VboParticles(shader);
	}

	public VboParticles(Shader shader) {
		super(GL_POINTS, 1 << 16, 
				new VboAttribute(2, GL_SHORT, false, 2), // position		4
				new VboAttribute(4, GL_UNSIGNED_BYTE, true, 1), // normal	4
				new VboAttribute(4, GL_UNSIGNED_BYTE, true, 1));// color	4
		
		sorter = new VboSorter(MAX_ELEMENTS*3);
		
		
		this.shader = shader;
		sBuff = buffer.asIntBuffer();
		size[0] = 1;

	}

	void setNew(int pointSize) {
		layer++;
		size[layer] = pointSize;
	}

	final void flush(int pointSize) {
		
		bind();
		shader.bind();
		sBuff.position(0);
		Counts ss = sorter.fill(sBuff);
		buffer.position(sBuff.position()*4);
		upload();
		
		for (int i = 0; i <= layer; i++) {
			
			int fromI = ss.from[i];
			int toI = ss.to[i];
			if (toI > fromI) {
				GlHelper.Stencil.setLEQUALreplaceOnPass(i);
				flush(fromI/3, toI/3, size[i]);
			}
			
			
			
		}

		clear(pointSize);
		glUseProgram(0);
		
	}

	public int count() {
		return buffer.position();
	}
	
	private void flush(int from, int to, int size) {
		if (size < 1)
			throw new RuntimeException();
		glPointSize(size);
		glDrawElements(GL11.GL_POINTS, (to - from), GL11.GL_UNSIGNED_INT, from * 4);
	}


	public void clear(int pointSize) {
		super.clear();
		layer = 0;
		sorter.clear();
		size[0] = pointSize;
	}

	public void render(short x, short y, byte nX, byte nY, byte nZ, byte nA, COLOR color, OPACITY opacity) {
		VboSorter sorter = this.sorter;
		
		sorter.add(layer, ((y) << 16) | ((x & 0x0FFFF)));
		sorter.add(layer, (((nA) << 24) | ((nZ&0x0FF) << 16) | ((nY&0x0FF) <<8) | ((nX&0x0FF))));
		sorter.add(layer, (((opacity.get()) << 24) | ((color.blue()&0x0FF) << 16) | ((color.green()&0x0FF) <<8) | ((color.red()&0x0FF))));
		
	}

	public void render(short x, short y, byte red, byte green, byte blue) {

		VboSorter sorter = this.sorter;
		
		sorter.add(layer, ((y) << 16) | ((x & 0x0FFFF)));
		sorter.add(layer, (((byteZero) << 24) | ((byteZero&0x0FF) << 16) | ((byteZero&0x0FF) <<8) | ((byteZero&0x0FF))));
		sorter.add(layer, (((byteFull) << 24) | ((blue&0x0FF) << 16) | ((green&0x0FF) <<8) | ((red&0x0FF))));
	}



	@Override
	public void dis() {
		shader.dis();
		super.dis();
	}
	
	public void dis(boolean leaveIndexArrayTheFuckAlone) {
		shader.dis();
		super.dis();
	}

}
