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

}
