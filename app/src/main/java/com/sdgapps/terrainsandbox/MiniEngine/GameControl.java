package com.sdgapps.terrainsandbox.MiniEngine;
import com.sdgapps.terrainsandbox.MVP.SceneInterface;

import java.util.ArrayList;

/*
 * Root class of the engine, holds the scenes and the systems
 */
public class GameControl implements RendererInterface {

    private Scene currentScene;
    private ArrayList<Scene> scenes;

    public GameControl()
    {
        scenes = new ArrayList<>();
    }

    @Override
    public void draw()
    {
        if(currentScene!=null)
            currentScene.draw();
    }

    @Override
    public void setAspectRatio(float r) {
        if(currentScene!=null)
            currentScene.setAspectRatio(r);
    }

    @Override
    public void invalidateGLData() {
        if(currentScene!=null)
            currentScene.invalidateGLData();
    }

    @Override
    public void update()
    {
        if(currentScene!=null)
            currentScene.update();
    }

    public void changeScene(int i)
    {
        currentScene = scenes.get(i);
    }

    public void addScene(Scene s)
    {
        if(!scenes.contains(s))
            scenes.add(s);
    }

    public void addSceneAndSetCurrent(Scene s)
    {
        if(!scenes.contains(s))
            scenes.add(s);
        currentScene=s;
    }

    public Scene getCurrentScene() {
        return currentScene;
    }
}
