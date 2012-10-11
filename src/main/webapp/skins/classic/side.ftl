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
    <#list sideTags as sideTag>
    <a href="/tags/${sideTag.tagTitle?url('utf-8')}">${sideTag.tagTitle}</a>&nbsp;
    </#list>
</div>