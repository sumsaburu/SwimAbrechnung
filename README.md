# SwimAbrechnung

Eine moderne Android-App zur einfachen Erfassung und Abrechnung von Trainer-, Betreuer- und Kampfrichterstunden sowie Fahrtkosten. Die App ist speziell für Sportvereine konzipiert, um den administrativen Aufwand bei Aufwandsentschädigungen zu minimieren.

## Funktionen

- **Trainer-Abrechnung:** Erfassung von Trainingseinheiten inklusive Ort und Dauer.
- **Wettkampf-Abrechnung:** Detaillierte Erfassung von Einsätzen als Betreuer oder Kampfrichter inklusive Start- und Endzeit sowie automatischer Zeitberechnung.
- **Konfigurierbare Sätze:** Hinterlege deine individuellen Vergütungssätze für Trainer, Betreuer, Kampfrichter und Kilometer direkt in den Einstellungen.
- **Trainerstatus:** Wähle deine Qualifikationsstufe (z.B. Trainer C), welche automatisch auf den Berichten vermerkt wird.
- **Kilometer-Logik:** Automatische Fahrtkostenerfassung mit integrierter Prüfung (nur eine Kilometerpauschale pro Kalendertag zulässig).
- **PDF-Export:** Generierung von professionellen, rechtssicheren Abrechnungsdokumenten als PDF, automatisch aufgeteilt nach Trainer- und Wettkampfeinsätzen.
- **Digitale Unterschrift:** Hinterlege deine Unterschrift einmalig, um sie automatisch auf allen Berichten an der richtigen Stelle einzufügen.
- **Versand-Tracking:** Die App merkt sich, wann du den letzten Bericht für ein Quartal versendet hast.
- **Datenschutz & Offline-First:** Alle Daten werden ausschließlich lokal in einer verschlüsselten Room-Datenbank auf deinem Gerät gespeichert. Keine Cloud, kein Tracking.

## Einrichtung & Nutzung

1. **Profil:** Hinterlege beim ersten Start deinen Namen, Anschrift, IBAN und deine digitale Unterschrift.
2. **Sätze & Status:** Konfiguriere unter "Einstellungen > Sätze & Status" deine aktuellen Vergütungssätze und deine Trainer-Qualifikation.
3. **Erfassung:** Wähle auf dem Startbildschirm zwischen Trainer- und Wettkampf-Abrechnung.
4. **Bericht:** Wähle das gewünschte Jahr/Quartal aus, überprüfe die Summen und erstelle per Klick das fertige PDF für den Versand.

## Technische Details

Die App wurde mit modernen Android-Technologien entwickelt:
- **Sprache:** Kotlin
- **UI:** Jetpack Compose (Material 3 Design)
- **Datenbank:** Room Persistence Library
- **PDF-Engine:** Native Android PdfDocument API
- **Architektur:** MVVM (Model-View-ViewModel)

---

# SwimAbrechnung (English)

A modern Android app for easy recording and billing of trainer, supervisor and judge hours as well as travel costs. The app is specifically designed for sports clubs to minimize the administrative effort for expense allowances.

## Features

- **Coach Billing:** Recording of training sessions including location and duration.
- **Competition Billing:** Detailed recording of assignments as a supervisor or judge including start and end times as well as automatic duration calculation.
- **Configurable Rates:** Store your individual compensation rates for coaches, supervisors, judges and mileage directly in the settings.
- **Coach Status:** Choose your qualification level (e.g. Coach C), which is automatically noted on the reports.
- **Mileage Logic:** Automatic travel cost tracking with integrated validation (only one mileage allowance per calendar day permitted).
- **PDF Export:** Generation of professional, legally compliant PDF documents, automatically separated into coach and competition assignments.
- **Digital Signature:** Store your signature once to automatically insert it in the correct place on all reports.
- **Dispatch Tracking:** The app remembers when you last sent a report for a quarter.
- **Privacy & Offline-First:** All data is stored exclusively locally in an encrypted Room database on your device. No cloud, no tracking.

## Setup & Usage

1. **Profile:** Enter your name, address, IBAN and digital signature on the first start.
2. **Rates & Status:** Configure your current compensation rates and your coaching qualification under "Settings > Rates & Status".
3. **Entry:** Choose between coach or competition billing on the home screen.
4. **Report:** Select the desired year/quarter, check the totals and create the finished PDF for dispatch with one click.

## Technical Details

The app was developed with modern Android technologies:
- **Language:** Kotlin
- **UI:** Jetpack Compose (Material 3 Design)
- **Database:** Room Persistence Library
- **PDF Engine:** Native Android PdfDocument API
- **Architecture:** MVVM (Model-View-ViewModel)

---
*Entwickelt für eine einfache und transparente Abrechnung im Ehrenamt / Developed for simple and transparent volunteer accounting.*
