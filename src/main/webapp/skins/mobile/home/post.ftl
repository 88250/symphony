<#--

    Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
    Copyright (C) 2012-2018, b3log.org & hacpai.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

-->
<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${addArticleLabel} - ${symphonyLabel}">
        <meta name="robots" content="none" />
        </@head>
        <link rel="stylesheet" href="${staticServePath}/js/lib/editor/codemirror.min.css">
        <link rel="stylesheet" href="${staticServePath}/js/lib/highlight.js-9.6.0/styles/github.css">
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper post">
                <div class="fn-hr10"></div>
                <div class="form fn-flex-1 fn-clear">
                    <input type="text" id="articleTitle" tabindex="1"
                           value="<#if article??>${article.articleTitle}</#if>" placeholder="${titleLabel}" />
                    <div class="fn-hr10"></div>
                    <div class="fn-hr10"></div>
                    <div class="article-content">
                        <textarea id="articleContent" tabindex="2"
                                  placeholder="<#if !article?? && 1 == articleType>${addDiscussionEditorPlaceholderLabel}</#if>${addArticleEditorPlaceholderLabel}"><#if article??>${article.articleContent}</#if><#if at??>@${at}</#if></textarea>
                    </div>
                    <div class="tags-wrap">
                        <div class="tags-input"><span class="tags-selected"></span>
                        <input id="articleTags" type="text" tabindex="3" 
                               value="<#if article??>${article.articleTags}<#else>${tags}</#if>" placeholder="${tagLabel}（${tagSeparatorTipLabel}）" autocomplete="off" />
                        </div>
                        <#if addArticleDomains?size != 0>
                        <div class="domains-tags">
                            <#list addArticleDomains as domain>
                                <#if domain.domainTags?size gt 0>
                                    <span data-id="${domain.oId}" class="btn small<#if 0 == domain_index> current</#if>">${domain.domainTitle}</span>&nbsp;
                                </#if>
                            </#list>
                            <div class="fn-hr5"></div>
                            <#list addArticleDomains as domain>
                                <#if domain.domainTags?size gt 0>
                                <div id="tags${domain.oId}" class="domain-tags<#if 0 != domain_index> fn-none</#if>">
                                    <#list domain.domainTags as tag>
                                    <span class="tag">${tag.tagTitle}</span>
                                    </#list>
                                </div>
                                </#if>
                            </#list>
                        </div>
                        </#if>
                        <br/>
                    </div>
                    <button id="showReward" class="fn-ellipsis" onclick="$(this).next().show(); $(this).hide()">
                        ${rewardEditorPlaceholderLabel} &dtrif;
                    </button>
                    <div class="fn-none">
                        <div class="fn-clear article-reward-content">
                            <textarea id="articleRewardContent" tabindex="4"
                                      placeholder="${rewardEditorPlaceholderLabel}"><#if article??>${article.articleRewardContent}</#if></textarea>
                        </div><br>
                        <div>
                            <input id="articleRewardPoint" type="number" tabindex="5" min="1" 
                                   <#if article?? && 0 < article.articleRewardPoint>data-orval="${article.articleRewardPoint}"</#if> 
                                   value="<#if article?? && 0 < article.articleRewardPoint>${article.articleRewardPoint}</#if>" placeholder="${rewardPointLabel}" />
                        </div>
                    </div>
                    <div class="fn-hr10"></div>
                    <div class="tip" id="addArticleTip"></div>
                    <div class="fn-hr10"></div>
                    <div class="fn-clear fn-none">
                        <#if !article??>
                        <label> &nbsp;
                            <input tabindex="6" type="radio" name="articleType" <#if 0 == articleType>checked="checked"</#if> value="0"/> 
                                   ${articleLabel}
                        </label>
                        <label id="articleType3"> &nbsp;
                            <input tabindex="9" type="radio" name="articleType" <#if 3 == articleType>checked="checked"</#if> value="3"/> 
                                   ${thoughtLabel}
                        </label>
                        <label> &nbsp;
                            <input tabindex="7" type="radio" name="articleType" <#if 1 == articleType>checked="checked"</#if> value="1"/> 
                                   ${discussionLabel}
                        </label>
                        <label> &nbsp;
                            <input tabindex="8" type="radio" name="articleType" <#if 2 == articleType>checked="checked"</#if> value="2"/> 
                                   ${cityBroadcastLabel}
                        </label>
                        <#else>
                        <input class="fn-none" type="radio" name="articleType" value="${article.articleType}" checked="checked"/> 
                        </#if>
                    </div>
                    <div class="fn-clear">
                        <#if article?? && permissions["commonRemoveArticle"].permissionGrant>
                            <label class="ft-red fn-pointer" tabindex="11" onclick="AddArticle.remove('${csrfToken}', this)">${removeArticleLabel} &nbsp; &nbsp;</label>
                        </#if>
                        <#if hasB3Key>
                            <label class="article-anonymous">${syncLabel}<input
                                <#if article??> disabled="disabled"<#if article.syncWithSymphonyClient> checked</#if></#if>
                                type="checkbox" id="syncWithSymphonyClient"></label>
                        </#if>
                        <#if permissions["commonAddArticleAnonymous"].permissionGrant>
                            <label class="article-anonymous">&nbsp;  ${anonymousLabel}<input
                                <#if article??> disabled="disabled"<#if 1 == article.articleAnonymous> checked</#if></#if>
                                type="checkbox" id="articleAnonymous"></label>
                        </#if>

                        <#if article??>
                            <#if permissions["commonUpdateArticle"].permissionGrant>
                                <button class="fn-right" tabindex="10" onclick="AddArticle.add('${csrfToken}', this)">${submitLabel}</button>
                            </#if>
                        <#else>
                            <#if permissions["commonAddArticle"].permissionGrant>
                                <button class="fn-right" tabindex="10" onclick="AddArticle.add('${csrfToken}', this)">${postLabel}</button>
                            </#if>
                        </#if>
                    </div>
                    <br/>
                    <div class="fn-clear">
                            <#if !articleType??>
                            <#assign articleType=article.articleType>
                            </#if>
                            <#if 0 == articleType>
                                <svg><use xlink:href="#article"></use></svg> ${articleLabel}
                            <span class="ft-gray"><span class="ft-green">${addNormalArticleTipLabel}</span>
                            <#elseif 1 == articleType>
                                <svg><use xlink:href="#locked"></use></svg> ${discussionLabel}
                            <span class="ft-gray">${addDiscussionArticleTipLabel}</span>
                            <#elseif 2 == articleType>
                                <svg><use xlink:href="#feed"></use></svg> ${cityBroadcastLabel}
                            <span class="ft-gray">${addCityArticleTipLabel} <i>${broadcastPoint}</i> ${pointLabel}</span>
                            <#elseif 3 == articleType>
                                <svg><use xlink:href="#video"></use></svg> ${thoughtLabel}
                            <span class="ft-gray">${addThoughtArticleTipLabel}
                                <a href="https://hacpai.com/article/1441942422856" target="_blank">(?)</a></span>
                            </#if>
                    </div>
                </div>
            </div>
        </div>
        <#include "../footer.ftl"/>
        <script src="${staticServePath}/js/lib/editor/codemirror.min.js?4.13"></script>
        <script src="${staticServePath}/js/lib/highlight.js-9.6.0/highlight.pack.js"></script>
        <script src="${staticServePath}/js/lib/jquery/file-upload-9.10.1/jquery.fileupload.min.js"></script>
        <script src="${staticServePath}/js/lib/sound-recorder/SoundRecorder.js"></script>
        <script>
            Label.articleTitleErrorLabel = "${articleTitleErrorLabel}";
            Label.articleContentErrorLabel = "${articleContentErrorLabel}";
            Label.tagsErrorLabel = "${tagsErrorLabel}";
            Label.userName = "${currentUser.userName}";
            Label.recordDeniedLabel = "${recordDeniedLabel}";
            Label.recordDeviceNotFoundLabel = "${recordDeviceNotFoundLabel}";
            Label.uploadLabel = "${uploadLabel}";
            Label.audioRecordingLabel = '${audioRecordingLabel}';
            Label.uploadingLabel = '${uploadingLabel}';
            Label.articleRewardPointErrorLabel = '${articleRewardPointErrorLabel}';
            Label.discussionLabel = '${discussionLabel}';
            Label.insertEmojiLabel = '${insertEmojiLabel}';
            Label.addBoldLabel = '${addBoldLabel}';
            Label.addItalicLabel = '${addItalicLabel}';
            Label.insertQuoteLabel = '${insertQuoteLabel}';
            Label.addBulletedLabel = '${addBulletedLabel}';
            Label.addNumberedListLabel = '${addNumberedListLabel}';
            Label.addLinkLabel = '${addLinkLabel}';
            Label.undoLabel = '${undoLabel}';
            Label.redoLabel = '${redoLabel}';
            Label.previewLabel = '${previewLabel}';
            Label.helpLabel = '${helpLabel}';
            Label.fullscreenLabel = '${fullscreenLabel}';
            Label.uploadFileLabel = '${uploadFileLabel}';
            Label.qiniuDomain = '${qiniuDomain}';
            Label.qiniuUploadToken = '${qiniuUploadToken}';
            Label.commonAtUser = '${permissions["commonAtUser"].permissionGrant?c}';
            <#if article??>Label.articleOId = '${article.oId}' ;</#if>
            Label.articleType = ${articleType};
            Label.confirmRemoveLabel = '${confirmRemoveLabel}';
        </script>
        <script src="${staticServePath}/js/add-article${miniPostfix}.js?${staticResourceVersion}"></script>
        <script>
            Util.uploadFile({
                "id": "fileUpload",
                "pasteZone": $("#articleContent").next().next(),
                "qiniuUploadToken": "${qiniuUploadToken}",
                "editor": AddArticle.editor,
                "uploadingLabel": "${uploadingLabel}",
                "qiniuDomain": "${qiniuDomain}",
                "imgMaxSize": ${imgMaxSize?c},
                "fileMaxSize": ${fileMaxSize?c}
            });
            Util.uploadFile({
                "id": "rewardFileUpload",
                "pasteZone": $("#articleRewardContent").next().next(),
                "qiniuUploadToken": "${qiniuUploadToken}",
                "editor": AddArticle.rewardEditor,
                "uploadingLabel": "${uploadingLabel}",
                "qiniuDomain": "${qiniuDomain}",
                "imgMaxSize": ${imgMaxSize?c},
                "fileMaxSize": ${fileMaxSize?c}
            });
        </script>
    </body>
</html>
