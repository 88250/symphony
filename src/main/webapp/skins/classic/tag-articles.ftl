<#include "macro-head.ftl">
<#include "macro-list.ftl">
<#include "macro-pagination.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="B3log 社区">
        <meta name="keywords" content="${tag.tagTitle}"/>
        <meta name="description" content=""/>
        </@head>
        <link type="text/css" rel="stylesheet" href="/css/index.css" />
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper fn-clear">
                <div class="content">
                    <div class="fn-clear">
                        <#if tag.tagIconPath!="">
                        <img style="width:60px;height:60px;" class="avatar fn-left" src="${tag.tagIconPath}">
                        </#if>
                        <h1><a title="${tag.tagTitle}" href="/tags/${tag.tagTitle?url('utf-8')}">${tag.tagTitle}</a></h1>
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
                            <a class="ft-noline fn-left" title="${creatorLabel}:${tag.tagCreatorName}" href="/${tag.tagCreatorName}">
                                <img style="margin-left:20px" class="avatar fn-left" src="${tag.tagCreatorThumbnailURL}">
                            </a>
                            <div class="fn-right">
                                <#list tag.tagParticipants as commenter>
                                <a class="ft-noline" title="${contributorLabel}:${commenter.tagParticipantName}" href="/${commenter.tagParticipantName}">
                                    <img class="avatar fn-left" src="${commenter.tagParticipantThumbnailURL}">
                                </a>
                                </#list>
                            </div>
                        </div>
                    </div>
                    <@list listData=articles/>
                    <@pagination url="/tags/${tag.tagTitle?url('utf-8')}"/>
                </div> 
                <div class="side">
                    <#include "side.ftl">
                </div>
            </div>
        </div>
        <#include "footer.ftl">
    </body>
</html>
