## ApkRenamer

Simple bash script that creates new apps with great aims

**Requirements**

* Oracle JDK 8+

* Currently on Linux 32bit: zipalign must be set in PATH

* Currently on Windows [Cygwin](https://www.cygwin.com/) (not tested)

Also on Mac may be required some changes in main bash script.

Tested only on Linux :heart:

**How to use**

 1. [Download][1] or clone the project:

    `git clone https://github.com/androidovshchik/DoSmthGreat.git`

 2. Then put *base apk* `DoSmthGreat...apk` (latest version recommended) in ./AimGenerator/

 3. Create a folder with desired app name e.g. `DoNewAwesomeAim`

 4. Fill it (or later) with content e.g. data${VERSION}.db with notification.ogg. Edit them at any time

 5. Run script `sh generate.sh "DoNewAwesomeAimFrom" "DD.MM.YYYY" "DD.MM.YYYY"` where:

> 1-st arg (required): folder name
>
> 2-nd arg (optional): `From` date
>
> 3-rd arg (optional): `To` date

 6. Look at `DoNewAwesomeAim` folder :smile: for `_DoMyAwesomeAim[FromDD.MM.YYYY][ToDD.MM.YYYY].apk`

 Your are done! Copy this folder to device on home dir and install it's apk

[1]: https://github.com/androidovshchik/DoSmthGreat/archive/master.zip
