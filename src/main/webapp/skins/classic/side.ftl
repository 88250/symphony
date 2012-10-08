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
                    ${randomArticle.articleCreateTime?string('yyyy-MM-dd HH:mm:ss')}
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
    <a href="">${sideTag.tagTitle}</a>
    </#list>
</div>