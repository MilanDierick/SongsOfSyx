
package snake2d;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import org.lwjgl.opengl.GL11;

import snake2d.util.light.LIGHT_AMBIENT;

class VboLightAmbient extends VboAbs{
    
	private boolean specialLayer;
	private final Shader shader;
	
    VboLightAmbient(SETTINGS sett){
    	
    	super( GL11.GL_TRIANGLES,
    			500,
    			new VboAttribute(3, GL_FLOAT, false, 4), //direction/centre position	3*4
    			new VboAttribute(2, GL_SHORT, false, 2), //coo							4
    			new VboAttribute(3, GL_FLOAT, false, 4), //colour						3*4
    			new VboAttribute(4, GL_UNSIGNED_BYTE, true, 1) //shaded + 3 padding
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
        			
        			+ "layout(location = 0) in vec3 in_tilt;" + "\n"
        			+ "layout(location = 1) in vec2 in_posXY;" + "\n"
        			+ "layout(location = 2) in vec3 in_color;" + "\n"
        			+ "layout(location = 3) in vec4 in_shaded;" + "\n"
        			
        			+ "out vec3 v_tilt;" + "\n"
        			+ "out vec3 v_color;" + "\n"
        			+ "out vec3 v_dir;" + "\n"
        			+ "out vec2 v_sampler_coo;" + "\n"
        			
        			+ "void main(){" + "\n"
    					+ "v_tilt = in_tilt;" + "\n"
    					+ "v_color = in_color;" + "\n"
        				+ "v_sampler_coo = in_posXY*screen/2.0;" + "\n"
        				+ "v_dir = (v_tilt - vec3(in_posXY, 0.0));" + "\n"
        				+ "gl_Position = vec4((in_posXY * screen)+trans, in_shaded.x, 1.0);" + "\n"
        			+ "}";
         	
         	
        	String FRAGMENT = 
        			"#version 330 core" + "\n"
        			
        			+ "in vec3 v_tilt;" + "\n"
        			+ "in vec2 v_sampler_coo;" + "\n"
        			+ "in vec3 v_color;" + "\n"
        			+ "in vec3 v_dir;" + "\n"
        			+ "uniform sampler2D Tdiffuse;" + "\n"
        			+ "uniform sampler2D Tnormal;" + "\n"
        			
    				+ "layout(location = 0) out vec4 fragColor;" + "\n"
    				
    				+ "vec4 texColor;" + "\n"
    				+ "vec3 normal;" + "\n"

    				+ "float intensity;" + "\n"
    				+ "float dottis;" + "\n"
    				
        			
        			+ "void main(){" + "\n"
        				
        				+ "texColor = texture(Tdiffuse, v_sampler_coo);" + "\n"
        				+ "if (texColor.w <= 0.0){discard; return;}" + "\n"
        				
        				+ "normal = texture(Tnormal, v_sampler_coo).rgb;" + "\n"
        				+ "normal *= 2.0;" + "\n"
        				+ "normal -= 1.0;" + "\n"
    					

    					+ "dottis = dot(normal.rgb, v_tilt);"
    					+ "if (dottis > 0.0){" + "\n"
        					+ "fragColor.rgb = texColor.rgb*dottis*v_color.rgb;" + "\n"
        				+ "}else{" + "\n"
        					+ "discard;" + "\n"
    					+ "}" + "\n"
        			+ "}";
        	
         	
        	super.compile(VERTEX, FRAGMENT);
    		
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
	
    void render(LIGHT_AMBIENT l,
    		int x1, int x2, int y1, int y2, byte depth){
    	
    	buffer.putFloat(l.x()).putFloat(l.y()).putFloat(l.z());
    	buffer.putShort((short)x1).putShort((short)y2);
    	buffer.putFloat((float)l.r()).putFloat((float)l.g()).putFloat((float)l.b());
    	buffer.put(depth);
    	
    	buffer.put(Byte.MAX_VALUE).put(Byte.MAX_VALUE).put(Byte.MAX_VALUE);
    	
    	//SE
    	buffer.putFloat(l.x()).putFloat(l.y()).putFloat(l.z());
    	buffer.putShort((short)x2).putShort((short)y2);
    	buffer.putFloat((float)l.r()).putFloat((float)l.g()).putFloat((float)l.b());
    	buffer.put(depth);
    	
    	buffer.put(Byte.MAX_VALUE).put(Byte.MAX_VALUE).put(Byte.MAX_VALUE);
    	
    	//NW
    	buffer.putFloat(l.x()).putFloat(l.y()).putFloat(l.z());
    	buffer.putShort((short)x1).putShort((short)y1);
    	buffer.putFloat((float)l.r()).putFloat((float)l.g()).putFloat((float)l.b());
    	buffer.put(depth);
    	
    	buffer.put(Byte.MAX_VALUE).put(Byte.MAX_VALUE).put(Byte.MAX_VALUE);
    	
    	//NE
    	buffer.putFloat(l.x()).putFloat(l.y()).putFloat(l.z());
    	buffer.putShort((short)x2).putShort((short)y1);
    	buffer.putFloat((float)l.r()).putFloat((float)l.g()).putFloat((float)l.b());
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
