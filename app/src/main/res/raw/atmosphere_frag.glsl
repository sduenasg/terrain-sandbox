#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif
uniform vec3 u_atmosphere_color;
uniform sampler2D u_Texture;  //gradient map
varying float incidenceAngle;
varying vec4 col;

void main() {

    // tint with gradient texture ramp of 70% brightness value and multiply by 1.4 to re-adjust brightness level
    vec2 gradientLevel = vec2(incidenceAngle, 0);
    vec4 fcol = col* texture2D(u_Texture, gradientLevel)* 1.4;
    gl_FragColor=vec4(fcol.xyz,col.a);
    //gl_FragColor = vec4(u_atmosphere_color, 0.5);
}
