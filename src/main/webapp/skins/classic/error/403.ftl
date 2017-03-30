<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="403 Forbidden! - ${symphonyLabel}">
        <meta name="robots" content="none" />
        </@head>
        <link rel="stylesheet" href="${staticServePath}/css/error.css?${staticResourceVersion}" />
    </head>
    <body class="error error-403">
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="module">
                    <h2 class="sub-head">${reloginLabel}</h2>
                    <div class="need-login">
                        <button onclick="Util.goLogin()" class="red">${loginLabel}</button> &nbsp;
                        <button onclick="Util.goRegister()" class="green">${nowLabel}${registerLabel}</button>
                        &nbsp; &nbsp; &nbsp; &nbsp;
                        ${indexIntroLabel} &nbsp; &nbsp;
                        <a href="https://github.com/b3log/symphony" target="_blank" class="tooltipped tooltipped-n" aria-label="${siteCodeLabel}">
                            <svg class="ft-gray" height="16" width="16" viewBox="0 0 16 16">${githubIcon}</svg></a> &nbsp;
                        <a href="http://weibo.com/u/2778228501" target="_blank" class="tooltipped tooltipped-n" aria-label="${followWeiboLabel}">
                            <svg class="ft-gray" width="18" height="18" viewBox="0 0 37 30">${weiboIcon}</svg></a>   &nbsp; 
                        <a target="_blank" class="tooltipped tooltipped-n" aria-label="${joinQQGroupLabel}"
                           href="http://shang.qq.com/wpa/qunwpa?idkey=981d9282616274abb1752336e21b8036828f715a1c4d0628adcf208f2fd54f3a">
                            <svg class="ft-gray" width="16" height="16" viewBox="0 0 30 30">${qqIcon}</svg></a>
                    </div>
                </div>
            </div>
            <br>
            <br>
            <div class="wrapper">
                <div class="content">
                    <div class="module">
                        <#if timelines?size <= 0>
                        <div id="emptyTimeline" class="no-list">${emptyTimelineLabel}</div>
                        </#if>
                        <div class="list timeline ft-gray single-line<#if timelines?size <= 0> fn-none</#if>">
                            <ul>
                                <#list timelines as timeline>
                                <li>${timeline.content}</li>
                                </#list>
                            </ul>
                        </div>
                    </div>
                </div>
                <div class="side">
                    <#include "../side.ftl">
                </div>
            </div>
        </div> 
        <#include '../footer.ftl'/>
        <script src="${staticServePath}/js/channel${miniPostfix}.js?${staticResourceVersion}"></script>
        <script>
            // Init [Timeline] channel
            TimelineChannel.init("${wsScheme}://${serverHost}:${serverPort}${contextPath}/timeline-channel", 20);
        </script>
    </body>
</html>
