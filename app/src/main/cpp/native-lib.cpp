#include <jni.h>
#include <memory.h>
#include <string>
#include <unistd.h>
#include <android/native_window.h> // requires ndk r5 or newer
#include <android/native_window_jni.h> // requires ndk r5 or newer

#include <EGL/egl.h> // requires ndk r5 or newer
#include <GLES/gl.h>

#include "svrApi.h"

#include "logger.h"

extern "C"
JNIEXPORT jstring

#define LOG_TAG "Qualcomm6DoF"


JNICALL
Java_com_realmax_quacomm6doftest_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject thiz,
        jobject surface) {

    LOG_INFO("stringFromJNI");

    if(surface == NULL){
        LOG_INFO("surface is null");
        //ignore init step and try to get 6-dof data directly
    }else{
        LOG_INFO("surface is not null");
        //try to initialize the EGL environment

        ANativeWindow * nativeWindow = ANativeWindow_fromSurface(env, surface);
        LOG_INFO("Got window %p", nativeWindow);

        if(nativeWindow == NULL){
            //if nativeWindow is NULL, then can not call beginVR successfully
            LOG_ERROR("native windows is null");
        }
        else{
            LOG_INFO("nativeWindow is not null, try to init ELG&VRSDK");
            //egl init
            const EGLint attribs[] = {
                    EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
                    EGL_BLUE_SIZE, 8,
                    EGL_GREEN_SIZE, 8,
                    EGL_RED_SIZE, 8,
                    EGL_NONE
            };
            EGLDisplay display;
            EGLConfig config;
            EGLint numConfigs;
            EGLint format;
            EGLSurface eglsurface;
            EGLContext context;
            EGLint width;
            EGLint height;
            GLfloat ratio;

            if ((display = eglGetDisplay(EGL_DEFAULT_DISPLAY)) == EGL_NO_DISPLAY) {
                LOG_ERROR("eglGetDisplay() returned error %d", eglGetError());
                return env->NewStringUTF("eglGetDisplay() returned error");
            }

            if (!eglInitialize(display, 0, 0)) {
                LOG_ERROR("eglInitialize() returned error %d", eglGetError());
                return env->NewStringUTF("eglGetDisplay() returned error");
            }

            if (!eglChooseConfig(display, attribs, &config, 1, &numConfigs)) {
                LOG_ERROR("eglChooseConfig() returned error %d", eglGetError());
                return env->NewStringUTF("eglGetDisplay() returned error");
            }

            if (!eglGetConfigAttrib(display, config, EGL_NATIVE_VISUAL_ID, &format)) {
                LOG_ERROR("eglGetConfigAttrib() returned error %d", eglGetError());
                return env->NewStringUTF("eglGetConfigAttrib() returned error");
            }

            ANativeWindow_setBuffersGeometry(nativeWindow, 0, 0, format);

            if (!(eglsurface = eglCreateWindowSurface(display, config, nativeWindow, 0))) {
                LOG_ERROR("eglCreateWindowSurface() returned error %d", eglGetError());
                return env->NewStringUTF("eglCreateWindowSurface() returned error");
            }

            if (!(context = eglCreateContext(display, config, 0, 0))) {
                LOG_ERROR("eglCreateContext() returned error %d", eglGetError());
                return env->NewStringUTF("eglCreateContext() returned error");
            }

            if (!eglMakeCurrent(display, eglsurface, eglsurface, context)) {
                LOG_ERROR("eglMakeCurrent() returned error %d", eglGetError());
                return env->NewStringUTF("eglMakeCurrent() returned error");
            }

            if (!eglQuerySurface(display, eglsurface, EGL_WIDTH, &width) ||
                !eglQuerySurface(display, eglsurface, EGL_HEIGHT, &height)) {
                LOG_ERROR("eglQuerySurface() returned error %d", eglGetError());
                return env->NewStringUTF("eglQuerySurface() returned error");
            }
            //EGL init finished

            //if not EGL init step, some errors will be shown
            //E/libEGL: eglQueryContext:832 error 3006 (EGL_BAD_CONTEXT)
            //E/libEGL: eglQuerySurface:640 error 300d (EGL_BAD_SURFACE)

            //E/svr: svrUpdateEyeContextSurface: Failed to create eye context pbuffer surface

            //E/svr: Calling QVRServiceClient_Create()...
            //E/svr: QVRServiceClient_Create failed.
            //
            //E/svr: Error getting pose from predictive sensor!


            //init svr service
            svrInitParams params;
            memset(&params, 0, sizeof(params));
            params.javaActivityObject = thiz;
            JavaVM *jvm;
            env->GetJavaVM(&jvm);
            params.javaVm = jvm;
            params.javaEnv = env;

            if(svrInitialize(&params) != SVR_ERROR_NONE)
            {
                LOG_ERROR("svrInitialize failed");
                return env->NewStringUTF("svr initialization failed!");
            }

            //set tracking mode
            svrSetTrackingMode(kTrackingRotation | kTrackingPosition);

            //enter vr mode
            svrBeginParams beginParamsparams;
            memset(&beginParamsparams, 0, sizeof(beginParamsparams));
            beginParamsparams.cpuPerfLevel = (svrPerfLevel)0;
            beginParamsparams.gpuPerfLevel = (svrPerfLevel)0;
            beginParamsparams.mainThreadId = gettid();
            beginParamsparams.nativeWindow = nativeWindow;

            if (svrBeginVr(&beginParamsparams) != SVR_ERROR_NONE)  //crash when called svrBeginVr
            {
                LOG_ERROR("svrBeginVr failed");
                return env->NewStringUTF("svr BeginVr failed!");
            }
        }
    }

    char str[256];
    float predictedTimeMs = svrGetPredictedDisplayTime();
    svrHeadPoseState poseState = svrGetPredictedHeadPose(predictedTimeMs); //if not initialized or not in vr mode, will return 00000000
//    LOG_INFO("Pose data from 1 millisecond ago => Position: (%0.2f, %0.2f, %0.2f); Orientation: (%0.2f, %0.2f, %0.2f, %0.2f)",  poseState.pose.position.x,
//            poseState.pose.position.y,
//            poseState.pose.position.z,
//            poseState.pose.rotation.x,
//            poseState.pose.rotation.y,
//            poseState.pose.rotation.z,
//            poseState.pose.rotation.w);

    //'{"1": 1, "2": 2, "3": {"4": 4, "5": {"6": 6}}}'
    //"\"" "Hello" "\""

//    sprintf(str, "Pose data from 1 millisecond ago => Position: (%0.2f, %0.2f, %0.2f); Orientation: (%0.2f, %0.2f, %0.2f, %0.2f)",  poseState.pose.position.x,
//            poseState.pose.position.y,
//            poseState.pose.position.z,
//            poseState.pose.rotation.x,
//            poseState.pose.rotation.y,
//            poseState.pose.rotation.z,
//            poseState.pose.rotation.w);

    sprintf(str, "{\"position\":{ \"x\":%0.2f, \"y\":%0.2f, \"z\":%0.2f},  \"rotation\": {\"x\":%0.2f, \"y\":%0.2f, \"z\":%0.2f, \"w\":%0.2f}}",
            poseState.pose.position.x,
            poseState.pose.position.y,
            poseState.pose.position.z,
            poseState.pose.rotation.x,
            poseState.pose.rotation.y,
            poseState.pose.rotation.z,
            poseState.pose.rotation.w);

    return env->NewStringUTF(str);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_realmax_quacomm6doftest_MainActivity_initSVR(JNIEnv *env, jobject instance,
                                                      jobject surface) {
    LOG_INFO("initSVR");

    if(surface == NULL){
        LOG_ERROR("surface is null");
        return false;
        //ignore init step and try to get 6-dof data directly
    }else{
        LOG_INFO("surface is not null");
        //try to initialize the EGL environment

        ANativeWindow * nativeWindow = ANativeWindow_fromSurface(env, surface);
        LOG_INFO("Got window %p", nativeWindow);

        if(nativeWindow == NULL){
            //if nativeWindow is NULL, then can not call beginVR successfully
            LOG_ERROR("native windows is null");
        }
        else{
            LOG_INFO("nativeWindow is not null, try to init ELG&VRSDK");
            //egl init
            const EGLint attribs[] = {
                    EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
                    EGL_BLUE_SIZE, 8,
                    EGL_GREEN_SIZE, 8,
                    EGL_RED_SIZE, 8,
                    EGL_NONE
            };
            EGLDisplay display;
            EGLConfig config;
            EGLint numConfigs;
            EGLint format;
            EGLSurface eglsurface;
            EGLContext context;
            EGLint width;
            EGLint height;
            GLfloat ratio;

            if ((display = eglGetDisplay(EGL_DEFAULT_DISPLAY)) == EGL_NO_DISPLAY) {
                LOG_ERROR("eglGetDisplay() returned error %d", eglGetError());
                return false;
            }

            if (!eglInitialize(display, 0, 0)) {
                LOG_ERROR("eglInitialize() returned error %d", eglGetError());
                return false;
            }

            if (!eglChooseConfig(display, attribs, &config, 1, &numConfigs)) {
                LOG_ERROR("eglChooseConfig() returned error %d", eglGetError());
                return false;
            }

            if (!eglGetConfigAttrib(display, config, EGL_NATIVE_VISUAL_ID, &format)) {
                LOG_ERROR("eglGetConfigAttrib() returned error %d", eglGetError());
                return false;
            }

            ANativeWindow_setBuffersGeometry(nativeWindow, 0, 0, format);

            if (!(eglsurface = eglCreateWindowSurface(display, config, nativeWindow, 0))) {
                LOG_ERROR("eglCreateWindowSurface() returned error %d", eglGetError());
                return false;
            }

            if (!(context = eglCreateContext(display, config, 0, 0))) {
                LOG_ERROR("eglCreateContext() returned error %d", eglGetError());
                return false;
            }

            if (!eglMakeCurrent(display, eglsurface, eglsurface, context)) {
                LOG_ERROR("eglMakeCurrent() returned error %d", eglGetError());
                return false;
            }

            if (!eglQuerySurface(display, eglsurface, EGL_WIDTH, &width) ||
                !eglQuerySurface(display, eglsurface, EGL_HEIGHT, &height)) {
                LOG_ERROR("eglQuerySurface() returned error %d", eglGetError());
                return false;
            }
            //EGL init finished

            //if not EGL init step, some errors will be shown
            //E/libEGL: eglQueryContext:832 error 3006 (EGL_BAD_CONTEXT)
            //E/libEGL: eglQuerySurface:640 error 300d (EGL_BAD_SURFACE)

            //E/svr: svrUpdateEyeContextSurface: Failed to create eye context pbuffer surface

            //E/svr: Calling QVRServiceClient_Create()...
            //E/svr: QVRServiceClient_Create failed.
            //
            //E/svr: Error getting pose from predictive sensor!


            //init svr service
            svrInitParams params;
            memset(&params, 0, sizeof(params));
            params.javaActivityObject = instance;
            JavaVM *jvm;
            env->GetJavaVM(&jvm);
            params.javaVm = jvm;
            params.javaEnv = env;

            if(svrInitialize(&params) != SVR_ERROR_NONE)
            {
                LOG_ERROR("svrInitialize failed");
                return false;
            }

            //set tracking mode
            svrSetTrackingMode(kTrackingRotation | kTrackingPosition);

            //enter vr mode
            svrBeginParams beginParamsparams;
            memset(&beginParamsparams, 0, sizeof(beginParamsparams));
            beginParamsparams.cpuPerfLevel = (svrPerfLevel)0;
            beginParamsparams.gpuPerfLevel = (svrPerfLevel)0;
            beginParamsparams.mainThreadId = gettid();
            beginParamsparams.nativeWindow = nativeWindow;

            if (svrBeginVr(&beginParamsparams) != SVR_ERROR_NONE)  //crash when called svrBeginVr
            {
                LOG_ERROR("svrBeginVr failed");
                return false;
            }
        }
    }

    return true;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_realmax_quacomm6doftest_MainActivity_exitSVR(JNIEnv *env, jobject instance) {
    LOG_INFO("exitSVR");
    if (svrEndVr() != SVR_ERROR_NONE)
    {
        LOG_ERROR("svr end VR failed!");
        return false;
    }

    return true;
//    svrShutdown();

//    JNIEnv* javaEnv = GetJNIEnv(plugin.javaVm);
//    javaEnv->DeleteGlobalRef(plugin.activity);
//    plugin.activity = NULL;
//    plugin.isInitialized = false;


//    SvrResult svrEndVr()
//        LOGI("svrEndVr");
//    LOGI("Stopping Timewarp...");
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_realmax_quacomm6doftest_MainActivity_resetSVR(JNIEnv *env, jobject instance) {
    // TODO
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_realmax_quacomm6doftest_MainActivity_getPose(JNIEnv *env, jobject instance) {
    char str[256];
    float predictedTimeMs = svrGetPredictedDisplayTime();
    svrHeadPoseState poseState = svrGetPredictedHeadPose(predictedTimeMs); //if not initialized or not in vr mode, will return 00000000
    LOG_INFO("Pose data from 1 millisecond ago => Position: (%0.2f, %0.2f, %0.2f); Orientation: (%0.2f, %0.2f, %0.2f, %0.2f)",  poseState.pose.position.x,
             poseState.pose.position.y,
             poseState.pose.position.z,
             poseState.pose.rotation.x,
             poseState.pose.rotation.y,
             poseState.pose.rotation.z,
             poseState.pose.rotation.w);

    sprintf(str, "{\"position\":{ \"x\":%0.2f, \"y\":%0.2f, \"z\":%0.2f},  \"rotation\": {\"x\":%0.2f, \"y\":%0.2f, \"z\":%0.2f, \"w\":%0.2f}}",
            poseState.pose.position.x,
            poseState.pose.position.y,
            poseState.pose.position.z,
            poseState.pose.rotation.x,
            poseState.pose.rotation.y,
            poseState.pose.rotation.z,
            poseState.pose.rotation.w);

    return env->NewStringUTF(str);
}