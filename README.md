# react-native-iflytek-asr-tts

iflytek-asr-tts

## Installation

```sh
npm install react-native-iflytek-asr-tts
```
React Native 下的科大讯飞语音库，可以进行语音识别与语音合成。

## Install
```
yarn add react-native-speech-iflytek
react-native link
```
android平台权限
   ```
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <!--读取网络信息状态 -->
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
    <!--获取当前wifi状态 -->
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
    <!--允许程序改变网络连接状态 -->
    <uses-permission android:name="android.permission.CHANGE_NETWORK_STATE"/>
    <!--读取手机信息权限 -->
    <uses-permission android:name="android.permission.READ_PHONE_STATE"/>
    <uses-permission android:name="android.permission.WRITE_SETTINGS" />
   ```
## Usage
（详见 Example）引入包：
```
import { Recognizer, Synthesizer, SpeechConstant } from "react-native-iflytek-asr-tts";
```
语音识别：
```
Recognizer.init("57c7c5b0");
this.recognizerEventEmitter = new NativeEventEmitter(Recognizer);
this.recognizerEventEmitter.addListener('onRecognizerResult', this.onRecognizerResult);
Recognizer.start();
```
处理识别结果：
```
onRecognizerResult(e) {
    if (!e.isLast) {
        return;
    }
    this.setState({ text: e.result });
}
```
## API

### Recognizer
#### Methods
- `Recognizer.init(String AppId)`
初始化语音识别
- `Recognizer.start()`
开始语音识别
- `Recognizer.cancel()`
取消语音识别
- `Recognizer.isListening()`
检测当前是否正在语音识别。返回 `Promise`，结果为 `bool` 类型，表示当前是否正在语音识别
- `Recognizer.stop()`
如果正在语音识别，则结束语音识别
- `Recognizer.setParameter(String parameter, String value)`
语音识别设置，详见讯飞语音文档
- `Recognizer.getParameter(String param)`
获取语音识别设置，详见讯飞语音文档。返回 `Promise`，结果为 `String` 类型，表示语音识别设置值
#### Events
- `onRecognizerResult(JSON result)`
语音识别结果，在语音识别时会不断触发该事件，`result` 为 `JSON` 类型，其值：

    - `text`：当次识别结果
    - `result`：当前识别结果，最常使用
    - `isLast`：是否是最后一次识别，调用 `Recognizer.stop()` 后，`isLast` 值为 `true`，否则一直为 `false`
    - `duration`：当前识别时间长度
- `onRecognizerVolumeChanged(JSON result)`
语音识别的音量大小，当识别的语音改变音量时会触发该事件，`result` 为 `JSON` 类型，其值：

    - `volume`: 当前音量大小

- `onRecognizerError(JSON error)`
语音识别出现错误，错误信息与讯飞文档保持一致，其值：

    - `errorCode`: 获取错误码，关于错误码请见官方文档 [MSC错误码](http://www.xfyun.cn/index.php/default/doccenter/doccenterInner?itemTitle=ZmFx&anchor=Y29udGl0bGU2Ng==) ：
    - `errorType`: （仅 iOS）获取错误码类型
    - `errorDesc`: （仅 iOS）获取错误描述
    - `errorDescription`: （仅 Android）获取错误描述，不包含错误码的描述信息
    - `plainDescription`: （仅 Android）获取错误描述，包含错误码的描述信息

### Synthesizer
#### Methods
- `Synthesizer.init(String AppId)`
初始化语音合成
- `Synthesizer.start(String content)`
开始语音合成
- `Synthesizer.stop()`
如果正在语音合成，则结束语音合成
- `Synthesizer.isSpeaking()`
检测当前是否正在语音合成。返回 `Promise`，结果为 `bool` 类型，表示当前是否正在语音合成
- `Synthesizer.pause()`
如果正在语音合成，则暂停语音合成（对应 resume）
- `Synthesizer.resume()`
如果正在语音合成，则开始语音合成（对应 pause）
- `Synthesizer.setParameter(String parameter, String value)`
语音合成设置，详见讯飞语音文档
- `Synthesizer.getParameter(String param)`
获取语音合成设置，详见讯飞语音文档。返回 `Promise`，结果为 `String` 类型，表示语音合成设置值
#### Events
- `onSynthesizerBufferCompletedEvent()`
语音合成缓冲完成时触发该事件
- `onSynthesizerSpeakCompletedEvent()`
语音合成播放完成时触发该事件

### SpeechConstant
本模块包含讯飞接口的所有常量，如设置发言人、发言速度等，详见讯飞文档，使用示例：
```
Synthesizer.setParameter(SpeechConstant.VOICE_NAME, "xiaoyu");
```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
