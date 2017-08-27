//
package fltDispersion;

import static fltDispersion.FLTUtils.undef;


//
public final class FltInitData{
	//
	public static final int nFields=9;
	
	public float npart =undef;	// A unique float identifier (1,2,3,...)
	public float tstart=undef;	// start date of integration of float (in s)
								// Note: if tstart=-1 floats are integrated right from the beginning
	public float xpart =undef;	// x position of float (in units of XC)
	public float ypart =undef;	// y position of float (in units of YC)
	public float kpart =undef;	// actual vertical level of float
	public float kfloat=undef;	// target level of float (should be the same as kpart at the beginning)
	public float iup   =undef;	// float if the float
								// - should profile  ( > 0 = return cycle (in s) to surface)
								// - remain at depth ( = 0 )
								// - is a 3D float   ( = -1).
								// - should be advected WITHOUT additional noise ( = -2).
								//   (This implies that the float is non-profiling)
								// - is a mooring    ( = -3), i.e., the float is not advected
	public float itop  =undef;	// time of float the surface (in s)
	public float tend  =undef;	// end date of integration of float (in s)
								// Note: if tend=-1 floats are integrated till the end of the integration
}
