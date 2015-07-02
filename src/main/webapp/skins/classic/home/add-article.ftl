<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${userName} - ${addArticleLabel}">
        <meta name="robots" content="none" />
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/home${miniPostfix}.css?${staticResourceVersion}" />
        <link type="text/css" rel="stylesheet" href="${staticServePath}/js/lib/codemirror-5.3/codemirror.css?${staticResourceVersion}" />
        <link type="text/css" rel="stylesheet" href="${staticServePath}/js/lib/codemirror-5.3/addon/display/fullscreen.css?${staticResourceVersion}" />
        <link type="text/css" rel="stylesheet" href="${staticServePath}/js/lib/codemirror-5.3/addon/hint/show-hint.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="form">
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
                        <form class="fn-none" id="fileupload" method="POST" enctype="multipart/form-data">
                            <input type="file" name="file">
                        </form>
                        <textarea id="articleContent" placeholder="${addArticleEditorPlaceholderLabel}"><#if article??>${article.articleContent}</#if></textarea>
                        <span id="articleContentTip" style="top: 304px; right: 2px;"></span>
                        <div class="fn-left grammar fn-none">
                            ${markdwonGrammarLabel}
                        </div>
                    </div>
                    <div>
                        <input id="articleTags" type="text" value="<#if article??>${article.articleTags}</#if>" placeholder="${tagLabel}（${tagSeparatorTipLabel}）"/>
                        <span style="right:2px;top:424px;"></span><br/><br/>
                    </div>
                    <div class="fn-clear article-reward-content">
                        <form class="fn-none" id="rewardFileupload" method="POST" enctype="multipart/form-data">
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
                            <button class="green<#if !article??> fn-none</#if>" onclick="AddArticle.preview()">${previewLabel}</button> &nbsp; &nbsp; 
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
        <script src="${staticServePath}/js/lib/jquery/jquery.bowknot.min.js?${staticResourceVersion}"></script>
        <script src="${staticServePath}/js/lib/codemirror-5.3/codemirror.js?${staticResourceVersion}"></script>
        <script src="${staticServePath}/js/lib/codemirror-5.3/mode/markdown/markdown.js?${staticResourceVersion}"></script>
        <script src="${staticServePath}/js/lib/codemirror-5.3/addon/display/placeholder.js?${staticResourceVersion}"></script>
        <script src="${staticServePath}/js/lib/codemirror-5.3/addon/display/fullscreen.js?${staticResourceVersion}"></script>
        <script src="${staticServePath}/js/overwrite/codemirror/addon/hint/show-hint.js?${staticResourceVersion}"></script>
        <script type="text/javascript" src="${staticServePath}/js/lib/jquery/file-upload-9.10.1/vendor/jquery.ui.widget.js?${staticResourceVersion}"></script>
        <script type="text/javascript" src="${staticServePath}/js/lib/jquery/file-upload-9.10.1/jquery.iframe-transport.js?${staticResourceVersion}"></script>
        <script type="text/javascript" src="${staticServePath}/js/lib/jquery/file-upload-9.10.1/jquery.fileupload.js?${staticResourceVersion}"></script>
        <script type="text/javascript" src="${staticServePath}/js/lib/jquery/file-upload-9.10.1/jquery.fileupload-process.js?${staticResourceVersion}"></script>
        <script type="text/javascript" src="${staticServePath}/js/lib/jquery/file-upload-9.10.1/jquery.fileupload-validate.js?${staticResourceVersion}"></script>
        <script src="${staticServePath}/js/add-article${miniPostfix}.js?${staticResourceVersion}"></script>
        <script>
                                                Label.articleTitleErrorLabel = "${articleTitleErrorLabel}";
                                                Label.articleContentErrorLabel = "${articleContentErrorLabel}";
                                                Label.articleTagsErrorLabel = "${articleTagsErrorLabel}";
                                                Label.userName = "${userName}";
                                                Util.uploadFile({
                                                        "id": "fileupload",
                                                        "pasteZone": $("#articleContent").next(),
                                                        "qiniuUploadToken": "${qiniuUploadToken}",
                                                        "editor": AddArticle.editor,
                                                        "uploadingLabel": "${uploadingLabel}",
                                                        "qiniuDomain": "${qiniuDomain}"
                                                });
                                                Util.uploadFile({
                                                        "id": "rewardFileupload",
                                                        "pasteZone": $("#articleRewardContent").next(),
                                                        "qiniuUploadToken": "${qiniuUploadToken}",
                                                        "editor": AddArticle.rewardEditor,
                                                        "uploadingLabel": "${uploadingLabel}",
                                                        "qiniuDomain": "${qiniuDomain}"
                                                });
        </script>
    </body>
</html>
