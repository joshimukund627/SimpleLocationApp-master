package com.sample.foo.simplelocationapp;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
 
/*
 * get current lat and long ..
 */
public class MyLocation 
{ 
	private static final int TWO_MINUTES = 1000 * 60 * 2;
    Timer timer1; 
    //Location currentBestLocation;
    LocationManager lm; 
    LocationResult locationResult; 
    boolean gps_enabled=false; 
    boolean network_enabled=false; 
 
    public boolean getLocation(Context context, LocationResult result) 
    { 
        //I use LocationResult callback class to pass location value from MyLocation to user code. 
        locationResult=result; 
        if(lm==null) 
            lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE); 
 
        //exceptions will be thrown if provider is not permitted. 
        try{gps_enabled=lm.isProviderEnabled(LocationManager.GPS_PROVIDER);}catch(Exception ex){} 
        try{network_enabled=lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);}catch(Exception ex){} 
 
        //don't start listeners if no provider is enabled 
        if(!gps_enabled && !network_enabled) 
            return false; 
 
        if(gps_enabled)
        {
        	try{lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);}catch(Exception ex){}
        }
        if(network_enabled)
        {
        	if(isInternetOn(context))
        	{
        		try{lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);}catch(Exception ex){}
        	}
        }
        timer1=new Timer(); 
        timer1.schedule(new GetLastLocation(), 20000); 
        return true; 
    } 
 
    LocationListener locationListenerGps = new LocationListener() { 
        public void onLocationChanged(Location location) { 
            timer1.cancel(); 
            /*if(location.getAccuracy() < 50)
            {
            	currentBestLocation=location;
            }*/
            locationResult.gotLocation(location);
            lm.removeUpdates(this); 
            lm.removeUpdates(locationListenerNetwork); 
        } 
        public void onProviderDisabled(String provider) {} 
        public void onProviderEnabled(String provider) {} 
        public void onStatusChanged(String provider, int status, Bundle extras) {} 
    }; 
 
    LocationListener locationListenerNetwork = new LocationListener() { 
        public void onLocationChanged(Location location) { 
            timer1.cancel(); 
            /*if(location.getAccuracy() < 50)
            {
            	currentBestLocation=location;
            }*/
            locationResult.gotLocation(location); 
            lm.removeUpdates(this); 
            lm.removeUpdates(locationListenerGps); 
        } 
        public void onProviderDisabled(String provider) {} 
        public void onProviderEnabled(String provider) {} 
        public void onStatusChanged(String provider, int status, Bundle extras) {} 
    }; 
 
    class GetLastLocation extends TimerTask { 
        @Override 
        public void run() { 
        	try
        	{
        		lm.removeUpdates(locationListenerGps); 
        		lm.removeUpdates(locationListenerNetwork); 
 
        		Location net_loc=null, gps_loc=null; 
        		if(gps_enabled) 
        			gps_loc=lm.getLastKnownLocation(LocationManager.GPS_PROVIDER); 
        		if(network_enabled) 
        			net_loc=lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER); 
 
        		//if there are both values use the latest one 
        		if(gps_loc!=null && net_loc!=null)
        		{
        			if(gps_loc.getTime()>net_loc.getTime()) 
        				locationResult.gotLocation(gps_loc); 
        			else 
        				locationResult.gotLocation(net_loc); 
        			return; 
        		} 
 
        		if(gps_loc!=null)
        		{ 
        			locationResult.gotLocation(gps_loc); 
        			return; 
        		} 
        		if(net_loc!=null)
        		{ 
        			locationResult.gotLocation(net_loc); 
        			return; 
        		} 
             locationResult.gotLocation(null);
        	}
        	catch(Exception e)
        	{
        	}
        } 
             
    } 
    
    protected boolean isBetterLocation(Location location, Location currentBestLocation) 
	{
	    if (currentBestLocation == null) 
	    {
	        // A new location is always better than no location
	        return true;
	    }

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
	    } 
	    else if (isSignificantlyOlder) 
	    {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) 
	    {
	        return true;
	    }
	    else if (isNewer && !isLessAccurate) 
	    {
	        return true;
	    }
	    else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) 
	    {
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
 
    public static abstract class LocationResult{ 
        public abstract void gotLocation(Location location); 
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
