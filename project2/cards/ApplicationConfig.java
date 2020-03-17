package cards;

import java.util.Set;
import javax.ws.rs.core.Application;

@javax.ws.rs.ApplicationPath("resources")

public class ApplicationConfig extends Application{

   @Override
   public Set<Class<?>> getClasses(){
      return getRestResourceClasses();
   }
   
   private Set<Class<?>> getRestResourceClasses(){
      Set<Class<?>> resources = new java.util.HashSet<Class<?>>();
      resources.add(cards.Service.class);
      return resources;
   }

}