package com.greaterjasol.complaints.data;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenStore {
    private SharedPreferences prefs;
    public TokenStore(Context ctx) { prefs = ctx.getSharedPreferences("auth", Context.MODE_PRIVATE); }
    public void saveToken(String token) { prefs.edit().putString("jwt", token).apply(); }
    public String getToken() { return prefs.getString("jwt", null); }
    public void clear() { prefs.edit().remove("jwt").apply(); }
}
