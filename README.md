# Planet Geometry
The planet is a sphere composed of 6 CDLOD meshes in the shape of a cube. Every terrain node is rendered using the same square grid mesh to save memory.
The grid mesh is modified to fit each node's requirements in the vertex shader (positioning, heightmapping, spacing, morphing...), where spherization is also done.

Each face of the cube has it's own set of textures and runs CDLOD independently of the others.

![img](https://i.imgur.com/2QXzZA2.png)
![img](https://i.imgur.com/47dvMxv.png)
![img](https://i.imgur.com/3DyVid2.png)


