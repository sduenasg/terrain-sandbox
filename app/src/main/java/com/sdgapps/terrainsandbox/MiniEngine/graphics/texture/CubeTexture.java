package com.sdgapps.terrainsandbox.MiniEngine.graphics.texture;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import android.opengl.GLUtils;

import java.io.IOException;

public class CubeTexture extends Texture {

    private String[] files;

    CubeTexture(String[] _names, boolean mipmap, boolean alpha, boolean _interpolation, boolean _wrapMode) {
        this.files = _names;
        this.mipmapping = mipmap;
        this.alpha = alpha;
        this.interpolation = _interpolation;
        this.wrapMode = _wrapMode;
    }

    public int loadTextureInternal(AssetManager am)
    {
        this.glID = newTextureID();

        /*Disable android's drawable auto-scaling*/
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScaled = false;

        Bitmap temp=null;
        for(int i=0;i<6;i++) {
            try {
                temp = BitmapFactory.decodeStream(am.open(files[i]),null,opts);
            } catch (IOException e) {
                e.printStackTrace();
            }
            height = temp.getHeight();
            width = temp.getWidth();

            GLUtils.texImage2D(GLES30.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, temp, 0);
            temp.recycle();
        }
        setFiltering(GLES30.GL_TEXTURE_CUBE_MAP);
        setWrapMode(GLES30.GL_TEXTURE_CUBE_MAP);

        //autogen mipmaps if they were requested but not provided
        if(mipmapping)
            GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
        return glID;
    }

    //given a path to a folder, fetches all the paths of the mipmap images inside and puts them in a list
    //the naming of the mipmaps corresponds to the default one that the Mali Texture compression tool outputs
    //imagename_mip_X.pkm - where X is the mipmap level that corresponds to that image
    private String[] fetchPKMMipmapPaths(String texpath, AssetManager am)
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

    private int loadInternalETC2Mipmapping(AssetManager am)
    {
        String[] mips= fetchPKMMipmapPaths(files[0],am);
        ETC2Util.ETC2Texture etctex=null;
        try {
            etctex = ETC2Util.createTexture(am.open(mips[0]));
        } catch (IOException e) {
            e.printStackTrace();
        }

        height = etctex.getHeight();
        width =  etctex.getWidth();

        for(int i=0;i<6;i++) {
            if(i>0) {
                mips= fetchPKMMipmapPaths(files[i],am);
            }
            for(int j=0;j<mipmaplevels;j++)
            {
                if(i>0 || j>0) {
                    try {
                        etctex = ETC2Util.createTexture(am.open(mips[j]));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                GLES30.glCompressedTexImage2D(
                        GLES30.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i,j,
                        etctex.getCompressionFormat(),etctex.getWidth(),etctex.getHeight(),0,
                        etctex.getData().remaining(),etctex.getData());
            }
        }

        return glID;
    }

    private int loadInternalETC2NoMM(AssetManager am)
    {
        ETC2Util.ETC2Texture etctex=null;
        try {
            etctex = ETC2Util.createTexture(am.open(files[0]));
        } catch (IOException e) {
            e.printStackTrace();
        }


        height = etctex.getHeight();
        width =  etctex.getWidth();

        for(int i=0;i<6;i++) {
            if(i>0) {
                try {
                    etctex = ETC2Util.createTexture(am.open(files[i]));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            GLES30.glCompressedTexImage2D(
                    GLES30.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i,0,
                    etctex.getCompressionFormat(),etctex.getWidth(),etctex.getHeight(),0,
                    etctex.getData().remaining(),etctex.getData());
        }
        return glID;
    }

    private int loadTextureInternalETC2(AssetManager am)
    {
        this.glID = newTextureID();
        GLES30.glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP, glID);
        int result;
        if(mipmapping)
            result = loadInternalETC2Mipmapping(am);
        else
            result =  loadInternalETC2NoMM(am);
        setFiltering(GLES30.GL_TEXTURE_CUBE_MAP);
        setWrapMode(GLES30.GL_TEXTURE_CUBE_MAP);
        return result;
    }

    private void fetch(AssetManager am,String path)
    {
        if(!files[0].contains(".") || files[0].contains(".pkm")) {
            compressionType=compression_ETC2;
        }
        else
        {
            compressionType = compression_NONE;
        }
    }

    @Override
    public int loadTexture(AssetManager am) {

        fetch(am,files[0]);

        if(compressionType==compression_NONE)
            return loadTextureInternal(am);
        else
            return loadTextureInternalETC2(am);
    }
}
