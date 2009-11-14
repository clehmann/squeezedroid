package net.chrislehmann.squeezedroid.activity;

import org.apache.commons.lang.StringUtils;

import net.chrislehmann.squeezedroid.R;
import net.chrislehmann.squeezedroid.listadapter.SearchResultExpandableListAdapter;
import net.chrislehmann.squeezedroid.model.SearchResult;
import net.chrislehmann.squeezedroid.service.SqueezeService;
import net.chrislehmann.squeezedroid.service.ServiceConnectionManager.SqueezeServiceAwareThread;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class SearchActivity extends SqueezedroidActivitySupport
{

   protected ExpandableListView resultsExpandableListView;
   protected EditText searchCriteriaText;
   protected ImageButton searchButton;
   protected TextView searchInstructionsText;
   protected TextView noResultsFoundText;
   
   protected SearchResultExpandableListAdapter adapter = new SearchResultExpandableListAdapter();
   
   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate( savedInstanceState );
      setContentView(R.layout.search_layout);
   
      searchButton = (ImageButton) findViewById( R.id.search_button );
      searchCriteriaText = (EditText) findViewById( R.id.search_input );
      searchInstructionsText = (TextView) findViewById( R.id.search_instructions_text );
      noResultsFoundText = (TextView) findViewById( R.id.search_no_results_text );
      
      resultsExpandableListView = (ExpandableListView) findViewById( R.id.search_expandable_list );
      resultsExpandableListView.setAdapter( adapter );

      searchButton.setOnClickListener( onSearchButtonClicked );
   
   };
   
   OnClickListener onSearchButtonClicked = new OnClickListener()
   {
      SqueezeServiceAwareThread doSearch = new SqueezeServiceAwareThread()
      {
         public void runWithService(SqueezeService service)
         {
            String searchTerms = searchCriteriaText.getText().toString();
            if( StringUtils.isNotBlank( searchTerms ))
            {
               final SearchResult result = service.search( searchTerms, 50 );
               if( result != null )
               {
                  adapter.setResult( result );
                  runOnUiThread( new Runnable()
                  {
                     public void run()
                     {
                        searchInstructionsText.setVisibility( View.INVISIBLE );
                        if( result.getTotalResults() == 0 )
                        {
                           resultsExpandableListView.setVisibility( View.INVISIBLE );
                           noResultsFoundText.setVisibility( View.VISIBLE );
                        }
                        else
                        {
                           resultsExpandableListView.setVisibility( View.VISIBLE );
                           noResultsFoundText.setVisibility( View.INVISIBLE );
                        }
                        adapter.notifyDataSetChanged();
                     }
                  });
               }
            }
            
         }
      };
      
      public void onClick(View v)
      {
         
         runWithService( doSearch, true );
         
      }
   };


}