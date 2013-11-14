<#include "macro-home.ftl">
<@home "comments">
<div class="list">
    <ul>
        <#list userHomeComments as comment>
        <li class="fn-clear">
            <a title="${comment.commentArticleAuthorName}"
               target="_blank" rel="nofollow" href="${comment.commentArticleAuthorURL}">
                <img class="avatar fn-left" src="${comment.commentArticleAuthorThumbnailURL}"/>
            </a>
            <h2 class="fn-inline"><a rel="bookmark" href="${comment.commentSharpURL}">${comment.commentArticleTitle}</a></h2>
            <span class="fn-right ft-small ico-date">
                ${comment.commentCreateTime?string('yyyy-MM-dd HH:mm')}  
            </span>    
            <div class="content-reset content-reset-p">
                ${comment.commentContent}
            </div>
        </li>
        </#list>  
    </ul>
</div>
<@pagination url="/member/${user.userName}/comments"/>
</@home>