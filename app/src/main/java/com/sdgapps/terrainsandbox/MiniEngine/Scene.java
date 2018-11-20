package com.sdgapps.terrainsandbox.MiniEngine;

import java.util.ArrayList;

public class Scene {
    ArrayList<GameObject> entities = new ArrayList<>();

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
    }
}
