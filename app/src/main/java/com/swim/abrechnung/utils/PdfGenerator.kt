package com.swim.abrechnung.utils

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.util.Base64
import androidx.core.content.FileProvider
import com.swim.abrechnung.data.Entry
import com.swim.abrechnung.data.UserProfile
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfGenerator {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN_BOTTOM = 50f
    private const val ROW_HEIGHT = 15f

    /** Hoehe von Summen-, Bestaetigungs- und Unterschriftsblock nach der Tabelle. */
    private const val WETTKAMPF_FOOTER_HEIGHT = 320f
    private const val TRAINER_FOOTER_HEIGHT = 230f

    /**
     * Haelt die aktuelle PDF-Seite und erzeugt bei Bedarf Folgeseiten.
     */
    private class PageWriter(private val doc: PdfDocument) {
        private var pageNumber = 0
        private var page: PdfDocument.Page? = null
        lateinit var canvas: Canvas
            private set
        var y = 0f

        fun newPage() {
            page?.let { doc.finishPage(it) }
            pageNumber++
            val info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            val p = doc.startPage(info)
            page = p
            canvas = p.canvas
            y = 40f
        }

        /** True, wenn noch [height] Punkte Platz bis zum unteren Rand sind. */
        fun fits(height: Float) = y + height <= PAGE_HEIGHT - MARGIN_BOTTOM

        fun finish() {
            page?.let { doc.finishPage(it) }
            page = null
        }
    }

    private fun fmt(value: Double) = String.format(Locale.GERMAN, "%.2f", value)

    fun generateAndSendPdf(
        context: Context,
        profile: UserProfile,
        entries: List<Entry>,
        quarter: Int,
        year: Int,
        onGenerated: (Long) -> Unit = {}
    ) {
        val pdfDocument = PdfDocument()
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN)
        val writer = PageWriter(pdfDocument)

        val wettkampfEntries = entries.filter { it.category == "Wettkampf" }.sortedBy { it.date }
        val trainerEntries = entries.filter { it.category == "Trainer" }.sortedBy { it.date }

        if (wettkampfEntries.isNotEmpty()) {
            drawWettkampfSection(writer, profile, wettkampfEntries, quarter, year, sdf)
        }

        if (trainerEntries.isNotEmpty()) {
            drawTrainerSection(writer, profile, trainerEntries, quarter, year, sdf)
        }
        writer.finish()

        val fileName = "Abrechnung_Q${quarter}_$year.pdf"
        val file = File(context.cacheDir, fileName)
        try {
            FileOutputStream(file).use { pdfDocument.writeTo(it) }
            onGenerated(System.currentTimeMillis())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        pdfDocument.close()

        sendEmail(context, file, profile, quarter, year)
    }

    private fun drawWettkampfSection(
        w: PageWriter,
        profile: UserProfile,
        entries: List<Entry>,
        quarter: Int,
        year: Int,
        sdf: SimpleDateFormat
    ) {
        val paint = Paint()
        val title = "Abrechnung Wettkampf (Kampfrichter/Betreuer) Q$quarter $year"
        val cols = floatArrayOf(40f, 95f, 165f, 335f, 465f)

        fun drawTableHeader() {
            paint.textSize = 9f
            paint.isFakeBoldText = true
            w.canvas.drawText("Datum", cols[0], w.y, paint)
            w.canvas.drawText("Uhrzeit", cols[1], w.y, paint)
            w.canvas.drawText("Wettkampf", cols[2], w.y, paint)
            w.canvas.drawText("Betreuer (h/km)", cols[3], w.y, paint)
            w.canvas.drawText("Kampfr. (h/km)", cols[4], w.y, paint)
            w.y += 5f
            w.canvas.drawLine(40f, w.y, 565f, w.y, paint)
            w.y += 15f
            paint.isFakeBoldText = false
            paint.textSize = 8f
        }

        fun continuationPage() {
            w.newPage()
            paint.textSize = 12f
            paint.isFakeBoldText = true
            w.canvas.drawText("$title (Fortsetzung) – ${profile.fullName}", 40f, w.y, paint)
            w.y += 30f
        }

        w.newPage()
        paint.textSize = 16f
        paint.isFakeBoldText = true
        w.canvas.drawText(title, 40f, w.y, paint)
        w.y += 40f

        drawProfileInfo(w.canvas, paint, profile, w.y)
        w.y += 100f

        drawTableHeader()

        var totalMinBetreuer = 0
        var totalMinKampf = 0
        var totalKmBetreuer = 0.0
        var totalKmKampf = 0.0

        entries.forEach { entry ->
            if (!w.fits(ROW_HEIGHT)) {
                continuationPage()
                drawTableHeader()
            }
            w.canvas.drawText(sdf.format(Date(entry.date)), cols[0], w.y, paint)
            w.canvas.drawText("${entry.startTime}-${entry.endTime}", cols[1], w.y, paint)
            val compName = if (entry.competitionName.length > 30) entry.competitionName.substring(0, 27) + "..." else entry.competitionName
            w.canvas.drawText(compName, cols[2], w.y, paint)

            val minutes = TimeUtils.calculateDurationMinutes(entry.startTime, entry.endTime)
            val hStr = fmt(minutes / 60.0)

            if (entry.isBetreuer) {
                totalMinBetreuer += minutes
                totalKmBetreuer += entry.kilometers
                val kmStr = if (entry.kilometers > 0) " / ${TimeUtils.formatNumber(entry.kilometers)} km" else ""
                w.canvas.drawText("$hStr$kmStr", cols[3], w.y, paint)
            }
            if (entry.isKampfrichter) {
                totalMinKampf += minutes
                val kmVal = if (!entry.isBetreuer) entry.kilometers else 0.0
                totalKmKampf += kmVal
                val kmStr = if (kmVal > 0) " / ${TimeUtils.formatNumber(kmVal)} km" else ""
                w.canvas.drawText("$hStr$kmStr", cols[4], w.y, paint)
            }
            w.y += ROW_HEIGHT
        }

        // Summen, Bestaetigung und Unterschrift bleiben zusammen auf einer Seite
        if (!w.fits(WETTKAMPF_FOOTER_HEIGHT)) {
            continuationPage()
        }

        w.y += 20f
        paint.isFakeBoldText = true
        paint.textSize = 8f
        w.canvas.drawLine(40f, w.y, 565f, w.y, paint)
        w.y += 15f

        val hBetreuer = totalMinBetreuer / 60.0
        val hKampf = totalMinKampf / 60.0
        val sumBetreuer = hBetreuer * profile.rateBetreuer
        val sumKampf = hKampf * profile.rateKampfrichter
        val sumKm = (totalKmBetreuer + totalKmKampf) * profile.rateKm

        w.canvas.drawText("Summe Betreuer: ${fmt(hBetreuer)} Std. / ${TimeUtils.formatNumber(totalKmBetreuer)} km", 40f, w.y, paint)
        w.y += 15f
        w.canvas.drawText("Summe Kampfrichter: ${fmt(hKampf)} Std. / ${TimeUtils.formatNumber(totalKmKampf)} km", 40f, w.y, paint)
        w.y += 30f

        paint.textSize = 10f
        w.canvas.drawText("Abrechnung gemäß Finanzordnung:", 40f, w.y, paint)
        w.y += 20f
        paint.isFakeBoldText = false
        w.canvas.drawText("Vergütung Betreuer: ${fmt(sumBetreuer)} € (Satz: ${fmt(profile.rateBetreuer)} €/h)", 40f, w.y, paint)
        w.y += 15f
        w.canvas.drawText("Vergütung Kampfrichter: ${fmt(sumKampf)} € (Satz: ${fmt(profile.rateKampfrichter)} €/h)", 40f, w.y, paint)
        w.y += 15f
        w.canvas.drawText("Erstattung Fahrtkosten Gesamt: ${fmt(sumKm)} € (Satz: ${fmt(profile.rateKm)} €/km)", 40f, w.y, paint)
        w.y += 25f

        paint.isFakeBoldText = true
        w.canvas.drawText("Gesamtbetrag Überweisung: ${fmt(sumBetreuer + sumKampf + sumKm)} €", 40f, w.y, paint)
        w.y += 30f

        paint.textSize = 8f
        paint.isFakeBoldText = false
        val disclaimer = "Ich bestätige, dass durch diese Abrechnung mein Übungsleiterfreibetrag (aktuell 3300€ p.a.)\nund mein Ehrenamtsfreibetrag (aktuell 960€ p.a) nicht überschritten werden."
        disclaimer.split("\n").forEach { line ->
            w.canvas.drawText(line, 40f, w.y, paint)
            w.y += 12f
        }

        w.y += 40f
        drawSignatureArea(w.canvas, paint, profile, w.y, "Unterschrift Betreuer / Kampfrichter")
    }

    private fun drawTrainerSection(
        w: PageWriter,
        profile: UserProfile,
        entries: List<Entry>,
        quarter: Int,
        year: Int,
        sdf: SimpleDateFormat
    ) {
        val paint = Paint()
        val title = "Abrechnung Trainerstunden Q$quarter $year"

        fun drawTableHeader() {
            paint.textSize = 9f
            paint.isFakeBoldText = true
            w.canvas.drawText("Datum", 40f, w.y, paint)
            w.canvas.drawText("Ort / Trainingseinheit", 140f, w.y, paint)
            w.canvas.drawText("Stunden", 460f, w.y, paint)
            w.y += 5f
            w.canvas.drawLine(40f, w.y, 565f, w.y, paint)
            w.y += 15f
            paint.isFakeBoldText = false
        }

        fun continuationPage() {
            w.newPage()
            paint.textSize = 12f
            paint.isFakeBoldText = true
            w.canvas.drawText("$title (Fortsetzung) – ${profile.fullName}", 40f, w.y, paint)
            w.y += 30f
        }

        w.newPage()
        paint.textSize = 16f
        paint.isFakeBoldText = true
        w.canvas.drawText(title, 40f, w.y, paint)
        w.y += 40f

        drawProfileInfo(w.canvas, paint, profile, w.y)
        w.y += 100f

        drawTableHeader()

        var totalHours = 0.0
        entries.forEach { entry ->
            if (!w.fits(ROW_HEIGHT)) {
                continuationPage()
                drawTableHeader()
            }
            w.canvas.drawText(sdf.format(Date(entry.date)), 40f, w.y, paint)
            val loc = if (entry.locationOrRoute.length > 50) entry.locationOrRoute.substring(0, 47) + "..." else entry.locationOrRoute
            w.canvas.drawText(loc, 140f, w.y, paint)
            w.canvas.drawText(TimeUtils.formatNumber(entry.value), 460f, w.y, paint)
            totalHours += entry.value
            w.y += ROW_HEIGHT
        }

        if (!w.fits(TRAINER_FOOTER_HEIGHT)) {
            continuationPage()
        }

        w.y += 20f
        paint.isFakeBoldText = true
        paint.textSize = 9f
        w.canvas.drawLine(40f, w.y, 565f, w.y, paint)
        w.y += 20f
        w.canvas.drawText("Gesamtstunden: ${TimeUtils.formatNumber(totalHours)} Std.", 40f, w.y, paint)
        w.y += 15f
        w.canvas.drawText("Trainerstatus: ${profile.trainerStatus} (Satz: ${fmt(profile.rateTrainer)} €/h)", 40f, w.y, paint)
        w.y += 20f
        val totalSum = totalHours * profile.rateTrainer
        w.canvas.drawText("Gesamtbetrag Überweisung: ${fmt(totalSum)} €", 40f, w.y, paint)

        w.y += 40f
        paint.textSize = 8f
        paint.isFakeBoldText = false
        w.canvas.drawText("Ich bestätige, dass durch diese Abrechnung mein Übungsleiterfreibetrag (aktuell 3300€ p.a.) nicht überschritten wird.", 40f, w.y, paint)

        w.y += 40f
        drawSignatureArea(w.canvas, paint, profile, w.y, "Unterschrift Trainer")
    }

    private fun drawSignatureArea(canvas: Canvas, paint: Paint, profile: UserProfile, y: Float, label: String) {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN)
        val currentY = y + 45f

        paint.textSize = 8f
        paint.isFakeBoldText = false

        canvas.drawText("Datum: ${sdf.format(Date())}", 40f, currentY, paint)

        profile.signatureBase64?.let { base64 ->
            try {
                val decodedString = Base64.decode(base64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                if (bitmap != null) {
                    val targetWidth = 140f
                    val aspectRatio = bitmap.height.toFloat() / bitmap.width.toFloat()
                    val targetHeight = targetWidth * aspectRatio
                    val destRect = RectF(180f, currentY - targetHeight - 5f, 180f + targetWidth, currentY - 5f)
                    val bitmapPaint = Paint().apply {
                        isAntiAlias = true
                        isFilterBitmap = true
                        isDither = true
                    }
                    canvas.drawBitmap(bitmap, null, destRect, bitmapPaint)
                }
            } catch (e: Exception) { e.printStackTrace() }
        }

        canvas.drawLine(180f, currentY, 320f, currentY, paint)
        canvas.drawText(label, 180f, currentY + 12f, paint)

        canvas.drawLine(400f, currentY, 540f, currentY, paint)
        canvas.drawText("Unterschrift Abteilungsleiter", 400f, currentY + 12f, paint)
    }

    private fun drawProfileInfo(canvas: Canvas, paint: Paint, profile: UserProfile, startY: Float) {
        var localY = startY
        paint.textSize = 11f
        paint.isFakeBoldText = false
        canvas.drawText("Name: ${profile.fullName}", 40f, localY, paint)
        localY += 15f
        canvas.drawText("Anschrift: ${profile.street} ${profile.houseNumber}, ${profile.zip} ${profile.city}", 40f, localY, paint)
        localY += 15f
        canvas.drawText("IBAN: ${profile.iban}", 40f, localY, paint)
        localY += 15f
        canvas.drawText("Bank: ${profile.bankName}", 40f, localY, paint)
    }

    private fun sendEmail(context: Context, file: File, profile: UserProfile, quarter: Int, year: Int) {
        val uri = FileProvider.getUriForFile(context, "com.swim.abrechnung.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_SUBJECT, "Abrechnung Q$quarter $year – ${profile.fullName}")
            putExtra(Intent.EXTRA_TEXT, "Hallo,\n\nanbei meine Abrechnung für Q$quarter $year.\n\nMit freundlichen Grüßen,\n${profile.fullName}")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Abrechnung senden..."))
    }
}
