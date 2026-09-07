package com.greaterjasol.complaints.util;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UploadUtils {
    private static OkHttpClient client = new OkHttpClient();

    public static boolean uploadToPresignedUrl(String uploadUrl, byte[] bytes, String mime) throws Exception {
        RequestBody body = RequestBody.create(bytes, MediaType.parse(mime));
        Request req = new Request.Builder().url(uploadUrl).put(body).build();
        try (Response resp = client.newCall(req).execute()) {
            return resp.isSuccessful();
        }
    }
}
