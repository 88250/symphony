<#if ADLabel!="">
<div class="module">
    <div class="module-header">
        <h2>
            ${sponsorLabel} 
            <a href="https://hacpai.com/article/1460083956075" class="fn-right ft-13 ft-gray" target="_blank">${wantPutOnLabel}</a>
        </h2>
    </div>
    <div class="module-panel ad fn-clear">
        ${ADLabel}
    </div>
</div>
</#if>
<div class="module">
    <div class="module-header">
        <h2>开源项目</h2>
    </div>
    <div class="module-panel">
        <ul class="module-list open-source">
            <li>
                <a target="_blank" href="https://github.com/b3log/solo"><b class="ft-red slogan">【Solo】</b></a>
                <a class="title" target="_blank" href="https://github.com/b3log/solo">GitHub 上 Star 数最多的 Java 博客</a>
            </li>
            <li>
                <a target="_blank" href="https://github.com/b3log/wide"><b class="ft-blue slogan">【Wide】</b></a>
                <a class="title" target="_blank" href="https://github.com/b3log/wide">Golang 黑科技之在线 IDE </a>
            </li>
            <li class="last">
                <a target="_blank" href="https://github.com/b3log/symphony"> <b class="ft-green slogan">【Sym】</b></a>
                <a class="title" target="_blank" href="https://github.com/b3log/symphony"> 黑客与画家的社区</a>
            </li>
        </ul>
    </div>
</div>
<#--
<#if navTrendTags?size!=0>
<div class="module">
    <div class="module-header">
        <h2>
            ${hotTopicLabel}
        </h2>
    </div>
    <div class="module-panel">
        <ul class="tags fn-clear">
            <#list navTrendTags as trendTag>
            <li>
                <a class="btn small" rel="nofollow" href="${servePath}/tag/${trendTag.tagURI}">${trendTag.tagTitle}</a>
            </li>
            </#list>
        </ul>
    </div>
</div>
</#if>
-->
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
                <#if "someone" != hotArticle.articleAuthorName>
                <a rel="nofollow" href="${servePath}/member/${hotArticle.articleAuthorName}"></#if>
                    <span class="avatar-small slogan"
                          style="background-image:url('${hotArticle.articleAuthorThumbnailURL20}')"></span>
                <#if "someone" != hotArticle.articleAuthorName></a></#if>
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
            ${recommendedTags}
        </h2>
    </div>
    <div class="module-panel">
        <ul class="tag-desc fn-clear">
            <#list sideTags as tag>
            <li>
                <a rel="nofollow" href="${servePath}/tag/${tag.tagURI}">
                    <#if tag.tagIconPath!="">
                    <img src="${staticServePath}/images/tags/${tag.tagIconPath}" alt="${tag.tagTitle}" /></#if>
                    ${tag.tagTitle}
                </a>
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
