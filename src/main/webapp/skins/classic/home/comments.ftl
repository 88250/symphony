<#include "macro-home.ftl">
<#include "../macro-pagination.ftl">
<@home "comments">
<div class="list">
    <ul class="home-comments">
        <#list userHomeComments as comment>
        <li>
            <div class="fn-flex">
                <h2 class="fn-flex-1">
                    <a title="${comment.commentArticleAuthorName}" class="ft-small"
                       target="_blank" rel="nofollow" href="/member/${comment.commentArticleAuthorName}">
                        <img class="avatar-small" src="${comment.commentArticleAuthorThumbnailURL}"/>
                    </a>
                    <a rel="bookmark" href="${comment.commentSharpURL}">${comment.commentArticleTitle}</a>
                </h2>
                <span class="ft-small">
                    <span class="icon icon-date"></span>
                    ${comment.commentCreateTime?string('yyyy-MM-dd HH:mm')}  
                </span>    
            </div>
            <div class="content-reset comment">
                ${comment.commentContent}
            </div>
        </li>
        </#list>  
    </ul>
</div>
<@pagination url="/member/${user.userName}/comments"/>
</@home>