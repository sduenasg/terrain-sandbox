uniform mat4 u_MVMatrix;
uniform mat4 u_Projectionmatrix;
attribute vec4 a_Position;

void main()
{
    mat4 billboardMV=u_MVMatrix;
    billboardMV[0][0] = 1.0;
    billboardMV[0][1] = 0.0;
    billboardMV[0][2] = 0.0;
    billboardMV[1][0] = 0.0;
    billboardMV[1][1] = 1.0;
    billboardMV[1][2] = 0.0;
    billboardMV[2][0] = 0.0;
    billboardMV[2][1] = 0.0;
    billboardMV[2][2] = 1.0;
    vec4 P = u_Projectionmatrix * billboardMV * a_Position;
    gl_Position = vec4(P.x, P.y, P.w, P.w);//max_depth to avoid culling
}