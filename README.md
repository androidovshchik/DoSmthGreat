# DoSmthGreat
> Android app which helps to do something great in life

<p>
  <a href="https://www.paypal.me/mrcpp" title="Donate to this project using Paypal">
    <img src="https://img.shields.io/badge/paypal-donate-green.svg" alt="PayPal donate button" height="18"/>
  </a>
</p>

A short story... It's hard for me to control my time and works which i must done. So i came up to conclusion about giving such important task for my phone to remind me what i must do at certain time

Why there is a task `word`? As for me i think that words have a great power. So this is the best weapon to fight with oneself first of all.

And about `actions`. I think it doesn't matter to specify here what action has been done because they must be similar. The primary idea is simplicity and extensibility.

Let's plan today a great purpose and getting closer to it everyday!

Good luck!!! :fist::smile:

### Binaries

[Download `DoSmthGreat...apk` with folder files to start usage](https://github.com/androidovshchik/DoSmthGreat/releases)

*IMPORTANT NOTE!* min supported Android version is Jelly Bean (API 17)

Enjoy!

### Usage example

Any folder contains files needed for app work:

* `notification.ogg` - sound for notifications (will be *default* if not exists and the format must be OGG)
* `data${VERSION}.db` [e.g. data1.db] (**VERY required**) - SQLite database (it will be copied after accessing read permission and on refresh in menu).
* *ANY other files (e.g. images)*, they will be interpreted as actions. Also there is no need to define them in tables because of random selection

*IMPORTANT!* `*.mp3` they will be interpreted in addition for `sound` tasks for random selection

*IMPORTANT!* `VERSION` must be compatible to app for preventing crashes (there is a special item of menu with information)

*IMPORTANT!* If custom filename starts with `_` then it will not be random selected. Also any `subfolder` containers will be ignored

*IMPORTANT!* Copied folder name must be the same as app name (**with** prefix if exists, without *From* and *To* keywords with their dates) and it must be in home directory on external storage

### Description of `data${VERSION}.db` file

#### DDL for table `timeline` (includes tasks)

```
CREATE TABLE timeline (
    day  VARCHAR (1)  DEFAULT NULL,
    time VARCHAR (5),
    task VARCHAR (40) NOT NULL ON CONFLICT ABORT,
    data TEXT         DEFAULT NULL
);
```
> `day` Short name of day **not case-sensitive** (`Mon`, `Tue`, `Wed`, `Thu`, `Fri`, `Sat` or `Sun`) in which specified task must be executed (`Null` value or not in range means all weekdays)
>
> `time` has default time format `HH:MM`
>
> `task` must be chosen from *available list* below
>
> `data` additional information for below tasks:
> > `word` task: defines the special text to show otherwise random
> >
> > `voice` task (**required**): defines the text to speech
> >
> > `sound` task: defines any audio file to play otherwise random mp3 file (not starting with `_`)
> >
> > `action` task: defines the special text or file or both of them divided by separator `/*/` (doesn't matter order) otherwise will be random row from `actions` table or file (not starting with `_`)

#### Available tasks:

| Name | Description |
| :------------- |:-------------|
| `word` | Shows words to inspire for next achievements |
| `voice` | Makes speech with specified text. It must be confirmed (by clicking on notification) otherwise will be repeated every minute |
| `sound` | Makes playing audio file at specified time. No confirmation needed. No repeats |
| `action` | Specifies an action to be executed at appearing time. Must be confirmed in app with a positive number after execution |
| `comment` | Offers to write a short note about your thoughts today dut to primary aim of app |
| `result` | Prints in notification count of action days, all days and number of all actions were done |

#### DDL for table `records`

```
CREATE TABLE records (
    day     VARCHAR (10),
    comment TEXT         NOT NULL ON CONFLICT ABORT,
    actions INTEGER      NOT NULL ON CONFLICT ABORT
                         DEFAULT (0)
);
```
> `day` has default date format `YYYY-MM-DD` 
>
> `comment` your comment at this day
>
> `actions` counts actions per day (by clinking `+` buttons in app)

*IMPORTANT!* this table is technical and there is no need in filling it with data. In fact this is the only table which has permanent data (except deletion app event and similar)

#### DDL for table `words`

```
CREATE TABLE words (
    word TEXT    NOT NULL ON CONFLICT ABORT,
    best BOOLEAN NOT NULL ON CONFLICT ABORT
                 DEFAULT (0) 
);
```
> `word` some text for inspiration :)
>
> `best` if true then will be displayed with other *best* words on app start event

*IMPORTANT NOTE!* next row for `word` task is random selected

#### DDL for table `actions`

```
CREATE TABLE actions (
    description TEXT,
    filename    TEXT
);
```
> `description` (optional) explains what to do at the time when was appeared
>
> `filename` (optional) associated file (may be open by clicking on notification)

*IMPORTANT NOTE!* next row for `action` task is random selected or may be selected separate file existing in app folder and which has no description in db (but how about describing in filename? :) )

### Customize app for your next purpose

* In Google Play there is a great app for duplicating installed apps [App Cloner](https://play.google.com/store/apps/details?id=com.applisto.appcloner)
* Clone installed e.g. `DoExample` app with changing it's name (may be also icon to taste). Format of name:

```
DoPurposeFromDD.MM.YYYYToDD.MM.YYYY
```

> `Do` (optional) - *any* prefix for order purpose in common apps list
>
> `Purpose` (**recommended**) - the name of app (must be the same as it's folder name and not include `From` or `To` strings)
>
> `From` (**recommended**) - *X date* when you have planned a new purpose (if not specified then *x date* will be today every time)
>
> `To` (**optional**) - if you think that the purpose is absolutely achieved then set `To` date (this will stop future calculations) and later come back again and again for watching what a great work you have done somewhen
>
> `DD.MM.YYYY` (**required only after keyword**) - the date. Comes with keywords (**From** or **To** which **are not case-sensitive**)

* Copy folder with needed files (also edit SQLite file)

You are done! :relaxed: 

Launch new app

*NOTE!* if you failed with achievement of purpose then it's better to delete the app and start new. **No fails!** otherwise you will achive nothing

### Tips and tricks

- Awesome cross platform program for edit SQLite databases https://sqlitestudio.pl

- Days in calendar are clickable

- Text can be passed with html tags

### TODO list

- [ ] Make calendar grid system github like which shows action activity for everyday
- [ ] Make import/export of `records` table (in case of changing phone and etc.)
- [ ] Add sample apps with great ideas
- [ ] May be something else?)

### PS

Big thanks to library https://github.com/nichbar/MonthView

Please, never gives up and always be a little bit or *more* better :)

### License

<img src="art/gplv3-127x51.png">
