package snake2d;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import java.nio.IntBuffer;

import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.sprite.TextureCoords;

final class VboSpritePointsOpti extends VboAbs{


	private final VboShaderAbs shader;
	private final int[] opti;
	private int optiI;
	private final IntBuffer sBuff;

	static VboSpritePointsOpti getDebug(SETTINGS sett) {
		ShaderDebug shader = new ShaderDebug(sett.getNativeWidth(), sett.getNativeHeight(), true);
		return new VboSpritePointsOpti(shader);
	}

	static VboSpritePointsOpti getDeffered(SETTINGS sett) {
		ShaderDebug shader = new ShaderDebug(sett.getNativeWidth(), sett.getNativeHeight(), false);
		return new VboSpritePointsOpti(shader);
	}

	public VboSpritePointsOpti(VboShaderAbs shader) {
		super( GL_POINTS,
				1<<17,
				new VboAttribute(2, GL_SHORT, false, 2), // position upper left
				new VboAttribute(2, GL_SHORT, false, 2), // position lower right
				new VboAttribute(2, GL_UNSIGNED_SHORT, 2), // texture coords1
				new VboAttribute(2, GL_UNSIGNED_SHORT, 2), // texture coords2
				new VboAttribute(2, GL_UNSIGNED_SHORT, 2), // texture coords width
				new VboAttribute(4, GL_UNSIGNED_BYTE, true, 1) // color
		);
		this.shader = shader;
		opti = new int[MAX_ELEMENTS*6];
		sBuff = buffer.asIntBuffer();
	}

	private static class ShaderDebug extends VboShaderAbs {

		ShaderDebug(float width, float height, boolean debug) {
			
			String VERTEX = "#version 330 core" + "\n"

				+ getScreenVec(width, height) 
				+ "const vec2 trans = vec2(-1.0,1.0);" + "\n"

				+ "layout(location = 0) in vec2 in_position1;" + "\n"
				+ "layout(location = 1) in vec2 in_position2;" + "\n" 
				+ "layout(location = 2) in uvec2 in_texCoo1;" + "\n"
				+ "layout(location = 3) in uvec2 in_texCoo2;" + "\n" 
				+ "layout(location = 4) in uvec2 in_texCooWidth;" + "\n" 
				+ "layout(location = 5) in vec4 in_color;" + "\n"
				
				+ "out vec2 gTexCoo1;" + "\n" 
				+ "out vec2 gTexCoo2;" + "\n"
				+ "out vec2 gTexCooWidth;" + "\n"
				+ "out vec2 gPos2;" + "\n" 
				+ "out vec4 gColor;" + "\n"

				+ "void main(){" + "\n" 
					+ "gTexCoo1 = in_texCoo1;" + "\n" 
					+ "gTexCoo2 = in_texCoo2;" + "\n"
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
				+ "in vec2 gNor[];" + "\n"
				
				+ "out vec2 vTexCoo;" + "\n" 
				+ "out vec2 vTexCoo2;" + "\n" 
				+ "out vec4 vColor;" + "\n"

				+ "void main(){" + "\n" 
					+ "vColor = gColor[0];" + "\n"
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
					+ "uniform sampler2DRect sampler1;" + "\n" 
					+ "uniform sampler2DRect sampler2;" + "\n"

					+ "layout(location = 0) out vec4 out_diffuse;" + "\n" 
					+ "layout(location = 1) out vec4 out_normal;" + "\n"

					+ "float ww;" + "\n"
					
					+ "void main(){" + "\n"
						+ "out_diffuse = texture(sampler1, vTexCoo);" + "\n"
						+ "ww = out_diffuse.w;" + "\n"
						+ "if (out_diffuse.w == 0){discard;}" + "\n" 
						+ "out_diffuse = texture(sampler1, vTexCoo2)*vColor;" + "\n"
						+ "out_diffuse.w = out_diffuse.w*ww;" + "\n"
						//fixes roads, ruins water
						+ "if (out_diffuse.w == 0){discard;}" + "\n" 
						+ "if (vColor.w < 1){out_normal = vec4(0.0,0.0,0.0,0.0);}" + "\n" 
						+ "else{" + "\n"
							+ "out_normal = texture(sampler2, vTexCoo2);" + "\n" 
						+ "}" + "\n"
						
					+ "}" + "\n";

			if (debug) {
				FRAGMENT = "#version 330 core" + "\n" 
							+ "in vec2 vTexCoo;" + "\n" + "in vec2 vTexCoo2;" + "\n"
							+ "in vec4 vColor;" + "\n"
							+ "uniform sampler2DRect u_texture;" + "\n" 
							+ "out vec4 fragColor;" + "\n"

						+ "void main(){" + "\n" + 
							"vec4 frag = texture(u_texture, vTexCoo);" + "\n"

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
		
		if (count == 0) {
			clear();
			return;
		}
		
		int off = 0;
		bind();
		shader.bind();
		vTo[current] = count;
		while(count > 0) {
			int am = count;
			if (am > 0x01000)
				am = 0x01000;
			
			sBuff.put(opti, off*6, am*6);
			buffer.position(sBuff.position()*4);
			sBuff.clear();
			optiI = 0;
			upload();
			shader.bind();
			
			
			for (int i = 0; i <= current; i++) {
				int f = vFrom[i]-off;
				int t = vTo[i]-off;
				if (t < 0)
					continue;
				if (f < 0)
					f = 0;
				if (t <= f)
					continue;
				
					GlHelper.Stencil.setLEQUALreplaceOnPass(i);
				flush(f, t);
				
			}
			off+= am;
			count -= am;
		}
		
		clear();
		glUseProgram(0);
		
//		sBuff.put(opti, 0, optiI);
//		buffer.position(sBuff.position()*4);
//		
//		sBuff.clear();
//		optiI = 0;
//		
//		bindAndUpload();
//		shader.bind();
//		int i = 0;
//		vTo[current] = count;
//		while (i <= current) {
//			if (vFrom[i] != vTo[i]) {
//				GlHelper.Stencil.setLEQUALreplaceOnPass(i);
//				flush(vFrom[i], vTo[i]);
//			}
//			i++;
//		}
//		clear();
//		glUseProgram(0);
	}
	
	@Override
	public void clear() {
		optiI = 0;
		super.clear();
	}

	void render(TextureCoords t, int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4, COLOR color,
			OPACITY opacity) {
		
		render(t, t, x1, x2, y1, y2, color, opacity);
	}

	void render(TextureCoords t, TextureCoords to, int x1, int x2, int y1, int y2, COLOR color, OPACITY opacity) {
		
		
		if (count >= MAX_ELEMENTS) {
			return;
		}

		
		opti[optiI] = ((y1) << 16) | ((x1 & 0x0FFFF));
		opti[optiI+1] = ((y2) << 16) | ((x2 & 0x0FFFF));
		
		opti[optiI+2] = ((t.y1()) << 16) | ((t.x1()));
		opti[optiI+3] = ((to.y1()) << 16) | ((to.x1()));
		
		opti[optiI+4] = ((t.y2()-t.y1()) << 16) | ((t.x2()-t.x1()));
		
		opti[optiI+5] = (((opacity.get()) << 24) | ((color.blue()&0x0FF) << 16) | ((color.green()&0x0FF) <<8) | ((color.red()&0x0FF)));
		
		
		optiI += 6;
		count++;
	}

	@Override
	public void dis() {
		shader.dis();
		super.dis();
	}

}
