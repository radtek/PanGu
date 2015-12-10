/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class nirvana_hall_image_jni_NativeImageConverter */

#ifndef _Included_nirvana_hall_image_jni_NativeImageConverter
#define _Included_nirvana_hall_image_jni_NativeImageConverter
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     nirvana_hall_image_jni_NativeImageConverter
 * Method:    loadLibrary
 * Signature: (Ljava/lang/String;I)J
 */
JNIEXPORT jlong JNICALL Java_nirvana_hall_image_jni_NativeImageConverter_loadLibrary
  (JNIEnv *, jclass, jstring, jint);

/*
 * Class:     nirvana_hall_image_jni_NativeImageConverter
 * Method:    freeLibrary
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_nirvana_hall_image_jni_NativeImageConverter_freeLibrary
  (JNIEnv *, jclass, jlong);

/*
 * Class:     nirvana_hall_image_jni_NativeImageConverter
 * Method:    decodeByManufactory
 * Signature: (JLjava/lang/String;Ljava/lang/String;[BI)[B
 */
JNIEXPORT jbyteArray JNICALL Java_nirvana_hall_image_jni_NativeImageConverter_decodeByManufactory
  (JNIEnv *, jclass, jlong, jstring, jstring, jbyteArray, jint);

/*
 * Class:     nirvana_hall_image_jni_NativeImageConverter
 * Method:    decodeByWSQ
 * Signature: ([BIII)[B
 */
JNIEXPORT jbyteArray JNICALL Java_nirvana_hall_image_jni_NativeImageConverter_decodeByWSQ
  (JNIEnv *, jclass, jbyteArray, jint, jint, jint);

/*
 * Class:     nirvana_hall_image_jni_NativeImageConverter
 * Method:    encodeByWSQ
 * Signature: ([BIIII)[B
 */
JNIEXPORT jbyteArray JNICALL Java_nirvana_hall_image_jni_NativeImageConverter_encodeByWSQ
  (JNIEnv *, jclass, jbyteArray, jint, jint, jint,jint);

#ifdef __cplusplus
}
#endif
#endif
