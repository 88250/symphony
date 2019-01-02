<#--

    Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
    Copyright (C) 2012-2019, b3log.org & hacpai.com

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
<@admin "comments">
<div class="content">
    <div class="module">
        <div class="module-header">
            <h2>${unmodifiableLabel}</h2>
        </div>
        <div class="module-panel form fn-clear form--admin">
            <div class="fn__flex">
                <label>
                    <div>Id</div>
                    <input onfocus="this.select()" type="text" id="oId" value="${comment.oId}" readonly="readonly"/>
                </label>
                <label>
                    <div>${authorIdLabel}</div>
                    <input onfocus="this.select()" type="text" id="commentAuthorId" name="commentAuthorId"
                           value="${comment.commentAuthorId}" readonly="readonly"/>
                </label>
                <label>
                    <div>${articleLabel} Id</div>
                    <input onfocus="this.select()" type="text" id="commentOnArticleId" name="commentOnArticleId"
                           value="${comment.commentOnArticleId}" readonly="readonly"/>
                </label>
                <label>
                    <div>IP</div>
                    <input onfocus="this.select()" type="text" id="commentIP" name="commentIP"
                           value="${comment.commentIP}" readonly="readonly"/>
                </label>
            </div>
            <div class="fn__flex">
                <label>
                    <div>URL</div>
                    <input onfocus="this.select()" type="text" id="commentSharpURL" name="commentSharpURL"
                           value="${comment.commentSharpURL}" readonly="readonly"/>
                </label>
                <label>
                    <div>${anonymousLabel}</div>
                    <input onfocus="this.select()" type="text"
                           value="<#if 0 == comment.commentAnonymous>${noLabel}<#else>${yesLabel}</#if>"
                           readonly="readonly"/>
                </label>
            </div>
            <div class="fn__flex">
                <label>
                    <div>UA</div>
                    <input onfocus="this.select()" type="text" id="commentUA" name="commentUA"
                           value="${comment.commentUA}" readonly="readonly"/>
                </label>
            </div>
        </div>
    </div>

    <#if permissions["commentUpdateCommentBasic"].permissionGrant>
    <div class="module">
        <div class="module-header">
            <h2>${modifiableLabel}</h2>
        </div>
        <div class="module-panel form fn-clear form--admin">
            <form action="${servePath}/admin/comment/${comment.oId}" method="POST">
                <div class="fn__flex">
                    <label>
                        <div>${commentContentLabel}</div>
                        <textarea id="commentContent" name="commentContent"
                                  rows="10">${comment.commentContent}</textarea>
                    </label>
                </div>
                <div class="fn__flex">
                    <label>
                        <div>${commentStatusLabel}</div>
                        <select id="commentStatus" name="commentStatus">
                            <option value="0"<#if 0 == comment.commentStatus> selected</#if>>${validLabel}</option>
                            <option value="1"<#if 1 == comment.commentStatus> selected</#if>>${banLabel}</option>
                        </select>
                    </label>
                    <label class="mid">
                        <div>${goodCntLabel}</div>
                        <input type="text" id="commentGoodCnt" name="commentGoodCnt" value="${comment.commentGoodCnt}"/>
                    </label>
                    <label>
                        <div>${badCntLabel}</div>
                        <input type="text" id="commentBadCnt" name="commentBadCnt" value="${comment.commentBadCnt}"/>
                    </label>
                </div>
                <br/>
                <button type="submit" class="green fn-right">${submitLabel}</button>
            </form>
        </div>
    </div>
    </#if>

    <#if permissions["commentRemoveComment"].permissionGrant>
    <div class="module">
        <div class="module-header">
            <h2 class="ft-red">${removeDataLabel}</h2>
        </div>
        <div class="module-panel form fn-clear form--admin">
            <form class="fn__flex" action="${servePath}/admin/remove-comment" method="POST"
                  onsubmit="return window.confirm('${confirmRemoveLabel}')">
                <label>
                    <div>Id</div>
                    <input type="text" id="commentId" name="commentId" value="${comment.oId}" readonly class="input--admin-readonly"/>
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