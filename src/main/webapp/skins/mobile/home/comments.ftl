<#include "macro-home.ftl">
<#include "../macro-pagination.ftl">
<@home "comments">
<div class="list">
    <ul>
        <#list userHomeComments as comment>
        <li class="fn-flex comment-list-item">
            <a target="_blank" rel="nofollow" href="/member/${comment.commentArticleAuthorName}" 
               title="${comment.commentArticleAuthorName}">
                <div class="avatar" style="background-image:url('${comment.commentArticleAuthorThumbnailURL}-64.jpg?${comment.commenter.userUpdateTime?c}')"></div>
            </a>
            <div class="fn-flex-1">
                <div>
                    <h2>
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
</@home>