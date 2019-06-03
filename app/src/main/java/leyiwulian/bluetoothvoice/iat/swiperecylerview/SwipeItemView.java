package leyiwulian.bluetoothvoice.iat.swiperecylerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

public class SwipeItemView extends ViewGroup {

    private FunctionType functionType = FunctionType.right;
    private ISwipeRecyclerViewAdapter swipeRecyclerViewAdapter = null;
    private int position =0;
    private Scroller scroller;
    private EditType editType = EditType.none;
    public SwipeItemView(Context context) {
        super(context);
        init(context,null);
    }

    public SwipeItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SwipeItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    private void init(Context c, AttributeSet attributeSet){
        if (attributeSet != null){
            TypedArray typedArray = c.obtainStyledAttributes(attributeSet,R.styleable.SwipeItemView);
            int type = typedArray.getInteger(R.styleable.SwipeItemView_functionType,2);
            if (type == 1)functionType = FunctionType.left;
            else if (type == 2)functionType = FunctionType.right;
            else if (type == 3)functionType = FunctionType.leftAndRight;
            typedArray.recycle();
        }
        scroller = new Scroller(c);
    }

    public void setSwipeRecyclerViewAdapter(ISwipeRecyclerViewAdapter swipeRecyclerViewAdapter,int position) {
        this.swipeRecyclerViewAdapter = swipeRecyclerViewAdapter;
        this.position = position;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if ((functionType == FunctionType.left || functionType == FunctionType.right) && getVisibilityChildCount() != 2){
            throw new RuntimeException("when functionType is left or right,this view must contain 2 visibility children");
        }else if (functionType == FunctionType.leftAndRight && getVisibilityChildCount() != 3){
            throw new RuntimeException("when functionType is leftAndRight,this view must contain 3 visibility children");
        }
        View leftView = getLeftView();
        View contentView = getContentView();
        View rightView = getRightView();
        if (swipeRecyclerViewAdapter != null){
            int leftWidth = swipeRecyclerViewAdapter.leftFunctionWidth(position);
            int rightWidth = swipeRecyclerViewAdapter.rightFunctionWidth(position);

            if (functionType == FunctionType.left){
//                measureChild(leftView,widthMeasureSpec,heightMeasureSpec);
                leftView.measure(MeasureSpec.makeMeasureSpec(leftWidth,MeasureSpec.EXACTLY),heightMeasureSpec);
            }else if (functionType == FunctionType.right){
//                measureChild(rightView,widthMeasureSpec,heightMeasureSpec);
                rightView.measure(MeasureSpec.makeMeasureSpec(rightWidth,MeasureSpec.EXACTLY),heightMeasureSpec);
            }else if (functionType == FunctionType.leftAndRight){
                leftView.measure(MeasureSpec.makeMeasureSpec(leftWidth, MeasureSpec.EXACTLY),heightMeasureSpec);
                rightView.measure(MeasureSpec.makeMeasureSpec(rightWidth, MeasureSpec.EXACTLY),heightMeasureSpec);
            }
            if (contentView != null){
                measureChild(contentView,widthMeasureSpec,heightMeasureSpec);
            }
        }else {
            if (functionType == FunctionType.left){
                measureChild(leftView,widthMeasureSpec,heightMeasureSpec);
            }else if (functionType == FunctionType.right){
                measureChild(rightView,widthMeasureSpec,heightMeasureSpec);
            }else if (functionType == FunctionType.leftAndRight){
                measureChild(leftView,widthMeasureSpec,heightMeasureSpec);
                measureChild(rightView,widthMeasureSpec,heightMeasureSpec);
            }
            if (contentView != null){
                measureChild(contentView,widthMeasureSpec,heightMeasureSpec);
            }
        }
    }

    private int getVisibilityChildCount(){
        int count = getChildCount();
        for (int i=0;i<getChildCount();i++){
            if (getChildAt(i).getVisibility() != VISIBLE){
                count--;
            }
        }
        return count;
    }

    View getLeftView(){
        if (functionType == FunctionType.left || functionType== FunctionType.leftAndRight){
            for (int i=0;i<getChildCount();i++){
                if (getChildAt(i).getVisibility() == VISIBLE){
                    return getChildAt(i);
                }
            }
        }
        return null;
    }

    private View getContentView(){
        if (functionType == FunctionType.left || functionType== FunctionType.leftAndRight){
            int index = 0;
            for (int i=0;i<getChildCount();i++){
                if (getChildAt(i).getVisibility() == VISIBLE){
                    if (index == 1){
                        return getChildAt(i);
                    }
                    index++;
                }
            }
        }else {
            for (int i=0;i<getChildCount();i++){
                if (getChildAt(i).getVisibility() == VISIBLE){
                    return getChildAt(i);
                }
            }
        }
        return null;
    }

    View getRightView(){
        if (functionType == FunctionType.right || functionType== FunctionType.leftAndRight){
            for (int i=getChildCount()-1;i>=0;i--){
                if (getChildAt(i).getVisibility() == VISIBLE){
                    return getChildAt(i);
                }
            }
        }
        return null;
    }

    public int getLeftFunctionViewWidth(){
        View left = getLeftView();
        if (left != null)return left.getMeasuredWidth();
        return 0;
    }

    public int getRightFunctionViewWidth(){
        View right = getRightView();
        if (right != null)return right.getMeasuredWidth();
        return 0;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        View leftView = getLeftView();
        View contentView = getContentView();
        View rightView = getRightView();
        if (functionType == FunctionType.left){
            leftView.layout(-leftView.getMeasuredWidth(),0,0,leftView.getMeasuredHeight());
            contentView.layout(0,0,contentView.getMeasuredWidth(),contentView.getMeasuredHeight());
        }else if (functionType == FunctionType.right){
            rightView.layout(getMeasuredWidth(),0,getMeasuredWidth()+rightView.getMeasuredWidth(),rightView.getMeasuredHeight());
            contentView.layout(0,0,contentView.getMeasuredWidth(),contentView.getMeasuredHeight());
        }else if (functionType == FunctionType.leftAndRight){
            leftView.layout(-leftView.getMeasuredWidth(),0,0,leftView.getMeasuredHeight());
            contentView.layout(0,0,contentView.getMeasuredWidth(),contentView.getMeasuredHeight());
            rightView.layout(getMeasuredWidth(),0,getMeasuredWidth()+rightView.getMeasuredWidth(),rightView.getMeasuredHeight());
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (scroller.computeScrollOffset()){
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            postInvalidate();
        }
    }

    public void setEditType(EditType type){
        this.editType = type;
        switch (editType){
            case left:{
                View leftView = getLeftView();
                scroller.startScroll(getScrollX(),0,-leftView.getMeasuredWidth()-getScrollX(),0);
            }break;
            case right:{
                View rightView = getRightView();
                if (functionType == FunctionType.leftAndRight)rightView = getChildAt(2);
                scroller.startScroll(getScrollX(),0,rightView.getMeasuredWidth()-getScrollX(),0);
            }break;
            case none:{
                scroller.startScroll(getScrollX(),0,-getScrollX(),0);
            }break;
        }
        invalidate();
    }

    private enum FunctionType{
        left,right,leftAndRight
    }

    public enum EditType{
        none,left,right
    }

}
