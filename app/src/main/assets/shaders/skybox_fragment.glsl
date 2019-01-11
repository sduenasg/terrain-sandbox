#version 300 es
precision mediump float;

uniform samplerCube skyboxTex;
smooth in vec3 eyeDirection;
out vec4 fragColor;

void main()
{
   fragColor = texture(skyboxTex,eyeDirection);
}