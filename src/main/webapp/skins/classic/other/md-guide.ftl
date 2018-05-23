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
<#include "../macro-head.ftl">
    <!DOCTYPE html>
    <html>
    <head>
        <@head title="Markdown ${tutorialLabel} - ${symphonyLabel}" />
        <link rel="stylesheet" href="${staticServePath}/css/index.css?${staticResourceVersion}"/>
        <link rel="canonical" href="${servePath}/guide/markdown">
        <link rel="stylesheet" href="${staticServePath}/js/lib/highlight.js-9.6.0/styles/github.css">
    </head>
    <body>
    <#include "../header.ftl">
        <div class="main">
            <div class="wrapper guide">
                <div class="module">
                    <div class="module-header ft-center"><strong>Markdown ${tutorialLabel}</strong></div>
                    <div class="module-panel fn-flex">
                        <div class="md">
                            <pre>
${md}
                            </pre>
                        </div>
                        <div class="content-reset">
                            ${html}
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <#include "../footer.ftl"><script src="${staticServePath}/js/lib/highlight.js-9.6.0/highlight.pack.js"></script>
            <script>
             $('pre code').each(function (i, block) {
                    hljs.highlightBlock(block);
                });
        </script>
    </body>
    </html>