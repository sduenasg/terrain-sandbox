#version 300 es
in vec4 a_Position;
in vec4 a_Normal;
in vec2 a_TexCoordinate;

uniform mat4  u_Modelmatrix;
uniform mat4  u_MVPMatrix;
uniform vec3  camPos;
uniform vec3  lightPos;
uniform vec3  u_atmosphere_color;

out float incidenceAngle;
out vec4 col;

float _TransitionWidth = 0.1;
float _FresnelExponent = 0.1;
#define PI 3.14159265;

void main()
{
    vec4 worldpos=u_Modelmatrix*a_Position;
    vec3 viewDirection = normalize(camPos - worldpos.xyz);
    vec3 lightDirection = normalize(lightPos-worldpos.xyz);

    // assuming the object is a sphere, the angles between normals and light determines the positions on the sphere
    incidenceAngle = acos(dot(lightDirection, a_Normal.xyz)) / PI;
    // shade atmosphere according to this ramp function from 0 to 180 degrees
    float shadeFactor = 0.1 * (1.0 - incidenceAngle) + 0.9 * (1.0 - (clamp(incidenceAngle, 0.5, 0.5 + _TransitionWidth) - 0.5) / _TransitionWidth);
    // the viewer should be able to see further distance through atmosphere towards edges of the sphere
    float angleToViewer = sin(acos(dot(a_Normal.xyz, viewDirection)));

    // this ramp function lights up edges, especially the very edges of the sphere contour
    float perspectiveFactor = 0.3 + 0.2 * pow(angleToViewer, _FresnelExponent) + 0.5 * pow(angleToViewer, _FresnelExponent * 20.0);

    col = vec4(u_atmosphere_color * perspectiveFactor * shadeFactor,(1.0-angleToViewer)*10.0);
    gl_Position = u_MVPMatrix * a_Position;
}

