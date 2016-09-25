<div class="nav"> 
    <h1>
        ${logoIcon}
    </h1>
    <div class="nav-tabs">
        <#list domains as domain>
        <a href="${servePath}/domain/${domain.domainURI}"<#if selected?? && selected == domain.domainURI> class="selected"</#if>>${domain.domainIconPath} ${domain.domainTitle}</a>
        </#list>
        <a href="${servePath}/perfect"<#if selected?? && 'perfect' == selected> class="selected"</#if>>
            <svg height="16" viewBox="3 2 11 12" width="14">${perfectIcon}</svg> ${perfectLabel}</a>
        <a href="${servePath}/recent"<#if selected?? && 'recent' == selected> class="selected"</#if>>
           <svg height="16" viewBox="0 0 14 16" width="14">${timeIcon}</svg> ${latestLabel}</a>
        <a href="${servePath}/hot"<#if selected?? && 'hot' == selected> class="selected"</#if>>
           <svg height="16" viewBox="0 0 12 16" width="12">${hotIcon}</svg> ${hotLabel}</a>
        <#if isLoggedIn && "" != currentUser.userCity>
        <a href="${servePath}/city/my"<#if selected?? && 'city' == selected> class="selected"</#if>>
           <svg height="16" viewBox="0 0 12 16" width="12">${localIcon}</svg> ${currentUser.userCity}</a>
        </#if>
        <a href="${servePath}/timeline"<#if selected?? && 'timeline' == selected> class="selected"</#if>>
           <svg height="14" viewBox="0 0 16 14" width="16">${timelineIcon}</svg> ${timelineLabel}</a>
        <a href="${servePath}/forge/link"<#if selected?? && 'forge' == selected> class="selected"</#if>>
           <svg height="14" viewBox="0 1 16 14" width="16">${baguaIcon}</svg> ${forgeLabel}</a>
    </div>
    <#if !esEnabled || !algoliaEnabled>
    <form class="responsive-hide fn-left" target="_blank" action="/search">
        <input class="search" placeholder="Search HacPai" type="text" name="key" id="search" value="<#if key??>${key}</#if>" >
        <input type="submit" class="fn-none" value=""/>
    </form>
    </#if>
    <div class="user-nav">
        <#if isLoggedIn>
        <a id="aNotifications" class="tooltipped tooltipped-w <#if unreadNotificationCount == 0>no-msg<#else>msg</#if>" href="${servePath}/notifications" aria-label="${messageLabel}">${unreadNotificationCount}</a>
        <a href="${servePath}/activities" aria-label="${activityLabel}" class="tooltipped tooltipped-w"><span class="icon-flag"></span></a>
        <a href="${servePath}/member/${currentUser.userName}" aria-label="Go Home" class="tooltipped tooltipped-w <#if 'adminRole' != userRole>last </#if>nav-avatar">
            <span class="avatar-small" style="background-image:url('${currentUser.userAvatarURL20}')"></span>
        </a>
        <#if "adminRole" == userRole>
        <a href="${servePath}/admin" aria-label="${adminLabel}" class="tooltipped tooltipped-w last"><span class="icon-userrole"></span></a>
        </#if>
        <#else>
        <a href="javascript: Util.showLogin();" class="unlogin">${loginLabel}</a>
        <a id="aRegister" href="javascript:Util.goRegister()" class="last ft-blue unlogin">${registerLabel}</a>
        <div class="form fn-none">
            <table cellspacing="0" cellpadding="0">
                <tr>
                    <td width="40">
                        <label for="nameOrEmail">${accountLabel}</label>
                    </td>
                    <td>
                        <input id="nameOrEmail" type="text" placeholder="${nameOrEmailLabel}" />
                    </td>
                </tr>
                <tr>
                    <td>
                        <label for="loginPassword">${passwordLabel}</label>
                    </td>
                    <td>
                        <input type="password" id="loginPassword" />
                    </td>
                </tr>
                <tr>
                    <td colspan="2" align="right">
                        <div id="loginTip" class="tip"></div><br/>
                        <button class="info" onclick="window.location.href = '${servePath}/forget-pwd'">${forgetPwdLabel}</button>
                        <button class="red" onclick="Util.login()">${loginLabel}</button>
                    </td>
                </tr>
            </table>
        </div>
        </#if>
    </div>
</div>
