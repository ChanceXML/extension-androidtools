package extension.androidtools;

#if (!android && !native)
#error 'extension-androidtools is not supported on your current platform'
#end
import extension.androidtools.jni.JNICache;
import lime.app.Event;
import lime.math.Rectangle;
import lime.system.JNI;
#if sys
import sys.io.Process;
#end

class Tools
{
	private static var _enableAppSecureJNI:Null<Dynamic>;
	private static var _disableAppSecureJNI:Null<Dynamic>;
	private static var _launchPackageJNI:Null<Dynamic>;
	private static var _showAlertDialogJNI:Null<Dynamic>;
	private static var _isDolbyAtmosJNI:Null<Dynamic>;
	private static var _showNotificationJNI:Null<Dynamic>;
	private static var _setActivityTitleJNI:Null<Dynamic>;
	private static var _minimizeWindowJNI:Null<Dynamic>;
	private static var _isAndroidTVJNI:Null<Dynamic>;
	private static var _isTabletJNI:Null<Dynamic>;
	private static var _isChromebookJNI:Null<Dynamic>;
	private static var _isDeXModeJNI:Null<Dynamic>;
	private static var _pickFileJNI:Null<Dynamic>;

	public static function enableAppSecure():Void
	{
		try {
			if (_enableAppSecureJNI == null)
				_enableAppSecureJNI = JNICache.createStaticMethod('org/haxe/extension/Tools', 'enableAppSecure', '()V');

			if (_enableAppSecureJNI != null)
				_enableAppSecureJNI();
		} catch (e:Dynamic) {
			trace("Error in enableAppSecure: " + e);
		}
	}

	public static function disableAppSecure():Void
	{
		try {
			if (_disableAppSecureJNI == null)
				_disableAppSecureJNI = JNICache.createStaticMethod('org/haxe/extension/Tools', 'disableAppSecure', '()V');

			if (_disableAppSecureJNI != null)
				_disableAppSecureJNI();
		} catch (e:Dynamic) {
			trace("Error in disableAppSecure: " + e);
		}
	}

	public static function launchPackage(packageName:String, requestCode:Int = 1):Void
	{
		try {
			if (_launchPackageJNI == null)
				_launchPackageJNI = JNICache.createStaticMethod('org/haxe/extension/Tools', 'launchPackage', '(Ljava/lang/String;I)V');

			if (_launchPackageJNI != null)
				_launchPackageJNI(packageName, requestCode);
		} catch (e:Dynamic) {
			trace("Error in launchPackage: " + e);
		}
	}

	public static function showAlertDialog(title:String, message:String, ?positiveButton:ButtonData, ?negativeButton:ButtonData):Void
	{
		try {
			if (positiveButton == null)
				positiveButton = {name: "OK", func: null};

			if (negativeButton == null)
				negativeButton = {name: null, func: null};

			if (_showAlertDialogJNI == null)
				_showAlertDialogJNI = JNICache.createStaticMethod('org/haxe/extension/Tools', 'showAlertDialog',
					'(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lorg/haxe/lime/HaxeObject;Ljava/lang/String;Lorg/haxe/lime/HaxeObject;)V');

			if (_showAlertDialogJNI != null)
				_showAlertDialogJNI(title, message, positiveButton.name, new ButtonListener(positiveButton.func), negativeButton.name,
					new ButtonListener(negativeButton.func));
		} catch (e:Dynamic) {
			trace("Error in showAlertDialog: " + e);
		}
	}

	#if sys
	public static function isRooted():Bool
	{
		try {
			final process:Process = new Process('su');
			final exitCode:Null<Int> = process.exitCode(true);
			return exitCode != null && exitCode != 255;
		} catch (e:Dynamic) {
			trace("Error in isRooted: " + e);
			return false;
		}
	}
	#end

	public static function isDolbyAtmos():Bool
	{
		try {
			if (_isDolbyAtmosJNI == null)
				_isDolbyAtmosJNI = JNICache.createStaticMethod('org/haxe/extension/Tools', 'isDolbyAtmos', '()Z');
			
			return _isDolbyAtmosJNI != null && _isDolbyAtmosJNI();
		} catch (e:Dynamic) {
			trace("Error in isDolbyAtmos: " + e);
			return false;
		}
	}

	public static function showNotification(title:String, message:String, ?channelID:String = 'unknown_channel',
			?channelName:String = 'Unknown Channel', ?ID:Int = 1):Void
	{
		try {
			if (_showNotificationJNI == null)
				_showNotificationJNI = JNICache.createStaticMethod('org/haxe/extension/Tools', 'showNotification',
					'(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V');
			
			if (_showNotificationJNI != null)
				_showNotificationJNI(title, message, channelID, channelName, ID);
		} catch (e:Dynamic) {
			trace("Error in showNotification: " + e);
		}
	}

	public static function setActivityTitle(title:String):Bool
	{
		try {
			if (_setActivityTitleJNI == null)
				_setActivityTitleJNI = JNICache.createStaticMethod('org/libsdl/app/SDLActivity', 'setActivityTitle', '(Ljava/lang/String;)Z');
			
			return _setActivityTitleJNI != null && _setActivityTitleJNI(title);
		} catch (e:Dynamic) {
			trace("Error in setActivityTitle: " + e);
			return false;
		}
	}

	public static function minimizeWindow():Void
	{
		try {
			if (_minimizeWindowJNI == null)
				_minimizeWindowJNI = JNICache.createStaticMethod('org/libsdl/app/SDLActivity', 'minimizeWindow', '()V');
			
			if (_minimizeWindowJNI != null)
				_minimizeWindowJNI();
		} catch (e:Dynamic) {
			trace("Error in minimizeWindow: " + e);
		}
	}

	public static function isAndroidTV():Bool
	{
		try {
			if (_isAndroidTVJNI == null)
				_isAndroidTVJNI = JNICache.createStaticMethod('org/libsdl/app/SDLActivity', 'isAndroidTV', '()Z');
			
			return _isAndroidTVJNI != null && _isAndroidTVJNI();
		} catch (e:Dynamic) {
			trace("Error in isAndroidTV: " + e);
			return false;
		}
	}

	public static function isTablet():Bool
	{
		try {
			if (_isTabletJNI == null)
				_isTabletJNI = JNICache.createStaticMethod('org/libsdl/app/SDLActivity', 'isTablet', '()Z');
			
			return _isTabletJNI != null && _isTabletJNI();
		} catch (e:Dynamic) {
			trace("Error in isTablet: " + e);
			return false;
		}
	}

	public static function isChromebook():Bool
	{
		try {
			if (_isChromebookJNI == null)
				_isChromebookJNI = JNICache.createStaticMethod('org/libsdl/app/SDLActivity', 'isChromebook', '()Z');
			
			return _isChromebookJNI != null && _isChromebookJNI();
		} catch (e:Dynamic) {
			trace("Error in isChromebook: " + e);
			return false;
		}
	}

	public static function isDeXMode():Bool
	{
		try {
			if (_isDeXModeJNI == null)
				_isDeXModeJNI = JNICache.createStaticMethod('org/libsdl/app/SDLActivity', 'isDeXMode', '()Z');
			
			return _isDeXModeJNI != null && _isDeXModeJNI();
		} catch (e:Dynamic) {
			trace("Error in isDeXMode: " + e);
			return false;
		}
	}

	public static function pickFile(extensions:String, onComplete:String->Void):Void
    {
    	try {
    		if (_pickFileJNI == null)
	    		_pickFileJNI = JNICache.createStaticMethod('org/haxe/extension/Tools', 'pickFile', '(Ljava/lang/String;Lorg/haxe/lime/HaxeObject;)V');
 
    		if (_pickFileJNI != null)
		    	_pickFileJNI(extensions, new FilePickerCallback(onComplete));
    	} catch (e:Dynamic) {
    		if (onComplete != null)
		    	onComplete("");
    	}
    }
}

@:noCompletion
typedef ButtonData =
{
	name:String,
	func:Void->Void
}

@:noCompletion
private class ButtonListener #if (lime >= "8.0.0") implements JNISafety #end
{
	@:noCompletion
	private var onClickEvent:Event<Void->Void> = new Event<Void->Void>();

	public function new(clickCallback:Void->Void):Void
	{
		if (clickCallback != null)
			onClickEvent.add(clickCallback);
	}

	@:keep
	#if (lime >= "8.0.0")
	@:runOnMainThread
	#end
	public function onClick():Void
	{
		onClickEvent.dispatch();
	}
}

@:noCompletion
private class FilePickerCallback #if (lime >= "8.0.0") implements JNISafety #end
{
	private var onPicked:String->Void;

	public function new(callback:String->Void):Void
	{
		this.onPicked = callback;
	}

	@:keep
	#if (lime >= "8.0.0")
	@:runOnMainThread
	#end
	public function onFilePicked(path:String):Void
	{
		if (onPicked != null)
			onPicked(path);
	}
}
