package com.sdgapps.terrainsandbox.MiniEngine.terrain;

public class SelectableNode {

    boolean[] selection = new boolean[5];

    /**
     * @param i [0,3] -> part of the parent node that covers children i's area. [4] -> Whole parent node
     */
    public void Select(int i) {
        if (i == 4) {
            selection[4] = true;
        } else if (i == 0)
            selection[0] = true;
        else if (i == 1)
            selection[1] = true;
        else if (i == 2)
            selection[2] = true;
        else if (i == 3)
            selection[3] = true;
    }

    public void ClearSelectionValues() {
        for (int i = 0; i < 5; i++) {
            selection[i] = false;
        }
    }
}
