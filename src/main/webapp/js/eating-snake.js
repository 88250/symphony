/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2018, b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
/**
 * @fileOverview Eating snake game. Alexar wrote it in Love2D, Zephyr translate it into JavaScript.
 * 
 * @author Zephyr
 * @author Alexar
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author zonghua
 * @version 1.1.0.1, Sep 22, 2016
 */
var EatingSnake = {
    dir: null,
    lastDir: null,
    map: null,
    food: null,
    R: 10, // 圆半径或者外接正方形尺寸的一半
    size: 30,
    snake: null,
    // oMark: null, // 分数显示框
    isPause: false, // 是否暂停
    snakeCanvas: null,
    interval: null,
    currTime: 200,
    stepTime: 5,
    baseLen: 6,
    startTime: null,
    endTime: null,
    countTime: null,
    snakeColor: 0,
    appleColor: 255,
    // 1: snake
    // 0: nothing
    // 2: apple
    // 3: block
    setupMap: function () {
        for (var x = 1; x <= EatingSnake.size; x++) {
            EatingSnake.map[x] = new Array();
            for (var y = 1; y <= EatingSnake.size; y++) {
                if (x == 1 || x == EatingSnake.size || y == 1 || y == EatingSnake.size)
                    EatingSnake.map[x][y] = 3
                else
                    EatingSnake.map[x][y] = 0
            }
        }
    },
    initMap: function (snakeCanvasId) {
        // EatingSnake.oMark = document.getElementById(oMarkId);
        EatingSnake.snakeCanvas = document.getElementById(snakeCanvasId).getContext('2d');
        EatingSnake.map = new Array();
        EatingSnake.setupMap();
        if (EatingSnake.snakeCanvas != null)
            EatingSnake.snakeCanvas.clearRect(0, 0, (EatingSnake.size - 1) * 2 * EatingSnake.R, (EatingSnake.size - 1) * 2 * EatingSnake.R);
        for (var x = 1; x <= EatingSnake.size; x++) {
            for (var y = 1; y <= EatingSnake.size; y++) {
                switch (EatingSnake.map[x][y]) {
                    case 0:
                        EatingSnake.snakeCanvas.strokeStyle = "gray";
                        EatingSnake.snakeCanvas.strokeRect((x - 1) * 2 * EatingSnake.R, (y - 1) * 2 * EatingSnake.R, 2 * EatingSnake.R, 2 * EatingSnake.R);
                        break;
                    case 1:
                        EatingSnake.snakeCanvas.fillStyle = "rgb(" + EatingSnake.snakeColor + ",0,0)";
                        EatingSnake.snakeCanvas.fillRect((x - 1) * 2 * EatingSnake.R, (y - 1) * 2 * EatingSnake.R, 2 * EatingSnake.R, 2 * EatingSnake.R);
                        break;
                    case 2:
                        EatingSnake.snakeCanvas.fillStyle = "rgb(" + EatingSnake.appleColor + ",0,0)";
                        EatingSnake.snakeCanvas.fillRect((x - 1) * 2 * EatingSnake.R, (y - 1) * 2 * EatingSnake.R, 2 * EatingSnake.R, 2 * EatingSnake.R);
                        break;
                    case 3:
                        EatingSnake.snakeCanvas.fillStyle = "gray";
                        EatingSnake.snakeCanvas.fillRect((x - 1) * 2 * EatingSnake.R, (y - 1) * 2 * EatingSnake.R, 2 * EatingSnake.R, 2 * EatingSnake.R);
                        break;
                }
            }
        }
    },
    drawMap: function () {
        EatingSnake.snakeCanvas.clearRect(0, 0, (EatingSnake.size - 1) * 2 * EatingSnake.R, (EatingSnake.size - 1) * 2 * EatingSnake.R);
        for (var x = 1; x <= EatingSnake.size; x++) {
            for (var y = 1; y <= EatingSnake.size; y++) {
                switch (EatingSnake.map[x][y]) {
                    case 0:
                        EatingSnake.snakeCanvas.strokeStyle = "gray";
                        EatingSnake.snakeCanvas.strokeRect((x - 1) * 2 * EatingSnake.R, (y - 1) * 2 * EatingSnake.R, 2 * EatingSnake.R, 2 * EatingSnake.R);
                        break;
                    case 1:
                        EatingSnake.snakeCanvas.fillStyle = "black";
                        EatingSnake.snakeCanvas.fillRect((x - 1) * 2 * EatingSnake.R, (y - 1) * 2 * EatingSnake.R, 2 * EatingSnake.R, 2 * EatingSnake.R);
                        break;
                    case 2:
                        EatingSnake.snakeCanvas.fillStyle = "red";
                        EatingSnake.snakeCanvas.fillRect((x - 1) * 2 * EatingSnake.R, (y - 1) * 2 * EatingSnake.R, 2 * EatingSnake.R, 2 * EatingSnake.R);
                        break;
                    case 3:
                        EatingSnake.snakeCanvas.fillStyle = "gray";
                        EatingSnake.snakeCanvas.fillRect((x - 1) * 2 * EatingSnake.R, (y - 1) * 2 * EatingSnake.R, 2 * EatingSnake.R, 2 * EatingSnake.R);
                        break;
                }
            }
        }
    },
    check: function (x, y) {
        if (EatingSnake.map[x][y] != 0)
            return true; // true代表此处有填充p
        else
            return false;
    },
    setupSnake: function () {
        for (var i = 1; i <= 5; i++) {
            EatingSnake.snake[i] = {
                x: i + 5,
                y: 7
            };
        }
    },
    drawSnake: function (toggle) {
        for (var i = 1; i < EatingSnake.snake.length; i++) {
            EatingSnake.map[EatingSnake.snake[i].x][EatingSnake.snake[i].y] = toggle;
        }
    },
    newFood: function () {
        do {
            EatingSnake.food.x = Math.floor(Math.random() * (EatingSnake.size - 1) + 1);
            EatingSnake.food.y = Math.floor(Math.random() * (EatingSnake.size - 1) + 1);
        } while (EatingSnake.check(EatingSnake.food.x, EatingSnake.food.y) == true)
        EatingSnake.map[EatingSnake.food.x][EatingSnake.food.y] = 2;
    },
    gameover: function () {
        clearInterval(EatingSnake.interval);

        var requestJSONObject = {
            score: (EatingSnake.snake.length - EatingSnake.baseLen)
        };

        $.ajax({
            url: Label.servePath + "/activity/eating-snake/collect",
            type: "POST",
            cache: false,
            data: JSON.stringify(requestJSONObject),
            beforeSend: function () {
                var $btn = $("button.green");
                $btn.attr("disabled", "disabled").css("opacity", "0.3").text($btn.text() + 'ing');
            },
            success: function (result, textStatus) {
                if (!result.sc) {
                    alert(result.msg);

                    return;
                }

                EatingSnake.snakeCanvas.fillStyle = "black";
                EatingSnake.snakeCanvas.fillRect(150, 100, 300, 200);
                EatingSnake.snakeCanvas.clearRect(155, 105, 290, 190);
                EatingSnake.snakeCanvas.font = '36px serif';
                var textWidth = EatingSnake.snakeCanvas.measureText("Game Over!").width;
                EatingSnake.snakeCanvas.fillText("Game Over!", 155 + (290 - textWidth) / 2, 150);
                EatingSnake.snakeCanvas.font = '24px serif';
                var score = EatingSnake.snake.length - EatingSnake.baseLen;
                textWidth = EatingSnake.snakeCanvas.measureText("Your Score: " + score).width;
                EatingSnake.snakeCanvas.fillText("Your Score: " + score, 155 + (290 - textWidth) / 2, 200);
                EatingSnake.snakeCanvas.fillStyle = "red";
                EatingSnake.snakeCanvas.font = "18px serif";
//                ctx.measureText(txt).width
                var resultText;
                if (score <= 10) {
                    resultText = "童鞋，换键盘吧，要不行换手";
                } else if (score > 10 && score <= 20) {
                    resultText = "如此平凡的分数恕我无力吐槽";
                } else if (score > 20 && score <= 30) {
                    resultText = "哇哦，好厉害哦！";
                } else if (score > 30 && score <= 40) {
                    resultText = "哎呀我滴老天爷呀";
                } else if (score > 40 && score <= 50) {
                    resultText = "请收下我的膝盖 OTZ";
                } else {
                    resultText = "太假了！(╯‵□′)╯︵┻━┻";
                }
                textWidth = EatingSnake.snakeCanvas.measureText(resultText).width;
                EatingSnake.snakeCanvas.fillText(resultText, 155 + (290 - textWidth) / 2, 250);
            },
            complete: function () {
                var $btn = $("button.green");
                $btn.removeAttr("disabled").css("opacity", "1").text($btn.text().substr(0, $btn.text().length - 3));

            }
        });

    },
    eat: function () {
        EatingSnake.snake[EatingSnake.snake.length] = {
            x: EatingSnake.snake[1].x,
            y: EatingSnake.snake[1].y
        };
        EatingSnake.newFood();
        EatingSnake.snakeColor += 5;
        EatingSnake.appleColor -= 5;
        clearInterval(EatingSnake.interval);
        if (EatingSnake.currTime >= 25) {
            EatingSnake.currTime = EatingSnake.currTime - EatingSnake.stepTime;
        }
        EatingSnake.interval = setInterval(EatingSnake.gameRun, EatingSnake.currTime);
    },
    updateSnake: function () {
        EatingSnake.lastDir.x = EatingSnake.dir.x
        EatingSnake.lastDir.y = EatingSnake.dir.y
        var targetX = EatingSnake.snake[1].x + EatingSnake.dir.x,
                targetY = EatingSnake.snake[1].y + EatingSnake.dir.y;
        if (EatingSnake.check(targetX, targetY)) {
            if (targetX == EatingSnake.food.x && targetY == EatingSnake.food.y) { // eat
                EatingSnake.eat();
            } else { // hit
                EatingSnake.gameover();
                return;
            }
        }
        EatingSnake.drawSnake(0)
        for (var i = EatingSnake.snake.length - 1; i >= 2; i--) {
            EatingSnake.snake[i].x = EatingSnake.snake[i - 1].x
            EatingSnake.snake[i].y = EatingSnake.snake[i - 1].y
        }
        EatingSnake.snake[1].x = targetX
        EatingSnake.snake[1].y = targetY

        EatingSnake.drawSnake(1)
    },
    input: function (keyCode) {
        switch (keyCode) {
            case 65:
            case 37: // 左边
                if (EatingSnake.lastDir.x == 0) {
                    EatingSnake.dir.x = -1;
                    EatingSnake.dir.y = 0;
                }
                break;
            case 87:
            case 38: // 上边
                if (EatingSnake.lastDir.y == 0) {
                    EatingSnake.dir.x = 0;
                    EatingSnake.dir.y = -1;
                }
                break;
            case 68:
            case 39: // 右边
                if (EatingSnake.lastDir.x == 0) {
                    EatingSnake.dir.x = 1;
                    EatingSnake.dir.y = 0;
                }
                break;
            case 83:
            case 40: // 下边
                if (EatingSnake.lastDir.y == 0) {
                    EatingSnake.dir.x = 0;
                    EatingSnake.dir.y = 1;
                }
                break;
            case 80: // 开始/暂停
                if (EatingSnake.isPause) {
                    EatingSnake.interval = setInterval(gameRun, currTime);
                    EatingSnake.isPause = false;
                } else {
                    clearInterval(interval);
                    EatingSnake.isPause = true;
                }
                break;
        }
    },
    init: function () {
        EatingSnake.dir = {
            x: 0,
            y: 1
        };
        EatingSnake.lastDir = {
            x: 0,
            y: 0
        };
        EatingSnake.map = new Array();
        EatingSnake.food = {
            x: 0,
            y: 0
        };
        EatingSnake.currTime = 200;

        EatingSnake.snake = new Array();
        EatingSnake.setupMap();
        EatingSnake.setupSnake();
        EatingSnake.drawSnake(1);
        EatingSnake.newFood();
        clearInterval(EatingSnake.interval);
    },
    gameRun: function () {
        // countTime += currTime;
        EatingSnake.updateSnake();
        EatingSnake.drawMap();
//        EatingSnake.oMark.innerHtml = EatingSnake.snake.length - EatingSnake.baseLen;
    },
    start: function (csrfToken) {

        window.addEventListener('keydown', function (event) {
            // 如果游戏的时候时使用方向键
            if (event.keyCode == 37 || event.keyCode == 38 || event.keyCode == 39 || event.keyCode == 40) {
                // 阻止方向键滚屏
                event.preventDefault();
                return false;
            }
        });

        $.ajax({
            url: Label.servePath + "/activity/eating-snake/start",
            type: "POST",
            headers: {"csrfToken": csrfToken},
            cache: false,
            success: function (result, textStatus) {
                if (result.sc) {
                    EatingSnake.init();
                    // countTime = 0;
                    EatingSnake.interval = setInterval(EatingSnake.gameRun, EatingSnake.currTime);
                    // startTime = new Date().getTime();

                    return;
                } else {
                    $("#tip").addClass("error").removeClass('succ').html('<ul><li>' + result.msg + '</li></ul>');
                }

                $("#tip").show();

                setTimeout(function () {
                    $("#tip").hide();
                }, 3000);
            }
        });
    }
};
