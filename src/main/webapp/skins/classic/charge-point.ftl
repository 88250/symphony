<#--

    Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
    Copyright (C) 2012-2018, b3log.org & hacpai.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

-->
<#include "macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${chargePointLabel} - ${symphonyLabel}">
        </@head>
        <link rel="stylesheet" href="${staticServePath}/css/index.css?${staticResourceVersion}" />
        <link rel="canonical" href="${servePath}/charge/point">
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content">
                    <div class="module">
                        <h2 class="sub-head"><span class="ft-red">♦</span> ${chargePointLabel}</h2>
                        <div class="content-reset fn-content">
                            <div class="fn-clear">
                                <img class="fn-left" width="45%" src="https://static.b3log.org/images/alipay-donate.jpg">
                                <img class="fn-right" width="45%" src="https://static.b3log.org/images/wechat-donate.jpg">
                            </div>
                            <br>
                            <ul>
                                <li>转账时请在备注里填写你的黑客派用户名</li>
                                <li>如果你忘记填写用户名（扫码转账可能无法输入备注），可以<a href="${servePath}/post?type=1&tags=充值积分&at=88250">点此发帖</a>：
                                    <ul>
                                        <li>在内容中 <code>@88250</code></li>
                                        <li>附上交易号末 <code>4</code> 位</li>
                                    </ul>
                                </li>
                            </ul>
                            <h4>充值积分规则</h4>
                            <ul>
                                <li>充值 &nbsp;&nbsp;￥30 将获得 &nbsp;&nbsp;3,000 积分</li>
                                <li>充值 &nbsp;&nbsp;￥50 将获得 &nbsp;&nbsp;6,000 积分</li>
                                <li>充值 ￥100 将获得 14,000 积分</li>
                                <li>充值 ￥200 将获得 28,000 积分</li>
                                <li><em>请勿充值其他金额</em></li>
                            </ul>
                            <hr>
                            <h3>兑换红包</h3>
                            <h4>积分兑换红包规则</h4>
                            黑客派的积分可以兑换成红包，具体兑换规则请看<a href="https://hacpai.com/article/1455692733962">这里</a>。
                        </div>
                        <div class="top-ranking">
                            <#include "common/ranking.ftl">
                        </div>
                        <br/>
                    </div>
                </div>
                <div class="side">
                    <#include "side.ftl">
                </div>
            </div>
        </div>
        <#include "footer.ftl">
    </body>
</html>