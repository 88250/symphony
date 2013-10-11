<#if ADLabel!="">
<div class="index-module">
    ${ADLabel}
</div>
</#if>
<#if sideLatestCmts?size!=0>
<div class="index-module">
    <h2>
        ${recentCommentLabel}
    </h2>
    <ul class="index-module-list module-line">
        <#list sideLatestCmts as cmt>
        <li class="fn-clear<#if !cmt_has_next> last</#if>">
            <table cellpadding="0" cellspacing="0">
                <tr>
                    <td valign="top">
                        <a rel="nofollow" href="/member/${cmt.commenter.userName}" 
                           title="${cmt.commenter.userName}"><img class="avatar-small" src="${cmt.commenter.userThumbnailURL}" /></a>
                    </td>
                    <td valign="middle">
                        <#assign length = cmt.commentContent?length>
                        <#if length gt 32>
                        <#assign length = 32>
                        </#if>
                        <a rel="nofollow" class="comment-content"
                           title="${cmt.commentArticleTitle}" href="${cmt.commentSharpURL}">${cmt.commentContent?substring(0, length)}</a>
                    </td>
                </tr>
            </table>
        </li>
        </#list>
    </ul>
</div>
</#if>
<#if sideRandomArticles?size!=0>
<div class="index-module">
    <h2>
        ${randomArticleLabel}
    </h2>
    <ul class="index-module-list module-line">
        <#list sideRandomArticles as randomArticle>
        <li<#if !randomArticle_has_next> class="last"</#if>>
            <a rel="nofollow" href="${randomArticle.articlePermalink}">${randomArticle.articleTitle}</a>
            <a class="ft-small" rel="nofollow" href="/member/${randomArticle.articleAuthorName}">${randomArticle.articleAuthorName}</a>
        </li>
        </#list>
    </ul>
</div>
</#if>
<#if sideTags?size!=0>
<div class="index-module">
    <h2>
        ${tagLabel}
    </h2>
    <ul class="tags fn-clear">
        <#list sideTags as tag>
        <li>
            <span>
                <#if tag.tagIconPath!="">
                <img src="${staticServePath}/images/tags/${tag.tagIconPath}" /></#if><a rel="tag" href="/tags/${tag.tagTitle?url('utf-8')}">${tag.tagTitle}</a>
            </span>
            <div <#if tag.tagDescription="2">style="width:auto;white-space: nowrap;"</#if>>
                <div>${tag.tagDescription}</div>
                <span class="fn-right">
                    <span class="ft-small">${referenceLabel}</span> 
                    ${tag.tagReferenceCount} &nbsp;
                    <span class="ft-small">${cmtLabel}</span>
                    ${tag.tagCommentCount}&nbsp;
                </span>

            </div>
        </li>
        </#list>
    </ul>
</div>
</#if>