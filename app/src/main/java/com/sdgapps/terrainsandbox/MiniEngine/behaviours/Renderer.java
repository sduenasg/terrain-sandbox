package com.sdgapps.terrainsandbox.MiniEngine.behaviours;

import com.sdgapps.terrainsandbox.MiniEngine.Behaviour;
import com.sdgapps.terrainsandbox.MiniEngine.RenderPackage;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.Material;

import java.util.ArrayList;

public class Renderer extends Behaviour {

    public ArrayList<RenderPackage> renderPackages = new ArrayList<>();
    public Material material;

    public Renderer() {
        renderer = true;
    }

    public void draw() {

    }

    @Override
    public void update() {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

}
