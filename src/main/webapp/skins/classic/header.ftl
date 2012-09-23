<div class="nav">
    <div class="wrapper fn-clear">
        <div class="fn-left">
            B3log Symphony
        </div>
        <div class="fn-right">
            <a href="/">首页</a>
            <a href="/hot">热门</a>
            <a href="/tags">Tags</a>
            <a href="javascript: Util.showLogin();">登录</a>
        </div>
        <div class="fn-right">
            <input/>
            <#list 1..10 as i>
            <a href="">tag${i_index}</a>
            </#list>
        </div>
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
    </div>
</div>