<#include "macro-home.ftl">
<#include "../macro-pagination.ftl">
<@home "${type}">
<div class="tabs-sub fn-clear">
    <a pjax-title="${articleLabel} - ${user.userName} - ${symphonyLabel}" href="${servePath}/member/${user.userName}"<#if type == "home"> class="current"</#if>>${articleLabel}</a>
    <a pjax-title="${cmtLabel} - ${user.userName} - ${symphonyLabel}" href="${servePath}/member/${user.userName}/comments"<#if type == "comments"> class="current"</#if>>${cmtLabel}<#if type == "comments"> &nbsp;<span class="count">${paginationRecordCount?c}</span></#if></a>
    <#if currentUser?? && currentUser.userName == user.userName>
    <a pjax-title="${anonymousArticleLabel} - ${user.userName} - ${symphonyLabel}"<#if type == "articlesAnonymous"> class="current"</#if> href="${servePath}/member/${user.userName}/articles/anonymous">${anonymousArticleLabel}<#if type == "articlesAnonymous"> &nbsp;<span class="count">${paginationRecordCount?c}</span></#if></a>
    <a pjax-title="${anonymousCommentLabel} - ${user.userName} - ${symphonyLabel}"<#if type == "commentsAnonymous"> class="current"</#if> href="${servePath}/member/${user.userName}/comments/anonymous">${anonymousCommentLabel}<#if type == "commentsAnonymous"> &nbsp;<span class="count">${paginationRecordCount?c}</span></#if></a>
    </#if>
</div>
<#if 0 == user.userCommentStatus || (isLoggedIn && ("adminRole" == currentUser.userRole || currentUser.userName == user.userName))>
<div class="list">
    <#if userHomeComments?size == 0>
        <p class="ft-center ft-gray home-invisible">${chickenEggLabel}</p>
    </#if>
    <ul>
        <#list userHomeComments as comment>
        <li class="fn-flex comment-list-item
        <#if !(paginationPageCount?? && paginationPageCount!=0 && paginationPageCount!=1) && comment_index == userHomeComments?size - 1>
            last</#if>">
            <div>
            <#if comment.commentArticleAuthorName != "someone">
            <a target="_blank" class="tooltipped tooltipped-n" rel="nofollow" href="${servePath}/member/${comment.commentArticleAuthorName}"
               aria-label="${comment.commentArticleAuthorName}"></#if>
                <div class="avatar" style="background-image:url('${comment.commentArticleAuthorThumbnailURL}')"></div>
            <#if comment.commentArticleAuthorName != "someone">
            </a>
            </#if>
            </div>
            <div class="fn-flex-1">
                <div class="fn-flex">
                    <h2 class="fn-flex-1">
                        <#if 1 == comment.commentArticlePerfect>
                        <span class="tooltipped tooltipped-n" aria-label="${perfectLabel}"><svg height="20" viewBox="3 3 11 12" width="14">${perfectIcon}</svg></span>
                        </#if>
                        <#if 1 == comment.commentArticleType>
                        <span class="tooltipped tooltipped-n" aria-label="${discussionLabel}"><span class="icon-locked"></span></span>
                        <#elseif 2 == comment.commentArticleType>
                        <span class="tooltipped tooltipped-n" aria-label="${cityBroadcastLabel}"><span class="icon-feed"></span></span>
                        <#elseif 3 == comment.commentArticleType>
                        <span class="tooltipped tooltipped-n" aria-label="${thoughtLabel}"><span class="icon-video"></span></span>
                        </#if>
                        <a rel="bookmark" href="${comment.commentSharpURL}">${comment.commentArticleTitle}</a>
                    </h2>
                    <span class="ft-gray">
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
<@pagination url="${servePath}/member/${user.userName}/comments" pjaxTitle="${cmtLabel} - ${user.userName} - ${symphonyLabel}"/>
<#else>
<p class="ft-center ft-gray home-invisible">${setinvisibleLabel}</p>
</#if>
</@home>