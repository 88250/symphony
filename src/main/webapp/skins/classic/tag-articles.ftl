<#include "macro-head.ftl">
<#include "macro-list.ftl">
<#include "macro-pagination.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${symphonyLabel} - ${tag.tagTitle}">
        <meta name="keywords" content="${tag.tagTitle},${tag.tagCreatorName}"/>
        <meta name="description" content="${tag.tagCreatorName},${tag.tagDescription}"/>
        </@head>
        <link type="text/css" rel="stylesheet" href="/css/index${miniPostfix}.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "header.ftl">
        <div class="main tag-articles">
            <div class="wrapper fn-clear">
                <div class="content">
                    <div class="fn-clear">
                        <#if tag.tagIconPath != "">
                        <img class="tag-img fn-left" src="${staticServePath}/images/tags/${tag.tagIconPath}">
                        </#if>
                        <div>
                            <h1 class="fn-inline">
                                <a rel="tag" 
                                   title="${tag.tagTitle?url('UTF-8')}" 
                                   href="/tags/${tag.tagTitle?url('utf-8')}">${tag.tagTitle}</a>
                            </h1>
                            <#if isAdminLoggedIn>
                            <a class="ft-small icon icon-setting" href="${servePath}/admin/tag/${tag.oId}" title="${adminLabel}"></a>
                            </#if>
                            <#if isLoggedIn> 
                            <#if isFollowing>
                            <button class="red fn-right" onclick="Util.unfollow(this, '${tag.oId}', 'tag')"> 
                                ${unfollowLabel}
                            </button>
                            <#else>
                            <button class="green fn-right" onclick="Util.follow(this, '${tag.oId}', 'tag')"> 
                                ${followLabel}
                            </button>
                            </#if>
                            </#if>
                        </div>
                        ${tag.tagDescription}
                    </div>
                    <div class="fn-clear">
                        <br/>
                        <ul class="tags">
                            <#list tag.tagRelatedTags as relatedTag>
                            <li>
                                <span>
                                    <#if relatedTag.tagIconPath != "">
                                    <img src="${staticServePath}/images/tags/${relatedTag.tagIconPath}" /></#if><a rel="tag" href="/tags/${relatedTag.tagTitle?url('utf-8')}">${relatedTag.tagTitle}</a>
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
                    <div class="fn-clear">
                        <div class="fn-left">
                            <ul class="status fn-clear">
                                <li>
                                    <strong>${tag.tagReferenceCount}</strong>
                                    <span class="ft-small">${referenceLabel}</span>
                                </li>
                                <li>
                                    <strong>${tag.tagCommentCount}</strong>
                                    <span class="ft-small">${cmtLabel}</span>
                                </li>
                            </ul>
                        </div>
                        <div class="tag-artile-user">
                            <a rel="nofollow" class="fn-left" title="${creatorLabel}:${tag.tagCreatorName}" 
                               href="/member/${tag.tagCreatorName}">
                                <img class="avatar" src="${tag.tagCreatorThumbnailURL}">
                            </a>
                            <div class="fn-right">
                                <#list tag.tagParticipants as commenter>
                                <a rel="nofollow" class="fn-left" 
                                   title="${contributorLabel}:${commenter.tagParticipantName}"
                                   href="/member/${commenter.tagParticipantName}">
                                    <img class="avatar" src="${commenter.tagParticipantThumbnailURL}">
                                </a>
                                </#list>
                            </div>
                        </div>
                    </div>
                    <div class="fn-clear">
                        <@list listData=articles/>
                        <@pagination url="/tags/${tag.tagTitle?url('utf-8')}"/>
                    </div>
                </div> 
                <div class="side">
                    <#include "side.ftl">
                </div>
            </div>
        </div>
        <#include "footer.ftl">
    </body>
</html>
