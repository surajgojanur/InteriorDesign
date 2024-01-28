package com.example.interiordesign;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity2 extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PICK_IMAGE = 2;

    private ImageView imageView;
    private GenerativeModelFutures model;
    private TextView summaryTextView; // Added TextView for displaying the generated summary

    String apiKey = "AIzaSyDtkotAm_4SZTW_In0-uYoIbkkbvzxdYCk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        imageView = findViewById(R.id.imageView);
        Button captureButton = findViewById(R.id.captureButton);
        Button generateButton = findViewById(R.id.generateButton);
        Button captureCameraButton = findViewById(R.id.captureCameraButton); // Added new button
        // Added new button
        summaryTextView = findViewById(R.id.summaryTextView); // Initialize TextView

        Executor executor = Executors.newSingleThreadExecutor();

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        model = GenerativeModelFutures.from(new GenerativeModel("gemini-pro-vision", apiKey));

        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateSummary();
            }
        });

        captureCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntentCamera();
            }
        });


    }

    private void dispatchTakePictureIntent() {
        // Create an Intent to show options for capturing or selecting an image
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");

        // Create a chooser to let the user choose between camera and gallery
        Intent chooserIntent = Intent.createChooser(intent, "Select Image");

        // Check if there is a camera app available
        if (intent.resolveActivity(getPackageManager()) != null) {
            // If yes, show the chooser
            startActivityForResult(chooserIntent, REQUEST_PICK_IMAGE);
        } else {
            // If no camera app, show an error message or handle accordingly
            // You might want to inform the user that there is no camera app available
        }
    }

    private void dispatchTakePictureIntentCamera() {
        // Create an Intent to capture an image using the camera
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Check if there is a camera app available
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // If yes, start the camera activity
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            // If no camera app, show an error message or handle accordingly
            // You might want to inform the user that there is no camera app available
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PICK_IMAGE) {
                // User selected an image from the gallery
                if (data != null && data.getData() != null) {
                    Uri selectedImageUri = data.getData();
                    imageView.setImageURI(selectedImageUri);
                }
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // User captured an image using the camera
                if (data != null && data.getExtras() != null) {
                    Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                    imageView.setImageBitmap(imageBitmap);
                }
            }
        }
    }

    private void generateSummary() {
        Bitmap image = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        Content content = new Content.Builder()
                .addText("What simple and concise interior design upgrades can be suggested for this space?")
                .addImage(image)
                .build();

        Executor executor = Executors.newSingleThreadExecutor();

        CompletableFuture.supplyAsync(() -> {
            try {
                return model.generateContent(content).get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executor).thenAcceptAsync(result -> {
            String resultText = result.getText();

            // Log the generated summary text
            System.out.println("Generated Summary: " + resultText);

            // Update the UI with the generated summary text
            runOnUiThread(() -> summaryTextView.setText(resultText));
        }, executor).exceptionally(e -> {
            e.printStackTrace();
            // Handle failure, e.g., display an error message to the user
            return null;
        });

        // You might want to provide user feedback here, like showing a loading spinner
    }
}
