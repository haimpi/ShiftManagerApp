package com.example.shiftmanagerapp.activities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.content.Context;

public class CreateICSFile {

    public static File createICSFile(Context context, String title, String description, String location, long startTime, long endTime) {
        String eventDetails = "BEGIN:VCALENDAR\n" +
                "VERSION:2.0\n" +
                "CALSCALE:GREGORIAN\n" +
                "BEGIN:VEVENT\n" +
                "DTSTART:" + convertToICSFormat(startTime) + "\n" +
                "DTEND:" + convertToICSFormat(endTime) + "\n" +
                "SUMMARY:" + title + "\n" +
                "DESCRIPTION:" + description + "\n" +
                "LOCATION:" + location + "\n" +
                "END:VEVENT\n" +
                "END:VCALENDAR";

        File file = new File(context.getExternalFilesDir(null), "work_shift.ics");

        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(eventDetails.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    private static String convertToICSFormat(long timeInMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date(timeInMillis));
    }
}