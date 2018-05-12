#! /bin/bash
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
iam=`whoami`
mkdir -p "$1"
find "./$1" -name "*.apk" -type f -delete
find "./$1" -name "_*-mapping.txt" -type f -delete
alias=`echo "$1" | sed "s/[^a-zA-Z0-9.]/d/g" | tr 'A-Z' 'a-z'`
# package name must start with letter (not number e.g.)
packageName=rf.androidovshchik.i$alias
echo \> New package name is $packageName
echo \> New app name is \"$appName\"
if [ $iam != "vlad" ]; then
    echo \> New alias, store and key passwords is \"$alias\"
fi
echo \> Setting configuration
cd ..
cp -f app/buildCopyForAimGenerator.txt app/build.gradle
sed -i "s/rf.androidovshchik.dosmthgreat/$packageName/g" app/build.gradle
sed -i "s/DoSmthGreat/$appName/g" app/build.gradle
if [ $iam = "vlad" ]; then
    echo \> Building new debug apk
    ./gradlew assembleDebug 2>/dev/null | grep -F ":app:"
    echo \> Moving files
    cp -f app/buildCopyForAimGenerator.txt app/build.gradle
    apk=`ls app/build/outputs/apk/debug | grep "^_$appName.*\.apk$"`
    mv "app/build/outputs/apk/debug/$apk" "AimGenerator/$1"
else
    echo \> Generating certificate
    rm -f app/certificate.jks
    keytool -genkey -noprompt -keystore app/certificate.jks -alias $alias -storepass $alias \
        -keypass $alias -dname "c=RU" -keyalg RSA -keysize 2048 -validity 10000 2>/dev/null
    echo \> Building new release apk
    ./gradlew assembleRelease -PstoreFile="certificate.jks" -PstorePassword=$alias \
        -PkeyAlias=$alias -PkeyPassword=$alias 2>/dev/null | grep -F ":app:"
    rm -f app/certificate.jks
    echo \> Moving files
    cp -f app/buildCopyForAimGenerator.txt app/build.gradle
    mv "app/release/$appName-mapping.txt" "AimGenerator/$1/_$appName-mapping.txt"
    apk=`ls app/build/outputs/apk/release | grep "^_$appName.*\.apk$"`
    mv "app/build/outputs/apk/release/$apk" "AimGenerator/$1"
fi
echo \> All is done