package com.sample.foo.simplelocationapp;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.SettingsApi;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity 
{
	private static final int TWO_MINUTES = 1000 * 60 * 2;
	
	double longitudeBest, latitudeBest;
    double longitudeFused, latitudeFused;
    double longitudeGPSNetwork, latitudeGPSNetwork;
    
    TextView longitudeValueBest, latitudeValueBest;
    TextView longitudeValueFused, latitudeValueFused;
    TextView longitudeValueGPSNetwork, latitudeValueGPSNetwork;
    TextView Valueprovider;
    
    Location gpsnetwroklocation,fusedlocation;

    //new code

	// Mukund Joshi mac

	//VISHAL

	//Cool 67
	//ronil
	
	//Mukund Ramdas Joshi

	//ayyaz khan

	//branch changes

	//branch change 2

	//akshay...

    //account changed

	//changes with new branch...

    Button btngetnetgpslocation,btngetfusedlocation,btnbestlocation;
    
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        longitudeValueBest = (TextView) findViewById(R.id.longitudeValueBest);
        latitudeValueBest = (TextView) findViewById(R.id.latitudeValueBest);
        
        longitudeValueFused = (TextView) findViewById(R.id.longitudeValueFused);
        latitudeValueFused = (TextView) findViewById(R.id.latitudeValueFused);
        
        longitudeValueGPSNetwork = (TextView) findViewById(R.id.longitudeValueGPSNETWROK);
        latitudeValueGPSNetwork = (TextView) findViewById(R.id.latitudeValueGPSNETWROK);
        
        Valueprovider= (TextView) findViewById(R.id.Valueprovider);
        
        btngetnetgpslocation= (Button) findViewById(R.id.locationControllerGPSNETWROK);
        btngetnetgpslocation.setOnClickListener(new OnClickListener() 
        {
			@Override
			public void onClick(View v) 
			{
 				PackageManager pm = getApplicationContext().getPackageManager();
 		        boolean hasGps = pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
 		        if(hasGps || isInternetOn(getApplicationContext()))
 		        {
 		        	Toast.makeText(getApplicationContext(),"GPSNETWORK button click",Toast.LENGTH_SHORT).show();
 		        	GPSNETWORK();
 		        }
 		        else
 		        {
 		        	Toast.makeText(getApplicationContext(),"GPS chip not available in your device",Toast.LENGTH_SHORT).show();
 		        }
			}
		});
        
        btngetfusedlocation= (Button) findViewById(R.id.locationControllerFused);
        btngetfusedlocation.setOnClickListener(new OnClickListener() 
        {
			@Override
			public void onClick(View v) 
			{
				Toast.makeText(getApplicationContext(),"FUSED button click",Toast.LENGTH_SHORT).show();
				if (isGooglePlayServicesAvailable()) 
				{
					Toast.makeText(getApplicationContext(),"FUSED button click",Toast.LENGTH_SHORT).show();
					FUSEDLOCATION();
				}
				else
				{
					Toast.makeText(getApplicationContext(),"Google play service is not available in your device",Toast.LENGTH_SHORT).show();
				}
			}
		});
        
        btnbestlocation= (Button) findViewById(R.id.locationControllerBEST);
        btnbestlocation.setOnClickListener(new OnClickListener() 
        {
			@Override
			public void onClick(View v) 
			{
				Toast.makeText(getApplicationContext(),"BESTLOCATION button click",Toast.LENGTH_SHORT).show();
				//if there are both values use the latest one 
        		if(null!=gpsnetwroklocation && null!=fusedlocation)
        		{ 
        			if(isBetterLocation(gpsnetwroklocation, fusedlocation))
    				{
        				latitudeValueBest.setText("Latitude: "+gpsnetwroklocation.getLatitude());
    					longitudeValueBest.setText("Longitude: "+gpsnetwroklocation.getLongitude()+"\nProvider: "+gpsnetwroklocation.getProvider());

    				}
    				else
					{
						latitudeValueBest.setText("Latitude: "+fusedlocation.getLatitude());
						longitudeValueBest.setText("Longitude: "+fusedlocation.getLongitude()+"\nProvider: "+fusedlocation.getProvider());
					}

                }
        		else if(null!=gpsnetwroklocation)
				{
					latitudeValueBest.setText("Latitude: "+gpsnetwroklocation.getLatitude());
					longitudeValueBest.setText("Longitude: "+gpsnetwroklocation.getLongitude()+"\nProvider: "+gpsnetwroklocation.getProvider());

				}
				else if(null!=fusedlocation)
				{
					latitudeValueBest.setText("Latitude: "+fusedlocation.getLatitude());
					longitudeValueBest.setText("Longitude: "+fusedlocation.getLongitude());
					longitudeValueBest.setText("Longitude: "+fusedlocation.getLongitude()+"\nProvider: "+fusedlocation.getProvider());
				}
			}
		});
	}

	
	/** Determines whether one Location reading is better than the current Location fix
	  * @param location  The new Location that you want to evaluate
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	  */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) 
	{
	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
	    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) 
	    {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}
	
	public void GPSNETWORK()
	{
		MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
            @Override
            public void gotLocation(Location location) 
            {
            	if (location != null)
                {
            		gpsnetwroklocation=location;
            		
            		runOnUiThread (new Thread(new Runnable() 
                	{ 
                        public void run() 
                        {
                        	latitudeValueGPSNetwork.setText("Latitude: "+gpsnetwroklocation.getLatitude() + "");
                        	longitudeValueGPSNetwork.setText("Longitude: "+gpsnetwroklocation.getLongitude() + "\nAccuracy: " + gpsnetwroklocation.getAccuracy() + "\n" + gpsnetwroklocation.getAltitude());
                        	Valueprovider.setText("Provider: "+gpsnetwroklocation.getProvider());

                        	Toast.makeText(MainActivity.this, "My location code update", Toast.LENGTH_SHORT).show();
                        }
                    }));
                }
               else
                {
            	   Log.d("MainActivitycode", "Location not found");
                }
            }
        };

        MyLocation myLocation = new MyLocation();
        myLocation.getLocation(getApplicationContext(), locationResult);
	}
	
	
	public void FUSEDLOCATION()
	{
		FusedLocation.LocationResult locationResult = new FusedLocation.LocationResult() {
            @Override
            public void gotLocation(Location location) 
            {
                if (location != null) 
                {
                	fusedlocation=location;
                	
                	runOnUiThread (new Thread(new Runnable() 
                	{ 
                        public void run() 
                        {
                        	latitudeValueFused.setText("Latitude: "+fusedlocation.getLatitude() + "");
                        	longitudeValueFused.setText("Longitude: "+fusedlocation.getLongitude() + "\nAccuracy: " + fusedlocation.getAccuracy() + "\n" + fusedlocation.getAltitude());
                        	
                        	Toast.makeText(MainActivity.this, "Fused location code update", Toast.LENGTH_SHORT).show();
                        }
                    }));
                	
                }
                else
                {
                	Log.d("MainActivitycode", "Location not found");
    			}
            }
        };

        FusedLocation fusedLocation = new FusedLocation();
        fusedLocation.getLocation(getApplicationContext(), locationResult);
		
	}
	
	 private boolean isGooglePlayServicesAvailable() {
	        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	        if (ConnectionResult.SUCCESS == status) {
	            return true;
	        } else {
	            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
	            return false;
	        }
	    }

	 /**
	 * check internet working or not
	 */
	public final boolean isInternetOn(Context context) {
		ConnectivityManager connec = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		// ARE WE CONNECTED TO THE NET
		if (connec.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED
				|| connec.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTING
				|| connec.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTING
				|| connec.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED) {
			// MESSAGE TO SCREEN FOR TESTING (IF REQ)
			// Toast.makeText(this, connectionType + � connected�,
			// Toast.LENGTH_SHORT).show();
			return true;
		} else if (connec.getNetworkInfo(0).getState() == NetworkInfo.State.DISCONNECTED
				|| connec.getNetworkInfo(1).getState() == NetworkInfo.State.DISCONNECTED) {
			// System.out.println(�Not Connected�);
			return false;
		}
		return false;
	}
}
