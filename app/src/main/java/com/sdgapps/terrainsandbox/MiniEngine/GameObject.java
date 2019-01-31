package com.sdgapps.terrainsandbox.MiniEngine;

import com.sdgapps.terrainsandbox.MiniEngine.behaviours.Renderer;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.Transform;

import java.util.ArrayList;

public class GameObject {
    ArrayList<Behaviour> behaviours = new ArrayList<>();
    public Transform transform = new Transform();

    public EngineManagers engineManagers;

    public void update() {
        for (int i = 0; i < behaviours.size(); i++) {
            behaviours.get(i).update();
        }

    }

    public void draw() {
        transform.updateModelMatrix();
        for (int i = 0; i < behaviours.size(); i++) {
            Behaviour b = behaviours.get(i);
            if (b.renderer)
                ((Renderer) b).draw();
        }
    }

    public void add(Behaviour b) {
        behaviours.add(b);
        b.gameObject = this;
        b.transform = this.transform;
        b.onAddedToEntity();
    }

    public void invalidateGLData() {

        for (int i = 0; i < behaviours.size(); i++) {
            Behaviour b = behaviours.get(i);
            if (b.renderer)
                ((Renderer) b).invalidateGLData();
        }
    }
}
