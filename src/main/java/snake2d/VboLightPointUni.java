
package snake2d;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

class VboLightPointUni extends VboAbs{
    
	public final static int MAX_ELEMENTS = 65536/4;
	private float[] lights = new float[255*5];
	private final float inv = 1f/255f;
	private final Shader shader;
	private boolean specialLayer;
	
    VboLightPointUni(SETTINGS sett){
    	
    	super( GL_POINTS,
    			MAX_ELEMENTS,
    			new VboAttribute(4, GL_SHORT, false, 2) //centre
    	);
    	shader = new Shader(sett.getNativeWidth(), sett.getNativeHeight());
    	
    }   

    private static class Shader extends VboShaderAbs{

        private final int DIFFUSE_LOC;
        private final int NORMAL_LOC;
    	
		private final int uColor;
		private final int uShaded;
		private final int uFalloff;
        
    	Shader(float width, float height){
    		
         	String VERTEX = "#version 330 core" + "\n"
        			+ getScreenVec(width, height)
        			+ "const vec2 trans = vec2(-1.0,1.0);" + "\n"
        			+ "const float sqrt2 = sqrt(2);" + "\n"
        			
					+ "uniform float u_shaded = 1.0;" + "\n"
        			+ "layout(location = 0) in vec4 in_centre;" + "\n"
        			
					+ "out vec3 g_pos1;" + "\n"
					+ "out vec3 g_pos2;" + "\n"
        			+ "out vec2 g_sampler1;" + "\n"
        			+ "out vec2 g_sampler2;" + "\n"
        			+ "out vec3 g_dir1;" + "\n"
        			+ "out vec3 g_dir2;" + "\n"
        			+ "out float g_radius;" + "\n"
        			+ "out vec2 g_off;" + "\n"
        			+ "out vec3 g_centre;" + "\n"
        			+ "vec2 tmp;" + "\n"
        			
        			+ "void main(){" + "\n"
    					+ "g_radius = in_centre.w;" + "\n"
    					+ "g_centre = in_centre.xyz;" + "\n"
    					+ "g_pos1 = vec3(in_centre.x -g_radius, in_centre.y-g_radius, 0);" + "\n"
    					+ "g_pos2 = vec3(in_centre.x +g_radius, in_centre.y+g_radius, 0);" + "\n"
        				+ "g_sampler1 = vec2(g_pos1.xy)*screen/2;" + "\n"
        				+ "g_sampler2 = vec2(g_pos2.xy)*screen/2;" + "\n"
        				
        				+ "g_off = vec2(g_pos2.xy*screen)+trans;" + "\n"
        				+ "gl_Position = vec4((g_pos1.xy * screen)+trans, u_shaded, 1.0);" + "\n"
        			+ "}";
         	
			String GEOMETRY = "#version 330 core" + "\n"

				+ "layout (points) in;" + "\n" 
				+ "layout (triangle_strip, max_vertices = 4) out;" + "\n"
				
				+ "uniform float dim = 0;" + "\n"
				
				+ "in vec3 g_pos1[];" + "\n"
				+ "in vec3 g_pos2[];" + "\n"
				+ "in vec2 g_sampler1[];" + "\n"
				+ "in vec2 g_sampler2[];" + "\n"
				+ "in float g_radius[];" + "\n"
				+ "in vec2 g_off[];" + "\n"
				+ "in vec3 g_centre[];" + "\n"

				
				+ "out float v_radius;" + "\n"
        		+ "out vec3 v_pos;" + "\n"
        		+ "out vec2 v_sampler;" + "\n"
        		+ "out vec3 v_centre;" + "\n"
        		

				+ "void main(){" + "\n" 
				
					+ "v_radius = g_radius[0];"
					+ "v_centre = g_centre[0];" + "\n"
					+ "v_pos = vec3(g_pos1[0].x, g_pos1[0].y, 0);" + "\n"
					+ "v_sampler = vec2(g_sampler1[0].x, g_sampler1[0].y);" + "\n"
					+ "gl_Position = gl_in[0].gl_Position;" + "\n"
					+ "EmitVertex();" + "\n"
					
					+ "v_radius = g_radius[0];"
					+ "v_centre = g_centre[0];" + "\n"
					+ "v_pos = vec3(g_pos2[0].x, g_pos1[0].y, 0);" + "\n"
					+ "v_sampler = vec2(g_sampler2[0].x, g_sampler1[0].y);" + "\n"
					+ "gl_Position = gl_in[0].gl_Position;" + "\n"
					+ "gl_Position.x = g_off[0].x;" + "\n"
					+ "EmitVertex();" + "\n"
					
					+ "v_radius = g_radius[0];"
					+ "v_centre = g_centre[0];" + "\n"
					+ "v_pos = vec3(g_pos1[0].x, g_pos2[0].y, 0);" + "\n"
					+ "v_sampler = vec2(g_sampler1[0].x, g_sampler2[0].y);" + "\n"
					+ "gl_Position = gl_in[0].gl_Position;" + "\n"
					+ "gl_Position.y = g_off[0].y;" + "\n"
					+ "EmitVertex();" + "\n"
					
					+ "v_radius = g_radius[0];"
					+ "v_centre = g_centre[0];" + "\n"
					+ "v_pos = vec3(g_pos2[0].x, g_pos2[0].y, 0);" + "\n"
					+ "v_sampler = vec2(g_sampler2[0].x, g_sampler2[0].y);" + "\n"
					+ "gl_Position = gl_in[0].gl_Position;" + "\n"
					+ "gl_Position.x = g_off[0].x;" + "\n"
					+ "gl_Position.y = g_off[0].y;" + "\n"
					+ "EmitVertex();" + "\n"

					+ "EndPrimitive();" + "\n"
					
				+ "}";
         	
        	String FRAGMENT = 
        			"#version 330 core" + "\n"
        			
					+ "uniform vec3 u_color = vec3(1.0,1.0,1.0);" + "\n"
					+ "uniform float falloff = 2;" + "\n"
					+ "uniform sampler2D Tdiffuse;" + "\n"
					+ "uniform sampler2D Tnormal;" + "\n"		
        			
					+ "in float v_radius;" + "\n"
        			+ "in vec3 v_pos;" + "\n"
        			+ "in vec2 v_sampler;" + "\n"
        			+ "in vec3 v_centre;" + "\n"
        			
    				+ "layout(location = 0) out vec4 fragColor;" + "\n"
    				
    				+ "vec4 texColor;" + "\n"
    				+ "vec4 normal;" + "\n"
    				+ "vec3 dir;" + "\n"
    				+ "float intensity;" + "\n"
    				+ "float dottis;" + "\n"
    				
        			
        			+ "void main(){" + "\n"
        				
        				+ "texColor = texture(Tdiffuse, v_sampler);" + "\n"
        				+ "if (texColor.w <= 0.0){discard; return;}" + "\n"
        				
        				+ "normal = texture(Tnormal, v_sampler);" + "\n"
        				+ "normal.rgb *= 2.0;" + "\n"
        				+ "normal.rgb -= 1.0;" + "\n"
        				+ "dir = v_centre-v_pos;" + "\n"
    					+ "intensity = length(dir)/v_radius;" + "\n"
    					+ "if (intensity >= 1.0){discard;}"  + "\n"
    					+ "intensity = 1.0 - intensity;" + "\n"
    					+ "intensity = pow(intensity, falloff);" + "\n"
    					+ "dottis = dot(normal.rgb, normalize(dir));" + "\n"
    					+ "if (dottis > 0.0){" + "\n"
        					+ "fragColor.rgb = texColor.rgb*dottis*intensity*u_color.rgb;" + "\n"
        				+ "}else{discard;}" + "\n"
    					+ "" + "\n"
        			+ "}";
        	
         	
        	super.compile(VERTEX, FRAGMENT, GEOMETRY);
    		
            DIFFUSE_LOC = super.getUniformLocation("Tdiffuse");
            NORMAL_LOC = super.getUniformLocation("Tnormal");
            super.setUniform1i(DIFFUSE_LOC, 2);
            super.setUniform1i(NORMAL_LOC, 3);
	        uColor = super.getUniformLocation("u_color");
	        uShaded = super.getUniformLocation("u_shaded");
	        uFalloff = super.getUniformLocation("falloff");
		}
		
		void upload(float r, float g, float b, float shaded, float falloff){
	    	
			super.setUniform(uShaded, shaded);
			super.setUniform(uColor, r, g, b);
			super.setUniform(uFalloff, falloff);
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
			int k = i*5;
			shader.upload(lights[k], lights[k+1], lights[k+2], lights[k+3], lights[k+4]);
			flush(vFrom[i], vTo[i]);
			i++;
		}
		glUseProgram(0);
		GlHelper.enableDepthTest(false);
		GlHelper.setBlendNormal();
		clear();

	}

    void render(short x, short y, short z, short radius){
    	
    	if (count >= MAX_ELEMENTS) {
    		return;
    	}
    	buffer.putShort(x).putShort(y).putShort(z).putShort(radius);
    	
    	count++;
    }
    
	void setLight(float radius, float red, float green, float blue, float falloff, byte depth) {
		
		int i = current*5;
		lights[i] = red;
		lights[i+1] = green;
		lights[i+2] = blue;
		lights[i+3] = (depth & 0x0FF)*inv;
		lights[i+4] = falloff;
	}
    
    @Override
    public void dis() {
    	shader.dis();
    	super.dis();
    }
    
    @Override
    public void clear() {
    	current = 0;
		specialLayer = false;
    	super.clear();
    }
    
}
