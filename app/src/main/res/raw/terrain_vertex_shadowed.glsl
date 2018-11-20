#ifdef GL_FRAGMENT_PRECISION_HIGH
    precision highp float;
#else
    precision mediump float;
#endif

attribute vec2 a_gridPosition; //normalized [0,1] position of this vertex in the vertex grid
attribute vec3 a_barycentric;

//basic uniforms
uniform mat4 u_MVPMatrix;
uniform mat4 u_MVMatrix;    		
uniform vec3 u_LightPos; //Light position in eye space
uniform mat4 u_shadowmapMVP;//matriz mvp del render del shadowmap

// terrain CDLOD
uniform sampler2D u_heightmap;
uniform sampler2D u_BumpMap;
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
varying vec4 v_ShadowCoord;
varying float morph;

//for the shadows
const mat4 biasMatrix = mat4(0.5, 0.0, 0.0, 0.0,
                              0.0, 0.5, 0.0, 0.0,
                              0.0, 0.0, 0.5, 0.0,
                              0.5, 0.5, 0.5, 1.0);

/**Returns the viewportHeight value using uv coords as input*/
float getHeightuv(vec2 v) {
    float heightmap=texture2D( u_heightmap, v).r;
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

    float dist = distance(cameraPosition,hmpos.xyz);
    float morphLerpK  = 1.0 - clamp(range.x - dist * range.y, 0.0, 1.0 );
    //float morphLerpK  = 1.0 - smoothstep(0.0,0.17,range.x - dist * range.y);

    hmpos.xz = morphVertex(a_gridPosition, hmpos.xz, morphLerpK); //morphed vertex

    //get the morphed vertex' texture coordinates
    uvcoords=getuvsxy(hmpos.xz);
    hmpos.y = getHeightuv(uvcoords); //get morphed vertex's height from the heightmap
    distancef = length (hmpos.xyz-cameraPosition);
    v_TexCoordinate=uvcoords;
	v_Position = u_MVMatrix * hmpos;

    vec4 finalpos = u_MVPMatrix * hmpos;
	depthPosition = finalpos;

 // float distance = abs(v_Position.z/v_Position.w);
    barycentric=a_barycentric;

    mat4 depthBiasMVP = biasMatrix * u_shadowmapMVP;
    v_ShadowCoord = depthBiasMVP * hmpos;
	gl_Position = finalpos;
	morph=morphLerpK;
}
