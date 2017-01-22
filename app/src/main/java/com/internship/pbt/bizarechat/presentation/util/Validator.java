package com.internship.pbt.bizarechat.presentation.util;


import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.IOException;

public class Validator {

    private final String PASSWORD_REGEX = "(?=(.*\\d){2})(?=(.*[a-z]))(?=(.*[A-Z]){2}).*";
    private static final String TAG = "Validator";
    private static final int SIX = 6;
    private static final int TWELVE = 12;
    private final String EMAIL_REGEX = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private final String PHONE_REGEX = "(\\+[0-9]+[\\- \\.]*)?"
            + "(\\([0-9]+\\)[\\- \\.]*)?"
            + "([0-9][0-9\\- \\.]+[0-9])";

    public boolean isValidEmail(String email) {
        return email.matches(EMAIL_REGEX);
    }

    public boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.matches(PHONE_REGEX);
    }

    public boolean isValidPassword(String password) {
        return password.matches(PASSWORD_REGEX);
    }

    public boolean isPasswordLengthMatches(String password) {
        return password.length() >= SIX && password.length() <= TWELVE;
    }

    public boolean isPasswordMatch (String password, String confirmPsw){
        return password.equals(confirmPsw);
    }

    public boolean isValidAvatarSize(Context context, Uri uri){
        try {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        if (bitmap.getByteCount() / 10000000 < 1)
            return true;
        else
            return false;

        } catch (IOException ex) {
            Log.d(TAG, ex.getMessage(), ex);
            return false;
        }

    }
}