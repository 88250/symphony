//单位长度
unitSize=50
chessLength=30

//画棋盘
drawChessBoard:function(){
    Gobang.chessCanvas.strokeStyle = "gray";
    Gobang.chessCanvas.strokeRect(0,0,5+unitSize*chessLength,5+unitSize*chessLength);
}
//画棋子
drawChessMan:function(){

}