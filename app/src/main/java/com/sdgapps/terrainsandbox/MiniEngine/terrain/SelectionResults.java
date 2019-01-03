package com.sdgapps.terrainsandbox.MiniEngine.terrain;

import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.GLSLProgram;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderUniform1fv;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderUniform2fv;

import java.util.HashSet;

public class SelectionResults {


    private HashSet<SelectableNode> selectionList = new HashSet<>();

    public int getLowestLodReached() {
        return lowestLodReached;
    }

    private int lowestLodReached = Integer.MAX_VALUE;

    public void clear() {
        lowestLodReached = Integer.MAX_VALUE;
        selectionList.clear();
    }

    int fullnodes=0;
    int partialNodes=0;
    public void add(CDLODNode cdlodNode, int lod) {
        selectionList.add(cdlodNode);
        lowestLodReached = Math.min(lowestLodReached, lod);
        if(cdlodNode.selection[4])
            fullnodes++;
        else
        {
            if(cdlodNode.selection[0])
                partialNodes++;
            if(cdlodNode.selection[1])
                partialNodes++;
            if(cdlodNode.selection[2])
                partialNodes++;
            if(cdlodNode.selection[3])
                partialNodes++;
        }
    }

    public HashSet<SelectableNode> getSelectionList() {
        return selectionList;
    }


    public void renderSelection(GridMesh gridMesh, GLSLProgram targetShader)
    {
        for (SelectableNode snode : getSelectionList()) {
            ((CDLODNode) snode).renderSelectedParts(gridMesh, targetShader);
        }
        fullnodes=0;
        partialNodes=0;
    }

    static int drawnNodes=0;
    public void renderSelectionInstanced(GridMesh gridMesh, GLSLProgram targetShader)
    {
        //TODO investigate a more efficient way to gather the data
        int nFullNodes=fullnodes;
        int nSubquadlNodes=partialNodes;
        //Logger.log("TEST "+fullnodes + " " + nSubquadlNodes);
        float[] fullquadQuadScales=  new float[nFullNodes];
        float[] fullquadRanges=      new float[nFullNodes*3];
        float[] fullquadNodeOffsets= new float[nFullNodes*2];
        float[] fullquadLodLevel=    new float[nFullNodes];

        float[] subquadQuadScales=   new float[nSubquadlNodes];
        float[] subquadRanges=       new float[nSubquadlNodes*3];
        float[] subquadNodeOffsets=  new float[nSubquadlNodes*2];
        float[] subquadLodLevel=     new float[nSubquadlNodes];

        int i=0;
        int sqi=0;
        CDLODNode node;
        ShaderUniform1fv CDLODLodlevel = (ShaderUniform1fv)targetShader.getUniform("lodlevel");
        ShaderUniform2fv CDLODNodeOffset=(ShaderUniform2fv)targetShader.getUniform("nodeoffset");
        ShaderUniform2fv CDLODrange = ( ShaderUniform2fv)targetShader.getUniform  ("range");
        ShaderUniform1fv CDLODQuadScale =(ShaderUniform1fv)targetShader.getUniform("quad_scale");

        for (SelectableNode snode : selectionList) {
            node = (CDLODNode) snode;
            if (snode.selection[4]) {
                fullquadQuadScales[i] = node.quadScale;
                fullquadRanges[2 * i] = node.getMorphContz();
                fullquadRanges[2 * i + 1] = node.getRangeDistance();
                fullquadNodeOffsets[2 * i] = node.xoffset;
                fullquadNodeOffsets[2 * i + 1] = node.zoffset;
                fullquadLodLevel[i] = node.lod;
                i++;
                drawnNodes++;
            }
            else
            {
                drawnNodes++;
                float halfd=node.quadScale*(float)gridMesh.gridDim/2f;
                if(snode.selection[0]) {
                    subquadQuadScales[sqi] = node.quadScale;
                    subquadRanges[2 * sqi] = node.getMorphContz();
                    subquadRanges[2 * sqi + 1] = node.getRangeDistance();
                    subquadLodLevel[sqi] = node.lod;
                    subquadNodeOffsets[2 * sqi] = node.xoffset;
                    subquadNodeOffsets[2 * sqi + 1] = node.zoffset;
                    sqi++;

                }
                if(snode.selection[1])
                {
                    subquadQuadScales[sqi] = node.quadScale;
                    subquadRanges[2 * sqi] = node.getMorphContz();
                    subquadRanges[2 * sqi + 1] = node.getRangeDistance();
                    subquadLodLevel[sqi] = node.lod;
                    subquadNodeOffsets[2 * sqi] = node.xoffset + halfd;
                    subquadNodeOffsets[2 * sqi + 1] = node.zoffset;
                    sqi++;

                }
                if(snode.selection[2])
                {
                    subquadQuadScales[sqi] = node.quadScale;
                    subquadRanges[2 * sqi] = node.getMorphContz();
                    subquadRanges[2 * sqi + 1] = node.getRangeDistance();
                    subquadLodLevel[sqi] = node.lod;
                    subquadNodeOffsets[2 * sqi] = node.xoffset;
                    subquadNodeOffsets[2 * sqi + 1] = node.zoffset + halfd;
                    sqi++;

                }
                if(snode.selection[3])
                {
                    subquadQuadScales[sqi] = node.quadScale;
                    subquadRanges[2 * sqi] = node.getMorphContz();
                    subquadRanges[2 * sqi + 1] = node.getRangeDistance();
                    subquadLodLevel[sqi] = node.lod;
                    subquadNodeOffsets[2 * sqi] = node.xoffset + halfd;
                    subquadNodeOffsets[2 * sqi + 1] = node.zoffset + halfd;
                    sqi++;
                }
            }


        }

        CDLODLodlevel.array=fullquadLodLevel;
        CDLODNodeOffset.array=fullquadNodeOffsets;
        CDLODrange.array=fullquadRanges;
        CDLODQuadScale.array=fullquadQuadScales;

        CDLODLodlevel.bind();
        CDLODNodeOffset.bind();
        CDLODrange.bind();
        CDLODQuadScale.bind();

        gridMesh.instancedFullmeshDraw(nFullNodes);

        CDLODLodlevel.array   =subquadLodLevel;
        CDLODNodeOffset.array =subquadNodeOffsets;
        CDLODrange.array      =subquadRanges;
        CDLODQuadScale.array  =subquadQuadScales;

        CDLODLodlevel.bind();
        CDLODNodeOffset.bind();
        CDLODrange.bind();
        CDLODQuadScale.bind();

        gridMesh.instancedQuarterMeshDraw(nSubquadlNodes);

        fullnodes=0;
        partialNodes=0;
    }
}
