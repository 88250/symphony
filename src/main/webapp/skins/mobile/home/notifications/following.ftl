<#include "macro-notifications.ftl">
<@notifications "following">
<#if followingNotifications?size != 0>
<ul class="notification">
    <#list followingNotifications as notification>
    <li class="fn-flex<#if notification.hasRead> read</#if>">
        <#if "someone" != notification.authorName>
        <a target="_blank" rel="nofollow" href="${servePath}/member/${notification.authorName}" 
           title="${notification.authorName}"></#if>
            <div class="avatar" style="background-image:url('${notification.thumbnailURL}')"></div>
        <#if "someone" != notification.authorName></a></#if>

        <#if notification.isComment>
            <div class="fn-flex-1">
                <div class="fn-flex">
                    <h2 class="fn-flex-1">
                        <@icon notification.articlePerfect notification.articleType></@icon>
                        <a rel="bookmark" href="${notification.url}"> ${notification.articleTitle}</a>
                    </h2>
                    <span class="ft-gray">
                    ${notification.createTime?string('yyyy-MM-dd HH:mm')}
                </span>
                </div>
                <div class="content-reset comment">
                    ${notification.content}
                </div>
            </div>
            <#else>
                <div class="fn-flex-1 has-view">
                    <h2>
                        <@icon notification.articlePerfect notification.articleType></@icon>
                        <a rel="bookmark" href="${notification.url}"> ${notification.articleTitle}</a>
                    </h2>
                    <div class="ft-gray">
                        <#list notification.articleTagObjs as articleTag>
                        <a rel="tag" class="tag" href="${servePath}/tag/${articleTag.tagURI}">
                            ${articleTag.tagTitle}</a>
                        </#list> <br/>
                        <svg><use xlink:href="#date"></use></svg>
                        ${notification.createTime?string('yyyy-MM-dd HH:mm')}
                    </div>
                </div>

                <#if notification.articleCommentCount != 0>
                <div class="cmts" title="${cmtLabel}">
                    <a class="count ft-gray" href="${notification.url}">${notification.articleCommentCount}</a>
                </div>
                </#if>
        </#if>
    </li>
    </#list>
</ul>
<#else>
<div class="fn-hr10"></div>
<div class="ft-center">${noMessageLabel}</div>
</#if>

<@pagination url="${servePath}/notifications/following"/>
</@notifications>