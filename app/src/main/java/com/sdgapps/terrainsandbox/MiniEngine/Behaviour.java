package com.sdgapps.terrainsandbox.MiniEngine;

import com.sdgapps.terrainsandbox.MiniEngine.graphics.Transform;

public abstract class Behaviour {

    public boolean renderer=false;
    public GameObject gameObject;
    public Transform transform;
    public void update(){};
    public void onStart(){};
    public void onEnable(){};
    public void onDisable(){};
    public void onAddedToEntity(){};
}
