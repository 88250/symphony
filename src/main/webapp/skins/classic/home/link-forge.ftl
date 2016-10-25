<#include "macro-home.ftl">
<#include "../macro-pagination.ftl">
<@home "${type}">
<#if 0 == user.userForgeLinkStatus || (isLoggedIn && ("adminRole" == currentUser.userRole || currentUser.userName == user.userName))>
<div class="link-forge">
    <div class="link-forge-upload form">
        <input type="text" placeholder="${linkForgeTipLabel}" /><button class="green">${submitLabel}</button>
        <div id="uploadLinkTip" class="tip"></div>
    </div>
    <#list tags as tag>
    <div class="module">
        <div class="module-header">
            <h2>
                <a href="${servePath}/tag/${tag.tagURI}">
                    <#if tag.tagIconPath != ''>
                    <span class="avatar-small"  style="background-image:url('${staticServePath}/images/tags/${tag.tagIconPath}')"></span>
                    </#if>
                    ${tag.tagTitle}
                </a>
            </h2>
            <a class="ft-gray fn-right" rel="nofollow" href="javascropt:void(0)">${tag.tagLinksCnt} Links</a>
        </div>
        <div class="module-panel">
            <ul class="module-list">
                <#list tag.tagLinks as link>
                <li>
                <a class="title fn-ellipsis" target="_blank" rel="nofollow" href="${link.linkAddr}">${link.linkTitle}</a>
                </li>
                </#list>
            </ul>
        </div>
    </div>
    </#list>
</div>
<#else>
<div class="module">
<p class="ft-center ft-gray home-invisible">${setinvisibleLabel}</p>
</div>
</#if>
</@home>