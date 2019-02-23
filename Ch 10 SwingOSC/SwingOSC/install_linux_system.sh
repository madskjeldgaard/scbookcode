#!/bin/sh

cp -a SuperCollider/SCClassLibrary/SwingOSC $1/share/SuperCollider/Extensions
cp -a SuperCollider/Help/SwingOSC $1/share/SuperCollider/Extensions/SwingOSC/Help
mkdir $1/share/SuperCollider/SwingOSC
cp -a SuperCollider/examples $1/share/SuperCollider/SwingOSC

cp build/SwingOSC.jar $1/bin/
