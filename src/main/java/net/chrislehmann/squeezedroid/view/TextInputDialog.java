package net.chrislehmann.squeezedroid.view;

import net.chrislehmann.squeezedroid.R;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class TextInputDialog extends Dialog
{

   private Button okButton;
   private Button cancelButton;
   
   private EditText editText;
   private android.view.View.OnClickListener userOnClick;
   
   public TextInputDialog(Context context, int theme)
   {
      super( context, theme );
      initalize();
   }

   public TextInputDialog(Context context)
   {
      super( context );
      initalize();
   }

   private void initalize()
   {
      setContentView( R.layout.text_input_dialog_layout );
      okButton = (Button) findViewById( R.id.okButton );
      editText = (EditText) findViewById( R.id.editText );
      cancelButton = (Button) findViewById( R.id.cancelButton );
      
      okButton.setOnClickListener( onClick );
      cancelButton.setOnClickListener( onCancel );
   }
   

   public void setOnOkClickedListener( android.view.View.OnClickListener listener )
   {
      userOnClick = listener;
   }
   
   public String getText()
   {
      return editText.getText().toString();
   }
   
   private android.view.View.OnClickListener onCancel = new android.view.View.OnClickListener()
   {
      public void onClick(View v)
      {
         cancel();
      }
   };

   private android.view.View.OnClickListener onClick = new android.view.View.OnClickListener()
   {
      public void onClick(View v)
      {
         dismiss();
         if( userOnClick != null )
         {
            userOnClick.onClick( v );
         }
      }
   };
}
