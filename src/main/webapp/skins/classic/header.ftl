<div class="nav">
    <div class="wrapper fn-clear">
        <h1 style="margin-top:4px"><a rel="nofollow" href="/">Symphony</a></h1>
        <form target="_blank" method="get" action="http://www.google.com/search">
            <input class="search" type="text" name="q" />
            <input class="fn-none" type="submit" name="btnG" value="" class="none" />
            <input type="hidden" name="oe" value="UTF-8" />
            <input type="hidden" name="ie" value="UTF-8" />
            <input type="hidden" name="newwindow" value="0" />
            <input type="hidden" name="sitesearch" value="symphony.b3log.org" />
        </form>
        <span class="tags">
            <#list trendTags as trendTag>
            <a rel="tag" href="/tags/${trendTag.tagTitle}">${trendTag.tagTitle}</a>
            </#list>
        </span>
        <div class="fn-right fn-clear user-nav">
            <#if isLoggedIn>
            <a href="/member/${userName}">${userName}</a>
            <a href="/add-article">${addArticleLabel}</a>
            <a href="/settings">${settingsLabel}</a>
            <a href="${logoutURL}" title="${logoutLabel}">${logoutLabel}</a>
            <#else>
            <a href="javascript: Util.showLogin();" title="${loginLabel}">${loginLabel}</a>
            <a href="javascript:Util.goRegister()">${registerLabel}</a>
            <div class="form fn-none">
                <table cellspacing="0" cellpadding="0">
                    <tr>
                        <td width="40">
                            <label for="nameOrEmail">${accountLabel}</label>
                        </td>
                        <td>
                            <input id="nameOrEmail" type="text"/>
                            <span style="left:220px;left:208px\9;top:20px;"></span>
                        </td>
                        <td>&nbsp;</td>
                    </tr>
                    <tr>
                        <td>
                            <label for="loginPassword">${passwordLabel}</label>
                        </td>
                        <td>
                            <input type="password" id="loginPassword" />
                            <span style="left:220px;left:208px\9;top:68px;"></span>
                        </td>
                        <td>&nbsp;</td>
                    </tr>
                    <tr>
                        <td colspan="3" align="right">
                            <span id="loginTip"></span>
                            <button onclick="Util.login()">${loginLabel}</button>
                        </td>
                    </tr>
                </table>
            </div>
            </#if>
        </div>
    </div>
</div>