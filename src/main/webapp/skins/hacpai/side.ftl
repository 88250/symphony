<#include 'common/person-info.ftl'/>

<#if ADLabel!="">
<div class="module">
    <div class="module-header nopanel">
        ${ADLabel}
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
                    <img src="${staticServePath}/images/tags/${tag.tagIconPath}" /></#if><a rel="tag" href="/tag/${tag.tagTitle?url('utf-8')}">${tag.tagTitle}</a>
                </span>
                <div<#if tag.tagDescription == ''> style="width:auto"</#if>>
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
                <a rel="nofollow" title="${hotArticle.articleTitle}" href="${hotArticle.articlePermalink}">${hotArticle.articleTitle}</a>
                <a class="ft-small" rel="nofollow" href="/member/${hotArticle.articleAuthorName}" 
                   title="${hotArticle.articleAuthorName}">${hotArticle.articleAuthorName}</a>
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
                <a rel="nofollow" href="${randomArticle.articlePermalink}">${randomArticle.articleTitle}</a>
                <a class="ft-small" rel="nofollow" href="/member/${randomArticle.articleAuthorName}">${randomArticle.articleAuthorName}</a>
            </li>
            </#list>
        </ul>
    </div>
</div>
</#if>
