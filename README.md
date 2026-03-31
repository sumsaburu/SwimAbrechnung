# SwimAbrechnung

Eine moderne Android-App zur einfachen Erfassung und Abrechnung von Trainer-, Betreuer- und Kampfrichterstunden sowie Fahrtkosten.

## Funktionen

- **Trainer-Abrechnung:** Erfassung von Trainingseinheiten, Ort und Dauer.
- **Wettkampf-Abrechnung:** Detaillierte Erfassung von Einsätzen als Betreuer oder Kampfrichter inklusive von/bis Uhrzeiten.
- **Kilometer-Logik:** Automatische Fahrtkostenerfassung mit integrierter Prüfung (nur einmal Kilometer pro Kalendertag zulässig).
- **PDF-Export:** Generierung von professionellen Abrechnungsdokumenten als PDF, fertig aufgeteilt nach Kategorien.
- **Digitale Unterschrift:** Hinterlege deine Unterschrift einmalig in den Einstellungen, um sie automatisch auf allen Berichten einzufügen.
- **Offline-First:** Alle Daten werden sicher und lokal in einer Room-Datenbank auf dem Gerät gespeichert. Keine Cloud-Anbindung erforderlich.

## Abrechnungssätze (Stand: Aktuell)

- **Trainer:** 2,00 € pro Stunde
- **Betreuer / Kampfrichter:** 1,00 € pro Stunde
- **Fahrtkosten:** 0,20 € pro Kilometer

## Installation

1. Lade die aktuelle `app-debug.apk` herunter.
2. Installiere die APK auf deinem Android-Smartphone (unbekannte Quellen müssen ggf. erlaubt werden).
3. Richte beim ersten Start dein Profil (Name, IBAN, Unterschrift) ein.

## Entwicklung

Die App wurde mit **Kotlin** und **Jetpack Compose** entwickelt.

- **Datenbank:** Room
- **Navigation:** Navigation Compose
- **PDF-Erstellung:** Android PdfDocument API
- **Design:** Material 3 (Clean, Minimalistic UI)

---
*Erstellt für die effiziente Verwaltung von Aufwandsentschädigungen.*
