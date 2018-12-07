#version 300 es
/*
this extension is now default in GLES3.0 and up.
The shader silently crashes during compilation on the following device if this is used on GLES3.0
(ASUS zenpad)
#ifdef GL_OES_standard_derivatives
#extension GL_OES_standard_derivatives : enable
#endif*/

#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif

const float shininess = 5.0;
const vec3 specularColor = vec3(0.5, 0.5, 0.5);
const float fogScale=0.6;
const float detailTextureMult = 100.0;
const vec3 rcolor=vec3(0.0,1.0,0.0);
const vec3 gcolor=vec3(0.27,0.21,0.13);//cliffs
const vec3 bcolor=vec3(0.0,0.0,1.0);
const vec3 acolor=vec3(1.0,1.0,1.0);
const float detailThreshold = 0.99;//distance [0,1] threshold at which detail will start showing

uniform sampler2D u_colorMap;  //color map
uniform sampler2D u_heightMap; //heightmap (for debugging)
uniform sampler2D u_splatMap;
uniform sampler2D u_splatSheet;
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
in vec4 vertColor;
in float incidenceAngle;
in vec3 v_normal;
flat in float v_lod;

out vec4 fragColor;
// Functions
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
    float lod=mod(v_lod,7.0);
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


float getSplatSheetColor(vec4 splatWeights)
{

    /*Array texture for the detail maps:
    Pros: Avoid a thousand problems related to atlassing, mipmapping and linear interpolation
    Cons: OpenGL ES 3.0 required
    */
    vec2 coords = fract(v_TexCoordinate * 1200.0);
    vec2 coords2 = fract(v_TexCoordinate * 600.0).yx; //rotated and scaled to minimize tiling by mixing


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
    vec3 mixedColor;
    vec3 colorMap;
       // colorMap = vec3(0.8,0.8,0.8); //no color texture

    colorMap = texture(u_colorMap, v_TexCoordinate).rgb;

    vec4 splatvalue=texture(u_splatMap,v_TexCoordinate);

    /*alpha comes in pre-multiplied from android SDK. //TODO investigate*/
    splatvalue.r/=splatvalue.a;
    splatvalue.g/=splatvalue.a;
    splatvalue.b/=splatvalue.a;

    //avoiding the if statement (if depthValue>detailthresh)
    float depthValue = depthPosition.z /depthPosition.w;
    float detailFactor= (depthValue-detailThreshold)/(0.999-detailThreshold);
    float splatcolor=getSplatSheetColor(splatvalue);

    splatcolor=mix(2.0*splatcolor,1.0,clamp(detailFactor,0.0,1.0));
    colorMap*=splatcolor;//apply the detail value

    /*//debug the splatmap with solid colors
   colorMap =  splatvalue.r * rcolor +
                splatvalue.g * gcolor +
                splatvalue.b * bcolor +
                splatvalue.a * acolor;*/

    vec3 n = v_normal;// getNormal(v_TexCoordinate);
    vec3 l = normalize(u_LightPos - v_Position.xyz);
    vec3 E = normalize(-v_Position.xyz);   // we are in Eye Coordinates, so EyePos is (0,0,0)
    //vec3 h = normalize(l+E); //half dir = lightdir + eyedir
    vec3 r = normalize(-reflect(l,n));
    const float shininess = 50.0;
    const vec3 specularColor=vec3(0.980, 0.922 , 0.608);

    float specularity=splatvalue.b;//water
    //Specular term
    vec3 Ispec = max(splatvalue.b,0.1)*specularColor* pow(max(dot(r,E),0.0) , shininess);

    float lightDot = dot(n,l);
    vec3 Idiff = colorMap * lightDot;
    vec3 diffspec = Idiff+Ispec;

    vec2 gradientLevel = vec2(incidenceAngle, 0);
    vec3 atmocol = vertColor.rgb * texture(u_atmoGradient, gradientLevel).rgb;

    float atmofactor=clamp(vertColor.a,0.0,1.0);
    atmocol=atmocol*atmofactor+diffspec*(1.0-atmofactor);//mix(atmocol,diffspec,0.9);

    vec3 baseColor = mix(atmocol,diffspec, atmofactor);

    if((mode==3.0 || mode==7.0 )){ //wireframe
           wirecolor=mix(getWireColor(), baseColor.rgb, edgeFactor());
           fragColor =  vec4(mix(u_Fogcolor,wirecolor, fogFactor),1.0); //<-
    }
    else{ // no wireframe
       fragColor =  vec4(mix (u_Fogcolor, baseColor, fogFactor),1.0); //<-
    }

}

