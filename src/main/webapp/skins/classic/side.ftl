<div class="index-module">
    AD
</div>
<div class="index-module">
    <h2>
        Recent Post
    </h2>
    <ul>
        <#list recentArticles as recentArticle>
        <li class="fn-ellipsis">
            <a href="">${recentArticle.articleTitle}</a>
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