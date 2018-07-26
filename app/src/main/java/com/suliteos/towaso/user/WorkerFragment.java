package com.suliteos.towaso.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class WorkerFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final LatLng DHANBAD_ISM = new LatLng(23.8110746, 86.4423321);
    private HashMap<String, MarkerHolder> markerHolderMap = new HashMap<>();
    private PolylineOptions mPolyLine;
    private Button mMapType;
    private ListenerRegistration registration;
    private Button showPath;
    ArrayList<LatLng> markerPoints;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        Bundle bundle = new Bundle();
        bundle.putLong("timestamp",System.currentTimeMillis());
        assert mUser != null;
        bundle.putString("name",mUser.getDisplayName());
        bundle.putString("fragment",WorkerFragment.class.getSimpleName());
        Analytics.logEventFragmentOpened(getContext(),bundle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_worker, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        showPath = rootView.findViewById(R.id.show_path);
        showPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLine();
            }
        });
        mMapType = rootView.findViewById(R.id.map_view);
        mMapType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapTypeView();
            }
        });
        mPolyLine = new PolylineOptions();
        markerPoints = new ArrayList<>();
        return rootView;
    }

    private void mapTypeView(){
        if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL){
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            mMapType.setText(getString(R.string.normal_view));
        }else if (mMap.getMapType() == GoogleMap.MAP_TYPE_SATELLITE){
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mMapType.setText(getString(R.string.satellite_view));
        }
    }

    private void showLine() {
        if (TextUtils.equals(showPath.getText(),getString(R.string.showPath))) {
            showPath.setText(getString(R.string.get_current_location));
            registration.remove();
//            int size = markerPoints.size();
//            Toast.makeText(getContext(), String.valueOf(size), Toast.LENGTH_SHORT).show();
//            LatLng origin = markerPoints.get(0);
//            LatLng dest = markerPoints.get(size-1);
//
//            // Getting URL to the Google Directions API
//            String url = getDirectionsUrl(origin, dest);
//            DownloadTask downloadTask = new DownloadTask();
//
//            // Start downloading json data from Google Directions API
//            downloadTask.execute(url);

            mPolyLine.width(8).color(Color.GREEN);
            mMap.addPolyline(mPolyLine);
        }else if (TextUtils.equals(showPath.getText(),getString(R.string.get_current_location))){
            showPath.setText(getString(R.string.showPath));loadData();
        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service

        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("while downloading url", e.toString());
        } finally {
            assert iStream != null;
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }
                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(6);
                lineOptions.color(Color.GREEN);

            }
            // Drawing polyline in the Google Map for the i-th route
            mMap.addPolyline(lineOptions);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapters());
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(DHANBAD_ISM)
                .zoom(12)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        loadData();
    }

    private void loadData() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.my_data_file), Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(getString(R.string.data_file_json_key), "");
        User mUser = gson.fromJson(json, User.class);

        String area = mUser.getArea();
        CollectionReference mLocationRef = FirebaseFirestore.getInstance().collection("Location").document(mUser.getState()).collection(mUser.getDistrict()).document(String.valueOf(mUser.getWard())).collection(area);

        registration = mLocationRef.document("Current").addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e == null) {
                    mMap.clear();
                    GeoPoint geoPoint = documentSnapshot.getGeoPoint("loc");
                    Date timeStamp = documentSnapshot.getDate("timeStamp");
                    LatLng latLng = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(documentSnapshot.get("name").toString())
                            .draggable(false)
                            .snippet(timeStamp.toString())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                    MarkerHolder mHolder = new MarkerHolder();
                    mHolder.setImageUrl(documentSnapshot.get("imageUrl").toString());
                    markerHolderMap.put(marker.getId(), mHolder);
                }else {
                    Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        String timeStamp = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(new Date());

        mLocationRef.document("Last").collection(timeStamp).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (e == null) {
                    if (documentSnapshots.isEmpty()) {
                        Toast.makeText(getContext(), "No Today Data", Toast.LENGTH_SHORT).show();
                    } else {
                        for (DocumentSnapshot documentSnapshot : documentSnapshots) {
                            if (documentSnapshot.exists()) {
                                GeoPoint geoPoint = documentSnapshot.getGeoPoint("loc");
                                LatLng location = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                                mPolyLine.add(location);
                                markerPoints.add(location);
                            } else {
                                Toast.makeText(getContext(), "No Today Data", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }else {
                    Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private class CustomInfoWindowAdapters implements GoogleMap.InfoWindowAdapter {

        private final View mContents;

        CustomInfoWindowAdapters() {
            mContents = getActivity().getLayoutInflater().inflate(R.layout.custom_info_contents, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            render(marker, mContents);
            return mContents;
        }

        private void render(Marker marker, View view) {

            MarkerHolder mHolder = markerHolderMap.get(marker.getId());

            final ImageView imageView = view.findViewById(R.id.badge);

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference mStorageRef = storage.getReference().child(mHolder.getImageUrl());
            final long ONE_MEGABYTE = 1024 * 1024;
            mStorageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    imageView.setImageBitmap(bitmap);
                }
            });

            String title = marker.getTitle();
            TextView titleUi = view.findViewById(R.id.title);
            if (title != null) {
                // Spannable string allows us to edit the formatting of the text.
                SpannableString titleText = new SpannableString(title);
                titleText.setSpan(new ForegroundColorSpan(Color.RED), 0, titleText.length(), 0);
                titleUi.setText(titleText);
            } else {
                titleUi.setText("");
            }

            String snippet = marker.getSnippet();
            TextView snippetUi = view.findViewById(R.id.snippet);
            if (snippet != null && snippet.length() > 12) {
                SpannableString snippetText = new SpannableString(snippet);
                snippetText.setSpan(new ForegroundColorSpan(Color.MAGENTA), 0, 10, 0);
                snippetText.setSpan(new ForegroundColorSpan(Color.BLUE), 12, snippet.length(), 0);
                snippetUi.setText(snippetText);
            } else {
                snippetUi.setText("");
            }
        }
    }

}
