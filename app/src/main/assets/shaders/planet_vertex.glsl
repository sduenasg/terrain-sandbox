#version 300 es
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

const int numberOfChunks = 50;
const float _TransitionWidth = .03;
const float _FresnelExponent = .1;
const float atmoBorderWidth=1.9;

in vec2 a_gridPosition; //position of this vertex in the vertex grid range: [0,gridSize]
in vec3 a_barycentric;

//basic uniforms
uniform mat4 u_MVPMatrix;
uniform mat4 u_MVMatrix;    		
uniform vec3 u_LightPos; //Light position in eye space

// terrain CDLOD
uniform sampler2D u_heightMap;
uniform sampler2D u_normalMap;

//specific node uniforms
uniform float quad_scale; //Quad size of the current lod grid mesh
uniform vec2 range; //x= range, y= 1/(morphend-morphstart) - current range distance inverted
uniform vec2 nodeoffset;//position offset of the patch this vertex belongs to
uniform float lodlevel;
uniform float gridDim;

//general uniforms
uniform vec3 cameraPosition;
uniform vec3 meshInfo; //x=meshSize in distance units, y=patch size in distance units, z= yscale
uniform vec3 ambientLight;

out vec4 v_Position;
out vec2 v_TexCoordinate;
out vec4 depthPosition;
out float distancef;
out vec3 barycentric;
out float morph;
out vec3 v_normal;

#define PI 3.14159265;

// 1/(size of the texture) = the value to move one pixel up/down/left/right
const float texelSize=1.0/2080.0;

//Returns the Height value using uv coords as input
float getHeightuv(in vec2 uv, in bool usefilter) {

    float heightmap=0.0;
    heightmap = texture(u_heightMap, uv).r;//make sure the heightmaps are loaded with the NEAREST mode(no filter)
	return meshInfo.z * heightmap;
}

//get the plane's uvs from an xz position
vec2 getuvsxy(in vec2 v)
{
    vec2 uv =  v/meshInfo.x;
    return uv;
}

vec2 morphVertex( in vec2 gridPos, in vec2 worldPos, in float morph) {
    vec2 fracPart = vec2(quad_scale) * fract(gridPos.xy * vec2(gridDim)*0.5) * 2.0/vec2(gridDim);
    return worldPos - fracPart * morph;
}

/*Spherization method 1 -> center the cube around the origin, normalize the vertex position, undo centering.
Doesn't distribute the vertices very evenly around the sphere, but it's the faster method*/
vec3 getRadiusVector(in vec4 p, in float radius)
{
    return normalize(p.xyz-vec3(radius,-radius,radius));
}

vec4 spherizePointNormalization(in vec4 p, in float radius)
{
    vec4 result;
    result.xyz = getRadiusVector(p,radius) * radius + vec3(radius,-radius,radius);
    result.w=1.0;
    return result;
}

vec4 applyHeightmapToSpherizedPoint(in vec4 p, in float heightValue)
{
    float radius=meshInfo.x * 0.5;
    vec3 radiusVector = normalize(p.xyz-vec3(radius,-radius,radius));
    return vec4(p.xyz + radiusVector * heightValue,1.0);
}

//morph original height with the fully morphed height using the morphLerpK value
float interpolateHeights(in float oldheight, in vec2 newUvCoords,in float morph)
{
    return(mix(oldheight,getHeightuv(newUvCoords,false),morph));
}

vec3 getNormal(vec2 uv) {
    /*
    The normal map is baked on Blender (object space normal map). Coordinates there are
    different than here (.xzy, y=-yb)
    */
    vec3 nobj = texture( u_normalMap, uv).xzy *2.0 - 1.0;
    nobj.y = -nobj.y;
    nobj = ( u_MVMatrix * vec4(nobj, 0.0)).xyz;
	return normalize(nobj);
}

void main()                                                 	
{
    float radius=meshInfo.x * 0.5;
	vec4 hmpos=vec4(0);
    hmpos.w = 1.0;
	hmpos.xz = a_gridPosition * quad_scale+ nodeoffset.xy ;

    vec2 uvcoords = getuvsxy(hmpos.xz);

    //sample vertex' original position height value
    float height=getHeightuv(uvcoords,false);

    //distance from the spherized and model-transformed vertex to the camera
    float dist = length(u_MVMatrix * applyHeightmapToSpherizedPoint(spherizePointNormalization(hmpos,radius),height));
    float morphLerpK = 1.0 - clamp(range.x - dist * range.y, 0.0, 1.0 );

    hmpos.xz = morphVertex(a_gridPosition, hmpos.xz, morphLerpK); //morphed vertex


    //get the morphed vertex' texture coordinates
    uvcoords = getuvsxy(hmpos.xz);

    hmpos = spherizePointNormalization(hmpos,radius); //spherize
    hmpos = applyHeightmapToSpherizedPoint(hmpos,interpolateHeights(height,uvcoords,morphLerpK));//apply heightmap
    v_Position = u_MVMatrix * hmpos;
    distancef = length(v_Position);

    v_TexCoordinate = uvcoords;

    vec4 finalpos = u_MVPMatrix * hmpos;
	depthPosition = finalpos;
    float distance = abs(v_Position.z/v_Position.w);
    barycentric = a_barycentric;
    gl_Position = finalpos;
    morph=morphLerpK;

    vec3 radiusVector = getRadiusVector(hmpos,radius);
    v_normal=getNormal(uvcoords);

}
