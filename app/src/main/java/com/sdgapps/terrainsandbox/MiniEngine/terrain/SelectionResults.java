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


    public void renderSelection(GridMesh gridMesh, GLSLProgram targetShader, float[] rangeDistances, float[] morphconsts)
    {
        for (SelectableNode snode : getSelectionList()) {
            ((CDLODNode) snode).renderSelectedParts(gridMesh, targetShader,rangeDistances,morphconsts);
        }
        fullnodes=0;
        partialNodes=0;
    }

    static int drawnNodes=0;
}
