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
*Entwickelt für eine einfache und transparente Abrechnung im Ehrenamt.*
