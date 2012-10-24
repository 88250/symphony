<div class="index-module">
    AD
</div>
<div class="index-module">
    <h2>
        ${randomArticleLabel}
    </h2>
    <ul>
        <#list sideRandomArticles as randomArticle>
        <li>
            <a href="">${randomArticle.articleTitle}</a>
            <div class="ft-small">
                <span>
                    <a href="/${randomArticle.articleAuthorName}">${randomArticle.articleAuthorName}</a>
                    <span class="date-ico"> ${randomArticle.articleCreateTime?string('yyyy-MM-dd')}</span>
                </span>
            </div>
        </li>
        </#list>
    </ul>
</div>
<div class="index-module">
    <h2>
        ${tagLabel}
    </h2>
    <ul class="tags fn-clear">
        <#list sideTags as tag>
        <li>
            <span>
                <img src="${tag.tagIconPath}" />
                <a href="/tags/${tag.tagTitle?url('utf-8')}">${tag.tagTitle}</a>
            </span>
            <div <#if tag.tagDescription="">style="width:auto;white-space: nowrap;"</#if>>
                <div>${tag.tagDescription}</div>
                <span class="ft-small fn-right">
                    引用 ${tag.tagReferenceCount}
                    评论 ${tag.tagCommentCount} 
                </span>
            </div>
        </li>
        </#list>
    </ul>
</div>