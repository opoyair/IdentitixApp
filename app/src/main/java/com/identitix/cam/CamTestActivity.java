package com.identitix.cam;

/**
 * @author Jose Davis Nidhin
 */

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;

public class CamTestActivity extends Activity {

	private static final String TAG = "CamTestActivity";
//	private static final String SERVER = "http://192.168.43.251:52531/api/Main";
//	private static final String SERVER = "http://192.168.43.251:52531/api/Find";
    private static final String SERVER = "http://172.15.167.114:1243/api/Find";
	Preview preview;
	Button buttonClick;
	EditText pinNumber;
	String pin;
	Camera camera;
	Activity act;
	Context ctx;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ctx = this;
		act = this;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.main);

		preview = new Preview(this, (SurfaceView)findViewById(R.id.surfaceView));
		preview.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		((FrameLayout) findViewById(R.id.layout)).addView(preview);
		preview.setKeepScreenOn(true);

		/*preview.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				camera.takePicture(shutterCallback, rawCallback, jpegCallback);

			}
		}); */

		//Toast.makeText(ctx, getString(R.string.take_photo_help), Toast.LENGTH_LONG).show();
		        pinNumber  = (EditText) findViewById(R.id.editText);
				buttonClick = (Button) findViewById(R.id.btnCapture);

				buttonClick.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
		////				preview.camera.takePicture(shutterCallback, rawCallback, jpegCallback);
					pin = pinNumber.getText().toString();
					camera.takePicture(shutterCallback, rawCallback, jpegCallback);
					}
				});
		//		
				buttonClick.setOnLongClickListener(new View.OnLongClickListener(){
					@Override
					public boolean onLongClick(View arg0) {
						camera.autoFocus(new Camera.AutoFocusCallback(){
							@Override
							public void onAutoFocus(boolean arg0, Camera arg1) {
								//camera.takePicture(shutterCallback, rawCallback, jpegCallback);
							}
						});
						return true;
					}
				});
	}

	@Override
	protected void onResume() {
		super.onResume();
		int numCams = Camera.getNumberOfCameras();
		if(numCams > 0){
			try{
				camera = Camera.open(0);
				camera.startPreview();
				preview.setCamera(camera);
			} catch (RuntimeException ex){
				Toast.makeText(ctx, getString(R.string.camera_not_found), Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	protected void onPause() {
		if(camera != null) {
			camera.stopPreview();
			preview.setCamera(null);
			camera.release();
			camera = null;
		}
		super.onPause();
	}

	private void resetCam() {
		camera.startPreview();
		preview.setCamera(camera);
	}

	private void refreshGallery(File file) {
		Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		mediaScanIntent.setData(Uri.fromFile(file));
		sendBroadcast(mediaScanIntent);
	}
	private void sendFile(File file) {
		new SendImageTask(SERVER).execute(file);
	}

	ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
			//			 Log.d(TAG, "onShutter'd");
		}
	};

	PictureCallback rawCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			//			 Log.d(TAG, "onPictureTaken - raw");
		}
	};

	PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			new SaveImageTask().execute(data);
			resetCam();
			Log.d(TAG, "onPictureTaken - jpeg");
		}
	};

	private class SaveImageTask extends AsyncTask<byte[], Void, Void> {

		@Override
		protected Void doInBackground(byte[]... data) {
			FileOutputStream outStream = null;

			// Write to SD Card
			try {
				File sdCard = Environment.getExternalStorageDirectory();
				File dir = new File (sdCard.getAbsolutePath() + "/identitix");
				dir.mkdirs();				

				String fileName = String.format("%d.jpg", System.currentTimeMillis());
				File outFile = new File(dir, fileName);

				outStream = new FileOutputStream(outFile);
				outStream.write(data[0]);
				outStream.flush();
				outStream.close();

				Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length + " to " + outFile.getAbsolutePath());

				refreshGallery(outFile);
				sendFile(outFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
			}
			return null;
		}

	}
	private class SendImageTask extends AsyncTask<File, Void, Void> {

		private String server;

		public SendImageTask(final String server) {
			this.server = server;
		}

		@Override
		protected Void doInBackground(File... data) {

          //  String url = "http://localhost:52531/";


			File file = data[0];
			byte bytes[] = new byte[0];
			try {
				bytes = FileUtils.readFileToByteArray(file);
			} catch (IOException e) {
				e.printStackTrace();
			}

			////
			Bitmap bm = BitmapFactory.decodeFile(file.getPath());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
			byte[] b = baos.toByteArray();
			String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
			////

			JSONObject json = new JSONObject();
			try {
				json.put("GroupID", "steel_rabbits");
				json.put("Pin",pin);
				//json.put("Image",b);
				json.put("Image",encodedImage);
				//json.put("Image", Base64.encode(bytes,0));

				StringEntity se = new StringEntity(json.toString());
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(this.server);
				httpPost.setEntity(se);
				httpPost.setHeader("Accept", "application/json");
				httpPost.setHeader("Content-type", "application/json");
				HttpResponse response;
				try {
					response = httpclient.execute(httpPost);
				}
				catch(Exception e) {
					response = null;
				}
				if (response != null && response.getStatusLine().getStatusCode()== HttpsURLConnection.HTTP_OK){
					//String responseStr = EntityUtils.toString(response.getEntity());
					String responseStr = response.getEntity().getContent().toString();
					if(responseStr.equals("NotFound!")) {

					}
					else {
						Bundle userData = new Bundle();
						userData.putString("name",responseStr);
						Intent in  = new Intent(getApplicationContext(),Success.class);
						in.putExtras(userData);
						startActivity(in);
						finish();
					}

				}
				else {
					Intent in  = new Intent(getApplicationContext(),Error.class);
					/*camera.stopPreview();
					//preview.setCamera(null);
					camera.release();
					camera = null;
					*/
					startActivity(in);
					finish();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

             /*
			try {
				HttpClient httpclient = new DefaultHttpClient();

				HttpPost httppost = new HttpPost(this.server);

				InputStreamEntity reqEntity = new InputStreamEntity(
						new FileInputStream(file), -1);
				reqEntity.setContentType("binary/octet-stream");
				reqEntity.setChunked(true); // Send in multiple parts if needed
				httppost.setEntity(reqEntity);
				HttpResponse response = httpclient.execute(httppost);
				//Do something with response...

			} catch (Exception e) {
				// show error
			}*/
			finally {
			}
			return null;
		}

	}
}


