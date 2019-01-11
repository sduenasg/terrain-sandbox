package com.sdgapps.terrainsandbox.MiniEngine.graphics.texture;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import android.opengl.GLUtils;

import com.sdgapps.terrainsandbox.MiniEngine.graphics.MiniMath;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**Warning: only supported from OpenGL ES 3.0 and up*/
public class ArrayTexture extends Texture {

    private int layerCount;

    public String path;
    private String[] pathList;

    /**
     * @param path assetmanager path to the folder that contains the textures for the array
     *
     *  Inside this folder, there needs to be either:
     *             A - Non ETC2 texture files (e.g png files)
     *             B - Folders. Each folder containing a set of ETC2 (pkg) textures (all the mipmap levels of the texture)
     */
    ArrayTexture(String path, boolean mipmap, boolean alpha, boolean _interpolation, boolean _wrapMode) {
        this.path=path;
        this.mipmapping = mipmap;
        this.alpha = alpha;
        this.interpolation = _interpolation;
        this.wrapMode = _wrapMode;
        if(!mipmapping)
            mipmaplevels=1;
    }


    private void fetchTexturePaths(AssetManager am)
    {
        String[] files=null;
        try {
            files=am.list(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(files[0].contains(".")) {
            compressionType = compression_NONE;
        }
        else {

            String[] subfiles=null;
            try {
                subfiles=am.list(path+files[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mipmaplevels=subfiles.length;
            compressionType = compression_ETC2;
        }

        layerCount=files.length;
        pathList=new String[layerCount];


        String base;
        for(int i=0;i<files.length;i++)
        {
            base=files[i].trim();
            pathList[i]=path+"/"+base;
        }
    }


    @Override
    public int loadTexture(Resources res, AssetManager am) {
        fetchTexturePaths(am);
        if(compressionType==compression_NONE)
            return loadInternal(res,am);
        else
            return loadInternalETC2(res,am);
    }

    //given a path to a folder, fetches all the paths of the mipmap images inside and puts them in a list
    //the naming of the mipmaps corresponds to the default one that the Mali Texture compression tool outputs
    //imagename_mip_X.pkm - where X is the mipmap level that corresponds to that image
    String[] fetchPKMMipmapPaths(String texpath, AssetManager am)
    {
        String[] files=null;
        try {
            files=am.list(texpath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mipmaplevels=files.length;
        String[] mipPaths=new String[mipmaplevels];
        String[] tokens=files[0].split("_mip");
        String base=tokens[0].trim();

        String[] dotokens=files[0].split("[.]+");
        String extension="."+dotokens[dotokens.length-1].trim();

        for(int i=0;i<mipmaplevels;i++)
        {
            mipPaths[i]=texpath+"/"+base+"_mip_"+i+extension;
        }
        return mipPaths;
    }

    private int loadInternalETC2(Resources res, AssetManager am)
    {
        this.glID = newTextureID();

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D_ARRAY, glID);
        String[] mips= fetchPKMMipmapPaths(pathList[0],am);

        ETC2Util.ETC2Texture etctex=null;
        try {
            etctex = ETC2Util.createTexture(am.open(mips[0]));
        } catch (IOException e) {
            e.printStackTrace();
        }

        height = etctex.getHeight();
        width =  etctex.getWidth();

        GLES30.glTexStorage3D(GLES30.GL_TEXTURE_2D_ARRAY, mipmaplevels, etctex.getCompressionFormat(), width, height, layerCount);

        for(int i=0;i<pathList.length;i++) {
            if(i>0) {
                mips= fetchPKMMipmapPaths(pathList[i],am);
            }
            for (int j=0;j<mipmaplevels;j++) {
                if(i>0 || j>0) {
                    try {
                        etctex = ETC2Util.createTexture(am.open(mips[j]));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                height = etctex.getHeight();
                width =  etctex.getWidth();

                ByteBuffer pixelbuf = etctex.getData();

                GLES30.glCompressedTexSubImage3D(
                        GLES30.GL_TEXTURE_2D_ARRAY, j,
                        0, 0, i,
                        width, height, 1,
                        etctex.getCompressionFormat(), etctex.getData().remaining(),
                        pixelbuf);
            }
        }

        setFiltering(GLES30.GL_TEXTURE_2D_ARRAY);
        setWrapMode(GLES30.GL_TEXTURE_2D_ARRAY);
        return glID;
    }

    private int loadInternal(Resources res, AssetManager am)
    {
        this.glID = newTextureID();

        /*Disable android's drawable auto-scaling*/
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScaled = false;
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D_ARRAY, glID);

        Bitmap temp=null;
        try {
            temp = BitmapFactory.decodeStream(am.open(pathList[0]),null,opts);
        } catch (IOException e) {
            e.printStackTrace();
        }

        height = temp.getHeight();
        width = temp.getWidth();

        if(mipmapping)
        {
            int t=Math.max(height,width);
            mipmaplevels= (int)(1 + Math.floor(MiniMath.binlog2(t)));
        }

        //Allocate storage space for the textures
        GLES30.glTexStorage3D(GLES30.GL_TEXTURE_2D_ARRAY, mipmaplevels, GLES30.GL_RGBA8, width, height, layerCount);

        for(int i=0;i<pathList.length;i++) {
            if(i>0) {
                try {
                    temp = BitmapFactory.decodeStream(am.open(pathList[i]), null, opts);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            height = temp.getHeight();
            width = temp.getWidth();

            //get the pixel buffer
            ByteBuffer pixelbuf= ByteBuffer.allocateDirect(width * height *IntBytes);
            pixelbuf.order(ByteOrder.nativeOrder());
            temp.copyPixelsToBuffer(pixelbuf);
            pixelbuf.position(0);

            int internalFormat=GLUtils.getInternalFormat(temp);
            int type=GLUtils.getType(temp);

            // Upload pixel data.
            GLES30.glTexSubImage3D(GLES30.GL_TEXTURE_2D_ARRAY, 0, 0, 0, i, width, height, 1, internalFormat, type, pixelbuf);
//https://www.khronos.org/opengl/wiki/Array_Texture
//https://www.khronos.org/registry/OpenGL-Refpages/es3.0/
            temp.recycle();
        }

        setFiltering(GLES30.GL_TEXTURE_2D_ARRAY);
        setWrapMode(GLES30.GL_TEXTURE_2D_ARRAY);

        return glID;
    }

}
