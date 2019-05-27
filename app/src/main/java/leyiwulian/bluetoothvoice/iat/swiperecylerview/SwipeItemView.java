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
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if ((functionType == FunctionType.left || functionType == FunctionType.right) && getChildCount() != 2){
            throw new RuntimeException("when functionType is left or right,this view must contain 2 children");
        }else if (functionType == FunctionType.leftAndRight && getChildCount() != 3){
            throw new RuntimeException("when functionType is leftAndRight,this view must contain 3 children");
        }
        if (swipeRecyclerViewAdapter != null){
            int leftWidth = swipeRecyclerViewAdapter.leftFunctionWidth(position);
            int rightWidth = swipeRecyclerViewAdapter.rightFunctionWidth(position);
            View contentView = null;
            if (functionType == FunctionType.left){
                View leftView = getChildAt(0);
                contentView = getChildAt(1);
                measureChild(leftView,widthMeasureSpec,heightMeasureSpec);
//                leftView.measure(MeasureSpec.makeMeasureSpec(leftWidth,MeasureSpec.EXACTLY),heightMeasureSpec);
            }else if (functionType == FunctionType.right){
                View rightView = getChildAt(1);
                contentView = getChildAt(0);
                measureChild(rightView,widthMeasureSpec,heightMeasureSpec);
//                rightView.measure(MeasureSpec.makeMeasureSpec(rightWidth,MeasureSpec.EXACTLY),heightMeasureSpec);
            }else if (functionType == FunctionType.leftAndRight){
                View leftView = getChildAt(0);
                contentView = getChildAt(1);
                leftView.measure(MeasureSpec.makeMeasureSpec(leftWidth, MeasureSpec.EXACTLY),heightMeasureSpec);
                View rightView = getChildAt(2);
                rightView.measure(MeasureSpec.makeMeasureSpec(rightWidth, MeasureSpec.EXACTLY),heightMeasureSpec);
            }
            if (contentView != null){
                measureChild(contentView,widthMeasureSpec,heightMeasureSpec);
            }
        }else {
            View contentView = null;
            if (functionType == FunctionType.left){
                View leftView = getChildAt(0);
                contentView = getChildAt(1);
                measureChild(leftView,widthMeasureSpec,heightMeasureSpec);
            }else if (functionType == FunctionType.right){
                View rightView = getChildAt(1);
                contentView = getChildAt(0);
                measureChild(rightView,widthMeasureSpec,heightMeasureSpec);
            }else if (functionType == FunctionType.leftAndRight){
                View leftView = getChildAt(0);
                contentView = getChildAt(1);
                measureChild(leftView,widthMeasureSpec,heightMeasureSpec);
                View rightView = getChildAt(2);
                measureChild(rightView,widthMeasureSpec,heightMeasureSpec);
            }
            if (contentView != null){
                measureChild(contentView,widthMeasureSpec,heightMeasureSpec);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (functionType == FunctionType.left){
            View leftView = getChildAt(0);
            View contentView = getChildAt(1);
            leftView.layout(-leftView.getMeasuredWidth(),0,0,leftView.getMeasuredHeight());
            contentView.layout(0,0,contentView.getMeasuredWidth(),contentView.getMeasuredHeight());
        }else if (functionType == FunctionType.right){
            View rightView = getChildAt(1);
            View contentView = getChildAt(0);
            rightView.layout(getMeasuredWidth(),0,getMeasuredWidth()+rightView.getMeasuredWidth(),rightView.getMeasuredHeight());
            contentView.layout(0,0,contentView.getMeasuredWidth(),contentView.getMeasuredHeight());
        }else if (functionType == FunctionType.leftAndRight){
            View leftView = getChildAt(0);
            View contentView = getChildAt(1);
            View rightView = getChildAt(2);
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
                View leftView = getChildAt(0);
                scroller.startScroll(getScrollX(),0,-leftView.getMeasuredWidth()-getScrollX(),0);
            }break;
            case right:{
                View rightView = getChildAt(1);
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
