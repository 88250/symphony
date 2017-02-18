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
                        <#if 1 == comment.commentArticlePerfect>
                        <svg height="20" viewBox="3 4 11 12" width="14">${perfectIcon}</svg>
                        </#if>
                        <#if comment.commentArticleType == 1>
                        <span class="icon-locked" title="${discussionLabel}"></span>
                        <#elseif comment.commentArticleType == 2>
                        <span class="icon-feed" title="${cityBroadcastLabel}"></span>
                        </#if>
                        <a rel="bookmark" href="${comment.commentSharpURL}">${comment.commentArticleTitle}</a>
                    </h2>
                    <span class="ft-gray fn-sub">
                        <span class="icon-date"></span>
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