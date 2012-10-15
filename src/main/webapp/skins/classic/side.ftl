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
            <span>
                <img src="/favicon.png" />
                <a href="/tags/${sideTag.tagTitle?url('utf-8')}">${sideTag.tagTitle}</a>
            </span>
            <div>
                你好，世界！ (75)你好，世界！ (75)你好，世界！ (75)你好，世界！ (75)
                引用 ${sideTag.tagReferenceCount}<br/>
                评论 ${sideTag.tagCommentCount} 
            </div>
        </li>
        </#list>
    </ul>
</div>