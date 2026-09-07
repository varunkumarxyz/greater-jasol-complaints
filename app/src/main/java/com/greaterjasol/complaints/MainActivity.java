package com.greaterjasol.complaints;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.greaterjasol.complaints.data.Repository;
import com.greaterjasol.complaints.data.TokenStore;
import com.greaterjasol.complaints.network.NetworkModule;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private Spinner wardSpinner;
    private EditText titleInput, descriptionInput;
    private Button pickPhotosBtn, submitBtn;
    private LinearLayout imagesContainer;
    private TextView resultText;

    private List<Uri> selectedUris = new ArrayList<>();
    private ActivityResultLauncher<String> pickLauncher;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private TokenStore tokenStore;
    private Repository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tokenStore = new TokenStore(getApplicationContext());
        NetworkModule.setTokenProvider(new NetworkModule.TokenProvider() {
            @Override
            public String getToken() {
                return tokenStore.getToken();
            }
        });
        repository = new Repository(getApplicationContext());

        wardSpinner = findViewById(R.id.wardSpinner);
        titleInput = findViewById(R.id.titleInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        pickPhotosBtn = findViewById(R.id.pickPhotosBtn);
        submitBtn = findViewById(R.id.submitBtn);
        imagesContainer = findViewById(R.id.imagesContainer);
        resultText = findViewById(R.id.resultText);

        // Populate wards 1..20
        List<String> wards = new ArrayList<>();
        for (int i = 1; i <= 20; i++) wards.add("Ward " + i);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, wards);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wardSpinner.setAdapter(adapter);

        pickLauncher = registerForActivityResult(new ActivityResultContracts.GetMultipleContents(),
                new ActivityResultCallback<List<Uri>>() {
                    @Override
                    public void onActivityResult(List<Uri> result) {
                        if (result != null) {
                            selectedUris.addAll(result);
                            displaySelectedImages();
                        }
                    }
                });

        pickPhotosBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickLauncher.launch("image/*");
            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitComplaint();
            }
        });
    }

    private void displaySelectedImages() {
        imagesContainer.removeAllViews();
        for (Uri uri : selectedUris) {
            ImageView iv = new ImageView(this);
            int size = (int) (96 * getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
            lp.setMargins(8, 8, 8, 8);
            iv.setLayoutParams(lp);
            Picasso.get().load(uri).resize(size, size).centerCrop().into(iv);
            imagesContainer.addView(iv);
        }
    }

    private void submitComplaint() {
        final int ward = wardSpinner.getSelectedItemPosition() + 1;
        final String title = titleInput.getText().toString().trim();
        final String desc = descriptionInput.getText().toString().trim();

        if (title.isEmpty()) {
            resultText.setText("Please enter a title");
            return;
        }

        submitBtn.setEnabled(false);
        resultText.setText("Uploading...");

        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    List<String> uploadedUrls = new ArrayList<>();
                    for (Uri uri : selectedUris) {
                        String url = repository.uploadImageAndGetUrl(uri);
                        if (url != null) uploadedUrls.add(url);
                    }
                    // Create complaint
                    String complaintId = repository.createComplaint(ward, title, desc, uploadedUrls);
                    final String res = complaintId != null ? "Submitted: " + complaintId : "Submission failed";
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            resultText.setText(res);
                            submitBtn.setEnabled(true);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            resultText.setText("Error: " + e.getMessage());
                            submitBtn.setEnabled(true);
                        }
                    });
                }
            }
        });
    }
}
