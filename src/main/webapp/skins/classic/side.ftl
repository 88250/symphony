<#--

    Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
    Copyright (C) 2012-2018, b3log.org & hacpai.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

-->
<#include 'common/person-info.ftl'/>
<#if tag?? && tag.tagAd != ''>
    <div class="module">
        <div class="module-panel">
            ${tag.tagAd}
        </div>
    </div>
</#if>

<#if ADLabel!="" && ((tag?? && tag.tagShowSideAd == 0) || !tag??)>
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
                    <span class="avatar-small tooltipped tooltipped-se slogan"
                          aria-label="${hotArticle.articleAuthorName}"
                          style="background-image:url('${hotArticle.articleAuthorThumbnailURL20}')"></span>
                    <#if "someone" != hotArticle.articleAuthorName></a></#if>
                <a rel="nofollow" class="title" href="${servePath}${hotArticle.articlePermalink}">${hotArticle.articleTitleEmoj}</a>
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
                <#if "someone" != randomArticle.articleAuthorName>
                <a rel="nofollow" href="${servePath}/member/${randomArticle.articleAuthorName}"></#if>
                    <span aria-label="${randomArticle.articleAuthorName}"
                          style="background-image:url('${randomArticle.articleAuthorThumbnailURL20}')"
                          class="avatar-small tooltipped tooltipped-se slogan"></span>
                    <#if "someone" != randomArticle.articleAuthorName></a></#if>
                <a class="title" rel="nofollow" href="${servePath}${randomArticle.articlePermalink}">${randomArticle.articleTitleEmoj}</a>
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
        <ul class="fn-clear tags">
            <#list newTags as newTag>
            <li>
                <a class="ft-gray ft-13" rel="nofollow" href="${servePath}/tag/${newTag.tagURI}">${newTag.tagTitle}</a> &nbsp;
            </li>
            </#list>
        </ul>
    </div>
</div>
<div class="module">
    <div class="module-header">
        <h2>开源项目</h2>
    </div>
    <div class="module-panel">
        <ul class="module-list open-source">
            <li>
                <a target="_blank" rel="noopener" href="https://github.com/b3log/solo"><b class="ft-red slogan">【Solo】</b></a>
                <a class="title" target="_blank" rel="noopener" href="https://github.com/b3log/solo">GitHub 上 Star 数最多的 Java 博客</a>
            </li>
            <li class="last">
                <a target="_blank" rel="noopener" href="https://github.com/b3log/symphony"> <b class="ft-green slogan">【Sym】</b></a>
                <a class="title" target="_blank" rel="noopener" href="https://github.com/b3log/symphony">现代化的社区论坛系统</a>
            </li>
            <li class="last">
                <a target="_blank" rel="noopener" href="https://github.com/b3log/pipe"> <b class="ft-gray slogan">【Pipe】</b></a>
                <a class="title" target="_blank" rel="noopener" href="https://github.com/b3log/pipe">小而美的博客平台</a>
            </li>
            <li>
                <a target="_blank" rel="noopener" href="https://github.com/b3log/wide"><b class="ft-blue slogan">【Wide】</b></a>
                <a class="title" target="_blank" rel="noopener" href="https://github.com/b3log/wide">Golang 黑科技之在线 IDE </a>
            </li>
        </ul>
    </div>
</div>
</#if>
