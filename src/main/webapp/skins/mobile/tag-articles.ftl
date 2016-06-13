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
                           title="${tag.tagTitle?url('UTF-8')}" 
                           href="/tag/${tag.tagTitle?url('utf-8')}">${tag.tagTitle}</a>
                    </h1> 
                    <#if tag.tagDomains?size gt 0>/</#if>
                    <#list tag.tagDomains as domain>
                    <a class="ft-gray" href="/domain/${domain.domainURI}">${domain.domainTitle}</a>
                    </#list>
                    <span class='fn-right'>
                        <#if isLoggedIn> &nbsp;
                        <#if isFollowing>
                        <span class="icon-star ft-red fn-pointer" title="${unfollowLabel}" onclick="Util.unfollow(this, '${tag.oId}', 'tag', 'tag-articles')"></span>
                        <#else>
                        <span class="icon-star ft-gray fn-pointer" title="${followLabel}" onclick="Util.follow(this, '${tag.oId}', 'tag', 'tag-articles')"></span>
                        </#if>
                        </#if>
                        <#if isAdminLoggedIn> &nbsp;
                        <a class="icon-setting" href="/admin/tag/${tag.oId}" title="${adminLabel}"></a>
                        </#if>
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
                        <span>
                            <#if relatedTag.tagIconPath != "">
                            <img src="${staticServePath}/images/tags/${relatedTag.tagIconPath}" alt="${relatedTag.tagTitle}" /></#if><a rel="tag" href="/tag/${relatedTag.tagTitle?url('utf-8')}">${relatedTag.tagTitle}</a>
                        </span>
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
                        <a rel="nofollow" class="fn-left" title="${creatorLabel} ${tag.tagCreatorName}" 
                           href="/member/${tag.tagCreatorName}">
                            <div class="avatar" style="background-image:url('${tag.tagCreatorThumbnailURL}-64.jpg?${tag.tagCreatorThumbnailUpdateTime?c}')"></div>
                        </a>
                        <div class="fn-right">
                            <#list tag.tagParticipants as commenter>
                            <#if commenter_index < 4>
                            <a rel="nofollow" class="fn-left" 
                               title="${contributorLabel} ${commenter.tagParticipantName}"
                               href="/member/${commenter.tagParticipantName}">
                                <div class="avatar" style="background-image:url('${commenter.tagParticipantThumbnailURL}-64.jpg?${commenter.tagParticipantThumbnailUpdateTime?c}')"></div>
                            </a>
                            </#if>
                            </#list>
                        </div>
                    </div>
                </div>
                <div class="fn-clear">
                    <@list listData=articles/>
                    <@pagination url="/tag/${tag.tagTitle?url('utf-8')}"/>
                </div>
            </div> 
            <div class="side wrapper">
                <#include "side.ftl">
            </div>
        </div>
        <#include "footer.ftl">
        <script>
            <#if (isLoggedIn && !tag.isReserved) || (tag.isReserved && isAdminLoggedIn)>
            $('.person-info .btn.red').attr('onclick', 'window.location = "/post?tags=${tag.tagTitle?url('utf-8')}&type=0"');
            </#if>
        </script>
    </body>
</html>
