#version 300 es

#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif

uniform vec3 u_atmosphere_color;
uniform sampler2D u_Texture;  //gradient map

in float incidenceFactor;
in vec2 uv;

out vec4 fragColor;

void main() {

    fragColor=incidenceFactor*texture(u_Texture,uv);//vec4(fcol.xyz,col.a);
    //fragColor=texture(u_Texture,uv);//vec4(fcol.xyz,col.a);
}
