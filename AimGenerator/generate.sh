#!/bin/bash
if [ "$#" -ge 3 ]; then
    appName="$1From$2To$3"
elif [ "$#" -ge 2 ]; then
    appName="$1From$2"
elif [ "$#" -ge 1 ]; then
    appName="$1"
elif [ "$#" -ne 1 ]; then
    echo You must enter at least folder name as the 1-st argument
    exit
fi
mkdir -p "$1"
find "./$1" -name "*.apk" -type f -delete
find "./$1" -name "*-mapping.txt" -type f -delete
alias=`echo "$1" | sed "s/[^a-zA-Z0-9.]/d/g" | tr 'A-Z' 'a-z'`
# package name must start with letter (not number e.g.)
packageName=rf.androidovshchik.i$alias
echo \> New package name is $packageName
echo \> New app name is \"$appName\"
echo \> New alias, store and key passwords is \"$alias\"
echo \> Setting configuration
cd ..
cp -f app/buildCopyForAimGenerator.txt app/build.gradle
sed -i "s/rf.androidovshchik.dosmthgreat/$packageName/g" app/build.gradle
sed -i "s/DoSmthGreat/$appName/g" app/build.gradle
echo \> Generating certificate
rm -f app/certificate.jks
keytool -genkey -noprompt -keystore app/certificate.jks -alias $alias -storepass $alias \
    -keypass $alias -dname "c=RU" -keyalg RSA -keysize 2048 -validity 10000 2>/dev/null
echo \> Building new apk
./gradlew assembleRelease -PstoreFile="certificate.jks" -PstorePassword=$alias \
    -PkeyAlias=$alias -PkeyPassword=$alias
rm -f app/certificate.jks
echo \> Moving files
cp -f app/buildCopyForAimGenerator.txt app/build.gradle
mv "app/release/$appName-mapping.txt" "AimGenerator/$1"
apks=`ls app/build/outputs/apk/release | grep "_$appName.*\.apk$"`
for apk in $apks
do
    mv "app/build/outputs/apk/release/$apk" "AimGenerator/$1"
    echo \> All is done
    exit
done
echo No apk found