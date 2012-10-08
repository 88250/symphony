<div class="nav">
    <div class="wrapper fn-clear">
        <div class="fn-left">
            <h1><a href="/">Symphony</a></h1>
        </div>
        <div class="fn-left">
            <form target="_blank" method="get" action="http://www.google.com/search">
                <input class="search" type="text" name="q" />
                <input class="fn-none" type="submit" name="btnG" value="" class="none" />
                <input type="hidden" name="oe" value="UTF-8" />
                <input type="hidden" name="ie" value="UTF-8" />
                <input type="hidden" name="newwindow" value="0" />
                <input type="hidden" name="sitesearch" value="${servePath}" />
            </form>
            <span class="tags">
                <#list trendTags as trendTag>
                <a href="/tags/${trendTag.tagTitle}">${trendTag.tagTitle}</a>
                </#list>
            </span>
        </div>
        <div class="fn-right fn-clear user-nav">
            <#if isLoggedIn>
            <a href="/${userName}">${userName}</a>
            <a href="/add-article">${addArticleLabel}</a>
            <a href="/settings">${settingsLabel}</a>
            <a href="${logoutURL}" title="${logoutLabel}">${logoutLabel}</a>
            <#else>
            <a href="javascript: Util.showLogin();" title="${loginLabel}">${loginLabel}</a>
            <div class="form fn-none">
                <table cellspacing="0" cellpadding="0">
                    <tr>
                        <td>
                            <label for="nameOrEmail">Email/User Name</label>
                        </td>
                        <td>
                            <input id="nameOrEmail" type="text"/>
                            <span style="left:286px;top:20px;"></span>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <label for="loginPassword">Password</label>
                        </td>
                        <td>
                            <input type="password" id="loginPassword" />
                            <span style="left:286px;top:68px;"></span>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <span id="loginTip"></span>
                        </td>
                        <td>
                            <button onclick="Util.login()">登录</button>
                            <a href="javascript:Util.goRegister()">注册</a>
                            <a href="/">忘记密码</a>
                        </td>
                    </tr>
                </table>
            </div>
            </#if>
        </div>
    </div>
</div>