package com.sample.foo.simplelocationapp;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class FusedLocation implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    //Comment added by VISHAL.

    //Mukund joshi

    private static final String TAG = "LocationActivity";
    
    LocationResult locationResult;
    Timer timer1; 
    
    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters
    
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    String mLastUpdateTime;

    //cool

    protected void createLocationRequest() 
    {
        mLocationRequest = new LocationRequest();
        //mLocationRequest.setInterval(UPDATE_INTERVAL);
        //mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT); // 10 meters
    }

    public void getLocation(Context context, LocationResult result)
    {
    	locationResult=result;
    	 
    	createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        
        mGoogleApiClient.connect();
        
        timer1=new Timer(); 
        timer1.schedule(new GetLastFusedLocation(), 20000);
    }
    
    protected void startLocationUpdates() 
    {
    	PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, "Location update started ..............: ");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: " + connectionResult.toString());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Firing onLocationChanged..............................................");
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();
    }

    private void updateUI() 
    {
        Log.d(TAG, "UI update initiated .............");
        if (null != mCurrentLocation) 
        {
        	/*String lat = String.valueOf(mCurrentLocation.getLatitude());
            String lng = String.valueOf(mCurrentLocation.getLongitude());
            tvLocation.setText("At Time: " + mLastUpdateTime + "\n" +
                    "Latitude: " + lat + "\n" +
                    "Longitude: " + lng + "\n" +
                    "Accuracy: " + mCurrentLocation.getAccuracy() + "\n" +
                    "Provider: " + mCurrentLocation.getProvider());*/
            
            
            timer1.cancel(); 
            locationResult.gotLocation(mCurrentLocation);
            stopLocationUpdates();
        }
        else 
        {
            Log.d(TAG, "location is null ...............");
        }
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        Log.d(TAG, "Location update stopped .......................");
    }
	
	@Override
    public void onConnected(Bundle bundle)
    {
        Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        startLocationUpdates();
    }
	
	class GetLastFusedLocation extends TimerTask 
	{ 
        @Override 
        public void run() 
        { 
        	//stopLocationUpdates();
        	mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        	try
        	{
        		if (null != mCurrentLocation)
        		{ 
        			locationResult.gotLocation(mCurrentLocation);
        			return; 
        		} 
             locationResult.gotLocation(null);
        	}
        	catch(Exception e)
        	{
        		Log.d(TAG, "location is ..............."+e);
        	}
        } 
             
    }

	public static abstract class LocationResult{ 
        public abstract void gotLocation(Location location); 
    }
}