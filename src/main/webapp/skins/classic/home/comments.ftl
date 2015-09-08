<#include "macro-home.ftl">
<#include "../macro-pagination.ftl">
<@home "comments">
<div class="list">
    <ul>
        <#list userHomeComments as comment>
        <li class="fn-flex">
            <a target="_blank" rel="nofollow" href="/member/${comment.commentArticleAuthorName}" 
               title="${comment.commentArticleAuthorName}" class="responsive-hide">
                <img class="avatar" src="${comment.commentArticleAuthorThumbnailURL}-64.jpg?${comment.commenter.userUpdateTime?c}"/>
            </a>
            <div class="fn-flex-1">
                <div class="fn-flex">
                    <h2 class="fn-flex-1">
                        <a target="_blank" rel="nofollow" href="/member/${comment.commentArticleAuthorName}" 
                           title="${comment.commentArticleAuthorName}">
                            <img class="avatar-small responsive-show" src="${comment.commentArticleAuthorThumbnailURL}-64.jpg?${comment.commenter.userUpdateTime?c}"/>
                        </a>
                        <a rel="bookmark" href="${comment.commentSharpURL}">${comment.commentArticleTitle}</a>
                    </h2>
                    <span class="ft-gray fn-sub list-info">
                        <span class="icon-date"></span>
                        ${comment.commentCreateTime?string('yyyy-MM-dd HH:mm')}  
                    </span>
                </div>

                <div class="content-reset comment list-info">
                    ${comment.commentContent}
                </div>
            </div>
        </li>
        </#list>  
    </ul>
</div>
<@pagination url="/member/${user.userName}/comments"/>
</@home>