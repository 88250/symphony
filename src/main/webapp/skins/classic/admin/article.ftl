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
<div class="content">
    <div class="module">
        <div class="module-header">
            <h2>${unmodifiableLabel}</h2>
            <a class="fn__right" href="${servePath}${article.articlePermalink}">${permalinkLabel}</a>
        </div>
        <div class="module-panel form fn-clear form--admin">
            <div class="fn__flex">
                <label>
                    <div>Id</div>
                    <input onfocus="this.select()" type="text" id="oId" name="oId" value="${article.oId}"
                           readonly="readonly"/>
                </label>
                <label>
                    <div>${authorIdLabel}</div>
                    <input onfocus="this.select()" type="text" id="articleAuthorId" name="articleAuthorId"
                           value="${article.articleAuthorId}"
                           readonly="readonly"/>
                </label>
                <label>
                    <div>${commentCountLabel}</div>
                    <input onfocus="this.select()" type="text" id="articleCommentCount" name="articleCommentCount"
                           value="${article.articleCommentCount?c}" readonly="readonly"/>
                </label>
                <label>
                    <div>${viewCountLabel}</div>
                    <input onfocus="this.select()" type="text" id="articleViewCount" name="articleViewCount"
                           value="${article.articleViewCount?c}"
                           readonly="readonly"/>
                </label>
            </div>
            <div class="fn__flex">
                <label>
                    <div>${permalinkLabel}</div>
                    <input onfocus="this.select()" type="text" id="articlePermalink" readonly name="articlePermalink"
                           value="${servePath}${article.articlePermalink}"/>
                </label>
            </div>
            <div class="fn__flex">
                <label>
                    <div>${createTimeLabel}</div>
                    <input onfocus="this.select()" type="text" id="articleCreateTime" name="articleCreateTime"
                           readonly
                           value="${article.articleCreateTime?number_to_datetime}"/>
                </label>
                <label>
                    <div>${updateTimeLabel}</div>
                    <input onfocus="this.select()" type="text" id="articleUpdateTime" name="articleUpdateTime"
                           readonly
                           value="${article.articleUpdateTime?number_to_datetime}"/>
                </label>
            </div>
            <div class="fn__flex">
                <label>
                    <div>${eidotrTypeLabel}</div>
                    <input onfocus="this.select()" type="text" id="articleEditorType" name="articleEditorType"
                           value="<#if 0 == article.articleEditorType>Markdown<#else>${article.articleEditorType}</#if>"
                           readonly="readonly"/>
                </label>
                <label>
                    <div>IP</div>
                    <input onfocus="this.select()" type="text" id="articleIP" name="articleIP"
                           value="${article.articleIP}"
                           readonly="readonly"/>
                </label>
                <label>
                    <div>${stickLabel}</div>
                    <input onfocus="this.select()" type="text" id="articleStick" name="articleStick"
                           value="<#if 0 == article.articleStick>${noLabel}<#else>${yesLabel}</#if>"
                           readonly="readonly"/>
                </label>
                <label>
                    <div>${anonymousLabel}</div>
                    <input onfocus="this.select()" type="text" id="articleStick" name="articleStick"
                           value="<#if 0 == article.articleAnonymous>${noLabel}<#else>${yesLabel}</#if>"
                           readonly="readonly"/>
                </label>
            </div>
            <div class="fn__flex">
                <label>
                    <div>UA</div>
                    <input onfocus="this.select()" type="text" id="articleUA" name="articleUA"
                           value="${article.articleUA}"
                           readonly="readonly"/>
                </label>
            </div>
        </div>
    </div>

    <#if permissions["articleUpdateArticleBasic"].permissionGrant>
    <div class="module">
        <div class="module-header">
            <h2>${modifiableLabel}</h2>
        </div>
        <div class="module-panel form fn-clear form--admin">
            <form action="${servePath}/admin/article/${article.oId}" method="POST">
                <div class="fn__flex">
                    <label>
                        <div>${titleLabel}</div>
                        <input type="text" id="articleTitle" name="articleTitle" value="${article.articleTitle}"/>
                    </label>
                </div>
                <div class="fn__flex">
                    <label>
                        <div>${tagLabel}</div>
                        <input type="text" id="articleTags" name="articleTags" value="${article.articleTags}"/>
                    </label>
                </div>
                <div class="fn__flex">
                    <label>
                        <div>${contentLabel}</div>
                        <textarea name="articleContent" rows="28">${article.articleContent}</textarea>
                    </label>
                </div>
                <div class="fn__flex">
                    <label>
                        <div>${rewardContentLabel}</div>
                        <textarea name="articleRewardContent">${article.articleRewardContent}</textarea>
                    </label>
                </div>
                <div class="fn__flex">
                    <label>
                        <div>${rewardPointLabel}</div>
                        <input type="text" id="articleRewardPoint" name="articleRewardPoint"
                               value="${article.articleRewardPoint?c}"/>
                    </label>
                    <label class="mid">
                        <div>${perfectLabel}</div>
                        <select id="articlePerfect" name="articlePerfect">
                            <option value="0"<#if 0 == article.articlePerfect> selected</#if>>${noLabel}</option>
                            <option value="1"<#if 1 == article.articlePerfect> selected</#if>>${yesLabel}</option>
                        </select>
                    </label>
                    <label>
                        <div>${commentableLabel}</div>
                        <select id="articleCommentable" name="articleCommentable">
                            <option value="true"<#if article.articleCommentable> selected</#if>>${yesLabel}</option>
                            <option value="false"<#if !article.articleCommentable> selected</#if>>${noLabel}</option>
                        </select>
                    </label>
                </div>
                <div class="fn__flex">
                    <label>
                        <div>${articleStatusLabel}</div>
                        <select id="articleStatus" name="articleStatus">
                            <option value="0"<#if 0 == article.articleStatus> selected</#if>>${validLabel}</option>
                            <option value="1"<#if 1 == article.articleStatus> selected</#if>>${banLabel}</option>
                            <option value="2"<#if 2 == article.articleStatus> selected</#if>>${lockLabel}</option>
                        </select>
                    </label>
                    <label class="mid">
                        <div>${articleTypeLabel}</div>
                        <select id="articleType" name="articleType">
                            <option value="0"<#if 0 == article.articleType> selected</#if>>${articleLabel}</option>
                            <option value="1"<#if 1 == article.articleType> selected</#if>>${discussionLabel}</option>
                            <option value="2"<#if 2 == article.articleType> selected</#if>>${cityBroadcastLabel}</option>
                            <option value="3"<#if 3 == article.articleType> selected</#if>>${thoughtLabel}</option>
                            <option value="5"<#if 5 == article.articleType> selected</#if>>${qnaLabel}</option>
                        </select>
                    </label>
                    <label>
                        <div>${goodCntLabel}</div>
                        <input type="text" id="articleGoodCnt" name="articleGoodCnt" value="${article.articleGoodCnt}"/>
                    </label>
                </div>
                <div class="fn__flex">
                    <label>
                        <div>${badCntLabel}</div>
                        <input type="text" id="articleBadCnt" name="articleBadCnt" value="${article.articleBadCnt}"/>
                    </label>
                    <label class="mid">
                        <div>${miscAllowAnonymousViewLabel}</div>
                        <select name="articleAnonymousView">
                            <option value="0"<#if 0 == article.articleAnonymousView>
                                    selected</#if>>${useGlobalLabel}</option>
                            <option value="1"<#if 1 == article.articleAnonymousView> selected</#if>>${noLabel}</option>
                            <option value="2"<#if 2 == article.articleAnonymousView> selected</#if>>${yesLabel}</option>
                        </select>
                    </label>
                    <label>
                        <div>${pushLabel} Email ${pushLabel}</div>
                        <input type="number" name="articlePushOrder" value="${article.articlePushOrder}" />
                    </label>
                </div>
                <div class="fn__flex">
                    <label>
                        <div>${qnaOfferPointLabel}</div>
                        <input type="text" name="articleQnAOfferPoint" value="${article.articleQnAOfferPoint?c}"/>
                    </label>
                </div>
                <br/>
                <button type="submit" class="green fn-right">${submitLabel}</button>
            </form>
        </div>
    </div>
    </#if>

    <#if permissions["articleStickArticle"].permissionGrant>
    <div class="module">
        <div class="module-header">
            <h2>${stickLabel}</h2>
        </div>
        <div class="module-panel form fn-clear form--admin">
            <form action="${servePath}/admin/stick-article" method="POST" class="fn__flex">
                <label>
                    <div>Id</div>
                    <input type="text" id="articleId" name="articleId" value="${article.oId}" readonly class="input--admin-readonly"/>
                </label>
                <div>
                    &nbsp; &nbsp;
                    <button type="submit" class="green fn-right btn--admin">${submitLabel}</button>
                </div>
            </form>
        </div>
    </div>
    </#if>

    <#if permissions["articleCancelStickArticle"].permissionGrant>
    <div class="module">
        <div class="module-header">
            <h2>${cancelStickLabel}</h2>
        </div>
        <div class="module-panel form fn-clear form--admin">
            <form action="${servePath}/admin/cancel-stick-article" method="POST" class="fn__flex">
                <label>
                    <div>Id</div>
                    <input type="text" id="articleId" name="articleId" value="${article.oId}" readonly class="input--admin-readonly"/>
                </label>
                <div>
                    &nbsp; &nbsp;
                    <button type="submit" class="green fn-right btn--admin">${submitLabel}</button>
                </div>
            </form>
        </div>
    </div>
    </#if>

    <#if (esEnabled || algoliaEnabled) && permissions["articleReindexArticle"].permissionGrant>
    <div class="module">
        <div class="module-header">
            <h2>${searchIndexLabel}</h2>
        </div>
        <div class="module-panel form fn-clear form--admin">
            <form action="${servePath}/admin/search-index-article" method="POST" class="fn__flex">
                <label>
                    <div>Id</div>
                    <input type="text" id="articleId" name="articleId" value="${article.oId}" readonly class="input--admin-readonly"/>
                </label>
                <div>
                    &nbsp; &nbsp;
                    <button type="submit" class="green fn-right btn--admin">${submitLabel}</button>
                </div>
            </form>
        </div>
    </div>
    </#if>

    <#if permissions["articleRemoveArticle"].permissionGrant>
    <div class="module">
        <div class="module-header">
            <h2 class="ft-red">${removeDataLabel}</h2>
        </div>
        <div class="module-panel form fn-clear form--admin">
            <form action="${servePath}/admin/remove-article" method="POST"
                  class="fn__flex"
                  onsubmit="return window.confirm('${confirmRemoveLabel}')">
                <label>
                    <div>Id</div>
                    <input type="text" id="articleId" name="articleId" value="${article.oId}" readonly class="input--admin-readonly"/>
                </label>
                <div>
                    &nbsp; &nbsp;
                    <button type="submit" class="red fn-right btn--admin">${submitLabel}</button>
                </div>
            </form>
        </div>
    </div>
    </#if>
</div>
</@admin>