package com.example.stars

import android.os.Environment
import java.io.File
import java.io.OutputStream


var path = Environment.getExternalStoragePublicDirectory(
    Environment.DIRECTORY_DOWNLOADS
)
var file = File(path, "example.csv")

fun OutputStream.writeReservationsToCsv(reservations: List<Reservation>) {
    val writer = bufferedWriter()
    writer.write("\"Start\", \"End\", \"Campsite\", \"CampsiteID\"")
    writer.newLine()

    reservations.forEach {
        writer.write("${it.timeRange.start}, ${it.timeRange.end}, \"${"Blank"}\", \"${it.campsiteId}\"")
        writer.newLine()
    }
    writer.flush()
}

fun OutputStream.writeReservationToCsv(reservation: Reservation) {
    val writer = bufferedWriter()
    writer.write("\"Start\", \"End\", \"Campsite\", \"CampsiteID\"")
    writer.newLine()

    writer.write("${reservation.timeRange.start}, ${reservation.timeRange.end}, \"${"Blank"}\", \"${reservation.campsiteId}\"")
    writer.newLine()
    writer.flush()
}

FileOutputStream(file)writeReservationToCsv(movies)