package com.sdhy.video.client;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

public class InstantAutoComplete extends AutoCompleteTextView
{  
    private int myThreshold =0;  
  
    public InstantAutoComplete(Context context) {  
        super(context);  
    }  
  
    public InstantAutoComplete(Context context, AttributeSet attrs) {  
        super(context, attrs);  
    }  
  
    public InstantAutoComplete(Context context, AttributeSet attrs, int defStyle) {  
        super(context, attrs, defStyle);  
    }  
  
    @Override  
    public boolean enoughToFilter() {  
        return true;  
    }  
    
  

	@Override  
    protected void onFocusChanged(boolean focused, int direction,  
                                  Rect previouslyFocusedRect) {  
        super.onFocusChanged(focused, direction, previouslyFocusedRect);  
        if (focused) 
        {  
        	if(getText()==null)
        	{
//        		Log.e(")))))))))))))))))", "Line40");
        	}
        	else
        	{
				if (getText()!=null&&getText().length()>0)
				{
//					Log.e("InstantAuto1 ###### ",getText().toString());
					performFiltering(getText(), 0);
//					Log.e("InstantAuto2 ****** ",getText().toString());
				}
			}
//            showDropDown(); 
        }  
    }  
  
    @Override
	public void setThreshold(int threshold) {  
        if (threshold < 0) {  
            threshold = 0;  
        }  
        myThreshold = threshold;  
    }  
  
    @Override
	public int getThreshold() {  
        return myThreshold;  
    }


}