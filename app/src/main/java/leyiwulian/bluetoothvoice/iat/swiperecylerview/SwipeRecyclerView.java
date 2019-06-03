package leyiwulian.bluetoothvoice.iat.swiperecylerview;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

public class SwipeRecyclerView extends RecyclerView {

    private int touchSlop;
    private VelocityTracker velocityTracker = null;
    private boolean enableEdit = true;

    public SwipeRecyclerView(Context context) {
        super(context);
        init(context);
    }

    public SwipeRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SwipeRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context c){
        touchSlop = (int)(ViewConfiguration.get(c).getScaledPagingTouchSlop()*2f);
    }

    public void endEditing(){
        if (touchView != null){
            touchView.endEditMode();
        }
    }

    //是否允许编辑
    public void enableEdit(boolean enable){
        enableEdit = enable;
    }

    private TouchView touchView = null;
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        if (velocityTracker == null){
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(ev);
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:{
                if (enableEdit){
                    TouchView p = getTouchPosition(x,y);
                    if (touchView != null && touchView.isEnterEditMode() && p != null){
                        if (p.position != touchView.position){
                            touchView.endEditMode();
                        }else {
                            touchView.reEnterEditMode(x,y);
                        }
                    }else {
                        touchView = p;
                    }
                }else {
                    touchView = null;
                }
            }
                break;
            case MotionEvent.ACTION_MOVE:{
                if (touchView != null && touchView.isAllowEnterEditMode()){
                    if (!touchView.isEnterEditMode()){
                        //判断是否达到进入编辑模式的条件
                        velocityTracker.computeCurrentVelocity(1000);
                        float speedX = Math.abs(velocityTracker.getXVelocity());
                        float speedY = Math.abs(velocityTracker.getYVelocity());
                        if (Math.abs(x - touchView.lastX) > touchSlop && (speedX > speedY)){
                            TouchView p = getTouchPosition(x,y);
                            if (p!=null && p.position == touchView.position){
                                touchView.editMode = (x-touchView.lastX) > 0 ? EditMode.LEFT : EditMode.RIGHT;
                            }else {
                                touchView.endEditMode();
                                break;
                            }
                        }
                    }else {
                        if (!touchView.scrollBy(x,y)){
                            return true;
                        }
                    }
                    if (touchView != null) touchView.computeLastPoint(x,y);
                }
            }
                break;
            case MotionEvent.ACTION_UP:{
                if (velocityTracker != null){
                    velocityTracker.recycle();
                    velocityTracker = null;
                }
                if (touchView != null && touchView.isEnterEditMode()){
                    if (touchView.touchUp(x,y)){
                        touchView.enterEditMode();
                    }else {
                        touchView.endEditMode();
                    }
                }
            }
                break;
            case MotionEvent.ACTION_CANCEL:{
                if (velocityTracker != null){
                    velocityTracker.recycle();
                    velocityTracker = null;
                }
                if (touchView != null && touchView.isEnterEditMode()){
                    touchView.endEditMode();
                }
            }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        int action = e.getAction();
        if (action == MotionEvent.ACTION_MOVE){
            if (touchView != null && touchView.isEnterEditMode()){
                return true;
            }
        }else if (action == MotionEvent.ACTION_UP){
            if (touchView != null && touchView.interceptTouchEvent()){
                return true;
            }
        }
        return super.onInterceptTouchEvent(e);
    }

    //获取该点的itemView对象
    private TouchView getTouchPosition(int x,int y){
        if (getLayoutManager() instanceof LinearLayoutManager){
            if (getAdapter() != null){
                int firstPosition = ((LinearLayoutManager)getLayoutManager()).findFirstVisibleItemPosition();
                Rect rect = new Rect();
                for (int i=0;i<getChildCount();i++){
                    View v = getChildAt(i);
                    if (v.getVisibility() == VISIBLE){
                        v.getHitRect(rect);
                        if (rect.contains(x,y)){
                            if (getAdapter() instanceof ISwipeRecyclerViewAdapter){
                                return new TouchView(v,firstPosition+i,x,y,(ISwipeRecyclerViewAdapter)getAdapter());
                            }else {
                                return new TouchView(v,firstPosition+i,x,y,null);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private class TouchView{

        int position;//位置
        View itemView;//此次操作的item View对象
        ISwipeRecyclerViewAdapter swipeRecyclerViewAdapter;//在Adapter类中实现该接口，也是判断RecyclerView 是否有编辑模式(必须实现)
        EditMode editMode = EditMode.NONE;
        int downX,downY,lastX,lastY = 0;
        private boolean interceptTouchEvent = false;//是否消费此次touch时间

        TouchView(View v, int position, int downX, int downY, ISwipeRecyclerViewAdapter adapter){
            this.position = position;
            this.itemView = v;
            this.downX = downX;
            this.lastX = downX;
            this.downY = downY;
            this.lastY = downY;
            swipeRecyclerViewAdapter = adapter;
        }

        //用于判断是否取消当前准备处理的操作
        private int markX = 0;
        /**
         * 移动item的位置，且处理防止越界问题
         * **/
        private boolean scrollBy(int x,int y){
            int scrollX = itemView.getScrollX();
            if (editMode == EditMode.DONE){
                return false;
            }else if (editMode == EditMode.LEFT){//防止越界
                int maxX = -leftFunctionWidth();
                int temp = (int)((lastX-x)/1.5f);
                if (scrollX+temp<maxX){//超过编辑view的最大宽度
                    temp = maxX - scrollX;
                }else if (scrollX+temp>0){//在进入左编辑模式后，此次操作不能进入右编辑模式
                    temp = -scrollX;
                }else if (temp > 0){//有取消左编辑模式的倾向，最终会在touch up中进行计算
                    if (markX == 0)markX = x;
                }else if (temp < 0){//正常处理，需把倾向取消，
                    markX=0;
                }
                if (Math.abs(x-downX) > touchSlop){
                    interceptTouchEvent = true;
                }
                itemView.scrollBy(temp, 0);
            }else if (editMode == EditMode.RIGHT){//防止越界 参考上面备注
                int minX = rightFunctionWidth();
                int temp = (int)((lastX-x)/1.5f);
                if (scrollX+temp>minX){
                    temp = minX - scrollX;
                }else if (scrollX+temp<0){
                    temp = -scrollX;
                }else if (temp < 0){
                    if (markX == 0)markX = x;
                }else if (temp > 0){
                    markX=0;
                }
                if (Math.abs(x-downX) > touchSlop){
                    interceptTouchEvent = true;
                }
                itemView.scrollBy(temp, 0);
            }
            lastX = x;
            lastY = y;
            return false;
        }

        //结束辑操作，本次事件不在处理
        private void endEditMode(){
            setEditing(false);
            markX = 0;
            editMode = EditMode.DONE;
        }

        private void enterEditMode(){
            setEditing(true);
            markX = 0;
        }

        //松开手指时判断是否进入编辑模式，true 达到进入编辑模式条件，false 为达到条件
        private boolean touchUp(int x,int y){
            if (editMode == EditMode.DONE){
                interceptTouchEvent = true;
                return false;
            }else if (!interceptTouchEvent){
                if (editMode == EditMode.LEFT && x < leftFunctionWidth()){
                    return true;
                }else if (editMode == EditMode.RIGHT && x > (itemView.getMeasuredWidth() - rightFunctionWidth())){
                    return true;
                }
                interceptTouchEvent = true;
                return false;
            }else if (markX > 0){
                boolean flag = Math.abs(markX-x) > touchSlop;
                markX = 0;
                interceptTouchEvent = true;
                return !flag;
            }else if (editMode == EditMode.LEFT && Math.abs(itemView.getScrollX()*1f/leftFunctionWidth()) > 0.25f){
                return true;
            }else if (editMode == EditMode.RIGHT && Math.abs(itemView.getScrollX()*1f/rightFunctionWidth()) > 0.25f){
                return true;
            }
            return false;
        }

        //设置时候进入编辑模式
        private void setEditing(boolean editing){
            if (itemView instanceof SwipeItemView){//有动画
                if (editing)((SwipeItemView) itemView).setEditType(editMode == EditMode.LEFT? SwipeItemView.EditType.left: SwipeItemView.EditType.right);
                else ((SwipeItemView) itemView).setEditType(SwipeItemView.EditType.none);
            }else {//无动画
                if (editMode == EditMode.LEFT){
                    if (editing) itemView.scrollTo(-leftFunctionWidth(),0);
                    else itemView.scrollTo(0,0);
                }else if (editMode == EditMode.RIGHT){
                    if (editing) itemView.scrollTo(rightFunctionWidth(),0);
                    else itemView.scrollTo(0,0);
                }
            }
            if (swipeRecyclerViewAdapter != null){
                if (editing)swipeRecyclerViewAdapter.enterEditMode(position,editMode==EditMode.LEFT?getLeftView():getRightView());
                else swipeRecyclerViewAdapter.exitEditMode(position,editMode==EditMode.LEFT?getLeftView():getRightView());
            }
        }

        //是否有编辑模式
        private boolean isAllowEnterEditMode(){
            return (leftFunctionWidth()>0 || rightFunctionWidth()>0) && (editMode != EditMode.DONE);
        }

        //是否已进入编辑模式
        private boolean isEnterEditMode(){
            return editMode == EditMode.LEFT || editMode == EditMode.RIGHT;
        }

        /**计算在手指移动时是否赋值上一个点（当x轴移动距离大于y轴移动距离，认为手指此时是为x方向移动，不要赋值），
         * 该方法为进入编辑模式而处理
         * **/
        private void computeLastPoint(int x,int y){
            int mx = Math.abs(lastX-x);
            int my = Math.abs(lastY-y);
            if (mx<my){
                lastX = x;
                lastY = y;
            }
        }

        //记录touch down的位置
        private void reEnterEditMode(int x,int y){
            downX = lastX = x;
            downY = lastY = y;
            interceptTouchEvent = false;
        }

        //左边菜单的宽度
        private int leftFunctionWidth(){
            if (itemView instanceof SwipeItemView){
                return ((SwipeItemView) itemView).getLeftFunctionViewWidth();
            }else if (swipeRecyclerViewAdapter == null){
                if (itemView instanceof ViewGroup && ((ViewGroup)itemView).getChildCount() > 1){
                    ViewGroup itemViewG = (ViewGroup)itemView;
                    for (int i=0;i<itemViewG.getChildCount();i++){
                        if (itemViewG.getChildAt(i).getVisibility() == VISIBLE){
                            return itemViewG.getChildAt(i).getMeasuredWidth();
                        }
                    }
                }
                return 0;
            }
            return (int)(getContext().getResources().getDisplayMetrics().density*swipeRecyclerViewAdapter.leftFunctionWidth(position));
        }

        //又边菜单的宽度
        private int rightFunctionWidth(){
            if (itemView instanceof SwipeItemView){
                return ((SwipeItemView) itemView).getRightFunctionViewWidth();
            }else if (swipeRecyclerViewAdapter == null){
                if (itemView instanceof ViewGroup && ((ViewGroup)itemView).getChildCount() > 1){
                    ViewGroup itemViewG = (ViewGroup)itemView;
                    for (int i=itemViewG.getChildCount()-1;i>0;i--){
                        if (itemViewG.getChildAt(i).getVisibility() == VISIBLE){
                            return itemViewG.getChildAt(i).getMeasuredWidth();
                        }
                    }
                }
                return 0;
            }
            return (int)(getContext().getResources().getDisplayMetrics().density*swipeRecyclerViewAdapter.rightFunctionWidth(position));
        }

        private boolean interceptTouchEvent(){
            return interceptTouchEvent;
        }

        private View getLeftView(){
            if (leftFunctionWidth() != 0){
                if (itemView instanceof SwipeItemView){
                    return ((SwipeItemView) itemView).getLeftView();
                }
                if (itemView instanceof ViewGroup){
                    ViewGroup itemViewG = (ViewGroup)itemView;
                    for (int i=0;i<itemViewG.getChildCount();i++){
                        if (itemViewG.getChildAt(i).getVisibility() == VISIBLE){
                            return itemViewG.getChildAt(i);
                        }
                    }
                }
            }
            return null;
        }

        private View getRightView(){
            if (rightFunctionWidth() >0){
                if (itemView instanceof SwipeItemView){
                    return ((SwipeItemView) itemView).getRightView();
                }
                if (itemView instanceof ViewGroup){
                    ViewGroup itemViewG = (ViewGroup)itemView;
                    for (int i=itemViewG.getChildCount()-1;i>0;i--){
                        if (itemViewG.getChildAt(i).getVisibility() == VISIBLE){
                            return itemViewG.getChildAt(i);
                        }
                    }
                }
            }
            return null;
        }

    }

    private enum EditMode{

        NONE,//初始
        LEFT,//左编辑模式
        RIGHT,//右编辑
        DONE//结束尺寸编辑模式

    }

}
