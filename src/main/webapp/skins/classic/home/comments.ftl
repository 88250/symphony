<#include "macro-home.ftl">
<#include "../macro-pagination.ftl">
<@home "${type}">
<div class="tabs-sub fn-clear">
    <a href="${servePath}/member/${user.userName}"<#if type == "home"> class="current"</#if>>${articleLabel}</a>
    <a href="${servePath}/member/${user.userName}/comments"<#if type == "comments"> class="current"</#if>>${cmtLabel}</a>
    <#if currentUser?? && currentUser.userName == user.userName>
    <a<#if type == "articlesAnonymous"> class="current"</#if> href="${servePath}/member/${user.userName}/articles/anonymous">${anonymousArticleLabel}</a>
    <a<#if type == "commentsAnonymous"> class="current"</#if> href="${servePath}/member/${user.userName}/comments/anonymous">${anonymousCommentLabel}</a>
    </#if>
</div>
<#if 0 == user.userCommentStatus || (isLoggedIn && ("adminRole" == currentUser.userRole || currentUser.userName == user.userName))>
<div class="list">
    <ul>
        <#list userHomeComments as comment>
        <li class="fn-flex comment-list-item">
            <#if comment.commentArticleAuthorName != "someone">
            <a target="_blank" class="tooltipped tooltipped-s" rel="nofollow" href="${servePath}/member/${comment.commentArticleAuthorName}" 
               aria-label="${comment.commentArticleAuthorName}"></#if>
                <div class="avatar" style="background-image:url('${comment.commentArticleAuthorThumbnailURL}?imageView2/1/w/64/h/64/interlace/0/q/80')"></div>
            <#if comment.commentArticleAuthorName != "someone">
            </a>
            </#if>
            <div class="fn-flex-1">
                <div class="fn-flex">
                    <h2 class="fn-flex-1">
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
<@pagination url="/member/${user.userName}/comments"/>
<#else>
<p class="ft-center ft-gray home-invisible">${setinvisibleLabel}</p>
</#if>
</@home>