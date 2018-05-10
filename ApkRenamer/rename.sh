#!/bin/bash
if [ "$#" -ne 1 ]; then
    echo "You must enter the new app name as 1-st argument"
    exit
fi
apks=`ls ./*.apk`
for apk in $apks
do
   echo \> Found base apk \"$apk\"
   package=rf.androidovshchik.do`date | md5sum | cut -c1-22`
   echo \> It\'s new package name is $package
   echo \> It\'s new app name is \"$1\"
   java -jar ./tools/apktool_2.3.3.jar d -s -f $apk 2>/dev/null
   sed -i "s/renameManifestPackage: null/renameManifestPackage: $package/g" ${apk%.apk}/apktool.yml
   sed -i "s/@string\/app_name/$1/g" ${apk%.apk}/AndroidManifest.xml
   echo \> Clearing renamed folder
   rm -rf ./renamed/*
   java -jar ./tools/apktool_2.3.3.jar b -o "./renamed/$1.apk" ${apk%.apk} 2>/dev/null
   echo \> Signing new apk
   java -jar ./tools/uber-apk-signer-0.8.4.jar -a ./renamed --ks ./certificate.jks \
       --ksAlias DoAlias --ksPass DoPassword --ksKeyPass DoPassword >/dev/null
   echo \> All is done
   exit
done