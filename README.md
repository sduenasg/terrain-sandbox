# Terrain-sandbox
This is my Android Java/OpenGLES 3.0 implementation of the [CDLOD](https://github.com/fstrugar/CDLOD) (Continuous distance-dependent level of detail) terrain rendering technique by Filip Strugar. The Android NDK is also used to integrate an external library (Shader Optimizer).

# Planet Geometry
The planet is a sphere composed of 6 separate CDLOD meshes (quadtrees) forming the shape of a cube. Every terrain node is rendered using the same square grid mesh to save memory. The grid mesh is modified to fit each node's requirements in the vertex shader (positioning, heightmapping, spacing, morphing...), where the cube is finally  spherized via normalization of the vertex position on the cube (fast). Spherization techniques with better vertex distribution are available, but they are slower. 

In CDLOD, neighbor terrain nodes with different LOD levels dynamically morph their vertices to avoid showing cracks on the terrain. Some other popular techniques use skirts (geometry patches) to hide the cracks. The limitation in CDLOD is that between two neighboring terrain nodes the LOD difference must be 0 or one, since a node with LOD level n can only morph into a LOD n-1. This is all well explained in the original [paper](https://github.com/fstrugar/CDLOD).

![img](https://i.imgur.com/2QXzZA2.png)
![img](https://i.imgur.com/47dvMxv.png)
![img](https://i.imgur.com/3DyVid2.png)


