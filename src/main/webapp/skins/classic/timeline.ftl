<#include "macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${timelineLabel} - ${symphonyLabel}">
        <meta name="description" content="${timelineLabel}"/>
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index${miniPostfix}.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "header.ftl">
        <div class="domains fn-clear">
            <div class="wrapper fn-clear">
                <#list domains as domain>
                <a href='/domain/${domain.domainURI}'>${domain.domainIconPath}&nbsp;${domain.domainTitle}</a>
                </#list>
                <a href="/">${latestLabel}</a>
                <a href="/hot">${hotLabel}</a>
                <#if isLoggedIn && "" != currentUser.userCity>
                <a href="/city/my">${currentUser.userCity}</a>
                </#if>
                <a href="/timeline" class="selected">${timelineLabel}</a>
            </div>
        </div>
        <div class="main">
            <div class="wrapper">
                <div class="content fn-clear">
                    <#if timelines?size <= 0>
                    <div id="emptyTimeline">${emptyTimelineLabel}</div>
                    </#if>
                    <div class="list single-line">
                        <ul id="ul">
                            <#list timelines as timeline>
                            <li>${timeline.content}</li>
                            </#list>
                        </ul>
                    </div>
                    <br/>
                    <div class="module">
                        <div class="module-header">
                            <h2>${domainLabel}${navigationLabel}</h2>
                            <a href="/domains" class="ft-gray fn-right">All Domains</a>
                        </div>
                        <div class="module-panel">
                            <ul class="module-list domain">
                                <#list domains as domain>
                                <#if domain.domainTags?size gt 0>
                                <li>
                                    <a rel="nofollow" class="slogan" href="/domain/${domain.domainURI}">${domain.domainTitle}</a>
                                    <div class="title">
                                        <#list domain.domainTags as tag>
                                        <a class="tag" rel="nofollow" href="/tag/${tag.tagTitle?url('utf-8')}">${tag.tagTitle}</a>
                                        </#list>
                                    </div>
                                </li>
                                </#if>
                                </#list>
                            </ul>
                        </div>
                    </div>
                </div>
                <div class="side">
                    <#include "side.ftl">
                </div>
            </div>
        </div>
        <#include "footer.ftl">

        <script type="text/javascript" src="${staticServePath}/js/lib/ws-flash/swfobject.js"></script>
        <script type="text/javascript" src="${staticServePath}/js/lib/ws-flash/web_socket.js"></script>
        <script type="text/javascript" src="${staticServePath}/js/lib/reconnecting-websocket.min.js"></script>
        <script type="text/javascript" src="${staticServePath}/js/channel${miniPostfix}.js?${staticResourceVersion}"></script>
        <script>
            WEB_SOCKET_SWF_LOCATION = "${staticServePath}/js/lib/ws-flash/WebSocketMain.swf";

            // Init [Timeline] channel
            TimelineChannel.init("${wsScheme}://${serverHost}:${serverPort}/timeline-channel");

            var timelineCnt = ${
                timelineCnt
            }
            ;
        </script>
    </body>
</html>
