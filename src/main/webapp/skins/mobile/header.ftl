<div class="nav"> 
    <div class="wrapper fn-clear">
        <div class="head-fn fn-left">
            <h1>
                ${logoIcon}
            </h1>
        </div>

        <div class="fn-right user-nav">
            <#if isLoggedIn>
            <#if permissions["menuAdmin"].permissionGrant>
            <a href="${servePath}/admin" title="${adminLabel}" class="last icon-userrole"></a>
            </#if>
            <a href="${servePath}/member/${currentUser.userName}" title="Home" class="<#if 'adminRole' != userRole>last </#if>nav-avatar">
                <span class="avatar-small" style="background-image:url('${currentUser.userAvatarURL20}')"></span>
            </a>
            <a href="${servePath}/activities" title="${activityLabel}" class="icon-flag"></a>
            <#if permissions["commonAddArticle"].permissionGrant>
                <a href="${servePath}/pre-post" title="${addArticleLabel}"
               class="icon-addfile"></a>
            </#if>
            <a id="aNotifications" class="<#if unreadNotificationCount == 0>no-msg<#else>msg</#if>" href="${servePath}/notifications" title="${messageLabel}">${unreadNotificationCount}</a>
            <a href="${servePath}/recent" class="icon-refresh"></a>
            <#else>
            <a id="aRegister" href="javascript:Util.goRegister()" class="last ft-blue unlogin" 
               title="${registerLabel}">${registerLabel}</a>
            <a href="javascript: Util.goLogin();" title="${loginLabel}" class="unlogin">${loginLabel}</a>
            </#if>
        </div>
    </div>
</div>
