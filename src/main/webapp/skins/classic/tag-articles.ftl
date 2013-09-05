<#include "macro-head.ftl">
<#include "macro-list.ftl">
<#include "macro-pagination.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="B3log ${symphonyLabel} - ${tag.tagTitle}">
        <meta name="keywords" content="${tag.tagTitle},${tag.tagCreatorName}"/>
        <meta name="description" content="${tag.tagCreatorName},${tag.tagDescription}"/>
        </@head>
        <link type="text/css" rel="stylesheet" href="/css/index.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper fn-clear">
                <div class="content">
                    <div class="fn-clear">
                        <#if tag.tagIconPath!="">
                        <img class="avatar tag-article-img" src="${staticServePath}/images/tags/${tag.tagIconPath}">
                        </#if>
                        <h1><a rel="tag" title="${tag.tagTitle}" href="/tags/${tag.tagTitle?url('utf-8')}">${tag.tagTitle}</a></h1>
                        ${tag.tagDescription}
                    </div>
                    <div class="fn-clear">
                        <div class="fn-left">
                            <ul class="status fn-clear">
                                <li>
                                    <strong>${tag.tagReferenceCount}</strong>
                                    <span class="ft-small">${referenceCountLabel}</span>
                                </li>
                                <li>
                                    <strong>${tag.tagCommentCount}</strong>
                                    <span class="ft-small">${cmtCountLabel}</span>
                                </li>
                            </ul>
                        </div>
                        <div style="margin-top:35px">
                            <a rel="nofollow" style="width: 48px;" class="ft-noline fn-left" title="${creatorLabel}:${tag.tagCreatorName}" href="/member/${tag.tagCreatorName}">
                                <img style="margin-left:20px" class="avatar fn-left" src="${tag.tagCreatorThumbnailURL}">
                            </a>
                            <div class="fn-right">
                                <#list tag.tagParticipants as commenter>
                                <a rel="nofollow" style="width: 58px;" class="ft-noline fn-left" title="${contributorLabel}:${commenter.tagParticipantName}" href="/member/${commenter.tagParticipantName}">
                                    <img class="avatar fn-left" src="${commenter.tagParticipantThumbnailURL}">
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
