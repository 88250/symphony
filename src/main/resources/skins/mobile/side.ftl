<#--

    Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
    Copyright (C) 2012-present, b3log.org

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
            <a href="${servePath}/about" class="fn-right ft-13 ft-gray" target="_blank">${wantPutOnLabel}</a>
        </h2>
    </div>
    <div class="module-panel ad fn-clear">
        ${ADLabel}
    </div>
</div>
</#if>

<div class="module">
    <div class="module-header form">
        <input id="breezemoonInput"
               type="text"
               class="comment__text breezemoon__input"
               placeholder="${breezemoonLabel}"/>
        <span id="breezemoonPostBtn" class="btn breezemoon__btn" data-csrf="${csrfToken}">${postLabel}</span>
    </div>
    <div class="module-panel">
        <ul class="module-list">
        <#list sideBreezemoons as item>
            <li>
                <a href="${servePath}/member/${item.breezemoonAuthorName}">
                    <span class="avatar-small slogan tooltipped tooltipped-se" aria-label="${item.breezemoonAuthorName}"
                          style="background-image: url(${item.breezemoonAuthorThumbnailURL48})"></span>
                </a>
                <a href="${servePath}/member/${item.breezemoonAuthorName}/breezemoons/${item.oId}"
                   class="title">${item.breezemoonContent}</a>
            </li>
        </#list>
            <#if sideBreezemoons?size == 0>
                <li class="ft-center ft-gray">${chickenEggLabel}</li>
            </#if>
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
                <a rel="nofollow" href="${servePath}/member/${hotArticle.articleAuthorName}">
                    <span class="avatar-small slogan" style="background-image:url('${hotArticle.articleAuthorThumbnailURL20}')"></span>
                </a>
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
                    <img src="${tag.tagIconPath}" alt="${tag.tagTitle}" /></#if>
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
