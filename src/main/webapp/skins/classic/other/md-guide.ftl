<#include "../macro-head.ftl">
    <!DOCTYPE html>
    <html>
    <head>
        <@head title="Markdown ${newbieGuideLabel} - ${symphonyLabel}" />
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index.css?${staticResourceVersion}"/>
        <link rel="canonical" href="${servePath}/guide/markdown">
    </head>
    <body>
    <#include "../header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="module guide">
                    <div class="module-header">Markdown ${newbieGuideLabel}</div>
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
        <#include "../footer.ftl">
    </body>
    </html>