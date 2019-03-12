package tcss450.uw.edu.chapp.weather;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tcss450.uw.edu.chapp.R;
import tcss450.uw.edu.chapp.WaitFragment;
import tcss450.uw.edu.chapp.model.Credentials;
import tcss450.uw.edu.chapp.utils.SendPostAsyncTask;
import tcss450.uw.edu.chapp.weather.WeatherLocationContent.WeatherLocationItem;

/**
 * fragment representing a list of WeatherLocationItems which are the previously stored weather
 * locations by the user to get the weather from again.
 */
public class WeatherLocationFragment extends Fragment {
    private OnListFragmentInteractionListener mListener;
    private RecyclerView mRecyclerView;
    private MyWeatherLocationRecyclerViewAdapter mAdapter;
    private ArrayList<WeatherLocationItem> mList;
    private Credentials mCreds;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public WeatherLocationFragment() {
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weatherlocation_list, container, false);

        if (getArguments() != null) {
            mCreds = (Credentials) getArguments().getSerializable(getString(R.string.key_credentials));
        }

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            mRecyclerView = recyclerView;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));

            //set the adapter equal to an array list of the weather items in the shared preferences.
            //need to use regex to isolate the fields for the WeatherLocationContent object.
            SharedPreferences prefs = getActivity().getSharedPreferences(getString(R.string.keys_shared_prefs),
                    Context.MODE_PRIVATE);
            //set of locations from
            Set<String> locationSet = prefs.getStringSet(getString(R.string.keys_prefs_location_set),
                    new HashSet<String>());

            setWeatherLocationsList();

            //mList = generateLocationList(locationSet);
//            mAdapter = new MyWeatherLocationRecyclerViewAdapter(mList, mListener, this);
//            recyclerView.setAdapter(mAdapter);
        }
        return view;
    }

    /**
     * method that will create an alert dialog where it is called
     * when an item from the WeatherLocationList is clicked on.
     * will allow for deleting of items or for loading the location value of the item.
     * @param listener is a OnListFragmentInteractionListener for on list clicked method.
     * @param position is the position in the list of items for the item to be deleted.
     * @param item is the item clicked on
     */
    public void makeAlertDialogOnItemClick(OnListFragmentInteractionListener listener, int position,
                                           WeatherLocationItem item) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Load " + item.city + " Weather?")
                .setNeutralButton("Delete", (i, d) -> {
                    deleteItem(position, item.city);
                })
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Ok", (i, d) -> {
                    listener.onListFragmentInteraction(item);
                })
                .create()
                .show();
    }

    private void deleteItem(int position, String city) {
        deleteItemFromList(position);
        deleteFromDataBase(city);


        //deleteItemFromSharedPrefs(prefString);
    }

    /**
     * method that will delete an item from the list of the position passed.
     * @param position
     */
    private void deleteItemFromList(int position) {
        //remove item
        mList.remove(position);
        mRecyclerView.removeViewAt(position);
        mAdapter.notifyItemRemoved(position);
        mAdapter.notifyItemRangeChanged(position, mList.size());
        //update the data
        mAdapter.notifyDataSetChanged();
    }

    private void deleteFromDataBase(String city) {
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_weather_base))
                .appendPath(getString(R.string.ep_weather_delete_location))
                .build();
        try {
            JSONObject deleteJSON = new JSONObject();
            deleteJSON.put("username", mCreds.getUsername());
            deleteJSON.put("city", city);
            new SendPostAsyncTask.Builder(uri.toString(), deleteJSON)
                    .build()
                    .execute();

        } catch(JSONException e) {
            e.printStackTrace();
        }
    }


//    private void deleteItemFromSharedPrefs(String prefString) {
//        SharedPreferences prefs = getActivity().getSharedPreferences(getString(R.string.keys_shared_prefs),
//                Context.MODE_PRIVATE);
//        Set<String> locationSet = prefs.getStringSet(getString(R.string.keys_prefs_location_set),
//                new HashSet<String>());
//
//        HashSet<String> copyLocationSet = new HashSet<String>(locationSet);
//        copyLocationSet.remove(prefString);
//        prefs.edit().putStringSet(getString(R.string.keys_prefs_location_set), copyLocationSet)
//                .apply();
//    }




    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * method that will generate a list of weather location items based on each
     * entry of the locationSet. will be using regex for getting individual fields.
     * @return list of weather location items.
     */
//    private ArrayList<WeatherLocationItem> generateLocationList(Set<String> locationSet) {
//        String pattern = getString(R.string.weather_prefs_location_regex);
//        Pattern regex = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
//
//        ArrayList<WeatherLocationItem> locations = new ArrayList<WeatherLocationItem>();
//
//        Iterator<String> itr = locationSet.iterator();
//        int i = 0;
//        while (itr.hasNext()) {
//            String locationString = itr.next();
//            Matcher m = regex.matcher(locationString);
//            if (m.matches()) {
//                String city = m.group(1);
//                double lat = Double.parseDouble(m.group(2));
//                double lon = Double.parseDouble(m.group(3));
//                //locations.add(new WeatherLocationItem(Integer.toString(i),city, lat, lon, locationString));
//                locations.add(new WeatherLocationItem(Integer.toString(i), city, lat, lon, 0));
//                i++;
//            }
//        }
//        return locations;
//    }

    private void setWeatherLocationsList() {
        if (mCreds != null) {
            String username = mCreds.getUsername();
            JSONObject usernameJSON = new JSONObject();
            try {
                usernameJSON.put("username", username);
                Uri uri = new Uri.Builder()
                        .scheme("https")
                        .appendPath(getString(R.string.ep_base_url))
                        .appendPath(getString(R.string.ep_weather_base))
                        .appendPath(getString(R.string.ep_weather_get_locations))
                        .build();
                new SendPostAsyncTask.Builder(uri.toString(), usernameJSON)
                        .onPreExecute(this::handleGetWeatherOnPre)
                        .onPostExecute(this::handleGetWeatherOnPost)
                        .build()
                        .execute();
            }
            catch(JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private void handleGetWeatherOnPre() {
        mListener.onWaitFragmentInteractionShow();
    }

    /**
     * method that is called when the post async task returns
     * @param result
     */
    private void handleGetWeatherOnPost(String result) {
        ArrayList<WeatherLocationItem> weatherLocationList = new ArrayList<WeatherLocationItem>();
        try  {
            JSONObject resultJSON = new JSONObject(result);
            if (resultJSON.getBoolean("success")) {
                JSONArray locationsJSON = resultJSON.getJSONArray("locations");
                for (int i = 0; i < locationsJSON.length(); i++) {
                    JSONObject locationJSON = locationsJSON.getJSONObject(i);
                    String city = locationJSON.getString("nickname");
                    String latString = locationJSON.getString("lat");
                    String lonString = locationJSON.getString("long");
                    String zipString = locationJSON.getString("zip");
                    //lat and lon or zip might be null
                    double lat=0, lon=0;
                    int zip = 0;
                    if (latString != null && !latString.equals("null") && lonString != null && !lonString.equals("null")) {
                        lat = Double.parseDouble(latString);
                        lon = Double.parseDouble(lonString);

                    }
                    if (zipString != null && !zipString.equals("null")) {
                        zip = Integer.parseInt(zipString);
                    }
                    WeatherLocationItem item = new WeatherLocationItem(Integer.toString(i), city, lat, lon, zip);
                    weatherLocationList.add(item);
                }
                /* Set the adapter to the list */
                mList = weatherLocationList;
                mAdapter = new MyWeatherLocationRecyclerViewAdapter(mList, mListener, this);
                mRecyclerView.setAdapter(mAdapter);


            }

        } catch (JSONException e) {
            Log.e("WEATHER LOAD JSON ERROR", e.toString());
        }
        mListener.onWaitFragmentInteractionHide();
    }



    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener extends WaitFragment
            .OnFragmentInteractionListener{

        void onListFragmentInteraction(WeatherLocationItem item);

    }
}
