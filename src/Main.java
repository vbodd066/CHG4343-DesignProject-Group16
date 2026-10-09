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
        DecimalFormat df_simple = new DecimalFormat("0"); // format decimals

        //========================================================================================================
        //              This is where the main code goes and we can organize all the print statements
        //========================================================================================================

        // Part 1 of deliverable 2:

        // Define the fluid properties for both streams and the geometry of the heat exchanger
        Fluid_Properties process = new Fluid_Properties(0.00164, 1040, 2.5, 25, 3600, 0.44,0.0002);
        Fluid_Properties hotWater = new Fluid_Properties(0.00038, 975, 2.0, 90, 4200, 0.66,0.0001 );
        HeatExchanger_Geometry exchanger1 = new HeatExchanger_Geometry(3,6,0.053,0.06,0.102,"Counter-Current");

        // Create out exchanger for the desired base case
        Complete_Exchanger baseCase = new Complete_Exchanger(exchanger1,process,hotWater);
        Cost_estimation price_baseCase = new Cost_estimation(baseCase, 1000);

        // Calculate Heat Transfer rate
        double q = baseCase.calculate_q(process,hotWater);
        System.out.println("====================================================================");
        System.out.println(" Base Case: The Heat Transfer Rate (q) is: " + df.format(q) + " (W)");

        // Find the outlet temperatures for both streams
        double[] t_out = baseCase.calculate_t_out(process,hotWater);
        System.out.println("====================================================================");
        System.out.println(" The outlet temperatures from the heat exchanger are: ");
        System.out.println(" Process Stream: " + df.format(t_out[0]) + " (ºC)");
        System.out.println(" Hot Water Stream: " + df.format(t_out[1]) + " (ºC)");

        // Calculate pressure drop
        double[] del_P = baseCase.calculate_P();
        System.out.println("====================================================================");
        System.out.println("The total pressure drop across the heat exchanger is: ");
        System.out.println("The Inner stream: " + df_simple.format(del_P[0]) + " (Pa)");
        System.out.println("The Annular stream: " + df_simple.format(del_P[1]) + " (Pa)");

        // Calculate the costs
        double cost = price_baseCase.c_annual();
        System.out.println("====================================================================");
        System.out.println(" Base Case: total annual cost is: " + df.format(cost) + " ($ CAD)");

        // Part 2 of deliverable 2:

        // create NLE_solver object with our baseCase exchanger
        boolean hotWaterInAnnulus = true;
        NLE_Solver targetProcessTemp = new NLE_Solver(exchanger1,process,hotWater,hotWaterInAnnulus,45,0.001,100 );

        // find the required mass flow rate to acheive the outlet temperature
        double[] M_flow_required = new double[2];
        M_flow_required = targetProcessTemp.calculateM_flow_required();
        if (M_flow_required == null){
            System.out.println("====================================================================");
            System.out.println(" A hot water mass flow rate could not be found within the bounds to ");
            System.out.println(" satisfy the process stream outlet temperature of " + df_simple.format(targetProcessTemp.getT_out_desired()) + "ºC.");
        }
        else {
            System.out.println("====================================================================");
            System.out.println(" The mass flow rate of hot water required to achieve a process ");
            System.out.println(" stream outlet temperature of " + df_simple.format(targetProcessTemp.getT_out_desired()) +"ºC is: " + df.format(M_flow_required[0]) + "+/-" + df.format(M_flow_required[1]) + " (kg/s)");
        }









    }
}