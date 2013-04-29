package com.example.bikesafety;

import android.content.SearchRecentSuggestionsProvider; 


/* Not actually used. for possible future implemenation of recent search suggestions*/
public class SuggestionProvider 
      extends SearchRecentSuggestionsProvider { 
   
   public static final String AUTHORITY = 
		   SuggestionProvider.class.getName(); 

   public static final int MODE = DATABASE_MODE_QUERIES; 

   public SuggestionProvider() { 
      setupSuggestions(AUTHORITY, MODE); 
   } 
}
