/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

#include <jni.h>

#include <Cocoa/Cocoa.h>

/* Our global variables */
static JavaVM *g_jvm;
static jobject g_object;
static jclass g_class;
static jmethodID g_postMethodID, g_postProximityMethodID;

/*
** A subclass of NSApplication which overrides sendEvent and calls back into Java with the event data for mouse events.
** We don't handle tablet proximity events yet.
*/
@interface CustomApplication : NSApplication 
@end

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved)
{
  g_jvm = vm;
  
  return JNI_VERSION_1_4;
}

static jint GetJNIEnv(JNIEnv **env, bool *mustDetach)
{
    jint getEnvErr = JNI_OK;
    *mustDetach = false;
    if (g_jvm) {
        getEnvErr = (*g_jvm)->GetEnv(g_jvm, (void **)env, JNI_VERSION_1_4);
        if (getEnvErr == JNI_EDETACHED) {
            getEnvErr = (*g_jvm)->AttachCurrentThread(g_jvm, (void **)env, NULL);
            if (getEnvErr == JNI_OK)
                *mustDetach = true;
        }
    }
    return getEnvErr;
}

@implementation CustomApplication
- (void) sendEvent:(NSEvent *)event
{
    JNIEnv *env;
    bool shouldDetach = false;
	int clickCount = -1;

    switch ( [event type] ) {
    case NSLeftMouseDown:
    case NSLeftMouseUp:
		clickCount = [event clickCount];
	case NSMouseMoved:
    case NSLeftMouseDragged:
//		if( [event subtype] != NX_SUBTYPE_TABLET_POINT ) break;
    case NSTabletPoint:
		if (GetJNIEnv(&env, &shouldDetach) != JNI_OK) {
			NSLog(@"JNITablet: Couldn't attach to JVM");
			return;
		}
		NSPoint tilt = [event tilt];
		NSPoint location = [event locationInWindow];
		NSArray *vendorDefined = [event vendorDefined];
		(*env)->CallVoidMethod( env, g_object, g_postMethodID,
				[event type],
				[event deviceID],
				location.x,
				location.y,
				[event absoluteX],
				[event absoluteY],
				[event absoluteZ],
				[event buttonMask],
//				[event buttonNumber],
				clickCount,
				[event pressure],
				[event rotation],
				tilt.x,
				tilt.y,
				[event tangentialPressure],
				[[vendorDefined objectAtIndex:0] shortValue],
				[[vendorDefined objectAtIndex:1] shortValue],
				[[vendorDefined objectAtIndex:2] shortValue]
			);
		if (shouldDetach)
			(*g_jvm)->DetachCurrentThread(g_jvm);
        break;
		
	case NSTabletProximity:
		if (GetJNIEnv(&env, &shouldDetach) != JNI_OK) {
			NSLog(@"JNITablet: Couldn't attach to JVM");
			return;
		}
		(*env)->CallVoidMethod( env, g_object, g_postProximityMethodID,
			[event deviceID],
			[event capabilityMask],
			[event isEnteringProximity] ? 1 : 0,
			[event pointingDeviceID],
			[event pointingDeviceSerialNumber],
			[event pointingDeviceType],
			[event systemTabletID],
			[event tabletID],
			[event uniqueID],
			[event vendorID],
			[event vendorPointingDeviceType]
			);
		if (shouldDetach)
			(*g_jvm)->DetachCurrentThread(g_jvm);
		break;
		
    default:
        break;
    }
    [super sendEvent: event];
}
@end

/*
** Start up: use poseAsClass to subclass the NSApplication object on the fly.
*/
JNIEXPORT void JNICALL Java_com_jhlabs_jnitablet_TabletWrapper_startup(JNIEnv *env, jobject this) {
    [CustomApplication poseAsClass: [NSApplication class]];
    g_object = (*env)->NewGlobalRef( env, this );
    g_class = (*env)->GetObjectClass( env, this );
    g_class = (*env)->NewGlobalRef( env, g_class );
    if( g_class != (jclass) 0 ) {
        g_postMethodID			= (*env)->GetMethodID( env, g_class, "postEvent", "(IIFFIIIIIFFFFFSSS)V" );
        g_postProximityMethodID	= (*env)->GetMethodID( env, g_class, "postProximityEvent", "(IIZIIIIIIII)V" );
	} else {
		NSLog(@"JNITablet: Couldn't register java methods");
	}
}

/*
** Shut down: release our data.
*/
JNIEXPORT void JNICALL Java_com_jhlabs_jnitablet_TabletWrapper_shutdown(JNIEnv *env, jobject this) {
    if ( g_object )
        (*env)->DeleteGlobalRef( env, g_object );
    if ( g_class )
        (*env)->DeleteGlobalRef( env, g_class );
    g_object = NULL;
    g_class = NULL;
}
