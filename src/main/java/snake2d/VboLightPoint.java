
package snake2d;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import snake2d.util.light.LIGHT_POINT;

class VboLightPoint extends VboAbs{
    
	private boolean specialLayer;
	private final Shader shader;
	
    VboLightPoint(SETTINGS sett){
    	
    	super( GL_POINTS,
    			10000*2,
    			new VboAttribute(3, GL_FLOAT, false, 4), //direction/centre position 	3*4
    			new VboAttribute(2, GL_SHORT, false, 2), //coo							4
    			new VboAttribute(2, GL_SHORT, false, 2), //coo2							4
    			new VboAttribute(4, GL_FLOAT, false, 4), //colour						4*4
    			new VboAttribute(4, GL_UNSIGNED_BYTE, true, 1),  //coo intensity		4
    			new VboAttribute(1, GL_FLOAT, false, 4),  //radius						4
    			new VboAttribute(4, GL_UNSIGNED_BYTE, true, 1)  //depth + 3 padding		4
    	);
    	shader = new Shader(sett.getNativeWidth(), sett.getNativeHeight());
    	
    }   

    private static class Shader extends VboShaderAbs{

        private final int DIFFUSE_LOC;
        private final int NORMAL_LOC;
    	
    	Shader(float width, float height){
    		
         	String VERTEX = "#version 330 core" + "\n"
        			+ getScreenVec(width, height)
        			+ "const vec2 trans = vec2(-1.0,1.0);" + "\n"
        			+ "const float sqrt2 = sqrt(2);" + "\n"
        			
        			+ "layout(location = 0) in vec3 in_centreCoords;" + "\n"
        			+ "layout(location = 1) in vec2 in_posXY1;" + "\n"
        			+ "layout(location = 2) in vec2 in_posXY2;" + "\n"
        			+ "layout(location = 3) in vec4 in_color;" + "\n"
        			+ "layout(location = 4) in vec4 in_sideStrength;" + "\n"
        			
        			+ "layout(location = 5) in float in_radius;" + "\n"
        			+ "layout(location = 6) in vec4 in_shaded;" + "\n"
        			
					+ "out vec2 g_pos2;" + "\n"
        			+ "out vec4 g_color;" + "\n"
        			+ "out vec4 g_sideStrength;" + "\n"
        			+ "out vec2 g_sampler_coo;" + "\n"
        			+ "out vec2 g_sampler_coo2;" + "\n"
        			+ "out float g_radius;" + "\n"
        			+ "out vec3 g_dir;" + "\n"
        			+ "out vec3 g_dir2;" + "\n"
        			
        			+ "void main(){" + "\n"
    					+ "g_color = in_color;" + "\n"
    					+ "g_radius = in_radius;" + "\n"
    					+ "g_sideStrength = in_sideStrength;" + "\n"
        				+ "g_sampler_coo = in_posXY1*screen/2;" + "\n"
        				+ "g_sampler_coo2 = in_posXY2*screen/2;" + "\n"
        				+ "g_pos2 = vec2((in_posXY2 * screen)+trans);" + "\n" 
        				+ "g_dir = (in_centreCoords - vec3(in_posXY1, 0.0));" + "\n"
        				+ "g_dir2 = (in_centreCoords - vec3(in_posXY2, 0.0));" + "\n"
        				+ "gl_Position = vec4((in_posXY1 * screen)+trans, in_shaded.x, 1.0);" + "\n"
        			+ "}";
         	
			String GEOMETRY = "#version 330 core" + "\n"

				+ "layout (points) in;" + "\n" 
				+ "layout (triangle_strip, max_vertices = 4) out;" + "\n"
				
				+ "in vec2 g_pos2[];" + "\n"
				+ "in vec4 g_color[];" + "\n"
				+ "in vec4 g_sideStrength[];" + "\n"
				+ "in vec2 g_sampler_coo[];" + "\n"
				+ "in vec2 g_sampler_coo2[];" + "\n"
				+ "in float g_radius[];" + "\n"
				+ "in vec3 g_dir[];" + "\n"
				+ "in vec3 g_dir2[];" + "\n"
				
        		+ "out vec4 v_color;" + "\n"
        		+ "out vec3 v_dir;" + "\n"
        		+ "out float v_sideStrength;" + "\n"
        		+ "out vec2 v_sampler_coo;" + "\n"
        		+ "out float v_radius;" + "\n"

				+ "void main(){" + "\n" 
				
					+ "v_color = g_color[0];"
					+ "v_dir = g_dir[0];" + "\n"
					+ "v_sideStrength = g_sideStrength[0].r;" + "\n"
					+ "v_sampler_coo = g_sampler_coo[0];" + "\n"
					+ "v_radius = g_radius[0];" + "\n"
					+ "gl_Position = gl_in[0].gl_Position;" + "\n"
					+ "EmitVertex();" + "\n"
					
					+ "v_color = g_color[0];"
					+ "v_dir = vec3(g_dir2[0].x, g_dir[0].y, g_dir[0].z);" + "\n"
					+ "v_sideStrength = g_sideStrength[0].g;" + "\n"
					+ "v_sampler_coo = vec2(g_sampler_coo2[0].x, g_sampler_coo[0].y);" + "\n"
					+ "v_radius = g_radius[0];" + "\n"
					+ "gl_Position = gl_in[0].gl_Position;" + "\n"
					+ "gl_Position.x = g_pos2[0].x;" + "\n"
					+ "EmitVertex();" + "\n"
					
					+ "v_color = g_color[0];"
					+ "v_dir = vec3(g_dir[0].x, g_dir2[0].y, g_dir[0].z);" + "\n"
					+ "v_sideStrength = g_sideStrength[0].a;" + "\n"
					+ "v_sampler_coo = vec2(g_sampler_coo[0].x, g_sampler_coo2[0].y);" + "\n"
					+ "v_radius = g_radius[0];" + "\n"
					+ "gl_Position = gl_in[0].gl_Position;" + "\n"
					+ "gl_Position.y = g_pos2[0].y;" + "\n"
					+ "EmitVertex();" + "\n"
					
					+ "v_color = g_color[0];"
					+ "v_dir = vec3(g_dir2[0].x, g_dir2[0].y, g_dir[0].z);" + "\n"
					+ "v_sideStrength = g_sideStrength[0].b;" + "\n"
					+ "v_sampler_coo = vec2(g_sampler_coo2[0].x, g_sampler_coo2[0].y);" + "\n"
					+ "v_radius = g_radius[0];" + "\n"
					+ "gl_Position = gl_in[0].gl_Position;" + "\n"
					+ "gl_Position.x = g_pos2[0].x;" + "\n"
					+ "gl_Position.y = g_pos2[0].y;" + "\n"
					+ "EmitVertex();" + "\n"

					+ "EndPrimitive();" + "\n"
					
				+ "}";
         	
        	String FRAGMENT = 
        			"#version 330 core" + "\n"
        			
        			+ "in vec2 v_sampler_coo;" + "\n"
        			+ "in vec4 v_color;" + "\n"
        			+ "in float v_sideStrength;" + "\n"
        			+ "in vec3 v_dir;" + "\n"
        			+ "uniform sampler2D Tdiffuse;" + "\n"
        			+ "uniform sampler2D Tnormal;" + "\n"
        			+ "in float v_radius;" + "\n"
        			
    				+ "layout(location = 0) out vec4 fragColor;" + "\n"
    				
    				+ "vec4 texColor;" + "\n"
    				+ "vec4 normal;" + "\n"

    				+ "float intensity;" + "\n"
    				+ "float dottis;" + "\n"
    				
        			
        			+ "void main(){" + "\n"
        				
        				+ "texColor = texture(Tdiffuse, v_sampler_coo);" + "\n"
        				+ "if (texColor.w <= 0.0){discard; return;}" + "\n"
        				
        				+ "normal = texture(Tnormal, v_sampler_coo);" + "\n"
        				+ "normal.rgb *= 2.0;" + "\n"
        				+ "normal.rgb -= 1.0;" + "\n"
    					+ "intensity = length(v_dir.xy)/v_radius;" + "\n"
    					+ "if (intensity >= 1.0){discard;}"  + "\n"
    					+ "intensity = 1.0 - intensity;" + "\n"
    					+ "intensity = pow(intensity, v_color.w);" + "\n"
    					+ "dottis = dot(normal.rgb, normalize(v_dir));" + "\n"
    					+ "if (dottis > 0.0){" + "\n"
        					+ "fragColor.rgb = v_sideStrength*texColor.rgb*dottis*intensity*v_color.rgb;" + "\n"
        				+ "}else{discard;}" + "\n"
        			+ "}";
        	
         	
        	super.compile(VERTEX, FRAGMENT, GEOMETRY);
    		
            DIFFUSE_LOC = super.getUniformLocation("Tdiffuse");
            NORMAL_LOC = super.getUniformLocation("Tnormal");
            super.setUniform1i(DIFFUSE_LOC, 2);
            super.setUniform1i(NORMAL_LOC, 3);
        	
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
    
	void flush(){
		
		
		bindAndUpload();
		
		GlHelper.setBlendAdditative();
		GlHelper.enableDepthTest(true);
		GlHelper.setDepthTestLess();
		shader.bind();
		int i = 0;
		vTo[current] = count;
		while (i <= current){
			if (specialLayer && i == current){
				GlHelper.Stencil.setLEQUALKeepOnFail(i);
			}else{
				GlHelper.Stencil.setEQUALKeepOnFail(i);
			}
			flush(vFrom[i], vTo[i]);
			i++;
		}
		glUseProgram(0);
		GlHelper.enableDepthTest(false);
		GlHelper.setBlendNormal();
		clear();

	}
	
    void render(LIGHT_POINT l, float x, float y, float z, int radius, 
    		int x1, int x2, int y1, int y2, byte ne, byte se, byte sw, byte nw, byte depth){
    	
    	if (count >= MAX_ELEMENTS) {
			return;
		}
    	
    	float d = l.getRadius();
    	d /= radius;

    	buffer.putFloat(x).putFloat(y).putFloat(l.cz());
    	buffer.putShort((short)x1).putShort((short)y1).putShort((short)x2).putShort((short)y2);
    	
    	buffer.putFloat(l.getRed()).putFloat(l.getGreen()).putFloat(l.getBlue()).putFloat(l.getFalloff()*d);
    	buffer.put(nw).put(ne).put(se).put(sw);
    	buffer.putFloat(radius);
    	buffer.put(depth);
    	buffer.put(Byte.MAX_VALUE).put(Byte.MAX_VALUE).put(Byte.MAX_VALUE);
    	
    	count++;
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
