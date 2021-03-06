## AimGenerator

Simple bash script that creates new apps with great aims

Apktool and other ways not fully renamed package. So the only easy way is to change it in sources

**Requirements**

* Oracle JDK 8+

* Android SDK

* Currently on Linux 32bit: zipalign must be set in PATH

* Currently on Windows [Cygwin](https://www.cygwin.com/) (not tested)

Also on Mac may be required some changes in main bash script.

Tested only on Linux 64bit :heart:

**How to use**

 1. [Download][1] or clone the project:

    `git clone https://github.com/androidovshchik/DoSmthGreat.git`

 2. Create a folder with desired app name e.g. `DoNewAwesomeAim`

 3. Fill it (or later) with content e.g. `data${VERSION}.db` with `notification.ogg`. Edit them at any time

 4. Run script `sh generate.sh "DoNewAwesomeAim" "DD.MM.YYYY" "DD.MM.YYYY"` where:

> 1-st arg (required): folder name
>
> 2-nd arg (optional): `From` date
>
> 3-rd arg (optional): `To` date

 6. Look at `DoNewAwesomeAim` folder :smile: for `_DoMyAwesomeAim[FromDD.MM.YYYY][ToDD.MM.YYYY].apk`

Your are done! Copy this folder to device on home dir and install it's apk

*Note!* **package name and signature** depend on folder name. So it must be permanent. As a bonus if there will be later a new version of apk then it's easy to update an existing app without deletion of data

[1]: https://github.com/androidovshchik/DoSmthGreat/archive/master.zip

