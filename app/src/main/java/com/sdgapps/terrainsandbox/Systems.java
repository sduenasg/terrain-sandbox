package com.sdgapps.terrainsandbox;

import com.sdgapps.terrainsandbox.MiniEngine.TimeSystem;
import com.sdgapps.terrainsandbox.MiniEngine.behaviours.Camera;
import com.sdgapps.terrainsandbox.MiniEngine.behaviours.Light;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderSystem;

public class Systems {

    public ShaderSystem sShaderSystem = new ShaderSystem();
    public Camera mainCamera;
    public TimeSystem sTime = new TimeSystem();
    public Light mainLight;
}
