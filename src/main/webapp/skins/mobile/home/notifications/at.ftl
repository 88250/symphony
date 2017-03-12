<#include "macro-notifications.ftl">
<@notifications "at">
<#if atNotifications?size != 0>
<ul class="notification">
    <#list atNotifications as notification>
    <li class="comment-list-item fn-flex<#if notification.hasRead> read</#if>">
        <#if 2 == notification.dataType>
            <#if "someone" != notification.authorName>
            <a rel="nofollow" href="${servePath}/member/${notification.authorName}"
               title="${notification.authorName}"></#if>
                <div class="avatar" style="background-image:url('${notification.thumbnailURL}')"></div>
            <#if "someone" != notification.authorName></a></#if>
            <#if !notification.atInArticle>
            <div class="fn-flex-1">
                <div>
                    <h2>
                        <#if 1 == notification.articlePerfect>
                        <svg height="20" viewBox="3 4 11 12" width="14">${perfectIcon}</svg>
                        </#if>
                        <#if notification.articleType == 1>
                        <span class="icon-locked" title="${discussionLabel}"></span>
                        <#elseif notification.articleType == 2>
                        <span class="icon-feed" title="${cityBroadcastLabel}"></span>
                        </#if>
                        <a rel="bookmark" href="${notification.url}"> ${notification.articleTitle}</a>
                    </h2>
                    <span class="ft-gray fn-sub">
                        <span class="icon-date"></span>
                        ${notification.createTime?string('yyyy-MM-dd HH:mm')}
                    </span>
                </div>
                <div class="content-reset comment">
                    ${notification.content}
                </div>
            </div>
            <#else>
            <div class="fn-flex-1">
                <h2>
                    <#if 1 == notification.articlePerfect>
                    <svg height="20" viewBox="3 4 11 12" width="14">${perfectIcon}</svg>
                    </#if>
                    <#if notification.articleType == 1>
                    <span class="icon-locked" title="${discussionLabel}"></span>
                    <#elseif notification.articleType == 2>
                    <span class="icon-feed" title="${cityBroadcastLabel}"></span>
                    </#if>
                    <a rel="bookmark" href="${notification.url}"> ${notification.articleTitle}</a>
                </h2>

                <p class="ft-gray">
                    <#list notification.articleTagObjs as articleTag>
                    <a rel="tag" class="tag" href="${servePath}/tag/${articleTag.tagURI}">
                        ${articleTag.tagTitle}</a>
                    </#list>
                    <br/>
                    <span class="icon-date"></span>
                    ${notification.createTime?string('yyyy-MM-dd HH:mm')}
                </p>
                <#if notification.articleCommentCount != 0>
                <div class="cmts" title="${cmtLabel}">
                    <a class="count ft-gray" href="${notification.url}">${notification.articleCommentCount}</a>
                </div>
                </#if>
            </div>
            </#if>
        <#else>
            <a target="_blank" rel="nofollow" href="${servePath}/member/${notification.userName}">
            <div class="avatar tooltipped tooltipped-se" aria-label="${notification.userName}" style="background-image:url('${notification.thumbnailURL}')"></div>
            </a>
            <div class="fn-flex-1">
                <div>
                    <h2>${notification.description}</h2>
                    <span class="ft-gray fn-sub">
                        <span class="icon-date"></span>
                        ${notification.createTime?string('yyyy-MM-dd HH:mm')}
                    </span>
                </div>
            </div>
        </#if>
    </li>
    </#list>
</ul>
<#else>
<div class="fn-hr10"></div>
<div class="ft-center">${noMessageLabel}</div>
</#if>

<@pagination url="${servePath}/notifications/at"/>
</@notifications>