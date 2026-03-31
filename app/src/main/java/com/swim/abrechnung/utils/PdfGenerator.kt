package com.swim.abrechnung.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
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
    private const val RATE_WETTKAMPF = 1.0
    private const val RATE_TRAINER = 2.0
    private const val KM_RATE = 0.20

    fun generateAndSendPdf(
        context: Context,
        profile: UserProfile,
        entries: List<Entry>,
        quarter: Int,
        year: Int
    ) {
        val pdfDocument = PdfDocument()
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN)
        
        val wettkampfEntries = entries.filter { it.category == "Wettkampf" }.sortedBy { it.date }
        val trainerEntries = entries.filter { it.category == "Trainer" }.sortedBy { it.date }

        if (wettkampfEntries.isNotEmpty()) {
            drawWettkampfPage(pdfDocument, profile, wettkampfEntries, quarter, year, sdf)
        }

        if (trainerEntries.isNotEmpty()) {
            drawTrainerPage(pdfDocument, profile, trainerEntries, quarter, year, sdf)
        }

        val fileName = "Abrechnung_Q${quarter}_$year.pdf"
        val file = File(context.cacheDir, fileName)
        try {
            pdfDocument.writeTo(FileOutputStream(file))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        pdfDocument.close()

        sendEmail(context, file, profile, quarter, year)
    }

    private fun drawWettkampfPage(
        doc: PdfDocument,
        profile: UserProfile,
        entries: List<Entry>,
        quarter: Int,
        year: Int,
        sdf: SimpleDateFormat
    ) {
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()
        var y = 40f

        paint.textSize = 16f
        paint.isFakeBoldText = true
        canvas.drawText("Abrechnung Wettkampf (Kampfrichter/Betreuer) Q$quarter $year", 40f, y, paint)
        y += 40f

        drawProfileInfo(canvas, paint, profile, y)
        y += 100f

        paint.textSize = 9f
        paint.isFakeBoldText = true
        val cols = floatArrayOf(40f, 95f, 165f, 335f, 465f)
        canvas.drawText("Datum", cols[0], y, paint)
        canvas.drawText("Uhrzeit", cols[1], y, paint)
        canvas.drawText("Wettkampf", cols[2], y, paint)
        canvas.drawText("Betreuer (h/km)", cols[3], y, paint)
        canvas.drawText("Kampfr. (h/km)", cols[4], y, paint)
        y += 5f
        canvas.drawLine(40f, y, 565f, y, paint)
        y += 15f

        var totalMinBetreuer = 0
        var totalMinKampf = 0
        var totalKmBetreuer = 0.0
        var totalKmKampf = 0.0

        paint.isFakeBoldText = false
        paint.textSize = 8f
        entries.forEach { entry ->
            canvas.drawText(sdf.format(Date(entry.date)), cols[0], y, paint)
            canvas.drawText("${entry.startTime}-${entry.endTime}", cols[1], y, paint)
            val compName = if (entry.competitionName.length > 30) entry.competitionName.substring(0, 27) + "..." else entry.competitionName
            canvas.drawText(compName, cols[2], y, paint)

            val minutes = TimeUtils.calculateDurationMinutes(entry.startTime, entry.endTime)
            val hStr = String.format(Locale.GERMAN, "%.2f", minutes / 60.0)

            if (entry.isBetreuer) {
                totalMinBetreuer += minutes
                totalKmBetreuer += entry.kilometers
                val kmStr = if (entry.kilometers > 0) " / ${entry.kilometers}km" else ""
                canvas.drawText("$hStr$kmStr", cols[3], y, paint)
            }
            if (entry.isKampfrichter) {
                totalMinKampf += minutes
                val kmVal = if (!entry.isBetreuer) entry.kilometers else 0.0
                totalKmKampf += kmVal
                val kmStr = if (kmVal > 0) " / $kmVal" else ""
                canvas.drawText("$hStr$kmStr", cols[4], y, paint)
            }
            y += 15f
        }

        y += 20f
        paint.isFakeBoldText = true
        canvas.drawLine(40f, y, 565f, y, paint)
        y += 15f
        
        val hBetreuer = totalMinBetreuer / 60.0
        val hKampf = totalMinKampf / 60.0
        val sumBetreuer = hBetreuer * RATE_WETTKAMPF
        val sumKampf = hKampf * RATE_WETTKAMPF
        val sumKm = (totalKmBetreuer + totalKmKampf) * KM_RATE

        canvas.drawText("Summe Betreuer: ${String.format(Locale.GERMAN, "%.2f", hBetreuer)} Std. / $totalKmBetreuer km", 40f, y, paint)
        y += 15f
        canvas.drawText("Summe Kampfrichter: ${String.format(Locale.GERMAN, "%.2f", hKampf)} Std. / $totalKmKampf km", 40f, y, paint)
        y += 30f

        paint.textSize = 10f
        canvas.drawText("Abrechnung gemäß Finanzordnung:", 40f, y, paint)
        y += 20f
        paint.isFakeBoldText = false
        canvas.drawText("Vergütung Betreuer: ${String.format(Locale.GERMAN, "%.2f", sumBetreuer)} € (Übungsleiterfreibetrag)", 40f, y, paint)
        y += 15f
        canvas.drawText("Vergütung Kampfrichter: ${String.format(Locale.GERMAN, "%.2f", sumKampf)} € (Ehrenamtsfreibetrag)", 40f, y, paint)
        y += 15f
        canvas.drawText("Erstattung Fahrtkosten Gesamt: ${String.format(Locale.GERMAN, "%.2f", sumKm)} €", 40f, y, paint)
        y += 25f
        
        paint.isFakeBoldText = true
        canvas.drawText("Gesamtbetrag Überweisung: ${String.format(Locale.GERMAN, "%.2f", sumBetreuer + sumKampf + sumKm)} €", 40f, y, paint)
        y += 30f

        paint.textSize = 8f
        paint.isFakeBoldText = false
        val disclaimer = "Ich bestätige, dass durch diese Abrechnung mein Übungsleiterfreibetrag (aktuell 3300€ p.a.)\nund mein Ehrenamtsfreibetrag (aktuell 960€ p.a) nicht überschritten werden."
        disclaimer.split("\n").forEach { line ->
            canvas.drawText(line, 40f, y, paint)
            y += 12f
        }

        y += 40f
        drawSignatureArea(canvas, paint, profile, y, "Unterschrift Betreuer / Kampfrichter")

        doc.finishPage(page)
    }

    private fun drawTrainerPage(
        doc: PdfDocument,
        profile: UserProfile,
        entries: List<Entry>,
        quarter: Int,
        year: Int,
        sdf: SimpleDateFormat
    ) {
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 2).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()
        var y = 40f

        paint.textSize = 16f
        paint.isFakeBoldText = true
        canvas.drawText("Abrechnung Trainerstunden Q$quarter $year", 40f, y, paint)
        y += 40f

        drawProfileInfo(canvas, paint, profile, y)
        y += 100f

        paint.textSize = 9f
        paint.isFakeBoldText = true
        canvas.drawText("Datum", 40f, y, paint)
        canvas.drawText("Ort / Trainingseinheit", 140f, y, paint)
        canvas.drawText("Stunden", 460f, y, paint)
        y += 5f
        canvas.drawLine(40f, y, 565f, y, paint)
        y += 15f

        var totalHours = 0.0
        paint.isFakeBoldText = false
        entries.forEach { entry ->
            canvas.drawText(sdf.format(Date(entry.date)), 40f, y, paint)
            val loc = if (entry.locationOrRoute.length > 50) entry.locationOrRoute.substring(0, 47) + "..." else entry.locationOrRoute
            canvas.drawText(loc, 140f, y, paint)
            canvas.drawText("${entry.value}", 460f, y, paint)
            totalHours += entry.value
            y += 15f
        }

        y += 20f
        paint.isFakeBoldText = true
        canvas.drawLine(40f, y, 565f, y, paint)
        y += 20f
        canvas.drawText("Gesamtstunden: $totalHours Std.", 40f, y, paint)
        y += 20f
        val totalSum = totalHours * RATE_TRAINER
        canvas.drawText("Gesamtbetrag Überweisung: ${String.format(Locale.GERMAN, "%.2f", totalSum)} €", 40f, y, paint)
        
        y += 40f
        paint.textSize = 8f
        paint.isFakeBoldText = false
        canvas.drawText("Ich bestätige, dass durch diese Abrechnung mein Übungsleiterfreibetrag (aktuell 3300€ p.a.) nicht überschritten wird.", 40f, y, paint)

        y += 40f
        drawSignatureArea(canvas, paint, profile, y, "Unterschrift Trainer")

        doc.finishPage(page)
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
