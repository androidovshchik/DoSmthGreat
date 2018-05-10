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
   mkdir -p "$1"
   echo \> Found base apk \"$apk\"
   packageName=rf.androidovshchik.do`date | md5sum | cut -c1-18`
   echo \> New package name is $packageName
   echo \> New app name is \"$appName\"
   echo \> Decompiling base apk
   java -jar ./tools/apktool_2.3.3.jar d -s -f -o ./temp $apk 2>/dev/null
   sed -i "s/renameManifestPackage: null/renameManifestPackage: $packageName/g" ./temp/apktool.yml
   sed -i "s/@string\/app_name/$appName/g" ./temp/AndroidManifest.xml
   echo \> Building new apk
   java -jar ./tools/apktool_2.3.3.jar b -o "./$1/temp.apk" ./temp 2>/dev/null
   echo \> Signing new apk
   java -jar ./tools/uber-apk-signer-0.8.4.jar -a "./$1" --ks ./certificate.jks --ksAlias DoAlias \
       --ksPass DoPassword --ksKeyPass DoPassword >/dev/null
   echo \> Removing extra files
   mv "./$1/temp-aligned-signed.apk" "./$1/_$appName.apk"
   rm "./$1/temp.apk"
   rm -rf ./temp
   echo \> All is done
   exit
done
echo No base apk found