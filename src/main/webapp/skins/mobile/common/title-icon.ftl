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
<#macro icon perfect type>
<#if 1 == perfect>
    <span class="tooltipped tooltipped-e" aria-label="${perfectLabel}"><svg><use xlink:href="#perfect"></use></svg></span>
</#if>
<#if 1 == type>
    <span class="tooltipped tooltipped-e" aria-label="${discussionLabel}"><svg><use xlink:href="#locked"></use></svg></span>
<#elseif 2 == type>
    <span class="tooltipped tooltipped-e" aria-label="${cityBroadcastLabel}"><svg><use xlink:href="#feed"></use></svg></span>
<#elseif 3 == type>
    <span class="tooltipped tooltipped-e" aria-label="${thoughtLabel}"><svg><use xlink:href="#video"></use></svg></span>
</#if>
</#macro>