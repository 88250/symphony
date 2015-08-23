<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${symphonyLabel} - ${addArticleLabel}">
        <meta name="robots" content="none" />
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/home${miniPostfix}.css?${staticResourceVersion}" />
        <link type="text/css" rel="stylesheet" href="${staticServePath}/js/lib/codemirror-5.3/codemirror.css" />
        <link type="text/css" rel="stylesheet" href="${staticServePath}/js/lib/codemirror-5.3/addon/display/fullscreen.css" />
        <link type="text/css" rel="stylesheet" href="${staticServePath}/js/lib/codemirror-5.3/addon/hint/show-hint.css" />
        <link type="text/css" rel="stylesheet" href="${staticServePath}/js/lib/highlight.js-8.6/styles/github.css">
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper post">
                <div class="form fn-flex-1">
                    <div>
                        <input type="text" id="articleTitle" value="<#if article??>${article.articleTitle}</#if>" placeholder="${titleLabel}" />
                        <span style="right:2px;top:4px;"></span>
                    </div>
                    <div class="fn-clear">
                        <label class="article-content-label">
                            Markdown
                            <a href="javascript:AddArticle.grammar()">${baseGrammarLabel}</a>
                            <a target="_blank" href="http://daringfireball.net/projects/markdown/syntax">${allGrammarLabel}</a>
                            |
                            <a target="_blank" href="http://www.emoji-cheat-sheet.com">Emoji</a>
                        </label>
                    </div>
                    <div class="fn-clear article-content">
                        <form class="fn-none" id="fileUpload" method="POST" enctype="multipart/form-data">
                            <input type="file" name="file">
                        </form>
                        <textarea id="articleContent" placeholder="${addArticleEditorPlaceholderLabel}"><#if article??>${article.articleContent}</#if></textarea>
                        <span id="articleContentTip" style="top: 304px; right: 2px;"></span>
                        <div class="fn-left grammar fn-none">
                            ${markdwonGrammarLabel}
                        </div>
                    </div>
                    <div>
                        <input id="articleTags" type="text" value="<#if article??>${article.articleTags}<#else>${tags}</#if>" placeholder="${tagLabel}（${tagSeparatorTipLabel}）"/>
                        <span style="right:2px;top:424px;"></span><br/><br/>
                    </div>
                    <div class="fn-clear article-reward-content">
                        <form class="fn-none" id="rewardFileUpload" method="POST" enctype="multipart/form-data">
                            <input type="file" name="file">
                        </form>
                        <textarea id="articleRewardContent" placeholder="${rewardEditorPlaceholderLabel}"><#if article??>${article.articleRewardContent}</#if></textarea>
                        <span id="articleRewardContentTip" style="top: 304px; right: 2px;"></span>
                    </div>
                    <div>
                        <input id="articleRewardPoint" type="text" value="<#if article?? && 0 != article.articleRewardPoint>${article.articleRewardPoint}</#if>" placeholder="${rewardPointLabel}" <#if article?? && 0 < article.articleRewardPoint>readonly="readonly"</#if>/>
                               <span style="right:2px;top:424px;"></span><br/><br/>
                    </div>
                    <div class="fn-clear">
                        <div class="fn-left">
                            <input<#if article??> disabled="disabled"<#if article.syncWithSymphonyClient> checked="checked"</#if></#if> type="checkbox" id="syncWithSymphonyClient"/> 
                                ${syncWithSymphonyClientLabel}
                        </div>
                        <div class="fn-left"> &nbsp;
                            <input<#if article??><#if article.articleCommentable> checked="checked"</#if><#else> checked="checked"</#if> type="checkbox" id="articleCommentable"/> 
                                ${commentableLabel}
                        </div>
                        <div class="fn-left"> &nbsp;
                            <input<#if article?? && 1 == article.articleType> checked="checked"</#if> type="checkbox" id="articleType"/> 
                                ${discussionLabel}
                        </div>
                        <div class="fn-right">
                            <button class="green" onclick="AddArticle.preview()">${previewLabel}</button> &nbsp; &nbsp; 
                            <button class="red" onclick="AddArticle.add(<#if article??>'${article.oId}'</#if>)"><#if article??>${editLabel}<#else>${postLabel}</#if></button>
                        </div>
                    </div>
                    <div id="addArticleTip" style="bottom: 70px;right: 137px;"></div>
                    ${postGuideLabel}
                </div>
            </div>
        </div>
        <div id="preview" class="content-reset"></div>
        <#include "../footer.ftl">
        <script src="${staticServePath}/js/lib/jquery/jquery.bowknot.min.js"></script>
        <script src="${staticServePath}/js/lib/codemirror-5.3/codemirror.js"></script>
        <script src="${staticServePath}/js/lib/codemirror-5.3/mode/markdown/markdown.js"></script>
        <script src="${staticServePath}/js/lib/codemirror-5.3/addon/display/placeholder.js"></script>
        <script src="${staticServePath}/js/lib/codemirror-5.3/addon/display/fullscreen.js?"></script>
        <script src="${staticServePath}/js/overwrite/codemirror/addon/hint/show-hint.js"></script>
        <script type="text/javascript" src="${staticServePath}/js/lib/highlight.js-8.6/highlight.pack.js"></script>
        <script type="text/javascript" src="${staticServePath}/js/lib/jquery/file-upload-9.10.1/vendor/jquery.ui.widget.js"></script>
        <script type="text/javascript" src="${staticServePath}/js/lib/jquery/file-upload-9.10.1/jquery.iframe-transport.js"></script>
        <script type="text/javascript" src="${staticServePath}/js/lib/jquery/file-upload-9.10.1/jquery.fileupload.js"></script>
        <script type="text/javascript" src="${staticServePath}/js/lib/jquery/file-upload-9.10.1/jquery.fileupload-process.js"></script>
        <script type="text/javascript" src="${staticServePath}/js/lib/sound-recorder/SoundRecorder.js"></script>
        <script src="${staticServePath}/js/add-article${miniPostfix}.js?${staticResourceVersion}"></script>

        <script>
                                                Label.articleTitleErrorLabel = "${articleTitleErrorLabel}";
                                                Label.articleContentErrorLabel = "${articleContentErrorLabel}";
                                                Label.tagsErrorLabel = "${tagsErrorLabel}";
                                                Label.userName = "${userName}";
                                                Util.uploadFile({
                                                        "id": "fileUpload",
                                                        "pasteZone": $("#articleContent").next(),
                                                        "qiniuUploadToken": "${qiniuUploadToken}",
                                                        "editor": AddArticle.editor,
                                                        "uploadingLabel": "${uploadingLabel}",
                                                        "qiniuDomain": "${qiniuDomain}"
                                                });
                                                Util.uploadFile({
                                                        "id": "rewardFileUpload",
                                                        "pasteZone": $("#articleRewardContent").next(),
                                                        "qiniuUploadToken": "${qiniuUploadToken}",
                                                        "editor": AddArticle.rewardEditor,
                                                        "uploadingLabel": "${uploadingLabel}",
                                                        "qiniuDomain": "${qiniuDomain}"
                                                });
                                                $('#articleTitle').focus();
        </script>
        <script>
            var qiniuToken = '${qiniuUploadToken}';
            var qiniuDomain = '${qiniuDomain}';
            var audioRecordingLabel = '${audioRecordingLabel}';
            var uploadingLabel = '${uploadingLabel}';
            
			// variables
			var wavFileBlob = null;
			var recorderObj = null;
			var scriptProcessor = null;										
			var detectGetUserMedia = new BrowserGetUserMediaDetection();
			
			//First, check to see if get user media is supported:
			console.log("Get user media supported: " + detectGetUserMedia.getUserMediaSupported());
															
			if(detectGetUserMedia.getUserMediaSupported())
			{   
				console.log("Get user media is supported!");
				console.log("Supported get user media method: " + detectGetUserMedia.getUserMediaMethod());
				
				console.log("Assigning get user media method.");
				navigator.getUserMedia = detectGetUserMedia.getUserMediaMethod();
				
				console.log("Requesting microphone access from browser.");
				navigator.getUserMedia({audio:true}, success, failure);
				
				//document.getElementById("div_RecordingNotSupported").style.display="none"; // hide recording not supported message
				//document.getElementById("button_startRecording").style.display="block"; // show start recording button
					
			}
			else
			{
				console.log("ERROR: getUserMedia not supported by browser.");
								
				alert('Your browser does not appear to support audio recording.');
			}
			
			
			//Get user media failure callback function:
			function failure(e)
			{
				console.log("getUserMedia->failure(): ERROR: Microphone access request failed!");												    												    
				
				var errorMessageToDisplay;
				var PERMISSION_DENIED_ERROR = "PermissionDeniedError";
				var DEVICES_NOT_FOUND_ERROR = "DevicesNotFoundError"; 
				
				console.log(e);
				console.log(e.name);
			   
				switch(e.name)
				{
					case PERMISSION_DENIED_ERROR:
						errorMessageToDisplay = '${recordDeniedLabel}';
						break;
					case DEVICES_NOT_FOUND_ERROR:
						errorMessageToDisplay = '${recordDeviceNotFoundLabel}';
						break;
					default:
						errorMessageToDisplay = 'ERROR: The following unexpected error occurred while attempting to connect to your microphone: '+e.name;
						break;                
				}
				console.log("getUserMedia->failure(): "+errorMessageToDisplay);
				alert(errorMessageToDisplay); 
			}
			
			//Get user media success callback function:
			function success(e)
			{
				console.log("getUserMedia->success(): Microphone access request was successful!");
				
				var BUFFER_SIZE = 2048;
				var RECORDING_MODE = PredefinedRecordingModes.MONO_5_KHZ; // 单声道 5kHz 最低的采样率
				var SAMPLE_RATE = RECORDING_MODE.getSampleRate();
				var OUTPUT_CHANNEL_COUNT = RECORDING_MODE.getChannelCount();
				
				
				console.log("getUserMedia->success(): Detecting window audio context.");
				var detectWindowAudioContext = new BrowserWindowAudioContextDetection();
				
				if(detectWindowAudioContext.windowAudioContextSupported())
				{
					console.log("getUserMedia->success(): Window audio context supported.");
					
					var windowAudioContext = detectWindowAudioContext.getWindowAudioContextMethod();
					
					console.log("getUserMedia->success(): Window audio context method: " + windowAudioContext);
				
					console.log('getUserMedia->success(): Creating recorder object.');
					
					recorderObj = new SoundRecorder(windowAudioContext, BUFFER_SIZE, SAMPLE_RATE, OUTPUT_CHANNEL_COUNT);
					
					console.log('getUserMedia->success(): Initializing recorder object.');
					recorderObj.init(e);
			
					console.log('getUserMedia->success(): Assigning onaudioprocess event function.');
				
					recorderObj.recorder.onaudioprocess = function(e)
					{            
						//Do nothing if not recording:
						if (!recorderObj.isRecording()) return;                        
						// Copy the data from the input buffers;
						var left = e.inputBuffer.getChannelData (0);
						var right = e.inputBuffer.getChannelData (1);
						recorderObj.cloneChannelData(left, right);            
						console.log('SoundRecorder.recorder.onaudioprocess: Saving audio data...');
					};
					
					console.log('getUserMedia->success(): Recorder object successfully created and initialized.');
					console.log('getUserMedia->success(): Recorder object ready status: ' + recorderObj.isReady());
					
					//document.getElementById("button_startRecording").disabled = false;
					//document.getElementById("button_stopRecording").disabled = true;
				}
				else
				{
					var messageString = "Unable to detect window audio context, cannot continue.";
					console.log("getUserMedia->success(): "+ messageString);
					alert(messageString);
					return;
				}    
			}
						
			function handleStartRecording()
			{
				//Update UI:
				//document.getElementById("button_startRecording").disabled = true;
				//document.getElementById("button_startRecording").style.display = "none";
				//document.getElementById("button_stopRecording").style.display = "block";
				//document.getElementById("button_stopRecording").disabled = false;
				
				//Start the recording:
				console.log("Starting new recording...");
				recorderObj.startRecordingNewWavFile();
				
			}
			
			function handleStopRecording()
			{
				//Disable stop button to prevent clicking too many times:
				//document.getElementById("button_stopRecording").disabled = true;
				//document.getElementById("button_stopRecording").style.display = "none";
				
				//Stop the recording:
				console.log("Stopping recording.");
				recorderObj.stopRecording();
				
				//Save the recording by building the wav file blob and send it to the client:
				console.log("Building wav file.");
				wavFileBlob = recorderObj.buildWavFileBlob();
				
				console.log("Downloading...");
				//wavFileBlob.downloadLocally();
                                
				//Re-enable the start button after saving:
				//document.getElementById("button_startRecording").disabled = false;
				//document.getElementById("button_startRecording").style.display = "block";
			}
	</script>
    </body>
</html>
