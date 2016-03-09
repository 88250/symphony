<#include 'common/person-info.ftl'/>

<#if ADLabel!="">
<div class="module">
    <div class="module-header nopanel">
        ${ADLabel}
    </div>
</div>
</#if>
<div class="module">
    <div class="module-header">
        <h2>开源项目</h2>
    </div>
    <div class="module-panel">
        <ul class="module-list ad">
            <li>
                <b class="ft-red slogan">【Solo】</b>
                <a class="title" target="_blank" href="https://github.com/b3log/solo">GitHub 上 Star 数最多的 Java 博客</a>
            </li>
            <li>
                <b class="slogan ft-blue">【Wide】</b>
                <a class="title" target="_blank" href="https://github.com/b3log/wide">Golang 黑科技之在线 IDE </a>
            </li>
            <li class="last">
                <b class="ft-green slogan">【Sym】</b>
                <a class="title" target="_blank" href="https://github.com/b3log/symphony"> 黑客与画家的社区</a>
            </li>
        </ul>
    </div>
</div>
<#if navTrendTags?size!=0>
<div class="module">
    <div class="module-header">
        <h2>
            ${hotTopicLabel}
        </h2>
    </div>
    <div class="module-panel">
        <ul class="tags fn-clear tags-no-desc">
            <#list navTrendTags as trendTag>
            <li>
                <span><a rel="nofollow" href="/tags/${trendTag.tagTitle?url('UTF-8')}">${trendTag.tagTitle}</a></span>
            </li>
            </#list>
        </ul>
    </div>
</div>
</#if>
<#if sideHotArticles?size!=0>
<div class="module">
    <div class="module-header">
        <h2>
            ${hotArticleLabel}
        </h2>
    </div>
    <div class="module-panel">
        <ul class="module-list">
            <#list sideHotArticles as hotArticle>
            <li<#if !hotArticle_has_next> class="last"</#if>>
                <a class="avatar-small slogan" rel="nofollow" href="/member/${hotArticle.articleAuthorName}" 
               style="background-image:url('http://symx.fangstar.net/upload/f41bd2595fd241bdac153a45def8f68d.jpeg-64.jpg?1457429779882')"
               title="${hotArticle.articleAuthorName}"></a>
                <a rel="nofollow" class="title" title="${hotArticle.articleTitle}" href="${hotArticle.articlePermalink}">${hotArticle.articleTitleEmoj}</a>
            </li>
            </#list>
        </ul>
    </div>
</div>
</#if>
<#if sideTags?size!=0>
<div class="module">
    <div class="module-header">
        <h2>
            ${tagLabel}
        </h2>
    </div>
    <div class="module-panel">
        <ul class="tags fn-clear">
            <#list sideTags as tag>
            <li>
                <span>
                    <#if tag.tagIconPath!="">
                    <img src="${staticServePath}/images/tags/${tag.tagIconPath}" alt="${tag.tagTitle}" /></#if><a rel="nofollow" href="/tags/${tag.tagTitle?url('utf-8')}">${tag.tagTitle}</a>
                </span>
                <div<#if tag.tagDescription == ''> style="width:auto"</#if>>
                    <div>${tag.tagDescription}</div>
                    <span class="fn-right">
                        <span class="ft-gray">${referenceLabel}</span> 
                        ${tag.tagReferenceCount} &nbsp;
                        <span class="ft-gray">${cmtLabel}</span>
                        ${tag.tagCommentCount}&nbsp;
                    </span>

                </div>
            </li>
            </#list>
        </ul>
    </div>
</div>
</#if>
<#if sideRandomArticles?size!=0>
<div class="module">
    <div class="module-header">
        <h2>
            ${randomArticleLabel}
        </h2>
    </div>
    <div class="module-panel">
        <ul class="module-list">
            <#list sideRandomArticles as randomArticle>
            <li<#if !randomArticle_has_next> class="last"</#if>>
                  <a class="avatar-small slogan" rel="nofollow" href="/member/${randomArticle.articleAuthorName}"
               title="${randomArticle.articleAuthorName}"
               style="background-image:url('http://symx.fangstar.net/upload/f41bd2595fd241bdac153a45def8f68d.jpeg-64.jpg?1457429779882')"></a>
                <a class="title" rel="nofollow" title="${randomArticle.articleTitle}" href="${randomArticle.articlePermalink}">${randomArticle.articleTitleEmoj}</a>
            </li>
            </#list>
        </ul>
    </div>
</div>
</#if>
<#if newTags?size!=0>
<div class="module">
    <div class="module-header">
        <h2>
            ${newTagLabel}
        </h2>
    </div>
    <div class="module-panel">
        <ul class="tags fn-clear tags-no-desc">
            <#list newTags as newTag>
            <li>
                <span><a rel="nofollow" href="/tags/${newTag.tagTitle?url('UTF-8')}">${newTag.tagTitle}</a></span>
            </li>
            </#list>
        </ul>
    </div>
</div>
</#if>
