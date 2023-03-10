package snake2d;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import org.lwjgl.opengl.GL11;

import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;

class VboParticles extends VboAbs{

	private final byte byteZero = 0;
	private final byte byteFull = -1;
	private final int[] size = new int[255];
	private final VboShaderAbs shader;

	static VboParticles getDebug(SETTINGS sett) {
		ShaderDebug shader = new ShaderDebug(sett.getNativeWidth(), sett.getNativeHeight());
		return new VboParticles(shader);
	}

	static VboParticles getForTexture(int width, int height) {
		ShaderTexture shader = new ShaderTexture(width, height);
		return new VboParticles(shader);
	}

	static VboParticles getDeffered(SETTINGS sett) {
		ShaderDeffered shader = new ShaderDeffered(sett.getNativeWidth(), sett.getNativeHeight());
		return new VboParticles(shader);
	}

	public VboParticles(VboShaderAbs shader) {
		super(GL_POINTS, 1 << 16, 
				new VboAttribute(2, GL_SHORT, false, 2), // position		4
				new VboAttribute(4, GL_UNSIGNED_BYTE, true, 1), // normal	4
				new VboAttribute(4, GL_UNSIGNED_BYTE, true, 1));// color	4
		
		this.shader = shader;
		size[0] = 1;

	}

	private static class ShaderDebug extends VboShaderAbs {

		ShaderDebug(float width, float height) {

			String VERTEX = "#version 330 core" + "\n"
					+ getScreenVec(width, height)
					+ "const vec2 trans = vec2(-1.0,1.0);" + "\n"

					+ "layout(location = 0) in vec2 in_position;" + "\n" 
					+ "layout(location = 2) in vec4 in_color;"
					+ "\n"

					+ "out vec4 vColor;" + "\n"

					+ "void main(){" + "\n" + "vColor = vec4(in_color.xyz*2.0, in_color.w);" + "\n"
					+ "gl_Position = vec4((in_position * screen)+trans, 0.0, 1.0);" + "\n" + "}";

			String FRAGMENT = "#version 330 core" + "\n"

					+ "in vec4 vColor;" + "\n"

					+ "out vec4 out_diffuse;" + "\n"

					+ "void main(){" + "\n" + "out_diffuse = vColor;" + "\n"

					+ "}" + "\n";

			super.compile(VERTEX, FRAGMENT);

		}

	}
	
	private static class ShaderTexture extends VboShaderAbs {

		ShaderTexture(float width, float height) {

			String VERTEX = "#version 330 core" + "\n"

					+ getScreenVec(width-0.5f, height+0.5f) 
					+ "const vec2 trans = vec2(-1.0,1.0);" + "\n"

					+ "layout(location = 0) in vec2 in_position;" + "\n" 
					+ "layout(location = 2) in vec4 in_color;"
					+ "\n"

					+ "out vec4 vColor;" + "\n"

					+ "void main(){" + "\n" + "vColor = in_color;" + "\n"
					+ "gl_Position = vec4((in_position * screen)+trans, 0.0, 1.0);" + "\n" + "}";

			String FRAGMENT = "#version 330 core" + "\n"

					+ "in vec4 vColor;" + "\n"

					+ "out vec4 out_diffuse;" + "\n"

					+ "void main(){" + "\n" + "out_diffuse = vColor;" + "\n"

					+ "}" + "\n";

			super.compile(VERTEX, FRAGMENT);

		}

	}

	private static class ShaderDeffered extends VboShaderAbs {

		ShaderDeffered(float width, float height) {

			String VERTEX = "#version 330 core" + "\n"

					+ getScreenVec(width, height)  
					+ "const vec2 trans = vec2(-1.0,1.0);" + "\n"

					+ "layout(location = 0) in vec2 in_position;" + "\n" 
					+ "layout(location = 1) in vec4 in_normal;"
					+ "\n" + "layout(location = 2) in vec4 in_color;" + "\n"

					+ "out vec4 vColor;" + "\n" + "out vec4 vNormal;" + "\n"

					+ "void main(){" + "\n" + "vColor = vec4(in_color.xyz*2.0, in_color.w);" + "\n"
					+ "vNormal = in_normal;" + "\n" + "gl_Position = vec4((in_position * screen)+trans, 0.0, 1.0);"
					+ "\n" + "}";

			String FRAGMENT = "#version 330 core" + "\n"

					+ "in vec4 vColor;" + "\n" + "in vec4 vNormal;" + "\n"

					+ "layout(location = 0) out vec4 out_diffuse;" + "\n" + "layout(location = 1) out vec4 out_normal;"
					+ "\n"

					+ "void main(){" + "\n" + "out_diffuse = vColor;" + "\n" + "out_normal = vNormal;" + "\n"

					+ "}" + "\n";

			super.compile(VERTEX, FRAGMENT);

		}

	}

	void setNew(int pointSize) {
		vTo[current] = count;
		current++;
		vFrom[current] = count;
		size[current] = pointSize;
	}

	final void flush(int pointSize) {
		bindAndUpload();
		shader.bind();
		int i = 0;
		vTo[current] = count;
		while (i <= current) {
			if (vFrom[i] != vTo[i]) {
				GlHelper.Stencil.setLEQUALreplaceOnPass(i);
				flush(vFrom[i], vTo[i], size[i]);
			}
			i++;
		}
		clear(pointSize);
		glUseProgram(0); // puts an end to the goddamn nvidia errors
	}

	private void flush(int from, int to, int size) {
		glPointSize(size);
		glDrawElements(GL11.GL_POINTS, (to - from), GL11.GL_UNSIGNED_INT, from * 4);
	}


	public void clear(int pointSize) {
		super.clear();
		size[current] = pointSize;
	}

	public void render(short x, short y, byte nX, byte nY, byte nZ, byte nA, COLOR color, OPACITY opacity) {

		if (count >= MAX_ELEMENTS) {
			return;
		}

		buffer.putShort(x).putShort(y);
		buffer.put(nX).put(nY).put(nZ).put(nA);
		buffer.put(color.red()).put(color.green()).put(color.blue()).put(opacity.get());

		count++;
	}

	public void render(short x, short y, byte red, byte green, byte blue) {

		if (count >= MAX_ELEMENTS) {
			return;
		}

		buffer.putShort(x).putShort(y);
		buffer.put(byteZero).put(byteZero).put(byteZero).put(byteZero);
		buffer.put(red).put(green).put(blue).put(byteFull);

		count++;
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
