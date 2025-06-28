import {
  Synthesizer,
  Recognizer,
  SpeechConstant,
} from 'react-native-iflytek-asr-tts';
import {
  Text,
  View,
  StyleSheet,
  NativeEventEmitter,
  PermissionsAndroid,
  Button,
  TextInput,
} from 'react-native';
import { useState, useEffect } from 'react';
import { Picker } from '@react-native-picker/picker';
import Separator from './Separator';

export default function App() {
  const [complete, setComplete] = useState<string | undefined>();
  const [buffer, setBuffer] = useState<string | undefined>();
  const [value, onChangeText] = useState(
    '此功能为您的APPID和接口授权信息模拟一个真实环境下的DEMO，用于测试和体验您的API接口是否正常工作。'
  );

  const [result, setResult] = useState<string | undefined>();
  const [selectedLanguage, setSelectedLanguage] = useState();

  useEffect(() => {
    (async function requestAllPermissions() {
      try {
        //申请多个权限，传入一个权限对象数组
        const granted = await PermissionsAndroid.requestMultiple([
          PermissionsAndroid.PERMISSIONS.PROCESS_OUTGOING_CALLS,
          PermissionsAndroid.PERMISSIONS.RECORD_AUDIO,
          PermissionsAndroid.PERMISSIONS.READ_MEDIA_AUDIO,
          PermissionsAndroid.PERMISSIONS.WRITE_EXTERNAL_STORAGE,
          PermissionsAndroid.PERMISSIONS.READ_EXTERNAL_STORAGE,
        ]);
        if (
          granted['android.permission.CALL_PHONE'] ===
          PermissionsAndroid.RESULTS.GRANTED
        ) {
          console.log('CALL_PHONE 权限已获取');
        }
        if (
          granted['android.permission.PROCESS_OUTGOING_CALLS'] ===
          PermissionsAndroid.RESULTS.GRANTED
        ) {
          console.log('PROCESS_OUTGOING_CALLS 权限已获取');
        }
        if (
          granted['android.permission.USE_SIP'] ===
          PermissionsAndroid.RESULTS.GRANTED
        ) {
          console.log('USE_SIP 权限已获取');
        }
      } catch (err) {
        console.log(err);
      }
    })();
    // 语音合成初始化
    Synthesizer.init('0e7cfb2f');
    // 语音合成事件监听
    const synthesizerEventEmitter = new NativeEventEmitter(Synthesizer);
    synthesizerEventEmitter.addListener(
      'onSynthesizerSpeakCompletedEvent',
      (r) => {
        setComplete(JSON.stringify(r));
      }
    );
    synthesizerEventEmitter.addListener(
      'onSynthesizerBufferCompletedEvent',
      (r) => {
        setBuffer(JSON.stringify(r));
      }
    );

    // 语音识别初始化
    Recognizer.init('0e7cfb2f');
    // 语音识别事件监听
    const recognizerEventEmitter = new NativeEventEmitter(Recognizer);
    recognizerEventEmitter.addListener(
      'onRecognizerResult',
      onRecognizerResult
    );
  }, []);

  /**
   * 识别结果
   * @param e
   */
  const onRecognizerResult = (e: any) => {
    // if (!e.isLast) {
    //   return;
    // }

    setResult(e.result);
  };
  /**
   * 播放
   */
  const synthesize = () => {
    Synthesizer.setParameter(SpeechConstant.VOICE_NAME, selectedLanguage);
    Synthesizer.start(value);
  };

  /**
   * 识别
   */
  const recognize = () => {
    Recognizer.start();
  };
  /**
   * 取消识别
   */
  const cancelRecognize = () => {
    Recognizer.cancel();
  };

  /**
   * 动态修正
   * https://www.xfyun.cn/doc/asr/voicedictation/Android-SDK.html#_3%E3%80%81%E5%8F%82%E6%95%B0%E8%AF%B4%E6%98%8E
   */
  const dwa = () => {
    Recognizer.setParameter(SpeechConstant.DWA_ON, 'wpgs');
  };
  /**
   * 取消动态修正
   */
  const cancelDwa = () => {
    Recognizer.setParameter(SpeechConstant.DWA_ON, '');
  };

  return (
    <View style={styles.container}>
      <View>
        <Picker
          style={styles.picker}
          selectedValue={selectedLanguage}
          onValueChange={(itemValue) => setSelectedLanguage(itemValue)}
        >
          <Picker.Item label="小萍" value="aisxping" />
          <Picker.Item label="小燕" value="xiaoyan" />
          <Picker.Item label="许久" value="aisjiuxu" />
          <Picker.Item label="小婧" value="aisjinger" />
          <Picker.Item label="许小宝" value="aisbabyxu" />
        </Picker>
        <TextInput
          editable
          multiline
          onChangeText={(text) => onChangeText(text)}
          value={value}
          style={styles.input}
        />
        <Button title="播放合成语音" onPress={synthesize} />
        <Text>onSynthesizerSpeakCompletedEvent: {complete}</Text>
        <Text>onSynthesizerBufferCompletedEvent: {buffer}</Text>
      </View>

      <Separator />
      <View>
        <View style={styles.fixToText}>
          <Button title="开始语音识别" color="#f194ff" onPress={recognize} />
          <Button
            title="取消语音识别"
            color="#f194ff"
            onPress={cancelRecognize}
          />
        </View>
        <View style={styles.fixToText}>
          <Button title="开启动态修正" color="blue" onPress={dwa} />
          <Button title="取消动态修正" color="#blue" onPress={cancelDwa} />
        </View>
        <Text>{result}</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  input: {
    margin: 12,
    borderWidth: 1,
    padding: 10,
  },
  fixToText: {
    marginTop: 5,
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  container: {
    flex: 1,
    marginHorizontal: 16,
    justifyContent: 'center',
  },
  picker: {
    borderWidth: 1,
    borderColor: 'black',
  },
  separator: {
    marginVertical: 8,
    borderBottomColor: '#737373',
    borderBottomWidth: 2,
  },
});
