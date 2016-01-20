/*
 * Copyright (c) 2012-2016, b3log.org & hacpai.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @fileoverview util and every page should be used.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Aug 25, 2015
 */

/**
 * @description Audio
 * @static
 */
var Audio = {
    availabel: false,
    wavFileBlob: null,
    recorderObj: null,
    /**
     * @description 初识化音频
     */
    init: function () {
        var detectGetUserMedia = new BrowserGetUserMediaDetection();

        //First, check to see if get user media is supported:
        console.log("Get user media supported: " + detectGetUserMedia.getUserMediaSupported());

        if (detectGetUserMedia.getUserMediaSupported()) {
            console.log("Get user media is supported!");
            console.log("Supported get user media method: " + detectGetUserMedia.getUserMediaMethod());

            console.log("Assigning get user media method.");
            navigator.getUserMedia = detectGetUserMedia.getUserMediaMethod();

            console.log("Requesting microphone access from browser.");
            navigator.getUserMedia({audio: true}, success, failure);
        } else {
            console.log("ERROR: getUserMedia not supported by browser.");

            alert('Your browser does not appear to support audio recording.');
        }

        //Get user media failure callback function:
        function failure(e) {
            console.log("getUserMedia->failure(): ERROR: Microphone access request failed!");

            var errorMessageToDisplay;
            var PERMISSION_DENIED_ERROR = "PermissionDeniedError";
            var DEVICES_NOT_FOUND_ERROR = "DevicesNotFoundError";

            console.log(e);
            console.log(e.name);

            switch (e.name) {
                case PERMISSION_DENIED_ERROR:
                    errorMessageToDisplay = Label.recordDeniedLabel;
                    break;
                case DEVICES_NOT_FOUND_ERROR:
                    errorMessageToDisplay = Label.recordDeviceNotFoundLabel;
                    break;
                default:
                    errorMessageToDisplay = 'ERROR: The following unexpected error occurred while attempting to connect to your microphone: ' + e.name;
                    break;
            }

            console.log("getUserMedia->failure(): " + errorMessageToDisplay);
            alert(errorMessageToDisplay);
        }

        //Get user media success callback function:
        function success(e) {
            console.log("getUserMedia->success(): Microphone access request was successful!");

            var BUFFER_SIZE = 2048;
            var RECORDING_MODE = PredefinedRecordingModes.MONO_5_KHZ; // 单声道 5kHz 最低的采样率
            var SAMPLE_RATE = RECORDING_MODE.getSampleRate();
            var OUTPUT_CHANNEL_COUNT = RECORDING_MODE.getChannelCount();

            console.log("getUserMedia->success(): Detecting window audio context.");
            var detectWindowAudioContext = new BrowserWindowAudioContextDetection();

            if (detectWindowAudioContext.windowAudioContextSupported()) {
                console.log("getUserMedia->success(): Window audio context supported.");

                var windowAudioContext = detectWindowAudioContext.getWindowAudioContextMethod();

                console.log("getUserMedia->success(): Window audio context method: " + windowAudioContext);

                console.log('getUserMedia->success(): Creating recorder object.');

                Audio.recorderObj = new SoundRecorder(windowAudioContext, BUFFER_SIZE, SAMPLE_RATE, OUTPUT_CHANNEL_COUNT);

                console.log('getUserMedia->success(): Initializing recorder object.');
                Audio.recorderObj.init(e);

                console.log('getUserMedia->success(): Assigning onaudioprocess event function.');

                Audio.recorderObj.recorder.onaudioprocess = function (e)
                {
                    //Do nothing if not recording:
                    if (!Audio.recorderObj.isRecording()) {
                        return;
                    }
                    
                    // Copy the data from the input buffers;
                    var left = e.inputBuffer.getChannelData(0);
                    var right = e.inputBuffer.getChannelData(1);
                    Audio.recorderObj.cloneChannelData(left, right);
                    console.log('SoundRecorder.recorder.onaudioprocess: Saving audio data...');
                };

                console.log('getUserMedia->success(): Recorder object successfully created and initialized.');
                console.log('getUserMedia->success(): Recorder object ready status: ' + Audio.recorderObj.isReady());

                Audio.availabel = true;
            } else {
                var messageString = "Unable to detect window audio context, cannot continue.";
                console.log("getUserMedia->success(): " + messageString);
                alert(messageString);

                return;
            }
        }
    },
    /**
     * @description 开始录音
     */
    handleStartRecording: function () {
        console.log("Starting new recording...");
        Audio.recorderObj.startRecordingNewWavFile();
    },
    /**
     * @description 结束录音
     */
    handleStopRecording: function () {
        console.log("Stopping recording.");
        Audio.recorderObj.stopRecording();

        //Save the recording by building the wav file blob and send it to the client:
        console.log("Building wav file.");
        Audio.wavFileBlob = Audio.recorderObj.buildWavFileBlob();
    }
};
