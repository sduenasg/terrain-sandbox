package com.sdgapps.terrainsandbox.MiniEngine;

interface RendererInterface {

    void setAspectRatio(float r);
    void invalidateGLData();
    void update();
    void draw();
}
