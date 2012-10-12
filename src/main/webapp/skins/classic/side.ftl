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
        <#list sideTags as sideTag>
        <li>
            <a class="fn-left" href="/tags/${sideTag.tagTitle?url('utf-8')}">${sideTag.tagTitle}</a>
            <div class="fn-left ft-small">
                引用 ${sideTag.tagReferenceCount}<br/>
                评论 ${sideTag.tagCommentCount} 
            </div>
        </li>
        </#list>
    </ul>
</div>