package com.sample.foo.simplelocationapp;

import java.io.Serializable;

import android.location.Location;

public class CurrentLocation extends Location implements Serializable  {

	Location location ;
	
	public CurrentLocation(Location l) {
		super(l);
		this.location = l;
	}

}
