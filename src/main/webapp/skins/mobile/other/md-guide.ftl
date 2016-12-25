<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="Markdown ${newbieGuideLabel} - ${symphonyLabel}" />
        <link rel="canonical" href="${servePath}/guide/markdown">
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="fn-hr10"></div>
                <h2>Markdown ${newbieGuideLabel}</h2>
                <div class="fn-hr10"></div>
            </div>
            <div class="guide">
                    <div class="fn-flex">
                        <div class="md">
                        <pre>
# Guide

这是一篇讲解如何正确使用 **Markdown** 的排版示例，学会这个很有必要，能让你的文章有更佳清晰的排版。

> 引用文本：Markdown is a text formatting syntax inspired

## 语法指导

### 普通内容

这段内容展示了在内容里面一些小的格式，比如：

- **加粗** - `**加粗**`
- *倾斜* - `*倾斜*`
- ~~删除线~~ - `~~删除线~~`
                        </pre>
                        </div>
                        <div class="content-reset">
                            # Guide

                            这是一篇讲解如何正确使用 **Markdown** 的排版示例，学会这个很有必要，能让你的文章有更佳清晰的排版。

                            > 引用文本：Markdown is a text formatting syntax inspired

                            ## 语法指导

                            ### 普通内容

                            这段内容展示了在内容里面一些小的格式，比如：

                            - **加粗** - `**加粗**`
                            - *倾斜* - `*倾斜*`
                            - ~~删除线~~ - `~~删除线~~`
                        </div>
                    </div>
        </div>
        <#include "../footer.ftl">
    </body>
</html>