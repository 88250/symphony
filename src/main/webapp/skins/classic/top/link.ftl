<#--

    Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
    Copyright (C) 2012-2019, b3log.org & hacpai.com

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
<#include "macro-top.ftl">
<@top "link">
<h2 class="sub-head">
    <svg>
        <use xlink:href="#link"></use>
    </svg> ${linkRankLabel}</h2>
<div class="list">
    <ul>
        <#list topLinks as link>
           <li class="fn-flex-1 fn-clear">
               <h2 class="fn-left">
                   <a rel="bookmark" href="${servePath}/forward?goto=${link.linkAddr}">
                       ${link_index + 1}. ${link.linkTitle}</a>
               </h2>
               <div class="ft-gray fn-right">
                   ${link.linkClickCnt?c} ${clickLabel}
               </div>
           </li>
        </#list>
        <#if topLinks?size == 0>
            <li class="ft-center ft-gray home-invisible">${chickenEggLabel}</li>
        </#if>
    </ul>
    <br/>
</div>
</@top>