package chat.rocket;

import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import java.io.InputStream;

import java.io.File;
import java.util.ArrayList;

import javax.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class ShareModule extends ReactContextBaseJavaModule {

  private File tempFolder;

  public ShareModule(ReactApplicationContext reactContext) {
      super(reactContext);
  }

  @Override
  public String getName() {
      return "ReactNativeShareExtension";
  }

  @ReactMethod
  public void close() {
    if(getCurrentActivity() != null)
        getCurrentActivity().finish();
  }

//    @ReactMethod
//     public  void close() {
//        android.os.Process.killProcess(android.os.Process.myPid());
//     }
  @ReactMethod
  public void data(Promise promise) {
      promise.resolve(processIntent());
  }

  public WritableMap processIntent() {
        WritableMap map = Arguments.createMap();
        WritableArray arr = Arguments.createArray();
        String text = "";
        String type = "";
        String action = "";
        String mimeType = "";

        Context context = getReactApplicationContext();
        ContentResolver cR = context.getContentResolver();

        Activity currentActivity = getCurrentActivity();

        if (currentActivity != null) {
            this.tempFolder = new File(currentActivity.getCacheDir(), "rcShare");
            Intent intent = currentActivity.getIntent();
            action = intent.getAction();
            type = intent.getType();
            if (type == null) {
                type = "";
            }

            if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(type)) {
                text = intent.getStringExtra(Intent.EXTRA_TEXT);
                map.putString("value", text);
                map.putString("type", type);
            } else if (Intent.ACTION_SEND.equals(action)) {
                Uri uri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (uri != null) {
                    WritableMap b = Arguments.createMap();
                    text = "file://" + RealPathUtil.getRealPathFromURI(currentActivity, uri);
                    b.putString("value", text);
                    if ("image/*".equals(type) || "image/jpeg".equals(type) || "image/png".equals(type) || "image/jpg".equals(type) ) {
                        type = "image/jpeg";
                    } else if (type.equals("video/*")) {
                        type = "video/mp4";
                    }

                    b.putString("type", type);
                    arr.pushMap(b);
                    map.putArray("multiple", arr);

                }
            } else if(Intent.ACTION_SEND_MULTIPLE.equals(action)) {
                ArrayList<Uri> fileUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                // Log.d("send multiple", type);
                if(fileUris != null) {
                    for(int i = 0; i < fileUris.size() ;i++) {
                        WritableMap b = Arguments.createMap();
                        mimeType = cR.getType(fileUris.get(i));
                        text = "file://" + RealPathUtil.getRealPathFromURI(currentActivity,fileUris.get(i));
                        b.putString("value", text);
                        b.putString("type", mimeType);
                        arr.pushMap(b);
                    }
                    map.putArray("multiple", arr);
                }
            }
        }

        return map;
    }
}
