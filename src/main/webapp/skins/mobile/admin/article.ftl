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
<#include "macro-admin.ftl">
<@admin "articles">
<div class="wrapper">
    <div class="module">
        <div class="module-header">
            <h2>${unmodifiableLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <label for="oId">Id</label>
            <input type="text" id="oId" name="oId" value="${article.oId}" readonly="readonly" />

            <label for="articleAuthorId">${authorIdLabel}</label>
            <input type="text" id="articleAuthorId" name="articleAuthorId" value="${article.articleAuthorId}" readonly="readonly" />

            <label for="articleCommentCount">${commentCountLabel}</label>
            <input type="text" id="articleCommentCount" name="articleCommentCount" value="${article.articleCommentCount?c}" readonly="readonly" />

            <label for="articleViewCount">${viewCountLabel}</label>
            <input type="text" id="articleViewCount" name="articleViewCount" value="${article.articleViewCount?c}" readonly="readonly" />

            <label for="articlePermalink">${permalinkLabel}</label>
            <input type="text" id="articlePermalink" name="articlePermalink" value="${servePath}${article.articlePermalink}" />

            <label for="articleCreateTime">${createTimeLabel}</label>
            <input type="text" id="articleCreateTime" name="articleCreateTime" value="${article.articleCreateTime?c}" />

            <label for="articleUpdateTime">${updateTimeLabel}</label>
            <input type="text" id="articleUpdateTime" name="articleUpdateTime" value="${article.articleUpdateTime?c}" />

            <label for="syncWithSymphonyClient">${symClientSyncLabel}</label>
            <input type="text" id="syncWithSymphonyClient" name="syncWithSymphonyClient" value="${article.syncWithSymphonyClient?c}" readonly="readonly" />

            <label for="clientArticleId">${clientArticleIdLabel}</label>
            <input type="text" id="clientArticleId" name="clientArticleId" value="${article.clientArticleId}" readonly="readonly" />

            <label for="articleEditorType">${eidotrTypeLabel}</label>
            <input type="text" id="articleEditorType" name="articleEditorType" value="${article.articleEditorType}" readonly="readonly" />

            <label for="articleIP">IP</label>
            <input type="text" id="articleIP" name="articleIP" value="${article.articleIP}" readonly="readonly" />

            <label for="articleUA">UA</label>
            <input type="text" id="articleUA" name="articleUA" value="${article.articleUA}" readonly="readonly" />

            <label for"articleStick">${stickLabel}</label>
            <input type="text" id="articleStick" name="articleStick" value="${article.articleStick?c}" readonly="readonly" />

            <label for="articleAnonymous">${anonymousLabel}</label>
            <select id="articleAnonymous" name="articleAnonymous" disabled="disabled">
                <option value="0"<#if 0 == article.articleAnonymous> selected</#if>>${noLabel}</option>
                <option value="1"<#if 1 == article.articleAnonymous> selected</#if>>${yesLabel}</option>
            </select>
        </div>
    </div>

    <#if permissions["articleUpdateArticleBasic"].permissionGrant>
    <div class="module">
        <div class="module-header">
            <h2>${modifiableLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <form action="${servePath}/admin/article/${article.oId}" method="POST">
                <label for="articleTitle">${titleLabel}</label>
                <input type="text" id="articleTitle" name="articleTitle" value="${article.articleTitle}" />

                <label for="articleTags">${tagLabel}</label>
                <input type="text" id="articleTags" name="articleTags" value="${article.articleTags}" />

                <label for="articleContent">${contentLabel}</label>
                <textarea name="articleContent" rows="28">${article.articleContent}</textarea>

                <label for="articleRewardContent">${rewardContentLabel}</label>
                <textarea name="articleRewardContent">${article.articleRewardContent}</textarea>

                <label for="articleRewardPoint">${rewardPointLabel}</label>
                <input type="text" id="articleRewardPoint" name="articleRewardPoint" value="${article.articleRewardPoint?c}"/>

                <label>${perfectLabel}</label>
                <select id="articlePerfect" name="articlePerfect">
                    <option value="0"<#if 0 == article.articlePerfect> selected</#if>>${noLabel}</option>
                    <option value="1"<#if 1 == article.articlePerfect> selected</#if>>${yesLabel}</option>
                </select>

                <label>${commentableLabel}</label>
                <select id="articleCommentable" name="articleCommentable">
                    <option value="true"<#if article.articleCommentable> selected</#if>>${yesLabel}</option>
                    <option value="false"<#if !article.articleCommentable> selected</#if>>${noLabel}</option>
                </select>

                <label>${articleStatusLabel}</label>
                <select id="articleStatus" name="articleStatus">
                    <option value="0"<#if 0 == article.articleStatus> selected</#if>>${validLabel}</option>
                    <option value="1"<#if 1 == article.articleStatus> selected</#if>>${banLabel}</option>
                    <option value="2"<#if 2 == article.articleStatus> selected</#if>>${lockLabel}</option>
                </select>

                <label>${articleTypeLabel}</label>
                <select id="articleType" name="articleType">
                    <option value="0"<#if 0 == article.articleType> selected</#if>>${articleLabel}</option>
                    <option value="1"<#if 1 == article.articleType> selected</#if>>${discussionLabel}</option>
                    <option value="2"<#if 2 == article.articleType> selected</#if>>${cityBroadcastLabel}</option>
                    <option value="3"<#if 3 == article.articleType> selected</#if>>${thoughtLabel}</option>
                </select>

                <label for="articleGoodCnt">${goodCntLabel}</label>
                <input type="text" id="articleGoodCnt" name="articleGoodCnt" value="${article.articleGoodCnt}" />

                <label for="articleBadCnt">${badCntLabel}</label>
                <input type="text" id="articleBadCnt" name="articleBadCnt" value="${article.articleBadCnt}" />
                
                <label for="articleAnonymousView">${miscAllowAnonymousViewLabel}</label>
                <select id="articleAnonymousView" name="articleAnonymousView">
                    <option value="0"<#if 0 == article.articleAnonymousView> selected</#if>>${useGlobalLabel}</option>
                    <option value="1"<#if 1 == article.articleAnonymousView> selected</#if>>${noLabel}</option>
                    <option value="2"<#if 2 == article.articleAnonymousView> selected</#if>>${yesLabel}</option>
                </select>

                <br/><br/>
                <button type="submit" class="green fn-right" >${submitLabel}</button>
            </form>
        </div>
    </div>
    </#if>

    <#if permissions["articleStickArticle"].permissionGrant>
    <div class="module">
        <div class="module-header">
            <h2>${stickLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <form action="${servePath}/admin/stick-article" method="POST">
                <label for="articleId">Id</label>
                <input type="text" id="articleId" name="articleId" value="${article.oId}" readonly="readonly"/>

                <br/><br/>
                <button type="submit" class="green fn-right" >${submitLabel}</button>
            </form>
        </div>
    </div>
    </#if>

    <#if permissions["articleCancelStickArticle"].permissionGrant>
    <div class="module">
        <div class="module-header">
            <h2>${cancelStickLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <form action="${servePath}/admin/cancel-stick-article" method="POST">
                <label for="articleId">Id</label>
                <input type="text" id="articleId" name="articleId" value="${article.oId}" readonly="readonly"/>

                <br/><br/>
                <button type="submit" class="green fn-right" >${submitLabel}</button>
            </form>
        </div>
    </div>
    </#if>

    <#if (esEnabled || algoliaEnabled) && permissions["articleReindexArticle"].permissionGrant>
    <div class="module">
        <div class="module-header">
            <h2>${searchIndexLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <form action="${servePath}/admin/search-index-article" method="POST">
                <label for="articleId">Id</label>
                <input type="text" id="articleId" name="articleId" value="${article.oId}" readonly="readonly"/>

                <br/><br/>
                <button type="submit" class="green fn-right" >${submitLabel}</button>
            </form>
        </div>
    </div>
    </#if>

    <#if permissions["articleRemoveArticle"].permissionGrant>
    <div class="module">
        <div class="module-header">
            <h2 class="ft-red">${removeDataLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <form action="${servePath}/admin/remove-article" method="POST" onsubmit="return window.confirm('${confirmRemoveLabel}')">
                <label for="articleId">Id</label>
                <input type="text" id="articleId" name="articleId" value="${article.oId}" readonly="readonly"/>

                <br/><br/>
                <button type="submit" class="green fn-right" >${submitLabel}</button>
            </form>
        </div>
    </div>
    </#if>
</div>
</@admin>