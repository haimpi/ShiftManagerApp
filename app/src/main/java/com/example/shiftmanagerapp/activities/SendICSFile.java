package com.example.shiftmanagerapp.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.core.content.FileProvider;
import java.io.File;

public class SendICSFile {

    public static void sendEmailWithICS(Context context, File icsFile, String[] recipients) {
        Uri uri = FileProvider.getUriForFile(context, "com.example.myapplication.fileprovider", icsFile);

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/calendar");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, recipients);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "New Work Shift");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Please add this work shift to your calendar.");
        emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        context.startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }
}
