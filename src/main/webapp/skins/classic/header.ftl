<div class="nav">
    <div class="wrapper fn-clear">
        <span class="fn-left">
            <a href="/">首页</a>
            <a href="/hot">热门</a>
            <a href="/tags">Tags</a>
        </span>
        <span class="fn-right">
            <a href="javascript: Util.showLogin();">登录</a>
        </span>
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
<div class="header">
    <div class="wrapper">
        <img src="/images/logo.png" />
    </div>
</div>