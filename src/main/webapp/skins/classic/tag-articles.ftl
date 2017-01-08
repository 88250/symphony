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
        <link rel="stylesheet" href="${staticServePath}/css/index.css?${staticResourceVersion}" />
        ${tag.tagCSS}
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

                  
                            <span class="fn-right action-btns ft-fade ft-smaller">
                                <b class="article-level<#if tag.tagReferenceCount lt 40>${(tag.tagReferenceCount/1000)?int}<#else>4</#if>">${tag.tagReferenceCount?c}</b> ${referenceLabel}  &nbsp;•&nbsp;
                                <b class="article-level<#if tag.tagCommentCount lt 400>${(tag.tagCommentCount/100)?int}<#else>4</#if>">${tag.tagCommentCount?c}</b> ${cmtLabel} 
                                &nbsp;
                                <#if isLoggedIn && isFollowing>
                                <span class="tooltipped tooltipped-n ft-red" aria-label="${unfollowLabel} ${tag.tagFollowerCount}" onclick="Util.unfollow(this, '${tag.oId}', 'tag', ${tag.tagFollowerCount})"><span class="icon-star"></span> ${tag.tagFollowerCount}</span>
                                <#else>
                                <span class="tooltipped tooltipped-n" aria-label="${followLabel} ${tag.tagFollowerCount}" onclick="Util.follow(this, '${tag.oId}', 'tag', ${tag.tagFollowerCount})"><span class="icon-star"></span> ${tag.tagFollowerCount}</span>
                                </#if>
                                <#if permissions["tagUpdateTagBasic"].permissionGrant> &nbsp;
                                <a class="tooltipped tooltipped-n" href="${servePath}/admin/tag/${tag.oId}" aria-label="${adminLabel}"><span class="icon-setting"></span></a>
                                </#if>
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
                            <span class="fn-right ft-fade">
                                <a class="<#if "" == current>ft-gray</#if>" href="${servePath}/tag/${tag.tagURI}">
                                    ${defaultLabel}
                                </a>
                                /
                                <a class="<#if "/hot" == current>ft-gray</#if>" href="${servePath}/tag/${tag.tagURI}/hot">
                                    ${hotArticlesLabel}
                                </a>
                                /
                                <a class="<#if "/good" == current>ft-gray</#if>" href="${servePath}/tag/${tag.tagURI}/good">
                                    ${goodCmtsLabel}
                                </a>
                                /
                                <a class="<#if "/perfect" == current>ft-gray</#if>" href="${servePath}/tag/${tag.tagURI}/perfect">
                                    <svg height="16" viewBox="3 2 11 12" width="14">${perfectIcon}</svg>
                                    ${perfectLabel}
                                </a>
                                /
                                <a class="<#if "/reply" == current>ft-gray</#if>" href="${servePath}/tag/${tag.tagURI}/reply">
                                    ${recentCommentLabel}
                                </a>
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
