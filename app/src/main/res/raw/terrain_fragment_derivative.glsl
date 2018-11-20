#ifdef GL_OES_standard_derivatives
#extension GL_OES_standard_derivatives : enable
#endif

#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif

uniform sampler2D u_Texture;  //color map
uniform sampler2D u_Texture2; //detail texture
uniform sampler2D u_Texture3; //normal map
uniform sampler2D u_heightmap; //viewportHeight map (for debugging)
uniform sampler2D u_BumpMap;

uniform float zfar;
uniform float lodlevel;
uniform vec3 ambientLight;
uniform vec3 u_Fogcolor;
uniform vec3 u_LightPos; //ya llega en eye space!! (pre multiplicada por MVMatrix)
uniform mat4 u_MVMatrix;
uniform vec3 range;

varying vec2 v_TexCoordinate;
varying vec4 v_Position;
varying vec4 depthPosition;
varying vec3 barycentric;

const float shininess = 5.0;  //propiedad de shininess del material
const vec3 specularColor = vec3(0.5, 0.5, 0.5);
const float fogScale=0.6;
const float detailThreshold = 0.59;
const float detailTextureMult = 100.0;

varying vec3 v_color;
varying float distancef;
varying float morph;


// Functions
float calcFogLinear(float distanceToEye);
float calcFogExp(float distanceToEye);
vec3 getNormal(vec2 v);

vec3 getNormal(vec2 v) {
    /*
    the normal map is baked on blender (object space normal map), and it has different coord
    Blender: X to right, Y away from you, Z up.
    OpenGL: X' to right, Y' up, Z' towards you.
    */

    vec3 nworld = texture2D( u_BumpMap, v).xzy *2.0 - 1.0;
    nworld.y=-nworld.y;
    nworld = ( u_MVMatrix * vec4(nworld, 0.0)).xyz;
	return nworld;
}

float calcFogLinear(float distanceToEye)
{
    float fogEnd=zfar;
    float fogStart=zfar*fogScale;
	float f = (fogEnd - distanceToEye) / (fogEnd - fogStart);
	return clamp(f, 0.0, 1.0);
}

vec3 getWireColor()
{
   // int lod=int(lodlevel);
    //lod=mod(lod,7);

    float lod=mod(lodlevel,7.0);
    if(lod==0.0)      return mix(vec3(0.9,0.0,0.0),vec3(0.95,0.0,1.0),morph);
    else if(lod==1.0) return mix(vec3(0.95,0.0,1.0),vec3(0.0,0.0,1.0),morph);
    else if(lod==2.0) return mix(vec3(0.0,0.0,1.0),vec3(0.0,1.0,0.0),morph);
    else if(lod==3.0) return mix(vec3(0.0,1.0,0.0),vec3(0.6,0.0,1.0),morph);
    else if(lod==4.0) return mix(vec3(0.6,0.0,1.0),vec3(1.0,1.0,0.0),morph);
    else if(lod==5.0) return mix( vec3(1.0,1.0,0.0),vec3(1.0,0.2,0.1),morph);
    else if(lod==6.0) return mix( vec3(1.0,0.2,0.1),vec3(1.0,1.0,1.0),morph);
    else return vec3(1.0,0.3,0.0);
}

#ifdef GL_OES_standard_derivatives
float edgeFactor(){

    /*
     * Thanks to Florian Boesch for his excellent articles, especially this one about wireframe
     * rendering http://codeflow.org/entries/2012/aug/02/easy-wireframe-display-with-barycentric-coordinates
     */
    vec3 d = fwidth(barycentric);
    vec3 a3 = smoothstep(vec3(0.0), d*3.0, barycentric);
    return min(min(a3.x, a3.y), a3.z);
}
#endif

void main()
{
    float fogFactor = calcFogLinear(distancef);
    vec3 wirecolor;
    vec3 mixedColor;
    if(range.z==2.0 ||range.z==6.0)//wireframe, no solid
    {
    #ifdef GL_OES_standard_derivatives
        wirecolor=getWireColor();
        mixedColor= u_Fogcolor;
        wirecolor=mix(wirecolor, mixedColor, edgeFactor());
        gl_FragColor =  mix (vec4(u_Fogcolor,1.0),vec4(wirecolor,1.0), fogFactor); //<-
    #else
        if(any(lessThan(barycentric, vec3(linewidth))))
        {
            wirecolor=getWireColor();
            gl_FragColor =  mix (vec4(u_Fogcolor,1.0),vec4(wirecolor,1.0), fogFactor); //<-
        }
        else
        {
            gl_FragColor = vec4(u_Fogcolor,1.0);
        }
    #endif
    }
    else
    {
        vec3 n = getNormal(v_TexCoordinate);
        //Light

        //fragment base color
        vec4 colorMap;
        vec3 ambient;
        if(range.z==3.0 || range.z==1.0)
            colorMap = vec4(0.8,0.8,0.8,1.0); //no color texture
        else
            colorMap = texture2D(u_Texture, v_TexCoordinate);

        //Light
        vec3 l = normalize(u_LightPos - v_Position.xyz);

        //Diffuse term
        vec4 Idiff = colorMap  * max(dot(n,l),0.0);
        ambient = ambientLight * colorMap.rgb;
        vec4 baseColor = vec4( ambient + Idiff.rgb, 1.0);
//vec4 baseColor = colorMap;

        if((range.z==3.0 || range.z==7.0 )){ //wire + solid (texured or not)
           #ifdef GL_OES_standard_derivatives
               wirecolor=mix(getWireColor(), baseColor.rgb, edgeFactor());
               gl_FragColor =  mix (vec4(u_Fogcolor,1.0),vec4(wirecolor,1.0), fogFactor); //<-
           #else
               if(any(lessThan(barycentric, vec3(linewidth)))){
                  wirecolor=getWireColor();
                  gl_FragColor =  mix (vec4(u_Fogcolor,1.0),vec4(wirecolor,1.0), fogFactor);
               }
               else{
                  gl_FragColor =  mix (vec4(u_Fogcolor,1.0), baseColor, fogFactor);
               }
           #endif
        }
        else{ //solid, no wire
        gl_FragColor =  mix (vec4(u_Fogcolor,1.0), baseColor, fogFactor); //<-
        }
    }
}

