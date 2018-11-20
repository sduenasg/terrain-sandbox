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
uniform sampler2D u_Texture3; //detail normal map
uniform sampler2D u_heightmap; //viewportHeight map (for debugging)
uniform sampler2D u_Shadowmaptex; //shadow map
uniform sampler2D u_BumpMap;
uniform float zfar;
uniform float lodlevel;
uniform vec3 ambientLight;
uniform vec3 u_Fogcolor;
uniform vec3 u_LightPos; //Eye space light position
uniform mat4 u_MVMatrix;
uniform vec3 range;
uniform mat4 u_shadowmapMVP;//MVP from the light source camera

varying vec2 v_TexCoordinate;
varying vec4 v_Position;
varying vec4 depthPosition;
varying vec3 barycentric;
varying vec3 v_color;
varying float distancef;
varying vec4 v_ShadowCoord;
varying float morph;

const float shininess = 5.0;
const vec3 specularColor = vec3(0.5, 0.5, 0.5);
const float fogScale=0.6;
const float detailThreshold = 0.59;
const float detailTextureMult = 100.0;

vec3 getNormal(vec2 v) {
   vec3 nworld = texture2D( u_BumpMap, v).xzy *2.0 - 1.0;
       nworld.y=-nworld.y;
       //nworld.z=-nworld.z;
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
    int lod=int(lodlevel);

    if(lod==0)      return mix(vec3(0.9,0.0,0.0),vec3(0.95,0.0,1.0),morph);
    else if(lod==1) return mix(vec3(0.95,0.0,1.0),vec3(0.0,0.0,1.0),morph);
    else if(lod==2) return mix(vec3(0.0,0.0,1.0),vec3(0.0,1.0,0.0),morph);
    else if(lod==3) return mix(vec3(0.0,1.0,0.0),vec3(0.6,0.0,1.0),morph);
    else if(lod==4) return mix(vec3(0.6,0.0,1.0),vec3(1.0,1.0,0.0),morph);
    else if(lod==5) return mix( vec3(1.0,1.0,0.0),vec3(1.0,0.2,0.1),morph);
    else if(lod==6) return mix( vec3(1.0,0.2,0.1),vec3(1.0,1.0,1.0),morph);
    else return vec3(1.0,0.3,0.0);
}

// 1/(size of the heightmap)
float uxPixelOffset=1.0/8192.0;
// This defines the value to move one pixel up or down
float uyPixelOffset=1.0/8192.0;

float lookup( vec2 offSet,in vec4 ShadowCoord)
{
	float distanceFromLight = texture2D(u_Shadowmaptex, (ShadowCoord.xy +
	                               vec2(offSet.x * uxPixelOffset, offSet.y * uyPixelOffset)).st ).z;
	return float(distanceFromLight > ShadowCoord.z);
}

float shadowPCF(in vec4 ShadowCoord)
{
	float shadow = 0.0;

    float i=0.0;
	for (float y = -1.0; y <= 1.0; y = y + 1.0) {
		for (float x = -1.0; x <= 1.0; x = x + 1.0) {
			shadow += lookup(vec2(x,y), ShadowCoord);
			i++;
		}
	}

	shadow /= i;
	//shadow += 1.2;

	return shadow;
}

float calcshadowfactor()
{

 //shadowmap
 //nota: camara ortogonal -> zbuffer lineal. No afecta al resultado de la comparacion del shadow mapping
    vec4 shadowMapPosition = v_ShadowCoord / v_ShadowCoord.w;//not needed on ortho camera
    float shadow=shadowPCF(shadowMapPosition);

    //float shadow=lookup(vec2(0,0), shadowMapPosition);

    return shadow;
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
        vec3 l = normalize(u_LightPos - v_Position.xyz);

        //Shadowmap
        float visibility=calcshadowfactor();

        //color base del fragmento
        vec4 colorMap;
        vec3 ambient;
        if(range.z==3.0 || range.z==1.0)
            colorMap = vec4(0.8,0.8,0.8,1.0); //no color texture

        else
            colorMap = texture2D(u_Texture, v_TexCoordinate);


        //Diffuse term
        vec4 Idiff = colorMap  * max(dot(n,l),0.0);
        //Idiff = clamp(Idiff, 0.0, 1.0);

        ambient =  0.5 * ambientLight * colorMap.rgb;
        vec4 baseColor = vec4(ambient + visibility * Idiff.rgb, 1.0);
        float linewidth=0.05;

        if((range.z==3.0 || range.z==7.0 )){ //wire + solid (texured or not)
            //wireframe

            #ifdef GL_OES_standard_derivatives
                wirecolor=mix(getWireColor(), baseColor.rgb, edgeFactor());
                gl_FragColor =  mix (vec4(u_Fogcolor,1.0),vec4(wirecolor,1.0), fogFactor); //<-
            #else
                if(any(lessThan(barycentric, vec3(linewidth))))
                {
                   wirecolor=getWireColor();
                   gl_FragColor =  mix (vec4(u_Fogcolor,1.0),vec4(wirecolor,1.0), fogFactor);
                }
                else{
                   gl_FragColor =  mix (vec4(u_Fogcolor,1.0), baseColor, fogFactor);
                }
            #endif
        }
        else{ //solid, no wire
            gl_FragColor =  mix (vec4(u_Fogcolor,1.0), vec4(baseColor), fogFactor); //<-
        }
    }
}

