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
<#include "macro-list.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${symphonyLabel}">
        <meta name="description" content="${symDescriptionLabel}"/>
        </@head>
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class='domains-count'>
                    Domains: <b>${domainCnt}</b><br/>
                    Tags: <b>${tagCnt}</b>
                </div>
                <div class="content fn-clear">
                    <#list allDomains as domain>
                    <div class="module">
                        <div class="module-header">
                            <h2>${domain.domainTitle}</h2>
                            <a class="ft-gray fn-right" rel="nofollow" href="${servePath}/domain/${domain.domainURI}">${domain.domainTags?size} Tags</a>
                        </div>
                        <div class="module-panel">
                            <ul class="tags fn-clear">
                                <#list domain.domainTags as tag>
                                <li>
                                    <a class="tag" rel="nofollow" href="${servePath}/tag/${tag.tagURI}">${tag.tagTitle}</a>
                                </li>
                                </#list>
                            </ul>
                        </div>
                    </div>
                    </#list>
                </div>
                <div class="side">
                    <#if ADLabel!="">
                    <div class="module">
                        <div class="module-header">
                            <h2>
                                ${sponsorLabel} 
                                <a href="https://hacpai.com/article/1460083956075" class="fn-right ft-13 ft-gray" target="_blank">${wantPutOnLabel}</a>
                            </h2>
                        </div>
                        <div class="module-panel ad fn-clear">
                            ${ADLabel}
                        </div>
                    </div>
                    </#if>
                    <div class="module">
                        <div class="module-header">
                            <h2>开源项目</h2>
                        </div>
                        <div class="module-panel">
                            <ul class="module-list open-source">
                                <li>
                                    <a target="_blank" rel="noopener" href="https://github.com/b3log/solo"><b class="ft-red slogan">【Solo】</b></a>
                                    <a class="title" target="_blank" rel="noopener" href="https://github.com/b3log/solo">GitHub 上 Star 数最多的 Java 博客</a>
                                </li>
                                <li class="last">
                                    <a target="_blank" rel="noopener" href="https://github.com/b3log/symphony"> <b class="ft-green slogan">【Sym】</b></a>
                                    <a class="title" target="_blank" rel="noopener" href="https://github.com/b3log/symphony">现代化的社区论坛系统</a>
                                </li>
                                <li class="last">
                                    <a target="_blank" rel="noopener" href="https://github.com/b3log/pipe"> <b class="ft-gray slogan">【Pipe】</b></a>
                                    <a class="title" target="_blank" rel="noopener" href="https://github.com/b3log/pipe">小而美的博客平台</a>
                                </li>
                                <li>
                                    <a target="_blank" rel="noopener" href="https://github.com/b3log/wide"><b class="ft-blue slogan">【Wide】</b></a>
                                    <a class="title" target="_blank" rel="noopener" href="https://github.com/b3log/wide">Golang 黑科技之在线 IDE </a>
                                </li>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <#include "footer.ftl">
        <@listScript/>
    </body>
</html>
