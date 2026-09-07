package com.greaterjasol.complaints.data;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.greaterjasol.complaints.network.ApiService;
import com.greaterjasol.complaints.network.Models;
import com.greaterjasol.complaints.network.NetworkModule;
import com.greaterjasol.complaints.util.UploadUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class Repository {
    private Context context;
    private TokenStore tokenStore;
    private ApiService api;

    public Repository(Context ctx) {
        this.context = ctx;
        this.tokenStore = new TokenStore(ctx);
        NetworkModule.setTokenProvider(new NetworkModule.TokenProvider() {
            @Override
            public String getToken() { return tokenStore.getToken(); }
        });
        this.api = NetworkModule.api;
    }

    // Upload image: presign + PUT + return fileUrl
    public String uploadImageAndGetUrl(Uri uri) {
        try {
            String filename = uri.getLastPathSegment();
            String mime = context.getContentResolver().getType(uri);
            if (mime == null) mime = "image/jpeg";
            Call<Models.PresignResponse> presignCall = api.presign(filename, mime);
            Response<Models.PresignResponse> presignResp = presignCall.execute();
            if (!presignResp.isSuccessful() || presignResp.body() == null) return null;
            Models.PresignResponse presign = presignResp.body();

            InputStream is = context.getContentResolver().openInputStream(uri);
            byte[] bytes = new byte[is.available()];
            int read = is.read(bytes);
            is.close();

            boolean ok = UploadUtils.uploadToPresignedUrl(presign.uploadUrl, bytes, mime);
            if (!ok) return null;
            return presign.fileUrl;
        } catch (Exception e) {
            Log.e("Repo", "upload error", e);
            return null;
        }
    }

    public String createComplaint(int wardId, String title, String description, List<String> photoUrls) {
        try {
            Models.ComplaintCreate cc = new Models.ComplaintCreate();
            cc.ward_id = wardId; cc.title = title; cc.description = description; cc.photo_urls = photoUrls;
            Call<Models.ComplaintCreated> call = api.createComplaint(cc);
            Response<Models.ComplaintCreated> resp = call.execute();
            if (!resp.isSuccessful() || resp.body() == null) return null;
            return resp.body().id;
        } catch (Exception e) {
            Log.e("Repo", "create complaint error", e);
            return null;
        }
    }
}
