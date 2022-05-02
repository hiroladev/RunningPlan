# RunningPlan

*Read this in other languages: [Deutsch](README.de.md)*

## From the idea to the app

Some may know this. You decide to do more for your health. That's how I always feel. :grinning:
For this I searched the endless expanse of the net for suggestions and found a wonderful site
about running. Christian Zangl's website: https://lauftipps.ch.

In addition to lots of useful information, Mr. Zangl also provides training plans. With the help of these plans
even amateurs like me get into running.

Now I'm a computer scientist with a penchant for programming and I had the idea of creating the whole thing as an app.
Mr. Zangl kindly gave me permission to use his plans for this. Thank you again for this!
So, as a longtime Apple user, I've become familiar with the [Swift](https://developer.apple.com/swift/) language and the
associated IDE [Xcode](https://apps.apple.com/us/app/xcode/id497799835?mt=12) incorporated.

The app was basically finished.

However, in the meantime I have completely left Apple for various reasons and only use
Linux (Mint). I now use [CalyxOS](https://calyxos.org/) as my smartphone platform. With that, I also had to use my app
reprogram. It was good for me that I had to deal with Java from time to time.
I took a look at [Kotlin](https://kotlinlang.org/) and, like Swift, I think it's very good.
However, I would have had to learn a new programming language to familiarize myself with a new IDE, Android programming.
Then it was too much for me.

Well, it has now taken over a year from the idea to the first beta version of the app. Yes, and Android holds up too
just like Swift lots of little "nice things" :confused: ready for an inexperienced developer like me.
Developers always ensure things and settings that are understandable from the user's point of view, e.g. on the subject of security
Challenges :wink:.

An example: when you're running, the app doesn't always remain visible and active, you don't look all the time
on the smartphone. Of course, the training time should continue. What the user doesn't know and doesn't either
must know: As soon as an app is in the background (not visible), it "does" nothing more :astonished:!
Also, don't count time or record a route!

I do everything part-time and without financial interests. So please be lenient if not always
everything runs "round". I am always open to suggestions, constructive criticism and suggestions for improvement.

The creation of documentation, these pages or the conversion of the running schedules also cost time and energy.


## The idea behind the app

I still have a lot of ideas for the app. Unfortunately, not all of them can be implemented immediately due to time constraints.
Therefore (for the time being) the concentration on the most important functions. And these functions too, as banal as they may be
appear are difficult to implement.

- Provision of Mr. Zangl's running plans in a form that can be used for the app ([JSON](https://de.wikipedia.org/wiki/JavaScript_Object_Notation))
- Import templates
- Choose a running plan, set the start date and training days (week / weekend, number of running days)
- Record runs and give acoustic / graphical instructions depending on the distance / time (stop, start, pause, slow, ...)
- evaluate the recorded training after the tour

The app is not intended for running professionals and does not claim to enable correct running training.
For this I recommend, for example, to inform yourself on Mr. Zangl's website.

Personally, I don't want to use apps from Adidas, Nike, ... and I don't want to share my "successes".
Rather, a (private) training diary should be able to be created. I am a computer scientist and therefore not a big one
Printed paper friend. And I don't want to do the running training with paper :page_facing_up: and watch :stopwatch: :running:
begin.

The user can adhere to the specifications, but does not have to. They should be recommendations. I personally would/could
e.g. not running at the weekend. So the plan would be "postponed".

The plans are available as templates on my website (see below), more templates will follow.
At the same time, there will be an app based on Java for Windows, macOS and Linux, with which you can change the running plans or
can create your own. The app already existed for Swift and now "only" needs to be reprogrammed.


## What can the "RunningPlan" app do?

Based on the [running plans](https://lauftipps.ch/trainingsplaene/alle-trainingsplaene-auf-einen-blick/) developed by Mr. Christian Zangl, beginners and hobby runners can carry out their running training with the support of a smartphone.
The running plans offered by Mr. Zangl were put into a format required for use in the app and can be used there.
The templates can be adapted or you can create your own plans.


## What can't the "RunningPlan" app do?

The app cannot and should not meet the demands of professional runners. There are no "social media" features such as sharing runs.
There are enough apps for that. I am also not a running professional and the app accordingly does not offer any professional support or claim to be technically correct.

That aside, I'd like to offer running tips in the app. If someone would like to provide me with their own material, they are very welcome.


## Installing the app

Fortunately, using an app on Android is easier than on Apple. For security reasons, this can be a disadvantage.
In principle, you can imagine the app as a ZIP archive. Everything the app needs is in the archive, the APK file.
You are welcome to check this by simply unpacking the file with an unpacker program such as 7zip.

The app is therefore a file that you can simply install directly on your smartphone, just like under Windows.
However, since such a file can also contain malicious programs, you should only do this if you want the programmer and
trust the source (download) of the file!
For example, you don't know me personally and my app could have "bad" intentions :imp:.

That's why the App Store exists. The "official" Google and also alternative stores for people who
(understandably) don't trust Google (very much).
I myself only use the Playstore via [Aurora](https://f-droid.org/en/packages/com.aurora.store/) for apps and
otherwise mainly [F-Droid](https://f-droid.org/).

As soon as the app can be unleashed on humanity from **my point of view**, I will try the app both
in the PlayStore, as well as in F-Droid.

Until then, you can find the directly installable APK on Github. You can view the source code here and
just have to trust me.


## Screenshots

|                                                                **Overview**                                                                 | **Training** |                                                              **Running plans**                                                              |
|:-------------------------------------------------------------------------------------------------------------------------------------------:| :---: |:-------------------------------------------------------------------------------------------------------------------------------------------:|
| <img src="https://user-images.githubusercontent.com/48058062/159230830-dd74e050-c3b4-4d10-a65e-9cb44da2dd30.png" width="270" height="480"/> | <img src="https://user-images.githubusercontent.com/48058062/159230901-42658b61-88a4-4451-8139-088c250d5860.png" width="270" height="480"/> | <img src="https://user-images.githubusercontent.com/48058062/159230975-3bb4a868-9e85-4e85-afad-b5382483ad7f.png" width="270" height="480"/> |

|                                                     **Details of running plan**                                                     | |
|:-------------------------------------------------------------------------------------------------------------------------------------------:| :---: | 
| <img src="https://user-images.githubusercontent.com/48058062/159231070-c5fea648-cc87-4493-b562-3b19f4fd6285.png" width="270" height="480"/> | <img src="https://user-images.githubusercontent.com/48058062/159231123-4fdbaa20-5915-4db7-831b-cd6fcb22e716.png" width="270" height="480"/> |


## Requests, criticism, errors, comments, suggestions for improvement ...

Bring it on! :+1: Please write me everything. The app is supposed to be useful.
Either you use the same [page](https://github.com/hiroladev/RunningPlan/issues) or\
You write me an <a href="mailto:development@hirola.de">E-Mail<a>.


## Links

[Running plans from Mr. Zangl](https://lauftipps.ch/kostenlose-trainingsplaene/)

[More information about the app (only in German)](https://www.hirola.de/s/laufplan-runningplan/)

[My XING-Profile](https://www.xing.com/profile/Michael_Schmidt2350/cv) <image src="https://user-images.githubusercontent.com/48058062/152635585-d82a0f6d-1c4b-42c5-831f-eaf3caba1bd8.png" width="20" height="20">
