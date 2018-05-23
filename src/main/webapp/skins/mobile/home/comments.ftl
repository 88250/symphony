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
<#include "macro-home.ftl">
<#include "../macro-pagination.ftl">
<@home "${type}">
<#if 0 == user.userCommentStatus || (isLoggedIn && ("adminRole" == currentUser.userRole || currentUser.userName == user.userName))>
<div class="list">
    <ul>
        <#list userHomeComments as comment>
        <li class="fn-flex comment-list-item">
            <#if comment.commentArticleAuthorName != "someone">
            <a rel="nofollow" href="${servePath}/member/${comment.commentArticleAuthorName}" 
               title="${comment.commentArticleAuthorName}"></#if>
                <div class="avatar" style="background-image:url('${comment.commentArticleAuthorThumbnailURL}')"></div>
            <#if comment.commentArticleAuthorName != "someone">
            </a>
            </#if>
            <div class="fn-flex-1">
                <div>
                    <h2>
                        <@icon comment.commentArticlePerfect comment.commentArticleType></@icon>
                        <a rel="bookmark" href="${comment.commentSharpURL}">${comment.commentArticleTitle}</a>
                    </h2>
                    <span class="ft-gray fn-sub">
                        <svg><use xlink:href="#date"></use></svg>
                        ${comment.commentCreateTime?string('yyyy-MM-dd HH:mm')}  
                    </span>
                </div>

                <div class="content-reset comment">
                    ${comment.commentContent}
                </div>
            </div>
        </li>
        </#list>  
    </ul>
</div>
<@pagination url="${servePath}/member/${user.userName}/comments"/>
<#else>
<p class="ft-center ft-gray home-invisible">${setinvisibleLabel}</p>
</#if>
</@home>