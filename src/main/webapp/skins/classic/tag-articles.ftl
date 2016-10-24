<#include "macro-head.ftl">
<#include "macro-list.ftl">
<#include "macro-pagination.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${tag.tagSeoTitle} - ${symphonyLabel}">
        <meta name="keywords" content="${tag.tagSeoKeywords}"/>
        <meta name="description" content="${tag.tagSeoDesc}"/>
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "header.ftl">
        <div class="main tag-articles">
            <div class="wrapper">
                <div class="content">
                    <div class="module article-module">
                        <div class="fn-clear">
                            <#if tag.tagIconPath != "">
                            <div class="avatar fn-left" style="background-image:url('${staticServePath}/images/tags/${tag.tagIconPath}')" alt="${tag.tagTitle}"></div>
                            </#if>
                            <h1 class="fn-inline">
                                <a rel="tag"
                                   href="${servePath}/tag/${tag.tagURI}">${tag.tagTitle}</a>
                            </h1> 
                            <#if tag.tagDomains?size gt 0>/</#if>
                            <#list tag.tagDomains as domain>
                            <a class="ft-gray" href="${servePath}/domain/${domain.domainURI}">${domain.domainTitle}</a>
                            </#list> 

                            <span>
                                <span class="fn-right action-btns">
                                <span class="tooltipped tooltipped-n counts" aria-label="${referenceLabel} / ${cmtLabel} / ${followLabel}">
                                    ${tag.tagReferenceCount?c} /
                                    ${tag.tagCommentCount?c} /
                                    ${tag.tagFollowerCount?c}
                                </span>
                                <#if isLoggedIn && isFollowing>
                                <span class="tooltipped tooltipped-n fn-pointer" aria-label="${unfollowLabel} ${tag.tagFollowerCount}" onclick="Util.unfollow(this, '${tag.oId}', 'tag', ${tag.tagFollowerCount})"><span class="ft-red icon-star"></span></span>
                                <#else>
                                <span class="tooltipped tooltipped-n fn-pointer" aria-label="${followLabel} ${tag.tagFollowerCount}" onclick="Util.follow(this, '${tag.oId}', 'tag', ${tag.tagFollowerCount})"><span class="icon-star"></span></span>
                                </#if>
                                <#if isAdminLoggedIn> &nbsp;
                                <a class="tooltipped tooltipped-n" href="${servePath}/admin/tag/${tag.oId}" aria-label="${adminLabel}"><span class="icon-setting"></span></a>
                                </#if>
                                </span>
                            </span>
                        </div>
                        <#if tag.tagIconPath != "">
                        <div class="ft-smaller">
                            ${tag.tagDescription}
                        </div>
                        </#if>
                        <ul class="tag-desc fn-clear tag-articles-tag-desc">
                            <#list tag.tagRelatedTags as relatedTag>
                            <li>
                                <a rel="tag" href="${servePath}/tag/${relatedTag.tagURI}">
                                    <#if relatedTag.tagIconPath != "">
                                    <img src="${staticServePath}/images/tags/${relatedTag.tagIconPath}" alt="${relatedTag.tagTitle}" /></#if>
                                    ${relatedTag.tagTitle}</a>
                                <div<#if relatedTag.tagDescription == ''> style="width:auto"</#if>>
                                    <div>${relatedTag.tagDescription}</div>
                                    <span class="fn-right">
                                        <span class="ft-gray">${referenceLabel}</span> 
                                        ${relatedTag.tagReferenceCount?c} &nbsp;
                                        <span class="ft-gray">${cmtLabel}</span>
                                        ${relatedTag.tagCommentCount?c}&nbsp;
                                    </span>
                                </div>
                            </li>
                            </#list>
                        </ul>
                    </div>
                    <#if articles?size != 0>
                    <div class="module">
                        <div class="module-header fn-clear">
                            <span class="fn-right">
                                <a<#if "" == current> class="ft-gray"</#if> href="${servePath}/tag/${tag.tagURI}">${default1Label}</a>
                                <span class="ft-fade">/</span>
                                <a<#if "/hot" == current> class="ft-gray"</#if> href="${servePath}/tag/${tag.tagURI}/hot">${hotArticlesLabel}</a>
                                <span class="ft-fade">/</span>
                                <a<#if "/good" == current> class="ft-gray"</#if> href="${servePath}/tag/${tag.tagURI}/good">${goodCmtsLabel}</a>
                                <span class="ft-fade">/</span>
                                <a<#if "/perfect" == current> class="ft-gray"</#if> href="${servePath}/tag/${tag.tagURI}/perfect">${perfectLabel}</a>
                                <span class="ft-fade">/</span>
                                <a<#if "/reply" == current> class="ft-gray"</#if> href="${servePath}/tag/${tag.tagURI}/reply">${recentCommentLabel}</a>
                            </span>
                        </div>
                        <@list listData=articles/>
                        <@pagination url="${servePath}/tag/${tag.tagURI}${current}"/>
                    </div>
                    </#if>
                </div> 
                <div class="side">
                    <#include "side.ftl">
                </div>
            </div>
        </div>
        <#include "footer.ftl">
        <@listScript/>
        <script>
            <#if (isLoggedIn && !tag.isReserved) || (tag.isReserved && isAdminLoggedIn)>
            $('.person-info .btn.red').attr('onclick', 'window.location = "/post?tags=${tag.tagURI}&type=0"');
            </#if>
        </script>
    </body>
</html>
