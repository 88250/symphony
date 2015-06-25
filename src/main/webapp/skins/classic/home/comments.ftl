<#include "macro-home.ftl">
<#include "../macro-pagination.ftl">
<@home "comments">
<div class="list">
    <ul class="home-comments">
        <#list userHomeComments as comment>
        <li>
            <div class="fn-clear">
                <a title="${comment.commentArticleAuthorName}"
                   target="_blank" rel="nofollow" href="/member/${comment.commentArticleAuthorName}">
                    <img class="avatar-small fn-left" src="${comment.commentArticleAuthorThumbnailURL}"/>
                </a>
                &nbsp;
                <h2 class="fn-inline"><a rel="bookmark" href="${comment.commentSharpURL}">${comment.commentArticleTitle}</a></h2>
                <span class="fn-right ft-small">
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