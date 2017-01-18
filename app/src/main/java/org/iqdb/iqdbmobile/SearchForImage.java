package org.iqdb.iqdbmobile;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewDebug;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public class SearchForImage extends AppCompatActivity {

    private WebView responseView;

    public interface SendImage {
        @Multipart
        @POST("/")
        Call<ResponseBody> upload(@Part("MAX_FILE_SIZE") int MAX_FILE_SIZE,
                                  @Part("service[]") List<Integer> SERVICE,
                                  @Part MultipartBody.Part FILE);
    }

    private void uploadFile(String filePath) {
        SendImage service = ServiceGenerator.createService(SendImage.class);

        File file = new File(filePath);
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part requestFileBody = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        List<Integer> serviceList = new ArrayList<Integer>();
        serviceList.addAll(Arrays.asList(1,2,3,4,5,6,10,11,12,13));

        int MAX_FILE_SIZE = 8388608;

        Call<ResponseBody> call = service.upload(MAX_FILE_SIZE, serviceList, requestFileBody);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
//                    Now parse the HTML response here
                    String textResponse = response.body().string();
                    textResponse = textResponse.replace("\"//","\"http://");
                    textResponse = textResponse.replace("<img src='","<img src='http://iqdb.org");
                    textResponse = textResponse.replace("<a href='/'>Main page</a>","<a href='http://iqdb.org'>Main page</a>");
                    textResponse = textResponse.replace("×","x");
                    responseView = (WebView) findViewById(R.id.responseView);
                    responseView.setWebViewClient(new WebViewClient());
                    responseView.loadData(textResponse, "text/html", null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_for_image);

        Intent incomingRequest = getIntent();
//        String action = incomingRequest.getAction();
//        action doesn't matter, and type is always img/* (check before sending)
        String imageType = incomingRequest.getType();
        Uri imageUri = (Uri) incomingRequest.getParcelableExtra(Intent.EXTRA_STREAM);

        String fileName = "/cacheImg.";
        if (imageType.equalsIgnoreCase("image/png")){
            fileName += "png";
        } else if (imageType.equalsIgnoreCase("image/jpeg")) {
            fileName += "jpg";
        } else {
            fileName += "dat";
        }
        String filePath = getCacheDir() + fileName;

        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            File tempFile = new File(filePath);
            OutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[4 * 1024];
            int tmpRead;
            while ((tmpRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, tmpRead);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        uploadFile(filePath);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && responseView.canGoBack()) {
            responseView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
