package com.sdgapps.terrainsandbox.MVP;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.AppCompatSeekBar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Toast;

import com.sdgapps.terrainsandbox.GLSurfaceRenderer;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.MiniMath;
import com.sdgapps.terrainsandbox.R;
import com.sdgapps.terrainsandbox.databinding.ActivityMainBinding;
import java.util.concurrent.FutureTask;


/**
 * View implementation (MVP pattern)
 */
public class MainViewMvpImpl implements MainViewMvp,
        NavigationView.OnNavigationItemSelectedListener,
        DrawerLayout.DrawerListener {

    private final ActivityMainBinding mainBinding;
    private View mRootView;
    private MainViewMvpListener mListener;

    private float lastDrawerPos = 0;
    private boolean glViewInitialized = false;
    private GLSurfaceRenderer renderer;

    private boolean initialized = false;
    private float rightSideXDown, rightSideYDown, leftSideXDown, leftSideYDown;

    private int leftSidePtrId = -1;
    private int rightSidePtrId = -1;

    boolean manageTouch(View view, MotionEvent motionEvent) {
        if (mListener != null) {
            float newX, newY;
            int ptrId = motionEvent.getPointerId(0);
            int ptrIndex;
            int width = view.getMeasuredWidth();
            int height = view.getMeasuredHeight();

            switch (motionEvent.getActionMasked() & MotionEvent.ACTION_MASK) {
                case (MotionEvent.ACTION_UP):
                    if (leftSidePtrId == ptrId) {
                        leftSidePtrId = -1;
                        mListener.onVerticalTranslation(0);
                        mListener.onHorizontalTranslation(0);
                        mListener.onWalkForward(0);
                        leftSideXDown = 0;
                        leftSideYDown = 0;
                    } else if (rightSidePtrId == ptrId) {
                        rightSidePtrId = -1;
                        mListener.onCameraLookDirectionChanged(0, 0, 0);
                        rightSideXDown = 0;
                        rightSideYDown = 0;
                    } else
                        return false;
                    return true;
                case (MotionEvent.ACTION_POINTER_UP):
                    ptrIndex = motionEvent.getActionIndex();
                    ptrId = motionEvent.getPointerId(ptrIndex);
                    if (leftSidePtrId == ptrId) {
                        leftSidePtrId = -1;
                        //cancel strafing when user lets go
                        mListener.onVerticalTranslation(0);
                        mListener.onHorizontalTranslation(0);
                        mListener.onWalkForward(0);
                        leftSideXDown = 0;
                        leftSideYDown = 0;
                    } else if (rightSidePtrId == ptrId) {
                        rightSidePtrId = -1;
                        mListener.onCameraLookDirectionChanged(0, 0, 0);
                        rightSideXDown = 0;
                        rightSideYDown = 0;
                    } else
                        return false;
                    return true;
                case (MotionEvent.ACTION_DOWN):
                    newX = motionEvent.getX() / (float) width;
                    newY = motionEvent.getY() / (float) height;

                    if (rightSidePtrId == -1 && newX >= .5f) {
                        rightSidePtrId = ptrId;
                        rightSideXDown = newX;
                        rightSideYDown = newY;
                    } else if (leftSidePtrId == -1 && newX < .5f) {
                        leftSidePtrId = ptrId;
                        leftSideXDown = newX;
                        leftSideYDown = newY;
                    } else
                        return false;
                    return true;
                case (MotionEvent.ACTION_POINTER_DOWN):
                    ptrIndex = motionEvent.getActionIndex();
                    ptrId = motionEvent.getPointerId(ptrIndex);
                    newX = motionEvent.getX(ptrIndex) / (float) width;
                    newY = motionEvent.getY(ptrIndex) / (float) height;

                    if (rightSidePtrId == -1 && newX >= .5f) {
                        rightSidePtrId = ptrId;
                        rightSideXDown = newX;
                        rightSideYDown = newY;
                    } else if (leftSidePtrId == -1 && newX < .5f) {
                        leftSidePtrId = ptrId;
                        leftSideXDown = newX;
                        leftSideYDown = newY;
                    } else
                        return false;
                    return true;
                case (MotionEvent.ACTION_MOVE):
                    ptrIndex = motionEvent.findPointerIndex(leftSidePtrId);
                    if (ptrIndex != -1) {
                        newX = motionEvent.getX(ptrIndex) / (float) width;
                        newY = motionEvent.getY(ptrIndex) / (float) height;
                        float deltaX = deadZone(MiniMath.clampf(3 * (leftSideXDown - newX), -1, 1), .05f);
                        float deltaY = deadZone(MiniMath.clampf(3 * (leftSideYDown - newY), -1, 1), .02f);
                        mListener.onWalkForward(deltaY);
                        mListener.onHorizontalTranslation(deltaX);
                    }
                    ptrIndex = motionEvent.findPointerIndex(rightSidePtrId);
                    if (ptrIndex != -1) {
                        newX = motionEvent.getX(ptrIndex) / (float) width;
                        newY = motionEvent.getY(ptrIndex) / (float) height;
                        float deltaX = deadZone(MiniMath.clampf(3 * (rightSideXDown - newX), -1, 1), .05f);
                        float deltaY = deadZone(MiniMath.clampf(3 * (rightSideYDown - newY), -1, 1), .02f);
                        mListener.onCameraLookDirectionChanged(0, -deltaY * 10f, -deltaX * 10f);
                    }
                    return ptrIndex != -1;
            }
        }
        return false;
    }

    private float deadZone(float value, float dz) {
        if (Math.abs(value) > dz)
            return value - Math.signum(value) * dz;
        else
            return 0;
    }

    public void stop() {
        initialized = false;
        unregisterViewListeners();

    }


    public void start(GLSurfaceRenderer _renderer) {
        if (!glViewInitialized) {
            mainBinding.mainContent.glsurface.setEGLContextClientVersion(2);
            mainBinding.mainContent.glsurface.setEGLConfigChooser(8, 8, 8, 8, 24, 0);
            mainBinding.mainContent.glsurface.setRenderer(_renderer);
            glViewInitialized = true;
        }
        initialized = true;
    }

    public void configureUIListeners() {

        if (initialized) {
            setSwitchListeners();
            mainBinding.mainContent.menubutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!mainBinding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        mainBinding.drawerLayout.openDrawer(GravityCompat.START);
                        mainBinding.mainContent.menubutton.setVisibility(View.INVISIBLE);
                    }
                }
            });

            //little gamepad analog stick listener for test purposes
            mainBinding.drawerLayout.setOnGenericMotionListener(new View.OnGenericMotionListener() {
                private final float dz = .1f;

                @Override
                public boolean onGenericMotion(View view, MotionEvent motionEvent) {
                    float x = motionEvent.getAxisValue(MotionEvent.AXIS_X);
                    float y = motionEvent.getAxisValue(MotionEvent.AXIS_Y);
                    float z = motionEvent.getAxisValue(MotionEvent.AXIS_Z);
                    float rz = motionEvent.getAxisValue(MotionEvent.AXIS_RZ);

                    mListener.onWalkForward(deadZone(-y));
                    mListener.onHorizontalTranslation(deadZone(-x));
                    mListener.onCameraLookDirectionChanged(0, deadZone(rz) * 10f, deadZone(z) * 8f);

                    return true;
                }

                private float deadZone(float value) {
                    if (Math.abs(value) > dz)
                        return value - Math.signum(value) * dz;
                    else
                        return 0;
                }
            });

            //this one listens for gamepad digital button presses (d-pad, ABXY...)
           /* mainBinding.drawerLayout.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int i, KeyEvent keyEvent) {
                    return true;
                }
            });*/

            mainBinding.drawerLayout.setScrimColor(Color.TRANSPARENT); //disable darkening of the screen when the mainBinding.drawerLayout is open
            mainBinding.navView.setNavigationItemSelectedListener(this);
            mainBinding.drawerLayout.addDrawerListener(this);
            setupSeekbarListeners();
        }
    }

    private void unregisterViewListeners() {
        mainBinding.drawerLayout.removeDrawerListener(this);
        mainBinding.navView.setNavigationItemSelectedListener(null);
        mainBinding.mainContent.menubutton.setOnClickListener(null);

        mainBinding.drawerLayout.setOnKeyListener(null);
        mainBinding.drawerLayout.setOnGenericMotionListener(null);

        //seek bars
        MenuItem seekbarItem = mainBinding.navView.getMenu().findItem(R.id.rangeSeekbar);
        View actionView = MenuItemCompat.getActionView(seekbarItem);

        AppCompatSeekBar seek = actionView.findViewById(R.id.seeker);
        seek.setOnSeekBarChangeListener(null);

        seekbarItem = mainBinding.navView.getMenu().findItem(R.id.lightAzimuth);
        actionView = MenuItemCompat.getActionView(seekbarItem);
        seek = actionView.findViewById(R.id.seeker);

        seek.setOnSeekBarChangeListener(null);

        seekbarItem = mainBinding.navView.getMenu().findItem(R.id.lightElevation);
        actionView = MenuItemCompat.getActionView(seekbarItem);
        seek = actionView.findViewById(R.id.seeker);

        seek.setOnSeekBarChangeListener(null);

        //switches
        MenuItem wireframeSwitchItem = mainBinding.navView.getMenu().findItem(R.id.wireframecheck);
        CompoundButton wireframeSwitchView = (CompoundButton) MenuItemCompat.getActionView(wireframeSwitchItem);
        wireframeSwitchView.setOnCheckedChangeListener(null);

        MenuItem debugSwitchItem = mainBinding.navView.getMenu().findItem(R.id.debug);
        CompoundButton debugSwitchView = (CompoundButton) MenuItemCompat.getActionView(debugSwitchItem);
        debugSwitchView.setOnCheckedChangeListener(null);

        MenuItem textureSwitchItem = mainBinding.navView.getMenu().findItem(R.id.texture);
        CompoundButton textureSwitchView = (CompoundButton) MenuItemCompat.getActionView(textureSwitchItem);
        textureSwitchView.setOnCheckedChangeListener(null);

        MenuItem orbitSwitchItem = mainBinding.navView.getMenu().findItem(R.id.orbit);
        CompoundButton orbitSwitchView = (CompoundButton) MenuItemCompat.getActionView(orbitSwitchItem);
        orbitSwitchView.setOnCheckedChangeListener(null);

        MenuItem shadowmapSwitchItem = mainBinding.navView.getMenu().findItem(R.id.shadowmap);
        CompoundButton shadowmapSwitchView = (CompoundButton) MenuItemCompat.getActionView(shadowmapSwitchItem);
        shadowmapSwitchView.setOnCheckedChangeListener(null);
    }

    public MainViewMvpImpl(LayoutInflater inflater, ViewGroup container, GLSurfaceRenderer renderer) {
        mainBinding = ActivityMainBinding.inflate(inflater);
        mRootView = mainBinding.getRoot();
        this.start(renderer);
    }

    private void setupSeekbarListeners() {

        MenuItem seekbarItem = mainBinding.navView.getMenu().findItem(R.id.rangeSeekbar);
        View actionView = MenuItemCompat.getActionView(seekbarItem);

        AppCompatSeekBar seek = actionView.findViewById(R.id.seeker);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, final int i, boolean b) {
                mListener.onRangeDistanceChanged(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        seekbarItem = mainBinding.navView.getMenu().findItem(R.id.lightAzimuth);
        actionView = MenuItemCompat.getActionView(seekbarItem);
        seek = actionView.findViewById(R.id.seeker);

        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, final int i, boolean b) {
                Runnable task = new Runnable() {
                    @Override
                    public void run() {

                        mListener.onSunAzimuthChanged(i);
                    }
                };
                mainBinding.mainContent.glsurface.queueEvent(task);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        seekbarItem = mainBinding.navView.getMenu().findItem(R.id.lightElevation);
        actionView = MenuItemCompat.getActionView(seekbarItem);
        seek = actionView.findViewById(R.id.seeker);

        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, final int i, boolean b) {
                Runnable task = new Runnable() {
                    @Override
                    public void run() {
                        mListener.onSunElevationChanged(i);
                    }
                };
                mainBinding.mainContent.glsurface.queueEvent(task);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }


    @Override
    public void setListener(MainViewMvpListener listener) {
        mListener = listener;
    }

    @Override
    public void unregisterListener() {
        mListener = null;
    }

    @Override
    public void updateFps(int fps) {
        if (initialized) {
            mainBinding.mainContent.infoLayout.fpsText.setText(String.valueOf(fps));
        }
    }

    @Override
    public void updateDrawCalls(int drawcalls) {
        if (initialized) {
            mainBinding.mainContent.infoLayout.drawcallsText.setText(String.valueOf(drawcalls));
        }
    }

    @Override
    public void hideLoadingProgress() {

    }

    @Override
    public void setupRangeSeekbar(int rangeSeekbarMax) {
        MenuItem seekbarItem = mainBinding.navView.getMenu().findItem(R.id.rangeSeekbar);
        View actionView = MenuItemCompat.getActionView(seekbarItem);

        AppCompatSeekBar seek = actionView.findViewById(R.id.seeker);
        seek.setMax(rangeSeekbarMax);

        seek.setProgress(0);
    }

    @Override
    public void showViewInstructions(Context c) {
        Toast.makeText(c, "Use the left half of screen to move. The right half of the screen to rotate", Toast.LENGTH_LONG).show();
    }

    @Override
    public View getRootView() {
        return mRootView;
    }

    @Override
    public Bundle getViewState() {
        return null;
    }

    private void setSwitchListeners() {
        MenuItem wireframeSwitchItem = mainBinding.navView.getMenu().findItem(R.id.wireframecheck);
        CompoundButton wireframeSwitchView = (CompoundButton) MenuItemCompat.getActionView(wireframeSwitchItem);
        wireframeSwitchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mListener.onWireframeModeClicked(isChecked);
            }
        });


        MenuItem debugSwitchItem = mainBinding.navView.getMenu().findItem(R.id.debug);
        CompoundButton debugSwitchView = (CompoundButton) MenuItemCompat.getActionView(debugSwitchItem);
        debugSwitchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mListener.onDebugAABBModeClicked(isChecked);
            }
        });

        MenuItem textureSwitchItem = mainBinding.navView.getMenu().findItem(R.id.texture);
        CompoundButton textureSwitchView = (CompoundButton) MenuItemCompat.getActionView(textureSwitchItem);
        textureSwitchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mListener.onTextureModeClicked(isChecked);
            }
        });

        MenuItem orbitSwitchItem = mainBinding.navView.getMenu().findItem(R.id.orbit);
        CompoundButton orbitSwitchView = (CompoundButton) MenuItemCompat.getActionView(orbitSwitchItem);
        orbitSwitchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mListener.onLightAutorotateClicked(isChecked);
            }
        });

        MenuItem shadowmapSwitchItem = mainBinding.navView.getMenu().findItem(R.id.shadowmap);
        CompoundButton shadowmapSwitchView = (CompoundButton) MenuItemCompat.getActionView(shadowmapSwitchItem);
        shadowmapSwitchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                Runnable task = new Runnable() {
                    @Override
                    public void run() {
                        mListener.onShadowmapModeClicked(isChecked);
                    }
                };

                mainBinding.mainContent.glsurface.queueEvent(task);
            }
        });
    }

    public void dismissProgressIndicator() {
        if (mainBinding.mainContent.loadLayout.loadprogress != null)
            mainBinding.mainContent.loadLayout.loadprogress.setVisibility(View.INVISIBLE);
    }

    public void showProgressIndicator() {
        if (mainBinding.mainContent.loadLayout.loadprogress != null)
            mainBinding.mainContent.loadLayout.loadprogress.setVisibility(View.VISIBLE);
    }

    public void disableShadowMapSwitch() {
        MenuItem shadowmapSwitchItem = mainBinding.navView.getMenu().findItem(R.id.shadowmap);
        CompoundButton shadowmapSwitchView = (CompoundButton) MenuItemCompat.getActionView(shadowmapSwitchItem);
        shadowmapSwitchView.setEnabled(false);
        shadowmapSwitchView.setChecked(false);
    }

    public void pause() {
        mainBinding.mainContent.glsurface.onPause();
    }

    public void resume() {
        mainBinding.mainContent.glsurface.onResume();
    }

    public void closeDrawer() {
        mainBinding.drawerLayout.closeDrawer(GravityCompat.START);
    }

    public boolean isDrawerOpen() {
        return mainBinding.drawerLayout.isDrawerOpen(GravityCompat.START);
    }

    public void queueTaskForGLView(FutureTask preLoadSceneTask) {
        mainBinding.mainContent.glsurface.queueEvent(preLoadSceneTask);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        if (mListener != null) {
            int id = item.getItemId();
            switch (id) {
                case R.id.resetCamera:
                    mListener.onResetCameraClicked();
                    break;
                default:
                    break;
            }
            return true;
        }
        return false;
    }

    @Override
    public void onDrawerSlide(View view, float v) {
        boolean opening = v > lastDrawerPos;
        if (opening) {
            mainBinding.mainContent.menubutton.setVisibility(View.INVISIBLE);
        } else {
            mainBinding.mainContent.menubutton.setVisibility(View.VISIBLE);
        }
        lastDrawerPos = v;
    }

    @Override
    public void onDrawerOpened(View view) {
        mainBinding.mainContent.menubutton.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDrawerClosed(View view) {
        mainBinding.mainContent.menubutton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDrawerStateChanged(int i) {
    }
}
