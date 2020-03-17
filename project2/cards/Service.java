package cards;

import companydata.*; 
import javax.ws.rs.core.*;
import javax.ws.rs.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.*;
import javax.json.*;
import javax.json.stream.*;
import javax.json.stream.JsonParser.*;
import java.text.SimpleDateFormat;

@Path("CompanyServices")
public class Service{

   @Context
   UriInfo uriInfo;
   SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
   SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
   Validator val = new Validator();

   @Path("company")
   @DELETE
   @Produces("application/json")
   public String delCompany(@QueryParam("company") String CompanyName){
      //deletes all depts,employ, timecards.
      DataLayer dl = null;

      try{
         dl = new DataLayer(CompanyName);
         int rowsDeleted = dl.deleteCompany(CompanyName);
         JsonObject json = Json.createObjectBuilder()
            .add("success", CompanyName + "'s information deleted rows("+rowsDeleted+")")
            .build();
         return json.toString();
      } catch(Exception e){
         //System.out.println("Could not delete company" + e.getMessage());
         JsonObject json = Json.createObjectBuilder()
            .add("Error", "Could not delete company" + e.getMessage())
            .build();
         return json.toString();
      } finally{
         dl.close();
      }
   }
   
   @Path("department")
   @GET
   @Produces("application/json")
   public String getDepartment(@QueryParam("company") String CompanyName, 
      @QueryParam("dept_id") int deptID){
      
      DataLayer dl = null;

      try{
         dl = new DataLayer(CompanyName);
         Department dept = dl.getDepartment(CompanyName, deptID);
         
         JsonObject json = Json.createObjectBuilder()
            .add("dept_id", dept.getId())
            .add("company", dept.getCompany())
            .add("dept_name", dept.getDeptName())
            .add("dept_no", dept.getDeptNo())
            .add("location", dept.getLocation())
            .build();
        return json.toString();
      } catch (Exception e){
         JsonObject json = Json.createObjectBuilder()
            .add("Error", "Could not find the Department - " + e.getMessage())
            .build();
         return json.toString();
      } finally{
         dl.close();
      }
   }
   
   @Path("departments")
   @GET
   @Produces("application/json")
   public String getDepartments(@QueryParam("company") String CompanyName){
      //get list of departments as a JSON String
      DataLayer dl = null;
      String jsonString = "[";
      try{
         dl = new DataLayer(CompanyName);
         List<Department> departments = dl.getAllDepartment(CompanyName);
        
         for(Department dept : departments ){  
            JsonObject json = Json.createObjectBuilder()	  
               .add("dept_id", dept.getId())
               .add("company", dept.getCompany())
               .add("dept_name", dept.getDeptName())
               .add("dept_no", dept.getDeptNo())
               .add("location", dept.getLocation())
               .build();
            jsonString += json.toString() + ",";
         } 
         jsonString = jsonString.substring(0,jsonString.length()-1);
         jsonString += "]";
         return jsonString;
      } catch (Exception e){
         JsonObject json = Json.createObjectBuilder()
            .add("Error", "Could not retrive Department list" + e.getMessage())
            .build();
         return json.toString();
      }
      
   }
   
   @Path("department")
   @PUT
   @Produces("application/json")
   @Consumes("application/json")
   public String updateDepartment(String input){

      DataLayer dl = null;
      try{
         JsonReader jsonReader = Json.createReader(new StringReader(input));
         JsonObject jsonIn = jsonReader.readObject();
         jsonReader.close();
         dl = new DataLayer(jsonIn.getString("company"));
         
         //validation
         if (!(val.deptNoUnique(jsonIn.getString("dept_no"), getDeptNoList(jsonIn.getString("company"))))){
            //val returned false, dept_no is not unique
            throw new Exception("dept_no is not unique");
         }
         if ((getDepartment(jsonIn.getString("company"),jsonIn.getInt("dept_id")).contains("Error"))){
            //getDepartment contains a error, dept may not exist and put will fail
            throw new Exception("dept_id does not exist or is invalid");
         }
         
         Department dept = dl.getDepartment(jsonIn.getString("company"),jsonIn.getInt("dept_id"));
         
         if (!(jsonIn.getString("dept_name").isEmpty())){
            dept.setDeptName(jsonIn.getString("dept_name"));
         }
         if (!(jsonIn.getString("dept_no").isEmpty())){
            dept.setDeptNo(jsonIn.getString("dept_no"));
         }
         if (!(jsonIn.getString("location").isEmpty())){
            dept.setLocation(jsonIn.getString("location"));
         }
         dept = dl.updateDepartment(dept);
         
         //return updated department
         return getDepartment(jsonIn.getString("company"),jsonIn.getInt("dept_id"));
      } catch(Exception e){
         JsonObject json = Json.createObjectBuilder()
            .add("Error", "Could not update Department" + e.getMessage())
            .build();
         return json.toString();
      }    
   }
   
   @Path("department")
   @POST
   @Produces("application/json")
   //This is a new department
   public String setDepartment(@FormParam("company") String CompanyName, 
      @FormParam("dept_name") String deptName, @FormParam("dept_no") String deptNo,
      @FormParam("location") String location){
         
      DataLayer dl = null;
      try{
         dl = new DataLayer(CompanyName);
         
         //validation
         if (!(val.deptNoUnique(deptNo, getDeptNoList(CompanyName)))){
            //val returned false, dept_no is not unique
            throw new Exception("dept_no is not unique");
         }
      
         //make a new department
         Department dept = new Department(CompanyName,deptName,deptNo,location);
         dept = dl.insertDepartment(dept);
         if (dept.getId() > 0) {
            return getDepartment(dept.getCompany(),dept.getId());
         } else {
            JsonObject json = Json.createObjectBuilder()
            .add("error", deptName + " was unable to be inserted")
            .build();
            return json.toString();
         }
      } catch(Exception e){
         JsonObject json = Json.createObjectBuilder()
            .add("Error", "Could not create company" + e.getMessage()).build();
         return json.toString();
      } finally{
         dl.close();
      }
      
   }
   
   @Path("department")
   @DELETE
   @Produces("application/json")
   public String deleteDepartment(@QueryParam("company") String CompanyName, 
      @QueryParam("dept_id") int deptId){
      
      //return numnber of rows deleted
      DataLayer dl = null;

      try{
         dl = new DataLayer(CompanyName);
         int rowsDeleted = dl.deleteDepartment(CompanyName,deptId);
         if (rowsDeleted >= 1) {
            JsonObject json = Json.createObjectBuilder()
               .add("success", "Department "+deptId+" was deleted, rows deleted: " + rowsDeleted)
               .build();
            return json.toString();
         } else {
            JsonObject json = Json.createObjectBuilder()
               .add("error", "Department "+deptId+" was not able to be deleted")
               .build();
            return json.toString();
         }
      } catch(Exception e){
         JsonObject json = Json.createObjectBuilder()
            .add("Error", "Unable to delete department" + e.getMessage())
            .build();
         return json.toString();
      } finally{
         dl.close();
      }
      
   }
   
   @Path("employee")
   @GET
   @Produces("application/json")
   public String getEmployee(@QueryParam("company") String CompanyName, 
      @QueryParam("emp_id") int empId){
      
      DataLayer dl = null;
      //get dept info as a JSON String
      try{
         dl = new DataLayer(CompanyName);
         Employee emp = dl.getEmployee(empId);
         
         JsonObject json = Json.createObjectBuilder()
            .add("emp_id", emp.getId())
            .add("emp_name", emp.getEmpName())
            .add("emp_no", emp.getEmpNo())
            .add("hire_date", emp.getHireDate().toString())
            .add("job", emp.getJob())
            .add("salary", emp.getSalary())
            .add("dept_id", emp.getDeptId())
            .add("mng_id", emp.getMngId())
            .build();
        return json.toString();
      } catch (Exception e){
         JsonObject json = Json.createObjectBuilder()
            .add("Error", "Could not find the Employee" + e.getMessage())
            .build();
         return json.toString();
      } finally{
         dl.close();
      }
   }
   
   @Path("employees")
   @GET
   @Produces("application/json")
   public String getEmployees(@QueryParam("company") String CompanyName){
      
      DataLayer dl = null;
      String jsonString = "[";

      try{
         dl = new DataLayer(CompanyName);
         List<Employee> Employees = dl.getAllEmployee(CompanyName);
        
         for(Employee emp : Employees ){  
            JsonObject json = Json.createObjectBuilder()	  
               .add("emp_id", emp.getId())
               .add("emp_name", emp.getEmpName())
               .add("emp_no", emp.getEmpNo())
               .add("hire_date", emp.getHireDate().toString())
               .add("job", emp.getJob())
               .add("salary", emp.getSalary())
               .add("dept_id", emp.getDeptId())
               .add("mng_id", emp.getMngId())
               .build();
            jsonString += json.toString() + ",";
         } 
         jsonString = jsonString.substring(0,jsonString.length()-1);
         jsonString += "]";
         return jsonString;
      } catch (Exception e){
         JsonObject json = Json.createObjectBuilder()
            .add("Error", "Could not get the list of employees" + e.getMessage())
            .build();
         return json.toString();
      } finally{
         dl.close();
      }
   }
   
   @Path("employee")
   @POST
   @Produces("application/json")
   public String setEmployee(@FormParam("company") String CompanyName, 
      @FormParam("emp_name") String empName, @FormParam("emp_no") String empNo,
      @FormParam("hire_date") String hireDate, @FormParam("job") String job,
      @FormParam("salary") double salary, @FormParam("dept_id") int deptId,
      @FormParam("mng_id") int mngId){

      DataLayer dl = null;

      try{
         dl = new DataLayer(CompanyName);
         //convert date to proper "Date" format.
         Date d = df.parse(hireDate);
         
         //validation
         //comapny - regex 3letters4numbers
         if ((getDepartment(CompanyName,deptId).contains("Error"))){
            //getDepartment contains a error, dept may not exist and put will fail
            throw new Exception("dept_id does not exist or is invalid");
         }
         if (!(val.mngExists(mngId,getManList(CompanyName)))){
            throw new Exception("Manager does no exist");
         }
         if (!(val.hireDateValid(d.getTime()))){
            throw new Exception("Hire date is invalid");
         }
         
         
         //make a new employee
         //Created a date object from the string user input and will create a sql date.
         Employee emp = new Employee(empName,empNo,new java.sql.Date(d.getTime()),
            job,salary,deptId,mngId);
         emp = dl.insertEmployee(emp);
         if (emp.getId() > 0) {
            //return new employee info as JSON
            return getEmployee(CompanyName,emp.getId());
         } else {
            JsonObject json = Json.createObjectBuilder()
            .add("error", empName + " was unable to be created")
            .build();
            return json.toString();
         }
      } catch(Exception e){
         JsonObject json = Json.createObjectBuilder()
            .add("Error", "Could not create new employee" + e.getMessage()).build();
         return json.toString();
      } finally{
         dl.close();
      }
   }
   
   @Path("employee")
   @PUT
   @Produces("application/json")
   @Consumes("application/json")
   public String updateEmployee(String input){
      //input company name, employeeID, and any modified values
      
      //sanatize
      
      //edit existing employee
   
      //return updated employee
      DataLayer dl = null;
      //sanatize 

      try{
         JsonReader jsonReader = Json.createReader(new StringReader(input));
         JsonObject jsonIn = jsonReader.readObject();
         jsonReader.close();
         dl = new DataLayer(jsonIn.getString("company"));
         Employee emp = dl.getEmployee(jsonIn.getInt("emp_id"));
         
         if (!(jsonIn.getString("emp_name").isEmpty())){
            emp.setEmpName(jsonIn.getString("emp_name"));
         }
         if (!(jsonIn.getString("emp_no").isEmpty())){
            emp.setEmpNo(jsonIn.getString("emp_no"));
         }
         if (!(jsonIn.getString("hire_date").isEmpty())){
            Date d = df.parse(jsonIn.getString("hire_date"));
            emp.setHireDate(new java.sql.Date(d.getTime()));
         }
         if (!(jsonIn.getString("job").isEmpty())){
            emp.setJob(jsonIn.getString("job"));
         }
         if (!(jsonIn.getString("salary").isEmpty())){
            emp.setSalary(Double.parseDouble(jsonIn.getString("salary")));
         }
         if (jsonIn.getInt("dept_id") > 0){
            emp.setDeptId(jsonIn.getInt("dept_id"));
         }
         if (jsonIn.getInt("mng_id") > 0){
            emp.setMngId(jsonIn.getInt("mng_id"));
         }
         emp = dl.updateEmployee(emp);
         
         return getEmployee(jsonIn.getString("company"), emp.getId());
      } catch(Exception e){
         JsonObject json = Json.createObjectBuilder()
            .add("Error", "" + e.getMessage()).build();
         return json.toString();
      } finally{
         dl.close();
      }
   }
   
   @Path("employee")
   @DELETE
   @Produces("application/json")
   public String deleteEmployee(@QueryParam("company") String companyName, 
      @QueryParam("emp_id") int empId){
      
      //sanatize
   
      //return that employee was deleted
      DataLayer dl = null;
      //sanatize 

      try{
         dl = new DataLayer(companyName);
         int rowsDeleted = dl.deleteEmployee(empId);
         if (rowsDeleted > 0) {
            JsonObject json = Json.createObjectBuilder()
            .add("success", "Employee number "+empId+" was deleted, rows deleted: "+ rowsDeleted)
            .build();
            return json.toString();
         } else {
            JsonObject json = Json.createObjectBuilder()
            .add("error", "Employee ("+empId+") failed to be deleted")
            .build();
            return json.toString();
         }
      } catch(Exception e){
         JsonObject json = Json.createObjectBuilder()
            .add("Error", "Failed deleted employee" + e.getMessage()).build();
         return json.toString();
      } finally{
         dl.close();
      }
   }
   
   @Path("timecard")
   @GET
   @Produces("application/json")
   public String getTimecard(@QueryParam("company") String CompanyName, 
      @QueryParam("timecard_id") int cardId){
      //input company and timecardID as QueryParam
      
      //validate
      
      //sanatize
   
      //return timecard as JSON
      DataLayer dl = null;
      //sanatize 

      try{
         dl = new DataLayer(CompanyName);
         Timecard timecard = dl.getTimecard(cardId);
         
         JsonObject json = Json.createObjectBuilder()
            .add("timecard_id", timecard.getId())
            .add("start_time", timecard.getStartTime().toString())
            .add("end_time", timecard.getEndTime().toString())
            .add("emp_id", timecard.getEmpId())
            .build();
        return json.toString();
      } catch (Exception e){
         JsonObject json = Json.createObjectBuilder()
            .add("Error", "Could not find the timecard" + e.getMessage())
            .build();
         return json.toString();
      } finally{
         dl.close();
      }
   }
   
   @Path("timecards")
   @GET
   @Produces("application/json")
   public String getTimecards(@QueryParam("company") String CompanyName,
      @QueryParam("ii.	emp_id") int empId){
      //input employeeID QueryParam
      
      //validate
      
      //sanatize
      
   
      //return list timecards for that employee
      DataLayer dl = null;
      //sanatize 

      String jsonString = "[";
      try{
         dl = new DataLayer(CompanyName);
         List<Timecard> timecards = dl.getAllTimecard(empId);
        
         for(Timecard timecard : timecards ){  
            JsonObject json = Json.createObjectBuilder()
               .add("timecard_id", timecard.getId())
               .add("start_time", timecard.getStartTime().toString())
               .add("end_time", timecard.getEndTime().toString())
               .add("emp_id", timecard.getEmpId())
               .build();
            jsonString += json.toString() + ",";
         } 
         jsonString = jsonString.substring(0,jsonString.length()-1);
         jsonString += "]";
         return jsonString;
      } catch (Exception e){
         JsonObject json = Json.createObjectBuilder()
            .add("Error", "Could not retrive timecard list" + e.getMessage())
            .build();
         return json.toString();
      }finally{
         dl.close();
      }
   }
   
   @Path("timecard")
   @POST
   @Produces("application/json")
   @Consumes("application/json")
   public String setTimecard(@FormParam("company") String CompanyName, 
      @FormParam("emp_id") int empId,@FormParam("start_time") String start, 
      @FormParam("end_time") String end){
      //input timecard values as FormParams
      
      //validate++
      
      //sanatize
      
   
      //return new timecard as JSON
      DataLayer dl = null;
      //sanatize 

      try{
         dl = new DataLayer(CompanyName);
         
         Date sd = sdf.parse(start);
         java.sql.Timestamp startTime = new java.sql.Timestamp(sd.getTime());
         Date ed = sdf.parse(end);
         java.sql.Timestamp endTime = new java.sql.Timestamp(ed.getTime());
         Timecard tc = new Timecard(startTime,endTime,empId);        
         tc = dl.insertTimecard(tc);
         if (tc.getId() > 0) {
            return getTimecard(CompanyName, empId);
         } else {
            JsonObject json = Json.createObjectBuilder()
               .add("error", "Could not add timecard")
               .build();
            return json.toString();
         }
         
      } catch(Exception e){
         JsonObject json = Json.createObjectBuilder()
            .add("Error", "could not get timecard " + e.getMessage()).build();
         return json.toString();
      } finally{
         dl.close();
      }
   }
   
   @Path("timecard")
   @PUT
   @Produces("application/json")
   public String updateTimecard(String input){
      //input timecard values as FormParams
      
      //validate++
      
      //sanatize
      
   
      //return updated timecard as JSON
      DataLayer dl = null;
      //sanatize 

      try{
         JsonReader jsonReader = Json.createReader(new StringReader(input));
         JsonObject jsonIn = jsonReader.readObject();
         jsonReader.close();
         dl = new DataLayer(jsonIn.getString("company"));
         Timecard timecard = dl.getTimecard(jsonIn.getInt("timecard_id"));
         
         if (!(jsonIn.getString("start_time").isEmpty())){
            Date sd = sdf.parse(jsonIn.getString("start_time"));
            java.sql.Timestamp startTime = new java.sql.Timestamp(sd.getTime());
            timecard.setStartTime(startTime);
         }
         if (!(jsonIn.getString("end_time").isEmpty())){
            Date ed = sdf.parse(jsonIn.getString("end_time"));
            java.sql.Timestamp endTime = new java.sql.Timestamp(ed.getTime());
            timecard.setEndTime(endTime);
         }

         //no ability to change emp_id

         timecard = dl.insertTimecard(timecard);
         if (timecard.getId() > 0) {
            return getTimecard(jsonIn.getString("company"), jsonIn.getInt("emp_id"));
         } else {
            JsonObject json = Json.createObjectBuilder()
               .add("Error", "Unable to insert timecard").build();
            return json.toString();
         }
         
      } catch(Exception e){
         JsonObject json = Json.createObjectBuilder()
            .add("Error", "" + e.getMessage()).build();
         return json.toString();
      } finally{
         dl.close();
      }
   }
   
   @Path("timecard")
   @DELETE
   @Produces("application/json")
   public String deleteTimecard(@QueryParam("company") String companyName, 
      @QueryParam("timecard_id") int cardId){
      //input timecardID as QueryParam
      
      //validate company name and timecardID
      
      //sanatize
      
   
      //return number of rows deleted
      DataLayer dl = null;
      //sanatize 

      try{
         dl = new DataLayer(companyName);
         int rowsDeleted = dl.deleteTimecard(cardId);
         if (rowsDeleted > 0) {
            JsonObject json = Json.createObjectBuilder()
            .add("success", "timecard was deleted, rows deleted: "+ rowsDeleted)
            .build();
            return json.toString();
         } else {
            JsonObject json = Json.createObjectBuilder()
            .add("error", "timecard failed to be deleted")
            .build();
            return json.toString();
         }
      } catch(Exception e){
         JsonObject json = Json.createObjectBuilder()
            .add("Error", "Failed to delete timecard" + e.getMessage()).build();
         return json.toString();
      } finally{
         dl.close();
      }
   }
   
   //the following methods are not public and dont have signatures. They are here to help with
   //getting data for validation and keeping data layer access out of the validator class.
   
   private ArrayList<String> getDeptNoList(String CompanyName){
      //get list of department numbers for validation
      DataLayer dl = null;
      ArrayList<String> deptNoList = new ArrayList<String>();
      try{
         dl = new DataLayer(CompanyName);
         List<Department> departments = dl.getAllDepartment(CompanyName);
        
         for(Department dept : departments ){  
            deptNoList.add(dept.getDeptNo());
         } 
         return deptNoList;
      } catch (Exception e){
         //die silently
      }
      return null;
   }
   
   private ArrayList<Integer> getManList(String CompanyName){
      //get list of department numbers for validation
      DataLayer dl = null;
      ArrayList<Integer> manNoList = new ArrayList<Integer>();
      try{
         dl = new DataLayer(CompanyName);
         List<Employee> employees = dl.getAllEmployee(CompanyName);
        
         for(Employee emp : employees ){  
            manNoList.add(emp.getMngId());
         } 
         return manNoList;
      } catch (Exception e){
         //die silently
      }
      return null;
   }

   
}