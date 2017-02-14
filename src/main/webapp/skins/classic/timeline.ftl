<#include "macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${timelineLabel} - ${symphonyLabel}">
        <meta name="description" content="${timelineLabel}"/>
        </@head>
        <link rel="stylesheet" href="${staticServePath}/css/index.css?${staticResourceVersion}" />
        <link rel="canonical" href="${servePath}/timeline">
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content fn-clear">
                    <div class="module">
                    <#if timelines?size <= 0>
                    <div id="emptyTimeline" class="no-list">${emptyTimelineLabel}</div>
                    </#if>
                    <div class="list single-line ft-gray timeline<#if timelines?size <= 0> fn-none</#if>">
                        <ul>
                            <#list timelines as timeline>
                            <li>${timeline.content}</li>
                            </#list>
                        </ul>
                    </div>
                    </div>
                    <#include "common/domains.ftl">
                </div>
                <div class="side">
                    <#include "side.ftl">
                </div>
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
