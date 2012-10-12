<#include "macro-head.ftl">
<#include "macro-list.ftl">
<#include "macro-pagination.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="B3log 社区">
        <meta name="keywords" content=""/>
        <meta name="description" content=""/>
        </@head>
        <link type="text/css" rel="stylesheet" href="/css/index.css" />
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper fn-clear">
                <div class="content">
                    <div class="fn-clear" style="margin-bottom:20px">
                        <div class="fn-left">
                            <h1><a href="/tags/${tag.tagTitle?url('utf-8')}">${tag.tagTitle}</a></h1>
                            <span class="ft-small">
                                ${referenceCountLabel} ${tag.tagReferenceCount} &nbsp;
                                ${commentCountLabel} ${tag.tagCommentCount}
                            </span>
                        </div>
                        <div class="fn-left">
                            <a class="ft-noline" title="${creatorLabel}:${tag.tagCreatorName}" href="/${tag.tagCreatorName}">
                                <img style="margin-left:20px" class="avatar fn-left" src="${tag.tagCreatorThumbnailURL}">
                            </a>
                        </div>
                        <div class="fn-right">
                            <#list tag.tagParticipants as commenter>
                            <a class="ft-noline" title="${contributorLabel}:${commenter.tagParticipantName}" href="/${commenter.tagParticipantName}">
                                <img class="avatar fn-left" src="${commenter.tagParticipantThumbnailURL}">
                            </a>
                            </#list>
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
