#include <jni.h>
#include <stdlib.h>

#include "glsl_optimizer.h"

/**jni interface to the glsl-optimizer c++ library*/

extern "C"
JNIEXPORT jstring JNICALL Java_com_sdgapps_terrainsandbox_MiniEngine_graphics_glsl_GLSLShader_jnioptimize( JNIEnv* env, jobject thiz, jstring jstr,jboolean isFragment )
{
	jboolean iscopy;
	const char* shaderSource = (env)->GetStringUTFChars(jstr, &iscopy);
	unsigned options = options;

	glslopt_ctx* ctx = glslopt_initialize(kGlslTargetOpenGLES30);
    glslopt_shader* shader;

	if(isFragment)
	    shader = glslopt_optimize (ctx, kGlslOptShaderFragment, shaderSource, 0);
    else
        shader = glslopt_optimize (ctx, kGlslOptShaderVertex, shaderSource, 0);

	const char* optSource = glslopt_get_output(shader);
	jstring out = env->NewStringUTF(optSource);

	glslopt_shader_delete (shader);
	glslopt_cleanup (ctx);

	return out;
}