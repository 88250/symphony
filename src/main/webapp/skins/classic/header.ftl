<div class="nav">
    <h1 aria-label="${symphonyLabel}" class="tooltipped tooltipped-s">
        <a href="${servePath}">
            <svg><use xlink:href="#logo"></use></svg>
        </a>
    </h1>
    <div class="nav-tabs">
        <#list domains as domain>
        <a pjax-title="${domain.domainTitle} - ${domainLabel} - ${symphonyLabel}" href="${servePath}/domain/${domain.domainURI}"<#if selected?? && selected == domain.domainURI> class="current"</#if>>${domain.domainIconPath} ${domain.domainTitle}</a>
        </#list>
        <a pjax-title="${latestLabel} - ${symphonyLabel}" href="${servePath}/recent"<#if selected?? && 'recent' == selected> class="current"</#if>>
           <svg><use xlink:href="#refresh"></use></svg> ${latestLabel}</a>
        <a href="${servePath}/perfect"<#if selected?? && 'perfect' == selected> class="current"</#if>>
            <svg><use xlink:href="#perfect"></use></svg> ${perfectLabel}</a>
        <#if isLoggedIn && "" != currentUser.userCity>
        <a href="${servePath}/city/my"<#if selected?? && 'city' == selected> class="current"</#if>>
           <svg><use xlink:href="#local"></use></svg> ${currentUser.userCity}</a>
        </#if>
        <a href="${servePath}/timeline"<#if selected?? && 'timeline' == selected> class="current"</#if>>
           <svg><use xlink:href="#view"></use></svg> ${timelineLabel}</a>
        <a href="https://hacpai.com/tag/book_share"<#if selected?? && 'book' == selected> class="current"</#if>>
           <svg><use xlink:href="#book"></use></svg> ${bookShareLabel}</a>
    </div>
    <#if esEnabled || algoliaEnabled>
    <form class="responsive-hide fn-left" target="_blank" action="/search">
        <input class="search" placeholder="Search HacPai" type="text" name="key" id="search" value="<#if key??>${key}</#if>" >
        <input type="submit" class="fn-none" value=""/>
    </form>
    </#if>
    <div class="user-nav">
        <#if isLoggedIn>
            <a href="${servePath}/pre-post" class="tooltipped tooltipped-w" aria-label="${postArticleLabel}"><svg><use xlink:href="#addfile"></use></svg></a>
            <#if permissions["menuAdmin"].permissionGrant>
            <a href="${servePath}/admin" aria-label="${adminLabel}" class="tooltipped tooltipped-w"><svg><use xlink:href="#userrole"></use></svg></a>
            </#if>
            <a id="aNotifications" class="tooltipped tooltipped-w <#if unreadNotificationCount == 0>no-msg<#else>msg</#if>" href="${servePath}/notifications" aria-label="${messageLabel}">${unreadNotificationCount}</a>
            <a href="${servePath}/activities" aria-label="${activityLabel}" class="tooltipped tooltipped-w"><svg><use xlink:href="#flag"></use></svg></a>
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
