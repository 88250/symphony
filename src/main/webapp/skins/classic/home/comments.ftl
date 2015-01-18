<#include "macro-home.ftl">
<@home "comments">
<div class="list">
    <ul class="home-comments">
        <#list userHomeComments as comment>
        <li class="fn-clear">
            <a title="${comment.commentArticleAuthorName}"
               target="_blank" rel="nofollow" href="${comment.commentArticleAuthorURL}">
                <img class="avatar fn-left" src="${comment.commentArticleAuthorThumbnailURL}"/>
            </a>
            <h2 class="fn-inline"><a rel="bookmark" href="${comment.commentSharpURL}">${comment.commentArticleTitle}</a></h2>
            <span class="fn-right ft-small">
                <span class="icon icon-date"></span>
                ${comment.commentCreateTime?string('yyyy-MM-dd HH:mm')}  
            </span>    
            <div class="content-reset comment">
                ${comment.commentContent}
            </div>
        </li>
        </#list>  
    </ul>
</div>
<@pagination url="/member/${user.userName}/comments"/>
</@home>