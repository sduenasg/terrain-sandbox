#version 300 es

uniform mat4 u_MVPMatrix;
in vec4 a_Position;

void main()
{
    gl_Position = u_MVPMatrix * a_Position;
}

