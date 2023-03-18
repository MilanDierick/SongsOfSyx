package snake2d;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;

class VboSpriteDisplace extends VboAbs{

	private final VboShaderAbs shader;

	static VboSpriteDisplace getDebug(SETTINGS sett) {
		ShaderDebug shader = new ShaderDebug(sett.getNativeWidth(), sett.getNativeHeight(), true);
		return new VboSpriteDisplace(shader);
	}

	static VboSpriteDisplace getDeffered(SETTINGS sett) {
		ShaderDebug shader = new ShaderDebug(sett.getNativeWidth(), sett.getNativeHeight(), false);
		return new VboSpriteDisplace(shader);
	}

	public VboSpriteDisplace(VboShaderAbs shader) {
		super( GL_POINTS,
				1<<15,
				new VboAttribute(2, GL_SHORT, false, 2), // position upper left		4
				new VboAttribute(2, GL_SHORT, false, 2), // position lower right	4
				new VboAttribute(2, GL_FLOAT, false, 4), // texture coords1		4
				new VboAttribute(2, GL_FLOAT, false, 4), // texture coords2		4
				new VboAttribute(2, GL_UNSIGNED_SHORT, 2), // texture coords width	4
				
				new VboAttribute(4, GL_UNSIGNED_BYTE, true, 1), // color				4
				new VboAttribute(1, GL_FLOAT, false, 4) // scale
		);
		this.shader = shader;

	}

	private static class ShaderDebug extends VboShaderAbs {

		ShaderDebug(float width, float height, boolean debug) {
			
			String VERTEX = "#version 330 core" + "\n"

				+ getScreenVec(width, height) 
				+ "const vec2 trans = vec2(-1.0,1.0);" + "\n"

				+ "layout(location = 0) in vec2 in_position1;" + "\n"
				+ "layout(location = 1) in vec2 in_position2;" + "\n" 
				+ "layout(location = 2) in vec2 in_texCoo1;" + "\n"
				+ "layout(location = 3) in vec2 in_texCoo2;" + "\n" 
				+ "layout(location = 4) in uvec2 in_texCooWidth;" + "\n" 
				+ "layout(location = 5) in vec4 in_color;" + "\n"
				+ "layout(location = 6) in float in_scale;" + "\n"
				
				+ "out vec2 gTexCoo1;" + "\n" 
				+ "out vec2 gTexCoo2;" + "\n"
				+ "out vec2 gTexCooWidth;" + "\n"
				+ "out vec2 gPos2;" + "\n" 
				+ "out vec4 gColor;" + "\n"
				+ "out float gScale;" + "\n"

				+ "void main(){" + "\n" 
					+ "gTexCoo1 = in_texCoo1;" + "\n" 
					+ "gTexCoo2 = in_texCoo2;" + "\n"
					+ "gScale = in_scale;" + "\n"
					+ "gTexCooWidth = in_texCooWidth;" + "\n"
					+ "gColor = vec4(in_color.xyz*2.0, in_color.w);" + "\n"
					+ "gPos2 = vec2((in_position2 * screen)+trans);" + "\n" 
					+ "gl_Position = vec4((in_position1 * screen)+trans, 0.0, 1.0);" + "\n" 
				+ "}";
			
			String GEOMETRY = "#version 330 core" + "\n"

				+ "layout (points) in;" + "\n" 
				+ "layout (triangle_strip, max_vertices = 4) out;" + "\n"
					
				+ "in vec2 gTexCoo1[];" + "\n" 
				+ "in vec2 gTexCoo2[];" + "\n"
				+ "in vec2 gTexCooWidth[];" + "\n"
				+ "in vec2 gPos2[];" + "\n" 
				+ "in vec4 gColor[];" + "\n"
				+ "in float gScale[];" + "\n"
				
				+ "out vec2 vTexCoo;" + "\n" 
				+ "out vec2 vTexCoo2;" + "\n" 
				+ "out vec4 vColor;" + "\n"
				+ "out float vScale;" + "\n"

				+ "void main(){" + "\n" 
					+ "vColor = gColor[0];" + "\n"
					+ "vScale = gScale[0];" + "\n"
					+ "vTexCoo = gTexCoo1[0];" + "\n"
					+ "vTexCoo2 = gTexCoo2[0];" + "\n"
					+ "gl_Position = gl_in[0].gl_Position;" + "\n"
					+ "EmitVertex();" + "\n"
					
					+ "vTexCoo = vec2(gTexCoo1[0].x+gTexCooWidth[0].x, gTexCoo1[0].y);" + "\n"
					+ "vTexCoo2 = vec2(gTexCoo2[0].x+gTexCooWidth[0].x, gTexCoo2[0].y);" + "\n"
					+ "gl_Position = gl_in[0].gl_Position;" + "\n"
					+ "gl_Position.x = gPos2[0].x;" + "\n"
					+ "EmitVertex();" + "\n"
					
					
					+ "vTexCoo = vec2(gTexCoo1[0].x, gTexCoo1[0].y+gTexCooWidth[0].y);" + "\n"
					+ "vTexCoo2 = vec2(gTexCoo2[0].x, gTexCoo2[0].y+gTexCooWidth[0].y);" + "\n"
					+ "gl_Position = gl_in[0].gl_Position;" + "\n"
					+ "gl_Position.y = gPos2[0].y;" + "\n"
					+ "EmitVertex();" + "\n"
					
					+ "vTexCoo = vec2(gTexCoo1[0].x+gTexCooWidth[0].x, gTexCoo1[0].y+gTexCooWidth[0].y);" + "\n"
					+ "vTexCoo2 = vec2(gTexCoo2[0].x+gTexCooWidth[0].x, gTexCoo2[0].y+gTexCooWidth[0].y);" + "\n"
					+ "gl_Position = gl_in[0].gl_Position;" + "\n"
					+ "gl_Position.y = gPos2[0].y;" + "\n"
					+ "gl_Position.x = gPos2[0].x;" + "\n"
					+ "EmitVertex();" + "\n"

					+ "EndPrimitive();" + "\n"
					
				+ "}";

			String FRAGMENT = "#version 330 core" + "\n"

					+ "in vec2 vTexCoo;" + "\n" 
					+ "in vec2 vTexCoo2;" + "\n" 
					+ "in vec4 vColor;" + "\n"
					+ "in float vScale;" + "\n"
					+ "uniform sampler2DRect sampler1;" + "\n" 
					+ "uniform sampler2DRect sampler2;" + "\n"

					+ "layout(location = 0) out vec4 out_diffuse;" + "\n" 
					+ "layout(location = 1) out vec4 out_normal;" + "\n"

					+ "vec4 linear( sampler2DRect samp, vec2 texCoord_i )"
					+ "{"
					   + "vec4 p0q0 = texture(samp, texCoord_i);"
					   + "vec4 p1q0 = texture(samp, texCoord_i + vec2(1, 0));"
				
					   + "vec4 p0q1 = texture(samp, texCoord_i + vec2(0, 1));"
					   + "vec4 p1q1 = texture(samp, texCoord_i + vec2(1 , 1));"
				
					   + "float a = texCoord_i.x - int(texCoord_i.x);"
				
					   + "vec4 pInterp_q0 = mix( p0q0, p1q0, a );"
					   + "vec4 pInterp_q1 = mix( p0q1, p1q1, a );"
				
					   + "float b = texCoord_i.y - int(texCoord_i.y);"
					   + "return mix( pInterp_q0, pInterp_q1, b );"
					+"}"
					
					+ "void main(){" + "\n"
						+ "vec4 dis = linear(sampler1, vTexCoo);" + "\n"
						+ "vec2 tex = vec2(vTexCoo2.x + (dis.r*vScale), vTexCoo2.y + (dis.g*vScale));" + "\n"
						+ "out_diffuse = texture(sampler1, tex)*vColor;" + "\n"
						+ "if (out_diffuse.w == 0.0) {discard;}" 
						+ "out_normal = linear(sampler2, tex)*vec4(1.0,1.0,1.0,vColor.w);"  + "\n"
					+ "}" + "\n";
			
			

			if (debug) {
				FRAGMENT = "#version 330 core" + "\n" 
							+ "in vec2 vTexCoo;" + "\n" + "in vec2 vTexCoo2;" + "\n"
							+ "in vec4 vColor;" + "\n"
							+ "uniform sampler2DRect u_texture;" + "\n" 
							+ "layout(location = 0) out vec4 fragColor;" + "\n"

						+ "void main(){" + "\n" + 
							"vec4 frag = texture(u_texture, vTexCoo);" + "\n"
							+ "if (frag.w == 0.0) {discard;}" 
							+ "\n" + "fragColor = texture(u_texture, vTexCoo2)*vColor;" + "\n"
						+ "}";
			}
			
			super.compile(VERTEX, FRAGMENT, GEOMETRY);
			if (debug) {
				int TEX1_LOC = super.getUniformLocation("u_texture");
				super.setUniform1i(TEX1_LOC, 0);
			}else {
				int TEX1_LOC = super.getUniformLocation("sampler1");
				int TEX2_LOC = super.getUniformLocation("sampler2");
				super.setUniform1i(TEX1_LOC, 0);
				super.setUniform1i(TEX2_LOC, 1);
			}

		}

	}

	int setNew() {
		vTo[current] = count;
		current++;
		vFrom[current] = count;
		return current;
	}

	void flush() {
		bindAndUpload();
		shader.bind();
		int i = 0;
		vTo[current] = count;
		while (i <= current) {
			if (vFrom[i] != vTo[i]) {
				GlHelper.Stencil.setEQUALKeepOnFail(i);
				flush(vFrom[i], vTo[i]);
			}
			i++;
		}
		clear();
		glUseProgram(0);
	}

	void render(float tx1, float ty1, float dx1, float dy1, int w, int h, double scale, int x1, int x2, int y1, int y2, COLOR color, OPACITY opacity) {
		
		if (count >= MAX_ELEMENTS) {
			return;
		}
		
		buffer.putShort((short) x1).putShort((short) y1);
		buffer.putShort((short) x2).putShort((short) y2);
		buffer.putFloat(tx1).putFloat(ty1);
		buffer.putFloat(dx1).putFloat(dy1);
		buffer.putShort((short) (w)).putShort((short) (h));
		buffer.put(color.red()).put(color.green()).put(color.blue()).put(opacity.get());
		buffer.putFloat((float) scale);

		count++;
	}

	@Override
	public void dis() {
		shader.dis();
		super.dis();
	}

}
