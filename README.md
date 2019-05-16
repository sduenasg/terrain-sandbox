# Terrain-sandbox
This is my Android Java/OpenGLES 3.0 implementation of the [CDLOD](https://github.com/fstrugar/CDLOD) (Continuous distance-dependent level of detail) terrain rendering technique by Filip Strugar. The Android NDK is also used to integrate the [Shader Optimizer](https://github.com/aras-p/glsl-optimizer) C++ library.

# Planet Geometry
The planet is a sphere composed of 6 separate CDLOD meshes (quadtrees) forming the shape of a cube. Every terrain node is rendered using the same square grid mesh to save memory. The grid mesh is modified to fit each node's requirements in the vertex shader (positioning, heightmapping, spacing, morphing...), where the cube is finally spherized via normalization of the vertex position on the cube (fast). Spherization techniques with better vertex distribution are available, but they are reportedly slower.

In CDLOD, neighbor terrain nodes with different LOD levels dynamically morph their vertices to avoid showing cracks on the terrain. Some other popular terrain rendering techniques use skirts (geometry patches) to hide the cracks. The limitation in CDLOD is that between two neighboring terrain nodes the LOD difference must be 0 or one, since a node with LOD level n can only morph into a LOD n-1. This is all well explained in the original [paper](https://github.com/fstrugar/CDLOD).

# Screenshots
![Screenshot](https://i.imgur.com/2QXzZA2.png)
![Screenshot](https://i.imgur.com/47dvMxv.png)
![Screenshot](https://i.imgur.com/3DyVid2.png)
![Screenshot](https://user-images.githubusercontent.com/651022/57857850-7c664400-77df-11e9-91d9-d5ca80396498.png)
![Screenshot](https://user-images.githubusercontent.com/651022/57857849-7c664400-77df-11e9-9f68-c56ede2f4ea2.png)
