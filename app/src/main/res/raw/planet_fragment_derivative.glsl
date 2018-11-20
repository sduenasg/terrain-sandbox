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
uniform sampler2D u_heightmap; //heightmap (for debugging)
uniform sampler2D u_BumpMap;

uniform float zfar;
uniform float lodlevel;
uniform vec3 ambientLight;
uniform vec3 u_Fogcolor;
uniform vec3 u_LightPos; //in eye space
uniform mat4 u_MVMatrix;
uniform vec3 range;

varying vec2 v_TexCoordinate;
varying vec4 v_Position;
varying vec4 depthPosition;
varying vec3 barycentric;
varying float distancef;
varying float morph;
varying vec3 vertColor;

const float shininess = 5.0;
const vec3 specularColor = vec3(0.5, 0.5, 0.5);
const float fogScale=0.6;
const float detailThreshold = 0.59;
const float detailTextureMult = 100.0;

// Functions
float calcFogLinear(float distanceToEye);
float calcFogExp(float distanceToEye);
vec3 getNormal(vec2 v);

vec3 getNormal(vec2 v) {
    /*
    The normal map is baked on Blender (object space normal map). Coordinates there are
    different than here (.xzy, y=-yb)
    */
    vec3 nobj = texture2D( u_BumpMap, v).xzy *2.0 - 1.0;
    nobj.y = -nobj.y;
    nobj = ( u_MVMatrix * vec4(nobj, 0.0)).xyz;
	return normalize(nobj);
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
    float lod=mod(lodlevel,7.0);
    if(lod==0.0)      return mix(vec3(0.9,0.0,0.0),vec3(0.95,0.0,1.0),morph);
    else if(lod==1.0) return mix(vec3(0.95,0.0,1.0),vec3(0.0,0.0,1.0),morph);
    else if(lod==2.0) return mix(vec3(0.0,0.0,1.0),vec3(0.0,1.0,0.0),morph);
    else if(lod==3.0) return mix(vec3(0.0,1.0,0.0),vec3(0.6,0.0,1.0),morph);
    else if(lod==4.0) return mix(vec3(0.6,0.0,1.0),vec3(1.0,1.0,0.0),morph);
    else if(lod==5.0) return mix( vec3(1.0,1.0,0.0),vec3(1.0,0.2,0.1),morph);
    else if(lod==6.0) return mix( vec3(1.0,0.2,0.1),vec3(0.9,0.0,0.0),morph);
    else return vec3(1.0,0.3,0.0);
}

#ifdef GL_OES_standard_derivatives
float edgeFactor(){

    /*
     * http://codeflow.org/entries/2012/aug/02/easy-wireframe-display-with-barycentric-coordinates
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
        gl_FragColor =  vec4(mix(u_Fogcolor,wirecolor, fogFactor).xyz,1.0); //<-
    #else
        if(any(lessThan(barycentric, vec3(linewidth))))
        {
            wirecolor=getWireColor();
            gl_FragColor =  vec4(mix(u_Fogcolor,wirecolor, fogFactor).xyz,1.0); //<-
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

        vec3 colorMap;
        vec3 ambient;
        if(range.z==3.0 || range.z==1.0)
            colorMap = vec3(0.8,0.8,0.8); //no color texture
        else
            colorMap = texture2D(u_Texture, v_TexCoordinate).rgb;


        vec3 l = normalize(u_LightPos - v_Position.xyz);
        float lightDot = dot(n,l);
        vec3 Idiff = colorMap * lightDot;
        vec3 baseColor = mix(Idiff*vertColor,Idiff,clamp(lightDot,0.0,1.0));

       // baseColor=vertColor;
        if((range.z==3.0 || range.z==7.0 )){ //wire + solid (texured or not)
           #ifdef GL_OES_standard_derivatives
               wirecolor=mix(getWireColor(), baseColor.rgb, edgeFactor());
               gl_FragColor =  vec4(mix(u_Fogcolor,wirecolor, fogFactor),1.0); //<-
           #else
               if(any(lessThan(barycentric, vec3(linewidth)))){
                  wirecolor=getWireColor();
                  gl_FragColor =  vec4(mix(u_Fogcolor,wirecolor,fogFactor),1.0);
               }
               else{
                  gl_FragColor =  vec4(mix(u_Fogcolor, baseColor, fogFactor),1.0);
               }
           #endif
        }
        else{ //solid, no wire
            gl_FragColor =  vec4(mix (u_Fogcolor, baseColor, fogFactor),1.0); //<-
        }
    }
}

