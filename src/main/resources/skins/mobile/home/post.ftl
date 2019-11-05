<#--

    Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
    Copyright (C) 2012-present, b3log.org

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
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper post">
                <div class="fn-hr10"></div>
                <div class="fn-flex-1 fn-clear">
                    <div class="form">
                        <input type="text" id="articleTitle" tabindex="1"
                               value="<#if article??>${article.articleTitle}</#if>" placeholder="${titleLabel}" />
                    </div>
                    <div class="article-content">
                        <div id="articleContent"
                             data-placeholder="<#if !article?? && 1 == articleType>${addDiscussionEditorPlaceholderLabel}</#if>${addArticleEditorPlaceholderLabel}"></div>
                        <textarea class="fn-none"><#if article??>${article.articleContent?html}</#if><#if at??>@${at}</#if></textarea>
                    </div>
                    <div class="tags-wrap">
                        <div class="tags-input fn-flex"><span class="tags-selected"></span>
                        <input id="articleTags" type="text" tabindex="3" class="fn-flex-1"
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
                    <#if (!article?? && 5 == articleType) || (article?? && article.articleType == 5)>
                    <div class="form">
                        <input id="articleAskPoint"
                               value="<#if article??>${article.articleQnAOfferPoint}</#if>"
                               type="number" tabindex="5" min="1" placeholder="${qnaOfferPointLabel}"/>
                    </div>
                    <#else>
                    <button id="showReward" class="fn-ellipsis" onclick="$(this).next().show(); $(this).hide()">
                        ${rewardEditorPlaceholderLabel} &dtrif;
                    </button>
                    <div class="fn-none">
                        <div class="fn-clear article-reward-content">
                            <div id="articleRewardContent"
                                 data-placeholder="${rewardEditorPlaceholderLabel}"></div>
                            <textarea class="fn-none"><#if article??>${article.articleRewardContent}</#if></textarea>
                        </div><br>
                        <div class="form">
                            <input id="articleRewardPoint" type="number" tabindex="5" min="1" 
                                   <#if article?? && 0 < article.articleRewardPoint>data-orval="${article.articleRewardPoint}"</#if> 
                                   value="<#if article?? && 0 < article.articleRewardPoint>${article.articleRewardPoint}</#if>" placeholder="${rewardPointLabel}" />
                        </div>
                    </div>
                    </#if>
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
                        <label> &nbsp;
                            <input type="radio" name="articleType" <#if 5 == articleType>checked="checked"</#if>
                                   value="5"/>
                            ${qnaLabel}
                        </label>
                        <#else>
                        <input class="fn-none" type="radio" name="articleType" value="${article.articleType}" checked="checked"/> 
                        </#if>
                    </div>
                    <div class="fn-clear">
                        <#if permissions["commonAddArticleAnonymous"].permissionGrant && articleType != 2 && articleType != 5>
                            <label class="article-anonymous">&nbsp;  ${anonymousLabel}<input
                                <#if article??> disabled="disabled"<#if 1 == article.articleAnonymous> checked</#if></#if>
                                type="checkbox" id="articleAnonymous"></label>
                        </#if>
                        <label class="article-anonymous">&nbsp;  ${showInListLabel}<input
                                <#if (article?? && (1 == article.articleShowInList)) || !article??> checked="checked"</#if>
                                                                                                     type="checkbox" id="articleShowInList"></label>
                        <label class="article-anonymous">&nbsp;  ${commentableLabel}<input
                                <#if (article?? && article.articleCommentable) || !article??> checked="checked"</#if>
                                                type="checkbox" id="articleCommentable"></label>
                        <label class="article-anonymous">&nbsp;  ${notifyFollowersLabel}<input type="checkbox" id="articleNotifyFollowers"></label>
                        <br/><br/>
                        <#if article??>
                            <#if permissions["commonUpdateArticle"].permissionGrant>
                                <button class="fn-right" tabindex="10" onclick="AddArticle.add('${csrfToken}', this)">${submitLabel}</button>
                            </#if>
                        <#else>
                            <#if permissions["commonAddArticle"].permissionGrant>
                                <button class="fn-right" tabindex="10" onclick="AddArticle.add('${csrfToken}', this)">${postLabel}</button>
                            </#if>
                        </#if>
                        <span class="fn-right">&nbsp; &nbsp;</span>
                        <#if article?? && permissions["commonRemoveArticle"].permissionGrant>
                            <button class="red fn-right" tabindex="11" onclick="AddArticle.remove('${csrfToken}', this)">${removeArticleLabel}</button>
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
                                <a href="${servePath}/about" target="_blank">(?)</a></span>
                            <#elseif 5 == articleType>
                            <svg class="post__info">
                                <use xlink:href="#iconAsk"></use>
                            </svg> ${qnaLabel}
                            <span class="ft-gray">${addAskArticleTipLabel}</span>
                            </#if>
                    </div>
                </div>
            </div>
        </div>
        <#include "../footer.ftl"/>
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
            Label.commonAtUser = '${permissions["commonAtUser"].permissionGrant?c}';
            <#if article??>Label.articleOId = '${article.oId}' ;</#if>
            Label.articleType = ${articleType};
            Label.confirmRemoveLabel = '${confirmRemoveLabel}';
        </script>
        <#if 3 == articleType>
            <script src="${staticServePath}/js/lib/diff2html/diff.min.js"></script>
        </#if>
        <script src="${staticServePath}/js/add-article${miniPostfix}.js?${staticResourceVersion}"></script>
    </body>
</html>
