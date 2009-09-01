package net.chrislehmann.squeezedroid.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Gallery;

public class AnimatedGallery extends Gallery {

	Boolean _isProgrammaticMove;
	Activity _parent;
	
	
	public AnimatedGallery(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public AnimatedGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AnimatedGallery(Context context) {
		super(context);
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		return false;
	}
	
	public void moveToSelection(final int position) {
		// TODO Auto-generated method stub

		int difference = getSelectedItemPosition() - position;
		super.onFling(null, null, difference * 600, 0);
		
		//Make sure we don't 'miss'
		new Thread() {
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					return;
				}
				_parent.runOnUiThread( new Runnable() {
					public void run() {
						if( getSelectedItemPosition() != position )
						{
							_isProgrammaticMove = true;
							setSelection(position);
							_isProgrammaticMove = false;
						}
					}
				});
			};
		}.start();
		
		super.setSelection(position);
	}
	
	
}
