<#include "macro-head.ftl">
<#include "common/sub-nav.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${timelineLabel} - ${symphonyLabel}">
        <meta name="description" content="${timelineLabel}"/>
        </@head>
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <@subNav 'timeline' ''/>
            <div class="content fn-clear">
                <#if timelines?size <= 0>
                <div id="emptyTimeline" class="wrapper">${emptyTimelineLabel}</div>
                </#if>
                <div class="list">
                    <ul id="ul">
                        <#list timelines as timeline>
                        <li>${timeline.content}</li>
                        </#list>
                    </ul>
                </div>
            </div>
            <div class="fn-hr10"></div>
            <div class="side wrapper">
                <#include "side.ftl">
            </div>
        </div>
        <#include "footer.ftl">
        <script src="${staticServePath}/js/channel${miniPostfix}.js?${staticResourceVersion}"></script>
        <script>
            // Init [Timeline] channel
            TimelineChannel.init("${wsScheme}://${serverHost}:${serverPort}${contextPath}/timeline-channel", ${timelineCnt});
        </script>
    </body>
</html>
