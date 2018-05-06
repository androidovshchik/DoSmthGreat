# DoSmthGreat
> Android app which helps to do something great in life

<p>
  <a href="https://www.paypal.me/mrcpp" title="Donate to this project using Paypal">
    <img src="https://img.shields.io/badge/paypal-donate-green.svg" alt="PayPal donate button" height="18"/>
  </a>
</p>

A short story... It's hard for me to control my time and works which i must done. So i came up to conclusion about giving such important task for my phone to remind me what i must do at certain time

So let's plan today a great purpose and getting closer to it everyday! Good luck!!! :fist::smile:

**PS** Please, never gives up and always be a little bit or *more* better :)

### Binaries

[Download example apk with folder files here](https://github.com/androidovshchik/DoSmthGreat/releases)

### Usage example

Folder `Example` contains files needed for app work:

* `notification.(ogg|*)` - sound for notifications (will be *default* if not exists)
* `data.db` (**required**) - SQLite database (it will be copied after accessing read permission and on upgrade tasks)

*IMPORTANT!* Copied folder name must be the same as app name (without prefix, *From* and *To* keywords with their dates) and it must be in home directory on external storage

Fields:

| Name | Short Description |
| :------------- |:-------------|

### Customize app for your next purpose


* In Google Play there is a great app for duplicating intalled apps [App Cloner](https://play.google.com/store/apps/details?id=com.applisto.appcloner)
* Clone installed e.g. `Example` app with changing it's name (may be also icon to taste). Format of name:

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
> `DD.MM.YYYY` (**required only after keyword**) - the date. Comes with keywords (**From** or **To** which **are not** *case-sensitive*)

* Copy folder with needed files (also edit SQLite file)

You are done! :relaxed: 

Launch new app

*IMPORTANT NOTE!* if you falied with achievement of purpose then it's better to delete the app and start new. **No fails!** otherwise you will achive nothing

### Tips

- Awesome crossplatform program for edit SQLite databases https://sqlitestudio.pl

- Days in calendar are clickable

### License

<img src="art/gplv3-127x51.png">
