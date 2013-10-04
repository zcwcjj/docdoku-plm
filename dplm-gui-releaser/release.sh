#!/bin/bash

#
# DPLM Gui Releaser
#
# Author : Morgan Guimard
# Date : Wed Oct 2 2013
#
# Args : 
# $1 : source directory
# $2 : version (TODO : use date if no version specified)
#

# Check sources
if [ -d "$1" ] ; then
	echo "Source directory is $1";
	echo "Let's build dplm releases"
else
	echo "No such directory for $1"
	exit 1;
fi

# Vars init
RELEASER_DIR=$(dirname "$0");
SOURCE="$1/*";
TMP_DIR="$RELEASER_DIR/tmp";

NW_LINUX_32="$RELEASER_DIR/nw-releases/node-webkit-v0.7.5-linux-ia32/nw";
NW_LINUX_64="$RELEASER_DIR/nw-releases/node-webkit-v0.7.5-linux-x64/nw";
NW_WIN_32="$RELEASER_DIR/nw-releases/node-webkit-v0.7.5-win-ia32";
NW_OSX="$RELEASER_DIR/nw-releases/node-webkit-v0.7.5-osx-ia32/node-webkit.app";


OUT_DIR_LINUX_32="$RELEASER_DIR/out/linux-32";
OUT_DIR_LINUX_64="$RELEASER_DIR/out/linux-64";
OUT_DIR_WIN32="$RELEASER_DIR/out/win32";
OUT_DIR_OSX="$RELEASER_DIR/out/osx";


# Set the current dir
cd $RELEASER_DIR;

# Zip the source in a zip archive
echo "Creating nw archive ..."

# Remove old nw app if any
if [ -f "app.nw" ] ; then
	echo "Removing old archive"
	rm -f "app.nw";
fi

# Creating nw archive with sources
cp -R $SOURCE $TMP_DIR;
cd $TMP_DIR;
/usr/bin/zip -r ../app.nw *;
echo "... done";

#Clean the tmp dir
rm -rf $TMP_DIR/*;


############### 
#  LINUX 32   #
############### 

cd $RELEASER_DIR;
echo "Building linux 32bit app ...";

# prepare binary and pak file
/bin/cat $NW_LINUX_32 "app.nw" > "$TMP_DIR/dplm" && chmod +x "$TMP_DIR/dplm" ;
cp "$NW_LINUX_32.pak" $TMP_DIR;

# zipping dplm + pak file
cd $TMP_DIR;
zip dplm-linux-32-$2.zip *;

# Move the resulting archive in the output dir
mv dplm-linux-32-$2.zip $OUT_DIR_LINUX_32;

#Clean the tmp dir
rm -rf $TMP_DIR/*;

echo "... done";


###############
# LINUX 64    #
###############

cd $RELEASER_DIR;

echo "Building linux 64bit app ...";

# prepare binary and pak file
/bin/cat $NW_LINUX_64 app.nw > "$TMP_DIR/dplm" && chmod +x "$TMP_DIR/dplm" ;
cp "$NW_LINUX_64.pak" $TMP_DIR;

# zipping dplm + pak file
cd $TMP_DIR;
zip dplm-linux-64-$2.zip *;

# Move the resulting archive in the output dir
mv dplm-linux-64-$2.zip $OUT_DIR_LINUX_64;

#Clean the tmp dir
rm -rf $TMP_DIR/*;

echo "... done";



###############
# WINDOWS     #
###############

cd $RELEASER_DIR;
echo "Building windows app ...";

# prepare binary and DLLs
/bin/cat $NW_WIN_32/nw.exe app.nw > "$TMP_DIR/dplm.exe" && chmod +x "$TMP_DIR/dplm.exe" ;
cp "$NW_WIN_32/"*.dll $TMP_DIR;
cp "$NW_WIN_32/nw.pak" $TMP_DIR;
# zipping dplm + DLLs
cd $TMP_DIR;
zip dplm-win32-$2.zip *;

# Move the resulting archive in the output dir
mv dplm-win32-$2.zip $OUT_DIR_WIN32;

#Clean the tmp dir
rm -rf $TMP_DIR/*;


########################################################
# OSX (not available since chrome is only 32 bits...)  #
######################################################## 

cd $RELEASER_DIR;
echo "Building OSX app ...";

if [ -d "$OUT_DIR_OSX/dplm-$2.app/" ] ; then
	echo "Removing old .app"
	rm -rf "$OUT_DIR_OSX/dplm-$2.app/";
fi
# copy nutshell
echo "Copying nutshell ...";
cp -R $NW_OSX $OUT_DIR_OSX;
# rename
mv "$OUT_DIR_OSX/node-webkit.app/" "$OUT_DIR_OSX/dplm-$2.app/";
#copy the source in Contents/Resources
mkdir "$OUT_DIR_OSX/dplm-$2.app/Contents/Resources/app.nw"
echo "Copying source files ...";
cp -R $SOURCE "$OUT_DIR_OSX/dplm-$2.app/Contents/Resources/app.nw";
echo "Copying plist and icon ...";
#Replace Plist
#cp Info.plist "$OUT_DIR_OSX/dplm-$2.app/Contents/";
#Replace Icon
cp nw.icns "$OUT_DIR_OSX/dplm-$2.app/Contents/Resources/";
# chmod it
chmod -R 0775 "$OUT_DIR_OSX/dplm-$2.app/";
# zip the .app
cd $OUT_DIR_OSX;
zip -r dplm-$2.zip dplm-$2.app;
rm -rf dplm-$2.app;
echo "... done";


#################
# END OF SCRIPT #
#################


cd $RELEASER_DIR;
echo "All builds done, removing archive source...";
rm app.nw;
echo "... done";
echo "Exiting.";
echo "------------------";
exit 0;
