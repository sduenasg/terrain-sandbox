package com.sdgapps.terrainsandbox.MiniEngine;

import com.sdgapps.terrainsandbox.EngineManagers;

import java.util.ArrayList;

public class Scene {
    ArrayList<GameObject> entities = new ArrayList<>();
    public EngineManagers engineManagers;

    void setEngineManagers(EngineManagers engineManagers) {
        this.engineManagers = engineManagers;
    }

    public void update() {
        for (int i = 0; i < entities.size(); i++) {
            entities.get(i).update();
        }
    }

    public void draw() {
        for (int i = 0; i < entities.size(); i++) {
            entities.get(i).draw();
        }
    }

    public void add(GameObject e) {
        entities.add(e);
        e.engineManagers=engineManagers;
    }

    void invalidateGLData() {

        for(GameObject o:entities)
        {
            o.invalidateGLData();
        }
    }

    public void setAspectRatio(float r) {

    }
}
