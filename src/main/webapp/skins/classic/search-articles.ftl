<#include "macro-head.ftl">
<#include "macro-list.ftl">
<#include "macro-pagination-query.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${searchLabel} - ${articleLabel} - ${symphonyLabel}">
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index${miniPostfix}.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "header.ftl">
        <div class="tabs fn-clear">
            <div class="wrapper fn-clear">
                <#list domains as domain>
                <a href="${servePath}/domain/${domain.domainURI}">${domain.domainIconPath}&nbsp;${domain.domainTitle}</a>
                </#list>
                <a href="${servePath}/recent">
                    <svg height="16" viewBox="0 0 14 16" width="14">${timeIcon}</svg>&nbsp;${latestLabel}</a>
                <a href="${servePath}/hot">
                    <svg height="16" viewBox="0 0 12 16" width="12">${hotIcon}</svg>&nbsp;${hotLabel}</a>
                <#if isLoggedIn && "" != currentUser.userCity>
                <a href="${servePath}/city/my">
                    <svg height="16" viewBox="0 0 12 16" width="12">${localIcon}</svg>&nbsp;${currentUser.userCity}</a>
                </#if>
                <a href="${servePath}/timeline">
                    <svg height="14" viewBox="0 0 16 14" width="16">${timelineIcon}</svg>&nbsp;${timelineLabel}</a>
                <a href="${servePath}/community">
                    <svg height="16" viewBox="0 0 14 16" width="16">${noticeIcon}</svg>&nbsp;${communityGroupLabel}</a>
            </div>
        </div>
        <div class="main">
            <div class="wrapper">
                <div class="content fn-clear">
                    <@list listData=articles/>
                    <@pagination url="/search" query="key=${key}" />
                </div>
                <div class="side">
                    <#include "side.ftl">
                </div>
            </div>
        </div>
        <#include "footer.ftl">
        <@listScript/>
    </body>
</html>
