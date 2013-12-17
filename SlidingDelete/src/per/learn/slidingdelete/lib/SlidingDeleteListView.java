package per.learn.slidingdelete.lib;

import per.learn.slidingdelete.R;
import per.learn.slidingdelete.util.LogUtil;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ListView;

public class SlidingDeleteListView extends ListView implements View.OnTouchListener {

    public static final int MAX_DISTANCE = 100;

    private int mDeleteBtnID = -1;

    private float mLastMotionX, mLastMotionY;

    private View mItemView;
    private View mDeleteBtn;

    private Animation mShowAnim, mHideAnim;
    private boolean mIsAnimPlaying = false;

    private int mLastDeleteBtnShowingPos = -1;

    private OnDeleteItemListener mDeleteItemListener;
    private View.OnTouchListener mOnTouchListener;

    public SlidingDeleteListView(Context context) {
        super(context);
        init();
    }

    public SlidingDeleteListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SlidingDeleteListView(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        super.setOnTouchListener(this);
        mShowAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anim_show_delete_buttun);
        mShowAnim.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                mIsAnimPlaying = true;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //mIsAnimPlaying = false;
                //mDeleteBtn.clearAnimation();

                if(mDeleteItemListener != null)
                    mDeleteItemListener.onShowDeleteBtn(mDeleteBtn);

            }
        });
        mHideAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anim_hide_delete_button);
        mHideAnim.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mDeleteBtn.setVisibility(View.GONE);
                mDeleteBtn.clearAnimation();

                if(mDeleteItemListener != null)
                    mDeleteItemListener.onHideDeleteBtn(mDeleteBtn);
            }
        });
    }

    public void setDeleteButtonID(int id) {
        mDeleteBtnID = id;
    }

    public void setOnDeleteItemListener(OnDeleteItemListener listener) {
        mDeleteItemListener = listener;
    }

    @Override
    public boolean onTouch(final View v, MotionEvent event){
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN: {

                //LogUtil.Log("onTouch(), action down");
                mLastMotionX = event.getX();
                mLastMotionY = event.getY();

                ListView lv = (ListView)v;
                if(mLastDeleteBtnShowingPos != -1) {
                    int firstVisiblePos = lv.getFirstVisiblePosition()
                            - lv.getHeaderViewsCount();
                    int factPos = mLastDeleteBtnShowingPos - firstVisiblePos;
                    mItemView = lv.getChildAt(factPos);
                    if(mItemView != null) {
                        if(mDeleteBtnID == -1)
                            throw new IllegalDeleteButtonIDException("Illegal DeleteButton resource id,"
                            		+ "ensure excute the function setDeleteButtonID(int id)");

                        mDeleteBtn = mItemView.findViewById(mDeleteBtnID);
                        mDeleteBtn.startAnimation(mHideAnim);
                    }

                    mLastDeleteBtnShowingPos = -1;
                }
            }break;

            case MotionEvent.ACTION_MOVE: {
                if(mIsAnimPlaying) {
                    return true;
                }

                //LogUtil.Log("onTouch(), action move");
                ListView lv = (ListView)v;
                float curX = event.getX();
                float curY = event.getY();
                int lastPos = ((ListView)v).pointToPosition(
                        (int)mLastMotionX, (int)mLastMotionY);
                int curPos = lv.pointToPosition((int)curX, (int)curY);
                int distanceX = (int)(mLastMotionX - curX);
                if(lastPos == curPos && distanceX >= MAX_DISTANCE) {
                    int firstVisiblePos = lv.getFirstVisiblePosition() - lv.getHeaderViewsCount();
                    int factPos = curPos - firstVisiblePos;
                    mItemView = lv.getChildAt(factPos);
                    if(mItemView != null) {
                        if(mDeleteBtnID == -1)
                            throw new IllegalDeleteButtonIDException("Illegal DeleteButton resource id,"
                                    + "ensure excute the function setDeleteButtonID(int id)");

                        mDeleteBtn = mItemView.findViewById(mDeleteBtnID);
                        mDeleteBtn.setVisibility(View.VISIBLE);
                        mDeleteBtn.startAnimation(mShowAnim);

                        mLastDeleteBtnShowingPos = curPos;
                        mDeleteBtn.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                if(mDeleteItemListener != null)
                                    mDeleteItemListener.onDeleteBtnClick(v, mLastDeleteBtnShowingPos);
                                mDeleteBtn.setVisibility(View.GONE);
                            }
                        });
                    }
                }
            }break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                //LogUtil.Log("onTouch(), action up");
                mIsAnimPlaying = false;
            }break;
        }

        if(mOnTouchListener != null)
            return mOnTouchListener.onTouch(v, event);

        return false;
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        mOnTouchListener = l;
    }

    public static interface OnDeleteItemListener {
        void onShowDeleteBtn(View button);
        void onHideDeleteBtn(View button);
        void onDeleteBtnClick(View button, int position);
    }

    @SuppressWarnings("serial")
    public static class IllegalDeleteButtonIDException extends RuntimeException {

        public IllegalDeleteButtonIDException() {}

        public IllegalDeleteButtonIDException(String msg) {
            super(msg);
        }
    }

}
