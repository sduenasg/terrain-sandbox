#version 300 es

#ifdef GL_OES_standard_derivatives
#extension GL_OES_standard_derivatives : enable
#endif

#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif

uniform sampler2D u_colorMap;  //color map
uniform sampler2D u_heightMap; //heightmap (for debugging)
uniform sampler2D u_normalMap;
uniform sampler2D u_splatMap;
uniform sampler2D u_splatSheet;
uniform sampler2DArray u_splatArray;
uniform sampler2D u_atmoGradient;
uniform float zfar;
uniform float lodlevel;
uniform vec3 ambientLight;
uniform vec3 u_Fogcolor;
uniform vec3 u_LightPos; //in eye space
uniform mat4 u_MVMatrix;
uniform vec3 range;

in vec2 v_TexCoordinate;
in vec4 v_Position;
in vec4 depthPosition;
in vec3 barycentric;
in float distancef;
in float morph;
in vec3 vertColor;
in float incidenceAngle;

out vec4 fragColor;

const float shininess = 5.0;
const vec3 specularColor = vec3(0.5, 0.5, 0.5);
const float fogScale=0.6;
const float detailThreshold = 0.59;
const float detailTextureMult = 100.0;

//usethe splat map with these flat colors instead of textures for testing
const vec3 rcolor=vec3(0.0,1.0,0.0);
const vec3 gcolor=vec3(0.27,0.21,0.13);//cliffs
const vec3 bcolor=vec3(0.0,0.0,1.0);
const vec3 acolor=vec3(1.0,1.0,1.0);

// Functions
float calcFogLinear(float distanceToEye);
float calcFogExp(float distanceToEye);
vec3 getNormal(vec2 v);

vec3 getNormal(vec2 v) {
    /*
    The normal map is baked on Blender (object space normal map). Coordinates there are
    different than here (.xzy, y=-yb)
    */
    vec3 nobj = texture( u_normalMap, v).xzy *2.0 - 1.0;
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

vec3 getSplatSheetColor(vec4 splatWeights)
{
    /*Array texture for the detail maps:
    Pros: Avoid a thousand problems related to atlassing, mipmapping and linear interpolation
    Cons: OpenGL ES 3.0 required
    */
    vec2 coords = fract(v_TexCoordinate * 800.0);
    vec2 coords2 = fract(v_TexCoordinate * 400.0).yx; //rotated and scaled to minimize tiling by mixing

    vec3 grassvalue=texture( u_splatArray  , vec3(coords,0.0)).rgb;
    vec3 snowvalue=texture(  u_splatArray  , vec3(coords,2.0)).rgb;
    vec3 watervalue=texture( u_splatArray  , vec3(coords,3.0)).rgb;
    vec3 cliffsvalue=texture(u_splatArray  , vec3(coords,1.0)).rgb;

    vec3 grassvalue2=texture( u_splatArray  , vec3(coords2,0.0)).rgb;
    vec3 snowvalue2=texture(  u_splatArray  , vec3(coords2,2.0)).rgb;
    vec3 watervalue2=texture( u_splatArray  , vec3(coords2,3.0)).rgb;
    vec3 cliffsvalue2=texture(u_splatArray  , vec3(coords2,1.0)).rgb;

    vec3 outcolor = splatWeights.r * grassvalue +
                    splatWeights.g * cliffsvalue+
                    splatWeights.b * watervalue +
                    splatWeights.a * snowvalue;
    vec3 outcolor2=splatWeights.r * grassvalue2 +
                   splatWeights.g * cliffsvalue2+
                   splatWeights.b * watervalue2 +
                   splatWeights.a * snowvalue2;
    //return cliffsvalue;
    return mix(outcolor,outcolor2,0.5);
}

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
        fragColor =  vec4(mix(u_Fogcolor,wirecolor, fogFactor).xyz,1.0); //<-
    #else
        if(any(lessThan(barycentric, vec3(linewidth))))
        {
            wirecolor=getWireColor();
            fragColor =  vec4(mix(u_Fogcolor,wirecolor, fogFactor).xyz,1.0); //<-
        }
        else
        {
            fragColor = vec4(u_Fogcolor,1.0);
        }
    #endif
    }
    else
    {


        vec3 colorMap;
        if(range.z==3.0 || range.z==1.0)
            colorMap = vec3(0.8,0.8,0.8); //no color texture
        else
            colorMap = texture(u_colorMap, v_TexCoordinate).rgb;


        vec4 splatvalue=texture(u_splatMap,v_TexCoordinate);

        //alpha comes in pre-multiplied from android.
        splatvalue.r/=splatvalue.a;
        splatvalue.g/=splatvalue.a;
        splatvalue.b/=splatvalue.a;

        vec3 splatcolor=getSplatSheetColor(splatvalue);
        colorMap*=3.0*splatcolor;
        //n=normalize(n+splatcolor);
        /*//debug the splatmap with solid colors
       colorMap =  splatvalue.r * rcolor +
                    splatvalue.g * gcolor +
                    splatvalue.b * bcolor +
                    splatvalue.a * acolor;*/

        vec3 n = getNormal(v_TexCoordinate);
        vec3 l = normalize(u_LightPos - v_Position.xyz);
        vec3 E = normalize(-v_Position.xyz);   // we are in Eye Coordinates, so EyePos is (0,0,0)
        //vec3 h = normalize(l+E); //half dir = lightdir + eyedir
        vec3 r = normalize(-reflect(l,n));
        const float shininess = 50.0;
        const vec3 specularColor=vec3(1.0,1.0,1.0);
        //Specular term
        vec3 Ispec = max(splatvalue.b,0.1)*ambientLight* pow(max(dot(r,E),0.0) , shininess);

        float lightDot = dot(n,l);
        vec3 Idiff = colorMap * lightDot;
        vec3 diffspec = Idiff+Ispec;

        vec2 gradientLevel = vec2(incidenceAngle, 0);
        vec3 atmocol = vertColor.rgb* texture(u_atmoGradient, gradientLevel).rgb* 1.4;
        atmocol=mix(atmocol,diffspec,0.8);
        vec3 baseColor = mix(atmocol,diffspec, clamp(lightDot,0.0,1.0));
        //vec3 baseColor = mix(atmocol,diffspec, vertColor.a);
        if((range.z==3.0 || range.z==7.0 )){ //wire + solid (texured or not)
           #ifdef GL_OES_standard_derivatives
               wirecolor=mix(getWireColor(), baseColor.rgb, edgeFactor());
               fragColor =  vec4(mix(u_Fogcolor,wirecolor, fogFactor),1.0); //<-
           #else
               if(any(lessThan(barycentric, vec3(linewidth)))){
                  wirecolor=getWireColor();
                  fragColor =  vec4(mix(u_Fogcolor,wirecolor,fogFactor),1.0);
               }
               else{
                  fragColor =  vec4(mix(u_Fogcolor, baseColor, fogFactor),1.0);
               }
           #endif
        }
        else{ //solid, no wire
           fragColor =  vec4(mix (u_Fogcolor, baseColor, fogFactor),1.0); //<-
        }
    }
}

