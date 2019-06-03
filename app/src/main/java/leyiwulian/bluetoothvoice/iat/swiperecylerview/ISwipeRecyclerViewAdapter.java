package leyiwulian.bluetoothvoice.iat.swiperecylerview;

public interface ISwipeRecyclerViewAdapter {

       /**
     * @return left function width dp
     * **/
    int leftFunctionWidth(int position);

    /**
     * @return right function width dp
     * **/
    int rightFunctionWidth(int position);

    /**
     * 进入编辑模式
     * @param editView function view
     * **/
    void enterEditMode(int position, View editView);

    /**
     * 退出编辑模式
     * @param editView function view
     * **/
    void exitEditMode(int position,View editView);

}
