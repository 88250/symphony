<#include "macro-head.ftl">
<#include "macro-list.ftl">
<#include "macro-pagination.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${symphonyLabel} - ${tag.tagTitle}">
        <meta name="description" content="${tag.tagCreatorName},${tag.tagDescription}"/>
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index${miniPostfix}.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "header.ftl">
        <div class="main tag-articles">
            <div class="wrapper">
                <div class="content">
                    <div class="fn-clear title">
                        <#if tag.tagIconPath != "">
                        <img class="tag-img fn-left" src="${staticServePath}/images/tags/${tag.tagIconPath}">
                        </#if>
                        <h1 class="fn-inline">
                            <a rel="tag" 
                               title="${tag.tagTitle?url('UTF-8')}" 
                               href="/tag/${tag.tagTitle?url('utf-8')}">${tag.tagTitle}</a>
                        </h1>
                        <#if isLoggedIn> &nbsp;
                        <#if isFollowing>
                        <button class="red small" onclick="Util.unfollow(this, '${tag.oId}', 'tag')"> 
                            ${unfollowLabel}
                        </button>
                        <#else>
                        <button class="green small" onclick="Util.follow(this, '${tag.oId}', 'tag')"> 
                            ${followLabel}
                        </button>
                        </#if>
                        </#if>
                        <#if isAdminLoggedIn> &nbsp;
                        <a class="ft-small icon icon-setting" href="/admin/tag/${tag.oId}" title="${adminLabel}"></a>
                        </#if>
                    </div>
                    <p<#if tag.tagIconPath != ""> class="description" </#if>>
                        ${tag.tagDescription}
                </p>
                <div class="fn-clear">
                    <br/>
                    <ul class="tags">
                        <#list tag.tagRelatedTags as relatedTag>
                        <li>
                            <span>
                                <#if relatedTag.tagIconPath != "">
                                <img src="${staticServePath}/images/tags/${relatedTag.tagIconPath}" /></#if><a rel="tag" href="/tag/${relatedTag.tagTitle?url('utf-8')}">${relatedTag.tagTitle}</a>
                            </span>
                            <div<#if relatedTag.tagDescription == ''> style="width:auto"</#if>>
                                <div>${relatedTag.tagDescription}</div>
                                <span class="fn-right">
                                    <span class="ft-small">${referenceLabel}</span> 
                                    ${relatedTag.tagReferenceCount} &nbsp;
                                    <span class="ft-small">${cmtLabel}</span>
                                    ${relatedTag.tagCommentCount}&nbsp;
                                </span>
                            </div>
                        </li>
                        </#list>
                    </ul>
                </div>
                <div class="fn-flex">
                    <ul class="status fn-flex fn-flex-1">
                        <li>
                            <strong>${tag.tagReferenceCount}</strong>
                            <span class="ft-small">${referenceLabel}</span>
                        </li>
                        <li>
                            <strong>${tag.tagCommentCount}</strong>
                            <span class="ft-small">${cmtLabel}</span>
                        </li>
                        <li>
                            <strong>${tag.tagFollowerCount}</strong>
                            <span class="ft-small">${followLabel}</span>
                        </li>
                    </ul>
                    <div class="tag-artile-user">
                        <a rel="nofollow" class="fn-left" title="${creatorLabel} ${tag.tagCreatorName}" 
                           href="/member/${tag.tagCreatorName}">
                            <img class="avatar" src="${tag.tagCreatorThumbnailURL}-64.jpg?${tag.tagCreatorThumbnailUpdateTime?c}">
                        </a>
                        <div class="fn-right">
                            <#list tag.tagParticipants as commenter>
                            <a rel="nofollow" class="fn-left" 
                               title="${contributorLabel} ${commenter.tagParticipantName}"
                               href="/member/${commenter.tagParticipantName}">
                                <img class="avatar" src="${commenter.tagParticipantThumbnailURL}-64.jpg?${commenter.tagParticipantThumbnailUpdateTime?c}">
                            </a>
                            </#list>
                        </div>
                    </div>
                </div>
                <div class="fn-clear">
                    <@list listData=articles/>
                    <@pagination url="/tag/${tag.tagTitle?url('utf-8')}"/>
                </div>
            </div> 
            <div class="side">
                <#include "side.ftl">
            </div>
        </div>
    </div>
    <#include "footer.ftl">
    <script>
        Util.initArticlePreview();
    </script>
</body>
</html>
