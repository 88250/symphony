<#include "macro-home.ftl">
<#include "../macro-pagination.ftl">
<@home "${type}">
<div class="tabs-sub fn-clear">
    <a href="${servePath}/member/${user.userName}"<#if type == "linkForge"> class="current"</#if>>${linkForgeLabel}</a>
</div>
<#-- TODO -->
<#if 0 == user.userFollowerStatus || (isLoggedIn && ("adminRole" == currentUser.userRole || currentUser.userName == user.userName))>
<div class="form">
    <input type="text"/><button class="green" onclick="postLink()">${submitLabel}</button>
    <div id="uploadLinkTip" class="tip"></div>
</div>
<#else>
<p class="ft-center ft-gray home-invisible">${setinvisibleLabel}</p>
</#if>
</@home>