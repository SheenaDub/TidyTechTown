package com.tt.t.tidytechtowns;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;



    // nb this code was adapted from the code for the Android tutorial Weather app
public class CommunityScoresFragment extends Fragment {

    private MyDatabase db;
    private Cursor results;
    private ArrayAdapter<String> mScoresAdapter;

    public CommunityScoresFragment() {
    }

    @Override
    public void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        //Allow the fragement to access menu items.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu items for use in the action bar
        inflater.inflate(R.menu.scoresfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }


    // assign view variables and return view... ALSO
    // this function fetches the updated community totals and then
    // adds them to the fragments for them to be displayed
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_scores, container, false);

        // get community scores from database and place them in arraylist
        db = new MyDatabase(getActivity());
        results = db.getCommunityTotals();

        List<String> outputArray = new ArrayList<String>();

        ArrayList<community> communities = new ArrayList<community>();

        results.moveToFirst();
        while (results.isAfterLast() == false)  {
            communities.add(new community(results.getInt(1),results.getString(0)));

            results.moveToNext();
        }
        db.close();

        Collections.sort(communities, new Sortbyscore());
        for(community c:communities){
            outputArray.add(c.toString());
        }

        // add scores to list
        mScoresAdapter =
                new ArrayAdapter<String>(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_scores, // The name of the layout ID.
                        R.id.list_item_scores_textview, // The ID of the textview to populate.
                        outputArray);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_scores);
        listView.setAdapter(mScoresAdapter);

        return rootView;
    }

}

// Class for storing communities and their scores
class community{
    int score;
    String name;

    public community(int score, String name)
    {
        this.score = score;
        this.name = name;
    }

    public String toString()
    {
        return this.name + ":  " + this.score;
    }
}

// For sorting comminities by score
class Sortbyscore implements Comparator<community>
{
    // Used for sorting in descending order of score
    public int compare(community a, community b)
    {
        return b.score - a.score;
    }
}
