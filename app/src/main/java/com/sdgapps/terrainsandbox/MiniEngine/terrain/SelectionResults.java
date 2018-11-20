package com.sdgapps.terrainsandbox.MiniEngine.terrain;

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

    public void add(CDLODNode cdlodNode, int lod) {
        selectionList.add(cdlodNode);
        lowestLodReached = Math.min(lowestLodReached, lod);
    }

    public HashSet<SelectableNode> getSelectionList() {
        return selectionList;
    }

}
