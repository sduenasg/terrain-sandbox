#ifdef GL_FRAGMENT_PRECISION_HIGH
    precision highp float;
#else
    precision mediump float;
#endif
/*
*The spec doesn't define a default float precision for fragment shaders. It's not mandatory for devices
*to provide the highp precision, so we fallback to mediump on those devices where highp is not supported
*
*Vertex shaders default to highp if no precision is declared
*Fragment shaders don't have a default precision, it's mandatory to declare it
*/
attribute vec2 a_gridPosition; //normalized [0,1] position of this vertex in the full vertex grid
attribute vec3 a_barycentric;

//basic uniforms
uniform mat4 u_MVPMatrix;
uniform mat4 u_MVMatrix;    		
uniform vec3 u_LightPos; //Light position in eye space

// terrain CDLOD
uniform sampler2D u_heightmap;
uniform float gridDim;
uniform float quad_scale; //Quad size of the current lod grid mesh
uniform vec3 range; //x= range, y= 1/(morphend-morphstart) - distancia que abarca este rango
uniform vec3 cameraPosition;
uniform vec3 meshInfo; //x=meshSize in distance units, y=chunksize in distance units, z= yscale
uniform vec3 nodeoffset;

varying vec4 v_Position;
varying vec2 v_TexCoordinate;
varying vec4 depthPosition;
varying vec3 v_color;
varying float distancef;
varying vec3 barycentric;
varying float morph;

/**Returns the Height value using uv coords as input*/
float getHeightuv(vec2 v) {
    float heightmap = texture2D( u_heightmap, v).r;
	return meshInfo.z * heightmap;
}

//get the plane's uvs from an xz position
vec2 getuvsxy(vec2 v)
{
  vec2 uv=vec2( v.x, v.y);
  uv=uv/meshInfo.x;

  return uv;
}

vec2 morphVertex( vec2 gridPos, vec2 worldPos, float morph) {
	vec2 fracPart = vec2(quad_scale) * fract(gridPos.xy * vec2(gridDim)*0.5) * 2.0/vec2(gridDim);
    return worldPos - fracPart * morph;
}

void main()                                                 	
{
	vec4 hmpos=vec4(0);
    hmpos.w=1.0;
	hmpos.xz=a_gridPosition * quad_scale + nodeoffset.xz ;

    vec2 uvcoords = getuvsxy(hmpos.xz);

    hmpos.y = getHeightuv(uvcoords);

    float dist = distance(hmpos.xyz,cameraPosition);
    float morphLerpK = 1.0 - clamp(range.x - dist * range.y, 0.0, 1.0 );
    //float morphLerpK  = 1.0 - smoothstep(0.0,1.0,range.x - dist * range.y);

    hmpos.xz = morphVertex(a_gridPosition, hmpos.xz, morphLerpK); //morphed vertex

    //get the morphed vertex' texture coordinates
    uvcoords = getuvsxy(hmpos.xz);
    hmpos.y = getHeightuv(uvcoords); //get morphed vertex's viewportHeight from the viewportHeight map
    distancef = length (hmpos.xyz - cameraPosition);
    v_TexCoordinate = uvcoords;
	v_Position = u_MVMatrix * hmpos;

    vec4 finalpos = u_MVPMatrix * hmpos;
	depthPosition = finalpos;
    float distance = abs(v_Position.z/v_Position.w);
    barycentric = a_barycentric;
    gl_Position = finalpos;
    morph=morphLerpK;
}
