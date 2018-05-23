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
<#include "macro-settings.ftl">
<@home "point">

<div class="module">
    <div class="module-header">
        <h2>${pointTransferTipLabel}</h2>
        <span class="fn-right">
            <a class="ft-green" href="${servePath}/charge/point">${rechargePointLabel}</a>
        </span>
    </div>
    <div class="module-panel form fn-clear">
        <input id="pointTransferUserName" type="text" placeholder="${userNameLabel}"/>
        <br/> <br/>
        <input id="pointTransferAmount" type="number" placeholder="${amountLabel}"/> <br/><br/>
        <div id="pointTransferTip" class="tip"></div> <br/>
        <button class="red fn-right" onclick="Settings.pointTransfer('${csrfToken}')">${confirmTransferLabel}</button>
    </div>
</div>
</@home>