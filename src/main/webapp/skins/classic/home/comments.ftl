<#include "macro-home.ftl">
<@home "comments">
<div class="list">
    <ul>
        <#list userHomeComments as comment>
        <li class="fn-clear">
            <img class="avatar fn-left" src="${comment.commentArticleAuthorThumbnailURL}"/>
            <a target="_blank" rel="nofollow" href="${comment.commentArticleAuthorURL}">${comment.commentArticleAuthorName}</a>
            <span class="ft-small">${creatThemeLabel}</span>
            <a rel="bookmark" href="${comment.commentSharpURL}">${comment.commentArticleTitle}</a>
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