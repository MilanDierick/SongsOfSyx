package snake2d;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

class VboTileLight extends VboAbs{


	public final static int MAX_ELEMENTS = 65536/4;
	private float[] lights = new float[255*7];
	private final float inv = 1f/255f;
	private final ShaderDebug shader;
	private boolean specialLayer;

	public VboTileLight(SETTINGS sett) {
		super(GL_POINTS, MAX_ELEMENTS,
				new VboAttribute(2, GL_SHORT, false, 2), // position upper left		//4
				
				new VboAttribute(4, GL_UNSIGNED_BYTE, true, 1), //corner intensity //4
				new VboAttribute(2, GL_SHORT, false, 2) // dimension + 2 padding 
				
			);
		
		shader = new ShaderDebug(sett.getNativeWidth(), sett.getNativeHeight());
	}

	private static class ShaderDebug extends VboShaderAbs {

		private final int uTilt;
		private final int uColor;
		private final int uDepth;
		
		ShaderDebug(float width, float height) {
			
			String VERTEX = "#version 330 core" + "\n"

				+ getScreenVec(width, height) 
				+ "const vec2 trans = vec2(-1.0,1.0);" + "\n"
				+ "uniform float u_depth = 1.0;" + "\n"

				+ "layout(location = 0) in vec2 in_position1;" + "\n"
				+ "layout(location = 1) in vec4 in_cornerI;" + "\n" 
				+ "layout(location = 2) in vec2 in_dim;" + "\n" 
				
				
				+ "out vec2 gPos2;" + "\n"
				+ "out vec2 gSampler1;" + "\n" 
				+ "out vec2 gSampler2;" + "\n" 
				+ "out vec4 gIntens;" + "\n" 
				
				+ "void main(){" + "\n" 
					+ "gIntens = in_cornerI;" + "\n"
					+ "gSampler1 = in_position1*screen/2;" + "\n"
					+ "gSampler2 = vec2(in_position1.x + in_dim.x, in_position1.y + in_dim.x)*screen/2;" + "\n"
					+ "gPos2 = vec2(in_position1.x + in_dim.x, in_position1.y + in_dim.x)*screen + trans;" + "\n"
					+ "gl_Position = vec4((in_position1 * screen)+trans, u_depth, 1.0);" + "\n" 
				+ "}";
			
			String GEOMETRY = "#version 330 core" + "\n"

				+ "layout (points) in;" + "\n" 
				+ "layout (triangle_strip, max_vertices = 4) out;" + "\n"
					
				+ "in vec2 gSampler1[];" + "\n" 
				+ "in vec2 gSampler2[];" + "\n"
				+ "in vec2 gPos2[];" + "\n" 
				+ "in vec4 gIntens[];" + "\n" 
				
				+ "out vec2 vSampler;" + "\n" 
				+ "out float vIntensity;" + "\n"

				+ "void main(){" + "\n" 
					
					+ "vSampler = gSampler1[0];" + "\n"
					+ "gl_Position = gl_in[0].gl_Position;" + "\n"
					+ "vIntensity = gIntens[0].x;" + "\n"
					+ "EmitVertex();" + "\n"
					
					+ "vSampler =  gSampler1[0];" + "\n"
					+ "vSampler.x =  gSampler2[0].x;" + "\n"
					+ "gl_Position = gl_in[0].gl_Position;" + "\n"
					+ "gl_Position.x = gPos2[0].x;" + "\n"
					+ "vIntensity = gIntens[0].y;" + "\n"
					+ "EmitVertex();" + "\n"
					
					
					+ "vSampler =  gSampler1[0];" + "\n"
					+ "vSampler.y =  gSampler2[0].y;" + "\n"
					+ "gl_Position = gl_in[0].gl_Position;" + "\n"
					+ "gl_Position.y = gPos2[0].y;" + "\n"
					+ "vIntensity = gIntens[0].w;" + "\n"
					+ "EmitVertex();" + "\n"
					
					+ "vSampler =  gSampler2[0];" + "\n"
					+ "gl_Position = gl_in[0].gl_Position;" + "\n"
					+ "gl_Position.y = gPos2[0].y;" + "\n"
					+ "gl_Position.x = gPos2[0].x;" + "\n"
					+ "vIntensity = gIntens[0].z;" + "\n"
					+ "EmitVertex();" + "\n"

					+ "EndPrimitive();" + "\n"
					
				+ "}";

			String FRAGMENT = "#version 330 core" + "\n"
        			
        			+ "uniform vec3 v_tilt = vec3(0.0,0.0,1.0);" + "\n"
        			+ "uniform vec3 v_color = vec3(1.0,1.0,1.0);" + "\n"
        			+ "uniform sampler2D Tdiffuse;" + "\n"
        			+ "uniform sampler2D Tnormal;" + "\n"
        			
					+ "in vec2 vSampler;" + "\n"
					+ "in float vIntensity;" + "\n"
        			
    				+ "layout(location = 0) out vec4 fragColor;" + "\n"
    				
    				+ "vec4 texColor;" + "\n"
    				+ "vec3 normal;" + "\n"

    				
    				+ "float dottis;" + "\n"
    				
        			
        			+ "void main(){" + "\n"
        				
        				+ "texColor = texture(Tdiffuse, vSampler);" + "\n"
        				+ "if (texColor.w <= 0.0){discard; return;}" + "\n"
        				
        				+ "normal = texture(Tnormal, vSampler).rgb;" + "\n"
        				+ "normal *= 2.0;" + "\n"
        				+ "normal -= 1.0;" + "\n"
    					

    					+ "dottis = dot(normal.rgb, v_tilt)*vIntensity;"
    					+ "if (dottis > 0.0){" + "\n"
        					+ "fragColor.rgb = texColor.rgb*dottis*v_color.rgb;" + "\n"
        				+ "}else{" + "\n"
        					+ "discard;" + "\n"
    					+ "}" + "\n"
        			+ "}";
			
			super.compile(VERTEX, FRAGMENT, GEOMETRY);
			int DIFFUSE_LOC = super.getUniformLocation("Tdiffuse");
	        int NORMAL_LOC = super.getUniformLocation("Tnormal");
	        super.setUniform1i(DIFFUSE_LOC, 2);
	        super.setUniform1i(NORMAL_LOC, 3);
	        
	        uTilt = super.getUniformLocation("v_tilt");
	        uColor = super.getUniformLocation("v_color");
	        uDepth = super.getUniformLocation("u_depth");
		}
		
		void upload(float r, float g, float b, float x, float y, float z, float depth){
			super.setUniform(uColor, r, g, b);
			super.setUniform(uTilt, x, y, z);
			super.setUniform(uDepth, depth);
		}

	}


	
	void setNew(){
		if (specialLayer)
			return;
		vTo[current] = count;
		current++;
		vFrom[current] = count;
	}
	
	void setNewButKeepLight(){
		if (specialLayer)
			return;
		vTo[current] = count;
		current++;
		vFrom[current] = vFrom[current-1];
		
	}
	
	void setNewFinal(){
		if (specialLayer)
			throw new RuntimeException("can't set final layer twice!");
		specialLayer = true;
		vTo[current] = count;
		current++;
		vFrom[current] = count;
		
	}

	void flush() {
		bindAndUpload();
		
		GlHelper.setBlendAdditative();
		GlHelper.enableDepthTest(true);
		GlHelper.setDepthTestLess();
		shader.bind();
		int i = 0;
		vTo[current] = count;
		while (i <= current){
			if (vFrom[i] == vTo[i]) {
				i++;
				continue;
			}
			if (specialLayer && i == current){
				GlHelper.Stencil.setLEQUALKeepOnFail(i);
			}else{
				GlHelper.Stencil.setEQUALKeepOnFail(i);
			}
			int k = i*7;
			shader.upload(lights[k], lights[k+1], lights[k+2], lights[k+3], lights[k+4], lights[k+5], lights[k+6]);
			flush(vFrom[i], vTo[i]);
			i++;
		}
		glUseProgram(0);
		GlHelper.enableDepthTest(false);
		GlHelper.setBlendNormal();
		clear();

	}

	@Override
	public void clear() {
		super.clear();
		specialLayer = false;
	}
	

	void render(int x1, int y1, int dim, byte nw, byte ne, byte se, byte sw) {
		
		if (count >= MAX_ELEMENTS) {
			return;
		}
		
		buffer.putShort((short) x1).putShort((short) y1);
		buffer.put(nw).put(ne).put(se).put(sw);
		buffer.putShort((short) dim);
		buffer.put(Byte.MAX_VALUE).put(Byte.MAX_VALUE);

		count++;
	}
	
	void setLight(float red, float green, float blue, float x, float y, float z, byte depth) {
		int i = current*7;
		lights[i] = red;
		lights[i+1] = green;
		lights[i+2] = blue;
		lights[i+3] = x;
		lights[i+4] = y;
		lights[i+5] = z;
		lights[i+6] = (depth & 0x0FF)*inv;
	}

	@Override
	public void dis() {
		shader.dis();
		super.dis();
	}

}
