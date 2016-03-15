/*  this class holds various global constants
    for the engine---anything that involves more
    than one class, or has an independently
    explainable meaning

    For example, collTol (the collision envelope size)
    is the same for all
    pairs of bodies, so it's here, whereas the
    gravity vector is set for individual bodies
    because it can vary for certain effects (like
    things that don't fall---platforms, terrain)

    For a specific game, these constants may need to be
    adjusted by the game programmer
*/

public class Constants
{
  // the collision envelope distance---when we try to
  // advance time until a touch, we actually shoot for
  // the closest points to be this far apart
  // (actually in [collTolLow,collTolHigh] )
  public static double collTol = .0001;
  public static double collTolLow = 0.5*collTol;
  public static double collTolHigh = 1.5*collTol;

  // constants for advanceTime in SeparationTracker
  public static double fdeps = 0.0000001; // for finite difference derivative
  public static double initTrustRadiusFraction = 0.1; // 1/10th of total time
  public static double minInitTrustRadius = 0.1;
  public static double acceptableAccuracy = 0.5;
  public static double goodAccuracy = 0.5;
  
}

