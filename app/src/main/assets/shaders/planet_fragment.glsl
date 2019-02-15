#version 300 es
/*
this extension is now default in GLES3.0 and up.
The shader compilation silently crashes on the following device if this is used on GLES3.0
(ASUS zenpad)
#ifdef GL_OES_standard_derivatives
#extension GL_OES_standard_derivatives : enable
#endif*/

#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif

const float fogScale=0.6;
const float detailTextureMult = 100.0;
const vec3 rcolor=vec3(0.0,1.0,0.0);
const vec3 gcolor=vec3(0.27,0.21,0.13);//cliffs
const vec3 bcolor=vec3(0.0,0.0,1.0);
const vec3 acolor=vec3(1.0,1.0,1.0);
const float detailThreshold = 0.99;//distance [0,1] threshold at which detail will start showing
const float shininess = 50.0;
const vec3 specularColor=vec3(0.980, 0.922 , 0.608);

uniform sampler2D u_colorMap;  //color map
uniform sampler2D u_heightMap; //heightmap (for debugging)
uniform sampler2D u_splatMap;
uniform sampler2D u_splatSheet;
uniform float lodlevel;
/*
*asus tablet requires a separate precision qualifier for the sampler2DArray type
*renderer: PowerVR Rogue GX6250
*/
uniform mediump sampler2DArray u_splatArray;
uniform sampler2D u_atmoGradient;
uniform float zfar;

uniform vec3 ambientLight;
uniform vec3 u_Fogcolor;
uniform vec3 u_LightPos; //in eye space
uniform mat4 u_MVMatrix;
uniform float mode;

in vec2 v_TexCoordinate;
in vec4 v_Position;
in vec4 depthPosition;
in vec3 barycentric;
in float distancef;
in float morph;
in vec3 v_normal;

out vec4 fragColor;

float calcFogLinear(float distanceToEye);
float calcFogExp(float distanceToEye);

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
    else if(lod==5.0) return mix(vec3(1.0,1.0,0.0),vec3(1.0,0.2,0.1),morph);
    else if(lod==6.0) return mix(vec3(1.0,0.2,0.1),vec3(0.9,0.0,0.0),morph);
    else return vec3(1.0,0.3,0.0);
}

float edgeFactor(){
     // http://codeflow.org/entries/2012/aug/02/easy-wireframe-display-with-barycentric-coordinates

    vec3 d = fwidth(barycentric);
    vec3 a3 = smoothstep(vec3(0.0), d*3.0, barycentric);
    return min(min(a3.x, a3.y), a3.z);
}

float getSplatSheetColorSimple(vec4 splatWeights)
{
    vec2 coords = fract(v_TexCoordinate * 1200.0);
    float grassvalue =  texture( u_splatArray  , vec3(coords,0.0)).r;
    float snowvalue =   texture(  u_splatArray  , vec3(coords,2.0)).r;
    float watervalue =  texture( u_splatArray  , vec3(coords,3.0)).r;
    float cliffsvalue = texture(u_splatArray  , vec3(coords,0.0)).r;

    float outcolor = splatWeights.r * grassvalue +
                    splatWeights.g * cliffsvalue+
                    splatWeights.b * watervalue +
                    splatWeights.a * snowvalue;

    return outcolor;
}


 /* samples 2 sets of coordinates to minimize the tiling effect. Looks
       better but it is pretty slow on weaker devices*/
float getSplatSheetColor(vec4 splatWeights)
{
    vec2 coords = fract(v_TexCoordinate * 1200.0);
    vec2 coords2 = fract(v_TexCoordinate * 600.0).yx;

    float grassvalue=  texture( u_splatArray  , vec3(coords,0.0)).r;
    float snowvalue=   texture(  u_splatArray  , vec3(coords,2.0)).r;
    float watervalue=  texture( u_splatArray  , vec3(coords,3.0)).r;
    float cliffsvalue = texture(u_splatArray  , vec3(coords,0.0)).r;

    float grassvalue2= texture( u_splatArray  , vec3(coords2,0.0)).r;
    float snowvalue2=  texture(  u_splatArray  , vec3(coords2,2.0)).r;
    float watervalue2= texture( u_splatArray  , vec3(coords2,3.0)).r;
    float cliffsvalue2=texture(u_splatArray  , vec3(coords2,1.0)).r;

   float outcolor = splatWeights.r * grassvalue +
                    splatWeights.g * cliffsvalue+
                    splatWeights.b * watervalue +
                    splatWeights.a * snowvalue;
    float outcolor2=splatWeights.r * grassvalue2 +
                   splatWeights.g * cliffsvalue2+
                   splatWeights.b * watervalue2 +
                   splatWeights.a * snowvalue2;
    return mix(outcolor,outcolor2,0.5);
}

void main()
{
    float fogFactor = calcFogLinear(distancef);
    vec3 wirecolor;
    vec3 colorMap = texture(u_colorMap, v_TexCoordinate).rgb;

    // avoiding the if statement (if depthValue>detailthresh)
    float depthValue = depthPosition.z /depthPosition.w;
    float detailFactor= (depthValue-detailThreshold)/(0.999-detailThreshold);

    // splat maps must have a non-premultiplied alpha channel
    vec4 splatvalue=texture(u_splatMap,v_TexCoordinate);

    float splatcolor=mix(3.0*getSplatSheetColorSimple(splatvalue),1.0,clamp(detailFactor,0.0,1.0));

    colorMap*=splatcolor;// apply the detail value

    vec3 n = v_normal;
    vec3 l = normalize(u_LightPos - v_Position.xyz);
    vec3 E = normalize(-v_Position.xyz);   // v_position is in eye space (eye position is (0,0,0))
    vec3 r = -reflect(l,n);

    // specular term
    vec3 Ispec = max(splatvalue.b,0.1) * specularColor * pow(max(dot(r,E),0.0) , shininess);

    float lightDot = dot(n,l);
    vec3 Idiff = colorMap * lightDot;
    vec3 diffspec = Idiff+Ispec;

    vec4 outcolor;
    //outcolor =  vec4(diffspec,1.0);

    if((mode==7.0)){ //wireframe
           wirecolor=mix(getWireColor(), diffspec.rgb, edgeFactor());
           outcolor =  vec4(wirecolor,1.0);
    }
    else{ // no wireframe
           outcolor =  vec4(diffspec,1.0);
    }

    fragColor =  outcolor;
}

