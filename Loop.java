import java.util.Arrays;
import java.util.List;

public class Loop{

     public static void main(String[] args) {
	    //Array of numbers:
	    int[] arr = new int[] { 0,1,2,3,4,5,6,7,8,9 };
		
		//loop that outputs array of numbers
		System.out.print("Array: ");
		for (int i = 0; i < arr.length; i++) {
			System.out.print(arr[i]+" ");
			
		}
		
		//loop that outputs array of evens only
		System.out.println();
		System.out.print("Evens: ");
		for(int i = 0; i < arr.length; i++) {
		    if(arr[i] % 2 == 0) {
		        System.out.print(arr[i]+" ");
		    }
		}
		

     }
}