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
    </head>
    <body>
        <#include "header.ftl">
        <div class="main tag-articles">
            <div class="content">
                <div class="fn-clear title wrapper">
                    <#if tag.tagIconPath != "">
                    <div class="avatar fn-left" style="background-image:url('${staticServePath}/images/tags/${tag.tagIconPath}')" alt="${tag.tagTitle}"></div>
                    </#if>
                    <h1 class="fn-inline">
                        <a rel="tag" 
                           title="${tag.tagURI}" 
                           href="${servePath}/tag/${tag.tagURI}">${tag.tagTitle}</a>
                    </h1> 
                    <#if tag.tagDomains?size gt 0>/</#if>
                    <#list tag.tagDomains as domain>
                    <a class="ft-gray" href="${servePath}/domain/${domain.domainURI}">${domain.domainTitle}</a>
                    </#list>
                    <span class="article-action">
                    <span class='fn-right'>
                        <#if isLoggedIn && isFollowing>
                        <span class="ft-red" onclick="Util.unfollow(this, '${tag.oId}', 'tag', ${tag.tagFollowerCount})"><svg class="icon-star"><use xlink:href="#star"></use></svg> ${tag.tagFollowerCount}</span>
                        <#else>
                        <span onclick="Util.follow(this, '${tag.oId}', 'tag', ${tag.tagFollowerCount})"><svg class="icon-star"><use xlink:href="#star"></use></svg> ${tag.tagFollowerCount}</span>
                        </#if>
                        <#if permissions["tagUpdateTagBasic"].permissionGrant> &nbsp;
                        <a href="${servePath}/admin/tag/${tag.oId}"><svg><use xlink:href="#setting"></use></svg></a>
                        </#if>
                    </span>
                    </span>
                </div>
                <#if tag.tagIconPath != "">
                <div class="fn-hr10"></div>
                <div class="wrapper content-reset">
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
                <div>
                    <ul class="status fn-flex">
                        <li>
                            <strong>${tag.tagReferenceCount?c}</strong>
                            <span class="ft-gray">${referenceLabel}</span>
                        </li>
                        <li>
                            <strong>${tag.tagCommentCount?c}</strong>
                            <span class="ft-gray">${cmtLabel}</span>
                        </li>
                        <li>
                            <strong>${tag.tagFollowerCount?c}</strong>
                            <span class="ft-gray">${followLabel}</span>
                        </li>
                    </ul>
                    <div class="tag-artile-user fn-clear">
                        <#if "someone" != tag.tagCreatorName>
                        <a rel="nofollow" class="fn-left" title="${creatorLabel} ${tag.tagCreatorName}" 
                           href="${servePath}/member/${tag.tagCreatorName}"></#if>
                            <div class="avatar" style="background-image:url('${tag.tagCreatorThumbnailURL}')"></div>
                        <#if "someone" != tag.tagCreatorName></a></#if>
                        <div class="fn-right">
                            <#list tag.tagParticipants as commenter>
                            <#if commenter_index < 4>
                            <#if "someone" != commenter.tagParticipantName>
                            <a rel="nofollow" class="fn-left" 
                               title="${contributorLabel} ${commenter.tagParticipantName}"
                               href="${servePath}/member/${commenter.tagParticipantName}"></#if>
                                <div class="avatar" style="background-image:url('${commenter.tagParticipantThumbnailURL}')"></div>
                            <#if "someone" != commenter.tagParticipantName></a></#if>
                            </#if>
                            </#list>
                        </div>
                    </div>
                </div>
                <div class="fn-clear">
                    <@list listData=articles/>
                    <@pagination url="${servePath}/tag/${tag.tagURI}"/>
                </div>
            </div> 
            <div class="side wrapper">
                <#include "side.ftl">
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
