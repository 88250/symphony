/**
 * Author:  Robert Derveloy
 * Written: 2012-12-01
 * 
 * Copyright (c) 2012-2015 -  Robert Derveloy
 * 
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/4.0/.
 *
 * Requirements:
 *  jQuery
 */

/**
* RecordingMode: A RecordingMode is considered to be a combination of the number
* of audio channels and the sampling rate.
*/
function RecordingMode(desiredSampleRate, desiredChannelCount)
{
    //private variables
    var SAMPLE_RATE   = desiredSampleRate;
    var CHANNEL_COUNT = desiredChannelCount;
              
    //Publicly accessible methods:
    this.getSampleRate = function()
    {
        return SAMPLE_RATE;    
    };
    
    this.getChannelCount = function()
    {
        return CHANNEL_COUNT;    
    };
};

var PredefinedChannelCounts=
{
    MONO    : 1
    ,STEREO : 2
};

var PredefinedSamplingRates=
{
    SAMPLE_RATE_48_KHZ      : 48000   //Browser default for chrome and firefox.
    ,SAMPLE_RATE_44_1_KHZ   : 44100   //CD Audio Quality
    ,SAMPLE_RATE_32_KHZ     : 32000   //Digital Audio Quality
    ,SAMPLE_RATE_22_05_KHZ  : 22050   //FM Radio Audio Quality
    ,SAMPLE_RATE_11_025_KHZ : 11025   //Average Mono Voice Quality ??
    ,SAMPLE_RATE_8_KHZ      : 8000    //AM Radio Audio Quality
    ,SAMPLE_RATE_5_KHZ      : 5000    //Old Telephone
};

var PredefinedRecordingModes=
{
    MONO_48_KHZ  : new RecordingMode(PredefinedSamplingRates.SAMPLE_RATE_48_KHZ,PredefinedChannelCounts.MONO)
    ,MONO_44_KHZ : new RecordingMode(PredefinedSamplingRates.SAMPLE_RATE_44_1_KHZ,PredefinedChannelCounts.MONO)
    ,MONO_32_KHZ : new RecordingMode(PredefinedSamplingRates.SAMPLE_RATE_32_KHZ,PredefinedChannelCounts.MONO)
    ,MONO_22_KHZ : new RecordingMode(PredefinedSamplingRates.SAMPLE_RATE_22_05_KHZ,PredefinedChannelCounts.MONO)
    ,MONO_11_KHZ : new RecordingMode(PredefinedSamplingRates.SAMPLE_RATE_11_025_KHZ,PredefinedChannelCounts.MONO)
    ,MONO_8_KHZ  : new RecordingMode(PredefinedSamplingRates.SAMPLE_RATE_8_KHZ,PredefinedChannelCounts.MONO)
    ,MONO_5_KHZ  : new RecordingMode(PredefinedSamplingRates.SAMPLE_RATE_5_KHZ,PredefinedChannelCounts.MONO)
    
    ,STEREO_48_KHZ : new RecordingMode(PredefinedSamplingRates.SAMPLE_RATE_48_KHZ,PredefinedChannelCounts.STEREO)
    ,STEREO_44_KHZ : new RecordingMode(PredefinedSamplingRates.SAMPLE_RATE_44_1_KHZ,PredefinedChannelCounts.STEREO)
    ,STEREO_32_KHZ : new RecordingMode(PredefinedSamplingRates.SAMPLE_RATE_32_KHZ,PredefinedChannelCounts.STEREO)
    ,STEREO_22_KHZ : new RecordingMode(PredefinedSamplingRates.SAMPLE_RATE_22_05_KHZ,PredefinedChannelCounts.STEREO)
    ,STEREO_11_KHZ : new RecordingMode(PredefinedSamplingRates.SAMPLE_RATE_11_025_KHZ,PredefinedChannelCounts.STEREO)
    ,STEREO_8_KHZ  : new RecordingMode(PredefinedSamplingRates.SAMPLE_RATE_8_KHZ,PredefinedChannelCounts.STEREO)
    ,STEREO_5_KHZ  : new RecordingMode(PredefinedSamplingRates.SAMPLE_RATE_5_KHZ,PredefinedChannelCounts.STEREO)
};


if (!Date.now)
{
    Date.now = function() { return new Date().getTime(); };
}

function WavFileBlob(desiredDataView)
{
    //TODO:  Make these private:
    this.dataBlob = new Blob ( [ desiredDataView ], { type : 'audio/wav' } );
    this.NAME_PREFIX = 'output';    
    this.EXTENSION = '.wav';
    
    //Public methods:
    this.generateFileName = function()
    {
        return this.generateFileNameWithoutExtension() + this.EXTENSION;
    };
    
    this.generateFileNameWithoutExtension = function()
    {
        return this.NAME_PREFIX + Date.now();
    };
    
    this.getDataBlob = function()
    {
        return this.dataBlob;
    };
    
    this.downloadLocally = function()
    {   

        var url  = (window.URL || window.webkitURL).createObjectURL(this.dataBlob);
		var fileName = this.generateFileName();
//		console.log("WavFileBlob->downloadLocally(): The URL is: "+url);
//		console.log("WavFileBlob->downloadLocally(): The file name is: "+url);

        var link = window.document.createElement('a');
		
		
        link.href = url;
        link.download = fileName;
		link.target = "_blank";
		//link.click(); //This does not work in firefox.
		
		//This doesn't work with firefox either:
		/*
        var click = document.createEvent("Event");
        click.initEvent("click", true, true);
        link.dispatchEvent(click);
		*/
		
		//Firefox is special:
		//See: https://developer.mozilla.org/en-US/docs/Web/API/MouseEvent/initMouseEvent
		var EVENT_TYPE           = 'MouseEvents';
		var EVENT_NAME           = 'click';
		var CAN_BUBBLE           = true;
		var CANCELABLE           = true;
		var VIEW                 = document.defaultView;
		var DETAIL               = 1;
		var SCREEN_X             = 0;
		var SCREEN_Y             = 0;
		var CLIENT_X             = 0;
		var CLIENT_Y             = 0;
		var CTRL_KEY_PRESSED     = false;
		var ALT_KEY_PRESSED      = false;
		var SHIFT_KEY_PRESSED    = false;
		var META_KEY_PRESSED     = false;
		var MOUSE_BUTTON         = 0;
		var RELATED_EVENT_TARGET = null;
		
		var event = document.createEvent(EVENT_TYPE);
		event.initMouseEvent(EVENT_NAME, CAN_BUBBLE, CANCELABLE, VIEW, DETAIL, SCREEN_X, SCREEN_Y, CLIENT_X, CLIENT_Y, CTRL_KEY_PRESSED, ALT_KEY_PRESSED, SHIFT_KEY_PRESSED, META_KEY_PRESSED, MOUSE_BUTTON, RELATED_EVENT_TARGET);
		link.dispatchEvent(event);
		
    };
    
    this.sendToURL = function(desiredURL, desiredSuccessCallback, desiredFailureCallback, desiredProgressBarValueSetterCallback)
    {
//        console.log("WavFileBlob.sendToUrl(): Checking required values...");
        if(!desiredURL)
        {
//            console.log("WavFileBlob.sendToUrl(): Desired url is null.  Cannot continue.");
            return;
        }
        
        if(typeof(desiredURL)!='string')
        {
//            console.log("WavFileBlob.sendToUrl(): Desired url is not a string.  Cannot continue.");
            return;
        }
        
        if(desiredURL==="")
        {
//            console.log("WavFileBlob.sendToUrl(): Desired url is an empty string.  Cannot continue.");
            return;
        }
        
//        console.log("WavFileBlob.sendToUrl(): ...Finished!");
                                       
        var url = (window.URL || window.webkitURL).createObjectURL(this.dataBlob);
        
        var data = new FormData();
        data.append('file', this.dataBlob);
        data.append('file_name',this.generateFileNameWithoutExtension());
        data.append('file_extension',this.EXTENSION);
        
//        console.log("WavFileBlob.sendToUrl(): Sending AJAX query...");
        $.ajax({
            url :  desiredURL,
            type: 'POST',
            data: data,
            contentType: false,
            processData: false,
            success: function(data) {
//                console.log("WavFileBlob.sendToUrl()->success(): Send to server successful!");

                if(desiredSuccessCallback)
                {
//                    console.log("WavFileBlob.sendToUrl()->success(): Executing success callback function.");
                	desiredSuccessCallback(data);
                }
                else
                {
//                    console.log("WavFileBlob.sendToUrl()->success(): No success callback function specified.");
                }

            },    
            error: function(e) {
//                console.log("WavFileBlob.sendToUrl()->error(): Send to server unsuccessful!");
//                console.log(e);
                
                if(desiredFailureCallback)
                {
//                    console.log("WavFileBlob.sendToUrl()->error(): Executing failure callback function.");
                	desiredFailureCallback(e);
                }
                else
                {
//                    console.log("WavFileBlob.sendToUrl()->error(): No failure callback function specified.");
                }
                
            },
            xhr: function()
            {
                var xhr = new window.XMLHttpRequest();
                //Upload progress
                xhr.upload.addEventListener("progress", function(evt){
                    if (evt.lengthComputable) {
                        var percentComplete = evt.loaded / evt.total;
                        //Do something with upload progress

                        if(desiredProgressBarValueSetterCallback)
                        {
                            desiredProgressBarValueSetterCallback(percentComplete);
                        }

//                        console.log("Upload Raw Percentage: " + percentComplete);
                    }
                }, false);
                //Download progress
                xhr.addEventListener("progress", function(evt){
                    if (evt.lengthComputable) {
                        var percentComplete = evt.loaded / evt.total;
                        //Do something with download progress
                        if(desiredProgressBarValueSetterCallback)
                        {
                            desiredProgressBarValueSetterCallback(percentComplete);
                        }

//                        console.log("Download Raw Percentage: " + percentComplete);
                    }
                }, false);
                return xhr;
            }
        });  
    };
}

function SoundRecorder(desiredAudioContext, desiredBufferSize, desiredSampleRate, desiredOutputChannelCount)
{
    //Private constants:
    var BUFFER_SIZE          = desiredBufferSize;  //2048 suggested by demos
    var SAMPLE_RATE          = desiredSampleRate; //44100 suggested by demos;
    var DEFAULT_SAMPLE_RATE  = null;
    var INPUT_CHANNEL_COUNT  = 2;
    var OUTPUT_CHANNEL_COUNT = desiredOutputChannelCount;

    //Private variables:
    var recordingFlag        = false;
    var readyFlag            = false;
    var AudioContext         = desiredAudioContext;
    var context              = null;
    var volume               = null;
    var audioInput           = null;
    var leftChannel          = [];
    var rightChannel         = [];
    var recordingLength      = 0;
        
    //Publicly accessible variables:
    this.recorder = null; //This needs to be public so the 'onaudioprocess' event handler can be defined externally.    
    
    //Publicly accessible methods:
    this.cloneChannelData = function(leftChannelData, rightChannelData)
    {
        leftChannel.push (new Float32Array (leftChannelData));
        rightChannel.push (new Float32Array (rightChannelData));
        recordingLength += BUFFER_SIZE;    
    };
    
    this.init = function(e)
    {	
//        console.log('SoundRecorder.init(): Internal recorder initialization started.');
        readyFlag = false;
        
        if(isNaN(SAMPLE_RATE))
        {
            return;
        }
        
        if(isNaN(BUFFER_SIZE))
        {
            return;
        }
        
        if(!AudioContext)
        {
        	
            return;
        }
        
        // creates the audio context
        context = new AudioContext();
        DEFAULT_SAMPLE_RATE=context.sampleRate;    
//        console.log("SoundRecorder.init(): The browser's default sample rate is: "+DEFAULT_SAMPLE_RATE);
//        console.log("SoundRecorder.init(): The desired sample rate is: "+SAMPLE_RATE);
        
        // creates a gain node
        volume = context.createGain();
    
        // creates an audio node from the microphone incoming stream
        audioInput = context.createMediaStreamSource(e);
    
        // connect the stream to the gain node
        audioInput.connect(volume);
                
        /* From the spec: The size of the buffer controls how frequently the audioprocess event is 
        dispatched and how many sample-frames need to be processed each call. 
        Lower values for buffer size will result in a lower (better) latency. 
        Higher values will be necessary to avoid audio breakup and glitches */  
        this.recorder = context.createScriptProcessor(BUFFER_SIZE, INPUT_CHANNEL_COUNT, OUTPUT_CHANNEL_COUNT);             
    
        //The onaudioprocess event needs to be defined externally, so make sure it is not set:
        this.recorder.onaudioprocess = null;
                       
        // we connect the recorder
        volume.connect (this.recorder);
        this.recorder.connect (context.destination);      

        readyFlag = true;
//        console.log('SoundRecorder.init(): Internal recorder initialization finished!');
//        console.log('SoundRecorder.init(): Recorder should be ready so long as the onaudioprocess event has been set.');
        
    };
    
    this.isRecording = function()
    {        
        return (recordingFlag===true);
    };
    
    this.startRecordingNewWavFile = function()
    {
        if(this.isReady()===true)
        {
            setRecordingFlag();
            resetBuffers();        
        }
        else
        {
//            console.log("SoundRecorder.startRecordingNewWavFile(): Recorder object not ready!");
        }
    };
    
    this.isReady = function()
    {
        
        var eventSet = false;
        if(this.recorder.onaudioprocess)
        {
            eventSet = true;
        }
        else
        {            
            eventSet = false;
//            console.log("SoundRecorder.isReady(): Recorder onaudioprocess event not set!");
        }
        
        return (readyFlag && eventSet);
    };
    
    this.stopRecording = function()
    {
        clearRecordingFlag();
    };
    
    this.buildWavFileBlob = function()
    {
//    	console.log("SoundRecorder.buildWavFileBlob(): Building WAV file...");
    	var BITS_PER_BYTE      = 8;
    	var BYTES_PER_SAMPLE   = 2;
        var NUMBER_OF_CHANNELS = (OUTPUT_CHANNEL_COUNT==1)?1:2;
        var DATA_RATE          = (SAMPLE_RATE * BYTES_PER_SAMPLE * NUMBER_OF_CHANNELS);
        var BLOCK_ALIGN        = (BYTES_PER_SAMPLE * NUMBER_OF_CHANNELS);
        var BITS_PER_SAMPLE    = (BYTES_PER_SAMPLE * BITS_PER_BYTE);

        //Index variables.
        //See http://stackoverflow.com/questions/13039846/what-do-the-bytes-in-a-wav-file-represent
        var BYTE_INDEX_CHUNK_ID        = 0;
        var BYTE_INDEX_CHUNK_SIZE      = 4;
        var BYTE_INDEX_FORMAT          = 8;
        var BYTE_INDEX_SUBCHUNK_1_ID   = 12;
        var BYTE_INDEX_SUBCHUNK_1_SIZE = 16;
        var BYTE_INDEX_AUDIO_FORMAT    = 20;
        var BYTE_INDEX_NUM_CHANNELS    = 22;
        var BYTE_INDEX_SAMPLE_RATE     = 24;
        var BYTE_INDEX_BYTE_RATE       = 28;
        var BYTE_INDEX_BLOCK_ALIGN     = 32;
        var BYTE_INDEX_BITS_PER_SAMPLE = 34;
        var BYTE_INDEX_SUBCHUNK_2_ID   = 36;
        var BYTE_INDEX_SUBCHUNK_2_SIZE = 40;
        var BYTE_INDEX_DATA            = 44;

        //Header data values:
        var SUBCHUNK_1_SIZE   = 16;
        var AUDIO_FORMAT      = 1;
        var HEADER_BYTE_COUNT = 44;  //The first 44 bytes are a standard RIFF header.
        var CHUNK_ID_VALUE    = 'RIFF';
        var FORMAT_VALUE      = 'WAVE';


    	// console.log("SoundRecorder.buildWavFileBlob(): Flattening channels...");
        // we flat the left and right channels down
        var leftBuffer  = mergeBuffers (leftChannel);
        var rightBuffer = mergeBuffers (rightChannel);
        // console.log("SoundRecorder.buildWavFileBlob(): ...Finished!");


        // Interleave the left and right channels together:
        // console.log("SoundRecorder.buildWavFileBlob(): Interleaving channels...");
        // console.log("SoundRecorder.buildWavFileBlob(): ...The desired number of output channels is "+OUTPUT_CHANNEL_COUNT);
        var interleaved = (OUTPUT_CHANNEL_COUNT==1)?interleaveToMono ( leftBuffer, rightBuffer ):interleaveToStereo ( leftBuffer, rightBuffer );
        // console.log("SoundRecorder.buildWavFileBlob(): ...Finished!");

        //Downsample the audio data if necessary:
        // console.log("SoundRecorder.buildWavFileBlob(): Determining if downsampling is necessary...");
        if(DEFAULT_SAMPLE_RATE > SAMPLE_RATE)
        {
        	// console.log("SoundRecorder.buildWavFileBlob(): ...The desired sample rate is less than the default sample rate.  Downsampling needed.");
			interleaved = downsampleBuffer(interleaved,SAMPLE_RATE);
			// console.log("SoundRecorder.buildWavFileBlob(): ...Downsampling Finished!");
		}
		else
		{
			// console.log("SoundRecorder.buildWavFileBlob(): ...The desired sample rate is greater than or equal to the default sample rate.  No downsampling needed.");
		}

        // Now that the data is ready, build the wav file:
        // See:
        // http://stackoverflow.com/questions/13039846/what-do-the-bytes-in-a-wav-file-represent
        // http://www-mmsp.ece.mcgill.ca/documents/AudioFormats/WAVE/WAVE.html

        var totalByteCount = (HEADER_BYTE_COUNT + interleaved.length * 2);
        var buffer         = new ArrayBuffer(totalByteCount);
        var view           = new DataView(buffer);

        // Build the RIFF chunk descriptor:
        // console.log("SoundRecorder.buildWavFileBlob(): Writing RIFF chunk descriptor to  view...");

        writeUTFBytes(view, BYTE_INDEX_CHUNK_ID, CHUNK_ID_VALUE);
        view.setUint32(BYTE_INDEX_CHUNK_SIZE, totalByteCount, true);
        writeUTFBytes(view, BYTE_INDEX_FORMAT, FORMAT_VALUE);

        // console.log("SoundRecorder.buildWavFileBlob(): ...Finished!");

        // Build the FMT sub-chunk:
        // console.log("SoundRecorder.buildWavFileBlob(): Writing FMT sub-chunk to  view...");

        writeUTFBytes(view, BYTE_INDEX_SUBCHUNK_1_ID, 'fmt '); //subchunk1 ID is format
        view.setUint32(BYTE_INDEX_SUBCHUNK_1_SIZE, SUBCHUNK_1_SIZE,    true);//The sub-chunk size is 16.
        view.setUint16(BYTE_INDEX_AUDIO_FORMAT,    AUDIO_FORMAT,       true);//The audio format is 1.
        view.setUint16(BYTE_INDEX_NUM_CHANNELS,    NUMBER_OF_CHANNELS, true);//Number of interleaved channels.
        view.setUint32(BYTE_INDEX_SAMPLE_RATE,     SAMPLE_RATE,        true);//Sample rate.
        view.setUint32(BYTE_INDEX_BYTE_RATE,       DATA_RATE,          true);//Byte rate.
        view.setUint16(BYTE_INDEX_BLOCK_ALIGN,     BLOCK_ALIGN,        true);//Block align
        view.setUint16(BYTE_INDEX_BITS_PER_SAMPLE, BITS_PER_SAMPLE,    true);//Bits per sample.

        // console.log("SoundRecorder.buildWavFileBlob(): ...Finished!");


        // Build the data sub-chunk:
        // console.log("SoundRecorder.buildWavFileBlob(): Writing data sub-chunk to view...");

        var subChunk2ByteCount = interleaved.length * 2;
        writeUTFBytes(view, BYTE_INDEX_SUBCHUNK_2_ID, 'data');
        view.setUint32(BYTE_INDEX_SUBCHUNK_2_SIZE, subChunk2ByteCount, true);
        
        // Write the PCM samples to the view:
        var lng = interleaved.length;
        var index = BYTE_INDEX_DATA;
        var volume = 1;
        for (var i = 0; i < lng; i++)
        {
            view.setInt16(index, interleaved[i] * (0x7FFF * volume), true);
            index += 2;
        }

        // console.log("SoundRecorder.buildWavFileBlob(): ...Finished!");
        
        // console.log("SoundRecorder.buildWavFileBlob(): Converting view to blob...");

        // our final binary blob
        var blob = new WavFileBlob(view);

        // console.log("SoundRecorder.buildWavFileBlob(): ...Finished!");

        return blob;
    };
    
    this.recordingEventHandler = function(e)
    {            
        //Do nothing if not recording:
        if (!this.isRecording()) return;                      
        // Copy the data from the input buffers;
        var left = e.inputBuffer.getChannelData (0);
        var right = e.inputBuffer.getChannelData (1);
        this.cloneChannelData(left, right);            
        // console.log("SoundRecorder.recordingEventHandler(): Saving audio data...");
    };
    
    //Private methods:    
    var interleaveToStereo = function (leftBuffer, rightBuffer)
    {
    	// console.log("SoundRecorder.interleaveToStereo(): Begin interleaving stereo channels...");
        var length = leftBuffer.length + rightBuffer.length;
        var result = new Float32Array(length);
        var inputIndex = 0;
    
        for (var index = 0; index < length; )
        {
            result[index++] = leftBuffer[inputIndex];
            result[index++] = rightBuffer[inputIndex];
            inputIndex++;
        }
        // console.log("SoundRecorder.interleaveToStereo(): ...Finished!");
        return result;
    };
    
    var interleaveToMono = function(leftBuffer, rightBuffer)
    {
    	// console.log("SoundRecorder.interleaveToStereo(): Begin interleaving stereo channels to mono...");
        var result = new Float32Array(leftBuffer.length);
        
        for (var index = 0; index < leftBuffer.length; ++index)
        {
           result[index] = 0.5 * (leftBuffer[index] + rightBuffer[index]);
        }
        // console.log("SoundRecorder.interleaveToStereo(): ...Finished!");
        return result;
    };
    
    var downsampleBuffer = function (buffer, rate)
    {
    	// console.log("SoundRecorder.downsampleBuffer(): Begin downsampling audio...");
        if (rate == DEFAULT_SAMPLE_RATE)
        {
        	// console.log("SoundRecorder.downsampleBuffer(): ...The new rate is the same as the browser's default rate.  No need to downsample.");
            // console.log("SoundRecorder.downsampleBuffer(): ...Finished!");
            return buffer;
            
        }
        
        if (rate > DEFAULT_SAMPLE_RATE)
        {
        	
            //throw "downsampling rate show be smaller than original sample rate";
            // console.log("SoundRecorder.downsampleBuffer(): ...The new rate is greater than the browser's default rate.  No need to downsample.");
            // console.log("SoundRecorder.downsampleBuffer(): ...Finished!");
            return buffer;
        }
        
        
        // console.log("SoundRecorder.downsampleBuffer(): ...The desired rate is less than the browser's default rate.  Downsampling...");
        var sampleRateRatio = DEFAULT_SAMPLE_RATE / rate;
        var newLength = Math.round(buffer.length / sampleRateRatio);
        var result = new Float32Array(newLength);
        var offsetResult = 0;
        var offsetBuffer = 0;
        
        while (offsetResult < result.length)
        {
            var nextOffsetBuffer = Math.round((offsetResult + 1) * sampleRateRatio);
            var accum = 0, count = 0;
            for (var i = offsetBuffer; i < nextOffsetBuffer && i < buffer.length; i++)
            {
               accum += buffer[i];
               count++;
            }
            result[offsetResult] = accum / count;
            offsetResult++;
            offsetBuffer = nextOffsetBuffer;
        }
        
        // console.log("SoundRecorder.downsampleBuffer(): ...Finished!");
        return result;
    };
    
    var mergeBuffers = function(desiredChannelBuffer)
    {
    	// console.log("SoundRecorder.mergeBuffers(): Merging buffers...");
        var result = new Float32Array(recordingLength);
        var offset = 0;
        var lng = desiredChannelBuffer.length;
        for (var i = 0; i < lng; ++i)
        {
            var buffer = desiredChannelBuffer[i];
            result.set(buffer, offset);
            offset += buffer.length;
        }
        // console.log("SoundRecorder.mergeBuffers(): ...Finished!");
        return result;
    };
    
    var writeUTFBytes = function(view, offset, string)
    { 
    	// console.log("SoundRecorder.writeUTFBytes(): Writing UTF bytes to view...");
		var lng = string.length;
		for (var i = 0; i < lng; i++)
		{
			view.setUint8(offset + i, string.charCodeAt(i));
		}
		// console.log("SoundRecorder.writeUTFBytes(): ...Finished!");
    };    
    
    var setRecordingFlag = function()
    {
    	// console.log("SoundRecorder.setRecordingFlag(): Setting recording flag...");
        recordingFlag = true;
        // console.log("SoundRecorder.setRecordingFlag(): ...Finished!");
    };
    
    var clearRecordingFlag = function()
    {
    	// console.log("SoundRecorder.clearRecordingFlag(): Clearing recording flag...");
        recordingFlag = false;
        // console.log("SoundRecorder.clearRecordingFlag(): ...Finished!");
    };
    
    var resetBuffers = function()
    {
    	// console.log("SoundRecorder.resetBuffers(): Resetting buffers...");
        leftChannel.length = rightChannel.length = 0;
        recordingLength = 0;
        // console.log("SoundRecorder.resetBuffers(): ...Finished!");
    };
}

function BrowserWindowAudioContextDetection()
{   
    //Private variables:
    var windowAudioContextMethodPointer = window.AudioContext || window.webkitAudioContext;
    
    //Public methods:
    this.windowAudioContextSupported = function()
    {
        return ((windowAudioContextMethodPointer)?true:false);
    };
    
    this.getWindowAudioContextMethod = function()
    {
        return windowAudioContextMethodPointer;
    };
    
}

function BrowserGetUserMediaDetection()
{
    

    //Private variables:
    var getUserMediaMethodPointer = (navigator.getUserMedia = navigator.getUserMedia || navigator.webkitGetUserMedia || navigator.mozGetUserMedia || navigator.msGetUserMedia);
    
    //Public functions:
    this.getUserMediaSupported = function()
    {
        return ((getUserMediaMethodPointer)?true:false);
    };
    
    this.getUserMediaMethod = function()
    {
        return getUserMediaMethodPointer;
    };
}
