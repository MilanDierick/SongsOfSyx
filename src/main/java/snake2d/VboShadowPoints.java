package snake2d;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import snake2d.util.sprite.TextureCoords;

class VboShadowPoints extends VboAbs{

	private final int[] fromStencil = new int[255];
	private final VboShaderAbs shader;

	public VboShadowPoints(SETTINGS sett) {
		super(GL_POINTS, 1<<16, 
			new VboAttribute(2, GL_SHORT, false, 2), // position upper left			4
			new VboAttribute(2, GL_SHORT, false, 2), // position lower right		4
			new VboAttribute(2, GL_UNSIGNED_SHORT, 2), // texture coords1			4
			new VboAttribute(2, GL_UNSIGNED_SHORT, 2), // texture coords2			4
			new VboAttribute(4, GL_UNSIGNED_BYTE, true, 1) //d + depth + 2padding	4
			); 
		this.shader = new ShaderDebug(sett.getNativeWidth(), sett.getNativeHeight());

	}

	private static class ShaderDebug extends VboShaderAbs {

		ShaderDebug(float width, float height) {

			String VERTEX = "#version 330 core" + "\n"

					+ getScreenVec(width, height)
					+ "const vec2 trans = vec2(-1.0,1.0);" + "\n"

					+ "layout(location = 0) in vec2 in_position1;" + "\n"
					+ "layout(location = 1) in vec2 in_position2;" + "\n" 
					+ "layout(location = 2) in uvec2 in_texCoo;" + "\n"
					+ "layout(location = 3) in uvec2 in_texCoo2;" + "\n"
					+ "layout(location = 4) in vec4  in_d_depth;" + "\n"
					
					+ "\n"

					+ "out vec2 gTexCoo;" + "\n" 
					+ "out vec2 gTexCoo2;" + "\n" 
					+ "out vec2 gPos2;" + "\n" 
					+ "out float gvD;" + "\n" 
					
					+ "void main(){" + "\n" 
						+ "gTexCoo = in_texCoo;" + "\n"
						+ "gTexCoo2 = in_texCoo2;" + "\n"
						+ "gvD = in_d_depth.x*2.0;" + "\n"
						+ "gPos2 = vec2((in_position2 * screen)+trans);" + "\n" 
						+ "gl_Position = vec4((in_position1 * screen)+trans, in_d_depth.y, 1.0);" + "\n" 

					+ "}";

			String GEOMETRY = "#version 330 core" + "\n"

				+ "layout (points) in;" + "\n" 
				+ "layout (triangle_strip, max_vertices = 4) out;" + "\n"
					
				+ "in vec2 gTexCoo[];" + "\n" 
				+ "in vec2 gTexCoo2[];" + "\n"
				+ "in vec2 gPos2[];" + "\n" 
				+ "in float gvD[];" + "\n" 
				
				+ "out float vD;" + "\n" 
				+ "out vec2 vTexCoo;" + "\n" 

				+ "void main(){" + "\n" 
					+ "vD = gvD[0];" + "\n"
				
					+ "vTexCoo = gTexCoo[0];" + "\n"
					+ "gl_Position = gl_in[0].gl_Position;" + "\n"
					+ "EmitVertex();" + "\n"
					
					+ "vTexCoo = vec2(gTexCoo2[0].x, gTexCoo[0].y);" + "\n"
					+ "gl_Position = gl_in[0].gl_Position;" + "\n"
					+ "gl_Position.x = gPos2[0].x;" + "\n"
					+ "EmitVertex();" + "\n"
					
					+ "vTexCoo = vec2(gTexCoo[0].x, gTexCoo2[0].y);" + "\n"
					+ "gl_Position = gl_in[0].gl_Position;" + "\n"
					+ "gl_Position.y = gPos2[0].y;" + "\n"
					+ "EmitVertex();" + "\n"
					
					+ "vTexCoo = vec2(gTexCoo2[0].x, gTexCoo2[0].y);" + "\n"
					+ "gl_Position = gl_in[0].gl_Position;" + "\n"
					+ "gl_Position.y = gPos2[0].y;" + "\n"
					+ "gl_Position.x = gPos2[0].x;" + "\n"
					+ "EmitVertex();" + "\n"

					+ "EndPrimitive();" + "\n"
					
				+ "}";
			
			String FRAGMENT = "#version 330 core" + "\n"

					+ "in vec2 vTexCoo;" + "\n" 
					+ "in float vD;" + "\n" 
					+ "uniform sampler2DRect sampler1;" + "\n"
					+ "layout(location = 0) out vec4 out_diffuse;" + "\n"

					+ "void main(){" + "\n" 
						+ "if ((texture(sampler1, vTexCoo).w - vD) <= 0){discard;}" + "\n"

					+ "}" + "\n";

			super.compile(VERTEX, FRAGMENT, GEOMETRY);
			int TEX1_LOC = super.getUniformLocation("sampler1");
			super.setUniform1i(TEX1_LOC, 0);

		}

	}

	int setNewFinalOverride(int fromStencil) {
		vTo[current] = count;
		current++;
		vFrom[current] = count;
		this.fromStencil[current] = fromStencil;
		return current;

	}

	void flush() {
		if (count != 0) {

			
			bindAndUpload();
			GlHelper.enableDepthTest(true);
			GlHelper.setDepthTestAlways();
			glColorMask(false, false, false, false);
			shader.bind();
			int i = 0;
			vTo[current] = count;
			while (i <= current) {
				GlHelper.Stencil.setLESSKeepOnPass(fromStencil[i]);
				flush(vFrom[i], vTo[i]);
				i++;
			}
			glUseProgram(0);
			glColorMask(true, true, true, true);
			GlHelper.enableDepthTest(false);
		}
		clear();
		
	}


	public void render(TextureCoords t, int x1, int y1, int x2, int y2, int x3, int y3,
			int x4, int y4, byte d, byte depth) {

		if (count >= MAX_ELEMENTS) {
			return;
		}
		

		
		buffer.putShort((short) x1).putShort((short) y1);
		buffer.putShort((short) x3).putShort((short) y3);
		buffer.putShort(t.x1()).putShort(t.y1());
		buffer.putShort(t.x2()).putShort(t.y2());
		buffer.put(d);
		buffer.put(depth);
		buffer.put(Byte.MAX_VALUE);
		buffer.put(Byte.MAX_VALUE);
		count++;
	}
	
	@Override
	public void dis() {
		shader.dis();
		super.dis();
	}


}
