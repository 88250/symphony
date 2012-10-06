<div class="index-module">
    AD
</div>
<div class="index-module">
    <h2>
        Random Post
    </h2>
    <ul>
        <#list sideRandomArticles as randomArticle>
        <li>
            <a href="">${randomArticle.articleTitle}</a>
            <div class="ft-small">
                <span>
                    <a href="">vanesaa</a>
                    2011-1-1
                </span>
            </div>
        </li>
        </#list>
    </ul>
</div>
<div class="index-module">
    <h2>
        Tags
    </h2>
    <#list sideTags as sideTag>
    <a href="">${sideTag.tagTitle}</a>
    </#list>
</div>