package per.learn.slidingdelete.lib;

import per.learn.slidingdelete.R;
import per.learn.slidingdelete.util.LogUtil;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
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

    private int mLastDeleteBtnShowingPos = -1;
    private int[] mShowingDeleteBtnLocation = new int[2];

    private OnDeleteItemListener mDeleteItemListener;
    private View.OnTouchListener mOnTouchListener;
    private boolean mCancelMotionEvent = false;

    private VelocityTracker mTracker;
    private static final int MAX_FLING_VELOCITY = ViewConfiguration.getMinimumFlingVelocity() * 10;

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
        setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        super.setOnTouchListener(this);
        mShowAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anim_show_delete_buttun);
        mShowAnim.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
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
    public boolean onTouchEvent(MotionEvent event) {

        if(mCancelMotionEvent && event.getAction() == MotionEvent.ACTION_MOVE) {
            return true;
        } else if(mCancelMotionEvent && event.getAction() == MotionEvent.ACTION_DOWN) {
            event.setAction(MotionEvent.ACTION_CANCEL);
        }

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if(mTracker == null)
                    mTracker = VelocityTracker.obtain();
                else
                    mTracker.clear();

                LogUtil.Log("onTouch(), action down");

                mLastMotionX = event.getX();
                mLastMotionY = event.getY();
            }break;

            case MotionEvent.ACTION_MOVE: {
                //LogUtil.Log("onTouch(), action move");

                mTracker.addMovement(event);
                mTracker.computeCurrentVelocity(1000);
                int curVelocityX = (int) mTracker.getXVelocity();

                ListView lv = (ListView)this;
                float curX = event.getX();
                float curY = event.getY();
                int lastPos = ((ListView)this).pointToPosition(
                        (int)mLastMotionX, (int)mLastMotionY);
                int curPos = lv.pointToPosition((int)curX, (int)curY);
                int distanceX = (int)(mLastMotionX - curX);
                if(lastPos == curPos && (distanceX >= MAX_DISTANCE || curVelocityX < -MAX_FLING_VELOCITY)) {
                    LogUtil.Log("onTouch(), action move, curVelocityX = " + curVelocityX
                            + ", max fling velocity = " + MAX_FLING_VELOCITY);
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

                                mLastDeleteBtnShowingPos = -1;
                            }
                        });

                        mCancelMotionEvent = true;
                    }
                }

                lv = null;
            }break;

            case MotionEvent.ACTION_UP: {
                LogUtil.Log("onTouch(), action up");
                if(mTracker != null) {
                    mTracker.clear();
                    mTracker.recycle();
                    mTracker = null;
                }

                mCancelMotionEvent = false;

                if(mLastDeleteBtnShowingPos != -1) {
                    event.setAction(MotionEvent.ACTION_CANCEL);
                }
            }break;

            case MotionEvent.ACTION_CANCEL: {
                LogUtil.Log("onTouch(), action cancel");

                if(mLastDeleteBtnShowingPos != -1) {
                    ListView lv = (ListView)this;
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
        }

        return super.onTouchEvent(event);
    }

    /*@Override
    public boolean onTouch(View v, MotionEvent event){
        if(mCancelMotionEvent && event.getAction() == MotionEvent.ACTION_MOVE) {
            return true;
        } else if(mCancelMotionEvent && event.getAction() == MotionEvent.ACTION_DOWN) {
            event.setAction(MotionEvent.ACTION_CANCEL);
        }

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if(mTracker == null)
                    mTracker = VelocityTracker.obtain();
                else
                    mTracker.clear();

                LogUtil.Log("onTouch(), action down");

                mLastMotionX = event.getX();
                mLastMotionY = event.getY();
            }break;

            case MotionEvent.ACTION_MOVE: {
                //LogUtil.Log("onTouch(), action move");

                mTracker.addMovement(event);
                mTracker.computeCurrentVelocity(1000);
                int curVelocityX = (int) mTracker.getXVelocity();

                ListView lv = (ListView)v;
                float curX = event.getX();
                float curY = event.getY();
                int lastPos = ((ListView)v).pointToPosition(
                        (int)mLastMotionX, (int)mLastMotionY);
                int curPos = lv.pointToPosition((int)curX, (int)curY);
                int distanceX = (int)(mLastMotionX - curX);
                if(lastPos == curPos && (distanceX >= MAX_DISTANCE || curVelocityX < -MAX_FLING_VELOCITY)) {
                    LogUtil.Log("onTouch(), action move, curVelocityX = " + curVelocityX
                            + ", max fling velocity = " + MAX_FLING_VELOCITY);
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

                                mLastDeleteBtnShowingPos = -1;
                            }
                        });

                        mCancelMotionEvent = true;
                    }
                }

                lv = null;
            }break;

            case MotionEvent.ACTION_UP: {
                LogUtil.Log("onTouch(), action up");
                if(mTracker != null) {
                    mTracker.clear();
                    mTracker.recycle();
                    mTracker = null;
                }

                mCancelMotionEvent = false;

                if(mLastDeleteBtnShowingPos != -1) {
                    ListView lv = (ListView)v;
                    int firstVisibleItemPos = lv.getFirstVisiblePosition()
                            - lv.getHeaderViewsCount();
                    int factPos = mLastDeleteBtnShowingPos - firstVisibleItemPos;
                    mItemView = lv.getChildAt(factPos);
                    if(mItemView != null) {
                        getHandler().postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                if(mItemView != null) {
                                    mItemView.setPressed(false);
                                }
                            }
                        }, 100);
                    }

                    lv = null;

                    return true;
                }
            }break;

            case MotionEvent.ACTION_CANCEL: {
                LogUtil.Log("onTouch(), action cancel");

                if(mLastDeleteBtnShowingPos != -1) {
                    ListView lv = (ListView)v;
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
        }

        if(mOnTouchListener != null)
            return mOnTouchListener.onTouch(v, event);

        return false;
    }*/

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(mLastDeleteBtnShowingPos != -1 &&
                ev.getAction() == MotionEvent.ACTION_DOWN && !isClickDeleteBtn(ev)) {
            LogUtil.Log("onInterceptTouchEvent(), intercept action down event");

            ev.setAction(MotionEvent.ACTION_CANCEL);
            mCancelMotionEvent = true;

            return true;
        }

        return super.onInterceptTouchEvent(ev);
    };

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        mOnTouchListener = l;
    }

    private boolean isClickDeleteBtn(MotionEvent ev) {
        mDeleteBtn.getLocationOnScreen(mShowingDeleteBtnLocation);

        int left = mShowingDeleteBtnLocation[0];
        int right = mShowingDeleteBtnLocation[0] + mDeleteBtn.getWidth();
        int top = mShowingDeleteBtnLocation[1];
        int bottom = mShowingDeleteBtnLocation[1] + mDeleteBtn.getHeight();

        boolean result = (ev.getRawX() >= left
                && ev.getRawX() <= right
                && ev.getRawY() >= top
                && ev.getRawY() <= bottom);

        return result;
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

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

}
