<div class="nav">
    <div class="wrapper fn-clear">
        <div class="fn-left">
            <h1><a href="">Symphony</a></h1>
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
                <#list 1..10 as i>
                <a href="">tag${i_index}</a>
                </#list>
            </span>
        </div>
        <div class="fn-right fn-clear user-nav">
            <a href="/">首页</a>
            <a href="/hot">热门</a>
            <a href="/tags">Tags</a>
            <#if isLoggedIn>
            <a href="/home/${userName}">${userName}</a>
            <a href="${logoutURL}" title="${logoutLabel}">${logoutLabel}</a>
            <#else>
            <a href="javascript: Util.showLogin();" title="${loginLabel}">${loginLabel}</a>
            <div class="form fn-none">
                <table>
                    <tr>
                        <td>
                            <label for="nameOrEmail">Email/User Name</label>
                        </td>
                        <td>
                            <input id="nameOrEmail"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <label for="loginPassword">Password</label>
                        </td>
                        <td>
                            <input type="password" id="loginPassword" />
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