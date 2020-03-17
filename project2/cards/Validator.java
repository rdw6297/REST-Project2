package cards;

import java.util.*;

public class Validator{
   public boolean deptNoUnique(String dept_no, ArrayList<String> deptNumbers){
      int matchCounter = 0;
      for (String dno:deptNumbers){
         if (dno == dept_no){
            matchCounter++;
         }
      }
      //if this deptNo appeared more then once in the same company return false to fail validation
      return (matchCounter > 1) ? false : true;
   }
   
   public boolean mngExists(int mngId, ArrayList<Integer> mngNumbers){
      if (mngId == 0){
         //For the first employee or for employees with no manager 0 is used.
         return true;
      }
      for(Integer mn:mngNumbers){
         if (mn == mngId){
            //the manager exists
            return true;
         }
      }
      return false;
   }
   
   public boolean hireDateValid(long date){
      //date is in the format of secods since 1970
      Date now = new Date();
      if (date > now.getTime()){
         return false;
      } else{
         return true;
      }
   }
   
   
   
   
}