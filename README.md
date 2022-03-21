# RunningPlan

## Von der Idee zur App

Manche kennen dies vielleicht. Man nimmt sich vor, mehr für seine Gesundheit zu tun. So geht es mir auch immer wieder. :grinning:
Dafür suchte ich mal wieder in den unendlichen Weiten des Netzes nach Anregungen und fand dabei eine wunderbare Seite 
zum Thema Laufen. Die Seite von Christian Zangl: https://lauftipps.ch.

Neben den vielen nützlichen Informationen stellt Herr Zangl auch Trainingspläne bereit. Mithilfe dieser Pläne können 
auch Amateure wie ich in das Laufen einteigen.

Nun bin ich Informatiker mit einem Faible für das Programmieren und mir kam die Idee, das Ganze als App zu erstellen. 
Herr Zangl gab mir freundlicherweise die Erlaubnis, seine Pläne hierfür zu verwenden. Noch einmal vielen Dank hierfür!
Als langjähriger Apple-Nutzer habe ich mich also in die Sprache [Swift](https://developer.apple.com/swift/) und die 
dazugehörige IDE [Xcode](https://apps.apple.com/us/app/xcode/id497799835?mt=12) eingearbeitet. 

Die App war prinzipiell fertig.

Allerdings bin ich in der Zwischenzeit aus verschiedenen Gründen dann komplett von Apple weg und nutze nur noch 
Linux (Mint). Als SmartPhone-Plattform nutze ich nun [CalyxOS](https://calyxos.org/). Damit musste ich auch meine App 
neu programmieren. Da kam mir entgegen, dass ich mit Java immer mal wieder zu tun hatte.
[Kotlin](https://kotlinlang.org/) habe ich mir angeschaut und finde es, genau wie Swift, sehr gut. 
Allerdings hätte ich zur Einarbeitung in eine neue IDE, die Android-Programmierung auch noch eine neue Programmiersprache lernen müssen. 
Da war mir dann doch zu viel.

Tja, also hat es nun über ein Jahr von der Idee zur ersten Beta-Version der App gedauert. Ja, und auch Android hält 
genau wie Swift viele kleine "Nettigkeiten" :confused: für einen unerfahrenen Entwickler wie mich bereit. 
Für aus Sicht des Nutzers verständliche Dinge und Einstellungen, z.B. zum Thema Sicherheit, sorgen für Entwickler immer wieder für
Herausforderungen :wink:.

Ein Beispiel gefällig: Wenn man läuft, bleibt die App ja nicht ständig sichtbar und aktiv, man schaut ja nicht ständig 
auf das Smartphone. Die Trainingszeit allerdings soll natürlich weiterlaufen. Was der Nutzer nicht weiß und auch nicht 
wissen muss: Sobald eine App im Hintergrund (nicht sichtbar) ist, "macht" sie auch nichts mehr :astonished:! 
Auch keine Zeit zählen oder eine Strecke aufzeichnen!

Ich mache alles nebenberuflich und ohne finanzielle Interessen. Deshalb seien Sie bitte nachsichtig, wenn nicht immer 
alles "rund" läuft. Für Anregungen, konstruktive Kritik und Verbesserungsvorschläge bin ich immer offen.

Auch die Erstellung von Dokumentationen, diesen Seiten oder auch die Umwandlung der Laufpläne kosten Zeit und Energie.


## Die Idee hinter der App

Ideen für die App habe ich noch viele. Alle können jedoch aus Zeitgründen leider nicht sofort umgesetzt werden. 
Deshalb (vorerst) die Konzentration auf die wichtigsten Funktionen. Und auch diese Funktionen, so banal sie vielleicht 
erscheinen, sind aufwendig umzusetzen.

- Bereitstellung der Laufpläne von Herrn Zangl in einer für die App nutzbaren Form ([JSON](https://de.wikipedia.org/wiki/JavaScript_Object_Notation))
- Import von Vorlagen
- Laufplan wählen, Start-Datum und Trainingstage festlegen (Woche / Wochenende, Anzahl der Lauf-Tage)
- Läufe aufzeichnen und je nach Strecke / Zeit / akustisch / grafisch Hinweise geben (Stop, Start, Pause, Langsam, …)
- nach der Tour das aufgezeichnete Training auswerten

Die App ist nicht für Laufprofis gedacht und erhebt keinen Anspruch darauf ein korrektes Lauftraining zu ermöglichen. 
Dafür empfehle ich, sich z.B. auf der Seite von Herrn Zangl zu informieren.

Ich persönlich möchte z.B. nicht Apps von Adidas, Nike, … verwenden und meine "Erfolge" auch nicht teilen.
Eher soll ein (privates) Trainingstagebuch angelegt werden können. Ich bin Informatiker und auch deshalb kein großer
Freund von bedrucktem Papier. Und möchte nicht mit Papier :page_facing_up: und Uhr :stopwatch: das Lauftraining :running:
beginnen.

Der Nutzer kann sich an die Vorgaben halt, muss aber nicht. Es sollen Empfehlungen sein. Ich persönlich würde / könnte 
z.B. am Wochenende nicht laufen. Also würde sich der Plan "nach hinten verschieben".

Die Pläne stehen als Vorlagen auf meiner Webseite (s.u.) bereit, weitere Vorlagen werden folgen. 
Parallel wird es App auf Basis von Java für Windows, macOS und Linux geben, mit der man die Laufpläne verändern oder 
komplett eigene anlegen kann. Die App gab es bereits für Swift und muss jetzt "nur" noch umprogrammiert werden.


## Was kann die App „Laufplan“?

Auf Grundlage der von Herrn Christian Zangl entwickelten [Laufplänen](https://lauftipps.ch/trainingsplaene/alle-trainingsplaene-auf-einen-blick/) können Laufanfänger und Hobby-Läufer ihre Lauftrainings mit Unterstützung eines Smartphones durchführen.
Die von Herrn Zangl angebotenen Laufpläne wurden von mir in ein zur Nutzung in der App erforderliches Format gebracht und können dort genutzt werden. 
Die Vorlagen können angepasst oder eigene Pläne angelegt werden.


## Was kann die App „Laufplan“ nicht?

Die App kann und soll nicht die Ansprüche von professionellen Läufern erfüllen. Es gibt keine Funktionen für „Social Media“, wie z.B. das Teilen von Läufen. 
Dafür gibt es genug Apps. Ich bin auch kein Laufprofi und die App bietet dementsprechend auch keine professionelle Unterstützung oder erhebt auch nicht den Anspruch „lauftechnisch“ korrekt zu sein.

Unbesehen davon würde ich gern Tipps zum Laufen in der App anbieten. Wenn jemand mir eigenes Material zur Verfügung stellen möchte, sehr gern.  


## Installation der App

Eine App unter Android nutzen zu können, ist zum Glück einfacher als bei Apple. Aus Sicherheitsgründen kan dies ein Nachteil sein. 
Im Prinzip können Sie sich die App  wie ein ZIP-Archiv vorstellen. Alles, was die App benötigt, ist im Archiv, der APK-Datei. 
Sie können das gerne nachprüfen, in dem Sie die Datei mit einem Entpacker-Programm, wie z.B. 7zip, einfach mal auspacken.

Die App ist also eine Datei, welche Sie wie unter Windows z.B. einfach direkt auf Ihrem SmartPhone installieren könnten.
Da so eine Datei jedoch auch Schadprogramme enthalten kann, sollten Sie dies nur tun, wenn Sie dem Programmierer und 
der Quelle (Download) der Datei vertrauen! 
Mich z.B. kennen Sie ja auch nicht persönlich und meine App könnte ja "böse" Absichten hegen :imp:.

Darum gibt es den App-Store. Der "offizielle" von Google und auch alternative Stores für Menschen, die 
(verständlicherweise) Google nicht (sehr) vertrauen.
Ich selbst nutze den Playstore auch nur über [Aurora](https://f-droid.org/en/packages/com.aurora.store/) für Apps und 
ansonsten hauptsächlich [F-Droid](https://f-droid.org/).

Sobald die App aus **meiner Sicht** auf die Menschheit losgelassen werden kann, werde ich versuchen die App sowohl 
im PlayStore, wie auch bei F-Droid zur Verfügung stellen zu lassen.

Bis dahin finden Sie die direkt installierbare APK auf Github. Sie können sich hier den Quelltext anschauen und 
müssten mir einfach vertrauen.

## Screenshots

### Überblick



## Links

[Laufpläne von Herrn Zangl](https://lauftipps.ch/kostenlose-trainingsplaene/)

[Weitere Informationen zur App](https://www.hirola.de/s/laufplan-runningplan/)

[Mein XING-Profil](https://www.xing.com/profile/Michael_Schmidt2350/cv) <image src="https://user-images.githubusercontent.com/48058062/152635585-d82a0f6d-1c4b-42c5-831f-eaf3caba1bd8.png" width="20" height="20">
