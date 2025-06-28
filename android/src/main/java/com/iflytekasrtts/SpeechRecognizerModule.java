package com.iflytekasrtts;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.uimanager.IllegalViewOperationException;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.annotation.Nullable;

/**
 * 提供讯飞语音识别 React Native 接口
 */
public class SpeechRecognizerModule extends ReactContextBaseJavaModule {
  private static final String TAG = "SpeechRecognizer";
  private Context context;

  private static SpeechRecognizer mIat;
  private static RecognizerListener mIatListener;
  private static String result = "";

  private static long startTime;
  private static long endTime;
  /**
   * 开启动态修正
   */
  private static boolean dwa;

  // 用HashMap存储听写结果
  private static HashMap<String, String> mIatResults = new LinkedHashMap<>();


  public SpeechRecognizerModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.context = reactContext;
  }

  @Override
  public String getName() {
    return "SpeechRecognizerModule";
  }

  @ReactMethod
  public void init(String AppId) {
    Log.d(TAG, "init,AppId" + AppId);
    if (mIat != null) {
      return;
    }

    SpeechUtility.createUtility(this.context, SpeechConstant.APPID + "=" + AppId);

    mIat = SpeechRecognizer.createRecognizer(this.context, null);
    mIatListener = new RecognizerListener() {
      @Override
      public void onVolumeChanged(int volume, byte[] bytes) {
        Log.d(TAG, "onVolumeChanged:" + volume);
        WritableMap params = Arguments.createMap();
        params.putInt("volume", volume);
        SpeechRecognizerModule.this.onJSEvent(getReactApplicationContext(), "onRecognizerVolumeChanged", params);
      }

      @Override
      public void onBeginOfSpeech() {

      }

      @Override
      public void onEndOfSpeech() {

      }

      @Override
      public void onResult(RecognizerResult results, boolean isLast) {
        onIatResult(results, isLast);
      }

      @Override
      public void onError(SpeechError error) {
        onIatError(error);
      }

      @Override
      public void onEvent(int i, int i1, int i2, Bundle bundle) {

      }
    };
    setIatParam();
  }

  @ReactMethod
  public void start() {
    startTime = System.currentTimeMillis();
    Log.d(TAG, "start");
    if (mIat.isListening()) {
      mIat.cancel();
    }
    mIatResults.clear();
    mIat.startListening(mIatListener);
  }

  @ReactMethod
  public void cancel() {
    Log.d(TAG, "cancel");
    if (mIat.isListening()) {
      mIat.cancel();
    }
  }

  @ReactMethod
  public void isListening(Promise promise) {
    try {
      if (mIat.isListening()) {
        promise.resolve(true);
      } else {
        promise.resolve(false);
      }
    } catch (IllegalViewOperationException e) {
      Log.e(TAG, "isListening", e);
      promise.reject("Error: isListening()", e);
    }
  }

  @ReactMethod
  public void stop() {
    if (mIat.isListening()) {
      mIat.stopListening();
    }
  }

  @ReactMethod
  public void setParameter(String parameter, String value) {
    if (parameter.equals(SpeechConstant.ASR_AUDIO_PATH)) {
      value = Environment.getExternalStorageDirectory() + value;
    }
    if ("dwa".equals(parameter)) {
      setDwa(value);
      return;
    }
    mIat.setParameter(parameter, value);
  }

  @ReactMethod
  public void getParameter(String param, Promise promise) {
    String value = mIat.getParameter(param);
    try {
      promise.resolve(value);
    } catch (IllegalViewOperationException e) {
      Log.e(TAG, "getParameter", e);
      promise.reject("Error: getParameter()", e);
    }
  }

  private static void setIatParam() {
    // 清空参数
    //  mIat.setParameter(SpeechConstantModule.PARAMS, null);

    // 设置听写引擎
    mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);

    // 设置返回结果格式
    mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

    // 设置语言
    mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
    // 设置语言区域
    mIat.setParameter(SpeechConstant.ACCENT, "mandarin");

    // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
    mIat.setParameter(SpeechConstant.VAD_BOS, "5000");

    // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
    mIat.setParameter(SpeechConstant.VAD_EOS, "5000");

    // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
    mIat.setParameter(SpeechConstant.ASR_PTT, "1");
    // 设置动态修正
    //setDwa("wpgs");
    // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
    // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
    //mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
    //mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/App/SpeechRecognizer");
  }

  /**
   * 开启dwa
   * @param value v
   */
  private static void setDwa(String value) {
    if ("".equals(value) || value == null) {
      dwa = false;
    } else {
      dwa = true;
    }
    mIat.setParameter("dwa", value);
    Log.d(TAG, "set parameter dwa:" + value);
  }
  /**
   * 解析结果
   *
   * @param json 识别结果
   * @return 解析后结果
   */
  private static String parseIatResult(String json) {
    StringBuilder ret = new StringBuilder();
    try {
      JSONTokener token = new JSONTokener(json);
      JSONObject joResult = new JSONObject(token);
      JSONArray words = joResult.getJSONArray("ws");
      for (int i = 0; i < words.length(); i++) {
        // 转写结果词，默认使用第一个结果
        JSONArray items = words.getJSONObject(i).getJSONArray("cw");
        JSONObject obj = items.getJSONObject(0);
        ret.append(obj.getString("w"));
      }
    } catch (Exception e) {
      Log.e(TAG, "parseIatResult error", e);
    }
    return ret.toString();
  }

  /**
   * 开启动态修正
   *
   * @param results 识别结果
   * @return 动态修正结果
   */
  private static String parseDwa(RecognizerResult results) {
    String text = parseIatResult(results.getResultString());
    String sn = null;
    String pgs = null;
    String rg = null;
    // 读取json结果中的sn字段
    try {
      JSONObject resultJson = new JSONObject(results.getResultString());
      sn = resultJson.optString("sn");
      pgs = resultJson.optString("pgs");
      rg = resultJson.optString("rg");
    } catch (JSONException e) {
      Log.e(TAG, "parseDwa error", e);
    }
    //如果pgs是rpl就在已有的结果中删除掉要覆盖的sn部分
    if ("rpl".equals(pgs)) {
      String[] strings = rg.replace("[", "").replace("]", "").split(",");
      int begin = Integer.parseInt(strings[0]);
      int end = Integer.parseInt(strings[1]);
      for (int i = begin; i <= end; i++) {
        mIatResults.remove(i + "");
      }
    }

    mIatResults.put(sn, text);
    StringBuilder resultBuffer = new StringBuilder();
    for (String key : mIatResults.keySet()) {
      resultBuffer.append(mIatResults.get(key));
    }

    return resultBuffer.toString();
  }

  private void onIatResult(RecognizerResult results, boolean isLast) {
    String text;
    if (dwa) {
      text = parseDwa(results);
      result = text;
    } else {
      text = parseIatResult(results.getResultString());
      result += text;
    }


    endTime = System.currentTimeMillis();
    int duration = (int) (endTime - startTime);

    WritableMap params = Arguments.createMap();

    params.putString("text", text);
    params.putString("result", result);
    params.putBoolean("isLast", isLast);
    params.putInt("duration", duration);
    Log.d(TAG, "onIatResult result:" + result);
    if (isLast) {
      result = "";
    }

    this.onJSEvent(getReactApplicationContext(), "onRecognizerResult", params);
  }

  private void onIatError(SpeechError error) {
    Log.d(TAG, "onIatError error", error);
    WritableMap params = Arguments.createMap();

    params.putInt("errorCode", error.getErrorCode());
    params.putString("message", error.getErrorDescription());
    params.putString("plainDescription", error.getPlainDescription(true));

    this.onJSEvent(getReactApplicationContext(), "onRecognizerError", params);
  }

  private void showTip(final String str) {
    Toast.makeText(this.context, str, Toast.LENGTH_SHORT).show();
  }

  private void onJSEvent(ReactContext reactContext,
                         String eventName,
                         @Nullable WritableMap params) {
    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
      .emit(eventName, params);
  }
}
