#ifdef GL_FRAGMENT_PRECISION_HIGH
    precision highp float;
#else
    precision mediump float;
#endif

attribute vec2 a_gridPosition; //normalized [0,1] position of this vertex in the vertex grid

//basic uniforms
uniform mat4 u_MVPMatrix; //MVP from the light

// terrain CDLOD
uniform sampler2D u_heightmap;
uniform float gridDim;
uniform float quad_scale; //Quad size of the current lod grid mesh
uniform vec3 range; //x= range, y= 1/(morphend-morphstart) - distancia que abarca este rango
uniform vec3 cameraPosition;
uniform vec3 meshInfo; //x=meshSize in distance units, y=chunksize in distance units, z= yscale
uniform vec3 nodeoffset;

/**Returns the height value using uv coords as input*/
float getHeightuv(vec2 v) {
    float heightmap=texture2D( u_heightmap, v).r;
	return meshInfo.z * heightmap;
}

//get the plane's uvs from an xz position
vec2 getuvsxy(vec2 v)
{
  vec2 uv=vec2( v.x , v.y );
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
    hmpos.y = getHeightuv(uvcoords); //get morphed vertex's viewportHeight from the viewportHeight map

	gl_Position = u_MVPMatrix * hmpos;
}
