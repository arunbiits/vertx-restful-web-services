package io.arun.learning.vertx;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.arun.learning.vertx.model.Employee;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class App {
	
	private static List<Employee> employees = new ArrayList<Employee>();
    private static AtomicInteger id = new AtomicInteger(100);
    
    static {
    	employees.add(new Employee(id.getAndIncrement(), "Arun", "Sales", 5000D));
    	employees.add(new Employee(id.getAndIncrement(), "Vijay", "Dev", 3000D));
    	employees.add(new Employee(id.getAndIncrement(), "Kumar", "Test", 7000D));
    }
    
    public static boolean addEmployee(Employee emp) {
    	if(emp.getId() == null) {
    		emp.setId(id.incrementAndGet());
    	}
    	return employees.add(emp);
    }
    
    public static Employee updateEmployee(Employee emp) {
    	for(Employee e:employees) {
    		if(e.getId().equals(emp.getId())) {
    			int index = employees.indexOf(e);
    			employees.set(index, emp);
    			break;
    		}
    	}
    	return findById(emp.getId());
    }
    
    public static Employee findById(Integer id) {
    	for(Employee e: employees) {
    		if(e.getId().equals(id)) {
    			return e;
    		}
    	}
    	return null;
    }
    
    public static boolean deleteById(Integer id) {
    	for(Employee e: employees) {
    		if(e.getId().equals(id)) {
    			employees.remove(e);
    			break;
    		}
    	}
    	return findById(id) == null ? true : false;
    }
    
    public static List<Employee> getEmployees(){
    	return employees;
    }
    
	public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        HttpServer httpServer = vertx.createHttpServer();
        Router router = Router.router(vertx);
        
        /**
         * GET request to get the status of the server
         */
        Route status = router
        	.get("/status")
        	.produces("*/json")
        	.handler(routingContext -> {
        		routingContext.response().end("Server is UP and Running...");
        	});
        
        /**
         * POST request to create an employee
         */
        Route postEmployee = router
        	.post("/employees")
        	.handler(BodyHandler.create())
        	.handler(routingContext -> {
        		final Employee employee = Json.decodeValue(routingContext.getBody(),Employee.class);
        		HttpServerResponse response = routingContext.response();
        		response.setChunked(true);
        		response.end(Json.encodePrettily(addEmployee(employee) ? "Employee added successfully!":"Employee could not be added!"));
        	});
        
        /**
         * PUT request to update an existing employee
         */
        Route putEmployee = router
        	.put("/employees")
        	.handler(BodyHandler.create())
        	.handler(routingContext -> {
        		final Employee employee = Json.decodeValue(routingContext.getBody(),Employee.class);
        		HttpServerResponse response = routingContext.response();
        		response.setChunked(true);
        		response.end(Json.encodePrettily(updateEmployee(employee)));
        	});
        
        /**
         * GET request to get all the employees
         */
        Route getEmployees = router
        	.get("/employees")
        	.produces("*/json")
        	.handler(routingContext -> {
        		routingContext.response().setChunked(true).end(Json.encodePrettily(getEmployees()));
        	});
        
        /**
         * GET request to get employee by id
         */
        Route getEmployeeById = router
        	.get("/employees/:id")
        	.produces("*/json")
        	.handler(routingContext -> {
        		int id = Integer.valueOf(routingContext.request().getParam("id"));
        		routingContext
        			.response()
        			.setChunked(true)
        			.end(Json.encodePrettily(findById(id)));
        	});
        
        /**
         * DELETE request to delete employee by id
         */
        Route deleteEmployeeById = router
        	.delete("/employees/:id")
        	.produces("*/json")
        	.handler(routingContext -> {
        		int id = Integer.valueOf(routingContext.request().getParam("id"));
        		routingContext
        			.response()
        			.setChunked(true)
        			.end(Json.encodePrettily(deleteById(id)?"Employee deleted Successfully":"Employee could not be deleted!"));
        	});
        
        httpServer.requestHandler(router).listen(8888);
    }
}
