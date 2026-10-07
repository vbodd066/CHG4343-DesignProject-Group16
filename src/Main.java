public class Main {

    public static void main(String[] args) {
        // The program must calculate and print the heat transfer rate, the outlet temperature of each stream,
        // the pressure drop for each stream, and the total annual cost. The program can be a single class
        // containing static methods. The inputs can be hard coded.

        // Write a nonlinear equation (NLE) solver in Java that calculates and prints the flow rate of hot water
        // required for a process outlet temperature of 45 °C. The solver should call the heat exchanger
        // calculation methods written for task 1. The solver should use incremental search to find the bounds of
        // the root and Ridders’ method to calculate the flow rate.

        // Assume the hot water flow rate available is between 0.5 kilograms per second and 8 kilograms per second.
        // Your NLE should stop when the absolute residual is 0.001 degrees Celsius or less, or at 100 iterations.
        // If all your calculations are correct, your NLE should suggest a hot water flow of approximately 1.3
        // kilograms per second.

        //========================================================================================================
        //              This is where the main code goes and we can organize all the print statements
        //========================================================================================================

        // Define the fluid properties for both streams and the geometry fo the heat exchanger
        Fluid_Properties Process = new Fluid_Properties(0.00164, 1040, 2.5, 25, 3600, 0.44,0.0002);
        Fluid_Properties HotWater = new Fluid_Properties(0.00038, 975, 2.0, 90, 4200, 0.66,0.0001 );
        HeatExchanger_Geometry exchanger1 = new HeatExchanger_Geometry(3,6,0.053,0.06,0.102,"Counter-Current", "Inner");

        // Calculate Heat Transfer rate

        // Find the outlet temperatures for both streams

        // Calculate pressure drop

        // Calculate the costs














    }
}