/*
 * Copyright (c) 2012, B3log Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * @fileoverview add-article.
 *
 * @author <a href="mailto:LLY219@gmail.com">Liyuan Li</a>
 * @version 1.0.0.6, Nov 24, 2012
 */

/**
 * @description Add article function.
 * @static
 */
var AddArticle = {

    /**
     * @description 发布文章
     */
    add: function () {
        var isError = false;
        if (this.editor.getValue().length < 4 || this.editor.getValue().length > 1048576) {
            $("#articleContentTip").addClass("tip-error").text(Label.articleContentErrorLabel);
        } else {
            isError = true;
            $("#articleContentTip").removeClass("tip-error").text("");
        }
        
        if (Validate.goValidate([{
            "id": "articleTitle",
            "type": 256,
            "msg": Label.articleTitleErrorLabel
        }, {
            "id": "articleTags",
            "type": "tags",
            "msg": Label.articleTagsErrorLabel
        }]) && isError) {
            var requestJSONObject = {
                articleTitle: $("#articleTitle").val().replace(/(^\s*)|(\s*$)/g,""),
                articleContent: this.editor.getValue(),
                articleTags: $("#articleTags").val().replace(/(^\s*)|(\s*$)/g,""),
                syncWithSymphonyClient: $("#syncWithSymphonyClient").prop("checked")
            };
            
            $.ajax({
                url: "/article",
                type: "PUT",
                cache: false,
                data: JSON.stringify(requestJSONObject),
                beforeSend: function () {
                    $(".form button.red").attr("disabled", "disabled").css("opacity", "0.3");
                },
                success: function(result, textStatus){
                    $(".form button.red").removeAttr("disabled").css("opacity", "1");
                    if (result.sc) {
                        window.location = "/member/" + Label.userName;
                    } else {
                        $("#addArticleTip").addClass("tip-error").text(result.msg).css({
                            "border-left": "1px solid #E2A0A0",
                            "top": "-35px",
                            "width": "985px"
                        });
                    }
                },
                complete: function () {
                    $(".form button.red").removeAttr("disabled").css("opacity", "1"); 
                }
            });
        }
    },
    
    /**
     * @description 初识化发文页面，回车提交表单
     */
    init: function () {
        var it = this;
        it.editor = CodeMirror.fromTextArea(document.getElementById("articleContent"), {
            mode: 'markdown',
            lineNumbers: true,
            onKeyEvent: function (editor, event) {
                if (it.editor.getValue().replace(/(^\s*)|(\s*$)/g, "") !== "") {
                    $(".form .green").show();
                } else {
                    $(".form .green").hide();
                }
                
                if (event.keyCode === 13 && event.ctrlKey) {
                    AddArticle.add();
                }
            }
        });
            
        $("#articleTitle, #articleTags").keyup(function (event) {
            if (event.keyCode === 13) {
                AddArticle.add();
            }
        });
        
        $("#preview").dialog({
            "modal": true,
            "hideFooter": true,
            "height": 402
        });
    },
    
    /**
     * @description 预览文章
     */
    preview: function () {
        var it = this;
        $.ajax({
            url: "/markdown",
            type: "POST",
            cache: false,
            data: {
                markdownText: it.editor.getValue()
            },
            success: function(result, textStatus){
                $("#preview").dialog("open");
                $("#preview").html(result.html);
            }
        });
    },
    
    /**
     * @description 显示简要语法
     */
    grammar: function () {
        var $grammar = $(".grammar"),
        $codemirror = $(".CodeMirror");
        if ($codemirror.width() > 900) {
            $grammar.show();
            $codemirror.width(770);
        } else {
            $grammar.hide();
            $codemirror.width(996)
        }
       
    }
};

AddArticle.init();