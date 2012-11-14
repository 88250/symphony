<#macro list listData>
<div class="article-list list">
    <ul>
        <#list listData as article>
        <li>
            <div class="fn-clear">
                <a rel="nofollow" class="ft-noline" 
                   href="/member/${article.articleAuthorName}" 
                   title="${article.articleAuthorName}"><img class="avatar fn-left" src="${article.articleAuthorThumbnailURL}" /></a>
                <div class="fn-left" style="width: 550px">
                    <h2><a rel="bookmark" href="${article.articlePermalink}">${article.articleTitle}</a></h2>
                    <span class="ft-small">
                        <#list article.articleTags?split(",") as articleTag>
                        <a rel="tag" href="/tags/${articleTag?url('UTF-8')}">
                            ${articleTag}</a><#if articleTag_has_next>, </#if>
                        </#list>
                        <span class="date-ico">${article.articleCreateTime?string('yyyy-MM-dd HH:mm')}</span>
                    </span>
                </div>
            </div>
            <div class="count ft-small">
                ${viewLabel} <a rel="nofollow" href="${article.articlePermalink}">${article.articleViewCount}</a><br/>
                ${cmtLabel} <a rel="nofollow" href="${article.articlePermalink}#comments">${article.articleCommentCount}</a>
            </div>
            <div class="commenters">
                <#list article.articleParticipants as comment>
                <a rel="nofollow" href="${article.articlePermalink}#${comment.commentId}" title="${comment.articleParticipantName}" class="ft-noline">
                    <img class="avatar-small" src="${comment.articleParticipantThumbnailURL}" />
                </a>
                </#list>
            </div>
        </li>
        </#list>
    </ul>
</div>
</#macro>