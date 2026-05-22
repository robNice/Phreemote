# Phreemote – Philips TV-Steuerung für Android & Wear OS

Phreemote verwandelt dein **Android-Smartphone** oder deine **Wear-OS-Uhr** in eine Fernbedienung für deinen Philips-Fernseher.

<img src="docs/logo.webp" width="80" />

> Die App funktioniert **nur mit Fernsehern, die eine JointSpace-API bereitstellen**. Getestet wurde bislang API-Version 6. Bei einem **anderen JointSpace-API-Version** freue ich mich über Erfahrungsberichte und Beiträge aus der Community.

---

## Inhaltsverzeichnis

- [Voraussetzungen](#voraussetzungen)
- [Android-App](#android-app)
  - [Eigene Tasten](#eigene-tasten)
  - [Demo-Modus](#demo-modus)
- [Wear-OS-App](#wear-os-app)
- [Erste Einrichtung & Pairing](#erste-einrichtung--pairing)
  - [1. Fernseher suchen](#1-fernseher-suchen)
  - [2. Fernseher auswählen](#2-fernseher-auswählen)
  - [3. Pairing-Code am Fernseher ablesen](#3-pairing-code-am-fernseher-ablesen)
  - [4. Code eingeben](#4-code-eingeben)
  - [5. Fertig](#5-fertig)
- [Wenn das Pairing nicht klappt](#wenn-das-pairing-nicht-klappt)
- [Lizenz](#lizenz)

---

## Voraussetzungen

- **Smartphone oder Uhr** und **Fernseher** müssen im **selben lokalen Netzwerk** sein
- Der Fernseher muss **vollständig eingeschaltet** sein (kein Standby)
- Beim ersten Pairing am besten **in der Nähe des Fernsehers** bleiben – der TV zeigt einen Code an

---

## Android-App

| TV suchen | Pairing | Fernbedienung |
|:---------:|:-------:|:-------------:|
| ![scan](docs/scan-lan-mobile.png) | ![pair](docs/pair-mobile.png) | ![remote](docs/main-screen-mobile.png) |

Die Smartphone-App bietet eine vollständige Fernbedienung mit allen gängigen TV-Funktionen.  
Smartphone und Uhr koppeln **unabhängig voneinander** – das Pairing auf dem Handy hat keinen Einfluss auf die Uhr und umgekehrt.

### Eigene Tasten

In der Android-App lassen sich bis zu **4 eigene Tasten** konfigurieren, die in den Ecken des Steuerkreuzes erscheinen.  
Über **Einstellungen (⚙)** den Bereich **Eigene Tasten** aufklappen. Für jeden Slot kann ein beliebiger JointSpace-Befehl (z. B. Netflix, Amazon, Ambilight) und ein Emoji oder Symbol als Bezeichnung gewählt werden.  
Nur Slots, bei denen Befehl und Symbol gesetzt und die aktiviert sind, werden in der Fernbedienung angezeigt.

### Demo-Modus

In den Einstellungen **Demo-Modus** antippen, um die Fernbedienung ohne gekoppelten TV auszuprobieren.  
Alle Tasten sind sichtbar und bedienbar, es werden jedoch keine Befehle an ein Gerät gesendet.  
Den Demo-Modus beendet man über das Einstellungs-Symbol (⚙) in der Fernbedienungsansicht.

---

## Wear-OS-App

| Hauptfernbedienung | Ziffernfeld | Weitere Funktionen |
|:-----------------:|:-----------:|:-----------------:|
| ![main](docs/main-screen.png) | ![numpad](docs/numpad-screen.png) | ![more](docs/more-screen.png) |

Die Wear-OS-App ist für runde Uhrendisplays optimiert. Durch Wischen nach links oder rechts wechselst du zwischen Hauptfernbedienung, Ziffernfeld und weiteren Funktionen.

---

## Erste Einrichtung & Pairing

Beide Apps durchlaufen beim ersten Start denselben Einrichtungsablauf.

### 1. Fernseher suchen

| Smartphone | Uhr |
|:----------:|:---:|
| ![scan-mobile](docs/scan-lan-mobile.png) | ![scan-wear](docs/scan-lan.png) |

Tippe auf **LAN scannen**, um das lokale Netzwerk nach kompatiblen Fernsehern zu durchsuchen.  
Die App erkennt, prüft und listet gefundene Geräte auf.  
Sobald dein Fernseher erscheint, antippen.

### 2. Fernseher auswählen

Nach dem Antippen startet der Kopplungsvorgang automatisch.  
Auf dem Fernseher kann ein Hinweis erscheinen, dass ein neues Gerät verbunden werden möchte.

### 3. Pairing-Code am Fernseher ablesen

Der Fernseher zeigt einen **mehrstelligen Code** auf dem Bildschirm an.  
Dieser Code ist nur kurz gültig – am besten schon bereithalten, bevor du weitermachst.

### 4. Code eingeben

| Smartphone | Uhr |
|:----------:|:---:|
| ![pair-mobile](docs/pair-mobile.png) | ![pair-wear](docs/pair.png) |

Ein **Ziffernfeld** erscheint in der App.

- Ziffern des angezeigten Codes antippen
- Mit **⌫** Fehleingaben korrigieren
- Mit **OK** bestätigen
- Mit **✕** abbrechen und zurück zur TV-Auswahl

Den Code möglichst zügig eingeben – er läuft nach kurzer Zeit ab.

### 5. Fertig

War der Code korrekt, ist der Fernseher jetzt **gekoppelt**.  
Die App wechselt direkt zur Fernbedienungsansicht.  
Das Pairing muss **bei späteren Starts nicht wiederholt** werden.

---

## Wenn das Pairing nicht klappt

**Smartphone/Uhr und Fernseher nicht im selben Netzwerk?**  
Die häufigste Ursache. Sicherstellen, dass beide Geräte dasselbe WLAN nutzen.

**Fernseher nicht vollständig eingeschaltet?**  
Der TV muss vollständig gestartet sein, kein unklarer Standby-Zustand.

**Falschen Code eingegeben?**  
Schon eine falsche Ziffer verhindert die Kopplung. Vorgang neu starten und Code sorgfältig eingeben.

**Code abgelaufen?**  
Wenn die Eingabe zu lange dauert, wird der Code ungültig. Einfach das Pairing neu starten.

**Noch immer kein Erfolg?**  
Im Setup-Bereich **TV entfernen** tippen, erneut scannen und das Pairing von vorne beginnen.

---

## Lizenz

[`MIT`](./LICENSE)
