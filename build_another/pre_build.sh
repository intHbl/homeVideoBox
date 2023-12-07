#!/bin/bash

rm app/src/main/res/drawable*/app_icon.png

cp_file(){
	if [ -z "$1" ];then
		return
	fi
	
	src="build_another/files/$(basename "$1")"
	echo "[INFO] cp $src  -> $1"
	cp "$src" "$1"
}

echo "[INFO] cp_file ...."
cp_file  app/src/main/res/drawable-xxxhdpi/app_icon.png
cp_file  app/src/main/res/drawable/app_bg.png
cp_file  app/src/main/res/values/strings.xml
cp_file  app/build.gradle

cp_file  app/src/main/java/com/github/tvbox/osc/api/ApiConfig.java

echo "   # # "