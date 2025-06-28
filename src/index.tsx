import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-iflytek-asr-tts' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const IflytekAsrTts = NativeModules.IflytekAsrTts
  ? NativeModules.IflytekAsrTts
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

function multiply(a: number, b: number): Promise<number> {
  return IflytekAsrTts.multiply(a, b);
}

const SpeechConstant = NativeModules.SpeechConstantModule;

const Recognizer = NativeModules.SpeechRecognizerModule;

const Synthesizer = NativeModules.SpeechSynthesizerModule;

export { multiply, SpeechConstant, Recognizer, Synthesizer };
