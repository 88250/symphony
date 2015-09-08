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
                        <input autofocus="autofocus" type="text" id="articleTitle" tabindex="1"
                               value="<#if article??>${article.articleTitle}</#if>" placeholder="${titleLabel}" />
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
                        <textarea id="articleContent" tabindex="2"
                                  placeholder="${addArticleEditorPlaceholderLabel}"><#if article??>${article.articleContent}</#if></textarea>
                        <div class="fn-left grammar fn-none">
                            ${markdwonGrammarLabel}
                        </div>
                    </div>
                    <div>
                        <input id="articleTags" type="text" tabindex="3" 
                               value="<#if article??>${article.articleTags}<#else>${tags}</#if>" placeholder="${tagLabel}（${tagSeparatorTipLabel}）"/>
                        <br/><br/>
                    </div>
                    <div class="fn-clear article-reward-content">
                        <form class="fn-none" id="rewardFileUpload" method="POST" enctype="multipart/form-data">
                            <input type="file" name="file">
                        </form>
                        <textarea id="articleRewardContent" tabindex="4"
                                  placeholder="${rewardEditorPlaceholderLabel}"><#if article??>${article.articleRewardContent}</#if></textarea>
                    </div>
                    <div>
                        <input id="articleRewardPoint" type="text" tabindex="5" 
                               value="<#if article?? && 0 != article.articleRewardPoint>${article.articleRewardPoint}</#if>" placeholder="${rewardPointLabel}" <#if article?? && 0 < article.articleRewardPoint>readonly="readonly"</#if>/>
                               <br/><br/>
                    </div>
                    <div class="tip" id="addArticleTip"></div><br/>
                    <div class="fn-clear">
                        <#if !article??>
                        <div class="fn-left"> &nbsp;
                            <input tabindex="6" type="radio" name="articleType" checked="checked" value="0"/> 
                                   ${articleLabel}
                        </div>
                        <div class="fn-left"> &nbsp;
                            <input tabindex="7" type="radio" name="articleType" value="1"/> 
                                   ${discussionLabel}
                        </div>
                         <div class="fn-left"> &nbsp;
                            <input tabindex="8" type="radio" name="articleType" value="2"/> 
                                   ${cityBroadcastLabel}
                        </div>
                        <#else>
                        <#if 1 == article.articleType>
                        <div class="fn-left"> &nbsp;
                            <input tabindex="7" disabled="disabled" checked="checked" type="radio" name="articleType" value="1"/> 
                                   ${discussionLabel}
                        </div>
                        </#if>
                        <#if 2 == article.articleType>
                        <div class="fn-left"> &nbsp;
                            <input tabindex="8" disabled="disabled" checked="checked" type="radio" name="articleType" value="2"/> 
                                   ${cityBroadcastLabel}
                        </div>
                        </#if>
                        </#if>
                        <div class="fn-right">
                            <button class="green" onclick="AddArticle.preview()">${previewLabel}</button> &nbsp; &nbsp; 
                            <button class="red" tabindex="9" onclick="AddArticle.add(<#if article??>'${article.oId}'<#else>null</#if>,'${csrfToken}')"><#if article??>${editLabel}<#else>${postLabel}</#if></button>
                        </div>
                    </div>
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
        <script type="text/javascript" src="${staticServePath}/js/audio${miniPostfix}.js?${staticResourceVersion}"></script>

        <script>
                                                Label.articleTitleErrorLabel = "${articleTitleErrorLabel}";
                                                Label.articleContentErrorLabel = "${articleContentErrorLabel}";
                                                Label.tagsErrorLabel = "${tagsErrorLabel}";
                                                Label.userName = "${userName}";
                                                Label.recordDeniedLabel = "${recordDeniedLabel}";
                                                Label.recordDeviceNotFoundLabel = "${recordDeviceNotFoundLabel}";
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
                                                var qiniuToken = '${qiniuUploadToken}';
                                                var qiniuDomain = '${qiniuDomain}';
                                                var audioRecordingLabel = '${audioRecordingLabel}';
                                                var uploadingLabel = '${uploadingLabel}';
        </script>
    </body>
</html>
