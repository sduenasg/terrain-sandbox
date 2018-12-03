#version 300 es
uniform mat4 u_Projectionmatrix;
uniform mat4 u_Viewatrix;
in vec4 a_Position;

smooth out vec3 eyeDirection;

void main()
{
    mat4 inverseProjection = inverse(u_Projectionmatrix);
    //camera doesn't have any scaling or shearing, so the inversion can be simplified to transposition (faster)
    mat3 inverseview = transpose(mat3(u_Viewatrix));
    vec3 unprojected = (inverseProjection * a_Position).xyz;
    eyeDirection = inverseview * unprojected;
    gl_Position = a_Position;
}