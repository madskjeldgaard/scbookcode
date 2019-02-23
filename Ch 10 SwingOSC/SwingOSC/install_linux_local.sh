#!/bin/sh

cp -a SuperCollider/SCClassLibrary/SwingOSC ~/share/SuperCollider/Extensions
cp -a SuperCollider/Help/SwingOSC ~/share/SuperCollider/Extensions/SwingOSC/Help
mkdir ~/share/SuperCollider/SwingOSC
cp -a SuperCollider/examples ~/share/SuperCollider/SwingOSC

cp build/SwingOSC.jar ~/share/bin/
