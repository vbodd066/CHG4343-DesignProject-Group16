import java.text.DecimalFormat;

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

        DecimalFormat df = new DecimalFormat("0.00"); // format decimals

        //========================================================================================================
        //              This is where the main code goes and we can organize all the print statements
        //========================================================================================================

        // Define the fluid properties for both streams and the geometry of the heat exchanger
        Fluid_Properties Process = new Fluid_Properties(0.00164, 1040, 2.5, 25, 3600, 0.44,0.0002);
        Fluid_Properties HotWater = new Fluid_Properties(0.00038, 975, 2.0, 90, 4200, 0.66,0.0001 );
        HeatExchanger_Geometry exchanger1 = new HeatExchanger_Geometry(3,6,0.053,0.06,0.102,"Counter-Current");

        // Create out exchanger for the desired base case
        Complete_Exchanger baseCase = new Complete_Exchanger(exchanger1,Process,HotWater);
        Cost_estimation pricing = new Cost_estimation(baseCase, 1000);

        // Calculate Heat Transfer rate
        double q = baseCase.calculate_q(Process,HotWater);
        System.out.println("====================================================================");
        System.out.println(" Base Case: The Heat Transfer Rate (q) is: " + df.format(q) + " (W)");

        // Find the outlet temperatures for both streams
        double[] t_out = baseCase.calculate_t_out(Process,HotWater);
        System.out.println("====================================================================");
        System.out.println(" The outlet temperatures from the heat exchanger are: ");
        System.out.println(" Process Stream: " + df.format(t_out[0]) + " (ºC)");
        System.out.println(" Hot Water Stream: " + df.format(t_out[1]) + " (ºC)");

        // Calculate pressure drop
        double[] del_P = baseCase.calculate_P();
        System.out.println(" ====================================================================");
        System.out.println("The total pressure drop across the heat exchanger is: ");
        System.out.println("The Inner stream: " + df.format(del_P[0]) + " (Pa)");
        System.out.println("The Annular stream: " + df.format(del_P[1]) + " (Pa)");

        // Calculate the costs
        double cost = pricing.c_annual();
        System.out.println("====================================================================");
        System.out.println(" Base Case: total annual cost is: " + df.format(cost) + " ($ CAD)");

        // this script gets t_out and del_P as arrays where the first index is the Inner stream, and the second index is the Annular stream, if you swap the process and hot water then the print statements will no longer be correct












    }
}