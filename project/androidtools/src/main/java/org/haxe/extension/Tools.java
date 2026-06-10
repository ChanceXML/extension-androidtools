package org.haxe.extension;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.Log;
import android.util.DisplayMetrics;
import android.view.DisplayCutout;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.haxe.extension.Extension;
import org.haxe.lime.HaxeObject;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.FileInputStream;
import java.io.OutputStream;

public class Tools extends Extension
{
	public static final String LOG_TAG = "Tools";

	public static HaxeObject cbObject;
	private static HaxeObject filePickerCallback = null;
	private static HaxeObject fileSaverCallback = null;
	private static String pendingSaveFilePath = null;

	public static void initCallBack(final HaxeObject cbObject)
	{
		Tools.cbObject = cbObject;
	}

	public static String getPackageName()
	{
		return mainContext.getPackageName();
	}

	public static String[] getGrantedPermissions()
	{
		List<String> granted = new ArrayList<String>();

		try
		{
			final PackageInfo info = (PackageInfo) mainContext.getPackageManager().getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);

			for (int i = 0; i < info.requestedPermissions.length; i++)
			{
				if ((info.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0)
					granted.add(info.requestedPermissions[i]);
			}
		}
		catch (Exception e)
		{
			Log.e(LOG_TAG, e.toString());
		}

		return granted.toArray(new String[granted.size()]);
	}

	public static void makeToastText(final String message, final int duration, final int gravity, final int xOffset, final int yOffset)
	{
		mainActivity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					final Toast toast = Toast.makeText(mainContext, message, duration);

					if (gravity >= 0)
						toast.setGravity(gravity, xOffset, yOffset);

					toast.show();
				}
				catch (Exception e)
				{
					Log.e(LOG_TAG, e.toString());
				}
			}
		});
	}

	public static void showAlertDialog(final String title, final String message, final String positiveLabel, final HaxeObject positiveObject, final String negativeLabel, final HaxeObject negativeObject)
	{
		final Object lock = new Object();

		mainActivity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					final AlertDialog.Builder builder;

					if (Build.VERSION.SDK_INT >= 21) {
						builder = new AlertDialog.Builder(mainContext, android.R.style.Theme_DeviceDefault_Dialog_Alert);
					} else {
						builder = new AlertDialog.Builder(mainContext);
					}

					if (title != null)
						builder.setTitle(title);

					if (message != null)
						builder.setMessage(message);

					builder.setCancelable(false);

					if (positiveLabel != null)
					{
						builder.setPositiveButton(positiveLabel, new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								dialog.dismiss();

								if (positiveObject != null)
									positiveObject.call("onClick", new Object[]{});
							}
						});
					}

					if (negativeLabel != null)
					{
						builder.setNegativeButton(negativeLabel, new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								dialog.dismiss();

								if (negativeObject != null)
									negativeObject.call("onClick", new Object[]{});
							}
						});
					}

					final AlertDialog dialog = builder.create();
					dialog.setOnDismissListener(new DialogInterface.OnDismissListener()
					{
						@Override
						public void onDismiss(DialogInterface dialog)
						{
							synchronized (lock)
							{
								lock.notify();
							}
						}
					});
					dialog.show();
				}
				catch (Exception e)
				{
					Log.e(LOG_TAG, e.toString());
				}
			}
		});

		synchronized (lock)
		{
			try
			{
				lock.wait();
			}
			catch (InterruptedException e)
			{
				Log.e(LOG_TAG, e.toString());
			}
		}
	}

	public static void enableAppSecure()
	{
		mainActivity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					mainActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
				}
				catch (Exception e)
				{
					Log.e(LOG_TAG, e.toString());
				}
			}
		});
	}

	public static void disableAppSecure()
	{
		mainActivity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					mainActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
				}
				catch (Exception e)
				{
					Log.e(LOG_TAG, e.toString());
				}
			}
		});
	}

	public static void launchPackage(final String packageName, final int requestCode)
	{
		try
		{
			mainActivity.startActivityForResult(mainActivity.getPackageManager().getLaunchIntentForPackage(packageName), requestCode);
		}
		catch (Exception e)
		{
			Log.e(LOG_TAG, e.toString());
		}
	}

	public static void requestPermissions(String[] permissions, int requestCode)
	{
		List<String> ungrantedPermissions = new ArrayList<>();

		try
		{
			for (String permission : permissions)
			{
				if (Extension.mainActivity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
					ungrantedPermissions.add(permission);
			}

			if (!ungrantedPermissions.isEmpty())
				Extension.mainActivity.requestPermissions(ungrantedPermissions.toArray(new String[0]), requestCode);
		}
		catch (Exception e)
		{
			Log.e(LOG_TAG, e.toString());
		}
	}

	public static boolean hasManageAllFiles()
	{
		try
		{
			if (Build.VERSION.SDK_INT >= 30)
			{
				return Environment.isExternalStorageManager();
			}
		}
		catch (Exception e)
		{
			Log.e(LOG_TAG, e.toString());
		}
		return true; 
	}

	public static void requestManageAllFiles()
	{
		try
		{
			if (Build.VERSION.SDK_INT >= 30) 
			{
				try 
				{
					Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
					intent.addCategory("android.intent.category.DEFAULT");
					intent.setData(Uri.parse(String.format("package:%s", Extension.mainContext.getPackageName())));
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					Extension.mainActivity.startActivity(intent);
				} 
				catch (Exception e) 
				{
					Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					Extension.mainActivity.startActivity(intent);
				}
			}
			else
			{
				Log.w(LOG_TAG, "requestManageAllFiles is only applicable for Android 11+");
			}
		}
		catch (Exception e)
		{
			Log.e(LOG_TAG, e.toString());
		}
	}

	public static void requestSetting(final String setting, final int requestCode)
	{
		try
		{
			final Intent intent = new Intent(setting);
			intent.setData(Uri.fromParts("package", packageName, null));
			mainActivity.startActivityForResult(intent, requestCode);
		}
		catch (Exception e)
		{
			Log.e(LOG_TAG, e.toString());
		}
	}

	public static boolean isDolbyAtmos()
	{
		try
		{
			final MediaFormat formatEac3 = new MediaFormat();
			formatEac3.setString(MediaFormat.KEY_MIME, "audio/eac3-joc");

			final MediaFormat formatAc4 = new MediaFormat();
			formatAc4.setString(MediaFormat.KEY_MIME, "audio/ac4");

			final MediaCodecList codecList = new MediaCodecList(MediaCodecList.ALL_CODECS);

			if (codecList.findDecoderForFormat(formatEac3) != null || codecList.findDecoderForFormat(formatAc4) != null)
				return true;
		}
		catch (Exception e)
		{
			Log.e(LOG_TAG, e.toString());
		}

		return false;
	}

	@SuppressWarnings("deprecation")
	public static void showNotification(final String title, final String message, final String channelID, final String channelName, final int ID)
	{
		mainActivity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					if (Build.VERSION.SDK_INT >= 33)
					{
						if (ContextCompat.checkSelfPermission(mainContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
						{
							Log.w(LOG_TAG, "POST_NOTIFICATIONS permission not granted. Skipping notification.");
							return;
						}
					}

					final NotificationManager notificationManager = (NotificationManager) mainContext.getSystemService(Context.NOTIFICATION_SERVICE);

					if (Build.VERSION.SDK_INT >= 26)
						notificationManager.createNotificationChannel(new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_DEFAULT));

					final Notification.Builder builder;

					if (Build.VERSION.SDK_INT >= 26)
						builder = new Notification.Builder(mainContext, channelID);
					else
						builder = new Notification.Builder(mainContext);

					builder.setAutoCancel(true);
					builder.setContentTitle(title);
					builder.setContentText(message);
					builder.setSmallIcon(mainContext.getResources().getIdentifier("icon", "drawable", packageName));
					builder.setWhen(System.currentTimeMillis());
					notificationManager.notify(ID, builder.build());
				}
				catch (Exception e)
				{
					Log.e(LOG_TAG, e.toString());
				}
			}
		});
	}

	public static File getFilesDir()
	{
	    return mainContext.getFilesDir();
	}

	public static File getExternalFilesDir(final String type)
	{
		return mainContext.getExternalFilesDir(type);
	}

	public static File[] getExternalFilesDirs(final String type)
	{
		return mainContext.getExternalFilesDirs(type);
	}

	@SuppressWarnings("deprecation")
	public static File[] getExternalMediaDirs()
	{
		return mainContext.getExternalMediaDirs();
	}

	public static File getCacheDir()
	{
		return mainContext.getCacheDir();
	}

	public static File getExternalCacheDir()
	{
		return mainContext.getExternalCacheDir();
	}

	public static File[] getExternalCacheDirs()
	{
		return mainContext.getExternalCacheDirs();
	}

	public static File getCodeCacheDir()
	{
		return mainContext.getCodeCacheDir();
	}

	public static File getNoBackupFilesDir()
	{
		return mainContext.getNoBackupFilesDir();
	}

	public static File getObbDir()
	{
		return mainContext.getObbDir();
	}

	public static File[] getObbDirs()
	{
		return mainContext.getObbDirs();
	}

	public static void adjustStreamVolume(final int streamType, final int direction, final int flags)
	{
		try
		{
			final AudioManager audioManager = (AudioManager) mainContext.getSystemService(Context.AUDIO_SERVICE);

			audioManager.adjustStreamVolume(streamType, direction, flags);
		}
		catch (Exception e)
		{
			Log.e(LOG_TAG, e.toString());
		}
	}

	public static int getStreamVolume(final int streamType)
	{
		try
		{
			final AudioManager audioManager = (AudioManager) mainContext.getSystemService(Context.AUDIO_SERVICE);

			return audioManager.getStreamVolume(streamType);
		}
		catch (Exception e)
		{
			Log.e(LOG_TAG, e.toString());
		}

		return 0;
	}

	@SuppressWarnings("deprecation")
	public static int requestAudioFocus(final HaxeObject haxeCallbackObject, final int streamType, final int durationHint)
	{
		try
		{
			final AudioManager audioManager = (AudioManager) mainContext.getSystemService(Context.AUDIO_SERVICE);

			final AudioManager.OnAudioFocusChangeListener focusChangeListener = new AudioManager.OnAudioFocusChangeListener()
			{
				@Override
				public void onAudioFocusChange(int focusChange)
				{
					if (haxeCallbackObject != null)
						haxeCallbackObject.call1("onAudioFocusChange", focusChange);
				}
			};

			return audioManager.requestAudioFocus(focusChangeListener, streamType, durationHint);
		}
		catch (Exception e)
		{
			Log.e(LOG_TAG, e.toString());
		}

		return AudioManager.AUDIOFOCUS_REQUEST_FAILED;
	}

	@SuppressWarnings("deprecation")
	public static int abandonAudioFocus(final HaxeObject haxeCallbackObject)
	{
		try
		{
			final AudioManager audioManager = (AudioManager) mainContext.getSystemService(Context.AUDIO_SERVICE);

			final AudioManager.OnAudioFocusChangeListener focusChangeListener = new AudioManager.OnAudioFocusChangeListener()
			{
				@Override
				public void onAudioFocusChange(int focusChange)
				{
					if (haxeCallbackObject != null)
						haxeCallbackObject.call1("onAudioFocusChange", focusChange);
				}
			};

			return audioManager.abandonAudioFocus(focusChangeListener);
		}
		catch (Exception e)
		{
			Log.e(LOG_TAG, e.toString());
		}

		return AudioManager.AUDIOFOCUS_REQUEST_FAILED;
	}

	public static void pickFile(String extensions, final HaxeObject callback)
    {
      	filePickerCallback = callback;
    	Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
    	intent.addCategory(Intent.CATEGORY_OPENABLE);

	    if (extensions != null && !extensions.isEmpty()) 
    	{
		    intent.setType("*/*");
		    String[] exts = extensions.split("[,/]");
		    String[] mimes = new String[exts.length];
		
		    for (int i = 0; i < exts.length; i++) 
	    	{
	    		String ext = exts[i].trim().replace(".", "");
		    	String mime = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
		    	mimes[i] = mime != null ? mime : "*/*";
	    	}
		
	    	intent.putExtra(Intent.EXTRA_MIME_TYPES, mimes);
  	    } 
    	else 
       	{
		    intent.setType("*/*");
	    }

    	mainActivity.startActivityForResult(intent, 4321);
    }

	public static void saveFile(String sourceFilePath, String suggestedName, String mimeType, final HaxeObject callback)
	{
		pendingSaveFilePath = sourceFilePath;
		fileSaverCallback = callback;

		Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType(mimeType != null && !mimeType.isEmpty() ? mimeType : "*/*");
		intent.putExtra(Intent.EXTRA_TITLE, suggestedName);

		mainActivity.startActivityForResult(intent, 4322); 
	}
	
	
	@Override
	public boolean onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == 4321)
		{
			String resultPath = "";
			if (resultCode == Activity.RESULT_OK && data != null)
			{
				Uri uri = data.getData();
				if (uri != null)
				{
					try
					{
						String fileName = "picked_file";
						Cursor cursor = mainContext.getContentResolver().query(uri, null, null, null, null);
						if (cursor != null && cursor.moveToFirst())
						{
							int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
							if (index != -1)
								fileName = cursor.getString(index);
							cursor.close();
						}

						File file = new File(mainContext.getCacheDir(), fileName);
						InputStream inputStream = mainContext.getContentResolver().openInputStream(uri);
						FileOutputStream outputStream = new FileOutputStream(file);
						
						byte[] buffer = new byte[1024];
						int length;
						while ((length = inputStream.read(buffer)) > 0)
						{
							outputStream.write(buffer, 0, length);
						}
						
						outputStream.close();
						inputStream.close();
						resultPath = file.getAbsolutePath();
					}
					catch (Exception e)
					{
						Log.e(LOG_TAG, e.toString());
					}
				}
			}

			if (filePickerCallback != null)
			{
				filePickerCallback.call("onFilePicked", new Object[] { resultPath });
				filePickerCallback = null;
			}
			return true;
		}

		if (requestCode == 4322)
		{
			boolean success = false;
			if (resultCode == Activity.RESULT_OK && data != null)
			{
				Uri uri = data.getData();
				if (uri != null && pendingSaveFilePath != null)
				{
					try
					{
						InputStream inputStream = new FileInputStream(new File(pendingSaveFilePath));
						OutputStream outputStream = mainContext.getContentResolver().openOutputStream(uri);

						byte[] buffer = new byte[1024];
						int length;
						while ((length = inputStream.read(buffer)) > 0)
						{
							outputStream.write(buffer, 0, length);
						}

						outputStream.close();
						inputStream.close();
						success = true;
					}
					catch (Exception e)
					{
						Log.e(LOG_TAG, e.toString());
					}
				}
			}

			if (pendingSaveFilePath != null)
			{
				new File(pendingSaveFilePath).delete();
				pendingSaveFilePath = null;
			}

			if (fileSaverCallback != null)
			{
				fileSaverCallback.call("onFileSaved", new Object[] { success });
				fileSaverCallback = null;
			}
			return true;
		}

		return super.onActivityResult(requestCode, resultCode, data);
	}
}
