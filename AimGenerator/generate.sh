#!/bin/bash
if [ "$#" -ge 3 ]; then
    appName=$1From$2To$3
elif [ "$#" -ge 2 ]; then
    appName=$1From$2
elif [ "$#" -ge 1 ]; then
    appName=$1
elif [ "$#" -ne 1 ]; then
    echo You must enter at least folder name as the 1-st argument
    exit
fi
apks=`ls ./*.apk`
for apk in $apks
do
   echo \> Preparing
   mkdir -p "$1"
   find "./$1" -name "*.apk" -type f -delete
   echo \> Found base apk \"$apk\"
   alias=`echo "$1" | sed "s/[^a-zA-Z0-9.]/d/g" | tr 'A-Z' 'a-z'`
   packageName=rf.androidovshchik.i$alias
   echo \> New package name is $packageName
   echo \> New app name is \"$appName\"
   echo \> New alias is \"$alias\"
   echo \> Decompiling base apk
   rm -rf ./temp
   java -jar ./tools/apktool_2.3.3.jar d -s -f -o ./temp $apk 2>/dev/null
   sed -i "s/renameManifestPackage: null/renameManifestPackage: $packageName/g" ./temp/apktool.yml
   sed -i "s/@string\/app_name/$appName/g" ./temp/AndroidManifest.xml
   echo \> Building new apk
   java -jar ./tools/apktool_2.3.3.jar b -o "./$1/_$appName.apk" ./temp 2>/dev/null
   echo \> Signing new apk
   rm -f ./certificate.jks
   keytool -genkey -noprompt -keystore ./certificate.jks -alias $alias -storepass $alias \
       -keypass $alias -dname "c=RU" -keyalg RSA -keysize 2048 -validity 10000 2>/dev/null
   java -jar ./tools/uber-apk-signer-0.8.4.jar -a "./$1" --allowResign --ks ./certificate.jks \
       --ksAlias $alias --ksPass $alias --ksKeyPass $alias --overwrite >/dev/null
   echo \> Removing extra files
   rm -f ./certificate.jks
   rm -rf ./temp
   echo \> All is done
   exit
done
echo No base apk found