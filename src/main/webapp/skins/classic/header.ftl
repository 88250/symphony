<div class="nav">
    <h1>
        ${logoIcon}
    </h1>
    <div class="nav-tabs">
        <#list domains as domain>
        <a pjax-title="${domain.domainTitle} - ${domainLabel} - ${symphonyLabel}" href="${servePath}/domain/${domain.domainURI}"<#if selected?? && selected == domain.domainURI> class="current"</#if>>${domain.domainIconPath} ${domain.domainTitle}</a>
        </#list>
        <a pjax-title="${latestLabel} - ${symphonyLabel}" href="${servePath}/recent"<#if selected?? && 'recent' == selected> class="current"</#if>>
           <svg height="16" viewBox="0 0 14 16" width="14">${timeIcon}</svg> ${latestLabel}</a>
        <a href="${servePath}/perfect"<#if selected?? && 'perfect' == selected> class="current"</#if>>
           <svg height="16" viewBox="3 2 11 12" width="14">${perfectIcon}</svg> ${perfectLabel}</a>
        <#if isLoggedIn && "" != currentUser.userCity>
        <a href="${servePath}/city/my"<#if selected?? && 'city' == selected> class="current"</#if>>
           <svg height="16" viewBox="0 0 12 16" width="12">${localIcon}</svg> ${currentUser.userCity}</a>
        </#if>
        <a href="${servePath}/timeline"<#if selected?? && 'timeline' == selected> class="current"</#if>>
           <svg height="14" viewBox="0 0 16 14" width="16">${timelineIcon}</svg> ${timelineLabel}</a>
        <a href="https://hacpai.com/tag/book_share"<#if selected?? && 'book' == selected> class="current"</#if>>
           <svg height="16" viewBox="0 -1 17 14" width="16">${bookIcon}</svg> ${bookShareLabel}</a>
    </div>
    <#if esEnabled || algoliaEnabled>
    <form class="responsive-hide fn-left" target="_blank" action="/search">
        <input class="search" placeholder="Search HacPai" type="text" name="key" id="search" value="<#if key??>${key}</#if>" >
        <input type="submit" class="fn-none" value=""/>
    </form>
    </#if>
    <div class="user-nav">
        <#if isLoggedIn>
            <a href="${servePath}/pre-post" class="tooltipped tooltipped-w" aria-label="${postArticleLabel}"><span class="icon-addfile"></span></a>
            <#if permissions["menuAdmin"].permissionGrant>
            <a href="${servePath}/admin" aria-label="${adminLabel}" class="tooltipped tooltipped-w"><span class="icon-userrole"></span></a>
            </#if>
            <a id="aNotifications" class="tooltipped tooltipped-w <#if unreadNotificationCount == 0>no-msg<#else>msg</#if>" href="${servePath}/notifications" aria-label="${messageLabel}">${unreadNotificationCount}</a>
            <a href="${servePath}/activities" aria-label="${activityLabel}" class="tooltipped tooltipped-w"><span class="icon-flag"></span></a>
            <a href="javascript:void(0)" id="aPersonListPanel" class="tooltipped tooltipped-w" aria-label="${viewHomeAndProfileLabel}"
               data-url="${servePath}/member/${currentUser.userName}">
                <span class="avatar-small" style="background-image:url('${currentUser.userAvatarURL20}')"></span>
            </a>
            <div class="module person-list" id="personListPanel">
                <ul>
                    <li>
                        <a href="${servePath}/member/${currentUser.userName}">${goHomeLabel}</a>
                    </li>
                    <li>
                        <a href="${servePath}/settings">${settingsLabel}</a>
                    </li>
                    <li>
                        <a href="${servePath}/settings/help">${helpLabel}</a>
                    </li>
                    <li>
                        <a href="javascript:Util.logout()">${logoutLabel}</a>
                    </li>
                </ul>
            </div>
        <#else>
            <a href="javascript: Util.goLogin();" class="unlogin">${loginLabel}</a>
            <a href="javascript:Util.goRegister()" class="unlogin">${registerLabel}</a>
        </#if>
    </div>
</div>
